package automaton.constructor.view.grammar

import automaton.constructor.model.element.CFGSymbol
import automaton.constructor.model.element.ContextFreeGrammar
import automaton.constructor.model.element.Nonterminal
import automaton.constructor.model.element.Terminal
import automaton.constructor.utils.I18N
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.scene.control.ChoiceBox
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.TextField
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.HBox
import tornadofx.*

open class EditableCFGSymbol(open val cfgSymbol: CFGSymbol, var wasEdited: Boolean = false)

class EditableNonterminal(
    nonterminal: Nonterminal, wasEdited: Boolean = false
) : EditableCFGSymbol(nonterminal, wasEdited) {
    override val cfgSymbol: Nonterminal = nonterminal
}


class EditableProduction(
    val leftSide: EditableNonterminal,
    val rightSide: SimpleObjectProperty<MutableList<EditableCFGSymbol>>
)


class InputCFGLeftSideCell(
    private val blankFieldsCount: SimpleIntegerProperty,
    private val indexesOfSelectedProductions: MutableSet<Int>
) : TableCell<EditableProduction, EditableNonterminal>() {
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


class InputCFGRightSideCell(
    private val grammar: ContextFreeGrammar,
    private val productions: ObservableList<EditableProduction>,
    private val blankFieldsCount: SimpleIntegerProperty
) : TableCell<EditableProduction, MutableList<EditableCFGSymbol>>() {
    private fun getTextField(symbol: EditableCFGSymbol): TextField {
        return TextField().apply {
            if (symbol.wasEdited) {
                text = symbol.cfgSymbol.getSymbol()
            }
            if (symbol.cfgSymbol is Terminal) {
                promptText = "T"
                textProperty().addListener { _, _, newValue ->
                    if (newValue.length > 1) {
                        this.textProperty().set(newValue[0].toString())
                    }
                    if (newValue.isNotEmpty()) {
                        (symbol.cfgSymbol as Terminal).value = newValue[0]
                    }
                }
            }
            if (symbol.cfgSymbol is Nonterminal) {
                promptText = "N"
                textProperty().addListener { _, _, newValue ->
                    (symbol.cfgSymbol as Nonterminal).value = newValue
                }
            }
            prefWidth = 73.0
            contextMenu = contextmenu {
                item(I18N.messages.getString("Grammar.Delete")).setOnAction {
                    val productionRightSide = productions[index].rightSide.value
                    if (text.isEmpty()) {
                        blankFieldsCount.set(blankFieldsCount.value - 1)
                    }
                    productions[index].rightSide.set((productionRightSide - symbol).toMutableList())
                }
            }
            textProperty().addListener { _, _, newValue ->
                symbol.wasEdited = true
                if (newValue.isEmpty()) {
                    blankFieldsCount.set(blankFieldsCount.value + 1)
                } else {
                    blankFieldsCount.set(blankFieldsCount.value - 1)
                }
            }
        }
    }

    override fun updateItem(item: MutableList<EditableCFGSymbol>?, empty: Boolean) {
        super.updateItem(item, empty)
        graphic = if (item != null) {
            HBox().apply {
                hbox(3) {
                    item.forEachIndexed { index, _ ->
                        add(getTextField(item[index]))
                    }
                    padding = Insets(0.0, 3.0, 0.0, 0.0)
                }
                add(ChoiceBox<String>().apply {
                    items = observableListOf(
                        I18N.messages.getString("HellingsAlgorithm.Grammar.Terminal"),
                        I18N.messages.getString("HellingsAlgorithm.Grammar.Nonterminal")
                    )
                    value = "+"
                    setOnAction {
                        val productionRightSide = productions[index].rightSide.value
                        if (value == items[0]) {
                            productions[index].rightSide.set(
                                (
                                        productionRightSide + EditableCFGSymbol(Terminal('T'))).toMutableList()
                            )
                        }
                        if (value == items[1]) {
                            productions[index].rightSide.set(
                                (
                                        productionRightSide + EditableCFGSymbol(grammar.addNonterminal())).toMutableList()
                            )
                        }
                        blankFieldsCount.set(blankFieldsCount.value + 1)
                    }
                    prefWidth = 55.0
                })
            }
        } else {
            null
        }
    }
}


open class GrammarInputView(val label: String, val errorMessage: String) : Fragment() {
    protected val grammar = ContextFreeGrammar()
    protected val productions = observableListOf<EditableProduction>()
    protected val initialNonterminalValue = SimpleStringProperty()
    protected var blankFieldsCount = SimpleIntegerProperty(1)
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

        val grammarTableView = tableview(productions)
        val indexesOfSelectedProductions = mutableSetOf<Int>()
        center = grammarTableView
        val leftSideColumn =
            TableColumn<EditableProduction, EditableNonterminal>(I18N.messages.getString("CFGView.LeftSide"))
        val rightSideColumn =
            TableColumn<EditableProduction, MutableList<EditableCFGSymbol>>(I18N.messages.getString("CFGView.RightSide"))
        leftSideColumn.cellValueFactory = PropertyValueFactory("leftSide")
        leftSideColumn.setCellFactory { InputCFGLeftSideCell(blankFieldsCount, indexesOfSelectedProductions) }
        rightSideColumn.setCellValueFactory { p0 ->
            p0!!.value.rightSide
        }
        rightSideColumn.setCellFactory { InputCFGRightSideCell(grammar, productions, blankFieldsCount) }
        leftSideColumn.prefWidth = 80.0
        rightSideColumn.prefWidth = 430.0
        grammarTableView.columns.addAll(leftSideColumn, rightSideColumn)

        bottom = borderpane {
            left = hbox(5) {
                button(I18N.messages.getString("Grammar.Add")).action {
                    productions.add(
                        EditableProduction(
                            EditableNonterminal(
                                grammar.addNonterminal()
                            ), SimpleObjectProperty(mutableListOf())
                        )
                    )
                    blankFieldsCount.set(blankFieldsCount.value + 1)
                }
                button(I18N.messages.getString("Grammar.Delete")).action {
                    val productionsToDelete = indexesOfSelectedProductions.map { productions[it] }
                    productionsToDelete.forEach { production ->
                        val productionBlankFieldsCount = (production.rightSide.value + production.leftSide).count {
                            !it.wasEdited || it.cfgSymbol.getSymbol().isEmpty()
                        }
                        blankFieldsCount -= productionBlankFieldsCount
                    }
                    productions.removeAll(productionsToDelete)
                    indexesOfSelectedProductions.clear()
                }
                button(I18N.messages.getString("Grammar.OK")).action {
                    okButtonAction()
                }
                padding = Insets(5.0, 5.0, 5.0, 5.0)
            }
        }
        prefWidth = 510.0
    }

    protected open fun okButtonAction() {
        if (blankFieldsCount.value > 0 || productions.isEmpty()) {
            error(errorMessage)
        } else {
            close()
        }
    }

}