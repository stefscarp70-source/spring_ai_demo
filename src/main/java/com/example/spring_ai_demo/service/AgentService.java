package com.example.spring_ai_demo.service;

import com.example.spring_ai_demo.tool.songs.MusicTools;
import com.example.spring_ai_demo.tool.songs.dto.AgentAnswer;
import com.example.spring_ai_demo.tool.songs.dto.RunResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.AdvisorParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AgentService {

    private static final int MAX_STEPS = 5;
    private final MusicTools musicTools;
    private final ChatClient researchChatClient;
    private final ToolCallingManager toolCallingManager;
    private final BeanOutputConverter<AgentAnswer> answerConverter;

    public AgentService(MusicTools musicTools, ChatClient researchChatClient, ToolCallingManager toolCallingManager) {
        this.musicTools = musicTools;
        this.researchChatClient = researchChatClient;
        this.toolCallingManager = toolCallingManager;
        this.answerConverter = new BeanOutputConverter<>(AgentAnswer.class);
    }
    
    public RunResult run(String artist) {
        String context = String.format("""
                Find whether %s has released or is about to release an album and give me its complete tracklist
                
                The result must contain:
                
                - answer: a concise natural-language answer to the user
                - albumTitle: the title of the relevant album, or an empty string
                  if no album was found
                - trackList: a list of string containing one string for each track of the
                  album, or an empty array if no album was found
                """, artist);

        log.info("------------------------------------------");
        log.info("-----  New artist search starting: {}", artist);

        ToolCallback[] tools = ToolCallbacks.from(musicTools);
        ToolCallingChatOptions chatOptions = ToolCallingChatOptions.builder()
                .toolCallbacks(tools)
                .build();
        Prompt prompt = new Prompt(
                List.of(new UserMessage(context)),
                chatOptions
        );

        ChatResponse response = null;

        long total = 0L;
        int step;
        for(step=1; step<=MAX_STEPS; step++) {
            log.info("===== Agent step {} =========", step);

            //1. LLM must decide next action according to prompt
            response = researchChatClient
                    .prompt()
                    .messages(prompt.getInstructions())
                    .options(chatOptions.mutate())
                    .advisors(AdvisorParams.toolCallingAdvisorAutoRegister(false))
                    .call()
                    .chatResponse();

            if (response == null) {
                log.info(" ");
                return RunResult.error("Agent returned no response", step);
            }

            log.info("  Agent response: {}", response);


            // 2. if not tool call -> final answer
            if (!response.hasToolCalls()) {
                log.info(" ");
                total += response.getMetadata().getUsage().getTotalTokens();
                return RunResult.response(response, step, answerConverter, total);
            }
            log.info("  1. No final response, go on with next tool...");

            // 3. run next tool
            ToolExecutionResult result = toolCallingManager.executeToolCalls(
                    prompt, response
            );
            total += response.getMetadata().getUsage().getTotalTokens();
            log.info("  2. Tool executed (total: {})...", total);

            // 4. add ToolResponseMessage
            prompt = new Prompt(
                    result.conversationHistory(),
                    chatOptions
            );
            log.info("  3. Tool, response added, another iteration...");

            // 5. go on with loop


        }

        //Condizione success
        //if ()

        return RunResult.budgetExceeded(MAX_STEPS, total);
    }
}
