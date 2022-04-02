package automaton.constructor.utils

import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.stage.Popup
import javafx.util.Duration

interface HoverableTooltipScope {
    var onHide: () -> Unit
    fun hide()
    fun restartTimer()
}

fun Node.hoverableTooltip(
    activationDelay: Duration = Duration(500.0),
    tooltipNodeFactory: HoverableTooltipScope.() -> Node?
) = HoverableTooltips.install(this, activationDelay, tooltipNodeFactory)

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

    fun install(node: Node, activationDelay: Duration, tooltipNodeFactory: HoverableTooltipScope.() -> Node?) {
        val scope = object : HoverableTooltipScope {
            override var onHide = {}

            override fun hide() {
                timer.stop()
                shownTooltip?.hide()
                shownTooltip = null
                shownTooltipNode = null
                hoveredTool = null
                tooltipShower = null
                onHide()
            }

            fun onMouseExited() {
                if (node == hoveredTool && !node.isHover && shownTooltipNode?.isHover != true)
                    hide()
            }

            override fun restartTimer() {
                hide()
                hoveredTool = node
                tooltipShower = {
                    shownTooltipNode = tooltipNodeFactory()?.apply {
                        styleClass.setAll(STYLE_CLASS)
                        addEventHandler(MouseEvent.MOUSE_EXITED) { onMouseExited() }
                    }
                    if (shownTooltipNode == null) restartTimer()
                    else shownTooltip = Popup().apply {
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

        node.addEventHandler(MouseEvent.MOUSE_EXITED) { scope.onMouseExited() }
        node.addEventHandler(MouseEvent.MOUSE_MOVED) {
            lastScreenX = it.screenX
            lastScreenY = it.screenY
            if (node != hoveredTool) scope.restartTimer()
        }
        node.addEventHandler(MouseEvent.MOUSE_CLICKED) { scope.hide() }
    }
}
