package automaton.constructor.view.grammar

import automaton.constructor.utils.I18N
import automaton.constructor.controller.grammar.RecursiveAutomatonGrammarInputController
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Insets
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.HBox
import tornadofx.*


open class EditableEBNFSymbol(open val ebnfSymbol: Char, var wasEdited: Boolean = false)

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

class EBNFInputRightCell {
//    TODO: implement right side cell for EBNF productions with text input field instead of T/N choice
}


class EBNFInputView() :
    GrammarInputView(I18N.messages.getString("EBNF.Input.Info"), I18N.messages.getString("EBNF.Input.Error")) {
    val controller: RecursiveAutomatonGrammarInputController by param()
    val EBNFproductions = observableListOf<EditableEBNFProduction>()

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

        val grammarTableView = tableview(EBNFproductions)
        center = grammarTableView

        // left side column for EBNF productions
        val indexesOfSelectedProductions = mutableSetOf<Int>()
        val leftSideColumn =
            TableColumn<EditableEBNFProduction, EditableNonterminal>(I18N.messages.getString("CFGView.LeftSide"))
        leftSideColumn.cellValueFactory = PropertyValueFactory("leftSide")
        leftSideColumn.setCellFactory { EBNFInputLeftSideCell(blankFieldsCount, indexesOfSelectedProductions) }

        // right side column for EBNF productions
        val rightSideColumn = TableColumn<EditableEBNFProduction, String>(I18N.messages.getString("CFGView.RightSide"))
        rightSideColumn.cellValueFactory = PropertyValueFactory("rightSide")
        // TODO: `rightSideColumn.setCellFactory { EBNFInputRightCell(...) }`
        leftSideColumn.prefWidth = 80.0
        rightSideColumn.prefWidth = 430.0
        grammarTableView.columns.addAll(leftSideColumn, rightSideColumn)

        bottom = borderpane {
            left = hbox {
                button(I18N.messages.getString("HellingsAlgorithm.Grammar.Add")).action {
                    EBNFproductions.add(
                        EditableEBNFProduction(
                            EditableNonterminal(grammar.addNonterminal()), SimpleObjectProperty("")
                        )
                    )
                    blankFieldsCount.set(blankFieldsCount.value + 1)
                }
                button(I18N.messages.getString("HellingsAlgorithm.Grammar.Delete")).action {
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
                button(I18N.messages.getString("HellingsAlgorithm.Grammar.OK")).action {
                    okButtonAction()
                }
                padding = Insets(5.0, 5.0, 5.0, 5.0)
            }
        }
        prefWidth = 510.0
    }

    override fun okButtonAction() {
        if (blankFieldsCount.value > 0 || productions.isEmpty()) {
            error(errorMessage)
        } else {
            controller.grammar = grammar
            controller.onGrammarEdited()
            close()
        }
    }
}