package br.edu.ifsp.web.dao;

import br.edu.ifsp.web.model.Tarefa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Única camada com SQL. Toda consulta é filtrada pelo dono da tarefa. */
public class TarefaDAO {
    private static final String COLUNAS = "id, titulo, descricao, concluida, usuario_id";
    private final Connection conn;

    public TarefaDAO(Connection conn) { this.conn = conn; }

    public List<Tarefa> listarPorUsuario(long usuarioId) throws SQLException {
        String sql = "SELECT " + COLUNAS + " FROM tarefas WHERE usuario_id = ? ORDER BY id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Tarefa> lista = new ArrayList<>();
                while (rs.next()) lista.add(mapear(rs));
                return lista;
            }
        }
    }

    public Optional<Tarefa> buscar(long id, long usuarioId) throws SQLException {
        String sql = "SELECT " + COLUNAS + " FROM tarefas WHERE id = ? AND usuario_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public void inserir(Tarefa t) throws SQLException {
        String sql = "INSERT INTO tarefas (titulo, descricao, concluida, usuario_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getTitulo());
            ps.setString(2, t.getDescricao());
            ps.setBoolean(3, t.isConcluida());
            ps.setLong(4, t.getUsuarioId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) t.setId(keys.getLong(1));
            }
        }
    }

    public boolean atualizar(Tarefa t) throws SQLException {
        String sql = "UPDATE tarefas SET titulo = ?, descricao = ?, concluida = ? WHERE id = ? AND usuario_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getTitulo());
            ps.setString(2, t.getDescricao());
            ps.setBoolean(3, t.isConcluida());
            ps.setLong(4, t.getId());
            ps.setLong(5, t.getUsuarioId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean excluir(long id, long usuarioId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM tarefas WHERE id = ? AND usuario_id = ?")) {
            ps.setLong(1, id);
            ps.setLong(2, usuarioId);
            return ps.executeUpdate() > 0;
        }
    }

    private Tarefa mapear(ResultSet rs) throws SQLException {
        return new Tarefa(rs.getLong("id"), rs.getString("titulo"), rs.getString("descricao"),
                rs.getBoolean("concluida"), rs.getLong("usuario_id"));
    }
}
