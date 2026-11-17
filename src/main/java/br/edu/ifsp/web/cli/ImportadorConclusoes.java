package br.edu.ifsp.web.cli;

import br.edu.ifsp.web.model.Usuario;
import br.edu.ifsp.web.service.AutenticacaoService;
import br.edu.ifsp.web.service.AutenticacaoServiceImpl;
import br.edu.ifsp.web.service.CredenciaisInvalidasException;
import br.edu.ifsp.web.service.RecursoNaoEncontradoException;
import br.edu.ifsp.web.service.RegraDeDominioException;
import br.edu.ifsp.web.service.TarefaService;
import br.edu.ifsp.web.service.TarefaServiceImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Uso: ImportadorConclusoes <arquivo-de-ids> <email> <senha> */
public class ImportadorConclusoes {

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Uso: ImportadorConclusoes <arquivo-de-ids> <email> <senha>");
            System.exit(2);
        }
        // Único ponto do importador que conhece as implementações
        AutenticacaoService autenticacao = new AutenticacaoServiceImpl();
        TarefaService tarefas = new TarefaServiceImpl();

        Usuario usuario;
        try {
            usuario = autenticacao.autenticar(args[1], args[2]);
        } catch (CredenciaisInvalidasException e) {
            System.err.println("Erro: " + e.getMessage());
            System.exit(1);
            return;
        } catch (RuntimeException e) {
            System.err.println("Erro ao autenticar: " + e.getMessage());
            System.exit(3);
            return;
        }

        List<String> linhas;
        try {
            linhas = Files.readAllLines(Path.of(args[0]));
        } catch (IOException e) {
            System.err.println("Não foi possível ler o arquivo: " + e.getMessage());
            System.exit(2);
            return;
        }

        int numero = 0;
        for (String linha : linhas) {
            numero++;
            String texto = linha.trim();
            if (texto.isEmpty()) continue;
            System.out.println("linha " + numero + " (id " + texto + "): " + processar(tarefas, usuario, texto));
        }
    }

    /** A regra de conclusão é a do serviço; aqui só se traduz o resultado em texto. */
    private static String processar(TarefaService tarefas, Usuario usuario, String texto) {
        try {
            tarefas.concluirTarefa(usuario, Long.parseLong(texto));
            return "concluída";
        } catch (NumberFormatException e) {
            return "id inválido";
        } catch (RecursoNaoEncontradoException e) {
            return "não encontrada";
        } catch (RegraDeDominioException e) {
            return "já estava concluída";
        } catch (RuntimeException e) {
            return "erro: " + e.getMessage();
        }
    }
}
