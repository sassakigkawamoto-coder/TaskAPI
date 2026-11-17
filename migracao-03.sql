-- Migração da Tarefa 03: evolui o banco da Tarefa 02 sem recriá-lo.
-- Ajuste nomes de colunas se o seu schema da Tarefa 02 for diferente.
USE taskdb;

CREATE TABLE usuarios (
    id         INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nome       VARCHAR(100) NOT NULL,
    email      VARCHAR(150) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    CONSTRAINT uk_usuarios_email UNIQUE (email)
) ENGINE=InnoDB;

-- Senhas de teste: ana12345 e bruno12345 (guardadas como hash PBKDF2)
INSERT INTO usuarios (nome, email, senha_hash) VALUES
 ('Ana Souza',  'ana@exemplo.com',   'pbkdf2$120000$fFqGV1kuWOikb5scC9oJ3Q==$MsRhLwElTwZJ+FMf1lsb9ZsCda2wUnTukoCqJkPAngQ='),
 ('Bruno Lima', 'bruno@exemplo.com', 'pbkdf2$120000$6VopyWCmZEXBsgkzasIUxQ==$scxfyb7m33PGOmBflRMPX677lelYY0rGixOXlpPZExk=');

-- Tarefas já existentes passam a pertencer à Ana
ALTER TABLE tarefas ADD COLUMN usuario_id INT NULL;
UPDATE tarefas SET usuario_id = (SELECT id FROM usuarios WHERE email = 'ana@exemplo.com');

-- Dados de teste: 3 tarefas por usuário, concluídas e pendentes
INSERT INTO tarefas (titulo, descricao, concluida, usuario_id) VALUES
 ('Estudar Service Layer', 'Ler o capítulo do Fowler',  TRUE,  (SELECT id FROM usuarios WHERE email='ana@exemplo.com')),
 ('Escrever relatório',    'Responder as 5 perguntas',   FALSE, (SELECT id FROM usuarios WHERE email='ana@exemplo.com')),
 ('Revisar commits',       'Um commit por item',         FALSE, (SELECT id FROM usuarios WHERE email='ana@exemplo.com')),
 ('Configurar Tomcat',     'Versão 10+ (jakarta)',       TRUE,  (SELECT id FROM usuarios WHERE email='bruno@exemplo.com')),
 ('Testar com curl',       'Seguir o roteiro do item 09',FALSE, (SELECT id FROM usuarios WHERE email='bruno@exemplo.com')),
 ('Gravar prints',         'Listener e importador',      FALSE, (SELECT id FROM usuarios WHERE email='bruno@exemplo.com'));

-- Agora toda tarefa tem dono, garantido pelo banco
ALTER TABLE tarefas
    MODIFY usuario_id INT NOT NULL,
    ADD CONSTRAINT fk_tarefas_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id);
