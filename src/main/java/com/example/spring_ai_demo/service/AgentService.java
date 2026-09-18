package com.example.spring_ai_demo.service;

import com.example.spring_ai_demo.tool.songs.MusicTools;
import com.example.spring_ai_demo.tool.songs.dto.AgentAnswer;
import com.example.spring_ai_demo.tool.songs.dto.RecentAlbumResponse;
import com.example.spring_ai_demo.tool.songs.dto.RunResult;
import com.example.spring_ai_demo.tool.songs.dto.StatusSearch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.AdvisorParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class AgentService {

    private static final int MAX_STEPS = 5;
    private final MusicTools musicTools;
    private final ChatClient researchChatClient;
    private final OllamaChatModel ollamaChatModel;
    private final ToolCallingManager toolCallingManager;
    private final BeanOutputConverter<AgentAnswer> answerConverter;
    private final BeanOutputConverter<RecentAlbumResponse> albumConverter;

    public AgentService(MusicTools musicTools, ChatClient researchChatClient, ChatClient ollamaChatClient, OllamaChatModel ollamaChatModel,
                        ToolCallingManager toolCallingManager) {
        this.musicTools = musicTools;
        //this.researchChatClient = researchChatClient; //This for openAI agent
        this.researchChatClient = ollamaChatClient; //This for ollama agent
        this.ollamaChatModel = ollamaChatModel;
        this.toolCallingManager = toolCallingManager;
        this.albumConverter = new BeanOutputConverter<>(RecentAlbumResponse.class);
        this.answerConverter = new BeanOutputConverter<>(AgentAnswer.class);
    }
    
    public RunResult runOllama(String artist) {
        String context = String.format("""
                Find whether %s has released or is about to release an album and give me its complete tracklist
                
                The result must contain:
                
                - answer: a concise natural-language answer to the user
                - albumTitle: the title of the relevant album, or an empty string
                  if no album was found
                - trackList: a list of string containing one string for each track of the
                  album, or an empty array if no album was found
                  
                  Consider to use one of the tools you know to look for it.
                """, artist);

        String context0 = """
            Devi trovare un album recente o in uscita di Madonna.
            Per farlo devi utilizzare il tool findRecentAlbum.
            Non rispondere usando le tue conoscenze: chiama il tool.
            """;
        String context2 = String.format("""
            You need to find the most recent or upcoming album of %s
            and then find its complete tracklist.
        
            You MUST use the available tools to obtain this information.
            Do not answer from your own knowledge.
            
            """, artist);
        String context3 = String.format("""
                 Your task is to find whether %s has released or is about to
                           release an album and, if an album is found, obtain its complete
                           tracklist.
                
                           You have access to tools for completing this task.
                
                           Work iteratively, one turn at a time.
                
                           At each turn, examine the information currently available and
                           determine the single next action required to make progress.
                
                           If a tool is required, invoke exactly ONE available tool in this
                           turn.
                
                           The result of this turn must contain AT MOST ONE tool invocation.
                
                           Do not invoke multiple tools.
                           Do not plan future tool invocations.
                           Do not describe tool invocations in text.
                           Do not write JSON representing a tool invocation in text.
                           Do not assume the result of a tool.
                           Wait for the actual tool result before deciding the next action.
                
                           If no tool is required, provide the final answer.
            """, artist);

        String context4 = String.format("""
                Find whether %s has released or is about to release an album and give me its complete tracklist
                You have access to tools for completing this task.
                Use a tool if one is available that can provide the information and use the tool with its exact JSON schema.
            """, artist);

        String context5 = String.format("""
            User request:
            Find whether %s has released or is about to release an album
            and give me its complete tracklist.
        
            Find information needed to answer the user's request.
        
            You have access to tools for completing this task.
        
            At this point, perform only the next required action.
            Use a tool if one is available that can provide the information needed now.
            Use exactly one tool and follow its exact JSON schema.            
        
            Do not try to complete the entire task in this response.
            Do not plan subsequent actions. 
            """, artist);

        log.info("------------------------------------------");
        log.info("-----  New artist search starting: {}", artist);

        ToolCallback[] tools = ToolCallbacks.from(musicTools);
        ToolCallback[] tools2 = Arrays.stream(ToolCallbacks.from(musicTools))
                .filter(t -> t.getToolDefinition()
                        .name()
                        .equals("findRecentAlbum"))
                .toArray(ToolCallback[]::new);
        //Per Ollama
        OllamaChatOptions chatOptionsOll = OllamaChatOptions.builder()
                .model("llama3.1:8b-instruct-q4_K_M")
                .toolCallbacks(tools)
                .temperature(0.0)
                .build();

        String contextPromp = context5;
        log.debug("    context = {}", contextPromp);
        Prompt prompt = new Prompt(
                List.of(new UserMessage(contextPromp)),
                chatOptionsOll //Ollama
        );

        ChatResponse response = null;

        OllamaChatOptions.Builder chatOptionsOllBuilder = OllamaChatOptions.builder()
                .model("llama3.1:8b-instruct-q4_K_M")
                .toolCallbacks(tools)
                .temperature(0.0);
        /// /////////////////////////

        long total = 0L;
        int step;
        for(step=1; step<=MAX_STEPS; step++) {
            log.info("===== Agent step {} =========", step);

            log.debug("  Agent ChatClient = {}", researchChatClient);
            log.info("  Ollama options model = {}", chatOptionsOll.getModel());
            Arrays.stream(tools).sequential().forEach(tool ->
                    log.debug("  >> REGISTERED TOOL: {}",
                            tool.getToolDefinition().name()));

            //1. LLM must decide next action according to prompt
            //Ollama model
            //response = ollamaChatModel.call(prompt);


            response = researchChatClient
                    .prompt(prompt) //Ollama
                    //.messages(prompt.getInstructions())  //openAI
                    //.options(chatOptions.mutate()) //openAI
                    //.options(chatOptionsOll.mutate()) //Ollama
                    .advisors(AdvisorParams.toolCallingAdvisorAutoRegister(false))
                    .call()
                    .chatResponse();

            if (response == null) {
                log.info(" ");
                return RunResult.error("Agent returned no response", step);
            }
            log.debug("    Tool calls in response output: {}", response.getResult().getOutput().getToolCalls());
            log.debug("    Text content: {}", response.getResult().getOutput().getText());

            log.info("  Agent response: {}", response);

            // 2. if not tool call -> final answer
            if (!response.hasToolCalls()) {
                log.info(" ");
                total += response.getMetadata().getUsage().getTotalTokens();
                log.info("FINAL response: {}", response.getResult().getOutput().getText());
                return RunResult.response(response, step, answerConverter, total);
            }
            log.info("  1. No final response, go on with next tool...");

            // 3. run next tool
            ToolExecutionResult result = toolCallingManager.executeToolCalls(
                    prompt, response
            );
            log.info("  result after tool: ");

            List<Message> history = new ArrayList<>(result.conversationHistory());

            result.conversationHistory().forEach(message ->
                    log.debug("    MESSAGE {}: {}", message.getClass().getSimpleName(), message)
            );
            total += response.getMetadata().getUsage().getTotalTokens();
            log.info("  2. Tool executed (total: {})...", total);

            //Trying final conversion
            boolean okSuccess = false;
            try {
                Message lastMessage = result.conversationHistory().get(result.conversationHistory().size() - 1);
                if (lastMessage instanceof ToolResponseMessage toolResponseMessage) {

                    List<ToolResponseMessage.ToolResponse> responses = toolResponseMessage.getResponses();
                    ToolResponseMessage.ToolResponse lastResponse = responses.get(responses.size() - 1);

                    String responseData = lastResponse.responseData();
                    // deserializza responseData
                    log.debug("    raw from tool: {}", responseData);
                    RecentAlbumResponse agentAnswer = albumConverter.convert(responseData);

                    boolean isError = agentAnswer.status()== StatusSearch.ERROR;
                    if (isError) {
                        return RunResult.error(agentAnswer.errorMessage(), step);
                    }

                    okSuccess = !agentAnswer.album_details().track_list().isEmpty();
                    if (okSuccess) {
                        log.warn("SUCCESS: response found: {}", agentAnswer.album()!=null?agentAnswer.album().album_title():"null title");
                        return RunResult.responseFromTool(agentAnswer, step, total);
                    }
                }
            } catch (Exception e) {
                log.warn("  ...no possible final answer: {}", e.getMessage());
            }

            history.add(new UserMessage("""
                Any tool result with status EMPTY must terminate the search and answer is: No upcoming album.
                
                Re-evaluate the original user request using the tool result above.
                If the original request has been completely satisfied, provide the final answer now.
                Otherwise, call exactly ONE appropriate tool using its exact parameter schema.

                Do not describe the tool call as text.
                """)
            );

            // 4. add ToolResponseMessage
            prompt = new Prompt(history, chatOptionsOll);
            log.info("  3. Tool, response added, another iteration...");

            // 5. go on with loop

        }


        return RunResult.budgetExceeded(MAX_STEPS, total);
    }

    public RunResult runOpenAI(String artist) {
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

        log.debug("    context = {}", context);
        Prompt prompt = new Prompt(
                List.of(new UserMessage(context)),
                chatOptions //openAI
        );

        ChatResponse response = null;

        long total = 0L;
        int step;
        for(step=1; step<=MAX_STEPS; step++) {
            log.info("===== Agent step {} =========", step);


            response = researchChatClient
                    .prompt()
                    .messages(prompt.getInstructions())  //openAI
                    .options(chatOptions.mutate()) //openAI
                    .advisors(AdvisorParams.toolCallingAdvisorAutoRegister(false))
                    .call()
                    .chatResponse();

            if (response == null) {
                log.info(" ");
                return RunResult.error("Agent returned no response", step);
            }
            log.debug("    Tool calls in response output: {}", response.getResult().getOutput().getToolCalls());
            log.debug("    Text content: {}", response.getResult().getOutput().getText());

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


        return RunResult.budgetExceeded(MAX_STEPS, total);
    }

    public void testOllamaToolCalling() {
        ToolCallback[] tools = ToolCallbacks.from(musicTools);
        OllamaChatOptions options = OllamaChatOptions.builder()
                .model("llama3.1:8b-instruct-q4_K_M")
                .toolCallbacks(tools)
                .temperature(0.0)
                .build();

        Prompt prompt = new Prompt(
                """
                        Devi trovare un album recente o in uscita di Madonna.
                                    Per farlo devi utilizzare il tool findRecentAlbum.
                                    Non rispondere usando le tue conoscenze: chiama il tool.
                        """,
                options
        );

        ChatResponse response = ollamaChatModel.call(prompt);

        log.info("RAW RESPONSE: {}", response);
        log.info("TOOL CALLS: {}", response.getResult()
                .getOutput()
                .getToolCalls());
        log.info("TEXT: {}", response.getResult()
                .getOutput()
                .getText());
    }

    public void testOllamaToolCalling2() {
        ToolCallback[] tools = ToolCallbacks.from(musicTools);
        OllamaChatOptions options = OllamaChatOptions.builder()
                .model("llama3.1:8b-instruct-q4_K_M")
                .toolCallbacks(tools)
                .temperature(0.0)
                .build();

        Prompt prompt = new Prompt(
                """
                        Devi trovare un album recente o in uscita di Madonna.
                                    Per farlo devi utilizzare il tool findRecentAlbum.
                                    Non rispondere usando le tue conoscenze: chiama il tool.
                        """,
                options
        );

        ChatResponse response = researchChatClient
                .prompt(prompt)
                .call().chatResponse();


        log.info("RAW RESPONSE: {}", response);
        log.info("TOOL CALLS: {}", response.getResult()
                .getOutput()
                .getToolCalls());
        log.info("TEXT: {}", response.getResult()
                .getOutput()
                .getText());
    }
}
