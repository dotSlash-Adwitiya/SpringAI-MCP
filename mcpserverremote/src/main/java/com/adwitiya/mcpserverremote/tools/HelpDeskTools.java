package com.adwitiya.mcpserverremote.tools;

import com.adwitiya.mcpserverremote.entity.HelpDeskTicket;
import com.adwitiya.mcpserverremote.model.TicketRequest;
import com.adwitiya.mcpserverremote.service.HelpDeskTicketService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HelpDeskTools {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelpDeskTools.class);

    private final HelpDeskTicketService service;

    @McpTool(name = "createTicket", description = "Create a new helpdesk ticket.\n" +
            "        Before calling this tool, check existing tickets and ensure\n" +
            "        there is no existing unresolved ticket for the same issue.")
    String createTicket(@McpToolParam(description = "Details to create a Support ticket")
                        TicketRequest ticketRequest, ToolContext toolContext) {
        String username = (String) toolContext.getContext().get("username");
        LOGGER.info("Creating support ticket for user: {} with details: {}", username, ticketRequest);
        HelpDeskTicket savedTicket = service.createTicket(ticketRequest,username);
        LOGGER.info("Ticket created successfully. Ticket ID: {}, Username: {}", savedTicket.getId(), savedTicket.getUsername());
        return "Ticket #" + savedTicket.getId() + " created successfully for user " + savedTicket.getUsername();
    }

    @McpTool(description = "Fetch existing helpdesk tickets for the current user.\n" +
            "    Use this tool BEFORE creating a ticket when there may already\n" +
            "    be an existing ticket for the same issue.")
    List<HelpDeskTicket> getTicketStatus(ToolContext toolContext) {
//        String username = (String) toolContext.getContext().get("username");
        LOGGER.info("ToolContext = {}", toolContext);
        LOGGER.info("ToolContext map = {}", toolContext.getContext());

        String username = (String) toolContext.getContext().get("username");
        return service.getTicketsByUsername(username);
    }

}