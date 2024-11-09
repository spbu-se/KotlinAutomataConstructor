package automaton.constructor.view.automaton

import automaton.constructor.controller.AutomatonRepresentationController
import automaton.constructor.view.AutomatonElementView
import javafx.scene.layout.Pane

abstract class AutomatonRepresentationView: Pane() {
    abstract val controller: AutomatonRepresentationController
    abstract fun getAllElementsViews(): List<AutomatonElementView>
}
