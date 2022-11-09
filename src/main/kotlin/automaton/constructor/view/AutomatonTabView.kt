package automaton.constructor.view

import automaton.constructor.model.automaton.Automaton
import javafx.scene.control.SplitPane
import tornadofx.*

class AutomatonTabView(
    val automaton: Automaton,
    automatonViewContext: AutomatonViewContext
) : SplitPane() {
    val automatonView: AutomatonView = AutomatonView(automaton, automatonViewContext).also { add(it) }
    val undoRedoController get() = automatonView.undoRedoController

    private val automatonTransformationViewBinding =
        automaton.isInputForTransformationProperty.objectBinding { nullableTransformation ->
            nullableTransformation?.let { transformation ->
                AutomatonTransformationView(
                    transformation,
                    automatonViewContext
                )
            }
        }.apply {
            onChange { transformationView ->
                // make transformationView a second item of the SplitPane
                if (transformationView == null) {
                    if (items.size > 1) items.removeLast()
                } else {
                    if (items.size > 1) items[1] = transformationView
                    else items.add(transformationView)
                }
            }
        }
    private val automatonTransformationView: AutomatonTransformationView? by automatonTransformationViewBinding

    fun ensureAutomatonViewIsShown() {
        items.removeFirst()
        automatonView.removeFromParent()
        items.add(0, automatonView)
    }
}
