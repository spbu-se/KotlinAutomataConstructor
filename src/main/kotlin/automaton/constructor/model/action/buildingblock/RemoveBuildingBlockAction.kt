package automaton.constructor.model.action.buildingblock

import automaton.constructor.model.action.ActionAvailability
import automaton.constructor.model.action.createAutomatonElementAction
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.element.BuildingBlock
import automaton.constructor.utils.I18N
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination

fun createRemoveBuildingBlockAction(automaton: Automaton) = createAutomatonElementAction<Automaton, BuildingBlock>(
    automaton = automaton,
    displayName = I18N.messages.getString("AutomatonElementAction.RemoveBuildingBlock"),
    keyCombination = KeyCodeCombination(KeyCode.DELETE),
    getAvailabilityFor = { ActionAvailability.AVAILABLE },
    performOn = { removeVertex(it) }
)
