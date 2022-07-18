package automaton.constructor.model.module.tape

import automaton.constructor.model.memory.MemoryUnit
import javafx.scene.layout.GridPane

abstract class AbstractTape(val tracks: List<Track>) : MemoryUnit {
    override fun getCurrentFilterValues() = tracks.map { it.current }

    override fun createEditor() = GridPane().apply {
        tracks.forEachIndexed { i, track ->
            add(track.createProcessedCharsEditor(), 0, i)
            add(track.createCurrentCharEditor(), 1, i)
            add(track.createUnprocessedCharsEditor(), 2, i)
        }
    }
}
