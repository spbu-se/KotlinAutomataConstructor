package automaton.constructor.model.transition

import automaton.constructor.model.State
import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.property.DynamicProperty
import automaton.constructor.model.property.DynamicPropertyDescriptor
import automaton.constructor.model.property.DynamicPropertyGroup
import automaton.constructor.model.property.EPSILON_VALUE
import io.mockk.every
import io.mockk.mockk
import javafx.geometry.Point2D
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

    private val firstUnitFilters = emptyList<DynamicProperty<*>>()
    private val firstUnitFilterDescriptors = emptyList<DynamicPropertyDescriptor<*>>()

    private val firstUnitSideEffects = listOf<DynamicProperty<*>>(mockk("s11"), mockk("s12"))
    private val firstUnitSideEffectDescriptors = listOf<DynamicPropertyDescriptor<*>>(mockk("s11"), mockk("s12"))

    private val secondUnitFilters = listOf<DynamicProperty<*>>(mockk("f21"), mockk("f22"), mockk("f23"))
    private val secondUnitFilterDescriptors =
        listOf<DynamicPropertyDescriptor<*>>(mockk("f21"), mockk("f22"), mockk("f23"))

    private val secondUnitSideEffects = listOf<DynamicProperty<*>>(mockk("s21"))
    private val secondUnitSideEffectDescriptors = listOf<DynamicPropertyDescriptor<*>>(mockk("s21"))

    private val firstMemoryUnitDescriptor = mockk<MemoryUnitDescriptor> {
        every { transitionFilters } returns firstUnitFilterDescriptors
        every { transitionSideEffects } returns firstUnitSideEffectDescriptors
        every { stateFilters } returns emptyList()
        every { stateSideEffects } returns emptyList()
    }

    private val secondMemoryUnitDescriptor = mockk<MemoryUnitDescriptor> {
        every { transitionFilters } returns secondUnitFilterDescriptors
        every { transitionSideEffects } returns secondUnitSideEffectDescriptors
        every { stateFilters } returns emptyList()
        every { stateSideEffects } returns emptyList()
    }

    private val allProperties get() = firstUnitFilters + secondUnitFilters + firstUnitSideEffects + secondUnitSideEffects
    private val allPropertyDescriptors get() = firstUnitFilterDescriptors + secondUnitFilterDescriptors + firstUnitSideEffectDescriptors + secondUnitSideEffectDescriptors
    private val allPropertyAndDescriptorPairs get() = allProperties.zip(allPropertyDescriptors)

    @BeforeEach
    fun init() {
        allPropertyAndDescriptorPairs.forEach { (property, descriptor) ->
            every { descriptor.createProperty() } returns property andThenAnswer { fail("Unexpected second property creation") }
        }
        val memoryDescriptors = listOf(firstMemoryUnitDescriptor, secondMemoryUnitDescriptor)
        transition = Transition(
            State("", Point2D.ZERO, memoryDescriptors),
            State("", Point2D.ZERO, memoryDescriptors),
            memoryDescriptors
        )
    }

    @Test
    fun `propertyGroups should have expected value`() {
        assertEquals(
            listOf(
                DynamicPropertyGroup(firstMemoryUnitDescriptor, firstUnitFilters, firstUnitSideEffects),
                DynamicPropertyGroup(secondMemoryUnitDescriptor, secondUnitFilters, secondUnitSideEffects)
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
    fun `getProperty should return expected property`(propertyAndDescriptor: Pair<DynamicProperty<*>, DynamicPropertyDescriptor<*>>) {
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
    fun `when any property has non EPSILON_VALUE then isPure should be false`(property: DynamicProperty<*>) {
        allProperties.forEach {
            every { it.value } returns EPSILON_VALUE
        }
        every { property.value } returns Any()
        assertFalse(transition.isPure())
    }
}
