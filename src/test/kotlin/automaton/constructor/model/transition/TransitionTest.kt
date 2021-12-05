package automaton.constructor.model.transition

import automaton.constructor.model.State
import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.transition.property.EPSILON_VALUE
import automaton.constructor.model.transition.property.TransitionProperty
import automaton.constructor.model.transition.property.TransitionPropertyDescriptor
import automaton.constructor.model.transition.property.TransitionPropertyGroup
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue


class TransitionTest {
    private lateinit var transition: Transition

    private val firstUnitFilters = emptyList<TransitionProperty<*>>()
    private val firstUnitFilterDescriptors = emptyList<TransitionPropertyDescriptor<*>>()

    private val firstUnitSideEffects = listOf<TransitionProperty<*>>(mockk("s11"), mockk("s12"))
    private val firstUnitSideEffectDescriptors = listOf<TransitionPropertyDescriptor<*>>(mockk("s11"), mockk("s12"))

    private val secondUnitFilters = listOf<TransitionProperty<*>>(mockk("f21"), mockk("f22"), mockk("f23"))
    private val secondUnitFilterDescriptors =
        listOf<TransitionPropertyDescriptor<*>>(mockk("f21"), mockk("f22"), mockk("f23"))

    private val secondUnitSideEffects = listOf<TransitionProperty<*>>(mockk("s21"))
    private val secondUnitSideEffectDescriptors = listOf<TransitionPropertyDescriptor<*>>(mockk("s21"))

    private val firstMemoryUnitDescriptor = mockk<MemoryUnitDescriptor> {
        every { filters } returns firstUnitFilterDescriptors
        every { sideEffects } returns firstUnitSideEffectDescriptors
    }

    private val secondMemoryUnitDescriptor = mockk<MemoryUnitDescriptor> {
        every { filters } returns secondUnitFilterDescriptors
        every { sideEffects } returns secondUnitSideEffectDescriptors
    }

    private val allProperties get() = firstUnitFilters + secondUnitFilters + firstUnitSideEffects + secondUnitSideEffects
    private val allPropertyDescriptors get() = firstUnitFilterDescriptors + secondUnitFilterDescriptors + firstUnitSideEffectDescriptors + secondUnitSideEffectDescriptors
    private val allPropertyAndDescriptorPairs get() = allProperties.zip(allPropertyDescriptors)

    @BeforeEach
    fun init() {
        allPropertyAndDescriptorPairs.forEach { (property, descriptor) ->
            every { descriptor.createProperty() } returns property andThenAnswer { fail("Unexpected second property creation") }
        }
        transition = Transition(State(), State(), listOf(firstMemoryUnitDescriptor, secondMemoryUnitDescriptor))
    }

    @Test
    fun `propertyGroups should have expected value`() {
        assertEquals(
            listOf(
                TransitionPropertyGroup(firstMemoryUnitDescriptor, firstUnitFilters, firstUnitSideEffects),
                TransitionPropertyGroup(secondMemoryUnitDescriptor, secondUnitFilters, secondUnitSideEffects)
            ),
            transition.propertyGroups
        )
    }

    @Test
    fun `filters should have expected value`() {
        assertEquals(
            firstUnitFilters + secondUnitFilters,
            transition.filters
        )
    }

    @Test
    fun `sideEffects should have expected value`() {
        assertEquals(
            firstUnitSideEffects + secondUnitSideEffects,
            transition.sideEffects
        )
    }

    @Test
    fun `allProperties should have expected value`() {
        assertEquals(
            allProperties.toSet(),
            transition.allProperties.toSet()
        )
    }

    @ParameterizedTest
    @MethodSource("getAllPropertyAndDescriptorPairs")
    fun `getProperty should return expected property`(propertyAndDescriptor: Pair<TransitionProperty<*>, TransitionPropertyDescriptor<*>>) {
        assertSame(propertyAndDescriptor.first, transition.getProperty(propertyAndDescriptor.second))
    }

    @Test
    fun `when all properties have EPSILON_VALUE then isPure should be true`() {
        allProperties.forEach {
            every { it.value } returns EPSILON_VALUE
        }
        assertTrue(transition.isPure())
    }

    @ParameterizedTest
    @MethodSource("getAllProperties")
    fun `when any property has non EPSILON_VALUE then isPure should be false`(property: TransitionProperty<*>) {
        allProperties.forEach {
            every { it.value } returns EPSILON_VALUE
        }
        every { property.value } returns Any()
        assertFalse(transition.isPure())
    }
}
