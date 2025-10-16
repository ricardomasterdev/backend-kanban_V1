package com.seuprojeto.kanban.config;

import com.seuprojeto.kanban.repository.RoleRepository;
import com.seuprojeto.kanban.repository.UserRepository;
import com.seuprojeto.kanban.security.Role;
import com.seuprojeto.kanban.security.UserAccount;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitConfig {

    @Bean
    CommandLineRunner initUsers(RoleRepository roles, UserRepository users, PasswordEncoder encoder){
        return args -> {
            var adminRole = roles.findByName("ADMIN").orElseGet(() -> roles.save(new Role("ADMIN")));

            if (!users.existsByEmailIgnoreCase("admin@codex.local")) {
                UserAccount u = new UserAccount();
                u.setEmail("admin@codex.local");
                // SALVA BCRYPT (não {noop})
                u.setPassword(encoder.encode("admin"));
                u.getRoles().add(adminRole);
                users.save(u);
            }
        };
    }
}
