package com.example.chat_client_api.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    private final OllamaChatModel ollamaChatModel;

    public ChatClientConfig(OllamaChatModel ollamaChatModel) {
        this.ollamaChatModel = ollamaChatModel;
    }

    @Bean
    public ChatClient chatClientBuilder() {
        return ChatClient.builder(ollamaChatModel)
                .defaultSystem("You are a friendly chat bot that answers question in the voice of a {voice}")
                .build();
    }
}
