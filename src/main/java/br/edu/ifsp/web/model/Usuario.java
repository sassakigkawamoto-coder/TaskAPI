package br.edu.ifsp.web.model;

import java.io.Serializable;

public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String nome;
    private final String email;
    // transient: nunca entra em JSON (Gson) nem em sessão serializada
    private final transient String senhaHash;

    public Usuario(Long id, String nome, String email, String senhaHash) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenhaHash() { return senhaHash; }

    /** Cópia sem credencial: é o que sai da autenticação e vai para a sessão. */
    public Usuario semCredencial() {
        return new Usuario(id, nome, email, null);
    }
}
