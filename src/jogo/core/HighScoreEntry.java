package jogo.core;

import java.io.Serializable;

/**
 * Representa uma entrada individual na tabela de recordes do Arcade.
 */
public class HighScoreEntry implements Serializable, Comparable<HighScoreEntry> {
    private static final long serialVersionUID = 1L;
    
    private final String nome;
    private final int pontuacao;

    public HighScoreEntry(String nome, int pontuacao) {
        this.nome = nome;
        this.pontuacao = pontuacao;
    }

    public String getNome() {
        return nome;
    }

    public int getPontuacao() {
        return pontuacao;
    }

    @Override
    public int compareTo(HighScoreEntry outro) {
        // Ordenação decrescente: maior pontuação primeiro
        return Integer.compare(outro.pontuacao, this.pontuacao);
    }
}
