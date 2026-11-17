package br.edu.ifsp.web.service;

public class CredenciaisInvalidasException extends AplicacaoException {
    public CredenciaisInvalidasException() { super("E-mail ou senha inválidos"); }
}
