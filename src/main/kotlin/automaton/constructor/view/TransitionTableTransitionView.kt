package automaton.constructor.view

import automaton.constructor.model.element.Transition
import automaton.constructor.utils.I18N
import automaton.constructor.utils.Setting
import automaton.constructor.utils.SettingGroup
import automaton.constructor.utils.createUnmodifiableSettingControl
import javafx.scene.paint.Color
import tornadofx.*

class TransitionTableTransitionView(transition: Transition): TableTransitionView(transition) {
    init {
        hbox {
            label {
                textProperty().bind(transition.target.nameProperty)
                textLength = text.length
                textProperty().addListener(ChangeListener { _, oldValue, newValue ->
                    textLength = textLength - oldValue.length + newValue.length
                })
                textFill = Color.BLACK
            }
            if (transition.sideEffectsText.isNotEmpty()) {
                label(";")
                textLength += 1
                label {
                    textProperty().bind(transition.sideEffectsTextBinding)
                    textLength += text.length
                    textProperty().addListener(ChangeListener { _, oldValue, newValue ->
                        textLength = textLength - oldValue.length + newValue.length
                    })
                }
            }
        }
    }
}