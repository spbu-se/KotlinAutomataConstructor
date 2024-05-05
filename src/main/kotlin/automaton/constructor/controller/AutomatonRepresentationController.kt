package automaton.constructor.controller

import automaton.constructor.model.action.Action
import automaton.constructor.model.action.ActionAvailability
import automaton.constructor.model.action.ActionFailedException
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.element.AutomatonElement
import automaton.constructor.model.element.BuildingBlock
import automaton.constructor.model.element.State
import automaton.constructor.model.element.Transition
import automaton.constructor.utils.I18N
import automaton.constructor.view.AutomatonElementView
import automaton.constructor.view.AutomatonViewContext
import javafx.scene.control.ContextMenu
import javafx.scene.input.MouseButton
import tornadofx.*

open class AutomatonRepresentationController(
    val automaton: Automaton,
    val automatonViewContext: AutomatonViewContext
): Controller() {
    val lastSelectedElementProperty = objectProperty<AutomatonElementView?>(null)
    var lastSelectedElement by lastSelectedElementProperty
    val selectedElementsViews = mutableSetOf<AutomatonElementView>()

    fun registerAutomatonElementView(automatonElementView: AutomatonElementView) {
        automatonElementView.setOnMouseClicked {
            it.consume()
            automatonElementView.requestFocus()
            if (it.button == MouseButton.PRIMARY) {
                if (it.isStillSincePress)
                    automaton.isOutputOfTransformation?.let { transformation ->
                        transformation.step(automatonElementView.automatonElement)
                        return@setOnMouseClicked
                    }
                lastSelectedElement = when {
                    !it.isControlDown -> {
                        clearSelection()
                        selectedElementsViews.add(automatonElementView)
                        automatonElementView.selected = true
                        automatonElementView
                    }
                    automatonElementView.selected -> {
                        selectedElementsViews.remove(automatonElementView)
                        automatonElementView.selected = false
                        null
                    }
                    else -> {
                        selectedElementsViews.add(automatonElementView)
                        automatonElementView.selected = true
                        automatonElementView
                    }
                }
            } else if (it.button == MouseButton.SECONDARY && it.isStillSincePress && automaton.allowsModificationsByUser) {
                fun <T : AutomatonElement> showActionsMenu(element: T, actions: List<Action<T>>) {
                    val actionsWithAvailability = actions.map { action ->
                        action to action.getAvailabilityFor(element)
                    }

                    if (actionsWithAvailability.any { (_, availability) -> availability != ActionAvailability.HIDDEN }) {
                        ContextMenu().apply {
                            for ((action, availability) in actionsWithAvailability) {
                                item(action.displayName, action.keyCombination) {
                                    action {
                                        try {
                                            if (automaton.allowsModificationsByUser)
                                                action.performOn(element)
                                        } catch (exc: ActionFailedException) {
                                            error(
                                                exc.message,
                                                title = I18N.messages.getString("Dialog.error"),
                                                owner = automatonViewContext.uiComponent.currentWindow
                                            )
                                        }

                                    }
                                    isVisible = availability != ActionAvailability.HIDDEN
                                    isDisable = availability == ActionAvailability.DISABLED
                                }
                            }
                            show(automatonElementView.scene.window, it.screenX, it.screenY)
                        }
                        clearSelection()
                        selectedElementsViews.add(automatonElementView)
                        automatonElementView.selected = true
                        lastSelectedElement = automatonElementView
                    }
                }
                when (automatonElementView.automatonElement) {
                    is State -> showActionsMenu(
                        automatonElementView.automatonElement,
                        automaton.stateActions
                    )
                    is BuildingBlock -> showActionsMenu(
                        automatonElementView.automatonElement,
                        automaton.buildingBlockActions
                    )
                    is Transition -> showActionsMenu(
                        automatonElementView.automatonElement,
                        automaton.transitionActions
                    )
                }
            }
        }
        clearSelection()
        selectedElementsViews.add(automatonElementView)
        automatonElementView.selected = true
        lastSelectedElement = automatonElementView
    }

    fun clearSelection() {
        selectedElementsViews.onEach { it.selected = false }.clear()
        lastSelectedElement = null
    }
}