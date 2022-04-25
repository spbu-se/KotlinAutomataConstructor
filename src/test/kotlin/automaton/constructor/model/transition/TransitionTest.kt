package automaton.constructor.model.transition

import automaton.constructor.model.State
import automaton.constructor.model.property.AutomatonElementTest
import automaton.constructor.model.property.DynamicProperty
import automaton.constructor.model.property.DynamicPropertyGroup
import automaton.constructor.model.property.EPSILON_VALUE
import io.mockk.every
import io.mockk.mockk
import javafx.geometry.Point2D
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TransitionTest : AutomatonElementTest<Transition>() {
    override fun createAutomatonElement() = Transition(
        mockk(relaxed = false), // source shouldn't affect transition properties
        State("", Point2D.ZERO, memoryDescriptors),
        memoryDescriptors
    )

    override val expectedPropertyGroups
        get() = listOf(
            DynamicPropertyGroup(
                firstMemoryUnitDescriptor,
                firstUnitTransitionFilters,
                firstUnitTransitionSideEffects
            ),
            DynamicPropertyGroup(
                secondMemoryUnitDescriptor,
                secondUnitTransitionFilters,
                secondUnitTransitionSideEffects
            )
        )

    override val expectedFilters get() = firstUnitTransitionFilters + secondUnitTransitionFilters + firstUnitStateFilters + secondUnitStateFilters
    override val expectedSideEffects get() = firstUnitTransitionSideEffects + secondUnitTransitionSideEffects + firstUnitStateSideEffects + secondUnitStateSideEffects
    override val expectedAllProperties get() = allTransitionProperties + allStateProperties
    override val allPropertyAndDescriptorPairs get() = allTransitionPropertyAndDescriptorPairs

    @Test
    fun `when all properties have EPSILON_VALUE then isPure should be true`() {
        (allTransitionProperties + allStateProperties).forEach {
            every { it.value } returns EPSILON_VALUE
        }
        assertTrue(automatonElement.isPure())
    }

    private val allProperties get() = allTransitionProperties + allStateProperties

    @ParameterizedTest
    @MethodSource("getAllProperties")
    fun `when any property has non EPSILON_VALUE then isPure should be false`(property: DynamicProperty<*>) {
        allTransitionProperties.forEach {
            every { it.value } returns EPSILON_VALUE
        }
        every { property.value } returns Any()
        assertFalse(automatonElement.isPure())
    }
}
