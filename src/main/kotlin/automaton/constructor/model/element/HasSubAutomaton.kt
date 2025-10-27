package automaton.constructor.model.element

import automaton.constructor.model.automaton.Automaton

/**
 * Common interface for vertices that contain a sub-automaton (BuildingBlock, RecursiveAutomatonBox).
 */
interface HasSubAutomaton {
    val subAutomaton: Automaton
}

