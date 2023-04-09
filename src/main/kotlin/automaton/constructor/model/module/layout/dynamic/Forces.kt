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

import automaton.constructor.model.automaton.GRAPH_PANE_CENTER
import javafx.geometry.Point2D
import tornadofx.Vector2D
import tornadofx.minus
import tornadofx.plus
import tornadofx.times
import kotlin.math.ln1p

fun interface AttractionForce {
    fun apply(edge: ForceAtlas2Edge)
}

enum class AttractionType(val displayName: String) {
    LINEAR("linear"),
    LOGARITHMIC("logarithmic");

    override fun toString() = displayName
}

fun buildAttraction(
    type: AttractionType,
    dissuadeHubs: Boolean,
    preventOverlap: Boolean,
    coefficient: Double,
    edgeWeightExponent: Double
) = AttractionForce { edge ->
    with(edge) {
        val vector = to.pos - from.pos
        var distance = vector.magnitude()
        if (preventOverlap) distance -= from.radius + to.radius
        if (distance > 0) {
            var factor = coefficient
            factor *= when (type) {
                AttractionType.LINEAR -> 1.0
                AttractionType.LOGARITHMIC -> ln1p(distance) / distance
            }
            if (dissuadeHubs) factor /= from.mass
            val force = factor * vector
            from.velocity += force
            to.velocity -= force
        }
    }
}


fun interface RepulsionForce {
    fun apply(vertex: ForceAtlas2Vertex, pos: Point2D, mass: Double, radius: Double)

    fun apply(vertex: ForceAtlas2Vertex, otherVertex: ForceAtlas2Vertex) =
        apply(vertex, otherVertex.pos, otherVertex.mass, otherVertex.radius)

    fun apply(vertex: ForceAtlas2Vertex, pos: Point2D, mass: Double) =
        apply(vertex, pos, mass, -vertex.radius)
}

private const val ON_OVERLAP_REPULSION_MULTIPLIER = 4

fun buildRepulsion(preventOverlap: Boolean, coefficient: Double) = RepulsionForce { vertex, pos, mass, radius ->
    val vector = pos - vertex.pos
    var distance = vector.magnitude()
    if (preventOverlap) distance -= vertex.radius + radius
    var factor = coefficient * vertex.mass * mass
    if (distance > 0.5) factor /= distance * distance
    else factor *= ON_OVERLAP_REPULSION_MULTIPLIER
    vertex.velocity -= factor * vector
}


fun interface Gravity {
    fun apply(vertex: ForceAtlas2Vertex)
}

fun buildGravity(isStrong: Boolean, coefficient: Double) = Gravity { vertex ->
    var factor = coefficient
    val pos = vertex.pos - GRAPH_PANE_CENTER
    if (!isStrong && pos.magnitude() > 1.0) factor /= pos.magnitude()
    vertex.velocity -= factor * Vector2D(pos.x / ASPECT_RATIO, pos.y * ASPECT_RATIO)
}

private const val ASPECT_RATIO = 16.0 / 9.0