# SpringAI-MCP 🚀

> A Spring Boot **Model Context Protocol (MCP) Client** built with **Spring AI**, exposing MCP server tools to an AI model through a simple REST endpoint.

This project demonstrates how to build an MCP client using **Spring AI 2.0.1** and **Spring Boot 4.1.1** on **Java 25**. It connects to one or more MCP servers (configured via `mcp-servers.json`), registers their exposed tools with an OpenAI-compatible chat model, and lets you talk to the model over HTTP.

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

- **Client** = this Spring Boot application (Spring AI MCP Client).
- **Servers** = external MCP servers configured in `mcp-servers.json` (e.g., filesystem, database, GitHub, etc.).
- **Model** = an OpenAI-compatible LLM (e.g., OpenAI, Groq, or any compatible provider) that calls the tools exposed by the MCP servers.

---

## ✨ Features

- ✅ **MCP Client** — connects to external MCP servers and auto-discovers their tools
- ✅ **Dynamic Tool Registration** — all tools exposed by configured MCP servers are injected into the chat model via `ToolCallbackProvider`
- ✅ **Custom `GroqToolCallingAdvisor`** — sanitizes assistant messages so Groq/OpenAI-compatible models accept multi-turn tool-calling requests (`reasoningContent` → `reasoning_content` handling)
- ✅ **REST API** — simple `GET /api/chat` endpoint with per-request `username` header support
- ✅ **Request Logging** — `SimpleLoggerAdvisor` logs chat requests/responses for easy debugging
- ✅ **Modern Stack** — Spring Boot 4, Spring AI 2, Java 25, Maven Wrapper included

---

## 🛠 Tech Stack

| Layer        | Technology                                      |
| ------------ | ----------------------------------------------- |
| Language     | Java 25                                         |
| Framework    | Spring Boot 4.1.1 (WebMVC)                      |
| AI Framework | Spring AI 2.0.1                                 |
| Build Tool   | Maven (with Maven Wrapper `./mvnw`)             |
| MCP Support  | `spring-ai-starter-mcp-client`                  |
| Model Starter| `spring-ai-starter-model-openai`                |
| Dev Tools    | Spring Boot DevTools                            |

---

## 📁 Project Structure

```
SpringAI-MCP
├── .mvn/wrapper/                     # Maven Wrapper files
├── src
│   ├── main
│   │   ├── java/com/adwitiya/mcpClient
│   │   │   ├── McpClientApplication.java          # Spring Boot entry point
│   │   │   ├── advisor/
│   │   │   │   └── GroqToolCallingAdvisor.java    # Custom multi-turn tool calling advisor
│   │   │   └── controller/
│   │   │       └── MCPClientController.java       # REST controller (chat endpoint)
│   │   └── resources
│   │       ├── application.properties              # App configuration (gitignored)
│   │       └── mcp-servers.json                    # MCP server definitions (gitignored)
│   └── test
├── .gitattributes
├── .gitignore
├── mvnw / mvnw.cmd                   # Maven wrapper scripts
└── pom.xml                           # Maven build configuration
```

> **Note:** `application.properties` and `mcp-servers.json` contain local/secrets configuration and are intentionally excluded from version control via `.gitignore`.

---

## ✅ Prerequisites

- **Java 25** (or a compatible JDK that supports records & modern tooling)
- **Maven 3.9+** (or use the included `./mvnw` wrapper)
- An **OpenAI-compatible API key** (OpenAI, Groq, etc.)
- One or more **MCP servers** to connect to (local stdio or remote)

---

## ⚙️ Setup & Configuration

### 1. Configure the AI model

Create `src/main/resources/application.properties`:

```properties
# OpenAI-compatible model settings
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.openai.chat.options.model=gpt-4o-mini

# (Optional) When using Groq, point base-url to Groq's OpenAI-compatible endpoint
spring.ai.openai.base-url=https://api.groq.com/openai
spring.ai.openai.chat.options.model=llama-3.3-70b-versatile
```

### 2. Configure MCP servers

Create `src/main/resources/mcp-servers.json`:

```json
{
  "mcpServers": {
    "filesystem": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-filesystem", "/tmp"]
    }
  }
}
```

This registers a **filesystem MCP server** whose tools will automatically be available to the chat model.

---

## ▶️ Running the Application

```bash
# Build & run using the Maven wrapper
./mvnw spring-boot:run

# Or, if Maven is installed globally
mvn spring-boot:run
```

The app starts on the default port **`8080`**.

---

## 🔌 Usage

Send a chat message together with an optional username:

```bash
curl --location 'http://localhost:8080/api/chat?message=List%20the%20files%20in%20my%20directory' \
  --header 'username: Adwitiya'
```

**Request parameters:**

| Parameter | Location  | Type   | Required | Description                          |
| --------- | --------- | ------ | -------- | ------------------------------------ |
| `message` | Query     | String | ✅ Yes    | The user prompt sent to the model.   |
| `username`| Header    | String | ❌ No     | Injected into the prompt context.    |

**Response:** the model's text reply. If the model decides the MCP server's tools are needed to answer, it will call them automatically as part of the conversation.

---

## 🧠 How it Works

1. **Startup** — `McpClientApplication` boots the Spring context. The MCP client starter reads `mcp-servers.json`, connects to each server, and collects all exposed tools into a `ToolCallbackProvider`.
2. **`ChatClient` Assembly** — `MCPClientController` builds a `ChatClient` with:
   - `defaultTools(toolCallbackProvider)` → all MCP tools become callable by the model.
   - `GroqToolCallingAdvisor` → ensures follow-up messages are accepted by Groq/OpenAI-compatible APIs.
   - `SimpleLoggerAdvisor` → logs every request/response.
3. **Request Handling** — `GET /api/chat` passes the message (plus optional username) to the model.
4. **Tool Execution** — If the model issues tool calls, `ToolCallingManager` executes them against the MCP servers and feeds results back to the model until the conversation completes.

### `GroqToolCallingAdvisor` (why it exists)

Many OpenAI-compatible providers (notably **Groq**) reject multi-turn tool-calling requests when the assistant message metadata contains `reasoningContent`. Spring AI internally stores this in **camelCase**, while Groq's serialization sends it as `reasoning_content`, which it rejects.

This advisor extends `ToolCallingAdvisor` and, before sending the follow-up request, strips the `reasoningContent` metadata from each `AssistantMessage` — enabling smooth multi-turn tool calling on Groq & similar providers.

---

## 📋 Configuration Reference

| Property                                          | Description                                            |
| ------------------------------------------------- | ------------------------------------------------------ |
| `spring.ai.openai.api-key`                        | API key for the OpenAI-compatible provider.            |
| `spring.ai.openai.base-url`                       | Base URL (set to Groq etc. for alternate providers).   |
| `spring.ai.openai.chat.options.model`             | Model name (e.g., `gpt-4o-mini`, `llama-3.3-70b-versatile`). |
| `mcpServers` (in `mcp-servers.json`)              | Named MCP server definitions (stdio/SSE commands).     |

---

## 🗺 Roadmap

- [x] Basic MCP client with REST chat endpoint
- [x] Custom advisor for Groq multi-turn tool calling
- [ ] Add support for remote (SSE/streamable HTTP) MCP servers
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