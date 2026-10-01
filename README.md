# SpringAI-MCP 🚀

> A Spring Boot **Model Context Protocol (MCP)** playground built with **Spring AI** — a full MCP client plus two MCP servers (remote & stdio) that work together to expose tools to an AI model over HTTP.

This project demonstrates the complete MCP stack using **Spring AI 2.0.1** and **Spring Boot 4.1.1** on **Java 25**:

- **`mcpClient`** — the MCP client. It connects to one or more MCP servers (configured via `mcp-servers.json`), registers their exposed tools with an OpenAI-compatible chat model, and lets you talk to the model over a REST endpoint. It also ships with an optional `MCPFilter` (tool filter) to control which tools get registered per server.
- **`mcpserverremote`** — a **remote** MCP server that exposes helpdesk tools over the network using the **Streamable HTTP** transport (`spring-ai-starter-mcp-server-webmvc`, port `8090`).
- **`mcpserverstdio`** — a **stdio** MCP server that exposes the same helpdesk tools for local, client-spawned connections (`spring-ai-starter-mcp-server`, no web server).

![Java](https://img.shields.io/badge/Java-25-007396?style=flat-square&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Spring AI](https://img.shields.io/badge/Spring%20AI-2.0.1-6DB33F?style=flat-square&logo=spring&logoColor=white)
![License](https://img.shields.io/badge/License-Proprietary-lightgrey?style=flat-square)

---

## 📖 Table of Contents

- [What is MCP?](#-what-is-mcp)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Prerequisites](#-prerequisites)
- [Setup & Configuration](#-setup--configuration)
- [Running the Application](#-running-the-application)
- [Usage](#-usage)
- [How it Works](#-how-it-works)
- [Configuration Reference](#-configuration-reference)
- [Roadmap](#-roadmap)
- [License](#-license)

---

## 🧩 What is MCP?

The **Model Context Protocol (MCP)** is an open standard that provides a universal way for AI applications (clients) to connect to external data sources and tools (servers). Instead of building a custom integration for every data source, an AI client can discover and call any MCP-compatible server's tools dynamically.

In this project:

- **Client** = the `mcpClient` Spring Boot application (Spring AI MCP Client).
- **Servers** = MCP servers configured via `mcp-servers.json` (e.g., the local `mcpserverstdio` helpdesk server, filesystem, GitHub, etc.), as well as remote servers like `mcpserverremote` that can be reached over Streamable HTTP/SSE.
- **Model** = an OpenAI-compatible LLM (e.g., OpenAI, Groq, or any compatible provider) that calls the tools exposed by the MCP servers.

---

## ✨ Features

### Client (`mcpClient`)

- ✅ **MCP Client** — connects to external MCP servers and auto-discovers their tools
- ✅ **Dynamic Tool Registration** — all tools exposed by configured MCP servers are injected into the chat model via `ToolCallbackProvider`
- ✅ **`MCPFilter` (tool filter)** — optional `McpToolFilter` implementation that whitelists/denies tools per server (e.g., restrict the GitHub server to a curated set of tools) before they reach the model
- ✅ **Tool Inspection** — `GET /api/tools` returns the list of all tools currently registered from the MCP servers
- ✅ **Custom `GroqToolCallingAdvisor`** — sanitizes assistant messages so Groq/OpenAI-compatible models accept multi-turn tool-calling requests (`reasoningContent` → `reasoning_content` handling)
- ✅ **REST API** — simple `GET /api/chat` endpoint with per-request `username` header support
- ✅ **Request Logging** — `SimpleLoggerAdvisor` logs chat requests/responses for easy debugging

### Servers (`mcpserverremote` & `mcpserverstdio`)

- ✅ **Remote MCP Server** (`mcpserverremote`) — serves MCP tools over **Streamable HTTP** on port `8090` using Spring WebMVC
- ✅ **Stdio MCP Server** (`mcpserverstdio`) — runs as a plain stdio process (no web server) so it can be spawned locally by a client
- ✅ **Helpdesk Tools** — `createTicket` and `getTicketStatus` backed by Spring Data JPA + H2 (file-based `~/chatmemory`)
- ✅ **Per-User Context** — tools read the calling user's `username` from the `ToolContext` to create/fetch tickets for the right user

---

## 🛠 Tech Stack

| Layer          | Technology                                             |
| -------------- | ------------------------------------------------------ |
| Language       | Java 25                                                |
| Framework      | Spring Boot 4.1.1 (WebMVC)                             |
| AI Framework   | Spring AI 2.0.1                                        |
| Build Tool     | Maven (with Maven Wrapper `./mvnw`)                    |
| Client (MCP)   | `spring-ai-starter-mcp-client` + `spring-ai-starter-model-openai` |
| Remote Server  | `spring-ai-starter-mcp-server-webmvc` (Streamable HTTP)|
| Stdio Server   | `spring-ai-starter-mcp-server`                         |
| Persistence    | Spring Data JPA + H2 (file-based)                      |
| Dev Tools      | Spring Boot DevTools                                   |

---

## 📁 Project Structure

```
SpringAI-MCP
├── mcpClient/                           # MCP client application
│   ├── src/main/java/com/adwitiya/mcpClient
│   │   ├── McpClientApplication.java          # Spring Boot entry point
│   │   ├── advisor/
│   │   │   └── GroqToolCallingAdvisor.java    # Custom multi-turn tool calling advisor
│   │   ├── controller/
│   │   │   └── MCPClientController.java       # REST controller (/api/chat, /api/tools)
│   │   └── filters/
│   │       └── MCPFilter.java                 # Optional McpToolFilter (per-server tool whitelist)
│   └── src/main/resources
│       ├── application.properties             # Client config (gitignored)
│       └── mcp-servers.json                   # MCP server definitions (gitignored)
│
├── mcpserverremote/                     # Remote (Streamable HTTP) MCP server — port 8090
│   ├── pom.xml                            # spring-ai-starter-mcp-server-webmvc
│   └── src/main
│       ├── java/com/adwitiya/mcpserverremote
│       │   ├── McpserverremoteApplication.java   # Spring Boot entry point
│       │   ├── entity/HelpDeskTicket.java        # JPA entity
│       │   ├── model/TicketRequest.java          # Tool request model
│       │   ├── repository/HelpDeskTicketRepository.java
│       │   ├── service/HelpDeskTicketService.java
│       │   └── tools/HelpDeskTools.java          # createTicket, getTicketStatus
│       └── resources/application.properties      # Streamable HTTP, H2, port 8090
│
├── mcpserverstdio/                      # Stdio MCP server (no web server)
│   ├── pom.xml                            # spring-ai-starter-mcp-server
│   └── src/main
│       ├── java/com/adwitiya/mcpserverstdio
│       │   ├── McpserverstdioApplication.java    # Spring Boot entry point
│       │   ├── entity/HelpDeskTicket.java        # JPA entity
│       │   ├── model/TicketRequest.java          # Tool request model
│       │   ├── repository/HelpDeskTicketRepository.java
│       │   ├── service/HelpDeskTicketService.java
│       │   └── tools/HelpDeskTools.java          # createTicket, getTicketStatus
│       └── resources/application.properties      # stdio, H2, web-application-type=none
│
├── .gitignore
└── README.md
```

> **Note:** `application.properties` and `mcp-servers.json` contain local/secrets configuration and are intentionally excluded from version control via `.gitignore`.

---

## ✅ Prerequisites

- **Java 25** (or a compatible JDK that supports records & modern tooling)
- **Maven 3.9+** (or use the included `./mvnw` wrapper)
- An **OpenAI-compatible API key** (OpenAI, Groq, etc.) — only needed by the `mcpClient`
- One or more **MCP servers** to connect to (the local `mcpserverstdio` server, remote servers, or any external stdio/Streamable HTTP server)

---

## ⚙️ Setup & Configuration

### 1. Configure the AI model (client)

In `mcpClient/src/main/resources/application.properties`:

```properties
# OpenAI-compatible model settings
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.openai.chat.options.model=gpt-4o-mini

# (Optional) When using Groq, point base-url to Groq's OpenAI-compatible endpoint
spring.ai.openai.base-url=https://api.groq.com/openai
spring.ai.openai.chat.options.model=llama-3.3-70b-versatile
```

### 2. Configure MCP servers (client)

In `mcpClient/src/main/resources/mcp-servers.json`. You can point the client at the stdio server from this repository (once built) or any other MCP server:

```json
{
  "mcpServers": {
    "spring-ai-mcp": {
      "command": "/usr/bin/java",
      "args": ["-jar", "/path/to/mcpserverstdio/target/mcpserverstdio-0.0.1-SNAPSHOT.jar"]
    }
  }
}
```

This registers an MCP server whose tools (e.g., the helpdesk tools) will automatically be available to the chat model.

### 3. Configure the servers

Each server keeps its own `application.properties`:

- **`mcpserverremote`** — serves over Streamable HTTP on `server.port=8090`, uses H2 file DB, and sets `spring.ai.mcp.server.protocol=streamable`.
- **`mcpserverstdio`** — runs as a stdio process with `spring.main.web-application-type=none` (no web server), same H2 file DB.

By default both servers persist helpdesk tickets to an H2 file database at `~/chatmemory`.

---

## ▶️ Running the Application

Each module is an independent Spring Boot application with its own Maven Wrapper. Build and run them from their own folders.

**1. Stdio MCP server (`mcpserverstdio`)** — must be packaged so the client can spawn it as a jar:

```bash
cd mcpserverstdio
./mvnw package
java -jar target/mcpserverstdio-0.0.1-SNAPSHOT.jar
```

**2. Remote MCP server (`mcpserverremote`)** — starts on port **`8090`**:

```bash
cd mcpserverremote
./mvnw spring-boot:run
```

**3. MCP client (`mcpClient`)** — starts on port **`8080`**:

```bash
cd mcpClient
./mvnw spring-boot:run
```

> The client spawns/connects to the servers you configured in `mcp-servers.json` during startup.

---

## 🔌 Usage

Send a chat message together with an optional username (used for the helpdesk tools):

```bash
curl --location 'http://localhost:8080/api/chat?message=Create%20a%20ticket%20for%20me' \
  --header 'username: Adwitiya'
```

**Request parameters:**

| Parameter  | Location  | Type   | Required | Description                          |
| ---------- | --------- | ------ | -------- | ------------------------------------ |
| `message`  | Query     | String | ✅ Yes    | The user prompt sent to the model.   |
| `username` | Header    | String | ❌ No     | Injected into the prompt context.    |

**List the tools registered from the MCP servers:**

```bash
curl 'http://localhost:8080/api/tools'
```

**Response:** the model's text reply. If the model decides the MCP server's tools are needed (e.g., the helpdesk `createTicket` / `getTicketStatus` tools exposed by `mcpserverremote` / `mcpserverstdio`), it will call them automatically as part of the conversation.

---

## 🧠 How it Works

1. **Startup** — each application boots its own Spring context. The MCP client starter reads `mcp-servers.json`, connects to the configured servers (stdio/Streamable HTTP), and collects all exposed tools into a `ToolCallbackProvider`.
2. **Tool Filtering** — if enabled, `MCPFilter` (an `McpToolFilter`) runs while tools are being registered, so you can whitelist/deny tools per server (e.g., allow only a curated set of tools from the GitHub server) before they are exposed to the model.
3. **`ChatClient` Assembly** — `MCPClientController` builds a `ChatClient` with:
   - `defaultTools(toolCallbackProvider)` → all (filtered) MCP tools become callable by the model.
   - `GroqToolCallingAdvisor` → ensures follow-up messages are accepted by Groq/OpenAI-compatible APIs.
   - `SimpleLoggerAdvisor` → logs every request/response.
4. **Request Handling** — `GET /api/chat` passes the message (plus optional username) to the model; `GET /api/tools` lists the registered tool names.
5. **Tool Execution** — if the model issues tool calls, `ToolCallingManager` executes them against the MCP servers (e.g., the helpdesk tools persist/fetch tickets via JPA + H2) and feeds results back to the model until the conversation completes.

### `MCPFilter` (why it exists)

MCP servers can expose a large number of tools — not all of which you may want an AI model to call. `MCPFilter` implements Spring AI's `McpToolFilter`, letting you decide, per MCP server, which tools get registered on the client. The sample whitelists a curated set of tools for the `github` server (`get_me`, `get_file_contents`, `search_code`, etc.) and rejects everything else. The class is committed as a commented-out reference — uncomment and adapt it to your needs.

### `GroqToolCallingAdvisor` (why it exists)

Many OpenAI-compatible providers (notably **Groq**) reject multi-turn tool-calling requests when the assistant message metadata contains `reasoningContent`. Spring AI internally stores this in **camelCase**, while Groq's serialization sends it as `reasoning_content`, which it rejects.

This advisor extends `ToolCallingAdvisor` and, before sending the follow-up request, strips the `reasoningContent` metadata from each `AssistantMessage` — enabling smooth multi-turn tool calling on Groq & similar providers.

---

## 📋 Configuration Reference

| Property                                            | Description                                                   |
| --------------------------------------------------- | ------------------------------------------------------------- |
| `spring.ai.openai.api-key` (client)                 | API key for the OpenAI-compatible provider.                   |
| `spring.ai.openai.base-url` (client)                | Base URL (set to Groq etc. for alternate providers).          |
| `spring.ai.openai.chat.options.model` (client)      | Model name (e.g., `gpt-4o-mini`, `llama-3.3-70b-versatile`).  |
| `mcpServers` (client, in `mcp-servers.json`)        | Named MCP server definitions (stdio commands / remote URLs).  |
| `spring.ai.mcp.server.protocol` (remote server)     | MCP transport for `mcpserverremote` (`streamable`).           |
| `server.port` (remote server)                       | HTTP port for `mcpserverremote` (`8090`).                     |
| `spring.main.web-application-type` (stdio server)   | Set to `none` for `mcpserverstdio` (runs as stdio process).   |
| `spring.datasource.*` (servers)                     | H2 file database used by the helpdesk servers (`~/chatmemory`). |

---

## 🗺 Roadmap

- [x] Basic MCP client with REST chat endpoint
- [x] Custom advisor for Groq multi-turn tool calling
- [x] Remote (Streamable HTTP) MCP server (`mcpserverremote`)
- [x] Stdio MCP server (`mcpserverstdio`)
- [x] Optional per-server tool filtering (`MCPFilter`)
- [ ] Add streaming responses (`/api/chat/stream`)
- [ ] Add conversation memory / chat history
- [ ] Dockerize the application

---

## 📄 License

This project is for **personal/educational use**. No license file is currently included — please contact the author for re-use permissions.

---

<p align="center">
  Made with 💚 by <a href="https://github.com/dotSlash-Adwitiya">Adwitiya Mourya</a>
</p>