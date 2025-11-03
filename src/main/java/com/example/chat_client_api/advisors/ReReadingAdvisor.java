package com.example.chat_client_api.advisors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.Map;

public class ReReadingAdvisor implements BaseAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(ReReadingAdvisor.class);

    private static final String DEFAULT_RE2_ADVISE_TEMPLATE = """
            {re2_input_query}
            Read the question again: {re2_input_query}
            """;

    private final String re2AdviseTemplate;
    private int order = 0;

    public ReReadingAdvisor(){
        this(DEFAULT_RE2_ADVISE_TEMPLATE);
    }

    public ReReadingAdvisor(String re2AdvisorTemplate){
        this.re2AdviseTemplate = re2AdvisorTemplate;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        logRequest(chatClientRequest);
        String augmentedUserText = PromptTemplate.builder()
                .template(re2AdviseTemplate)
                .variables(Map.of("re2_input_query",chatClientRequest.prompt().getUserMessage().getText()))
                .build()
                .render();
        return chatClientRequest.mutate()
                .prompt(chatClientRequest.prompt().augmentUserMessage(augmentedUserText))
                .build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        logResponse(chatClientResponse);
        return chatClientResponse;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    public ReReadingAdvisor withOrder(int order){
        this.order = order;
        return this;
    }

    private void logRequest(ChatClientRequest chatClientRequest){
        logger.info("request {}",chatClientRequest);
    }

    private void logResponse(ChatClientResponse chatClientResponse){
        logger.info("response {}",chatClientResponse);
    }
}
