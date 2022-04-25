package automaton.constructor.model.factory

import automaton.constructor.utils.SettingListEditor
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.testfx.framework.junit5.ApplicationTest
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class AutomatonFactoriesTest : ApplicationTest() {
    private val allAutomatonFactories get() = getAllAutomatonFactories()

    @ParameterizedTest
    @MethodSource("getAllAutomatonFactories")
    fun `automaton factory string representation should be capitalised`(automatonFactory: AutomatonFactory) =
        assertFalse(automatonFactory.toString().first().isLowerCase())

    @ParameterizedTest
    @MethodSource("getAllAutomatonFactories")
    fun `automaton factory editor should not be empty SettingListEditor`(automatonFactory: AutomatonFactory) {
        val editor = automatonFactory.createEditor()
        assumeTrue(editor is SettingListEditor)
        assertNotEquals(emptyList(), (editor as SettingListEditor).settings)
    }
}
