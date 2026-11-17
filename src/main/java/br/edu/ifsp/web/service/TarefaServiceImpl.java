package br.edu.ifsp.web.service;

import br.edu.ifsp.web.dao.TarefaDAO;
import br.edu.ifsp.web.model.Tarefa;
import br.edu.ifsp.web.model.Usuario;

import java.sql.SQLException;
import java.util.List;

public class TarefaServiceImpl implements TarefaService {

    @Override
    public List<Tarefa> listarTarefas(Usuario usuario) {
        exigir(usuario);
        return Transacao.executar(conn -> new TarefaDAO(conn).listarPorUsuario(usuario.getId()));
    }

    @Override
    public Tarefa buscarTarefa(Usuario usuario, long id) {
        exigir(usuario);
        return Transacao.executar(conn -> carregar(new TarefaDAO(conn), id, usuario));
    }

    @Override
    public Tarefa criarTarefa(Usuario usuario, String titulo, String descricao) {
        exigir(usuario);
        Tarefa tarefa;
        try {
            tarefa = Tarefa.nova(titulo, descricao, usuario.getId());
        } catch (IllegalArgumentException e) {
            throw new DadosInvalidosException(e.getMessage(), e);
        }
        Transacao.executar(conn -> { new TarefaDAO(conn).inserir(tarefa); return null; });
        return tarefa;
    }

    @Override
    public Tarefa atualizarTarefa(Usuario usuario, long id, String titulo, String descricao) {
        exigir(usuario);
        return Transacao.executar(conn -> {
            TarefaDAO dao = new TarefaDAO(conn);
            Tarefa tarefa = carregar(dao, id, usuario);
            try {
                tarefa.alterar(titulo, descricao);
            } catch (IllegalArgumentException e) {
                throw new DadosInvalidosException(e.getMessage(), e);
            }
            dao.atualizar(tarefa);
            return tarefa;
        });
    }

    @Override
    public void concluirTarefa(Usuario usuario, long id) {
        exigir(usuario);
        Transacao.executar(conn -> {
            TarefaDAO dao = new TarefaDAO(conn);
            Tarefa tarefa = carregar(dao, id, usuario);
            try {
                tarefa.concluir();
            } catch (IllegalStateException e) {
                throw new RegraDeDominioException(e.getMessage());
            }
            dao.atualizar(tarefa);
            return null;
        });
    }

    @Override
    public void excluirTarefa(Usuario usuario, long id) {
        exigir(usuario);
        Transacao.executar(conn -> {
            if (!new TarefaDAO(conn).excluir(id, usuario.getId())) {
                throw naoEncontrada(id);
            }
            return null;
        });
    }

    /** Tarefa de outro usuário é tratada como inexistente: o DAO filtra pelo dono. */
    private Tarefa carregar(TarefaDAO dao, long id, Usuario usuario) throws SQLException {
        return dao.buscar(id, usuario.getId()).orElseThrow(() -> naoEncontrada(id));
    }

    private RecursoNaoEncontradoException naoEncontrada(long id) {
        return new RecursoNaoEncontradoException("Tarefa " + id + " não encontrada");
    }

    private void exigir(Usuario usuario) {
        if (usuario == null || usuario.getId() == null) {
            throw new IllegalArgumentException("Usuário obrigatório");
        }
    }
}
