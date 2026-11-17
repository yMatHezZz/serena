# Demonstração em até três minutos

**0:00–0:25 — Problema e público.** Uma equipe de telepsicologia precisa cadastrar pacientes e profissionais e organizar consultas sem conflitos. A API Serene oferece esse fluxo administrativo com persistência PostgreSQL.

**0:25–1:15 — Fluxo.** Inicie a aplicação antes da apresentação e abra http://127.0.0.1:8082/. Entre no painel administrativo. Mostre a visão geral, os usuários fictícios, os perfis e a disponibilidade. Em Consultas, agende usando os seletores por nome. Para preparar dados antes do pitch, você também pode executar `python scripts/demo.py`.

**1:15–2:05 — Valor.** Mostre a mensagem de conflito ao repetir o mesmo agendamento. Explique que o banco também bloqueia sobreposições simultâneas. Edite o status para Cancelada e mostre um novo agendamento no mesmo horário. Use busca e detalhes para encontrar o registro.

**2:05–2:35 — Requisitos técnicos.** Mostre as camadas Controller, Service, Repository, Entity e DTO; as validações; a migration e o resultado de `mvnw verify`. São seis recursos com CRUD completo, acima do mínimo de quatro.

**2:35–3:00 — Limites e continuidade.** O MVP tem painel administrativo e API com dados fictícios. Vídeo e atividades em sessão têm tabelas preparadas; integração com provedor não foi implementada. Próximo passo: login e autorização de paciente/profissional.

## Preparação e fallback

Execute o build e o script antes de apresentar. Confira a conexão PostgreSQL e as variáveis em outro terminal; não projete senhas nem arquivos `.env`. Use dados sintéticos e IDs do ensaio.

Se não houver internet, use o JAR já construído e o PostgreSQL local: o fluxo não depende de provedor externo. Se o servidor falhar durante o pitch, mostre `docs/VALIDACAO.md` e o contrato REST, identificando que são evidências do ensaio e não uma execução ao vivo. Não apresente vídeo ou exercícios como integrados.
