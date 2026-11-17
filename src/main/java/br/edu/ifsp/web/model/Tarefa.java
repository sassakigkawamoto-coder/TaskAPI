package br.edu.ifsp.web.model;

public class Tarefa {
    private Long id;
    private String titulo;
    private String descricao;
    private boolean concluida;
    private final Long usuarioId;

    public Tarefa(Long id, String titulo, String descricao, boolean concluida, Long usuarioId) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.descricao = descricao;
        this.concluida = concluida;
        setTitulo(titulo);
    }

    /** Tarefa nova: sempre nasce pendente. */
    public static Tarefa nova(String titulo, String descricao, Long usuarioId) {
        return new Tarefa(null, titulo, descricao, false, usuarioId);
    }

    /** Regra: só se conclui uma vez. Não existe setConcluida. */
    public void concluir() {
        if (concluida) {
            throw new IllegalStateException("A tarefa já está concluída");
        }
        concluida = true;
    }

    public void alterar(String titulo, String descricao) {
        setTitulo(titulo);
        this.descricao = descricao;
    }

    private void setTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("O título é obrigatório");
        }
        this.titulo = titulo.trim();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public boolean isConcluida() { return concluida; }
    public Long getUsuarioId() { return usuarioId; }
}
