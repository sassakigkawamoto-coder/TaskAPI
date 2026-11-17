# TaskAPI: Tarefa Prática 03 (Sessão HTTP e Service Layer)

API REST de tarefas em Java (Servlets + MySQL). Nesta etapa o projeto da Tarefa 02 ganhou
**usuários com login e sessão HTTP** e uma **Service Layer** que a API e um **importador de linha
de comando** compartilham.

Em resumo:
- Cada pessoa faz login e só enxerga as **próprias** tarefas.
- A regra de negócio (concluir tarefa, título obrigatório, isolamento entre usuários, transação)
  vive em um único lugar, a Service Layer, e é usada tanto pela API quanto pelo importador.
- Os Commands e servlets só **traduzem HTTP**: leem a entrada, chamam um serviço e devolvem a resposta.

---

## 1. Tecnologias e versões

| Ferramenta | Versão | Observação |
|---|---|---|
| Java (JDK) | **25 LTS** (25.0.5) | O mínimo exigido pelo Tomcat 11 é Java 17; o código usa recursos do Java 17 em diante |
| Apache Tomcat | **11.0.29** | Implementa Jakarta Servlet 6.1 (pacotes `jakarta.*`) |
| Jakarta Servlet API | **6.1** | Usa o `lib/servlet-api.jar` do próprio Tomcat |
| MySQL Server | **9.7.4 LTS** | A versão 8.4 LTS também serve |
| MySQL Connector/J (driver JDBC) | **26.10.0** | Compatível com MySQL 8.4 ou mais novo |
| Gson (JSON) | **2.15.0** | Converte objetos para JSON e lê o corpo das requisições |

Versões conferidas em novembro de 2026. Qualquer versão mais nova da mesma linha funciona.
A senha é protegida com **PBKDF2** do próprio JDK, sem biblioteca extra.

Os `.jar` ficam na pasta `lib/` e **não** são versionados (estão no `.gitignore`):
`gson-2.15.0.jar` e `mysql-connector-j-26.10.0.jar`.

---

## 2. Estrutura do projeto

```
src/main/java/br/edu/ifsp/web/
├── model/      Tarefa.java, Usuario.java          (regras do problema, sem HTTP e sem SQL)
├── dao/        ConnectionFactory, TarefaDAO, UsuarioDAO   (única camada com SQL)
├── service/    contratos, implementações e exceções da aplicação
├── command/    Commands da TaskAPI (traduzem HTTP)
├── servlet/    TaskApiServlet (/tarefas) e AutenticacaoServlet (/auth)
├── filter/     FiltroTempo (Tarefa 02) e FiltroAutenticacao
├── listener/   AplicacaoListener (cria os serviços) e SessoesListener (conta sessões)
└── cli/        ImportadorConclusoes.java
src/main/webapp/WEB-INF/web.xml    expiração da sessão e ordem dos filtros
migracao-03.sql                    evolui o banco da Tarefa 02
schema.sql                         estado final do banco
```

### Como as camadas se conversam

```
Clientes   API (curl/Postman)  ·  Importador de linha de comando
   ↓
Filtros    tempo → autenticação
   ↓
Controlador (TaskApiServlet / AutenticacaoServlet) + Commands: só traduzem HTTP
   ↓
Service Layer: operações de negócio, transação e regras de acesso
   ↓
Domínio    Tarefa e Usuario
   ↓
DAO        TarefaDAO e UsuarioDAO (único lugar com SQL)
   ↓
MySQL      banco taskdb
```

O importador entra direto na Service Layer, sem passar por filtros nem por controladores.

---

## 3. Banco de dados

**Banco já existente da Tarefa 02** (mantém os dados; as tarefas antigas passam a pertencer à Ana):

```bash
mysql -u root -p taskdb < migracao-03.sql
```

**Banco novo, do zero:** `mysql -u root -p < schema.sql` e depois insira os usuários de teste
(trecho de `INSERT` do `migracao-03.sql`).

O que mudou:
- Tabela `usuarios`: `id` (autoincremento), `nome`, `email` (único) e `senha_hash`, todos obrigatórios.
- Tabela `tarefas`: nova coluna `usuario_id`, obrigatória e com chave estrangeira para `usuarios`.
  Assim o próprio banco garante que toda tarefa tem exatamente um dono.
- A senha **nunca** é guardada em texto puro: fica o hash PBKDF2 (formato `pbkdf2$iterações$salt$hash`).

**Usuários de teste** (cada um com 3 tarefas, concluídas e pendentes). As senhas abaixo são só de teste:

| Nome | E-mail | Senha |
|---|---|---|
| Ana Souza | ana@exemplo.com | ana12345 |
| Bruno Lima | bruno@exemplo.com | bruno12345 |

---

## 4. Configuração

O acesso ao banco é configurado por variáveis de ambiente, para não versionar senha:

| Variável | Padrão | Para que serve |
|---|---|---|
| `DB_USER` | `root` | usuário do MySQL |
| `DB_SENHA` | (vazia) | senha do MySQL |
| `-Ddb.url=...` | `jdbc:mysql://localhost:3306/taskdb?...` | endereço do banco (opcional) |

Defina `DB_USER` e `DB_SENHA` antes de iniciar o Tomcat e antes de rodar o importador.
Exemplo: `export DB_SENHA=minha_senha_local`.

---

## 5. Compilar e executar

Coloque `gson-2.15.0.jar` e `mysql-connector-j-26.10.0.jar` na pasta `lib/` e defina
`CATALINA_HOME` (pasta do Tomcat). Em Windows, use o Git Bash ou troque `:` por `;` nos classpaths.

```bash
# 1. compilar
mkdir -p build/WEB-INF/classes build/WEB-INF/lib
javac -cp "lib/*:$CATALINA_HOME/lib/servlet-api.jar" \
      -d build/WEB-INF/classes $(find src/main/java -name '*.java')

# 2. montar a aplicação
cp lib/*.jar build/WEB-INF/lib/
cp src/main/webapp/WEB-INF/web.xml build/WEB-INF/

# 3. publicar no Tomcat (o nome da pasta é o caminho da URL)
cp -r build "$CATALINA_HOME/webapps/taskapi"
"$CATALINA_HOME/bin/startup.sh"
```

A API fica em `http://localhost:8080/taskapi`.

---

## 6. A API

Todo erro volta em JSON no formato `{"erro": "mensagem"}`.

### Autenticação

| Requisição | Sucesso | Erros |
|---|---|---|
| `POST /auth/login` com `{"email": "...", "senha": "..."}` | 200 com os dados do usuário (sem senha) e sessão criada | 401 (mesma mensagem para e-mail inexistente e senha errada) |
| `POST /auth/logout` | 204 | |
| `GET /auth/me` | 200 com o usuário e o número de sessões ativas | 401 sem sessão |

### Tarefas (todas exigem login; sem sessão a resposta é 401)

| Requisição | Sucesso | Erros |
|---|---|---|
| `GET /tarefas` | 200 com as tarefas do usuário | |
| `GET /tarefas/{id}` | 200 | 404 (não existe ou é de outro usuário) |
| `POST /tarefas` com `{"titulo": "...", "descricao": "..."}` | 201 com a tarefa criada e o id gerado | 400 dados inválidos (por exemplo, sem título) |
| `PUT /tarefas/{id}` | 200 com a tarefa atualizada | 400 · 404 |
| `PATCH /tarefas/{id}` (concluir) | 204 | 404 · 409 (já estava concluída) |
| `DELETE /tarefas/{id}` | 204 | 404 |

Falha interna (por exemplo, banco fora do ar) devolve 500.

Exemplo com `curl` (o `-c`/`-b` guarda o cookie da sessão):

```bash
curl -i -c cookies.txt -b cookies.txt -X POST http://localhost:8080/taskapi/auth/login \
     -H 'Content-Type: application/json' -d '{"email":"ana@exemplo.com","senha":"ana12345"}'
curl -i -c cookies.txt -b cookies.txt http://localhost:8080/taskapi/tarefas
```

### Como a sessão funciona
- No login o identificador da sessão **muda** (proteção contra fixação de sessão).
- A sessão guarda apenas o usuário **sem a senha** e expira após **30 minutos** sem uso (`web.xml`).
- O `FiltroAutenticacao` barra `/tarefas/*` sem usuário na sessão, sem criar sessão nova.
- O `SessoesListener` escreve no console cada sessão criada e destruída, com a contagem de sessões
  ativas, que é o valor devolvido por `GET /auth/me`.

---

## 7. O importador de linha de comando

Conclui várias tarefas de uma vez, **pela mesma operação de serviço** do `PATCH /tarefas/{id}`.
Roda fora do Tomcat e usa o mesmo banco.

Arquivo de texto com **um id por linha** (`ids.txt`):

```
1
2
999999
```

Execução (com `DB_SENHA` definida):

```bash
java -cp "build/WEB-INF/classes:lib/*" br.edu.ifsp.web.cli.ImportadorConclusoes \
     ids.txt ana@exemplo.com ana12345
```

Para cada linha ele imprime o resultado, por exemplo:

```
linha 1 (id 1): concluída
linha 2 (id 2): já estava concluída
linha 3 (id 999999): não encontrada
```

- Uma tarefa de outro usuário aparece como **não encontrada**.
- Um erro em uma linha não interrompe as seguintes.
- Credenciais inválidas: mensagem de erro e código de saída **1**. Problemas de argumentos ou de
  leitura do arquivo saem com código **2**.

---

## 8. Roteiro de verificação (item 09)

Execute as etapas na ordem, com o banco recém-migrado e o Tomcat no ar, usando `curl` com
`-c cookies.txt -b cookies.txt` para manter o cookie da sessão entre as requisições
(para o importador, a etapa 11, defina `DB_SENHA` antes).

Para as etapas 5 e 11, use ids reais: `GET /tarefas` logado como Ana mostra os ids dela, e o mesmo
`GET /tarefas` logado como Bruno mostra os dele.

| # | Ação | Resultado esperado |
|---|---|---|
| 1 | `GET /tarefas` sem login | 401 com corpo JSON |
| 2 | `POST /auth/login` com senha errada | 401 |
| 3 | `POST /auth/login` como Ana | 200, sem senha no corpo, identificador de sessão diferente do anterior |
| 4 | `GET /tarefas` | 200 apenas com as tarefas da Ana |
| 5 | `GET`, `PUT`, `PATCH` e `DELETE` em uma tarefa do Bruno | 404 em todos; a tarefa do Bruno permanece intacta |
| 6 | `POST /tarefas` sem título | 400 |
| 7 | `POST /tarefas` válido | 201 com o id gerado |
| 8 | `PATCH` na tarefa criada, duas vezes | 204 na primeira e 409 na segunda |
| 9 | `GET /auth/me` | 200 com o usuário e as sessões ativas |
| 10 | `POST /auth/logout` e depois `GET /tarefas` | 204 e 401 |
| 11 | Importador com ids da Ana, um id do Bruno e um id inexistente | conclui os da Ana e informa "não encontrada" para os outros, sem parar |

---

## 9. Decisões de projeto

- **Conexão e transação:** cada operação de serviço abre a própria conexão e a própria transação
  (classe `Transacao`): commit se tudo der certo, rollback se algo falhar. Por isso o `FiltroConexao`
  da Tarefa 02 foi removido.
- **Exceções da aplicação:** o serviço nunca deixa escapar `SQLException`. Os clientes recebem
  `RecursoNaoEncontradoException` (404), `RegraDeDominioException` (409), `DadosInvalidosException` (400),
  `CredenciaisInvalidasException` (401) ou `FalhaInternaException` (500).
- **Regra de conclusão no domínio:** `Tarefa.concluir()` recusa concluir duas vezes e não existe
  nenhum `setConcluida`. O título obrigatório também é validado no domínio, então vale para a API
  e para o importador.
- **Isolamento entre usuários:** toda consulta do DAO filtra pelo dono, então a tarefa de outro
  usuário simplesmente "não existe" (404).
- **Dependência só de contratos:** controladores, Commands e importador usam as interfaces
  `TarefaService` e `AutenticacaoService`. Só o `AplicacaoListener` (na API) e o `main` do importador
  criam as implementações.
- **Credencial protegida:** o hash nunca sai do serviço. Na classe `Usuario` ele é `transient`,
  então não aparece em JSON nem na sessão.

---

## 10. Relatório

**1. Caminho de um `PATCH /tarefas/{id}` e de uma linha do importador. Onde se encontram?**
O `PATCH` atravessa o filtro de tempo e o de autenticação e só chega ao `TaskApiServlet` se houver
usuário na sessão. O servlet resolve a rota e o `ConcluirTarefaCommand` chama
`concluirTarefa(usuario, id)`. O serviço abre a transação, busca a tarefa do usuário, executa
`concluir()` e grava. O importador autentica pelo serviço de autenticação, lê o arquivo e chama a
mesma `concluirTarefa(usuario, id)` para cada linha. Os dois caminhos se encontram nessa chamada,
dentro da Service Layer.

**2. Como a conexão e a transação chegam à Service Layer? O que aconteceu com o `FiltroConexao`?**
Cada operação de serviço abre a própria conexão pela classe `Transacao`, que dá commit em caso de
sucesso e rollback em qualquer falha, e repassa a conexão aos DAOs. O `FiltroConexao` foi removido
porque prendia o banco ao HTTP, o importador não passa por filtro e uma requisição recusada com 401
não deve abrir conexão. O custo é uma conexão por operação, que um pool resolveria.

**3. Por que os serviços recebem o usuário como parâmetro, e não o leem da sessão?**
Ler a sessão obrigaria o serviço a conhecer classes de servlet, o que a especificação proíbe. O
importador deixaria de funcionar, pois roda fora do Tomcat, sem requisição nem sessão. Receber o
usuário também deixa explícito quem executa a operação e facilita testar o serviço isolado.

**4. Por que o identificador da sessão muda no login? Por que 404 e não 403?**
O identificador muda para impedir a fixação de sessão: se um atacante conhecesse o identificador
antes do login, ele deixaria de valer depois da troca. O 404 evita revelar que a tarefa existe e
pertence a outra pessoa, o que um 403 confirmaria. Como o DAO filtra pelo dono, para o serviço essa
tarefa simplesmente não existe.

**5. A TaskAPI da Tarefa 02 precisava de Service Layer? O que mudou? Por que esse controlador para `/auth`?**
Na Tarefa 02 a camada era dispensável: havia um só cliente e a lógica era quase só CRUD, então os
Commands bastavam. Agora há um segundo cliente (o importador), regras de negócio reais (isolamento
entre usuários, conclusão única, título obrigatório) e a necessidade de um ponto único para
transação e tradução de erros. Para `/auth` foi escolhido um controlador simples com três ações
fixas, porque não há comportamento comum nem tantas rotas que justifiquem um Front Controller. No
`/tarefas` ele compensa, com seis rotas e tratamento de erro compartilhado.

---

## 11. Verificação da organização

- Nenhuma classe de `command` e `servlet` usa DAO, SQL ou `Connection`.
- Nenhuma classe de `service` importa `jakarta.servlet`.
- Nenhuma classe de `model` importa `jakarta.servlet` nem `java.sql`.
- Nenhuma senha real está versionada e os `.jar` ficam fora do repositório.
- O histórico mostra a refatoração em etapas, com ao menos um commit por item de 01 a 08.
