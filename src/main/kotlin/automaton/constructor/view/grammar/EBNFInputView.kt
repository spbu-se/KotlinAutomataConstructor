package automaton.constructor.view.grammar

import automaton.constructor.controller.grammar.RecursiveAutomatonGrammarInputController
import automaton.constructor.model.grammar.EBNFGrammar
import automaton.constructor.model.grammar.Nonterminal
import automaton.constructor.model.grammar.RARegex
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
        graphic = if (item != null && !empty) {
            HBox().apply {
                checkbox().apply {
                    action {
                        if (isSelected) indexesOfSelectedProductions.add(index)
                        else indexesOfSelectedProductions.remove(index)
                    }
                }
                textfield {
                    promptText = "N"
                    if (item.wasEdited) text = item.cfgSymbol.value
                    textProperty().addListener { _, _, newValue ->
                        val prevEmpty = item.cfgSymbol.value.isEmpty()
                        item.cfgSymbol.value = newValue
                        item.wasEdited = true
                        val nowEmpty = newValue.isEmpty()
                        if (prevEmpty && !nowEmpty) blankFieldsCount.set(blankFieldsCount.value - 1)
                        if (!prevEmpty && nowEmpty) blankFieldsCount.set(blankFieldsCount.value + 1)
                    }
                }
                spacing = 3.0
            }
        } else null
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
        val value = item.value ?: ""
        if (currentTextField == null) {
            currentTextField = TextField().apply {
                promptText = "e.g. (T | a) | N"
                prefWidth = 400.0
                text = value
                focusedProperty().addListener { _, wasFocused, isFocused ->
                    if (wasFocused && !isFocused) commitText(item)
                }
                setOnAction {
                    commitText(item)
                    runLater { requestFocus() }
                }
            }
        } else if (currentTextField?.text != value) {
            currentTextField?.text = value
        }
        graphic = HBox(currentTextField).apply {
            spacing = 5.0
            padding = Insets(2.0)
            isFocusTraversable = false
        }
    }

    private fun commitText(prop: SimpleObjectProperty<String>) {
        val oldVal = prop.value ?: ""
        val newVal = currentTextField?.text ?: ""
        if (oldVal.isEmpty() && newVal.isNotEmpty()) blankFieldsCount.set(blankFieldsCount.value - 1)
        if (oldVal.isNotEmpty() && newVal.isEmpty()) blankFieldsCount.set(blankFieldsCount.value + 1)
        prop.set(newVal)
    }

    override fun startEdit() {
        super.startEdit()
        currentTextField?.requestFocus()
    }
}

class EBNFInputView :
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
                        if (newValue.isEmpty()) blankFieldsCount.set(blankFieldsCount.value + 1)
                        else blankFieldsCount.set(blankFieldsCount.value - 1)
                    }
                    prefWidth = 73.0
                }
            }
            padding = Insets(5.0)
        }

        val grammarTableView = tableview(ebnfProductions)
        center = grammarTableView

        val leftSideColumn =
            TableColumn<EditableEBNFProduction, EditableNonterminal>(I18N.messages.getString("CFGView.LeftSide"))
        val rightSideColumn =
            TableColumn<EditableEBNFProduction, SimpleObjectProperty<String>>(I18N.messages.getString("CFGView.RightSide"))

        leftSideColumn.cellValueFactory = PropertyValueFactory("leftSide")
        leftSideColumn.setCellFactory { EBNFInputLeftSideCell(blankFieldsCount, indexesOfSelectedProductions) }

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
                            EditableNonterminal(ebnfGrammar.addNonterminal()), SimpleObjectProperty("")
                        )
                    )
                    blankFieldsCount.set(blankFieldsCount.value + 1)
                }
                button(I18N.messages.getString("Grammar.Delete")).action {
                    val toDelete = indexesOfSelectedProductions.mapNotNull { idx -> ebnfProductions.getOrNull(idx) }
                    toDelete.forEach { prod ->
                        val leftBlank = !prod.leftSide.wasEdited || prod.leftSide.cfgSymbol.value.isEmpty()
                        val rightBlank = prod.rightSide.value.isNullOrEmpty()
                        if (leftBlank) blankFieldsCount.set(blankFieldsCount.value - 1)
                        if (rightBlank) blankFieldsCount.set(blankFieldsCount.value - 1)
                    }
                    ebnfProductions.removeAll(toDelete)
                    indexesOfSelectedProductions.clear()
                }
                button(I18N.messages.getString("Grammar.OK")).action {
                    okButtonAction()
                }
                padding = Insets(5.0)
            }
        }
        prefWidth = 510.0
    }

    override fun okButtonAction() {
        if (blankFieldsCount.value > 0 || ebnfProductions.isEmpty()) {
            error(errorMessage)
            return
        }
        ebnfGrammar.clearProductions()
        val canonical = mutableMapOf<String, Nonterminal>()
        fun canon(name: String): Nonterminal = canonical.getOrPut(name) { ebnfGrammar.findOrAddNonterminal(name) }

        val initialName = initialNonterminalValue.value
        if (!initialName.isNullOrBlank()) {
            ebnfGrammar.initialNonterminal = canon(initialName)
            ebnfGrammar.declaredInitialName = initialName
        } else {
            error(I18N.messages.getString("Grammar.Error"))
            return
        }

        val parseErrors = mutableListOf<String>()
        ebnfProductions.forEach { p ->
            val leftName = p.leftSide.cfgSymbol.value
            val rhsText = p.rightSide.value ?: ""
            if (leftName.isNotBlank() && rhsText.isNotBlank()) {
                try {
                    val regex = RARegex.parse(rhsText) ?: throw IllegalArgumentException("Empty regex")
                    ebnfGrammar.addProduction(canon(leftName), regex)
                } catch (e: Exception) {
                    parseErrors += "$leftName -> $rhsText : ${e.message ?: "parse error"}"
                }
            }
        }

        if (parseErrors.isNotEmpty()) {
            error(
                I18N.messages.getString("Grammar.Error") + "\n" + parseErrors.joinToString("\n")
            )
            return
        }

        controller.grammar = ebnfGrammar
        controller.onGrammarEdited()
        close()
    }
}