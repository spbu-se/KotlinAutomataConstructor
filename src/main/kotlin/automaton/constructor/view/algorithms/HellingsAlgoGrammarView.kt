package automaton.constructor.view.algorithms

import automaton.constructor.controller.algorithms.HellingsAlgoController
import automaton.constructor.model.element.*
import automaton.constructor.utils.I18N
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.HBox
import tornadofx.*

class EditableCFGSymbol(val cfgSymbol: CFGSymbol, var wasEdited: Boolean = false)
class EditableProduction(val leftSide: Nonterminal, val rightSide: SimpleObjectProperty<MutableList<EditableCFGSymbol>>)

class HellingsLeftSideCell(
    private val blankFieldsCount: SimpleIntegerProperty
): TableCell<EditableProduction, Nonterminal>() {
    override fun updateItem(item: Nonterminal?, empty: Boolean) {
        super.updateItem(item, empty)
        graphic = if (item != null) {
            TextField().apply {
                promptText = "N"
                textProperty().addListener { _, _, newValue ->
                    item.value = newValue
                    if (newValue.isEmpty()) {
                        blankFieldsCount.set(blankFieldsCount.value + 1)
                    } else {
                        blankFieldsCount.set(blankFieldsCount.value - 1)
                    }
                }
            }
        } else {
            null
        }
    }
}

class HellingsRightSideCell(
    private val grammar: ContextFreeGrammar,
    private val productions: ObservableList<EditableProduction>,
    private val blankFieldsCount: SimpleIntegerProperty
): TableCell<EditableProduction, MutableList<EditableCFGSymbol>>() {
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
                        symbol.cfgSymbol.value = newValue[0]
                    }
                }
            }
            if (symbol.cfgSymbol is Nonterminal) {
                promptText = "N"
                textProperty().addListener { _, _, newValue ->
                    symbol.cfgSymbol.value = newValue
                }
            }
            prefWidth = 73.0
            contextMenu = contextmenu {
                item(I18N.messages.getString("HellingsAlgorithm.Grammar.Delete")).setOnAction {
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
                add(ComboBox<String>().apply {
                    items = observableListOf(
                        I18N.messages.getString("HellingsAlgorithm.Grammar.Terminal"),
                        I18N.messages.getString("HellingsAlgorithm.Grammar.Nonterminal")
                    )
                    promptText = "+"
                    setOnAction {
                        val productionRightSide = productions[index].rightSide.value
                        if (value == items[0]) {
                            productions[index].rightSide.set((
                                    productionRightSide + EditableCFGSymbol(Terminal('T'))).toMutableList())
                        }
                        if (value == items[1]) {
                            productions[index].rightSide.set((
                                    productionRightSide + EditableCFGSymbol(grammar.addNonterminal())).toMutableList())
                        }
                    }
                    prefWidth = 55.0
                })
            }
        } else {
            null
        }
    }
}

class HellingsAlgoGrammarView: Fragment() {
    val controller: HellingsAlgoController by param()
    private val grammar = ContextFreeGrammar()
    private val productions = observableListOf<EditableProduction>()
    private val initialNonterminalValue = SimpleStringProperty()
    private var blankFieldsCount = SimpleIntegerProperty(1)
    override val root = borderpane {
        top = hbox {
            label(I18N.messages.getString("CFGView.InitialNonterminal") + " = ") {
                padding = Insets(4.0, 0.0, 0.0, 0.0)
            }
            textfield() {
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
            padding = Insets(5.0, 5.0, 5.0, 5.0)
        }

        val grammarTableView = tableview(productions)
        center = grammarTableView
        val leftSideColumn = TableColumn<EditableProduction, Nonterminal>(I18N.messages.getString("CFGView.LeftSide"))
        val rightSideColumn = TableColumn<EditableProduction, MutableList<EditableCFGSymbol>>(I18N.messages.getString("CFGView.RightSide"))
        leftSideColumn.cellValueFactory = PropertyValueFactory("leftSide")
        leftSideColumn.setCellFactory { HellingsLeftSideCell(blankFieldsCount) }
        rightSideColumn.setCellValueFactory { p0 ->
            p0!!.value.rightSide
        }
        rightSideColumn.setCellFactory { HellingsRightSideCell(grammar, productions, blankFieldsCount) }
        leftSideColumn.prefWidth = 80.0
        rightSideColumn.prefWidth = 430.0
        grammarTableView.columns.addAll(leftSideColumn, rightSideColumn)

        bottom = borderpane {
            left = hbox(5) {
                button(I18N.messages.getString("HellingsAlgorithm.Grammar.Add")).action {
                    productions.add(EditableProduction(grammar.addNonterminal(), SimpleObjectProperty(mutableListOf())))
                }
                button(I18N.messages.getString("HellingsAlgorithm.Grammar.OK")).action {
                    if (blankFieldsCount.value > 0 || productions.isEmpty()) {
                        error(I18N.messages.getString("HellingsAlgorithm.Grammar.Error"))
                    } else {
                        controller.grammar = fixGrammar()
                        controller.execute()
                        close()
                    }
                }
                padding = Insets(5.0, 5.0, 5.0, 5.0)
            }
        }

        prefWidth = 510.0
    }

    private fun fixGrammar(): ContextFreeGrammar {
        val fixedGrammar = ContextFreeGrammar()
        val initialNonterminal = Nonterminal(initialNonterminalValue.value)
        fixedGrammar.addNonterminal(initialNonterminal)
        fixedGrammar.initialNonterminal = initialNonterminal
        productions.forEach { production ->
            fixedGrammar.addNonterminal(production.leftSide)
            production.rightSide.value.forEach {
                if (it.cfgSymbol is Nonterminal) {
                    fixedGrammar.addNonterminal(it.cfgSymbol)
                }
            }
        }
        productions.forEach { production ->
            val newLeftSide = fixedGrammar.nonterminals.find { it.value == production.leftSide.value }!!
            val newRightSide = mutableListOf<CFGSymbol>()
            production.rightSide.value.forEach { symbol ->
                if (symbol.cfgSymbol is Nonterminal) {
                    newRightSide.add(fixedGrammar.nonterminals.find { it.value == symbol.cfgSymbol.value }!!)
                } else {
                    newRightSide.add(symbol.cfgSymbol)
                }
            }
            fixedGrammar.productions.add(Production(newLeftSide, newRightSide))
        }
        return fixedGrammar
    }
}