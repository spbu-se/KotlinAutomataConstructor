package automaton.constructor.model.property

import automaton.constructor.model.element.AutomatonElement
import automaton.constructor.model.memory.MemoryUnitDescriptor
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals
import kotlin.test.assertSame

abstract class AutomatonElementTest<T : AutomatonElement> {
    protected lateinit var memoryDescriptors: List<MemoryUnitDescriptor>
    protected lateinit var automatonElement: T

    protected val firstUnitTransitionFilters = emptyList<DynamicProperty<*>>()
    protected val firstUnitTransitionFilterDescriptors = emptyList<DynamicPropertyDescriptor<*>>()

    protected val firstUnitTransitionSideEffects = listOf<DynamicProperty<*>>(mockk("ts11", relaxed = true), mockk("ts12", relaxed = true))
    protected val firstUnitTransitionSideEffectDescriptors =
        listOf<DynamicPropertyDescriptor<*>>(mockk("ts11"), mockk("ts12"))

    protected val firstUnitStateSideEffects = listOf<DynamicProperty<*>>(mockk("ss11", relaxed = true), mockk("ss12", relaxed = true))
    protected val firstUnitStateSideEffectDescriptors =
        listOf<DynamicPropertyDescriptor<*>>(mockk("ss11"), mockk("ss12"))

    protected val secondUnitTransitionFilters = listOf<DynamicProperty<*>>(mockk("tf21", relaxed = true), mockk("tf22", relaxed = true), mockk("tf23", relaxed = true))
    protected val secondUnitTransitionFilterDescriptors =
        listOf<DynamicPropertyDescriptor<*>>(mockk("tf21"), mockk("tf22"), mockk("tf23"))

    protected val secondUnitTransitionSideEffects = listOf<DynamicProperty<*>>(mockk("ts21", relaxed = true))
    protected val secondUnitTransitionSideEffectDescriptors = listOf<DynamicPropertyDescriptor<*>>(mockk("ts21"))

    protected val secondUnitStateSideEffects = emptyList<DynamicProperty<*>>()
    protected val secondUnitStateSideEffectDescriptors = emptyList<DynamicPropertyDescriptor<*>>()

    protected val firstMemoryUnitDescriptor = mockk<MemoryUnitDescriptor> {
        every { displayName } returns "firstMemoryUnitDescriptor"
        every { transitionFilters } returns firstUnitTransitionFilterDescriptors
        every { transitionSideEffects } returns firstUnitTransitionSideEffectDescriptors
        every { stateSideEffects } returns firstUnitStateSideEffectDescriptors
    }

    protected val secondMemoryUnitDescriptor = mockk<MemoryUnitDescriptor> {
        every { displayName } returns "secondMemoryUnitDescriptor"
        every { transitionFilters } returns secondUnitTransitionFilterDescriptors
        every { transitionSideEffects } returns secondUnitTransitionSideEffectDescriptors
        every { stateSideEffects } returns secondUnitStateSideEffectDescriptors
    }

    protected val allTransitionProperties get() = firstUnitTransitionFilters + secondUnitTransitionFilters + firstUnitTransitionSideEffects + secondUnitTransitionSideEffects
    protected val allTransitionPropertyDescriptors get() = firstUnitTransitionFilterDescriptors + secondUnitTransitionFilterDescriptors + firstUnitTransitionSideEffectDescriptors + secondUnitTransitionSideEffectDescriptors
    protected val allTransitionPropertyAndDescriptorPairs
        get() = allTransitionProperties.zip(allTransitionPropertyDescriptors)

    protected val allStateProperties get() = firstUnitStateSideEffects + secondUnitStateSideEffects
    protected val allStatePropertyDescriptors get() = firstUnitStateSideEffectDescriptors + secondUnitStateSideEffectDescriptors
    protected val allStatePropertyAndDescriptorPairs
        get() = allStateProperties.zip(allStatePropertyDescriptors)

    protected abstract fun createAutomatonElement(): T

    @BeforeEach
    fun init() {
        (allTransitionPropertyAndDescriptorPairs + allStatePropertyAndDescriptorPairs).forEach { (property, descriptor) ->
            every { descriptor.createProperty() } returns property andThenAnswer { fail("Unexpected second property creation") }
        }
        memoryDescriptors = listOf(firstMemoryUnitDescriptor, secondMemoryUnitDescriptor)
        automatonElement = createAutomatonElement()
    }

    protected abstract val expectedPropertyGroups: List<DynamicPropertyGroup>

    @Test
    fun `propertyGroups should have expected value`() =
        assertEquals(expectedPropertyGroups, automatonElement.propertyGroups)

    protected abstract val expectedFilters: List<DynamicProperty<*>>

    @Test
    fun `filters should have expected value`() = assertEquals(expectedFilters, automatonElement.filters)

    protected abstract val expectedSideEffects: List<DynamicProperty<*>>

    @Test
    fun `sideEffects should have expected value`() =
        assertEquals(expectedSideEffects, automatonElement.sideEffects)

    protected abstract val expectedAllProperties: List<DynamicProperty<*>>

    @Test
    fun `allProperties should have expected value`() =
        assertEquals(expectedAllProperties.toSet(), automatonElement.allProperties.toSet())

    protected abstract val allPropertyAndDescriptorPairs: List<Pair<DynamicProperty<*>, DynamicPropertyDescriptor<*>>>

    @ParameterizedTest
    @MethodSource("getAllPropertyAndDescriptorPairs")
    fun `getProperty should return expected property`(propertyAndDescriptor: Pair<DynamicProperty<*>, DynamicPropertyDescriptor<*>>) {
        assertSame(propertyAndDescriptor.first, automatonElement.getProperty(propertyAndDescriptor.second))
    }
}
