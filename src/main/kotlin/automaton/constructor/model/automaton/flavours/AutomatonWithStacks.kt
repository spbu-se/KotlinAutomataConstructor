package automaton.constructor.model.automaton.flavours

import automaton.constructor.model.memory.StackDescriptor
import automaton.constructor.model.property.DynamicProperty
import automaton.constructor.model.transition.Transition

interface AutomatonWithStacks {
    val stacks: List<StackDescriptor>


    val Transition.stacksExpectedCharsProperties: List<DynamicProperty<Char?>>
        get() = stacks.map { getProperty(it.expectedChar) }

    val Transition.stacksExpectedChars: DynamicPropertiesValues<Char?>
        get() = DynamicPropertiesValues(stacksExpectedCharsProperties)


    val Transition.stacksPushedValuesProperties: List<DynamicProperty<String?>>
        get() = stacks.map { getProperty(it.pushedValue) }

    val Transition.stacksPushedValues: DynamicPropertiesValues<String?>
        get() = DynamicPropertiesValues(stacksPushedValuesProperties)
}
