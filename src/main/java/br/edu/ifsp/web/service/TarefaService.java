package br.edu.ifsp.web.service;

import br.edu.ifsp.web.model.Tarefa;
import br.edu.ifsp.web.model.Usuario;

import java.util.List;

public interface TarefaService {
    List<Tarefa> listarTarefas(Usuario usuario);
    Tarefa buscarTarefa(Usuario usuario, long id);
    Tarefa criarTarefa(Usuario usuario, String titulo, String descricao);
    Tarefa atualizarTarefa(Usuario usuario, long id, String titulo, String descricao);
    void concluirTarefa(Usuario usuario, long id);
    void excluirTarefa(Usuario usuario, long id);
}
