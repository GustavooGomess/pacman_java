package jogo.mapa;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * Encapsula a matriz bidimensional do cenário, gerenciando paredes, pastilhas e power-ups.
 */
public class Labirinto {
    public static final int TAMANHO_CELULA = 28;
    public static final int LINHAS = 22;
    public static final int COLUNAS = 19;

    public static final int LINHA_INICIAL_PACMAN = 16;
    public static final int COLUNA_INICIAL_PACMAN = 9;

    /**
     * Mapa clássico inspirado no arcade Pac-Man:
     * '#' = Parede
     * '.' = Pastilha comum (10 pts)
     * 'o' = Power-Up / Pastilha energética (50 pts)
     * ' ' = Caminho vazio (túneis e casa dos fantasmas)
     */
    private static final String[] LAYOUT_PADRAO = {
        "###################",
        "#o.......#.......o#",
        "#.##.###.#.###.##.#",
        "#.##.###.#.###.##.#",
        "#.................#",
        "#.##.#.#####.#.##.#",
        "#....#...#...#....#",
        "####.### # ###.####",
        "   #.#       #.#   ",
        "####.# ## ## #.####",
        "    .  #   #  .    ",
        "####.# ##### #.####",
        "   #.#       #.#   ",
        "####.# ##### #.####",
        "#........#........#",
        "#.##.###.#.###.##.#",
        "#o.#..... .....#.o#",
        "##.#.#.#####.#.#.##",
        "#....#...#...#....#",
        "#.######.#.######.#",
        "#.................#",
        "###################"
    };

    private final TipoCelula[][] grade;
    private int pastilhasRestantes;
    private int contadorAnimacao;

    public Labirinto() {
        this.grade = new TipoCelula[LINHAS][COLUNAS];
        carregarLayoutPadrao();
    }

    /**
     * Carrega o layout clássico do labirinto na matriz 2D.
     */
    public final void carregarLayoutPadrao() {
        pastilhasRestantes = 0;

        for (int linha = 0; linha < LINHAS; linha++) {
            String linhaMapa = LAYOUT_PADRAO[linha];
            for (int coluna = 0; coluna < COLUNAS; coluna++) {
                char simbolo = linhaMapa.charAt(coluna);
                switch (simbolo) {
                    case '#' -> grade[linha][coluna] = TipoCelula.PAREDE;
                    case '.' -> {
                        grade[linha][coluna] = TipoCelula.PASTILHA;
                        pastilhasRestantes++;
                    }
                    case 'o' -> {
                        grade[linha][coluna] = TipoCelula.POWER_UP;
                        pastilhasRestantes++;
                    }
                    default -> grade[linha][coluna] = TipoCelula.VAZIO;
                }
            }
        }
    }

    public boolean posicaoValida(int linha, int coluna) {
        return linha >= 0 && linha < LINHAS && coluna >= 0 && coluna < COLUNAS;
    }

    public boolean ehParede(int linha, int coluna) {
        if (linha < 0 || linha >= LINHAS) {
            return true;
        }
        // Permite atravessar o túnel lateral horizontalmente
        int colunaNormalizada = ((coluna % COLUNAS) + COLUNAS) % COLUNAS;
        return grade[linha][colunaNormalizada] == TipoCelula.PAREDE;
    }

    /**
     * Verifica e consome o item na célula informada, retornando o TipoCelula coletado.
     */
    public TipoCelula coletarItem(int linha, int coluna) {
        if (!posicaoValida(linha, coluna)) {
            return TipoCelula.VAZIO;
        }

        TipoCelula celula = grade[linha][coluna];
        if (celula == TipoCelula.PASTILHA || celula == TipoCelula.POWER_UP) {
            grade[linha][coluna] = TipoCelula.VAZIO;
            pastilhasRestantes--;
            return celula;
        }
        return TipoCelula.VAZIO;
    }

    public int getPastilhasRestantes() {
        return pastilhasRestantes;
    }

    public int getLarguraPixels() {
        return COLUNAS * TAMANHO_CELULA;
    }

    public int getAlturaPixels() {
        return LINHAS * TAMANHO_CELULA;
    }

    public void desenhar(Graphics g) {
        contadorAnimacao++;
        Graphics2D g2d = (Graphics2D) g;

        for (int linha = 0; linha < LINHAS; linha++) {
            for (int coluna = 0; coluna < COLUNAS; coluna++) {
                int x = coluna * TAMANHO_CELULA;
                int y = linha * TAMANHO_CELULA;

                TipoCelula tipo = grade[linha][coluna];
                if (tipo == TipoCelula.PAREDE) {
                    g2d.setColor(new Color(18, 18, 140));
                    g2d.fillRoundRect(x + 1, y + 1, TAMANHO_CELULA - 2, TAMANHO_CELULA - 2, 8, 8);
                    g2d.setColor(new Color(55, 85, 255));
                    g2d.drawRoundRect(x + 2, y + 2, TAMANHO_CELULA - 5, TAMANHO_CELULA - 5, 6, 6);
                } else if (tipo == TipoCelula.PASTILHA) {
                    g2d.setColor(new Color(255, 195, 160));
                    int tamanhoPastilha = 6;
                    int offset = (TAMANHO_CELULA - tamanhoPastilha) / 2;
                    g2d.fillOval(x + offset, y + offset, tamanhoPastilha, tamanhoPastilha);
                } else if (tipo == TipoCelula.POWER_UP) {
                    // Faz o Power-Up piscar suavemente como no arcade
                    if ((contadorAnimacao / 12) % 2 == 0) {
                        g2d.setColor(new Color(255, 220, 180));
                        int tamanhoPowerUp = 14;
                        int offset = (TAMANHO_CELULA - tamanhoPowerUp) / 2;
                        g2d.fillOval(x + offset, y + offset, tamanhoPowerUp, tamanhoPowerUp);
                    }
                }
            }
        }
    }
}
