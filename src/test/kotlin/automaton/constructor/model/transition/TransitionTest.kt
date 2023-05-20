package automaton.constructor.model.transition

import automaton.constructor.model.element.State
import automaton.constructor.model.element.Transition
import automaton.constructor.model.element.AutomatonElementTest
import automaton.constructor.model.property.DynamicProperty
import automaton.constructor.model.property.DynamicPropertyGroup
import automaton.constructor.model.property.EPSILON_VALUE
import io.mockk.every
import javafx.geometry.Point2D
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TransitionTest : AutomatonElementTest<Transition>() {
    override fun createAutomatonElement() = Transition(
        State(emptyList(), "", Point2D.ZERO), // source properties shouldn't affect transition properties
        State(memoryDescriptors, "", Point2D.ZERO),
        memoryDescriptors
    )

    override val expectedPropertyGroups
        get() = listOf(
            DynamicPropertyGroup(
                firstMemoryUnitDescriptor.displayName,
                firstUnitTransitionFilters,
                firstUnitTransitionSideEffects
            ),
            DynamicPropertyGroup(
                secondMemoryUnitDescriptor.displayName,
                secondUnitTransitionFilters,
                secondUnitTransitionSideEffects
            )
        )

    override val expectedFilters get() = firstUnitTransitionFilters + secondUnitTransitionFilters
    override val expectedSideEffects get() = firstUnitTransitionSideEffects + secondUnitTransitionSideEffects
    override val expectedAllProperties get() = allTransitionProperties
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
