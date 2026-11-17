-- Estado final do banco (Tarefa 03). Ajuste a tabela tarefas ao seu schema da Tarefa 02.
CREATE DATABASE IF NOT EXISTS taskdb CHARACTER SET utf8mb4;
USE taskdb;

CREATE TABLE IF NOT EXISTS usuarios (
    id         INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nome       VARCHAR(100) NOT NULL,
    email      VARCHAR(150) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    CONSTRAINT uk_usuarios_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tarefas (
    id         INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    titulo     VARCHAR(200) NOT NULL,
    descricao  TEXT,
    concluida  BOOLEAN      NOT NULL DEFAULT FALSE,
    usuario_id INT          NOT NULL,
    CONSTRAINT fk_tarefas_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
) ENGINE=InnoDB;
