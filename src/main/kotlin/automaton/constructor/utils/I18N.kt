package automaton.constructor.utils

import java.util.*

object I18N {
    /**
     * List of supported Locales
     */
    private val locales = listOf<Locale>(Locale.ENGLISH)

    val labels: ResourceBundle = ResourceBundle.getBundle("messages", getDefaultLocale())

    private fun getDefaultLocale(): Locale {
        val systemDefault = Locale(System.getProperty("user.language"))
        return if (locales.contains(systemDefault)) systemDefault else Locale.ENGLISH
    }
}