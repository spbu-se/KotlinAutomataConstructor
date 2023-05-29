package automaton.constructor.controller

import automaton.constructor.utils.I18N
import tornadofx.Controller
import java.util.*

class SettingsController : Controller() {
    enum class Hint(val key: String) {
        STARTUP("ShowHint.Startup"),
        DYNAMIC_LAYOUT("ShowHint.DynamicLayout");
    }

    fun enableAllHints() {
        Hint.values().forEach { setHintEnabled(it, isEnabled = true) }
    }

    fun isHintEnabled(hint: Hint): Boolean =
        config.boolean(hint.key, defaultValue = true)

    fun setHintEnabled(hint: Hint, isEnabled: Boolean) {
        config[hint.key] = isEnabled
        config.save()
    }

    fun initGlobalLocale() {
        I18N.locale = config.string("locale")?.let { Locale.forLanguageTag(it) } ?: Locale.getDefault()
    }

    fun setLocale(locale: Locale) {
        config["locale"] = locale.toLanguageTag()
        config.save()
    }

    val availableLocales get() = I18N.availableLocales
}