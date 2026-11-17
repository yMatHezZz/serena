# Validação do redesign — 05/11/2026

Resultado: página inicial escura, abas Entrar/Criar conta, cadastro de paciente e psicólogo, portal próprio e painel administrativo preservado. Textos reduzidos e referências de vídeo identificadas como prévia sem integração.

Atualização de inicialização: reproduzi a execução sem DB_* com JDK correto; o padrão tenta autenticar o usuário serene no banco da 5432 e falha. Acrescentei `scripts/start-local.ps1`, que seleciona o JDK, inicia o cluster sintético e configura a conexão 55439, pedindo a senha administrativa. Executei o script com senha fictícia de teste: aplicação iniciou na 8080, GET / retornou 200 e GET /api/auth/me retornou papel ADMIN. Encerrei somente o processo iniciado para essa verificação, liberando a porta. Não repeti a suíte Java, pois a mudança foi no script de inicialização e no README.

## Comandos

- `mvnw.cmd verify`: aprovado, 16 testes (9 ApiTest + 7 AccountTest), sem falhas/erros/ignorados.
- `node --check` nos módulos app.js, public.js e account.js: aprovado.
- Após alterar apenas dois textos da interface, `mvnw.cmd package -DskipTests`: aprovado; os 16 testes anteriores cobrem a mesma implementação de comportamento. Não declarei uma segunda execução da suíte nesse empacotamento.
- PostgreSQL 16.14 local na porta 55439: Flyway registra versões 1 e 2 com success=true. Migration V2 aplicada somente nesse ambiente sintético; preparada, não aplicada em outros bancos.

Os testes de cadastro falharam antes da implementação. A nova autenticação revelou reutilização de um UserDetails administrativo cujo password o provider apaga após autenticar. Reproduzi com teste de três autenticações consecutivas e corrigi construindo UserDetails por requisição. A suíte completa passou depois da correção.

## Comportamentos cobertos

Cadastro PATIENT e PROFESSIONAL; senha e e-mail; aceites obrigatórios; recusa de ADMIN e papéis desconhecidos; duplicidade; perfil próprio; bloqueio de rota administrativa para conta pública; recusa de campo userId enviado pelo cliente; recusa de edição com papel diferente; conta inativa sem autenticação; autenticação administrativa repetida. Duas contas de paciente viram apenas a própria consulta, enquanto o profissional vinculado viu ambas. Mantive também os nove testes dos seis CRUDs, concorrência, validação e proteção de origem.

Fixtures dos testes são fictícias e podem permanecer no banco local junto aos seus aceites. Nenhuma consulta SQL envolveu banco remoto ou dados reais.

## Navegador e visual

Executei o JAR na porta 18081 com Playwright e banco PostgreSQL real:

- Página inicial em 1440×1000, sem overflow horizontal. Captura: `.local/serena-home.png`.
- Cadastro de paciente, abertura e leitura de Termos, marcação dos três aceites, confirmação de cadastro, login e gravação do próprio perfil com nascimento e telefone.
- Cadastro de psicóloga fictícia, login, gravação de CRP/especialidade. A tentativa de GET `/api/users` com essa conta retornou 403.
- Login administrativo e acesso à listagem de profissionais continuaram funcionando no tema novo.
- Cadastro em 390×844 sem overflow horizontal; opção Paciente selecionada por padrão. Captura: `.local/serena-register.png`.
- O console registrou o 403 esperado durante o teste de autorização; não observei exceção JavaScript da aplicação.

Os dados exibidos nas capturas são fictícios. Não houve deploy, push ou PR. A aplicação de teste foi encerrada após a validação; o banco local fica disponível para execução manual.

## Revisão de segurança

Codex Security Standard concluído em `src/main`, sem achados reportáveis. Revisão estática sequencial pelo implementador, sem baseline independente ou análise de vulnerabilidades de dependências.

Relatório: `%USERPROFILE%\.codex\state\plugins\codex-security\scans\Darlon\unversioned_20261105T230214Z_mtc1_1j9\report.md`.

Medição retornada pelo plugin para o contexto de execução: 843.710 tokens totais, 838.830 de entrada, 827.776 de entrada em cache. Não representa o custo isolado da revisão.

O registro de aceites guarda versões demo-2026-11-v1 e data/hora; os textos são condições de teste, não documentos jurídicos aprovados. Cadastro profissional é autodeclarado, sem validação de CRP. E-mail não verificado, recuperação de senha, vídeo, chat e prontuário ficam pendentes. Antes de qualquer uso real, revisar acesso, operação, documentação e tratamento dos dados.
