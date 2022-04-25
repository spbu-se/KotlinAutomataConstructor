package automaton.constructor.model

import automaton.constructor.model.property.AutomatonElementTest
import automaton.constructor.model.property.DynamicPropertyGroup
import javafx.geometry.Point2D
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class StateTest : AutomatonElementTest<State>() {
    companion object {
        private val INIT_POSITION = Point2D(1.0, 2.0)
        private val NEW_POSITION = Point2D(2.0, 3.0)
    }

    override fun createAutomatonElement() = State("", INIT_POSITION, memoryDescriptors)

    override val expectedPropertyGroups
        get() = listOf(
            DynamicPropertyGroup(
                firstMemoryUnitDescriptor,
                firstUnitStateFilters,
                firstUnitStateSideEffects
            ),
            DynamicPropertyGroup(
                secondMemoryUnitDescriptor,
                secondUnitStateFilters,
                secondUnitStateSideEffects
            )
        )

    override val expectedFilters get() = firstUnitStateFilters + secondUnitStateFilters
    override val expectedSideEffects get() = firstUnitStateSideEffects + secondUnitStateSideEffects
    override val expectedAllProperties get() = allStateProperties
    override val allPropertyAndDescriptorPairs get() = allStatePropertyAndDescriptorPairs

    @Test
    fun `updating lastReleasePosition should update position`() {
        automatonElement.lastReleasePosition = NEW_POSITION
        assertEquals(NEW_POSITION, automatonElement.position)
    }

    @Test
    fun `updating position shouldn't update lastReleasePosition`() {
        automatonElement.position = NEW_POSITION
        assertEquals(INIT_POSITION, automatonElement.lastReleasePosition)
    }

    @Test
    fun `lastReleasePosition should initially be equal to position`() =
        assertEquals(INIT_POSITION, automatonElement.lastReleasePosition)
}
