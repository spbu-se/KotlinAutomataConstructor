/*
MIT License

Copyright (c) 2021 Ilya Muravjov (https://github.com/IlyaMuravjov), Egor Denisov (https://github.com/Lev0nid), Timofey Zaynulin (https://github.com/Tizain)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package automaton.constructor.model.module.layout.dynamic

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.module.layout.dynamic.DynamicLayoutPolicy.*
import tornadofx.Vector2D
import tornadofx.plus
import tornadofx.times
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random

private val dynamicLayoutFactory: (Automaton) -> DynamicLayout =
    { automaton: Automaton -> ForceAtlas2Layout(automaton) }
val Automaton.dynamicLayout get() = getModule(dynamicLayoutFactory)

class ForceAtlas2Layout(val automaton: Automaton) : DynamicLayout {
    private var temperature = 1.0
    private var temperatureEfficiency = 1.0
    private var fa2Vertices = setOf<ForceAtlas2Vertex>()
    private var fa2VerticesToLayout = setOf<ForceAtlas2Vertex>()
    private var fa2Edges = setOf<ForceAtlas2Edge>()
    private val vertexToFA2Vertex = WeakHashMap<AutomatonVertex, ForceAtlas2Vertex>()
    private val props = ForceAtlas2Props(automaton.vertices.size)

    private fun AutomatonVertex.shouldBeLayout(policy: DynamicLayoutPolicy) = !forceNoLayout &&
            when (policy) {
                LAYOUT_ALL -> true
                LAYOUT_REQUIRING -> requiresLayout
                LAYOUT_NONE -> false
            }

    override fun sync(policy: DynamicLayoutPolicy) {
        fa2Vertices = automaton.vertices.map { vertex ->
            vertexToFA2Vertex.getOrPut(vertex) { ForceAtlas2Vertex(vertex.position + randomVector(), 2.5 * AutomatonVertex.RADIUS) }
                .also { fa2Vertex ->
                    if (vertex.shouldBeLayout(policy)) {
                        if (vertex.position.distance(fa2Vertex.pos) > 1.0)
                            vertex.position = fa2Vertex.pos
                    }
                    else fa2Vertex.pos = vertex.position
                }
        }.toSet().onEach { it.mass = 1.0 }
        fa2VerticesToLayout =
            automaton.vertices.filter { it.shouldBeLayout(policy) }.mapNotNull { vertexToFA2Vertex[it] }.toSet()
        fa2Edges = automaton.transitions.map {
            ForceAtlas2Edge(vertexToFA2Vertex.getValue(it.source), vertexToFA2Vertex.getValue(it.target))
        }.toSet().onEach {
            it.from.mass++
            it.to.mass++
        }
    }

    private val minEdgeLengthsOverLastIterations = ArrayDeque<Double>()

    private fun adjustScaling() {
        if (fa2VerticesToLayout.size * 2 < automaton.vertices.size) return
        minEdgeLengthsOverLastIterations.addLast(
            fa2Edges
                .filter { it.from.pos != it.to.pos && it.from in fa2VerticesToLayout && it.to in fa2VerticesToLayout }
                .minOfOrNull { it.from.pos.distance(it.to.pos) } ?: return
        )
        if (minEdgeLengthsOverLastIterations.size > 50) minEdgeLengthsOverLastIterations.removeFirst()
        if (minEdgeLengthsOverLastIterations.maxOrNull()!! < 4.0 * AutomatonVertex.RADIUS) props.scaling *= 1.01
        else if (minEdgeLengthsOverLastIterations.maxOrNull()!! > 12.0 * AutomatonVertex.RADIUS) props.scaling /= 1.01
    }

    private fun stop() {
        temperature = 1.0
        temperatureEfficiency = 1.0
        fa2Vertices = emptySet()
        fa2Edges = emptySet()
        vertexToFA2Vertex.clear()
    }

    override fun step(policy: DynamicLayoutPolicy) {
        if (fa2VerticesToLayout.isEmpty()) {
            stop()
            return
        }
        adjustScaling()
        fa2Vertices.forEach {
            it.oldVelocity = it.velocity
            it.velocity = Vector2D.ZERO
        }
        val posGroups = fa2Vertices.groupBy { it.pos }
        fa2Vertices.forEach {
            if ((posGroups[it.pos]?.size ?: 0) > 1)
                it.pos += randomVector()
        }
        val attraction = buildAttraction(
            type = props.attractionType,
            dissuadeHubs = props.dissuadeHubs,
            preventOverlap = props.preventOverlap,
            coefficient = if (props.dissuadeHubs) fa2Edges.size / (10.0 * fa2Vertices.size) else 1.0,
        )
        val repulsion = buildRepulsion(
            preventOverlap = props.preventOverlap,
            coefficient = props.scaling
        )
        val gravity = buildGravity(
            isStrong = props.strongGravity,
            coefficient = props.gravityCoefficient / props.scaling
        )
        val optimized = fa2VerticesToLayout.size > 1000
        val rootRegion = if (optimized) Region(fa2Vertices) else null
        val vertexStream =
            if (optimized) fa2VerticesToLayout.parallelStream() else fa2VerticesToLayout.stream()
        vertexStream.forEach { vertex ->
            if (optimized) rootRegion!!.repulse(vertex, repulsion, props.barnesHutTheta)
            else fa2Vertices.forEach { otherVertex -> repulsion.apply(vertex, otherVertex) }
            gravity.apply(vertex)
        }
        fa2Edges.forEach { attraction.apply(it) }
        if (fa2VerticesToLayout.all { it.velocity == Vector2D.ZERO }) return
        val totalSwinging = fa2VerticesToLayout.sumOf { it.swinging }
        val totalEffectiveTraction = fa2VerticesToLayout.sumOf { it.effectiveTraction }
        val estimatedOptimalTolerance = 0.05 * sqrt(fa2VerticesToLayout.size.toDouble())
        var tolerance = props.tolerance *
                (estimatedOptimalTolerance * totalEffectiveTraction / fa2VerticesToLayout.size / fa2VerticesToLayout.size)
                    .coerceAtMost(10.0).coerceAtLeast(sqrt(estimatedOptimalTolerance))
        val minTemperatureEfficiency = 0.05
        if (totalSwinging / totalEffectiveTraction > 2.0) {
            if (temperatureEfficiency > minTemperatureEfficiency) temperatureEfficiency *= 0.5
            tolerance = tolerance.coerceAtLeast(props.tolerance)
        }
        val targetTemperature =
            if (totalSwinging == 0.0) temperature else
            tolerance * temperatureEfficiency * totalEffectiveTraction / totalSwinging
        if (totalSwinging > tolerance * totalEffectiveTraction) {
            if (temperatureEfficiency > minTemperatureEfficiency) temperatureEfficiency *= 0.7
        } else if (temperature < 1000) temperatureEfficiency *= 1.3
        temperature = targetTemperature.coerceAtMost(1.5 * temperature)
        fa2VerticesToLayout.forEach { vertex ->
            var factor = temperature / (1.0 + sqrt(temperature * vertex.swinging))
            if (props.preventOverlap) factor *= 0.1
            val topSpeed = min(10.0, max(1.0, vertex.limitedVelocity.magnitude() * 1.2))
            if (vertex.velocity != Vector2D.ZERO)
                factor = factor.coerceAtMost(topSpeed / vertex.velocity.magnitude())
            vertex.limitedVelocity = factor * vertex.velocity
            vertex.pos += vertex.limitedVelocity
        }
    }

    private fun randomVector() = Vector2D(Random.nextDouble(-0.1, 0.1), Random.nextDouble(-0.1, 0.1))
}
