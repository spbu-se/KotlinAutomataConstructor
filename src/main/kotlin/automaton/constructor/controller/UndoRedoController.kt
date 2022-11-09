package automaton.constructor.controller

import automaton.constructor.view.AutomatonView
import javafx.beans.binding.BooleanBinding
import javafx.scene.control.TextInputControl
import javafx.scene.input.KeyCombination
import tornadofx.*

class UndoRedoController(val automatonView: AutomatonView) {
    companion object {
        val UNDO_COMBO: KeyCombination = KeyCombination.valueOf("Shortcut+Z")
        val REDO_COMBO: KeyCombination = KeyCombination.valueOf("Shortcut+Shift+Z")
    }

    val isUndoableBinding: BooleanBinding = automatonView.automaton.undoRedoManager.isUndoableProperty
        .and(automatonView.automaton.allowsModificationsByUserProperty)
    val isUndoable by isUndoableBinding
    val isRedoableBinding: BooleanBinding = automatonView.automaton.undoRedoManager.isRedoableProperty
        .and(automatonView.automaton.allowsModificationsByUserProperty)
    val isRedoable by isRedoableBinding

    fun onUndo() {
        if (!isUndoable) return
        val focusOwner = automatonView.scene?.focusOwner
        when {
            focusOwner is TextInputControl && focusOwner.isUndoable -> focusOwner.undo()
            else -> {
                automatonView.automaton.undoRedoManager.undo()
                automatonView.automatonGraphView.controller.clearSelection()
            }
        }
    }

    fun onRedo() {
        if (!isRedoable) return
        val focusOwner = automatonView.scene?.focusOwner
        when {
            focusOwner is TextInputControl && focusOwner.isRedoable -> focusOwner.redo()
            else -> {
                automatonView.automaton.undoRedoManager.redo()
                automatonView.automatonGraphView.controller.clearSelection()
            }
        }
    }
}
