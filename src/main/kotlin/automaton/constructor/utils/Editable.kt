package automaton.constructor.utils

import javafx.scene.Node
import javafx.scene.control.TextField
import tornadofx.*

interface Editable {
    /**
     * Human-readable name
     */
    val displayName: String

    /**
     * Node for editing this instance or null if this instance can't be edited
     */
    fun createEditor(): Node?
}

abstract class MonospaceEditableString(initValue: String = "") : Editable {
    val valueProperty = initValue.toProperty()
    var value: String by valueProperty

    override fun createEditor(): Node? = createTextFieldEditor()

    fun createTextFieldEditor() = TextField().apply {
        monospaced()
        textProperty().bindBidirectional(valueProperty)
    }
}
