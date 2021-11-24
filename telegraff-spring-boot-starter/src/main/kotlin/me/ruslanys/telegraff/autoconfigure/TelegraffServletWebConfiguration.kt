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
package me.ruslanys.telegraff.autoconfigure

import com.pengrad.telegrambot.TelegramBot
import me.ruslanys.telegraff.autoconfigure.property.TelegraffProperties
import me.ruslanys.telegraff.component.client.TelegraffPollingBot
import me.ruslanys.telegraff.component.client.TelegraffWebhookBot
import me.ruslanys.telegraff.component.client.TelegramClient
import me.ruslanys.telegraff.component.telegrambots.*
import me.ruslanys.telegraff.core.data.FormStateStorage
import me.ruslanys.telegraff.core.data.FormStorage
import me.ruslanys.telegraff.core.exception.AbstractFormExceptionHandler
import me.ruslanys.telegraff.core.handler.CompositeMessageHandler
import me.ruslanys.telegraff.core.handler.ConditionalMessageHandler
import me.ruslanys.telegraff.core.handler.DefaultCompositeMessageHandler
import me.ruslanys.telegraff.core.handler.FormMessageHandler
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.objects.Message

/**
 * Configuration for Telegraff when used in a servlet web context.
 *
 * @author Ruslan Molchanov
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(CompositeMessageHandler::class)
@AutoConfigureAfter(WebMvcAutoConfiguration::class)
open class TelegraffServletWebConfiguration(private val telegramProperties: TelegraffProperties) {

    @Bean
    @ConditionalOnMissingBean(name = ["telegramProperties"])
    open fun telegramProperties(): TelegraffProperties = telegramProperties

    // region Clients

    @Bean
    @ConditionalOnMissingBean(TelegramClient::class)
    @ConditionalOnProperty(name = ["telegram.mode"], havingValue = "polling", matchIfMissing = true)
    open fun telegramPollingClient(
        compositeMessageHandler: CompositeMessageHandler<Message>,
    ): TelegraffPollingBot {
        return TelegraffPollingBot(telegramProperties(), compositeMessageHandler)
    }

    @Bean
    @ConditionalOnMissingBean(TelegramClient::class)
    @ConditionalOnProperty(name = ["telegram.mode"], havingValue = "webhook")
    open fun telegramWebhookClient(
        compositeMessageHandler: CompositeMessageHandler<Message>,
    ): TelegraffWebhookBot {
        // TODO: Reconfigure with one of the following approaches
        /*
        @Bean(name = ["/ruslanys"])
        fun ruslanController(): Controller {
            val aa = RequestMappingHandlerAdapter()
            val bb = RequestMappingHandlerMapping()
            return Controller { request, response ->
                response.writer.print("ok")

                ModelAndView("index")
                // null
            }
        }
        */
        return TelegraffWebhookBot(telegramProperties(), compositeMessageHandler)
    }

    // endregion

    @Bean
    @ConditionalOnMissingBean(FormStorage::class)
    open fun formStorage(forms: List<TelegrambotForm>): FormStorage<Message, TelegrambotFormState> {
        return TelegrambotFormStorage(forms)
    }

    @Bean
    @ConditionalOnMissingBean(FormStateStorage::class)
    open fun formStorageStorage(): FormStateStorage<Message, TelegrambotFormState> {
        return TelegrambotFormStateStorage()
    }

    // region Filters

    @Bean
    @ConditionalOnMissingBean(CompositeMessageHandler::class)
    open fun compositeMessageHandler(
        handlers: List<ConditionalMessageHandler<Message>>,
        finalizer: FinalMessageHandler,
    ): CompositeMessageHandler<Message> {
        return DefaultCompositeMessageHandler(handlers, finalizer)
    }

    @Bean
    @ConditionalOnMissingBean(FormMessageHandler::class)
    open fun handlersFilter(
        formStorage: FormStorage<Message, TelegrambotFormState>,
        formStateStorage: FormStateStorage<Message, TelegrambotFormState>,
        exceptionHandlers: List<AbstractFormExceptionHandler<Message, TelegrambotFormState, out Exception>>,
    ): FormMessageHandler<Message, TelegrambotFormState> {
        return FormMessageHandler(formStorage, formStateStorage, exceptionHandlers)
    }

    @Bean
    @ConditionalOnMissingBean(AbstractTelegrambotCancelMessageHandler::class)
    open fun cancelFilter(
        formStateStorage: FormStateStorage<Message, TelegrambotFormState>,
        telegramBot: TelegramBot,
    ): AbstractTelegrambotCancelMessageHandler {
        return TelegrambotCancelMessageHandler(formStateStorage, telegramBot)
    }

    @Bean
    @ConditionalOnMissingBean(FinalMessageHandler::class)
    @ConditionalOnProperty(name = ["telegram.unresolved-filter.enabled"], matchIfMissing = true)
    open fun finalMessageHandler(
        telegramBotsApi: TelegramBotsApi,
    ): FinalMessageHandler {
        return FinalMessageHandler(telegramBotsApi)
    }

    // endregion

}