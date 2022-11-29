package automaton.constructor.view

import automaton.constructor.model.data.createAutomaton
import automaton.constructor.model.data.getData
import automaton.constructor.model.transformation.AutomatonTransformation
import automaton.constructor.utils.I18N
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*

class AutomatonTransformationView(
    val transformation: AutomatonTransformation,
    automatonViewContext: AutomatonViewContext
) : VBox() {
    private val automatonView = AutomatonView(transformation.resultingAutomaton, automatonViewContext)
    private var completeWasCalled = false

    init {
        transformation.isCompletedProperty.onChange {
            if (transformation.isCompleted) {
                if (!completeWasCalled) information(
                    transformation.displayName,
                    I18N.messages.getString("AutomatonTransformation.HasBeenCompleted"),
                    title = I18N.messages.getString("Dialog.information")
                )
                MainWindow(
                    // make sure it's a completely fresh Automaton instance independent of this window
                    transformation.resultingAutomaton.getData().createAutomaton()
                ).show()
            }
        }
        titledpane(transformation.displayName) {
            vbox {
                spacing = 10.0
                transformation.description?.let { label(it) }
                hbox {
                    spacing = 10.0
                    button(I18N.messages.getString("AutomatonTransformation.Complete")) {
                        action {
                            completeWasCalled = true
                            transformation.complete()
                        }
                    }
                    button(I18N.messages.getString("AutomatonTransformation.Cancel")) {
                        action {
                            transformation.stop()
                        }
                    }
                }
            }
        }
        add(automatonView)
        automatonView.vgrow = Priority.ALWAYS
    }
}
