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
                // 1. LIBERA O ACESSO PÚBLICO PARA ESSAS ROTAS
                .requestMatchers("/login", "/registrar", "/salvar-usuario", "/verificar", "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                // 2. TODAS AS OUTRAS EXIGEM LOGIN
                .anyRequest().authenticated()
            )
            .formLogin((form) -> form
                .loginPage("/login") // Diz qual é a sua página HTML de login
                .defaultSuccessUrl("/historico", true) // Redireciona para cá ao logar
                .permitAll()
            )
            .logout((logout) -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }

    // Bean para criptografar as senhas (essencial para o login funcionar)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}