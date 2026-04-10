-- Inserção de usuários de teste para ambiente de desenvolvimento
-- Senha para admin@ticketscale.com: admin123
-- Senha para usuario@ticketscale.com: usuario123

INSERT INTO usuarios (id, login, senha, papel) 
VALUES (gen_random_uuid(), 'admin@ticketscale.com', '$argon2id$v=19$m=65536,t=3,p=1$b26kXMjEClgouyQ33uRVUA$KHVkEe76ax/GDEncDTzNt8q2mNpcBAscwUbvbeeU4fw', 'ADMIN')
ON CONFLICT (login) DO NOTHING;

INSERT INTO usuarios (id, login, senha, papel) 
VALUES (gen_random_uuid(), 'usuario@ticketscale.com', '$argon2id$v=19$m=65536,t=3,p=1$yIgUuXAj08F+BQHrhHBDuw$agEUzs3oKFsn5gbOD2EosIiXXSWKXu0ABG9gB2jjLSY', 'USUARIO')
ON CONFLICT (login) DO NOTHING;
