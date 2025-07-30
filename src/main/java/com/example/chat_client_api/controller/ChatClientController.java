package com.example.chat_client_api.controller;

import com.example.chat_client_api.entiy.ActorFilms;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class ChatClientController {

    private final ChatClient chatClient;

    public ChatClientController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/ai")
    public String generation(@RequestParam String userInput) {
        return this.chatClient.prompt()
                .user(userInput)
                .call()
                .content();
    }

    @GetMapping("/ai-entity")
    public ActorFilms generateResponse(){
        return this.chatClient.prompt()
                .user("Return top 5 Salman Khan movies.")
                .call()
                .entity(ActorFilms.class);
    }

}
