# Serena: referência visual e cadastro

Pedido: aproximar o frontend das três referências fornecidas, com fundo escuro, acentos azul/verde, landing page e abas Entrar/Criar conta. Remover textos decorativos e manter descrições verificáveis.

Implementação: manter HTML/CSS/JS no Spring Boot; nome visual Serena, identificadores Java e API preservados. Landing pública sem simular sessão online real. Cadastro público de paciente/psicólogo cria usuário e papel do DER, com aceite versionado dos três textos de demonstração. Senhas BCrypt; acesso HTTP Basic em memória. Cadastro público não concede ADMIN. Portal limitado ao perfil e consultas da própria conta; painel administrativo continua separado.

Fluxo: início → criar conta → escolher paciente/psicólogo, nome, e-mail, senha e aceites → entrar → completar perfil com nascimento ou CRP/especialidade → consultar os próprios agendamentos. Psicólogo autodeclarado não equivale a credenciamento profissional validado. Sem videoconferência, chat, prontuário ou promessa de conformidade regulatória.

Plano e aceite: testes antes de alterar segurança; validar cadastro dos dois papéis, aceite obrigatório, duplicidade, login, bloqueio de administrador e isolamento entre contas; implementar API, reformular frontend, testar navegador desktop/mobile e atualizar README. Migration V2 somente em banco local sintético; outros ambientes preparados, não aplicados. Preservar cadastros existentes.

Resultado: implementado; 16 testes aprovados, cadastro/login/perfil dos dois papéis verificados com Playwright e revisão de segurança focada concluída. Evidências em VALIDACAO-REDESIGN.md. Agendamentos continuam sob gestão administrativa; portal próprio oferece leitura da agenda, sem adicionar funcionalidades clínicas.
