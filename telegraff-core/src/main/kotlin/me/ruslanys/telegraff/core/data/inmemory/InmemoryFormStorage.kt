/**
 * Copyright © 2018-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.ruslanys.telegraff.core.data.inmemory

import me.ruslanys.telegraff.core.data.FormStorage
import me.ruslanys.telegraff.core.dsl.Form
import me.ruslanys.telegraff.core.dto.TelegramMessage

class InmemoryFormStorage(
    forms: List<Form<TelegramMessage, InmemoryFormState>>
) : FormStorage<TelegramMessage, InmemoryFormState> {

    private val forms: MutableMap<String, Form<TelegramMessage, InmemoryFormState>> = hashMapOf()

    init {
        for (form in forms) {
            for (commandName in form.commands) {
                val previousValue = this.forms.put(commandName.lowercase(), form)
                if (previousValue != null) {
                    throw IllegalArgumentException("$commandName(ignore case) is already in use.")
                }
            }
        }
    }

    override fun findByMessage(message: TelegramMessage): Form<TelegramMessage, InmemoryFormState>? {
        val messageText = message.text?.lowercase() ?: return null
        val item = forms.entries.firstOrNull {
            messageText.startsWith(it.key)
        }
        return item?.value
    }

    override fun existByMessage(message: TelegramMessage): Boolean {
        val messageText = message.text?.lowercase() ?: return false
        return forms.any {
            messageText.startsWith(it.key)
        }
    }
}
