package automaton.constructor.controller

import automaton.constructor.view.AutomatonTabView
import javafx.scene.control.TextInputControl
import javafx.scene.input.KeyCombination

class UndoRedoController(val automatonTabView: AutomatonTabView) {
    companion object {
        val UNDO_COMBO: KeyCombination = KeyCombination.valueOf("Shortcut+Z")
        val REDO_COMBO: KeyCombination = KeyCombination.valueOf("Shortcut+Shift+Z")
    }

    val isUndoableProperty get() = automatonTabView.automaton.undoRedoManager.isUndoableProperty
    val isRedoableProperty get() = automatonTabView.automaton.undoRedoManager.isRedoableProperty

    fun onUndo() {
        val focusOwner = automatonTabView.scene.focusOwner
        when {
            focusOwner is TextInputControl && focusOwner.isUndoable -> focusOwner.undo()
            else -> {
                automatonTabView.automaton.undoRedoManager.undo()
                automatonTabView.automatonGraphView.controller.clearSelection()
            }
        }
    }

    fun onRedo() {
        val focusOwner = automatonTabView.scene.focusOwner
        when {
            focusOwner is TextInputControl && focusOwner.isRedoable -> focusOwner.redo()
            else -> {
                automatonTabView.automaton.undoRedoManager.redo()
                automatonTabView.automatonGraphView.controller.clearSelection()
            }
        }
    }
}
