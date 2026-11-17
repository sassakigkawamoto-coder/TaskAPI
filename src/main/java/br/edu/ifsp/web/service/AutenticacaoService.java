package br.edu.ifsp.web.service;

import br.edu.ifsp.web.model.Usuario;

public interface AutenticacaoService {
    /** Devolve o usuário SEM credencial, ou lança CredenciaisInvalidasException. */
    Usuario autenticar(String email, String senha);
}
