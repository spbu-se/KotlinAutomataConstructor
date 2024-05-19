package automaton.constructor.view.algorithms

import automaton.constructor.controller.algorithms.HellingsAlgoController
import automaton.constructor.model.automaton.PushdownAutomaton
import automaton.constructor.model.data.createAutomaton
import automaton.constructor.model.data.getData
import automaton.constructor.model.element.*
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.HBox
import tornadofx.*

class EditableProduction(val leftSide: Nonterminal, val rightSide: SimpleObjectProperty<MutableList<CFGSymbol>>)

class HellingsLeftSideCell: TableCell<EditableProduction, Nonterminal>() {
    override fun updateItem(item: Nonterminal?, empty: Boolean) {
        super.updateItem(item, empty)
        graphic = if (item != null) {
            TextField().apply {
                promptText = "N"
                textProperty().addListener { _, _, newValue ->
                    item.value = newValue
                }
            }
        } else {
            null
        }
    }
}

class HellingsRightSideCell(
    private val grammar: ContextFreeGrammar,
    private val productions: ObservableList<EditableProduction>
): TableCell<EditableProduction, MutableList<CFGSymbol>>() {
    private fun getTextField(symbol: CFGSymbol): TextField {
        return TextField().apply {
            text = symbol.getSymbol()
            if (symbol is Terminal) {
                promptText = "T"
                textProperty().addListener { _, _, newValue ->
                    if (newValue.length > 1) {
                        this.textProperty().set(newValue[0].toString())
                    }
                    if (newValue.isNotEmpty()) {
                        symbol.value = newValue[0]
                    }
                }
            }
            if (symbol is Nonterminal) {
                promptText = "N"
                textProperty().addListener { _, _, newValue ->
                    symbol.value = newValue
                }
            }
        }
    }

    override fun updateItem(item: MutableList<CFGSymbol>?, empty: Boolean) {
        super.updateItem(item, empty)
        graphic = if (item != null) {
            HBox().apply {
                hbox {
                    item.forEachIndexed { index, _ ->
                        add(getTextField(item[index]))
                    }
                }
                add(ComboBox<String>().apply {
                    items = observableListOf("Terminal", "Nonterminal")
                    promptText = "Add"
                    setOnAction {
                        val productionRightSide = productions[index].rightSide.value
                        if (value == "Terminal") {
                            productions[index].rightSide.set((productionRightSide + Terminal('T')).toMutableList())
                        }
                        if (value == "Nonterminal") {
                            productions[index].rightSide.set((productionRightSide + grammar.addNonterminal()).toMutableList())
                        }
                    }
                })
            }
        } else {
            null
        }
    }
}

class HellingsAlgoGrammarView: View() {
    val controller: HellingsAlgoController by param()
    private val grammar = ContextFreeGrammar()
    private val productions = observableListOf<EditableProduction>()
    private val initialNonterminalValue = SimpleStringProperty()
    override val root = vbox {
        hbox {
            label("Initial nonterminal = ")
            textfield() {
                promptText = "N"
                textProperty().bindBidirectional(initialNonterminalValue)
            }
        }

        val grammarTableView = tableview(productions)
        val leftSideColumn = TableColumn<EditableProduction, Nonterminal>("Left side")
        val rightSideColumn = TableColumn<EditableProduction, MutableList<CFGSymbol>>("Right side")
        leftSideColumn.cellValueFactory = PropertyValueFactory("leftSide")
        leftSideColumn.setCellFactory { HellingsLeftSideCell() }
        rightSideColumn.setCellValueFactory { p0 ->
            p0!!.value.rightSide
        }
        rightSideColumn.setCellFactory { HellingsRightSideCell(grammar, productions) }
        grammarTableView.columns.addAll(leftSideColumn, rightSideColumn)

        hbox {
            button("Add").action {
                productions.add(EditableProduction(grammar.addNonterminal(), SimpleObjectProperty(mutableListOf())))
            }
            button("OK").action {
                controller.grammar = fixGrammar()
                controller.getInputGraph()
                close()
            }
            button("Convert automaton to CFG").action {
                if (controller.openedAutomaton !is PushdownAutomaton || (controller.openedAutomaton as PushdownAutomaton).stacks.size > 1) {
                    error("Algorithm is implemented only for pushdown automatons with a single stack!")
                } else {
                    val automatonCopy = controller.openedAutomaton.getData().createAutomaton() as PushdownAutomaton
                    controller.grammar = automatonCopy.convertToCFG()
                    controller.getInputGraph()
                    close()
                }
            }
        }
    }

    private fun fixGrammar(): ContextFreeGrammar {
        val fixedGrammar = ContextFreeGrammar()
        val initialNonterminal = Nonterminal(initialNonterminalValue.value)
        fixedGrammar.addNonterminal(initialNonterminal)
        fixedGrammar.initialNonterminal = initialNonterminal
        productions.forEach { production ->
            fixedGrammar.addNonterminal(production.leftSide)
            production.rightSide.value.forEach {
                if (it is Nonterminal) {
                    fixedGrammar.addNonterminal(it)
                }
            }
        }
        productions.forEach { production ->
            val newLeftSide = fixedGrammar.nonterminals.find { it.value == production.leftSide.value }!!
            val newRightSide = mutableListOf<CFGSymbol>()
            production.rightSide.value.forEach { symbol ->
                if (symbol is Nonterminal) {
                    newRightSide.add(fixedGrammar.nonterminals.find { it.value == symbol.value }!!)
                } else {
                    newRightSide.add(symbol)
                }
            }
            fixedGrammar.productions.add(Production(newLeftSide, newRightSide))
        }
        return fixedGrammar
    }
}