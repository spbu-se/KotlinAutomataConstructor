package automaton.constructor.view.grammar

import automaton.constructor.controller.grammar.RecursiveAutomatonGrammarInputController
import automaton.constructor.model.element.EBNFGrammar
import automaton.constructor.model.element.Nonterminal
import automaton.constructor.utils.I18N
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Insets
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.TextField
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.HBox
import tornadofx.*


class EditableEBNFProduction(
    val leftSide: EditableNonterminal, val rightSide: SimpleObjectProperty<String>
)

class EBNFInputLeftSideCell(
    private val blankFieldsCount: SimpleIntegerProperty, private val indexesOfSelectedProductions: MutableSet<Int>
) : TableCell<EditableEBNFProduction, EditableNonterminal>() {
    override fun updateItem(item: EditableNonterminal?, empty: Boolean) {
        super.updateItem(item, empty)
        graphic = if (item != null) {
            HBox().apply {
                checkbox().apply {
                    action {
                        if (isSelected) {
                            indexesOfSelectedProductions.add(index)
                        } else {
                            indexesOfSelectedProductions.remove(index)
                        }
                    }
                }
                textfield {
                    promptText = "N"
                    if (item.wasEdited) {
                        text = item.cfgSymbol.value
                    }
                    textProperty().addListener { _, _, newValue ->
                        item.cfgSymbol.value = newValue
                        item.wasEdited = true
                        if (newValue.isEmpty()) {
                            blankFieldsCount.set(blankFieldsCount.value + 1)
                        } else {
                            blankFieldsCount.set(blankFieldsCount.value - 1)
                        }
                    }
                }
                spacing = 3.0
            }
        } else {
            null
        }
    }
}

class EBNFInputRightCell(
    private val blankFieldsCount: SimpleIntegerProperty
) : TableCell<EditableEBNFProduction, SimpleObjectProperty<String>>() {
    private var currentTextField: TextField? = null

    override fun updateItem(item: SimpleObjectProperty<String>?, empty: Boolean) {
        super.updateItem(item, empty)

        if (item == null || empty) {
            graphic = null
            currentTextField = null
            return
        }

        val stringValue = item.value ?: ""

        if (currentTextField == null) {
            currentTextField = TextField().apply {
                promptText = "e.g. (T | a) | N"
                prefWidth = 400.0
                text = stringValue

                focusedProperty().addListener { _, wasFocused, isFocused ->
                    if (wasFocused && !isFocused) {
                        updateModel(text, item)
                    }
                }

                setOnAction {
                    updateModel(text, item)
                    runLater { requestFocus() }
                }
            }
        } else {
            if (currentTextField?.text != stringValue) {
                currentTextField?.text = stringValue
            }
        }

        graphic = HBox(currentTextField).apply {
            spacing = 5.0
            padding = Insets(2.0)
            isFocusTraversable = false
        }
    }

    private fun updateModel(newValue: String, property: SimpleObjectProperty<String>) {
        val oldValue = property.value ?: ""

        if (oldValue.isEmpty() && newValue.isNotEmpty()) {
            blankFieldsCount.set(blankFieldsCount.value - 1)
        } else if (oldValue.isNotEmpty() && newValue.isEmpty()) {
            blankFieldsCount.set(blankFieldsCount.value + 1)
        }

        property.set(newValue)
    }

    override fun startEdit() {
        super.startEdit()
        currentTextField?.requestFocus()
    }
}


class EBNFInputView() :
    GrammarInputView(I18N.messages.getString("EBNF.Input.Info"), I18N.messages.getString("Grammar.Error")) {
    private val ebnfGrammar = EBNFGrammar()
    val controller: RecursiveAutomatonGrammarInputController by param()
    private val ebnfProductions = observableListOf<EditableEBNFProduction>()
    private val indexesOfSelectedProductions = mutableSetOf<Int>()

    override val root = borderpane {
        top = vbox(5.0) {
            label(label)
            hbox {
                label(I18N.messages.getString("CFGView.InitialNonterminal") + " = ") {
                    padding = Insets(4.0, 0.0, 0.0, 0.0)
                }
                textfield {
                    promptText = "N"
                    textProperty().bindBidirectional(initialNonterminalValue)
                    textProperty().addListener { _, _, newValue ->
                        if (newValue.isEmpty()) {
                            blankFieldsCount.set(blankFieldsCount.value + 1)
                        } else {
                            blankFieldsCount.set(blankFieldsCount.value - 1)
                        }
                    }
                    prefWidth = 73.0
                }
            }
            padding = Insets(5.0, 5.0, 5.0, 5.0)
        }

        val grammarTableView = tableview(ebnfProductions)
        center = grammarTableView

        // left side column for EBNF productions
        val leftSideColumn =
            TableColumn<EditableEBNFProduction, EditableNonterminal>(I18N.messages.getString("CFGView.LeftSide"))
        leftSideColumn.cellValueFactory = PropertyValueFactory("leftSide")
        leftSideColumn.setCellFactory { EBNFInputLeftSideCell(blankFieldsCount, indexesOfSelectedProductions) }

        // right side column for EBNF productions
        val rightSideColumn =
            TableColumn<EditableEBNFProduction, SimpleObjectProperty<String>>(I18N.messages.getString("CFGView.RightSide"))
        rightSideColumn.cellValueFactory = PropertyValueFactory("rightSide")
        rightSideColumn.setCellFactory { EBNFInputRightCell(blankFieldsCount) }

        leftSideColumn.prefWidth = 80.0
        rightSideColumn.prefWidth = 430.0
        grammarTableView.columns.addAll(leftSideColumn, rightSideColumn)

        bottom = borderpane {
            left = hbox {
                button(I18N.messages.getString("Grammar.Add")).action {
                    ebnfProductions.add(
                        EditableEBNFProduction(
                            EditableNonterminal(grammar.addNonterminal()), SimpleObjectProperty("")
                        )
                    )
                    blankFieldsCount.set(blankFieldsCount.value + 1)
                }
                button(I18N.messages.getString("Grammar.Delete")).action {
                    val productionsToDelete = indexesOfSelectedProductions.map { ebnfProductions[it] }
                    productionsToDelete.forEach { production ->
                        val isLeftSideBlank =
                            !production.leftSide.wasEdited || production.leftSide.cfgSymbol.value.isEmpty()
                        val isRightSideBlank = production.rightSide.value.isNullOrEmpty()

                        if (isLeftSideBlank) blankFieldsCount.set(blankFieldsCount.value - 1)
                        if (isRightSideBlank) blankFieldsCount.set(blankFieldsCount.value - 1)
                    }
                    ebnfProductions.removeAll(productionsToDelete)
                    indexesOfSelectedProductions.clear()
                }
                button(I18N.messages.getString("Grammar.OK")).action { okButtonAction() }
                padding = Insets(5.0, 5.0, 5.0, 5.0)
            }
        }
        prefWidth = 510.0
    }

    override fun okButtonAction() {
        if (blankFieldsCount.value > 0 || ebnfProductions.isEmpty()) {
            error(errorMessage)
        } else {
            // Rebuild grammar fresh
            ebnfGrammar.clearProductions()
            val canonical = mutableMapOf<String, Nonterminal>()
            fun canon(name: String): Nonterminal = canonical.getOrPut(name) { ebnfGrammar.findOrAddNonterminal(name) }
            val initialName = initialNonterminalValue.value
            if (!initialName.isNullOrEmpty()) {
                ebnfGrammar.initialNonterminal = canon(initialName)
                ebnfGrammar.declaredInitialName = initialName
            }
            ebnfProductions.forEach { production ->
                val leftSideName = production.leftSide.cfgSymbol.value
                val rightSide = production.rightSide.value ?: ""
                if (leftSideName.isNotEmpty() && rightSide.isNotEmpty()) {
                    ebnfGrammar.addProduction(canon(leftSideName), rightSide)
                }
            }
            controller.grammar = ebnfGrammar
            controller.onGrammarEdited()
            close()
        }
    }
}
