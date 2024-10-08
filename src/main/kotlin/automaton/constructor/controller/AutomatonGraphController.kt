package automaton.constructor.controller

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.automaton.allowsBuildingBlocks
import automaton.constructor.model.data.addContent
import automaton.constructor.model.element.*
import automaton.constructor.utils.*
import automaton.constructor.view.*
import automaton.constructor.view.automaton.AutomatonGraphView
import javafx.geometry.Point2D
import javafx.scene.control.ContextMenu
import javafx.scene.input.MouseButton
import javafx.scene.shape.Line
import tornadofx.*

class AutomatonGraphController(automaton: Automaton, automatonViewContext: AutomatonViewContext) :
    AutomatonRepresentationController(automaton, automatonViewContext) {
    private val settingsController by inject<SettingsController>()
    private val newTransitionLine = Line().apply { isVisible = false }
    private var newTransitionSourceProperty = objectProperty<AutomatonVertexView?>(null).apply {
        onChange {
            newTransitionLine.isVisible = it != null
            if (it != null) {
                newTransitionLine.startXProperty().unbind()
                newTransitionLine.startXProperty().bind(it.vertex.positionProperty.x)
                newTransitionLine.startYProperty().unbind()
                newTransitionLine.startYProperty().bind(it.vertex.positionProperty.y)
            }
        }
    }
    private var newTransitionSource by newTransitionSourceProperty

    fun select(elements: Set<AutomatonElementView>) {
        clearSelection()
        selectedElementsViews.addAll(elements.onEach { it.selected = true })
    }

    fun registerGraphView(graphView: AutomatonGraphView) {
        graphView.add(newTransitionLine)
        graphView.setOnMouseClicked {
            it.consume()
            graphView.requestFocus()
            if (it.button == MouseButton.PRIMARY && it.isStillSincePress) clearSelection()
            else if (it.button == MouseButton.SECONDARY && it.isStillSincePress && automaton.allowsModificationsByUser) {
                ContextMenu().apply {
                    item(I18N.messages.getString("AutomatonGraphController.AddState")) {
                        action {
                            if (automaton.allowsModificationsByUser)
                                automaton.addState(position = Point2D(it.x, it.y))
                        }
                    }
                    if (automaton.allowsBuildingBlocks) {
                        item(I18N.messages.getString("AutomatonGraphController.AddEmptyBuildingBlock")) {
                            action {
                                if (automaton.allowsModificationsByUser)
                                    automaton.addBuildingBlock(position = Point2D(it.x, it.y))
                            }
                        }
                        item(I18N.messages.getString("AutomatonGraphController.CopyBuildingBlockFromFile")) {
                            action {
                                if (!automaton.allowsModificationsByUser) return@action
                                val file = automatonViewContext.fileController.chooseFile(
                                    I18N.messages.getString("MainView.File.Open"),
                                    FileChooserMode.Single
                                ) ?: return@action
                                automatonViewContext.fileController.loadAsync(file) addOnSuccess { (type, vertices, transitions, edges) ->
                                    if (type != automaton.getTypeData()) error(
                                        I18N.messages.getString("AutomatonGraphController.BuildingBlockLoadingFailed"),
                                        I18N.messages.getString("AutomatonGraphController.IncompatibleAutomatonType"),
                                        owner = automatonViewContext.uiComponent.currentWindow
                                    )
                                    else {
                                        automaton.addBuildingBlock(position = Point2D(it.x, it.y)).apply {
                                            subAutomaton.addContent(vertices, transitions, edges)
                                            name = file.nameWithoutExtension
                                        }
                                    }
                                }
                            }
                        }
                    }
                    show(graphView.scene.window, it.screenX, it.screenY)
                }
            }
        }
        graphView.setOnMouseDragReleased {
            it.consume()
            newTransitionSource = null
        }
        graphView.focusedProperty().onChange { if (!it) newTransitionSource = null }
        enableShortcuts(graphView)
    }

    fun registerVertexView(automatonVertexView: AutomatonVertexView) {
        registerAutomatonElementView(automatonVertexView)
        if (automatonVertexView.vertex is BuildingBlock) automatonVertexView.onMouseClicked += {
            if (it.clickCount == 2) {
                it.consume()
                automatonViewContext.onBuildingBlockDoubleClicked(automatonVertexView.vertex)
            }
        }
        automatonVertexView.setOnMouseDragged {
            it.consume()
            if (it.button == MouseButton.PRIMARY && automatonVertexView.selected) {
                val change = Point2D(it.x, it.y) - automatonVertexView.vertex.position
                selectedElementsViews.forEach { elementView ->
                    if (elementView is AutomatonVertexView) {
                        elementView.vertex.position += change
                        elementView.vertex.requiresLayout = false
                        elementView.vertex.forceNoLayout = true
                    }
                }
            } else if (it.button == MouseButton.SECONDARY && automaton.allowsModificationsByUser) {
                automatonVertexView.vertex.requiresLayout = false
                newTransitionLine.endX = it.x
                newTransitionLine.endY = it.y
            }
        }
        automatonVertexView.setOnDragDetected {
            it.consume()
            if (it.button == MouseButton.PRIMARY) {
                if (!it.isControlDown && !automatonVertexView.selected) clearSelection()
                selectedElementsViews.add(automatonVertexView)
                automatonVertexView.selected = true
                lastSelectedElement = automatonVertexView
            } else if (it.button == MouseButton.SECONDARY) {
                newTransitionSource = automatonVertexView
                automatonVertexView.startFullDrag()
            }
        }
        automatonVertexView.setOnMouseReleased { event ->
            event.consume()
            if (!event.isStillSincePress) automaton.undoRedoManager.group {
                selectedElementsViews.forEach {
                    if (it is AutomatonVertexView) {
                        it.vertex.lastReleasePosition = it.vertex.position
                        it.vertex.forceNoLayout = false
                    }
                }
                if (settingsController.isHintEnabled(SettingsController.Hint.DYNAMIC_LAYOUT) && automaton.vertices.any { it.requiresLayout }) {
                    information(
                        I18N.messages.getString("AutomatonGraphController.DynamicLayoutHint"),
                        title = I18N.messages.getString("Dialog.information"),
                        owner = automatonViewContext.uiComponent.currentWindow
                    )
                    settingsController.setHintEnabled(SettingsController.Hint.DYNAMIC_LAYOUT, false)
                }
            }
        }
        automatonVertexView.setOnMouseDragReleased {
            it.consume()
            val source = newTransitionSourceProperty.value ?: return@setOnMouseDragReleased
            newTransitionSourceProperty.value = null
            if (automaton.allowsModificationsByUser) {
                source.vertex.requiresLayout = false
                automatonVertexView.vertex.requiresLayout = false
                automaton.addTransition(source.vertex, automatonVertexView.vertex)
            }
        }
    }

    fun registerEdgeView(edgeView: AutomatonEdgeView) {
        edgeView.setOnMouseClicked {
            it.consume()
            edgeView.requestFocus()
        }
        edgeView.transitionViews.forEach { registerTransitionView(it) }
        edgeView.transitionViews.onChange { change ->
            while (change.next())
                if (change.wasAdded())
                    change.addedSubList.forEach { registerTransitionView(it) }
        }
    }

    private fun registerTransitionView(transitionView: TransitionView) =
        registerAutomatonElementView(transitionView)
}
