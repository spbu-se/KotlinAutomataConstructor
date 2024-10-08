package automaton.constructor.view.automaton

import automaton.constructor.view.AutomatonElementView
import javafx.scene.layout.Pane

abstract class AutomatonRepresentationView: Pane() {
    abstract fun getAllElementsViews(): List<AutomatonElementView>
}
