package jogo.mapa;

/**
 * Representa os possíveis conteúdos de cada célula na matriz do labirinto.
 */
public enum TipoCelula {
    VAZIO(0),
    PAREDE(0),
    PASTILHA(10),
    POWER_UP(50);

    private final int pontos;

    TipoCelula(int pontos) {
        this.pontos = pontos;
    }

    public int getPontos() {
        return pontos;
    }
}

