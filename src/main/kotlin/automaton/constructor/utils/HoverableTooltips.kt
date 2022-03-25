package automaton.constructor.utils

import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.stage.Popup
import javafx.util.Duration

fun Node.hoverableTooltip(activationDelay: Duration = Duration(500.0), tooltipNodeFactory: () -> Node) =
    HoverableTooltips.install(this, activationDelay, tooltipNodeFactory)

object HoverableTooltips {
    private const val POPUP_OFFSET = 15.0
    private const val STYLE_CLASS = "context-menu"
    private var shownTooltip: Popup? = null
    private var shownTooltipNode: Node? = null
    private var hoveredTool: Node? = null
    private var lastScreenX = 0.0
    private var lastScreenY = 0.0
    private var tooltipShower: (() -> Unit)? = null
    private val timer = Timeline().apply { setOnFinished { tooltipShower?.invoke() } }

    fun install(node: Node, activationDelay: Duration, tooltipNodeFactory: () -> Node) {
        fun onMouseExited() {
            if (node == hoveredTool && !node.isHover && shownTooltipNode?.isHover != true) {
                timer.stop()
                shownTooltip?.hide()
                shownTooltip = null
                shownTooltipNode = null
                hoveredTool = null
                tooltipShower = null
            }
        }
        node.addEventHandler(MouseEvent.MOUSE_EXITED) { onMouseExited() }
        node.addEventHandler(MouseEvent.MOUSE_MOVED) {
            lastScreenX = it.screenX
            lastScreenY = it.screenY
            if (node != hoveredTool) {
                timer.stop()
                shownTooltip?.hide()
                shownTooltip = null
                shownTooltipNode = null
                hoveredTool = node
                tooltipShower = {
                    shownTooltipNode = tooltipNodeFactory().apply {
                        styleClass.setAll(STYLE_CLASS)
                        addEventHandler(MouseEvent.MOUSE_EXITED) { onMouseExited() }
                    }
                    shownTooltip = Popup().apply {
                        content.add(shownTooltipNode)
                        isAutoHide = true
                        consumeAutoHidingEvents = false
                        show(node.scene.window, lastScreenX - POPUP_OFFSET, lastScreenY)
                        content.first().requestFocus()
                    }
                }
                timer.keyFrames.setAll(KeyFrame(activationDelay))
                timer.playFromStart()
            }
        }
        node.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            timer.stop()
            shownTooltip?.hide()
            shownTooltip = null
            shownTooltipNode = null
            hoveredTool = null
            tooltipShower = null
        }
    }
}
