package automaton.constructor.model.automaton.flavours

import automaton.constructor.model.memory.output.MealyMooreOutputTapeDescriptor
import automaton.constructor.model.property.AutomatonElement
import automaton.constructor.model.property.DynamicProperty
import automaton.constructor.model.property.EPSILON_VALUE

interface AutomatonWithMealyMooreOutputTape {
    val mealyMooreOutputTape: MealyMooreOutputTapeDescriptor


    val AutomatonElement.mealyMooreOutputValueProperty: DynamicProperty<String?>
        get() = getProperty(mealyMooreOutputTape.outputValue)

    var AutomatonElement.mealyMooreOutputValue: String?
        get() = mealyMooreOutputValueProperty.get()
        set(value) = mealyMooreOutputValueProperty.set(value)

    val AutomatonElement.mealyMooreNotNullOutputValue: String
        get() = mealyMooreOutputValueProperty.get().takeIf { it != EPSILON_VALUE } ?: ""
}
