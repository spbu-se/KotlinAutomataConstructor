package automaton.constructor.controller

import automaton.constructor.utils.I18N
import tornadofx.*
import java.util.Locale

class LocaleController : Controller() {
    init {
        I18N.locale = config.string("locale")?.let { Locale.forLanguageTag(it) } ?: Locale.getDefault()
    }

    fun setLocale(locale: Locale) {
        config["locale"] = locale.toLanguageTag()
        config.save()
        information(
            I18N.messages.getString("LocaleController.RestartAppToApplyLanguageChange"),
            title = I18N.messages.getString("Dialog.information")
        )
    }

    val availableLocales get() = I18N.availableLocales
}
