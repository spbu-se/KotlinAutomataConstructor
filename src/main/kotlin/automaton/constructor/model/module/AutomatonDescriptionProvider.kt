package automaton.constructor.model.module

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.utils.I18N.messages
import automaton.constructor.utils.capitalize
import javafx.beans.value.ObservableValue
import tornadofx.*

private val automatonDescriptionProviderFactory = { automaton: Automaton -> AutomatonDescriptionProvider(automaton) }
val Automaton.automatonDescriptionProvider get() = getModule(automatonDescriptionProviderFactory)
val Automaton.descriptionBinding get() = automatonDescriptionProvider.descriptionBinding
val Automaton.description: String get() = descriptionBinding.value

class AutomatonDescriptionProvider(val automaton: Automaton) : AutomatonModule {
    private val determinicityPartBinding = stringBinding(automaton.isDeterministicBinding) {
        if (value) automaton.deterministicAdjective
        else automaton.nondeterministicAdjective
    }
    private val determinicityPart: String by determinicityPartBinding

    private val epsilonPartBinding: ObservableValue<String> =
        if (automaton.memoryDescriptors.any { memoryUnitDescriptor ->
                (memoryUnitDescriptor.transitionFilters + memoryUnitDescriptor.transitionSideEffects + memoryUnitDescriptor.stateSideEffects).any { it.canBeDeemedEpsilon }
            }) stringBinding(automaton.hasEpsilonBinding) {
            if (value) messages.getString("AutomatonDescriptionProvider.WithEpsilonTransitions")
            else messages.getString("AutomatonDescriptionProvider.WithoutEpsilonTransitions")
        }
        else "".toProperty()
    private val epsilonPart: String by epsilonPartBinding

    val descriptionBinding = stringBinding(determinicityPartBinding, epsilonPartBinding) {
        listOf(determinicityPart, automaton.typeDisplayName, epsilonPart).filter { it.isNotEmpty() }.joinToString(" ")
            .capitalize()
    }
}
