package automaton.constructor.model.module.layout.static

import automaton.constructor.model.TestAutomatons
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.module.layout.AbstractLayoutTest
import javafx.geometry.BoundingBox
import org.eclipse.elk.core.RecursiveGraphLayoutEngine
import org.eclipse.elk.core.util.NullElkProgressMonitor
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.Test
import kotlin.test.assertTrue

abstract class AbstractStaticLayoutTest(private val layout: StaticLayout) : AbstractLayoutTest() {
    override fun assertGoodLayout(automaton: Automaton) {
        val (forwardTransitions, backwardTransitions) = automaton.transitions.partition {
            it.source.position.x <= it.target.position.x
        }
        assertTrue(
            backwardTransitions.size <= forwardTransitions.size,
            "Backward transitions: ${backwardTransitions.size}, forward transitions: ${forwardTransitions.size}"
        )
        if (automaton.transitions.size % 2 == 1)
            assertTrue(
                backwardTransitions.size < forwardTransitions.size,
                "Backward transitions: ${backwardTransitions.size}, forward transitions: ${forwardTransitions.size}"
            )
    }

    override fun layout(automaton: Automaton) {
        val transitionLayoutBounds = automaton.transitions.associateWith {
            BoundingBox(0.0, 0.0, 100.0, 50.0)
        }
        val elkGraphMapping = automaton.toElkGraphMapping(transitionLayoutBounds)
        layout.configureLayout(elkGraphMapping.elkGraph)
        RecursiveGraphLayoutEngine().layout(elkGraphMapping.elkGraph, NullElkProgressMonitor())
        automaton.applyLayout(elkGraphMapping, transitionLayoutBounds)
    }

    @Test
    fun `layout should not throw if automaton is modified during layout`() = assertDoesNotThrow {
        val automaton = TestAutomatons.BINARY_ADDITION
        val transitionLayoutBounds = automaton.transitions.associateWith {
            BoundingBox(0.0, 0.0, 100.0, 50.0)
        }
        val elkGraphMapping = automaton.toElkGraphMapping(transitionLayoutBounds)
        layout.configureLayout(elkGraphMapping.elkGraph)

        // modifying automaton
        automaton.vertices.toList().forEach { automaton.removeVertex(it) }
        automaton.addTransition(automaton.addState(), automaton.addState())

        RecursiveGraphLayoutEngine().layout(elkGraphMapping.elkGraph, NullElkProgressMonitor())
        automaton.applyLayout(elkGraphMapping, transitionLayoutBounds)
    }

    @Test
    fun `layout should not throw if automaton and elkGraphMapping are modified during layout`() = assertDoesNotThrow {
        val automaton = TestAutomatons.BINARY_ADDITION
        val oldTransitionLayoutBounds = automaton.transitions.associateWith {
            BoundingBox(0.0, 0.0, 100.0, 50.0)
        }
        val oldElkGraphMapping = automaton.toElkGraphMapping(oldTransitionLayoutBounds)
        layout.configureLayout(oldElkGraphMapping.elkGraph)

        // modifying automaton
        automaton.vertices.toList().forEach { automaton.removeVertex(it) }
        automaton.addTransition(automaton.addState(), automaton.addState())

        val newTransitionLayoutBounds = automaton.transitions.associateWith {
            BoundingBox(0.0, 0.0, 100.0, 50.0)
        }

        val newElkGraphMapping = automaton.toElkGraphMapping(newTransitionLayoutBounds)

        RecursiveGraphLayoutEngine().layout(newElkGraphMapping.elkGraph, NullElkProgressMonitor())
        automaton.applyLayout(newElkGraphMapping, newTransitionLayoutBounds)
    }
}