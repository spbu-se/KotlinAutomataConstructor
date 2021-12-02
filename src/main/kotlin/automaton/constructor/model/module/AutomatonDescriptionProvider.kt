package automaton.constructor.model.module

import automaton.constructor.model.Automaton
import javafx.beans.value.ObservableValue
import tornadofx.*

val automatonDescriptionProviderFactory = { automaton: Automaton -> AutomatonDescriptionProvider(automaton) }
val Automaton.automatonDescriptionProvider get() = getModule(automatonDescriptionProviderFactory)
val Automaton.descriptionBinding get() = automatonDescriptionProvider.descriptionBinding

// TODO include automaton type (e.g. "finite automaton", "turing machine with 2 tapes") based on memory units types
class AutomatonDescriptionProvider(val automaton: Automaton) : AutomatonModule {
    private val determinicityPartBinding = stringBinding(automaton.isDeterministicBinding) {
        if (value) "deterministic" else "nondeterministic"
    }
    private val determinicityPart: String by determinicityPartBinding

    private val epsilonPartBinding: ObservableValue<String> =
        if (automaton.memoryDescriptors.any { memoryUnitDescriptor ->
                (memoryUnitDescriptor.filters + memoryUnitDescriptor.sideEffects).any { it.canBeDeemedEpsilon }
            }) stringBinding(automaton.hasEpsilonBinding) { (if (value) "with" else "without") + " epsilon transitions" }
        else "".toProperty()
    private val epsilonPart: String by epsilonPartBinding

    val descriptionBinding = stringBinding(determinicityPartBinding, epsilonPartBinding) {
        listOf(determinicityPart, automaton.typeName, epsilonPart).filter { it.isNotEmpty() }
            .joinToString(" ").capitalize()
    }
}
