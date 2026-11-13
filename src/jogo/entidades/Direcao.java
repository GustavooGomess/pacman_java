package jogo.entidades;

/**
 * Direções de movimento possíveis para as entidades do jogo.
 */
public enum Direcao {
    PARADO(0, 0, 0),
    DIREITA(1, 0, 0),
    CIMA(0, -1, 90),
    ESQUERDA(-1, 0, 180),
    BAIXO(0, 1, 270);

    private final int dx;
    private final int dy;
    private final int anguloBase;

    Direcao(int dx, int dy, int anguloBase) {
        this.dx = dx;
        this.dy = dy;
        this.anguloBase = anguloBase;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }

    public int getAnguloBase() {
        return anguloBase;
    }
}

