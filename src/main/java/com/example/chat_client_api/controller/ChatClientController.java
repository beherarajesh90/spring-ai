package com.example.chat_client_api.controller;

import com.example.chat_client_api.entity.Movie;
import com.example.chat_client_api.entity.ActorFilms;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping("/ai-entity-list")
    public List<ActorFilms> generateListResponse(){
        return this.chatClient.prompt()
                .user("Return top 5 Salman Khan movies and Shahrukh Khan movies.")
                .call()
                .entity(new ParameterizedTypeReference<List<ActorFilms>>() {});
    }

    @GetMapping("/ai-stream")
    public Flux<String> streamGeneration(@RequestParam String userInput) {
        return this.chatClient.prompt()
                .user(userInput)
                .stream()
                .content();
    }

    @GetMapping("/ai-stream-entityList")
    public List<ActorFilms> streamGenerateResponse(){
        var converter = new BeanOutputConverter<>(new ParameterizedTypeReference<List<ActorFilms>>() {});

        Flux<String> flux = this.chatClient.prompt()
                .user(u -> u.text("""
                        Generate the filmography for a random actor.
                        {format}
                      """)
                        .param("format", converter.getFormat()))
                .stream()
                .content();

        String content = flux.collectList().block().stream().collect(Collectors.joining());

        return converter.convert(content);
    }

    @GetMapping("/prompt-templates")
    public List<Movie> promptTemplates(@RequestParam("composer") String composer) {
        List<Movie> movies = this.chatClient.prompt()
                .user(u -> u
                        .text("Tell me the names of 5 movies whose soundtrack was composed by {composer}")
                        .param("composer", composer))
                .call()
                .entity(new ParameterizedTypeReference<List<Movie>>() {
                });
        return movies;
    }

    @GetMapping("/custom-prompt-templates")
    public String customPromptTemplates() {

        return this.chatClient.prompt()
                .user(u -> u
                        .text("Tell me the names of 5 movies whose soundtrack was composed by <composer>")
                        .param("composer", "John Williams"))
                .templateRenderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
                .call()
                .content();
    }
}
