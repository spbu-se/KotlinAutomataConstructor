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

import javafx.geometry.Point2D
import tornadofx.div
import tornadofx.plus
import tornadofx.times

class Region(vertices: Collection<ForceAtlas2Vertex>) {
    private val mass = vertices.sumOf { it.mass }
    private val massCenter = vertices.fold(Point2D.ZERO) { acc, v -> acc + v.mass * v.pos } / mass
    private val radius = vertices.maxOfOrNull { massCenter.distance(it.pos) } ?: Double.MIN_VALUE
    private val subregions: Collection<Region> =
        if (vertices.size <= 1 || radius < 1e-4) emptyList()
        else vertices.partition { it.pos.x < massCenter.x }.toList()
            .flatMap { side -> side.partition { v -> v.pos.y < massCenter.y }.toList() }
            .filter { it.isNotEmpty() }
            .map { Region(it) }

    fun repulse(vertex: ForceAtlas2Vertex, repulsion: RepulsionForce, theta: Double) {
        if (subregions.isEmpty() || massCenter.distance(vertex.pos) * theta > radius)
            repulsion.apply(vertex, massCenter, mass)
        else subregions.forEach { it.repulse(vertex, repulsion, theta) }
    }
}
