# Frontend administrativo Serene

Objetivo: disponibilizar pelo próprio Spring Boot login, visão geral e CRUD completo de usuários, pacientes, profissionais, disponibilidades, consultas e atividades.

Decisões locais: HTML/CSS/JavaScript sem dependências externas em `src/main/resources/static`; mesma origem da API. Visual verde profundo, fundo creme, tipografia com serifas em títulos e navegação responsiva. Não há serviços ou publicação externos. Credenciais HTTP Basic apenas em memória, sem localStorage ou sessionStorage; recarregar exige login novamente. Dados são lidos e gravados no PostgreSQL existente da API.

Plano: (1) testes para recursos públicos e escrita de mesma origem; (2) permitir somente assets públicos e validar origem/header das mutações; (3) construir login, painel, tabelas com busca/filtros/paginação, formulários com relações por nome, detalhes e exclusão confirmada; (4) validar os seis CRUDs, erros, teclado e mobile no navegador; (5) atualizar instruções.

Aceite: acessar `/` sem login apresenta formulário; acesso a dados exige administrador. É possível criar usuário, vincular perfil, abrir disponibilidade e agendar consulta pelo navegador. Carregamento, vazio, erro, cancelamento e conflitos têm retorno visível. Senhas e dados não entram como HTML. Bibliotecas, fontes e ícones externos não são necessários. Vídeo e exercícios em sessão permanecem pendentes e identificados na interface.

Resultado: implementado e validado. `mvnw verify`: nove testes aprovados. Playwright: cadastros, edições, detalhes, exclusões, conflito, cancelamento, falha de rede, login inválido, mobile, Tab/Escape e ausência de credenciais em storage verificados. Evidências em VALIDACAO.md. Reiniciar o processo antigo é necessário para carregar as novas classes de segurança e os assets.
