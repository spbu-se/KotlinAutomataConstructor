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

import javafx.beans.property.Property
import javafx.concurrent.Task
import javafx.event.Event
import javafx.event.EventHandler

operator fun <T : Event> EventHandler<in T>?.plus(other: EventHandler<T>) = this?.let {
    EventHandler<T> {
        handle(it)
        other.handle(it)
    }
} ?: other

operator fun <T : Event> Property<EventHandler<in T>>.plusAssign(other: EventHandler<T>) {
    value += other
}

infix fun <T> Task<T>.addOnSuccess(func: (T) -> Unit) = apply { onSucceeded += { func(value) } }
infix fun <T> Task<T>.addOnFail(func: (Throwable) -> Unit) = apply { onFailed += { func(exception) } }
infix fun <T> Task<T>.addOnCancel(func: () -> Unit) = apply { onCancelled += { func() } }
