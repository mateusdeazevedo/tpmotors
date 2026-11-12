# TPMotors - Plataforma de Vendas, Compras e Financiamentos de Veículos

O **TPMotors** é um sistema completo para intermediação de conversas, negociações de compra, venda e financiamento de veículos com suporte a pagamentos e checagens de crédito via gateway **PagSeguro**.

---

## 🏛️ Arquitetura do Sistema

```
tpmotors/
├── backend/                               # Backend RESTful em Java 17 / Spring Boot 3
│   ├── pom.xml                            # Dependências Maven (Spring Security, JPA, JJWT, Postgres)
│   └── src/main/
│       ├── java/com/tpmotors/
│       │   ├── TpMotorsApplication.java   # Ponto de entrada da aplicação Spring Boot
│       │   ├── config/                    # Configurações de Segurança e Filtros
│       │   │   ├── SecurityConfig.java         # Regras de acesso, CSRF, Stateless, Roles
│       │   │   ├── JwtAuthenticationFilter.java# Filtro de autenticação JWT
│       │   │   ├── RateLimitingFilter.java     # Mecanismo 2: Rate Limit e Allow List por IP
│       │   │   └── CorsConfig.java             # Suporte a CORS para o frontend puro
│       │   ├── controller/                # Controladores REST da API
│       │   │   ├── AuthController.java         # Login, Registro e Me
│       │   │   ├── CarroController.java        # CRUD e Vitrine de Veículos
│       │   │   ├── UsuarioController.java      # Gestão de Usuários (Admin)
│       │   │   ├── TransacaoController.java    # Checkout PagSeguro, Webhook e Propostas
│       │   │   └── CustomErrorController.java  # Tratamento padronizado de rotas não encontradas (404)
│       │   ├── dto/                       # Objetos de Transferência de Dados (DTOs)
│       │   ├── exception/                 # Manipulação Global de Erros (400, 404, 500)
│       │   ├── model/                     # Entidades JPA (Usuario, Carro, Transacao, Enums)
│       │   ├── repository/                # Interfaces Spring Data JPA
│       │   └── service/                   # Regras de Negócio e Serviços
│       │       ├── JwtService.java             # Emissão e parsing de JWT com payload customizado
│       │       ├── CarroService.java
│       │       ├── UsuarioService.java
│       │       ├── TransacaoService.java
│       │       └── PagSeguroService.java       # Service mockado de integração com PagSeguro
│       └── resources/
│           ├── application.properties     # Configuração JDBC Supabase, JWT e Rate Limit
│           └── schema.sql                 # DDL PostgreSQL + Supabase Row Level Security (RLS)
│
├── frontend/                              # Frontend Puro (HTML5 / CSS3 / Vanilla JS)
│   ├── css/
│   │   └── style.css                      # Design System moderno, dark/light automotivo
│   ├── js/
│   │   └── api.js                         # Wrapper nativo Fetch com autenticação JWT e interceptors
│   ├── index.html                         # Vitrine pública de veículos
│   ├── login.html                         # Formulários de Login e Cadastro de Usuário
│   ├── painel-admin.html                  # Dashboard e gestão administrativa completa
│   └── painel-usuario.html                # Dashboard do cliente (compra, venda e financiamento)
│
└── README.md
```

---

## 🔒 Mecanismos de Segurança Obrigatórios

### 1. Mecanismo 1: Autenticação via JWT (Spring Security)
- O token JWT gerado pelo `JwtService.java` contém obrigatoriamente no payload:
  - `user`: Nome do usuário (ex: `"Administrador TPMotors"`).
  - `status`: Nível de acesso (ex: `"admin"` ou `"user"`).
  - `role`: Role do Spring Security (`ADMIN` ou `USER`).
  - `userId`: Identificador numérico do usuário.
- O `JwtAuthenticationFilter.java` extrai o token enviado no cabeçalho `Authorization: Bearer <token>`, valida a assinatura HMAC-SHA256 e injeta as credenciais no contexto do Spring Security.

### 2. Mecanismo 2: Rate Limiting por IP e Tratamento de Rotas (404 / 429)
- **Rate Limiting (`RateLimitingFilter.java`)**:
  - Implementa controle por janela deslizante baseada em IP (`ConcurrentHashMap` thread-safe).
  - Limite configurável em `application.properties` (ex: 60 requisições/minuto).
  - Possui suporte a **IP Allow List** (bypass para rotas locais ou IPs parceiros autorizados).
  - Retorna **HTTP 429 Too Many Requests** com os cabeçalhos `Retry-After: 60`, `X-RateLimit-Limit` e corpo JSON padronizado caso o limite seja excedido.
- **Tratamento de Rotas Não Encontradas (`CustomErrorController.java` & `GlobalExceptionHandler.java`)**:
  - Garante que qualquer rota inexistente da API retorne **HTTP 404 Not Found** em formato JSON estruturado, sem vazamento de stacktrace ou whitelabel do Spring.

---

## 💳 Integração PagSeguro (Mock Service)

O `PagSeguroService.java` simula a comunicação com a API v4/v5 do PagBank/PagSeguro:
- Criação de ordens de pagamento e links de checkout transparente em Sandbox.
- Geração de código de chave "Copia e Cola" Pix.
- Webhook simulado em `/api/transacoes/webhook-pagseguro` para notificações de alteração de status (`WAITING_FOR_PAYMENT`, `PAID`, `DECLINED`).

---

## 🗄️ Banco de Dados: Supabase (PostgreSQL) com RLS

### Configuração no `application.properties`
As variáveis de conexão JDBC podem ser informadas diretamente ou via variáveis de ambiente:
```properties
spring.datasource.url=jdbc:postgresql://${SUPABASE_DB_HOST:db.tpmotors-ref.supabase.co}:${SUPABASE_DB_PORT:5432}/${SUPABASE_DB_NAME:postgres}?sslmode=require
spring.datasource.username=${SUPABASE_DB_USER:postgres}
spring.datasource.password=${SUPABASE_DB_PASSWORD:sua_senha}
```

### Script SQL (`schema.sql`)
Localizado em `backend/src/main/resources/schema.sql`, contém:
1. Criação das tabelas `usuarios`, `carros` e `transacoes`.
2. Habilitação do **Row Level Security (RLS)**:
   ```sql
   ALTER TABLE usuarios ENABLE ROW LEVEL SECURITY;
   ALTER TABLE carros ENABLE ROW LEVEL SECURITY;
   ALTER TABLE transacoes ENABLE ROW LEVEL SECURITY;
   ```
3. Políticas de segurança (Policies) para leitura pública da vitrine, proteção dos dados do usuário e autorização da `service_role`.

---

## 🚀 Como Executar o Projeto

### 1. Backend (Spring Boot)
Requisitos: Java 17+ e Maven.

```bash
cd backend
mvn clean spring-boot:run
```
A API iniciará na porta `8080`.

### 2. Frontend (HTML/CSS Puro)
Abra qualquer um dos arquivos HTML em um navegador ou utilize qualquer servidor de arquivos estáticos simples (como a extensão Live Server do VS Code ou `python -m http.server 5500` na pasta `frontend`).

- **Vitrine**: `frontend/index.html`
- **Login / Cadastro**: `frontend/login.html`
- **Painel Administrativo**: `frontend/painel-admin.html`
- **Painel do Cliente**: `frontend/painel-usuario.html`

### 3. Credenciais Padrão de Demonstração (Inseridas pelo `schema.sql`)
- **Admin**:
  - E-mail: `admin@tpmotors.com.br`
  - Senha: `admin123`
- **Cliente / Usuário**:
  - E-mail: `cliente@tpmotors.com.br`
  - Senha: `admin123`
