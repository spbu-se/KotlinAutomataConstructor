package automaton.constructor.model.module

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.property.EPSILON_VALUE
import automaton.constructor.utils.countBinding
import automaton.constructor.utils.filteredSet
import javafx.beans.binding.Bindings.isNotEmpty
import javafx.beans.binding.BooleanBinding
import tornadofx.*

private val epsilonDetectorFactory = { automaton: Automaton -> EpsilonDetector(automaton) }
val Automaton.epsilonDetector get() = getModule(epsilonDetectorFactory)
val Automaton.hasEpsilonBinding get() = epsilonDetector.hasEpsilonBinding
val Automaton.hasEpsilon: Boolean get() = hasEpsilonBinding.value

class EpsilonDetector(automaton: Automaton) : AutomatonModule {
    val hasEpsilonBinding: BooleanBinding = isNotEmpty(automaton.transitions.filteredSet { transition ->
        transition.allProperties.countBinding { property ->
            if (property.descriptor.canBeDeemedEpsilon) property.booleanBinding { it == EPSILON_VALUE }
            else false.toProperty()
        }.booleanBinding { it!!.toInt() > 0 }
    }).or(
        isNotEmpty(automaton.buildingBlocks.filteredSet { it.subAutomaton.hasEpsilonBinding })
    )
}
