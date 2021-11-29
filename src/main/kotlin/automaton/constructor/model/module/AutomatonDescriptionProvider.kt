package automaton.constructor.model.module

import automaton.constructor.model.Automaton
import tornadofx.*

val automatonDescriptionProviderFactory = { automaton: Automaton -> AutomatonDescriptionProvider(automaton) }
val Automaton.automatonDescriptionProvider get() = getModule(automatonDescriptionProviderFactory)
val Automaton.descriptionBinding get() = automatonDescriptionProvider.descriptionBinding

// TODO include automaton type (e.g. "finite automaton", "turing machine with 2 tapes") based on memory units types
class AutomatonDescriptionProvider(val automaton: Automaton) : AutomatonModule {
    val descriptionBinding =
        if (automaton.memoryDescriptors.any { memoryUnitDescriptor ->
                (memoryUnitDescriptor.filters + memoryUnitDescriptor.sideEffects).any {
                    it.canBeDeemedEpsilon
                }
            })
            nonNullObjectBinding(automaton.isDeterministicBinding, automaton.hasEpsilonBinding) {
                (if (automaton.isDeterministic) "Deterministic " else "Non deterministic ") +
                        "automaton " + (if (automaton.hasEpsilon) "with " else "without ") +
                        "epsilon transitions"
            }
        else nonNullObjectBinding(automaton.isDeterministicBinding) {
            (if (automaton.isDeterministic) "Deterministic " else "Non deterministic ") + "automaton"
        }
}
