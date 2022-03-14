package automaton.constructor.controller

import automaton.constructor.view.AutomatonView

class UndoRedoController(val automatonView: AutomatonView) {
    fun onUndo() {
        automatonView.automaton.undoRedoManager.undo()
        automatonView.automatonGraphView.controller.clearSelection()
    }

    fun onRedo() {
        automatonView.automaton.undoRedoManager.undo()
        automatonView.automatonGraphView.controller.clearSelection()
    }
}
