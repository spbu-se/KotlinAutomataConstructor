package automaton.constructor.controller

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.automaton.getDeadVertices
import automaton.constructor.model.automaton.getNondistinguishableStateGroups
import automaton.constructor.model.automaton.getUnreachableVertices
import automaton.constructor.utils.I18N
import javafx.beans.property.SimpleObjectProperty
import tornadofx.getValue
import tornadofx.setValue

class SelectController {
    val selectedAutomatonProperty = SimpleObjectProperty<Automaton>()
    var selectedAutomaton by selectedAutomatonProperty

    private var lastChoosenNondistinguishableGroupIndex = 0

    val selectors = listOf(
        I18N.messages.getString("SelectController.UnreachableStates") to { selectedAutomaton.getUnreachableVertices() },
        I18N.messages.getString("SelectController.DeadStates") to { selectedAutomaton.getDeadVertices() },
        I18N.messages.getString("SelectController.NextNondistinguishableStatesGroup") to {
            val groups = selectedAutomaton.getNondistinguishableStateGroups()
            if (groups.isEmpty()) emptySet()
            else groups[(lastChoosenNondistinguishableGroupIndex++) % groups.size]
        }
    )
}
