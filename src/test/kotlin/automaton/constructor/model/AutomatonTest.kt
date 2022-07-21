package automaton.constructor.model

import automaton.constructor.model.automaton.AbstractAutomaton
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.data.AutomatonTypeData
import automaton.constructor.model.memory.MemoryUnit
import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.memory.MemoryUnitStatus.READY_TO_ACCEPT
import automaton.constructor.model.memory.MemoryUnitStatus.REQUIRES_TERMINATION
import automaton.constructor.model.module.AutomatonModule
import automaton.constructor.model.transition.Transition
import automaton.constructor.model.transition.storage.TransitionStorage
import automaton.constructor.model.transition.storage.createTransitionStorageTree
import io.mockk.*
import javafx.collections.ObservableSet
import javafx.geometry.Point2D
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.fail

class AutomatonTest {
    private lateinit var automaton: Automaton
    private lateinit var states: ObservableSet<State>
    private lateinit var transitions: ObservableSet<Transition>
    private lateinit var memoryUnitDescriptorMockks: List<MemoryUnitDescriptor>
    private lateinit var notAddedState: State

    companion object {
        const val MEMORY_UNIT_DESCRIPTOR_DISPLAY_NAME = "memory unit descriptor display name"
    }

    @BeforeEach
    fun init() {
        memoryUnitDescriptorMockks = List(2) {
            mockk(relaxed = true) {
                every { displayName } returns MEMORY_UNIT_DESCRIPTOR_DISPLAY_NAME
            }
        }
        every {
            memoryUnitDescriptorMockks[0].displayName = any()
        } answers { fail("Unexpected first memory unit descriptor renaming") }
        justRun { memoryUnitDescriptorMockks[1].displayName = any() }
        automaton = object : AbstractAutomaton("typeName", memoryUnitDescriptorMockks) {
            override fun getTypeData(): AutomatonTypeData = error("")
        }
        transitions = automaton.transitions
        states = automaton.states
        notAddedState = State("", Point2D.ZERO, memoryUnitDescriptorMockks)
    }

    @Test
    fun `second memory unit descriptor with the same name should be renamed`() {
        verify {
            memoryUnitDescriptorMockks[1].displayName = "$MEMORY_UNIT_DESCRIPTOR_DISPLAY_NAME 2"
        }
    }

    @Test
    fun `unnamed states should get default names`() {
        val state1 = automaton.addState()
        val state2 = automaton.addState()
        assertEquals("S0", state1.name)
        assertEquals("S1", state2.name)
    }

    @Test
    fun `named states shouldn't be renamed and should not affect automatic naming`() {
        val name1 = "name"
        val name2 = "S" + "9".repeat(100) // test for out of bounds
        val state1 = automaton.addState(name1)
        val state2 = automaton.addState(name2)
        val state3 = automaton.addState()
        assertEquals(name1, state1.name)
        assertEquals(name2, state2.name)
        assertEquals("S0", state3.name)
    }

    @Test
    fun `passing same factory to getModule should return the same module created by the factory`() {
        val module = mockk<AutomatonModule>()
        val moduleFactory = mockk<(Automaton) -> AutomatonModule>()
        every { moduleFactory(automaton) } returns module andThenAnswer { fail("Unexpected second factory call") }
        assertSame(module, automaton.getModule(moduleFactory))
        assertSame(module, automaton.getModule(moduleFactory))
    }

    @Test
    fun `there should be no possible transitions from not added state`() {
        assertEquals(emptySet(), automaton.getPossibleTransitions(notAddedState, mockk()))
    }

    @Test
    fun `there should be no pure transitions from not added state`() {
        assertEquals(emptySet(), automaton.getPureTransitions(notAddedState))
    }

    @Test
    fun `states should be empty before anything is added`() {
        assertEquals(emptySet(), states)
    }

    @Test
    fun `transitions should be empty before anything is added`() {
        assertEquals(emptySet(), transitions)
    }

    @Nested
    inner class AfterState1IsAdded {
        private lateinit var state1: State
        private lateinit var state1TransitionStorageMockk: TransitionStorage
        private lateinit var transitionsFromState1: ObservableSet<Transition>

        @BeforeEach
        fun init() {
            state1TransitionStorageMockk = mockk(relaxed = true)
            mockkStatic(::createTransitionStorageTree) {
                every { createTransitionStorageTree(memoryUnitDescriptorMockks) } returns state1TransitionStorageMockk
                state1 = automaton.addState()
            }
            transitionsFromState1 = automaton.getTransitionsFrom(state1)
        }

        @Test
        fun `getPureTransitions(state1) should return pure transitions from state1 transition storage`() {
            val pureTransitions = mockk<Set<Transition>>()
            every { state1TransitionStorageMockk.getPureTransitions() } returns pureTransitions
            assertSame(pureTransitions, automaton.getPureTransitions(state1))
        }

        @Test
        fun `getPossibleTransitions(state1) should return possible transitions from state1 transition storage accounting for memory unit running out of data`() {
            val memory = listOf<MemoryUnit>(
                mockk {
                    every { status } returns READY_TO_ACCEPT
                    every { getCurrentFilterValues() } returns listOf(1, 2, 3)
                },
                mockk {
                    every { status } returns REQUIRES_TERMINATION
                    every { descriptor } returns memoryUnitDescriptorMockks[1]
                }
            )
            every { memoryUnitDescriptorMockks[1].transitionFilters } returns listOf(mockk(), mockk())
            val possibleTransitions = mockk<Set<Transition>>()
            every {
                state1TransitionStorageMockk.getPossibleTransitions(listOf(1, 2, 3, null, null))
            } returns possibleTransitions
            assertSame(possibleTransitions, automaton.getPossibleTransitions(state1, memory))
        }

        @Test
        fun `transitionsFromState1 should be empty before transitions are added`() {
            assertEquals(emptySet(), transitionsFromState1)
        }

        @Test
        fun `states should contain single state1`() {
            assertEquals(setOf(state1), states)
        }

        @Nested
        inner class AfterState1LoopIsAdded {
            private lateinit var state1Loop: Transition

            @BeforeEach
            fun init() {
                state1Loop = automaton.addTransition(state1, state1)
            }

            @Test
            fun `state1Loop should be added to state1TransitionStorageMockk`() {
                verify { state1TransitionStorageMockk.addTransition(state1Loop) }
            }

            @Test
            fun `transitions should contain single transition state1Loop`() {
                assertEquals(setOf(state1Loop), transitions)
            }

            @Test
            fun `transitionsFromState1 should contain single transition state1Loop`() {
                assertEquals(setOf(state1Loop), transitionsFromState1)
            }

            @Nested
            inner class AfterState1LoopIsRemoved {
                @BeforeEach
                fun init() {
                    automaton.removeTransition(state1Loop)
                }

                @Test
                fun `state1Loop should be removed from state1TransitionStorageMockk`() {
                    verify { state1TransitionStorageMockk.removeTransition(state1Loop) }
                }

                @Test
                fun `transitions should be empty`() {
                    assertEquals(emptySet(), transitions)
                }

                @Test
                fun `transitionsFromState1 should be empty`() {
                    assertEquals(emptySet(), transitionsFromState1)
                }
            }

            @Nested
            inner class AfterState2AndState2ToState1TransitionIsAdded {
                private lateinit var state2ToState1Transition: Transition
                private lateinit var state2: State
                private lateinit var state2TransitionStorageMockk: TransitionStorage
                private lateinit var transitionsFromState2: ObservableSet<Transition>

                @BeforeEach
                fun init() {
                    state2TransitionStorageMockk = mockk(relaxed = true)
                    mockkStatic(::createTransitionStorageTree) {
                        every { createTransitionStorageTree(memoryUnitDescriptorMockks) } returns state2TransitionStorageMockk
                        state2 = automaton.addState()
                    }
                    state2ToState1Transition = automaton.addTransition(state2, state1)
                    transitionsFromState2 = automaton.getTransitionsFrom(state2)
                }

                @Test
                fun `states should contain state1 and state2`() {
                    assertEquals(setOf(state1, state2), states)
                }

                @Test
                fun `state2ToState1Transition should be added to state2TransitionStorageMockk`() {
                    verify { state2TransitionStorageMockk.addTransition(state2ToState1Transition) }
                }

                @Test
                fun `transitionsFromState2 should contain single state2ToState1Transition`() {
                    assertEquals(setOf(state2ToState1Transition), transitionsFromState2)
                }

                @Test
                fun `transitions should contain state1Loop and state2ToState1Transition`() {
                    assertEquals(setOf(state1Loop, state2ToState1Transition), transitions)
                }

                @Nested
                inner class AfterState1IsRemovedAndThenState2IsRemoved : AfterAutomatonIsCleared() {
                    @BeforeEach
                    fun init() {
                        automaton.removeState(state1)
                        automaton.removeState(state2)
                    }
                }

                @Nested
                inner class AfterState2IsRemovedAndThenState1IsRemoved : AfterAutomatonIsCleared() {
                    @BeforeEach
                    fun init() {
                        automaton.removeState(state2)
                        automaton.removeState(state1)
                    }
                }

                @Nested
                inner class AfterTransitionsAreRemovedAndThenState1IsRemovedAndThenState2IsRemoved :
                    AfterAutomatonIsCleared() {
                    @BeforeEach
                    fun init() {
                        automaton.removeTransition(state1Loop)
                        automaton.removeTransition(state2ToState1Transition)
                        automaton.removeState(state1)
                        automaton.removeState(state2)
                    }
                }

                abstract inner class AfterAutomatonIsCleared {
                    @Test
                    fun `states should be empty`() {
                        assertEquals(emptySet(), states)
                    }

                    @Test
                    fun `transitions should be empty`() {
                        assertEquals(emptySet(), transitions)
                    }

                    @Test
                    fun `transitionsFromState1 should be empty`() {
                        assertEquals(emptySet(), transitionsFromState1)
                    }

                    @Test
                    fun `transitionsFromState2 should be empty`() {
                        assertEquals(emptySet(), transitionsFromState2)
                    }

                    @Test
                    fun `state1Loop should be removed from state1TransitionStorageMockk`() {
                        verify { state1TransitionStorageMockk.removeTransition(state1Loop) }
                    }

                    @Test
                    fun `state2ToState1Transition should be removed from state2TransitionStorageMockk`() {
                        verify { state2TransitionStorageMockk.removeTransition(state2ToState1Transition) }
                    }

                    @Test
                    fun `unnamed states should get default names`() {
                        val state1 = automaton.addState()
                        val state2 = automaton.addState()
                        assertEquals("S0", state1.name)
                        assertEquals("S1", state2.name)
                    }
                }
            }
        }
    }
}
