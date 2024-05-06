package automaton.constructor.view.algorithms

import automaton.constructor.controller.AlgorithmsController
import automaton.constructor.controller.algorithms.ConversionToCFGController
import automaton.constructor.model.element.CFGSymbol
import automaton.constructor.model.element.ContextFreeGrammar
import automaton.constructor.model.element.Nonterminal
import automaton.constructor.model.element.Production
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.HBox
import tornadofx.*

class LeftSideCell(val controller: ConversionToCFGController): TableCell<Production, Nonterminal>() {
    override fun updateItem(item: Nonterminal?, empty: Boolean) {
        super.updateItem(item, empty)
        graphic = if (item != null) {
            controller.getLabelsForNonterminal(item)
        } else {
            null
        }
    }
}

class RightSideCell(val controller: ConversionToCFGController): TableCell<Production, List<CFGSymbol>>() {
    override fun updateItem(item: List<CFGSymbol>?, empty: Boolean) {
        super.updateItem(item, empty)
        graphic = if (item != null) {
            HBox().apply {
                item.forEach {
                    if (it is Nonterminal) {
                        add(controller.getLabelsForNonterminal(it))
                    } else {
                        add(label(it.getSymbol()))
                    }
                }
            }
        } else {
            null
        }
    }
}

class ConversionToCFGView: View() {
    val grammar: ContextFreeGrammar by param()
    val controller: ConversionToCFGController by param()
    private val productionsTableView = tableview(grammar.productions.toObservable())
    private val leftSideColumn = TableColumn<Production, Nonterminal>("Left side")
    private val rightSideColumn = TableColumn<Production, List<CFGSymbol>>("Right side")

    init {
        leftSideColumn.cellValueFactory = PropertyValueFactory("leftSide")
        leftSideColumn.setCellFactory { LeftSideCell(controller) }
        rightSideColumn.cellValueFactory = PropertyValueFactory("rightSide")
        rightSideColumn.setCellFactory { RightSideCell(controller) }
        productionsTableView.columns.addAll(leftSideColumn, rightSideColumn)
    }

    override val root = vbox {
        hbox {
            label("Initial nonterminal = ")
            add(controller.getLabelsForNonterminal(grammar.initialNonterminal!!))
        }
        add(productionsTableView)

        style {
            fontSize = 15.0.px
        }
    }
}