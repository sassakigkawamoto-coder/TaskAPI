package br.edu.ifsp.web.service;

import br.edu.ifsp.web.dao.UsuarioDAO;
import br.edu.ifsp.web.model.Usuario;

public class AutenticacaoServiceImpl implements AutenticacaoService {

    @Override
    public Usuario autenticar(String email, String senha) {
        if (email == null || email.isBlank() || senha == null || senha.isEmpty()) {
            throw new CredenciaisInvalidasException();
        }
        Usuario usuario = Transacao.executar(
                conn -> new UsuarioDAO(conn).buscarPorEmail(email.trim().toLowerCase()).orElse(null));

        // mesma exceção (e mesma mensagem) para e-mail inexistente e senha errada
        if (usuario == null || !SenhaHasher.conferir(senha, usuario.getSenhaHash())) {
            throw new CredenciaisInvalidasException();
        }
        return usuario.semCredencial();
    }
}
