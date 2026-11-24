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

    public void alinharNaGrade() {
        this.x = Math.round((float) x / Labirinto.TAMANHO_CELULA) * Labirinto.TAMANHO_CELULA;
        this.y = Math.round((float) y / Labirinto.TAMANHO_CELULA) * Labirinto.TAMANHO_CELULA;
    }

    /**
     * Verifica se uma caixa delimitadora em (novoX, novoY) intersecta alguma parede.
     */
    public boolean colideComParede(int novoX, int novoY) {
        int margem = 2; // tolerância para permitir curvas sem engasgar
        int xEsq = novoX + margem;
        int xDir = novoX + tamanho - 1 - margem;
        int yTopo = novoY + margem;
        int yBase = novoY + tamanho - 1 - margem;

        int lTopo = Math.floorDiv(yTopo, Labirinto.TAMANHO_CELULA);
        int lBase = Math.floorDiv(yBase, Labirinto.TAMANHO_CELULA);
        int cEsq = Math.floorDiv(xEsq, Labirinto.TAMANHO_CELULA);
        int cDir = Math.floorDiv(xDir, Labirinto.TAMANHO_CELULA);

        // Se estiver no túnel (linha 10) e transitando pelas bordas externas, não é colisão com parede
        if (lTopo == 10 && lBase == 10) {
            if (cEsq < 0 || cDir >= Labirinto.COLUNAS) {
                return false;
            }
        }

        return labirinto.ehParede(lTopo, cEsq)
            || labirinto.ehParede(lTopo, cDir)
            || labirinto.ehParede(lBase, cEsq)
            || labirinto.ehParede(lBase, cDir);
    }

    /**
     * Teletransporta a entidade para o lado oposto caso atravesse o túnel lateral (linha 10),
     * ou restringe a entidade aos limites estritos do mapa em qualquer outra linha.
     */
    protected void verificarTunelHorizontal() {
        int linhaCentro = getLinhaCentro();
        int larguraMapa = labirinto.getLarguraPixels();
        int alturaMapa = labirinto.getAlturaPixels();

        if (linhaCentro == 10) {
            if (x <= -tamanho) {
                x = larguraMapa - tamanho;
            } else if (x >= larguraMapa) {
                x = 0;
            }
        } else {
            if (x < 0) {
                x = 0;
            } else if (x > larguraMapa - tamanho) {
                x = larguraMapa - tamanho;
            }
        }

        if (y < 0) {
            y = 0;
        } else if (y > alturaMapa - tamanho) {
            y = alturaMapa - tamanho;
        }
    }
}
