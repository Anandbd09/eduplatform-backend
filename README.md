# EduPlatform Backend

Backend API, services, and infrastructure for the EduPlatform learning platform.

## Overview

This repository contains the backend services and local infrastructure used by EduPlatform. The stack is split into multiple services:

- `gateway`: Spring Cloud Gateway for routing and JWT validation
- `core-service`: Spring Boot service for auth, users, courses, enrollments, payments, media, and notifications
- `ai-service`: FastAPI service for AI workflows, embeddings, and vector search
- `infra`: Local infrastructure bootstrap for MongoDB, RabbitMQ, Consul, and related dependencies

## Repository Structure

```text
.
|-- docker-compose.yml
|-- infra/
|   |-- consul/
|   |-- mongodb/
|   `-- rabbitmq/
`-- services/
    |-- ai-service/
    |-- core-service/
    `-- gateway/
```

## Tech Stack

- Java 17
- Spring Boot 3
- Spring Cloud Gateway
- Python 3.11+
- FastAPI
- MongoDB
- Redis
- RabbitMQ
- Consul
- ChromaDB
- Docker Compose

## Prerequisites

- Git
- Docker Desktop
- Java 17
- Maven or the included Maven wrapper
- Python 3.11+

## Environment Setup

1. Copy `.env.example` to `.env`
2. Update secrets and API keys as needed

Example:

```powershell
Copy-Item .env.example .env
```

Important:

- `.env` is ignored by git and should not be committed
- Replace default secrets before deploying anywhere beyond local development

## Run With Docker

Start the local infrastructure:

```powershell
docker compose up -d
```

This starts:

- MongoDB
- Redis
- RabbitMQ
- Consul
- ChromaDB
- Mongo Express
- Redis Commander

To stop everything:

```powershell
docker compose down
```

## Run Services Locally

Open separate terminals for each service.

### Gateway

```powershell
cd services\gateway
.\mvnw.cmd spring-boot:run
```

Default port: `8080`

### Core Service

```powershell
cd services\core-service
.\mvnw.cmd spring-boot:run
```

Default port: `8081`

### AI Service

```powershell
cd services\ai-service
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
python main.py
```

Default port: `8000`

## Default Local Ports

- Gateway: `8080`
- Core Service: `8081`
- AI Service: `8000`
- MongoDB: `27018`
- Redis: `6379`
- RabbitMQ AMQP: `5672`
- RabbitMQ UI: `15672`
- Consul UI: `8500`
- ChromaDB: `8003`
- Redis Commander: `8082`
- Mongo Express: `8083`

## Local URLs

- API Gateway: `http://localhost:8080`
- Core Service: `http://localhost:8081`
- AI Service: `http://localhost:8000`
- Consul UI: `http://localhost:8500`
- RabbitMQ UI: `http://localhost:15672`
- Redis Commander: `http://localhost:8082`
- Mongo Express: `http://localhost:8083`

## Notes

- The gateway configuration references a `chat-service`, but that service is not currently present in this repository.
- This README is a starter version. It can be expanded later with API docs, architecture diagrams, deployment steps, and service-specific troubleshooting.

## First Push To GitHub

If this is your first time pushing this project:

```powershell
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/<your-username>/eduplatform-backend.git
git push -u origin main
```
