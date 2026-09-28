package com.adwitiya.mcpClient.controller;

import com.adwitiya.mcpClient.advisors.GroqToolCallingAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api")
public class MCPClientController {

    private final ChatClient chatClient;
    private final SyncMcpToolCallbackProvider mcpToolCallbackProvider;

    public MCPClientController(
            ChatClient.Builder chatClientBuilder,
            ToolCallbackProvider toolCallbackProvider,
            ToolCallingManager toolCallingManager,
            SyncMcpToolCallbackProvider mcpToolCallbackProvider) {

        this.mcpToolCallbackProvider = mcpToolCallbackProvider;

        GroqToolCallingAdvisor groqToolCallingAdvisor =
                new GroqToolCallingAdvisor(toolCallingManager);

        this.chatClient = chatClientBuilder
                .defaultTools(toolCallbackProvider)
                .defaultAdvisors(
                        groqToolCallingAdvisor,
                        new SimpleLoggerAdvisor()
                )
                .build();
    }

    @GetMapping("/chat")
    public String chat(
            @RequestHeader(value = "username", required = false) String username,
            @RequestParam("message") String message) {

        return chatClient
                .prompt()
                .user(message + " My username is " + username)
                .call()
                .content();
    }

    @GetMapping("/tools")
    public List<String> tools() {

        return Arrays.stream(
                        mcpToolCallbackProvider.getToolCallbacks()
                )
                .map(tool -> tool.getToolDefinition().name())
                .toList();
    }
}