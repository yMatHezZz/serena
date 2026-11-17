# API Serene — plano e decisões

Objetivo: cumprir o trabalho Spring Boot com um fluxo administrativo de telepsicologia baseado no DER fornecido.

Escolha local: monólito Spring Boot 3.5.9, Java 17+, Maven, PostgreSQL 16 e Flyway. Pasta inicial vazia, sem Git e sem README. Nenhum serviço remoto será alterado. Executar diretamente nesta pasta, sem subagentes e sem commits automáticos.

1. Escrever testes HTTP com PostgreSQL real para CRUD, validação, autenticação e regras de agenda; observar falha antes da implementação.
2. Criar Entity, Repository, Service, Controller e DTO para users, patient_profiles, professional_profiles, availability_slots, consultations e activities. Migrations cobrem todas as tabelas do DER; módulos complementares sem endpoints ficam explicitamente pendentes.
3. Bloquear intervalos inválidos, consulta fora da disponibilidade, sobreposição de paciente/profissional e alteração de horário ocupado. Serializar agendamento por locks de usuário e aplicar proteção adicional no PostgreSQL.
4. Exigir HTTP Basic administrativo em todas as rotas. Senha configurada no ambiente; BCrypt para senhas de users; nenhum hash em respostas. Não usar dados reais.
5. Validar com Maven verify e fluxo HTTP em banco isolado; documentar comandos, contratos, exemplos, demonstração e limitações.

Aceite: seis recursos com GET coleção/ID, POST, PUT, DELETE; 400/401/403/404/409 consistentes; persistência PostgreSQL e migration reproduzível. Endpoint GET retorna até 100 itens por página. Edição de consulta permite AGENDADA/CANCELADA/CONCLUIDA, com conclusão somente após início e cancelamento liberando agenda. API voltada à administração; contas de pacientes não recebem acesso administrativo.

Limites: sem frontend, prontuário clínico, videoconferência real ou autenticação pública de pacientes. Não pressupor compatibilidade direta com banco existente: primeira migration requer banco vazio. Dados de exemplo são sintéticos.

Resultado: itens 1–5 concluídos. Sete testes aprovados, JAR gerado e demonstração HTTP executada com PostgreSQL real. Evidências e limitações em VALIDACAO.md. Correção de texto do aceite: conclusão somente após o término previsto, como implementado e testado. Banco remoto permanece não aplicado.
