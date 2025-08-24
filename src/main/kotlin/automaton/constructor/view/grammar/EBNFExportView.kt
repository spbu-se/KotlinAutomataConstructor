package automaton.constructor.view.grammar

import automaton.constructor.model.grammar.EBNFGrammar
import automaton.constructor.model.grammar.ProductionInterface
import automaton.constructor.model.grammar.Nonterminal
import automaton.constructor.utils.I18N
import javafx.geometry.Insets
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.HBox
import tornadofx.*

class EBNFExportLeftCell : TableCell<ProductionInterface<String>, Nonterminal>() {
    override fun updateItem(item: Nonterminal?, empty: Boolean) {
        super.updateItem(item, empty)
        graphic = if (item != null) EBNFExportView.getLabelsForNonterminal(item) else null
    }
}

class EBNFExportRightCell : TableCell<ProductionInterface<String>, String>() {
    override fun updateItem(item: String?, empty: Boolean) {
        super.updateItem(item, empty)
        text = if (item == null || empty) null else item
    }
}

class EBNFExportView : Fragment() {
    val grammar: EBNFGrammar by param()
    val warnings: List<String> by param(listOf())
    private val productionsTableView = tableview(grammar.productions.toObservable())
    private val leftSideColumn = TableColumn<ProductionInterface<String>, Nonterminal>(I18N.messages.getString("CFGView.LeftSide"))
    private val rightSideColumn = TableColumn<ProductionInterface<String>, String>(I18N.messages.getString("CFGView.RightSide"))

    init {
        leftSideColumn.cellValueFactory = PropertyValueFactory("leftSide")
        leftSideColumn.setCellFactory { EBNFExportLeftCell() }
        rightSideColumn.cellValueFactory = PropertyValueFactory("rightSide")
        rightSideColumn.setCellFactory { EBNFExportRightCell() }
        leftSideColumn.minWidth = 150.0
        rightSideColumn.minWidth = 250.0
        productionsTableView.columns.addAll(leftSideColumn, rightSideColumn)
    }

    override val root = vbox(5.0) {
        label(I18N.messages.getString("CFGView.InitialNonterminal") + " = ") {
            padding = Insets(5.0, 0.0, 0.0, 5.0)
        }
        hbox {
            add(getLabelsForNonterminal(grammar.initialNonterminal).apply { padding = Insets(0.0,5.0,5.0,5.0) })
        }
        if (warnings.isNotEmpty()) {
            textarea(warnings.joinToString("\n")) {
                isEditable = false
                prefRowCount = minOf(4, warnings.size)
            }
        }
        add(productionsTableView)
    }

    companion object {
        fun getLabelsForNonterminal(nonterminal: Nonterminal): HBox = HBox().apply {
            label(nonterminal.value)
        }
    }
}
