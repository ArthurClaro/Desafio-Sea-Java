package br.com.sea.desafio.config;

import br.com.sea.desafio.domain.Role;
import br.com.sea.desafio.domain.Usuario;
import br.com.sea.desafio.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cria os dois usuários exigidos pelo desafio na primeira inicialização:
 * admin (permissão total) e user (somente visualização).
 */
@Component
public class UsuarioSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(UsuarioSeeder.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String senhaAdmin;
    private final String senhaUser;

    public UsuarioSeeder(UsuarioRepository usuarioRepository,
                         PasswordEncoder passwordEncoder,
                         @Value("${app.seed.senha-admin}") String senhaAdmin,
                         @Value("${app.seed.senha-user}") String senhaUser) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.senhaAdmin = senhaAdmin;
        this.senhaUser = senhaUser;
    }

    @Override
    public void run(String... args) {
        criarSeNaoExistir("admin", senhaAdmin, Role.ADMIN);
        criarSeNaoExistir("user", senhaUser, Role.USER);
    }

    private void criarSeNaoExistir(String username, String senha, Role role) {
        if (!usuarioRepository.findByUsername(username).isPresent()) {
            usuarioRepository.save(new Usuario(username, passwordEncoder.encode(senha), role));
            log.info("Usuário '{}' ({}) criado.", username, role);
        }
    }
}
