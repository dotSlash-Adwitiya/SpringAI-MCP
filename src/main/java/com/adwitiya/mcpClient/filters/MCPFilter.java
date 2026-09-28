//package com.adwitiya.mcpClient.filters;
//
//import io.modelcontextprotocol.spec.McpSchema;
//import org.springframework.ai.mcp.McpConnectionInfo;
//import org.springframework.ai.mcp.McpToolFilter;
//import org.springframework.context.annotation.Bean;
//import org.springframework.stereotype.Component;
//
//import java.util.Set;
//
//@Component
//public class MCPFilter implements McpToolFilter {
//
//    @Override
//    public boolean test(
//            McpConnectionInfo connectionInfo,
//            McpSchema.Tool tool) {
//
//        System.out.println("MCP TOOL: " + tool.name());
//
//        return true;
//    }
//
//    @Bean
//    public McpToolFilter mcpToolFilter() {
//
//        Set<String> allowedGithubTools = Set.of(
//                "get_me",
//                "get_file_contents",
//                "search_code",
//                "search_repositories",
//                "search_issues",
//                "create_pull_request"
//        );
//
//        return (connectionInfo, tool) -> {
//
//            String serverName = connectionInfo.clientInfo().name();
//            String toolName = tool.name();
//
//            if ("github".equals(serverName)) {
//                return allowedGithubTools.contains(toolName);
//            }
//
//            return false;
//        };
//    }
//}