package automaton.constructor.model.action.buildingblock

import automaton.constructor.model.action.AbstractAction
import automaton.constructor.model.action.ActionAvailability
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.element.BuildingBlock
import automaton.constructor.utils.I18N
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination

class RemoveBuildingBlockAction(automaton: Automaton) : AbstractAction<Automaton, BuildingBlock>(
    automaton = automaton,
    displayName = I18N.messages.getString("Action.RemoveBuildingBlock"),
    keyCombination = KeyCodeCombination(KeyCode.DELETE)
) {
    override fun Automaton.doGetAvailabilityFor(actionSubject: BuildingBlock) = ActionAvailability.AVAILABLE

    override fun Automaton.doPerformOn(actionSubject: BuildingBlock) = removeVertex(actionSubject)
}
