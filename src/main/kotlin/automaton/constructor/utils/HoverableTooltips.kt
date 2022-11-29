package automaton.constructor.utils

import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.stage.Popup
import javafx.stage.WindowEvent
import javafx.util.Duration
import tornadofx.*

interface HoverableTooltipScope {
    var onHiding: (WindowEvent) -> Unit
    fun hide()
    fun scheduleShow()
}

fun Node.hoverableTooltip(
    activationDelay: Duration = Duration(500.0),
    stopManagingOnInteraction: Boolean = false,
    tooltipNodeFactory: HoverableTooltipScope.() -> Node?
) = HoverableTooltips.install(this, activationDelay, stopManagingOnInteraction, tooltipNodeFactory)

object HoverableTooltips {
    private const val POPUP_OFFSET = 15.0
    private const val STYLE_CLASS = "context-menu"
    private val MIN_SHOW_DURATION = 250.millis
    private var managedTool: Node? = null

    fun install(
        tool: Node,
        activationDelay: Duration,
        stopManagingOnInteraction: Boolean,
        tooltipNodeFactory: HoverableTooltipScope.() -> Node?
    ) {
        var lastScreenX = 0.0
        var lastScreenY = 0.0
        var tooltip: Popup? = null
        var showTask: FXTimerTask? = null

        val scope = object : HoverableTooltipScope {
            override var onHiding: (WindowEvent) -> Unit = { _ -> }
            private var justShown = false

            override fun hide() {
                tooltip?.hide()
                showTask?.cancel()
                showTask = null
            }

            fun hideIfNotHovered() {
                if (!justShown && managedTool === tool && !tool.isHover && tooltip?.content?.any { it.isHover } != true)
                    hide()
            }

            override fun scheduleShow() {
                showTask?.cancel()
                if (tooltip != null) hide()
                managedTool = tool
                showTask = runLater(activationDelay) {
                    val scene = tool.scene ?: return@runLater
                    val tooltipNode = tooltipNodeFactory()?.apply {
                        styleClass.setAll(STYLE_CLASS)
                        addEventHandler(MouseEvent.MOUSE_EXITED) { hideIfNotHovered() }
                        if (stopManagingOnInteraction)
                            addEventFilter(MouseEvent.MOUSE_CLICKED) {
                                if (managedTool === tool)
                                    managedTool = null
                            }
                    } ?: return@runLater
                    tooltip = Popup().apply {
                        content.add(tooltipNode)
                        isAutoHide = true
                        consumeAutoHidingEvents = false
                        justShown = true
                        show(scene.window, lastScreenX - POPUP_OFFSET, lastScreenY)
                        content.first().requestFocus()
                        setOnHiding { onHiding(it) }
                        setOnHidden {
                            content.clear() // in case `tooltipNode` will be reused later
                            tooltip = null
                        }
                        runLater(MIN_SHOW_DURATION) {
                            justShown = false
                            hideIfNotHovered()
                        }
                    }
                    showTask = null
                }
            }
        }

        tool.addEventHandler(MouseEvent.MOUSE_EXITED) { scope.hideIfNotHovered() }
        tool.addEventHandler(MouseEvent.MOUSE_MOVED) {
            lastScreenX = it.screenX
            lastScreenY = it.screenY
            if (tooltip == null) scope.scheduleShow()
        }
        tool.addEventFilter(MouseEvent.MOUSE_CLICKED) { scope.hide() }
        tool.layoutBoundsProperty().onChange { scope.hide() }
        tool.sceneProperty().onChange { if (it == null) scope.hide() }
    }
}
