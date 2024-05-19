package automaton.constructor.view.algorithms

import automaton.constructor.controller.algorithms.HellingsAlgoController
import automaton.constructor.model.automaton.PushdownAutomaton
import automaton.constructor.model.data.createAutomaton
import automaton.constructor.model.data.getData
import automaton.constructor.model.element.*
import automaton.constructor.utils.I18N
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.geometry.Pos
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
            prefWidth = 73.0
        }
    }

    override fun updateItem(item: MutableList<CFGSymbol>?, empty: Boolean) {
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
                            productions[index].rightSide.set((productionRightSide + Terminal('T')).toMutableList())
                        }
                        if (value == items[1]) {
                            productions[index].rightSide.set((productionRightSide + grammar.addNonterminal()).toMutableList())
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
    override val root = borderpane {
        top = hbox {
            label(I18N.messages.getString("CFGView.InitialNonterminal") + " = ") {
                padding = Insets(4.0, 0.0, 0.0, 0.0)
            }
            textfield() {
                promptText = "N"
                textProperty().bindBidirectional(initialNonterminalValue)
                prefWidth = 73.0
            }
            padding = Insets(5.0, 5.0, 5.0, 5.0)
        }

        val grammarTableView = tableview(productions)
        center = grammarTableView
        val leftSideColumn = TableColumn<EditableProduction, Nonterminal>(I18N.messages.getString("CFGView.LeftSide"))
        val rightSideColumn = TableColumn<EditableProduction, MutableList<CFGSymbol>>(I18N.messages.getString("CFGView.RightSide"))
        leftSideColumn.cellValueFactory = PropertyValueFactory("leftSide")
        leftSideColumn.setCellFactory { HellingsLeftSideCell() }
        rightSideColumn.setCellValueFactory { p0 ->
            p0!!.value.rightSide
        }
        rightSideColumn.setCellFactory { HellingsRightSideCell(grammar, productions) }
        leftSideColumn.prefWidth = 80.0
        rightSideColumn.prefWidth = 430.0
        grammarTableView.columns.addAll(leftSideColumn, rightSideColumn)

        bottom = borderpane {
            left = hbox(5) {
                button(I18N.messages.getString("HellingsAlgorithm.Grammar.Add")).action {
                    productions.add(EditableProduction(grammar.addNonterminal(), SimpleObjectProperty(mutableListOf())))
                }
                button(I18N.messages.getString("HellingsAlgorithm.Grammar.OK")).action {
                    controller.grammar = fixGrammar()
                    controller.getInputGraph()
                    close()
                }
                padding = Insets(5.0, 5.0, 5.0, 5.0)
            }

            right = hbox {
                button(I18N.messages.getString("HellingsAlgorithm.Grammar.Convert")).action {
                    if (controller.openedAutomaton !is PushdownAutomaton || (controller.openedAutomaton as PushdownAutomaton).stacks.size > 1) {
                        error(I18N.messages.getString("CFGView.Error"))
                    } else {
                        val automatonCopy = controller.openedAutomaton.getData().createAutomaton() as PushdownAutomaton
                        controller.grammar = automatonCopy.convertToCFG()
                        controller.getInputGraph()
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