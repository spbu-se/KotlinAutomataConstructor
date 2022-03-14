package automaton.constructor.utils

import javafx.beans.property.Property
import javafx.beans.value.WritableValue
import tornadofx.*

class UndoRedoManager(val maxUndo: Int = 20) {
    private val undoStack = ArrayDeque<UndoableAction>()
    private val redoStack = ArrayDeque<UndoableAction>()

    val wasModifiedProperty = false.toProperty()
    var wasModified by wasModifiedProperty

    val isUndoableProperty = false.toProperty()
    val isRedoableProperty = false.toProperty()

    var isUndoable by isUndoableProperty
    var isRedoable by isRedoableProperty

    private var inGroups = 0
    private var group: UndoableActionGroup? = null

    fun group(action: () -> Unit) {
        inGroups++
        action()
        inGroups--
        if (inGroups == 0) group = null
    }

    inline fun perform(crossinline act: () -> Unit, crossinline undo: () -> Unit) = perform(object : UndoableAction {
        override fun act() = act()
        override fun undo() = undo()
    })

    fun perform(undoableAction: UndoableAction) {
        group {
            undoableAction.act()
            onPerform(undoableAction)
        }
    }

    private fun onPerform(undoableAction: UndoableAction) {
        wasModified = true
        isRedoable = false
        redoStack.clear()
        group?.add(undoableAction) ?: run {
            if (undoStack.size == maxUndo) undoStack.removeLast()
            undoStack.addFirst(
                if (inGroups > 0) UndoableActionGroup(undoableAction).also { group = it }
                else undoableAction
            )
        }
        isUndoable = true
    }

    fun undo() {
        val undoableAction = undoStack.removeFirstOrNull() ?: return
        wasModified = true
        if (undoStack.isEmpty()) isUndoable = false
        redoStack.addFirst(undoableAction)
        undoableAction.undo()
        isRedoable = true
    }

    fun redo() {
        val undoableAction = redoStack.removeFirstOrNull() ?: return
        wasModified = true
        if (redoStack.isEmpty()) isRedoable = false
        undoStack.addFirst(undoableAction)
        undoableAction.act()
        isUndoable = true
    }

    fun reset() {
        undoStack.clear()
        redoStack.clear()
        isUndoable = false
        isRedoable = false
        wasModified = false
    }

    private var writing = 0
    private val listener = ChangeListener<Any?> { observable, oldValue, newValue ->
        if (writing == 0) {
            @Suppress("UNCHECKED_CAST")
            val writable = observable as WritableValue<Any?>
            fun write(value: Any?) {
                writing++
                try {
                    writable.value = value
                } finally {
                    writing--
                }
            }
            onPerform(object : UndoableAction {
                override fun act() = write(newValue)
                override fun undo() = write(oldValue)
            })
        }
    }

    fun registerProperty(property: Property<*>) = property.addListener(listener)
    fun unregisterProperty(property: Property<*>) = property.removeListener(listener)
}

interface UndoableAction {
    fun act()
    fun undo()
}

class UndoableActionGroup(undoableAction: UndoableAction) : UndoableAction {
    private val actions = mutableListOf(undoableAction)

    fun add(undoableAction: UndoableAction) = actions.add(undoableAction)
    override fun act() = actions.forEach { it.act() }
    override fun undo() = actions.asReversed().forEach { it.undo() }
}
