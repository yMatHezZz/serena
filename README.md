# Serena — painel e API

Aplicação acadêmica de telepsicologia em Java 17+, Spring Boot 3.5.9 e PostgreSQL 16, com frontend integrado em HTML/CSS/JavaScript. Tem página inicial, cadastro e login de paciente/psicólogo, portal de conta e painel administrativo com seis CRUDs. Os identificadores técnicos mantêm `serene`. Use apenas dados fictícios.

## Executar no Windows

### Iniciar a demonstração neste computador

Com o PostgreSQL 16 e o JDK 26 já instalados, execute na pasta do projeto:

```powershell
.\scripts\start-local.ps1
```

O script seleciona o Java, inicia o banco sintético exclusivo na porta 55440, configura a conexão e pede uma senha administrativa sem exibi-la. Abra http://127.0.0.1:8082/ após a mensagem `Started`. Use **admin** e a senha informada; ou crie uma conta de paciente/psicólogo na interface. O comando mantém a aplicação no terminal; Ctrl+C encerra. Ele usa apenas o cluster fictício em `.local`, sem acessar o banco padrão na 5432. Não use esse modo para dados reais. Para outro JDK, passe `-JavaHome 'CAMINHO_DO_JDK'`.

### Configuração manual ou outro banco

Pré-requisitos: JDK 17 ou 21 recomendado, PostgreSQL 16 e internet na primeira execução do Maven Wrapper. A validação desta entrega usou JDK 26.0.2 com compilação para Java 17; Maven e Mockito emitiram avisos de compatibilidade futura. Você não precisa instalar Maven separadamente.

Configure `JAVA_HOME` para um JDK, pois o Java padrão deste computador era Java 8:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-26.0.2'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
```

Crie um **banco vazio e exclusivo** chamado `serene` e um usuário com acesso a esse banco. Não aponte esta primeira migration para um banco existente do Serene. O usuário que executa a migration precisa criar tabelas e habilitar `btree_gist`; um administrador pode habilitar essa extensão antes e conceder apenas os privilégios necessários ao usuário da aplicação.

Exporte as variáveis no terminal. Digite suas próprias senhas com `Read-Host`; não as coloque em comandos versionados:

```powershell
$env:DB_URL='jdbc:postgresql://localhost:5432/serene'
$env:DB_USERNAME='serene'
$env:DB_PASSWORD=[System.Net.NetworkCredential]::new('',(Read-Host 'Senha PostgreSQL' -AsSecureString)).Password
$env:ADMIN_USERNAME='admin'
$env:ADMIN_PASSWORD=[System.Net.NetworkCredential]::new('',(Read-Host 'Senha administrativa: mínimo 12 caracteres' -AsSecureString)).Password
.\mvnw.cmd spring-boot:run
```

Abra **http://127.0.0.1:8082/** para acessar a página inicial. Em **Acessar**, entre com e-mail e senha da conta, ou com `ADMIN_USERNAME` e `ADMIN_PASSWORD` para abrir a administração. Em **Criar conta**, escolha Paciente ou Psicólogo. O Spring Boot serve a interface e a API no mesmo endereço; você não precisa instalar Node ou iniciar outro servidor. Se a aplicação já estava rodando antes da atualização, pressione Ctrl+C e execute `mvnw.cmd spring-boot:run` novamente.

Cadastro público: nome, e-mail, senha e três aceites dos textos de demonstração. O servidor grava usuário, papel PATIENT/PROFESSIONAL e as versões dos textos em `registration_consents`. Depois de entrar, complete o perfil: nascimento para paciente; CRP e especialidade para psicólogo. A conta acessa apenas o próprio perfil e até 100 consultas mais recentes vinculadas a ele. A administração continua responsável pelo agendamento. Psicólogo é uma classificação autodeclarada, sem validação de habilitação.

O painel oferece visão geral, consultas, disponibilidades, pacientes, profissionais, atividades e usuários. Cada seção permite cadastrar, buscar, filtrar quando aplicável, ver detalhes, editar e excluir com confirmação. A sequência para começar é **Usuários → Pacientes/Profissionais → Disponibilidades → Consultas**. Os seletores mostram nomes dos cadastros relacionados. As datas e horas dos formulários usam o fuso do navegador.

Os seis CRUDs em `/api/{recurso}` continuam exigindo autenticação administrativa. O cadastro público não concede ADMIN. Contas antigas criadas pelo CRUD não recebem papel de acesso público automaticamente. O endereço `/api` redireciona para a interface. O frontend envia HTTP Basic com credenciais apenas na memória da página, sem localStorage/sessionStorage. Ao sair ou recarregar, faça login novamente. Não faça login diretamente nas rotas JSON para usar o painel, pois o navegador pode manter credenciais Basic nativas em cache.

A proteção de escrita permite chamadas do frontend na mesma origem, com header específico. Origens diferentes, `Origin: null` e requisições cross-site permanecem bloqueadas. Clientes de API sem Origin continuam funcionando. Não configure o frontend em uma porta diferente sem revisar esse contrato.

O Spring Boot não carrega `.env` por padrão. As variáveis da configuração manual acima podem ser exportadas no terminal ou configuradas na IDE. Não versione senhas. A senha administrativa deve ter pelo menos 12 caracteres e no máximo 72 bytes UTF-8. Use HTTPS antes de expor a API fora de localhost.

### PostgreSQL com Docker, opcional

Se você já tiver Docker, configure `DB_PASSWORD` e execute:

```powershell
docker compose up -d postgres
$env:DB_URL='jdbc:postgresql://localhost:5433/serene'
```

O Compose usa porta 5433 para evitar conflito com uma instalação local e volume `serene-data`. Docker não estava disponível nesta sessão; este caminho não foi executado. A API roda fora do contêiner.

## Contrato REST

### Acesso e conta

| Método e rota | Acesso e comportamento |
| --- | --- |
| POST `/api/auth/register` | Público; nome, e-mail, senha, role PATIENT/PROFESSIONAL e termsAccepted/privacyAccepted/consentAccepted=true; retorna 201 |
| GET `/api/auth/me` | Autenticado; identidade e papel, sem senha/hash |
| GET `/api/account` | Paciente/psicólogo; perfil e consultas da própria conta |
| PUT `/api/account/patient-profile` | Paciente; dateOfBirth, gender, phone, address; identidade derivada da autenticação |
| PUT `/api/account/professional-profile` | Psicólogo; licenseNumber, specialty, bio, phone; identidade derivada da autenticação |

Cadastro exige senha de pelo menos 12 caracteres e até 72 bytes UTF-8. E-mail repetido retorna 409. Papéis desconhecidos, ADMIN e aceites ausentes/falsos retornam 400. Conta inativa não autentica. ID de outro usuário não é um campo aceito nos endpoints de perfil próprio.

### Administração

Cada recurso tem `GET /api/{recurso}`, `GET /api/{recurso}/{id}`, `POST /api/{recurso}`, `PUT /api/{recurso}/{id}` e `DELETE /api/{recurso}/{id}`.

| Recurso | Endpoint | Campos do POST/PUT |
| --- | --- | --- |
| Usuários | `/api/users` | `name`, `email`, `status`, `password` |
| Pacientes | `/api/patients` | `userId`, `dateOfBirth`, `gender`, `phone`, `address` |
| Profissionais | `/api/professionals` | `userId`, `licenseNumber`, `specialty`, `bio`, `phone` |
| Disponibilidades | `/api/availability-slots` | `professionalId`, `startAt`, `endAt`, `available` |
| Consultas | `/api/consultations` | `patientId`, `professionalId`, `scheduledStartAt`, `scheduledEndAt`, `status` |
| Atividades | `/api/activities` | `name`, `description`, `activityType`, `configuration`, `active` |

Identificadores são UUID. Datas de nascimento usam `YYYY-MM-DD`; horários usam ISO 8601 com fuso, por exemplo `2026-11-17T14:00:00-03:00`. `professionalId` e `patientId` apontam para o **ID do perfil**, enquanto `userId` aponta para `users`. As respostas incluem `id`, `createdAt` e `updatedAt` e não incluem senha/hash. No PUT de usuário, omitir `password` mantém a senha; no POST ela é obrigatória. PUT substitui os campos editáveis e campos opcionais omitidos ficam nulos. Vínculos com usuário/profissional/paciente não podem ser trocados no PUT.

Listagens aceitam `?page=0&size=20`, com máximo de 100 itens. A resposta contém `content`, `totalElements`, `totalPages`, `number` e `size`. Status de usuário: `ACTIVE` ou `INACTIVE`. Status de consulta: `SCHEDULED`, `CANCELLED` e `COMPLETED`.

| Resultado | HTTP |
| --- | --- |
| Consulta/atualização | 200 |
| Criação com header `Location` | 201 |
| Exclusão | 204 |
| Dados, JSON, UUID ou intervalo inválido | 400 |
| Credenciais ausentes/incorretas | 401 |
| Origem de escrita não permitida | 403 |
| Registro/vínculo não encontrado | 404 |
| Duplicidade, dependência ou conflito de negócio | 409 |

Exemplo de erro: `{"status":400,"message":"Dados inválidos.","errors":{"email":"deve ser um endereço de e-mail bem formado"}}`. As mensagens de Bean Validation dependem do locale; clientes devem usar o status e os nomes dos campos.

## Regras de negócio

- Uma consulta deve caber inteira em um horário disponível do profissional; início deve estar no futuro e término depois do início.
- Paciente e profissional não podem ter consultas sobrepostas. Restrições `EXCLUDE` do PostgreSQL também protegem requisições concorrentes. Intervalos usam `[início,fim)`: consultas consecutivas são permitidas.
- O cancelamento libera o horário. Consultas canceladas/concluídas não aceitam edição; conclusão só é permitida após o término previsto. Não altere horários no mesmo pedido de encerramento.
- Usuários inativos não podem agendar, e um profissional não pode atender a si mesmo.
- Não é permitido remover, desativar ou encurtar disponibilidade que deixe uma consulta agendada descoberta. Cancele/reagende primeiro.
- E-mail, usuário de cada perfil e registro profissional são únicos. Exclusões de registros com dependências retornam 409, sem cascata silenciosa.

## Banco e DER

`src/main/resources/db/migration/V1__schema_serene.sql` cria todas as tabelas do DER, com UUID, TIMESTAMPTZ e JSONB. Flyway aplica essa migration ao iniciar a aplicação; Hibernate apenas valida o esquema (`ddl-auto=validate`). A migration inclui os papéis fixos `PATIENT` e `PROFESSIONAL`, sem contas reais.

Na pasta original, a migration foi aplicada no banco local sintético `127.0.0.1:55439/serene_test`, em cluster independente dentro de `.local/pgdata`. Nenhuma alteração no banco já existente em 5432, em banco remoto ou em produção. Para outro banco, a migration está preparada, não aplicada.

As tabelas `consultation_participants`, `video_sessions`, `session_events`, `activity_sessions` e `activity_responses` estão preparadas no esquema, sem endpoints nem preenchimento automático no MVP. O catálogo `activities` tem CRUD; não executa exercícios. As associações dos seis recursos usam UUID nas entidades e FKs no PostgreSQL. A API não expõe `consultations.notes`.

Atualização de 05/11/2026: cadastro público passa a preencher `user_roles`. A migration incremental `V2__registration_consents.sql` acrescenta o registro versionado de aceites, sem alterar tabelas ou apagar dados existentes. Aplicada somente no banco local sintético durante os testes. Para outros bancos, está preparada, não aplicada.

## Testar e gerar o JAR

No Windows com PostgreSQL instalado no caminho padrão:

```powershell
.\scripts\test-db.ps1 start
.\mvnw.cmd verify
```

O script inicia um cluster exclusivo na porta 55440 com banco/usuário `serene_test`. Usa autenticação `trust` em **loopback, apenas para dados sintéticos de testes**. Não use esse cluster com dados reais ou credenciais de produção. Não interfere no serviço PostgreSQL instalado e não apaga bancos. Para outra instalação, passe `-PostgresBin 'CAMINHO\bin'`. Para parar, use `.\scripts\test-db.ps1 stop`.

Em outro sistema, crie um banco PostgreSQL exclusivo e configure `TEST_DB_URL`, `TEST_DB_USERNAME` e `TEST_DB_PASSWORD` antes de executar `./mvnw verify`. Os testes fazem requisições de CRUD e removem seus registros quando aprovados; falhas podem deixar fixtures fictícias. Nunca aponte `TEST_DB_URL` para produção.

O build gera `target/serene-api-0.0.1-SNAPSHOT.jar`. Com as variáveis de conexão/autenticação configuradas:

```powershell
java -jar target/serene-api-0.0.1-SNAPSHOT.jar
```

## Demonstrar

Com a API em execução, abra outro terminal, configure `ADMIN_USERNAME` e execute:

```powershell
python scripts/demo.py
```

O script pede a senha administrativa sem exibi-la; usa `ADMIN_PASSWORD` se ela já estiver no ambiente. `API_URL` permite trocar a porta. Ele cria dados fictícios com e-mails únicos, confirma leitura após gravação, exige 409 para conflito, cancela e agenda novamente. Mantém os registros para você mostrar na apresentação. Não é um mock de persistência: usa HTTP e PostgreSQL reais. Não faz chamadas a provedores de vídeo.

Veja `docs/DEMONSTRACAO.md` para o pitch de três minutos e `docs/VALIDACAO.md` para evidências e pendências.

## Estrutura e limites

`src/main/java/br/com/serene` contém Controller, Service, Repository, Entity, Request DTO e View DTO de cada recurso. `SchedulingRules` concentra regras de agenda. `SecurityConfig` e `OriginGuardFilter` protegem a API; `ApiErrorHandler` traduz falhas esperadas para HTTP.

Este MVP oferece administração e portal próprio de paciente/psicólogo. Vídeo, chat, prontuário, validação de CRP, verificação de e-mail, auditoria clínica e recuperação de senha ficam pendentes. Os papéis públicos não concedem permissões administrativas. Os textos de aceite são condições da demonstração, não documentos jurídicos aprovados para uso clínico. Não use dados clínicos reais nesta versão. Não houve deploy, push ou criação de PR; a pasta original não tinha Git.

O frontend em `src/main/resources/static` não usa bibliotecas, fontes ou serviços externos. O painel carrega até 10.000 registros de cada recurso e pagina/filtra na interface; volumes maiores exigem busca e paginação de servidor. Acesso exclusivamente administrativo, com dados acadêmicos. Veja `docs/FRONTEND.md` para o plano e as decisões.

Referência de configuração consultada: [Spring Boot 3.5.16: Flyway e PostgreSQL](https://github.com/spring-projects/spring-boot/blob/v3.5.16/spring-boot-project/spring-boot-docs/src/docs/antora/modules/how-to/pages/data-initialization.adoc). A versão fixada no build é 3.5.9 e foi validada no ambiente desta entrega.
