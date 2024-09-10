package automaton.constructor.view.algorithms

import automaton.constructor.model.element.CFGSymbol
import automaton.constructor.model.element.ContextFreeGrammar
import automaton.constructor.model.element.Nonterminal
import automaton.constructor.model.element.Production
import automaton.constructor.utils.I18N
import javafx.geometry.Insets
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.HBox
import javafx.scene.text.Font
import tornadofx.*

class LeftSideCell: TableCell<Production, Nonterminal>() {
    override fun updateItem(item: Nonterminal?, empty: Boolean) {
        super.updateItem(item, empty)
        graphic = if (item != null) {
            CFGView.getLabelsForNonterminal(item)
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
                        add(CFGView.getLabelsForNonterminal(it))
                    } else {
                        add(label(it.getSymbol()))
                    }
                }
                if (item.isEmpty()) {
                    add(label("ε"))
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
        leftSideColumn.minWidth = 190.0
        rightSideColumn.minWidth = 190.0
        productionsTableView.columns.addAll(leftSideColumn, rightSideColumn)
        if (productionsTableView.items.isEmpty()) {
            productionsTableView.items.add(Production(grammar.initialNonterminal, mutableListOf()))
        }
    }

    override val root = vbox {
        label(I18N.messages.getString("CFGView.Note")) {
            padding = Insets(5.0, 5.0, 5.0, 5.0)
        }
        hbox {
            label(I18N.messages.getString("CFGView.InitialNonterminal") + " = ") {
                padding = Insets(5.0, 0.0, 5.0, 5.0)
            }
            add(getLabelsForNonterminal(grammar.initialNonterminal).apply {
                padding = Insets(5.0, 5.0, 5.0, 0.0)
            })
        }
        add(productionsTableView)
    }

    companion object {
        fun getLabelsForNonterminal(nonterminal: Nonterminal): HBox {
            return HBox().apply {
                this.label(nonterminal.value[0].toString())
                this.label(nonterminal.value.subSequence(1, nonterminal.value.length).toString()) {
                    font = Font(9.0)
                }
            }
        }
    }
}
