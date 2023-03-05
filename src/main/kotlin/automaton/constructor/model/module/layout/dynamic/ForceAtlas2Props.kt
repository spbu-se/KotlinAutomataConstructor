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

data class ForceAtlas2Props(
    var tolerance: Double = 1.0,
    var scaling: Double,
    var strongGravity: Boolean = true,
    var gravityCoefficient: Double = 500.0,
    var preventOverlap: Boolean = false,
    var dissuadeHubs: Boolean = false,
    var attractionType: AttractionType = AttractionType.LINEAR,
    var edgeWeightExponent: Double = 1.0,
    var barnesHutTheta: Double = 0.85
) {
    constructor(vertexCount: Int) : this(scaling = if (vertexCount >= 100) 1000.0 else 5000.0)
}
