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

import me.ruslanys.telegraff.core.data.FormStateStorage
import me.ruslanys.telegraff.core.dsl.Form
import me.ruslanys.telegraff.core.dto.TelegramMessage
import java.util.concurrent.ConcurrentHashMap

open class InmemoryFormStateStorage : FormStateStorage<TelegramMessage, InmemoryFormState> {

    protected val states: MutableMap<Pair<Long, Long>, InmemoryFormState> = ConcurrentHashMap()

    override fun findByMessage(message: TelegramMessage): InmemoryFormState? = states[message.chatId to message.fromId]

    override fun storeAnswer(state: InmemoryFormState, formStepKey: String, answer: Any) {
        state.answers[formStepKey] = answer
    }

    override fun doNextStep(state: InmemoryFormState) {
        state.currentStep = state.currentStep
            ?.next
            ?.invoke(state)
            ?.let { state.form.getStepByKey(it) }
    }

    override fun removeByMessage(message: TelegramMessage) {
        states.remove(message.chatId to message.fromId)
    }

    override fun create(message: TelegramMessage, form: Form<TelegramMessage, InmemoryFormState>): InmemoryFormState {
        val newState = InmemoryFormState(message.chatId, message.fromId, form)
        states[message.chatId to message.fromId] = newState
        return newState
    }

    override fun remove(state: InmemoryFormState) {
        states.remove(state.chatId to state.fromId)
    }

    override fun existByMessage(message: TelegramMessage): Boolean {
        return states.containsKey(message.chatId to message.fromId)
    }
}
