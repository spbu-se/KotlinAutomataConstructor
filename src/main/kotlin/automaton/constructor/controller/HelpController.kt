package automaton.constructor.controller

import tornadofx.*

class HelpController {

    companion object {
        private const val USER_DOCUMENTATION_URL =
            "https://github.com/spbu-se/KotlinAutomataConstructor/wiki/%D0%91%D1%8B%D1%81%D1%82%D1%80%D1%8B%D0%B9-%D1%81%D1%82%D0%B0%D1%80%D1%82"
        private const val README_URL = "https://github.com/spbu-se/KotlinAutomataConstructor/blob/main/README.md"
    }

    fun onUserDocumentation() {
        FX.application.hostServices.showDocument(USER_DOCUMENTATION_URL)
    }

    fun onREADME() {
        FX.application.hostServices.showDocument(README_URL)
    }
}
