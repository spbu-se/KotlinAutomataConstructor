package automaton.constructor.view.algorithms

import automaton.constructor.controller.algorithms.ConversionToCFGController
import automaton.constructor.model.element.CFGSymbol
import automaton.constructor.model.element.ContextFreeGrammar
import automaton.constructor.model.element.Nonterminal
import automaton.constructor.model.element.Production
import automaton.constructor.utils.I18N
import automaton.constructor.utils.getLabelsForNonterminal
import javafx.geometry.Insets
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.HBox
import tornadofx.*

class LeftSideCell: TableCell<Production, Nonterminal>() {
    override fun updateItem(item: Nonterminal?, empty: Boolean) {
        super.updateItem(item, empty)
        graphic = if (item != null) {
            getLabelsForNonterminal(item)
        } else {
            null
        }
    }
}

class RightSideCell: TableCell<Production, List<CFGSymbol>>() {
    override fun updateItem(item: List<CFGSymbol>?, empty: Boolean) {
        super.updateItem(item, empty)
        graphic = if (item != null) {
            HBox().apply {
                item.forEach {
                    if (it is Nonterminal) {
                        add(getLabelsForNonterminal(it))
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

class CFGView: Fragment() {
    val grammar: ContextFreeGrammar by param()
    private val productionsTableView = tableview(grammar.productions.toObservable())
    private val leftSideColumn = TableColumn<Production, Nonterminal>(I18N.messages.getString("CFGView.LeftSide"))
    private val rightSideColumn = TableColumn<Production, List<CFGSymbol>>(I18N.messages.getString("CFGView.RightSide"))

    init {
        leftSideColumn.cellValueFactory = PropertyValueFactory("leftSide")
        leftSideColumn.setCellFactory { LeftSideCell() }
        rightSideColumn.cellValueFactory = PropertyValueFactory("rightSide")
        rightSideColumn.setCellFactory { RightSideCell() }
        leftSideColumn.minWidth = 150.0
        rightSideColumn.minWidth = 150.0
        productionsTableView.columns.addAll(leftSideColumn, rightSideColumn)
    }

    override val root = vbox {
        hbox {
            label(I18N.messages.getString("CFGView.InitialNonterminal") + " = ") {
                padding = Insets(5.0, 0.0, 5.0, 5.0)
            }
            add(getLabelsForNonterminal(grammar.initialNonterminal!!).apply {
                padding = Insets(5.0, 5.0, 5.0, 0.0)
            })
        }
        add(productionsTableView)
    }
}