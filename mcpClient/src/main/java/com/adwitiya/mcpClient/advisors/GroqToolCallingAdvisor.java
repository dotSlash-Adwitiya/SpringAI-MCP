package com.adwitiya.mcpClient.advisors;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.ToolCallingAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroqToolCallingAdvisor extends ToolCallingAdvisor {

    public GroqToolCallingAdvisor(ToolCallingManager toolCallingManager) {
        super(
                toolCallingManager,
                DEFAULT_TOOL_EXECUTION_ELIGIBILITY_CHECKER,
                DEFAULT_ORDER,
                true
        );
    }

    @Override
    protected List<Message> doGetNextInstructionsForToolCall(
            ChatClientRequest chatClientRequest,
            ChatClientResponse chatClientResponse,
            ToolExecutionResult toolExecutionResult) {

        List<Message> originalMessages =
                super.doGetNextInstructionsForToolCall(
                        chatClientRequest,
                        chatClientResponse,
                        toolExecutionResult
                );

        List<Message> cleanedMessages = new ArrayList<>();

        for (Message message : originalMessages) {

            if (message instanceof AssistantMessage assistantMessage) {

                Map<String, Object> properties =
                        new HashMap<>(assistantMessage.getMetadata());

                /*
                 * Spring AI internally uses camelCase:
                 *
                 * reasoningContent
                 *
                 * Groq/OpenAI-compatible serialization turns this
                 * into:
                 *
                 * reasoning_content
                 *
                 * Groq rejects this field in the follow-up request.
                 */
                properties.remove("reasoningContent");

                AssistantMessage cleanedMessage =
                        AssistantMessage.builder()
                                .content(assistantMessage.getText())
                                .properties(properties)
                                .toolCalls(assistantMessage.getToolCalls())
                                .media(assistantMessage.getMedia())
                                .build();

                cleanedMessages.add(cleanedMessage);
            }
            else {
                cleanedMessages.add(message);
            }
        }

        return cleanedMessages;
    }
}