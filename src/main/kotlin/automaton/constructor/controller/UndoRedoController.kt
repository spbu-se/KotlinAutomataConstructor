package automaton.constructor.controller

import automaton.constructor.view.AutomatonView
import javafx.scene.control.TextInputControl
import javafx.scene.input.KeyCombination

class UndoRedoController(val automatonView: AutomatonView) {
    companion object {
        val UNDO_COMBO: KeyCombination = KeyCombination.valueOf("Shortcut+Z")
        val REDO_COMBO: KeyCombination = KeyCombination.valueOf("Shortcut+Shift+Z")
    }

    fun onUndo() {
        val focusOwner = automatonView.scene.focusOwner
        when {
            focusOwner is TextInputControl && focusOwner.isUndoable -> focusOwner.undo()
            else -> {
                automatonView.automaton.undoRedoManager.undo()
                automatonView.automatonGraphView.controller.clearSelection()
            }
        }
    }

    fun onRedo() {
        val focusOwner = automatonView.scene.focusOwner
        when {
            focusOwner is TextInputControl && focusOwner.isRedoable -> focusOwner.redo()
            else -> {
                automatonView.automaton.undoRedoManager.redo()
                automatonView.automatonGraphView.controller.clearSelection()
            }
        }
    }
}
