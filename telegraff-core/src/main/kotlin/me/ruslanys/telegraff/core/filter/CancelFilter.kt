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
package me.ruslanys.telegraff.core.filter

import me.ruslanys.telegraff.core.annotation.TelegramFilterOrder
import me.ruslanys.telegraff.core.component.TelegramApi
import me.ruslanys.telegraff.core.dto.TelegramMessage
import me.ruslanys.telegraff.core.dto.request.MarkdownMessage

@TelegramFilterOrder(0)
class CancelFilter(private val telegramApi: TelegramApi, private val handlersFilter: HandlersFilter): TelegramFilter {

    override fun handleMessage(message: TelegramMessage, chain: TelegramFilterChain) {
        val text = message.text?.lowercase() ?: ""
        if (text.startsWith("/cancel") || text.startsWith("отмена")) {
            handlersFilter.clearState(message.chat)
            telegramApi.sendMessage(MarkdownMessage("Хорошо, давай начнем сначала", chatId = message.chat.id))
        } else {
            chain.doFilter(message)
        }
    }

}