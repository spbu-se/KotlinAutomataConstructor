package automaton.constructor.model

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.serializers.JsonAutomatonSerializer
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.div

object TestAutomatons {
    val BFS get() = getAutomatonFromJson("/bfs.atmtn")
    val BINARY_INCREMENT get() = getAutomatonFromJson("/binary-increment.atmtn")
    val EMPTY_INPUT_DETECTOR_WITH_EPSILON_LOOP get() = getAutomatonFromJson("/empty-input-detector-with-epsilon-loop.atmtn")
    val EVEN_PALINDROMES get() = getAutomatonFromJson("/even-palindromes.atmtn")
    val MEALY_REMOVE_ZEROES get() = getAutomatonFromJson("/mealy-remove-zeros.atmtn")
    val MOORE_IDENTITY get() = getAutomatonFromJson("/moore-identity.atmtn")
    val NO_FINAL_STATE get() = getAutomatonFromJson("/no-final-state.atmtn")
    val NO_INIT_STATE get() = getAutomatonFromJson("/no-init-state.atmtn")
    val NO_STATES get() = getAutomatonFromJson("/no-states.atmtn")
    val THREE_ZEROES_AND_ONE_ONE get() = getAutomatonFromJson("/three-zeros-and-one-one.atmtn")
    val TRANSITION_FROM_FINAL_STATE get() = getAutomatonFromJson("/transition-from-final-state.atmtn")

    private fun getAutomatonFromJson(path: String): Automaton {
        val file = File(requireNotNull(javaClass.getResource(path)) { "Missing resource $path" }.file)
        val tempFile = (Path(file.parent) / "temp.atmtn").toFile()
        val automaton = JsonAutomatonSerializer.deserialize(file).toAutomaton()
        JsonAutomatonSerializer.serialize(tempFile, automaton.toData()) // serialize it back to test serialization
        return JsonAutomatonSerializer.deserialize(tempFile).toAutomaton().also { tempFile.delete() }
    }
}
