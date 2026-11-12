-- ===================================================================
-- TPMotors - DDL e Configurações de Segurança do Banco de Dados
-- Compatibilidade: PostgreSQL 14+ / Supabase
-- ===================================================================

-- 1. Extensões úteis
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 2. Tabela: usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    telefone VARCHAR(30),
    documento VARCHAR(20) UNIQUE, -- CPF ou CNPJ
    role VARCHAR(20) NOT NULL DEFAULT 'USER', -- 'ADMIN' ou 'USER'
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    data_cadastro TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. Tabela: carros
CREATE TABLE IF NOT EXISTS carros (
    id BIGSERIAL PRIMARY KEY,
    marca VARCHAR(60) NOT NULL,
    modelo VARCHAR(80) NOT NULL,
    ano_fabricacao INT NOT NULL,
    ano_modelo INT NOT NULL,
    preco NUMERIC(14, 2) NOT NULL,
    quilometragem INT DEFAULT 0,
    cambio VARCHAR(30) DEFAULT 'Manual',
    combustivel VARCHAR(30) DEFAULT 'Flex',
    cor VARCHAR(40),
    descricao TEXT,
    imagem_url TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'DISPONIVEL', -- 'DISPONIVEL', 'EM_NEGOCIACAO', 'VENDIDO', 'INATIVO'
    usuario_proprietario_id BIGINT NOT NULL,
    data_cadastro TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_carros_usuario FOREIGN KEY (usuario_proprietario_id) 
        REFERENCES usuarios (id) ON DELETE CASCADE
);

-- 4. Tabela: transacoes (Compras, Vendas e Financiamentos com PagSeguro)
CREATE TABLE IF NOT EXISTS transacoes (
    id BIGSERIAL PRIMARY KEY,
    codigo_transacao VARCHAR(64) NOT NULL UNIQUE, -- Identificador único do TPMotors
    carro_id BIGINT NOT NULL,
    comprador_id BIGINT NOT NULL,
    vendedor_id BIGINT NOT NULL,
    tipo VARCHAR(30) NOT NULL DEFAULT 'COMPRA_DIRETA', -- 'COMPRA_DIRETA', 'FINANCIAMENTO', 'RESERVA'
    valor_total NUMERIC(14, 2) NOT NULL,
    valor_entrada NUMERIC(14, 2) DEFAULT 0,
    numero_parcelas INT DEFAULT 1,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDENTE', -- 'PENDENTE', 'EM_ANALISE', 'APROVADA', 'CANCELADA', 'ESTORNADA'
    id_transacao_pagseguro VARCHAR(100), -- Referência externa do gateway PagSeguro
    link_pagamento TEXT,
    qrcode_pix TEXT,
    data_criacao TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transacoes_carro FOREIGN KEY (carro_id) 
        REFERENCES carros (id) ON DELETE RESTRICT,
    CONSTRAINT fk_transacoes_comprador FOREIGN KEY (comprador_id) 
        REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT fk_transacoes_vendedor FOREIGN KEY (vendedor_id) 
        REFERENCES usuarios (id) ON DELETE RESTRICT
);

-- Índices para otimização de consultas
CREATE INDEX IF NOT EXISTS idx_usuarios_email ON usuarios(email);
CREATE INDEX IF NOT EXISTS idx_carros_status ON carros(status);
CREATE INDEX IF NOT EXISTS idx_carros_preco ON carros(preco);
CREATE INDEX IF NOT EXISTS idx_transacoes_codigo ON transacoes(codigo_transacao);
CREATE INDEX IF NOT EXISTS idx_transacoes_status ON transacoes(status);

-- ===================================================================
-- 5. ATIVAÇÃO DO ROW LEVEL SECURITY (RLS) - SUPABASE
-- Proteção das tabelas contra acessos diretos não autorizados no Supabase
-- ===================================================================

-- Habilita RLS em todas as tabelas principais
ALTER TABLE usuarios ENABLE ROW LEVEL SECURITY;
ALTER TABLE carros ENABLE ROW LEVEL SECURITY;
ALTER TABLE transacoes ENABLE ROW LEVEL SECURITY;

-- Política 1: Usuários
-- Usuários autenticados no Supabase Auth ou a Service Role podem ler/editar seus próprios dados
DROP POLICY IF EXISTS "Permitir leitura publica basica de usuarios" ON usuarios;
CREATE POLICY "Permitir leitura publica basica de usuarios" 
ON usuarios FOR SELECT 
USING (ativo = TRUE);

DROP POLICY IF EXISTS "Permitir atualizacao pelo proprio usuario ou service_role" ON usuarios;
CREATE POLICY "Permitir atualizacao pelo proprio usuario ou service_role" 
ON usuarios FOR UPDATE 
USING (
    auth.role() = 'service_role' OR 
    (auth.jwt() ->> 'email') = email
);

-- Política 2: Carros
-- Qualquer pessoa pode consultar carros disponíveis na vitrine
DROP POLICY IF EXISTS "Leitura publica de carros disponiveis" ON carros;
CREATE POLICY "Leitura publica de carros disponiveis" 
ON carros FOR SELECT 
USING (status != 'INATIVO');

-- Apenas o proprietário do carro ou a service_role do backend podem alterar ou remover o anúncio
DROP POLICY IF EXISTS "Modificacao de carros pelo proprietario ou backend" ON carros;
CREATE POLICY "Modificacao de carros pelo proprietario ou backend" 
ON carros FOR ALL 
USING (
    auth.role() = 'service_role' OR 
    EXISTS (
        SELECT 1 FROM usuarios u 
        WHERE u.id = carros.usuario_proprietario_id 
        AND u.email = (auth.jwt() ->> 'email')
    )
);

-- Política 3: Transações
-- Usuários só enxergam transações onde atuam como comprador ou vendedor; o backend tem acesso total
DROP POLICY IF EXISTS "Visualizacao restrita de transacoes para participantes" ON transacoes;
CREATE POLICY "Visualizacao restrita de transacoes para participantes" 
ON transacoes FOR SELECT 
USING (
    auth.role() = 'service_role' OR
    EXISTS (
        SELECT 1 FROM usuarios u 
        WHERE (u.id = transacoes.comprador_id OR u.id = transacoes.vendedor_id) 
        AND u.email = (auth.jwt() ->> 'email')
    )
);

DROP POLICY IF EXISTS "Manipulacao de transacoes pelo backend service_role" ON transacoes;
CREATE POLICY "Manipulacao de transacoes pelo backend service_role" 
ON transacoes FOR ALL 
USING (auth.role() = 'service_role');

-- Inserção de dados iniciais (Admin e Veículos de Demonstração)
-- Senha padrão do admin: 'admin123' (hash bcrypt para teste)
INSERT INTO usuarios (nome, email, senha, telefone, documento, role, ativo)
VALUES 
('Administrador TPMotors', 'admin@tpmotors.com.br', '$2a$10$w66Ea0uHnJekc6r9F0ErvOzZ4iHjWbU3q8d1b11WlB71Jd1qF1H4C', '(11) 99999-0000', '00000000000', 'ADMIN', true),
('Cliente Comprador', 'cliente@tpmotors.com.br', '$2a$10$w66Ea0uHnJekc6r9F0ErvOzZ4iHjWbU3q8d1b11WlB71Jd1qF1H4C', '(11) 98888-1111', '11122233344', 'USER', true)
ON CONFLICT (email) DO NOTHING;
