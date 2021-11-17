package automaton.constructor.model.module

import automaton.constructor.model.Automaton
import automaton.constructor.utils.countBinding
import automaton.constructor.utils.filteredSet
import javafx.beans.binding.Bindings.isNotEmpty
import javafx.beans.binding.BooleanBinding
import tornadofx.*

val epsilonDetectorFactory = { automaton: Automaton -> EpsilonDetector(automaton) }
val Automaton.epsilonDetector get() = getModule(epsilonDetectorFactory)
val Automaton.hasEpsilonBinding get() = epsilonDetector.hasEpsilonBinding
val Automaton.hasEpsilon get() = epsilonDetector.hasEpsilon

class EpsilonDetector(automaton: Automaton) : AutomatonModule {
    val hasEpsilonBinding: BooleanBinding = isNotEmpty(automaton.transitions.filteredSet { transition ->
        transition.allProperties.countBinding { it.isEpsilonProperty }.booleanBinding { it!!.toInt() > 0 }
    })
    val hasEpsilon by hasEpsilonBinding
}
