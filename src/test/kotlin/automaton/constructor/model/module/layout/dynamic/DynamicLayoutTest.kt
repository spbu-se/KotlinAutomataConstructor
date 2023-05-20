package automaton.constructor.model.module.layout.dynamic

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.module.layout.AbstractLayoutTest
import org.junit.jupiter.api.Assumptions.assumeTrue
import kotlin.test.assertTrue

open class DynamicLayoutTest : AbstractLayoutTest() {
    override fun assertGoodLayout(automaton: Automaton) {
        assumeTrue(automaton.transitions.size > 0)
        assumeTrue(automaton.vertices.size > 1)

        val averageDist = automaton.vertices.sumOf { vertex1 ->
            automaton.vertices.sumOf { vertex2 ->
                vertex1.position.distance(vertex2.position)
            }
        } / automaton.vertices.size / (automaton.vertices.size - 1)
        val averageAdjacentDist = automaton.transitions.sumOf {
            it.source.position.distance(it.target.position)
        } / automaton.transitions.size

        assertTrue(
            averageDist >= averageAdjacentDist,
            "Average dist: $averageDist, average adjacent dist: $averageAdjacentDist"
        )
    }

    override fun layout(automaton: Automaton) {
        repeat(20 * 60) {
            automaton.dynamicLayout.step(DynamicLayoutPolicy.LAYOUT_ALL)
            automaton.dynamicLayout.sync(DynamicLayoutPolicy.LAYOUT_ALL)
        }
    }
}