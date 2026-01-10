package com.madson.logdeestudos.service;

import com.madson.logdeestudos.model.Usuario;
import com.madson.logdeestudos.repository.UsuarioRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

@Service
public class UsuarioService implements UserDetailsService {

    @Autowired private UsuarioRepository repo;
    @Autowired private PasswordEncoder encoder;
    @Autowired private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String emailRemetente;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return repo.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }

    public void registrar(Usuario usuario, String siteURL) throws UnsupportedEncodingException, MessagingException {
        usuario.setSenha(encoder.encode(usuario.getSenha()));
        String codigo = UUID.randomUUID().toString();
        usuario.setCodigoVerificacao(codigo);
        usuario.setAtivo(false);
        repo.save(usuario);
        enviarEmailVerificacao(usuario, siteURL);
    }

    // --- ATUALIZAÇÃO SEGURA DE PERFIL ---
    public void atualizarPerfil(Usuario usuarioLogado, String novoNome, String senhaAtual, String novaSenha) throws Exception {
        // 1. Atualiza o nome sempre
        usuarioLogado.setNome(novoNome);

        // 2. Se a pessoa tentou digitar uma NOVA SENHA...
        if (novaSenha != null && !novaSenha.isEmpty()) {
            
            // ... Verificamos se a SENHA ATUAL está correta
            if (!encoder.matches(senhaAtual, usuarioLogado.getSenha())) {
                throw new Exception("A senha atual está incorreta. Não foi possível alterar.");
            }
            
            // Se estiver certa, criptografa e salva a nova
            usuarioLogado.setSenha(encoder.encode(novaSenha));
        }

        repo.save(usuarioLogado);
    }
    // ------------------------------------

    private void enviarEmailVerificacao(Usuario usuario, String siteURL) throws MessagingException, UnsupportedEncodingException {
        String toAddress = usuario.getEmail();
        String fromAddress = emailRemetente;
        String senderName = "StudyLog App";
        String subject = "Por favor, ative sua conta no StudyLog";
        String verifyURL = siteURL + "/verificar?codigo=" + usuario.getCodigoVerificacao();

        String content = "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ddd; border-radius: 10px;'>"
                + "<h2 style='color: #0d6efd;'>Bem-vindo ao StudyLog! 🚀</h2>"
                + "<p>Olá <b>" + usuario.getNome() + "</b>,</p>"
                + "<p>Falta pouco para você começar a organizar seus estudos.</p>"
                + "<p>Clique no botão abaixo para ativar sua conta:</p>"
                + "<a href=\"" + verifyURL + "\" style='background-color: #198754; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; font-weight: bold;'>ATIVAR MINHA CONTA</a>"
                + "<br><br><p>Se você não criou esta conta, ignore este e-mail.</p>"
                + "</div>";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);
        helper.setText(content, true);
        mailSender.send(message);
    }

    public boolean verificar(String codigo) {
        Usuario user = repo.findByCodigoVerificacao(codigo);
        if (user == null || user.isAtivo()) {
            return false;
        } else {
            user.setCodigoVerificacao(null);
            user.setAtivo(true);
            repo.save(user);
            return true;
        }
    }
}