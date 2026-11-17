# Validação — 03/11/2026

Atualização de 05/11/2026: o redesign e o cadastro público passaram em 16 testes. Veja [VALIDACAO-REDESIGN.md](VALIDACAO-REDESIGN.md) para as evidências atuais; as seções abaixo registram a entrega anterior.

## Atualização: frontend integrado

Resultado atual: **9 testes, 0 falhas, 0 erros e 0 ignorados**, com `mvnw.cmd verify` aprovado. O JAR atualizado tem 58.976.216 bytes. `node --check src/main/resources/static/assets/app.js` passou; Node foi usado apenas como ferramenta de validação, sem dependência no produto.

Os dois testes adicionais confirmam shell/assets públicos com API protegida, redirecionamento `/api` para `/`, escrita autorizada da mesma origem e bloqueio de origem diferente, porta diferente, Origin null e chamada de navegador sem header específico. Antes da mudança, ambos falharam com 401/403, como esperado. Ajustei a asserção de `/` para verificar o forward do Spring e testei o conteúdo HTML diretamente em `/index.html`, pois MockMvc não segue forwards internos.

Executei o JAR na porta 18081 com o PostgreSQL sintético na 55439. Preservei o processo da pessoa usuária na 8080. O frontend está no mesmo JAR, sem servidor adicional ou chamadas externas. Para ativar no processo da 8080, reinicie a aplicação no terminal que já contém as variáveis de ambiente.

### Playwright: procedimentos observados

- Login administrativo válido abriu o painel e carregou os dados reais da API; login inválido exibiu erro no formulário.
- Criei dois usuários fictícios, paciente, profissional, disponibilidade e consulta usando a interface e seletores por nome. Confirmei as listagens após gravação.
- Repetir o agendamento exibiu o conflito 409 no formulário. Alterar o status para Cancelada atualizou a lista.
- Cadastrei, editei, consultei detalhes e excluí uma atividade. Editei telefone do paciente, especialidade profissional, disponibilidade e nome de usuário. Excluí as fixtures criadas, começando pelas consultas e seguindo os vínculos. Usuário com perfil vinculado teve a exclusão bloqueada com mensagem visível.
- Busca sem resultado e exclusão do último item filtrado mostraram estado vazio. Interrompi uma requisição pelo Playwright: o painel exibiu falha de conexão; a nova tentativa carregou os dados. Atrasei uma resposta e observei o estado de carregamento.
- Desktop em 1440×960 e mobile em 390×844 não apresentaram overflow horizontal do documento. O menu móvel abriu e navegou; Tab avançou entre campos e Escape fechou o formulário. O diálogo móvel não teve overflow horizontal.
- Um nome de atividade contendo marcação HTML apareceu como texto literal, sem criar elemento img. Nenhum alerta executou. Excluí esse registro de teste.
- Sair e recarregar levaram ao login. localStorage e sessionStorage permaneceram vazios.
- O console registrou falhas de rede esperadas para os testes de 409, 401 e conexão abortada; não observei exceção JavaScript da aplicação.

Capturas locais: `.local/frontend-desktop.png` e `.local/frontend-mobile.png`. As capturas mostram dados fictícios de demonstração; ficam fora do versionamento. Alguns seletores de teste precisaram ser delimitados ao formulário/tabela devido a rótulos repetidos; isso não exigiu alteração no produto.

### Segurança e limites atuais

Concluí uma nova revisão Standard do Codex Security em `src/main`, incluindo frontend e controles de origem, sem achados reportáveis. Revisão pelo próprio implementador, sem baseline independente. Relatório: `%USERPROFILE%\.codex\state\plugins\codex-security\scans\Darlon\unversioned_20261104T020602Z_vl718uvx\report.md`.

Medição retornada pelo plugin para o contexto de execução: 785.681 tokens totais; 781.034 de entrada; 766.080 de entrada em cache. Não representa o custo isolado da revisão. O produto avisou sobre alterações no diretório durante o scan; acrescentei documentação fora de `src/main`, e o relatório permanece vinculado ao snapshot original.

O frontend oferece gestão administrativa dos seis recursos. Vídeo, execução de exercícios em sessão e login próprio de pacientes/profissionais continuam pendentes e identificados na interface. Não houve migration nova nem alteração de banco remoto. O painel carrega até 10.000 registros por recurso; volumes maiores exigem paginação e busca no servidor. A revisão não certifica uso clínico nem inclui análise de dependências.

As seções seguintes registram a entrega inicial da API e suas evidências históricas.

## Entrega inicial: ambiente e resultado

Windows, JDK 26.0.2, Maven Wrapper 3.9.11 e PostgreSQL 16.14. Compilação com `release=17`. Pasta inicial vazia, sem Git, sem aplicação preexistente. Usei apenas dados sintéticos e um cluster independente na porta 55439.

| Procedimento | Resultado | Evidência |
| --- | --- | --- |
| Primeiro `mvn test`, antes da aplicação | Falha esperada | Classe `SereneApplication` ainda inexistente |
| Suíte inicial | Falhou; corrigida | Teste agrupava requisições em uma transação abortada após conflito esperado; removi essa transação externa e mantive as transações reais dos services |
| `mvnw.cmd verify` | Aprovado | 7 testes, 0 falhas, 0 erros, 0 ignorados; código de saída 0 |
| JAR executável | Aprovado | `target/serene-api-0.0.1-SNAPSHOT.jar`, 58.955.758 bytes; Tomcat iniciou em 127.0.0.1:18080 |
| `python scripts/demo.py` contra o JAR | Aprovado | Cadastros, GET após gravação, conflito 409, cancelamento e novo agendamento no mesmo horário |
| Flyway em PostgreSQL real | Aprovado | `flyway_schema_history`: versão 1, success=true |
| Persistência em conexão independente | Aprovado | Consulta SQL após a demonstração encontrou 2 consultas: uma cancelada e uma agendada |
| Script `test-db.ps1 start` | Aprovado | Detectou o cluster já criado e o banco sintético, sem recriá-los |
| Docker Compose | Não executado | Docker indisponível no ambiente |
| Interface e Playwright | Não executado | Entrega contém API; não há interface de navegador |
| Banco remoto/produção | Não executado | Sem destino ou credenciais fornecidos; nenhum banco existente foi alterado |

## Cobertura dos sete testes

`src/test/java/br/com/serene/ApiTest.java` usa MockMvc, aplicação Spring real e PostgreSQL real, sem mocks de Repository ou Service:

1. Credenciais ausentes e incorretas retornam 401.
2. Entrada inválida retorna 400 com erros por campo; UUID inválido retorna 400; registro inexistente retorna 404.
3. Seis recursos passam por POST, listagem, GET por ID, PUT e DELETE; respostas de usuários não contêm senha/hash; conflito de consulta e exclusão de disponibilidade ocupada retornam 409.
4. Consulta fora de disponibilidade, conclusão antecipada e reabertura de consulta cancelada retornam 409; intervalo inválido retorna 400; cancelamento permite novo agendamento.
5. E-mail duplicado e exclusão de usuário com perfil retornam 409.
6. Escrita de navegador retorna 403; paginação acima do limite ou negativa retorna 400.
7. Duas requisições de agendamento simultâneas terminam com exatamente um 201 e um 409.

Os testes fazem requisições com transações independentes. O relatório local do Maven fica em `target/surefire-reports/br.com.serene.ApiTest.txt`. Não equivale a teste de login de paciente: somente o administrador tem acesso nesta versão.

## Revisão de segurança

Executei o fluxo Standard do Codex Security limitado a `src/main`, com revisão estática sequencial pelo próprio implementador. Não houve baseline nem revisão de arquitetura independentes, pois subagentes não foram autorizados. Revisei autenticação, proteção de origem, DTOs, Services, Repositories, configuração e migration; não encontrei achado reportável nesse escopo. Isso não certifica a aplicação para uso clínico.

Relatório: `%USERPROFILE%\.codex\state\plugins\codex-security\scans\Darlon\unversioned_20261104T013348Z_horciqeq\report.md`.

O produto registrou que o diretório mudou durante o scan: acrescentei testes, scripts e documentação fora de `src/main`. O relatório permanece vinculado ao snapshot original. Medição retornada pelo plugin para o chat: total 1.062.848 tokens; entrada 1.053.551; entrada em cache 1.016.448. Esses números incluem contexto acumulado do chat e não medem isoladamente o custo da revisão.

Não executei análise de dependências nem testes contra terceiros. Configuração operacional de TLS, backup, auditoria, retenção e acesso clínico continua fora deste MVP. Logs de violações do PostgreSQL podem conter valores dos registros envolvidos: mantenha apenas dados fictícios neste ambiente.

## Avisos e pendências

Maven/Guice e Mockito emitiram avisos no JDK 26 sobre APIs e carregamento de agente; o build passou. Spring Data avisou que a serialização direta de `Page` pode variar em atualizações: o contrato documentado vale para a versão fixada. Não há linter configurado; compilação, testes e empacotamento passaram.

Prioridades para a equipe/Gemini: (P0, se o escopo passar a incluir usuários finais) implementar login e autorização por paciente/profissional antes de conectar frontend; (P1) integrar participantes, vídeo e sessões de atividades; (P1) adicionar auditoria e política de dados antes de qualquer uso real. A migration exige banco vazio: para integrar a um banco já existente, comparar o esquema e preparar uma migration incremental revisada.

O teste deixa registros fictícios de demonstração no cluster local; não houve push, PR ou deploy. As ferramentas baixadas ficam em `.tools`, o cluster em `.local`, ambos ignorados pelo Git.
