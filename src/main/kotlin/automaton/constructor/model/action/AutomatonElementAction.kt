package automaton.constructor.model.action

import automaton.constructor.model.property.AutomatonElement

interface AutomatonElementAction<in T : AutomatonElement> {
    val displayName: String

    fun isAvailableFor(element: T): Boolean
    fun performOn(element: T)
}
