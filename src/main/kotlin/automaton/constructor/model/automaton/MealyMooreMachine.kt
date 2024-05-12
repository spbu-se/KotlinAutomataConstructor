package automaton.constructor.model.automaton

import automaton.constructor.model.action.Action
import automaton.constructor.model.action.state.MealyToMooreAction
import automaton.constructor.model.action.state.MooreToMealyAction
import automaton.constructor.model.automaton.flavours.AutomatonWithInputTape
import automaton.constructor.model.automaton.flavours.AutomatonWithOutputTape
import automaton.constructor.model.data.MealyMooreMachineData
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.model.memory.tape.OutputTapeDescriptor
import automaton.constructor.model.transformation.StatewiseTransformationAction
import automaton.constructor.utils.I18N

/**
 * Mealy/Moore machine.
 *
 * It's an automaton with an [input tape][inputTape] and a [Mealy/Moore output tape][outputTape] as [memory descriptors][memoryDescriptors].
 */
class MealyMooreMachine(
    override val inputTape: InputTapeDescriptor,
    override val outputTape: OutputTapeDescriptor
) : AbstractAutomaton(
    DISPLAY_NAME,
    memoryDescriptors = listOf(inputTape, outputTape),
    I18N.messages.getString("MealyMooreMachine.Deterministic"),
    I18N.messages.getString("MealyMooreMachine.Nondeterministic"),
    I18N.messages.getString("MealyMooreMachine.Untitled")
),
    AutomatonWithInputTape, AutomatonWithOutputTape {
    override fun getTypeData() = MealyMooreMachineData(
        inputTape = inputTape.getData(),
        outputTape = outputTape.getData()
    )

    override fun createEmptyAutomatonOfSameType() = MealyMooreMachine(inputTape, outputTape)

    override val stateActions = super.stateActions + listOf(
        MooreToMealyAction(mealyMooreMachine = this),
        MealyToMooreAction(mealyMooreMachine = this)
    )

    override val transformationActions: List<Action<Unit>>
        get() = super.transformationActions + listOf(
            StatewiseTransformationAction(
                displayName = I18N.messages.getString("MealyMooreMachine.ConvertToMoore"),
                automaton = this,
                unavailableMessage = I18N.messages.getString("MealyMooreMachine.AlreadyMoore")
            ) { machine ->
                machine.stateActions.first { it is MealyToMooreAction }
            },
            StatewiseTransformationAction(
                displayName = I18N.messages.getString("MealyMooreMachine.ConvertToMealy"),
                automaton = this,
                unavailableMessage = I18N.messages.getString("MealyMooreMachine.AlreadyMealy")
            ) { machine ->
                machine.stateActions.first { it is MooreToMealyAction }
            }
        )

    companion object {
        val DISPLAY_NAME: String = I18N.messages.getString("MealyMooreMachine")
    }
}
