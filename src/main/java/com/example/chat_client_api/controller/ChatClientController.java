package com.example.chat_client_api.controller;

import com.example.chat_client_api.entity.Movie;
import com.example.chat_client_api.entity.ActorFilms;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
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
        SimpleLoggerAdvisor customAdvisor = new SimpleLoggerAdvisor(
                request -> "Custom request: " + request.prompt().getUserMessage(),
                response -> "Custom response: " + response.getResult(),
                0
        );
        return this.chatClient.prompt()
                .advisors(customAdvisor)
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

    /*
    * returns the ChatResponse object that contains multiple generations and also metadata about the response,
    * for example how many token were used to create the response.
    *
    * Refer docs/ChatClientAPI.md/1&2 for more details
    * */
    @GetMapping("/call-return-chatresponse")
    public ChatResponse callReturnChatResponse() {

        return this.chatClient.prompt()
                .user(u -> u
                        .text("Tell me the names of 5 movies whose soundtrack was composed by <composer>")
                        .param("composer", "John Williams"))
                .templateRenderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
                .call()
                .chatResponse();
    }

    /*
    * returns a ChatClientResponse object that contains the ChatResponse object and the ChatClient execution context,
    * giving you access to additional data used during the execution of advisors (e.g. the relevant documents retrieved
    * in a RAG flow).
    * */
    @GetMapping("/call-return-ChatClientResponse")
    public ChatResponse chatClientResponse() {
        return this.chatClient.prompt()
                .user(u -> u
                        .text("Tell me the names of 5 movies whose soundtrack was composed by <composer>")
                        .param("composer", "John Williams"))
                .templateRenderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
                .call()
                .chatResponse();
    }

    /*
    * refer docs/ChatClientAPI.md/3 for more details
    * */
    @GetMapping("/ai/joke")
    Map<String, String> completion(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message,String voice) {
        return Map.of(
                "completion",
                this.chatClient.prompt()
                        .system(sp -> sp.param("voice",voice))
                        .user(message)
                        .call()
                        .content()
        );
    }

}
