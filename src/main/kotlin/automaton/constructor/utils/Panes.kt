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
package automaton.constructor.utils

import javafx.event.EventTarget
import javafx.geometry.Point2D
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.control.ScrollPane
import javafx.scene.layout.Pane
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import tornadofx.*
import kotlin.math.exp

fun Pane.subPane(subPane: Pane) {
    add(subPane)
    subPane.minWidthProperty().bind(widthProperty())
    subPane.minHeightProperty().bind(heightProperty())
}

fun Pane.subPane(op: Pane.() -> Unit = {}): Pane = Pane().also { subPane ->
    subPane(subPane)
    subPane.op()
}

fun EventTarget.zoomScrollPane(
    target: Node,
    scaleValue: Double = 1.0,
    zoomIntensity: Double = 1.0,
    op: ZoomScrollPane.() -> Unit
) = ZoomScrollPane(target, scaleValue, zoomIntensity).attachTo(this, op)

private const val ZOOM_INTENSITY_FACTOR = 0.002

class ZoomScrollPane(
    private val target: Node,
    private var scaleValue: Double,
    private val zoomIntensity: Double
) : ScrollPane() {
    private val zoomGroup = Group(target)

    init {
        content = VBox(zoomGroup).apply {
            alignment = Pos.CENTER
            setOnScroll {
                it.consume()
                onScroll(it.deltaY, Point2D(it.x, it.y))
            }
        }
        isPannable = true
        hbarPolicy = ScrollBarPolicy.NEVER
        vbarPolicy = ScrollBarPolicy.NEVER
        isFitToHeight = true
        isFitToWidth = true
        updateScale()
    }

    private fun updateScale() {
        target.scaleX = scaleValue
        target.scaleY = scaleValue
    }

    private fun onScroll(wheelDelta: Double, mousePos: Point2D) {
        val zoomFactor = exp(wheelDelta * ZOOM_INTENSITY_FACTOR * zoomIntensity)
        val innerBounds = zoomGroup.layoutBounds
        val viewportBounds = viewportBounds
        val valX = hvalue * (innerBounds.width - viewportBounds.width)
        val valY = vvalue * (innerBounds.height - viewportBounds.height)
        scaleValue *= zoomFactor
        updateScale()
        layout()
        val posInTarget = target.parentToLocal(zoomGroup.parentToLocal(mousePos))
        val adjustment = target.localToParentTransform.deltaTransform(posInTarget.multiply(zoomFactor - 1))
        val updatedInnerBounds = zoomGroup.boundsInLocal
        hvalue = (valX + adjustment.x) / (updatedInnerBounds.width - viewportBounds.width)
        vvalue = (valY + adjustment.y) / (updatedInnerBounds.height - viewportBounds.height)
    }
}

fun Region.customizedZoomScrollPane(op: Pane.() -> Unit): ZoomScrollPane {
    val innerPane = Pane()
    return zoomScrollPane(innerPane, 0.4) {
        style {
            focusColor = Color.TRANSPARENT
            backgroundColor += Color.TRANSPARENT
        }
        hvalue = 0.5
        vvalue = 0.5
        fitToWidth(this@customizedZoomScrollPane)
        fitToHeight(this@customizedZoomScrollPane)
        prefWidthProperty().bind(this@customizedZoomScrollPane.widthProperty())
        prefHeightProperty().bind(this@customizedZoomScrollPane.heightProperty())
        innerPane.op()
    }
}
