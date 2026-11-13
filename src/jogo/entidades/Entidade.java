package jogo.entidades;

import java.awt.Graphics;
import jogo.mapa.Labirinto;

/**
 * Classe abstrata que representa a base de qualquer personagem/entidade do jogo.
 * Aplica os conceitos de Abstração e Encapsulamento.
 */
public abstract class Entidade {
    private int x;
    private int y;
    private int velocidade;
    private int tamanho;
    private final Labirinto labirinto;

    public Entidade(int x, int y, int velocidade, int tamanho, Labirinto labirinto) {
        this.x = x;
        this.y = y;
        this.velocidade = velocidade;
        this.tamanho = tamanho;
        this.labirinto = labirinto;
    }

    public abstract void mover();

    public abstract void desenhar(Graphics g);

    public int getX() {
        return x;
    }

    protected void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    protected void setY(int y) {
        this.y = y;
    }

    public int getVelocidade() {
        return velocidade;
    }

    protected void setVelocidade(int velocidade) {
        this.velocidade = velocidade;
    }

    public int getTamanho() {
        return tamanho;
    }

    protected Labirinto getLabirinto() {
        return labirinto;
    }

    public int getLinhaCentro() {
        return Math.floorDiv(y + tamanho / 2, Labirinto.TAMANHO_CELULA);
    }

    public int getColunaCentro() {
        int col = Math.floorDiv(x + tamanho / 2, Labirinto.TAMANHO_CELULA);
        return Math.floorMod(col, Labirinto.COLUNAS);
    }

    protected boolean estaAlinhadoNaGrade() {
        return Math.floorMod(x, Labirinto.TAMANHO_CELULA) == 0
            && Math.floorMod(y, Labirinto.TAMANHO_CELULA) == 0;
    }

    protected boolean podeMoverPara(Direcao direcao) {
        if (direcao == Direcao.PARADO) {
            return false;
        }
        int linhaAtual = Math.floorDiv(y, Labirinto.TAMANHO_CELULA);
        int colunaAtual = Math.floorDiv(x, Labirinto.TAMANHO_CELULA);
        int proximaLinha = linhaAtual + direcao.getDy();
        int proximaColuna = colunaAtual + direcao.getDx();
        return !labirinto.ehParede(proximaLinha, proximaColuna);
    }

    /**
     * Teletransporta a entidade para o lado oposto caso atravesse o túnel lateral,
     * preservando o alinhamento exato com os múltiplos da grade.
     */
    protected void verificarTunelHorizontal() {
        int larguraMapa = labirinto.getLarguraPixels();
        if (x <= -tamanho) {
            x = larguraMapa - tamanho;
        } else if (x >= larguraMapa) {
            x = 0;
        }
    }
}
