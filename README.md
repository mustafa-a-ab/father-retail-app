# 🛒 Father Retail Store

A modern, full-stack retail grocery ordering platform featuring an autonomous **xAI Grok Conversational Agent** with dynamic tool/function calling, a full **Model Context Protocol (MCP)** server, a **React 19 + Material UI** frontend, and a **Spring Boot 3 / Java 21** REST API backend.

---

## 🏗️ Architecture Overview

The system is decoupled into two independent applications containerized and orchestrated via Docker Compose:

```
┌─────────────────────────────────────────────────────────────┐
│                       Browser / Client                      │
└──────────────┬───────────────────────────────┬──────────────┘
               │ Port 80                       │ Port 8080 (REST / MCP)
               ▼                               ▼
┌───────────────────────────────┐ ┌───────────────────────────┐
│     frontend (Nginx + SPA)    │ │   backend (Spring Boot)   │
│ - React 19 + Material UI (v6) │ │ - Java 21 + Spring Boot 3 │
│ - AI Chat Interface           │ │ - xAI Grok Integration    │
│ - Manual Order Form           │ │ - MCP Server (SSE & JSON) │
│ - Nginx Reverse Proxy         │ │ - SQLite + JPA Storage    │
└──────────────┬────────────────┘ └─────────────┬─────────────┘
               │ Proxy: /api/*, /mcp/*          │
               └────────────────────────────────┘
```

---

## ✨ Features

- **🤖 AI Conversational Ordering (xAI Grok)**:
  - Natural conversation interface powered by xAI Grok (`grok-4.20`).
  - Native **Function Calling / Tool Use** (`place_order`): Automatically extracts and validates:
    - Customer Name & Phone Number
    - Ordered Items & Units (Rice, Flour, Meat, Oil, Sugar, etc.)
    - Delivery Address
  - Real-time in-chat order confirmation with interactive receipt cards.
  - Suggestion chips for quick one-click prompts.
- **🌐 Model Context Protocol (MCP) Server**:
  - Full **MCP Server** implementation over Server-Sent Events (SSE) (`/mcp/sse`) and JSON-RPC 2.0 (`/mcp/messages`).
  - Standard MCP Tools exposed to external AI clients (Claude Desktop, Cursor, Antigravity IDE):
    - `place_order`: Save new retail orders and dispatch email notifications.
    - `get_order`: Retrieve order details and status by ID.
    - `list_recent_orders`: Query the store's latest placed orders.
    - `get_store_inventory`: Inspect catalog, available units, and in-stock items.
  - Standard MCP Resources: `orders://recent` and `catalog://inventory`.
- **📝 Manual Order Form**:
  - Clean, responsive Material UI form with instant tab-switching between AI Chat and manual entry.
- **💾 Persistent SQLite Database**:
  - Embedded SQLite database (`orders.db`) with automatic schema management via Hibernate/JPA.
  - Docker volume persistence (`sqlite_data`).
- **📧 Automated Email Alerts**:
  - Automatic email notifications dispatched to store administrators when new orders are placed.
- **🐳 Full Containerization & Docker Orchestration**:
  - Production-ready multi-stage Dockerfiles for both frontend (Node 20 -> Nginx Alpine) and backend (Maven 3.9 -> Eclipse Temurin JRE 21).

---

## 🛠️ Tech Stack

- **Frontend**: React 19, TypeScript, Vite, Material UI (MUI v6), Emotion
- **Backend**: Java 21, Spring Boot 3, Spring Data JPA, Hibernate, SQLite JDBC
- **AI Engine**: xAI Grok API (`grok-4.20-0309-non-reasoning`)
- **Protocol**: Model Context Protocol (MCP) Server (SSE & JSON-RPC 2.0)
- **Deployment & Containers**: Docker, Docker Compose, Nginx Reverse Proxy
- **Database**: SQLite

---

## 📁 Project Structure

```
Retail_Store/
├── frontend/                                # Standalone React SPA
│   ├── src/
│   │   ├── components/
│   │   │   ├── atoms/                       # Badges, status indicators, tab buttons
│   │   │   ├── molecules/                   # Chat bubbles, input bars, receipt cards
│   │   │   ├── organisms/                   # ChatSection, ManualOrderForm, StoreHeader
│   │   │   └── templates/                   # StoreLayout wrapper
│   │   ├── pages/                           # OrderPage orchestrator
│   │   ├── services/                        # api.ts (backend fetch client)
│   │   ├── theme/                           # MUI theme configuration
│   │   ├── types/                           # TypeScript types & interfaces
│   │   ├── vite-env.d.ts                    # Vite environment types
│   │   ├── App.tsx
│   │   └── main.tsx
│   ├── nginx.conf                           # Nginx production configuration & reverse proxy
│   ├── Dockerfile                           # Frontend multi-stage Dockerfile
│   ├── .dockerignore
│   ├── package.json
│   ├── tsconfig.json
│   └── vite.config.ts
│
├── father-retail-app/                       # Standalone Spring Boot REST Backend
│   ├── src/main/java/com/example/father_retail_app/
│   │   ├── controller/
│   │   │   ├── AiChatController.java        # POST /api/chat (Grok AI chat)
│   │   │   └── OrderApiController.java      # POST /api/orders (Order submission)
│   │   ├── dto/                             # Chat and Tool DTOs
│   │   ├── entity/Order.java                # JPA Entity for orders
│   │   ├── mcp/                             # Model Context Protocol subsystem
│   │   │   ├── dto/                         # JSON-RPC 2.0 models
│   │   │   ├── model/                       # McpToolDefinition
│   │   │   ├── registry/                    # McpToolRegistry
│   │   │   └── server/                      # McpServerController (SSE & HTTP)
│   │   ├── repository/                      # OrderRepository JPA interface
│   │   └── service/                         # GrokService, OrderService, EmailService
│   ├── src/main/resources/
│   │   ├── application.properties           # App configuration & env placeholders
│   │   ├── application.properties.example   # Example config template
│   │   └── schema.sql                       # Database initialization
│   ├── Dockerfile                           # Backend multi-stage Dockerfile
│   ├── .dockerignore
│   └── pom.xml
│
├── docker-compose.yml                       # Multi-container orchestration
├── .gitignore
└── README.md
```

---

## 🐳 Running with Docker (Recommended)

Both applications and the database volume are managed through **Docker Compose**.

### 1. Prerequisites
- Install [Docker Desktop](https://www.docker.com/products/docker-desktop/) (Windows / macOS / Linux) and make sure the Docker daemon is running.

### 2. Configure Environment (Optional)
You can create a `.env` file at the root of the repository to pass your API keys into Docker:

```env
GROK_API_KEY=xai-your-api-key-here
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-gmail-app-password
```

### 3. Start All Services
From the root directory:

```bash
# Build images and start all containers in the background
docker compose up --build -d
```

### 4. Access the Applications
| Service | URL | Description |
| :--- | :--- | :--- |
| **Frontend Web App** | [http://localhost](http://localhost) (Port 80) | React 19 UI with AI Chat and Form |
| **Backend REST API** | [http://localhost:8080](http://localhost:8080) | Spring Boot 3 JSON Endpoints |
| **MCP SSE Stream** | [http://localhost:8080/mcp/sse](http://localhost:8080/mcp/sse) | Model Context Protocol Stream |

### 5. Useful Docker Commands
```bash
# View live logs for both frontend and backend
docker compose logs -f

# View live logs for backend only
docker compose logs -f backend

# View live logs for frontend only
docker compose logs -f frontend

# Check container health and status
docker compose ps

# Rebuild and restart after making code changes
docker compose up --build -d

# Stop and remove all containers (data is safely preserved in sqlite_data volume)
docker compose down

# Stop containers without removing them
docker compose stop

# Restart stopped containers
docker compose start
```

---

## 💻 Local Development (Without Docker)

If you prefer developing locally without Docker containers:

### 1. Backend Setup (Spring Boot)
```bash
cd father-retail-app
./mvnw spring-boot:run
```
*(Runs on `http://localhost:8080`)*

### 2. Frontend Setup (React / Vite)
```bash
cd frontend
npm install
npm run dev
```
*(Runs on `http://localhost:5173` and automatically proxies `/api/*` and `/mcp/*` to `http://localhost:8080`)*

---

## ⚙️ Environment Variables & Configuration

Set these environment variables in your `.env` file or hosting environment:

| Variable | Description | Default / Example |
| :--- | :--- | :--- |
| `GROK_API_KEY` | xAI Grok API key | `xai-...` |
| `SPRING_MAIL_USERNAME` | SMTP Email username for admin alerts | `your-email@gmail.com` |
| `SPRING_MAIL_PASSWORD` | SMTP App password for Gmail | `your-app-password` |
| `SPRING_DATASOURCE_URL` | SQLite JDBC URL | `jdbc:sqlite:/app/data/orders.db` |
| `VITE_API_URL` | Base URL for backend API (Frontend) | Empty (uses Nginx reverse proxy) |

---

## 🔌 Connecting MCP Clients (Claude Desktop / Cursor)

Father Retail Store exposes an MCP server on `http://localhost:8080/mcp/sse`.

Add this configuration to your `claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "father-retail-store": {
      "url": "http://localhost:8080/mcp/sse"
    }
  }
}
```

### Available MCP Tools
- `place_order`: Places an order, saves to SQLite, and triggers admin email.
- `get_order`: Retrieves order details & status using an order ID.
- `list_recent_orders`: Lists the store's latest grocery orders.
- `get_store_inventory`: Lists products, packaging sizes, and in-stock items.

---

## 🧪 Testing & Verification

Run backend unit & integration tests:
```bash
cd father-retail-app
./mvnw test
```

Run frontend type check & production build:
```bash
cd frontend
npm run build
```
