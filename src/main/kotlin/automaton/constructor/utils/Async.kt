/*
MIT License

Copyright (c) 2021 Ilya Muravjov (https://github.com/IlyaMuravjov), Egor Denisov (https://github.com/Lev0nid), Timofey Zaynulin (https://github.com/Tizain)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package automaton.constructor.utils

import javafx.concurrent.Task
import javafx.stage.Modality
import javafx.stage.StageStyle
import tornadofx.*

fun <T> UIComponent.runAsyncWithDialog(title: String, daemon: Boolean, func: FXTask<*>.() -> T): Task<T> {
    lateinit var task: Task<T>
    val dialog = dialog(
        title = title,
        modality = Modality.NONE,
        stageStyle = StageStyle.UTILITY
    ) {
        stage.isResizable = false
        stage.setOnCloseRequest { task.cancel() }
        currentWindow?.let { window ->
            stage.x = window.x + window.width - stage.width - 30.0
            stage.y = window.y + window.height - stage.height - 30.0
        }
    }
    task = runAsync(func = func, daemon = daemon)
        .success {
            dialog?.close()
        }.fail {
            dialog?.close()
        }.cancel {
            dialog?.close()
        }
    return task
}
