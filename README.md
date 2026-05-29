# 🛡️ FleetRisk Microservices API

![Java](https://img.shields.io/badge/Java-21-blue?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.x-brightgreen?style=for-the-badge&logo=spring)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Event_Driven-FF6600?style=for-the-badge&logo=rabbitmq)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=for-the-badge&logo=redis)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker)
![JWT](https://img.shields.io/badge/Security-JWT_Auth-black?style=for-the-badge&logo=jsonwebtokens)
![Oracle Cloud](https://img.shields.io/badge/Cloud-Oracle_OCI-F80000?style=for-the-badge&logo=oracle)

Sistema avançado de cotação de seguros para frotas comerciais construído sob uma **Arquitetura de Microsserviços**. O projeto simula o ambiente central de uma seguradora, utilizando mensageria assíncrona para cálculos atuariais, roteamento seguro via API Gateway, cache de dados da Tabela FIPE e geração de propostas em PDF de forma automatizada.

## 🚀 Acesse o projeto em produção

- **Aplicação Front-end:** https://fleetrisk.netlify.app/login

- **Documentação da API (Swagger):** https://fleetrisk-ruanpablo2.duckdns.org/swagger-ui/index.html

## 🔥 Highlights Técnicos (diferenciais do projeto)

- **Ambiente de Demonstração Auto-Gerenciável:** Implementação de um Cron Job nativo no Spring Boot que roda a cada 12 horas, limpando dados de teste de usuários e restaurando automaticamente uma "vitrine" de cotações perfeitas no banco de dados para avaliação de recrutadores, sem consumir APIs externas.

- **Comunicação Real-Time Resiliente:** Uso de WebSockets integrados com **SockJS** para garantir o bypass em regras estritas de proxies reversos (Nginx) e API Gateways, permitindo a atualização do prêmio atuarial na tela do frontend em tempo real, sem falhas de handshake ou CORS.

## ☁️ Arquitetura de Nuvem e DevOps

O FleetRisk foi construído com foco total em **Nuvem (Cloud-Native)**. Isso significa que o código é 100% independente da máquina física, permitindo que o sistema seja implantado em qualquer provedor de nuvem de forma rápida, segura e com alta disponibilidade.
- **Infraestrutura em Nuvem:** Implantado em instância **Ampere A1 (ARM64)** na **Oracle Cloud Infrastructure (OCI)** com IP dinâmico gerenciado via **DuckDNS**.

- **Orquestração de Contêineres:** Todo o ecossistema (Bancos de dados, Cache, Broker e Microsserviços) é provisionado pelo Docker Compose utilizando redes internas (Bridge Network).

- **Database-per-Service:** Máximo isolamento de dados. Cada microsserviço gerencia seu próprio banco de dados PostgreSQL.

- **CI/CD:** Pipeline configurado via GitHub Actions para build dinâmico com Maven e deploy automatizado nos contêineres da Oracle Cloud.

## 📐 Estrutura dos Microsserviços (Monorepo)

O ecossistema é dividido em contextos delimitados (Domain-Driven Design), isolando as responsabilidades de negócio.

```text
📦 fleet-risk-backend
 ┣ 📂 fleet-common               # Biblioteca compartilhada: DTOs, Enums, Global Exceptions
 ┣ 📂 fleet-gateway              # Ponto de entrada (Porta 8080). Validação JWT, Swagger UI e Roteamento
 ┣ 📂 fleet-auth-service         # Gestão de identidade, Corretores, BCrypt e geração de JWT
 ┣ 📂 fleet-quote-service        # Core comercial: Rascunhos, apólices, persistência e Tenant Isolation
 ┣ 📂 fleet-vehicle-service      # Anti-Corruption Layer p/ FIPE, Cache Redis e Sincronização
 ┣ 📂 fleet-pricing-service      # Motor de cálculo atuarial stateless (Coreografia via RabbitMQ)
 ┣ 📂 fleet-document-service     # Engine de PDF (OpenPDF + Thymeleaf) para renderização de propostas
 ┗ 📂 fleet-notification-service # Disparo de e-mails assíncronos (Spring Mail + SMTP) com anexos
 ```

 ## 🛠 Tecnologias e Boas Práticas

- **Ecosistema Spring:** Spring Boot 4, Spring WebMVC, Spring Data JPA, Spring Security, Spring Cloud Gateway MVC.
- **Mensageria:** RabbitMQ com políticas de Retry e Dead Letter Queues (DLQ) para resiliência.
- **Integração Síncrona:** RestClient para chamadas inter-serviços com tratamento de fallbacks.
- **Cache:** Redis para armazenamento ultrarrápido de respostas da API FIPE.
- **Documentação:** Springdoc OpenAPI (Swagger UI) centralizado via agregador no Gateway.
- **Segurança:** Tenant Isolation (Corretores só acessam suas próprias apólices) garantido via injeção segura de cabeçalhos (X-Broker-Name, X-Broker-Cnpj) diretamente pelo Gateway.

---

## 🔗 Documentação completa da API (Endpoints)

Todas as requisições devem ser feitas apontando para o **API Gateway** (`http://localhost:8080`).

A interface interativa do Swagger UI está disponível em `/swagger-ui.html`.

### 👤 Autenticação e Identidade (`/api/v1/auth`)

| Método | Rota               | Descrição                                        | Permissão  |
| ------ | ------------------ | ------------------------------------------------ | ---------- |
| `POST` | `/register`        | Regista um novo corretor no sistema.             | 🌐 Público |
| `POST` | `/login`           | Valida as credenciais e retorna o Token JWT.     | 🌐 Público |

### 🚗 Veículos e Tabela FIPE (`/api/v1/vehicles`)

| Método | Rota                               | Descrição                                                        | Permissão      |
| ------ | ---------------------------------- | ---------------------------------------------------------------- | -------------- |
| `GET`  | `/models/search?query=`            | Busca local de veículos sincronizados.                           | 🔒 Autenticado |
| `GET`  | `/{fipeCode}/years/{yearId}`       | Retorna o valor atualizado da FIPE (com Cache no Redis).         | 🔒 Autenticado |

### 📝 Gestão de Cotações (`/api/v1/quotes`)
*Nota: Rotas protegidas contam com Isolamento de Inquilino (Tenant Isolation) validado via Headers no Gateway.*

| Método  | Rota                         | Descrição                                                                 | Permissão      |
| ------- | ---------------------------- | ------------------------------------------------------------------------- | -------------- |
| `POST`  | `/`                          | Cria a cotação como Rascunho (`PENDING`) e congela os dados da FIPE base. | 🔒 Autenticado |
| `GET`   | `/{id}`                      | Retorna o status, dados da frota e os prêmios calculados da cotação.      | 🔒 Autenticado |
| `PUT`   | `/{id}`                      | Atualiza a frota de um rascunho (Não aciona o motor atuarial).            | 🔒 Autenticado |
| `POST`  | `/{id}/calculate`            | Salva as alterações e envia para o motor de cálculo atuarial (RabbitMQ).  | 🔒 Autenticado |
| `PATCH` | `/{id}/approve`              | Aceita a cotação calculada e dispara a geração do PDF da apólice.         | 🔒 Autenticado |
| `POST`  | `/{id}/resend-document`      | Solicita o reenvio assíncrono do e-mail contendo a proposta comercial.    | 🔒 Autenticado |

### 📄 Documentos (`/api/v1/documents`)

| Método | Rota                           | Descrição                                                              | Permissão      |
| ------ | ------------------------------ | ---------------------------------------------------------------------- | -------------- |
| `GET`  | `/quotes/{id}/download`        | Retorna o arquivo PDF físico da proposta gerada para download (Stream).| 🔒 Autenticado |

---

## 📨 Contratos de Mensageria (Event-Driven)

O sistema utiliza Padrão de Coreografia para operações pesadas e processos de negócio distribuídos:

**Exchange:** `fleet.quote.events`
- `quote.created.key`: Disparado pelo Quote Service, consumido pelo Pricing Service para iniciar os cálculos.

- `quote.calculated.key`: Disparado pelo Pricing Service contendo os prêmios calculados para persistência no Quote.

- `quote.approved.key`: Disparado pelo Quote Service após aprovação comercial, consumido pelo Document Service para renderização de PDF.

**Exchange:** `fleet.document.events`
- `document.generated.key`: Disparado pelo Document Service, consumido pelo Notification Service com o caminho físico do arquivo para envio por e-mail.

---

## 🛠 Tratamento Global de Exceções

O sistema implementa o padrão `@RestControllerAdvice` na biblioteca compartilhada `fleet-common`, padronizando todas as respostas de erro JSON e evitando o vazamento de stack traces para o Front-end.

| Código Interno            | HTTP Status                | Descrição comum                                                          |
| ------------------------- | -------------------------- | ------------------------------------------------------------------------ |
| `VALIDATION_ERROR`        | `400 Bad Request`          | Falha no Bean Validation (ex: campos obrigatórios ou CNPJ inválido).     |
| `UNAUTHORIZED_ACCESS`     | `401 Unauthorized`         | Credenciais incorretas no login ou token JWT expirado/inválido.          |
| `FORBIDDEN_ACCESS`        | `403 Forbidden`            | Violação de Tenant Isolation (tentativa de acessar cotação de terceiros).|
| `RESOURCE_NOT_FOUND`      | `404 Not Found`            | ID da Cotação inexistente ou veículo não localizado na base FIPE.        |
| `BUSINESS_RULE_VIOLATION` | `422 Unprocessable Entity` | Violação de regra (ex: tentativa de aprovar uma cotação em rascunho).    |
| `INTEGRATION_ERROR`       | `502 Bad Gateway`          | Falha de comunicação síncrona com API paralela (ex: Tabela FIPE offline).|

---

## 🚀 Como Rodar o Projeto Localmente

### Pré-requisitos

- Docker e Docker Compose instalados.
- JDK 21+ e Maven (para compilação local, se necessário).

### Passos para Inicialização

**1. Clone o repositório**

```env
git clone [https://github.com/RuanPablo2/fleet-risk.git](https://github.com/RuanPablo2/fleet-risk.git)
cd fleet-risk
```

**2. Configuração de Variáveis (Ambiente)**

Crie um arquivo .env na raiz do projeto com suas credenciais seguras:

```bash
DB_USER=postgres
DB_PASSWORD=sua_senha_db
RABBIT_USER=admin
RABBIT_PASSWORD=sua_senha_rabbit
REDIS_PASSWORD=sua_senha_redis
JWT_SECRET=sua_chave_secreta_longa_em_base64_aqui
MAIL_USERNAME=seu_email@gmail.com
MAIL_PASSWORD=senha_de_app_do_google
```

**3. Executar via Docker Compose**

O projeto já contém scripts em postgres-init que criam os múltiplos bancos automaticamente no primeiro startup.

```bash
# Para subir todo o ecossistema em background construindo as imagens:
docker compose -f docker-compose.prod.yml up -d --build
```
**URLs de Acesso Rápido**
- **API Gateway (Centralizador):** `http://localhost:8080`

- **Swagger UI (Documentação):** `http://localhost:8080/swagger-ui.html`

- **RabbitMQ Management:** `http://localhost:15672 (Credenciais configuradas no .env)`

## 👨‍💻 Autor

Desenvolvido por Ruan Pablo (https://github.com/RuanPablo2). Feedbacks e contribuições são bem-vindos!
