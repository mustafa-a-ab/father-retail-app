# 🛒 Father Retail Store

A modern retail grocery ordering web application built with **Spring Boot** and integrated with **xAI Grok API** for autonomous, conversational order placement using tool/function calling.

Customers can either place grocery orders manually via a traditional web form or simply chat with the AI assistant, which automatically collects order details, saves orders to an SQLite database, and triggers email notifications to store administrators.

---

## ✨ Features

- **🤖 AI Conversational Ordering (Grok AI)**:
  - Natural conversation interface powered by xAI Grok (`grok-2` / `grok-4.20`).
  - Native **Function Calling / Tool Use** (`place_order`): Grok automatically extracts and validates:
    - Customer Name
    - Phone Number
    - Items Ordered (Rice, Flour, Meat, Tuna, Chicken, Oil, Sugar, etc.)
    - Quantity & Units
    - Delivery Address
  - Real-time in-chat order confirmation with dynamic receipt cards.
  - Quick action suggestion chips for one-click prompts.
- **📝 Traditional Order Form**:
  - Clean, responsive form with instantaneous tab switching between AI Chat and manual entry.
- **💾 SQLite & JPA Storage**:
  - Lightweight embedded SQLite database (`orders.db`) with automatic schema management via Hibernate/JPA.
- **📧 Email Notifications**:
  - Automated email dispatch using Spring Boot Mail to notify store admins whenever a new order is logged.

---

## 🛠️ Tech Stack

- **Backend**: Java 21+, Spring Boot, Spring Data JPA, Hibernate, SQLite JDBC
- **AI Engine**: xAI API (Grok models) via OpenAI-compatible endpoints with tool calling
- **Frontend**: Thymeleaf, HTML5, Vanilla CSS3, JavaScript (Fetch API)
- **Database**: SQLite (`orders.db`)
- **Build Tool**: Apache Maven (`mvnw`)

---

## 📁 Project Structure

```
father-retail-app/
├── src/
│   ├── main/
│   │   ├── java/com/example/father_retail_app/
│   │   │   ├── controller/
│   │   │   │   ├── AiChatController.java     # REST API for Grok AI chat (/api/chat)
│   │   │   │   └── OrderController.java     # MVC controller for web form (/orders/form)
│   │   │   ├── dto/chat/                    # DTOs for chat messages & tool calls
│   │   │   │   ├── ChatMessage.java
│   │   │   │   ├── ChatRequest.java
│   │   │   │   ├── ChatResponse.java
│   │   │   │   ├── FunctionCall.java
│   │   │   │   └── ToolCall.java
│   │   │   ├── entity/
│   │   │   │   └── Order.java               # JPA Entity for retail orders
│   │   │   ├── repository/
│   │   │   │   └── OrderRepository.java     # Spring Data JPA repository
│   │   │   └── service/
│   │   │       ├── EmailService.java        # Order email notification service
│   │   │       ├── GrokService.java         # xAI Grok integration & tool calling logic
│   │   │       └── OrderService.java        # Order processing & persistence
│   │   └── resources/
│   │       ├── application.properties       # App & API configurations (gitignored)
│   │       ├── static/css/style.css         # Modern styling & design system
│   │       └── templates/
│   │           ├── order-form.html          # Main UI: Grok Chat + Manual Form tabs
│   │           └── success.html             # Order confirmation page
│   └── test/                                # JUnit & Spring Boot test suite
├── orders.db                                # SQLite database
└── pom.xml
```

---

## 🚀 Getting Started

### 1. Prerequisites

- **Java 21 or higher** (tested with Java 21 / 23)
- **Git**
- An **xAI API Key** ([Get your key here](https://console.x.ai/))

### 2. Clone the Repository

```bash
git clone https://github.com/mustafa-a-ab/Retail_Store.git
cd Retail_Store/father-retail-app
```

### 3. Configure `application.properties`

Create or edit `father-retail-app/src/main/resources/application.properties`:

```properties
spring.application.name=father-retail-app
spring.jpa.hibernate.ddl-auto=update
spring.sql.init.mode=always

# Database
spring.datasource.url=jdbc:sqlite:orders.db
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect

# Email Notifications (Optional / Gmail SMTP)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Grok (xAI) Configuration
grok.api.key=${GROK_API_KEY:your-xai-api-key-here}
grok.api.url=https://api.x.ai/v1/chat/completions
grok.model=grok-2-latest
```

> **Note**: You can also set your API key as an environment variable without modifying the file:
> ```bash
> export GROK_API_KEY="xai-your-api-key"
> ```

### 4. Run the Application

Using the Maven wrapper:

```bash
./mvnw spring-boot:run
```

### 5. Access the Web Application

Open your browser and navigate to:
```
http://localhost:8080/orders/form
```

---

## 💬 How Conversational Ordering Works

1. **User opens chat**: Select the **"Chat with Grok"** tab.
2. **User sends request**: e.g., *"I need 5kg of Rice and 2 bottles of Oil"*.
3. **Grok converses**: Grok checks for missing delivery information and politely asks for customer name, phone number, and address.
4. **Tool Execution**: Once all details are provided, Grok invokes `place_order`.
5. **Order Saved**: The backend saves the order into SQLite, dispatches an admin email notification, and sends confirmation back to Grok.
6. **Receipt Card**: Grok provides a friendly confirmation message and renders an order receipt card directly in the chat!

---

## 🧪 Testing

Run all unit and integration tests:

```bash
./mvnw test
```
