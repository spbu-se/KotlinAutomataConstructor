package automaton.constructor.model.module

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.property.FormalRegex
import automaton.constructor.utils.countBinding
import automaton.constructor.utils.filteredSet
import javafx.beans.binding.Bindings.isNotEmpty
import javafx.beans.binding.BooleanBinding
import tornadofx.booleanBinding

private val regexDetectorFactory = { automaton: Automaton -> RegexDetector(automaton) }
val Automaton.regexDetector get() = getModule(regexDetectorFactory)
val Automaton.hasRegexesBinding get() = regexDetector.hasRegexesBinding
val Automaton.hasRegexes: Boolean get() = hasRegexesBinding.value

class RegexDetector(automaton: Automaton) : AutomatonModule {
    val hasRegexesBinding: BooleanBinding = isNotEmpty(automaton.transitions.filteredSet { transition ->
        transition.allProperties.countBinding { property ->
            property.booleanBinding { it is FormalRegex && it !is FormalRegex.Singleton }
        }.booleanBinding { it!!.toInt() > 0 }
    }).or(
        isNotEmpty(automaton.buildingBlocks.filteredSet { it.subAutomaton.hasRegexesBinding })
    )
}
