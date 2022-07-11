package automaton.constructor.controller

import tornadofx.*

class AutomatonDocumentationController {

    companion object {
        private const val userDocumentationURL =
            "https://docs.google.com/document/d/1jhqQSpF-SMvZJMpAzzRWi49u15uQ_wBPstUS369gO-Y/edit?usp=sharing"
        private const val readmeURL = "https://github.com/spbu-se/KotlinAutomataConstructor/blob/main/README.md"
    }

    fun onUserDocumentation() {
        FX.application.hostServices.showDocument(userDocumentationURL)
    }

    fun onREADME() {
        FX.application.hostServices.showDocument(readmeURL)
    }
}