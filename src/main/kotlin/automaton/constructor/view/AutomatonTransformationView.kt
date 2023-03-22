package automaton.constructor.view

import automaton.constructor.model.module.layout.static.STATIC_LAYOUTS
import automaton.constructor.model.transformation.AutomatonTransformation
import automaton.constructor.utils.I18N
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*

class AutomatonTransformationView(
    val transformation: AutomatonTransformation,
    private val automatonViewContext: AutomatonViewContext
) : VBox() {
    private val automatonView = AutomatonView(transformation.resultingAutomaton, automatonViewContext)

    init {
        transformation.isCompletedProperty.onChange { checkCompleted() }
        titledpane(transformation.displayName) {
            vbox {
                spacing = 10.0
                transformation.description?.let { label(it) }
                hbox {
                    spacing = 10.0
                    button(I18N.messages.getString("AutomatonTransformation.Complete")) {
                        action {
                            transformation.complete()
                        }
                    }
                    button(I18N.messages.getString("AutomatonTransformation.Cancel")) {
                        action {
                            transformation.stop()
                        }
                    }
                    menubutton(I18N.messages.getString("AutomatonTransformation.RelayoutAll")) {
                        STATIC_LAYOUTS.forEach { layout ->
                            item(layout.name).action {
                                automatonViewContext.layoutController.layout(
                                    automatonView.automaton,
                                    automatonView.automatonGraphView.transitionLayoutBounds(),
                                    layout
                                )
                            }
                        }
                    }
                }
            }
        }
        add(automatonView)
        automatonView.vgrow = Priority.ALWAYS
        checkCompleted()
    }

    private fun checkCompleted() {
        if (transformation.isCompleted) {
            information(
                I18N.messages.getString("AutomatonTransformation.HasBeenCompleted"),
                content=transformation.completionMessage,
                title = I18N.messages.getString("Dialog.information")
            )
            automatonViewContext.openInNewWindow(transformation.resultingAutomaton)
        }
    }
}
