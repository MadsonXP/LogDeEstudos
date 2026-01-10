package com.madson.logdeestudos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((requests) -> requests
                // Páginas Públicas (Login, Registro, CSS, H2-Console)
                .requestMatchers("/login", "/registrar", "/salvar-usuario", "/verificar", "/css/**", "/js/**", "/h2-console/**").permitAll()
                // Todo o resto exige estar logado
                .anyRequest().authenticated()
            )
            .formLogin((form) -> form
                .loginPage("/login") // Nossa página HTML customizada
                .usernameParameter("email") // O campo no form se chamará 'email'
                .defaultSuccessUrl("/", true) // Se logar, vai pro Dashboard
                .permitAll()
            )
            .logout((logout) -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            // Configuração necessária apenas para acessar o banco H2 sem erros de frame
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
            .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Criptografia forte para as senhas
    }
}