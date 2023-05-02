package automaton.constructor.controller

import automaton.constructor.utils.I18N
import tornadofx.Controller
import tornadofx.UIComponent
import tornadofx.information
import java.util.*

class LocaleController(private val uiComponent: UIComponent?) : Controller() {
    fun initGlobalLocale() {
        I18N.locale = config.string("locale")?.let { Locale.forLanguageTag(it) } ?: Locale.getDefault()
    }

    fun setLocale(locale: Locale) {
        config["locale"] = locale.toLanguageTag()
        config.save()
        information(
            I18N.messages.getString("LocaleController.RestartAppToApplyLanguageChange"),
            title = I18N.messages.getString("Dialog.information"),
            owner = uiComponent?.currentWindow
        )
    }

    val availableLocales get() = I18N.availableLocales
}
