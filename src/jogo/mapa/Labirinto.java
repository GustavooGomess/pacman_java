package jogo.mapa;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * Encapsula a matriz bidimensional do cenário com renderização estilo Arcade Retro Pixel-Art
 * (paredes conectadas continuamente, porta laser da jaula e pastilhas pixelizadas).
 */
public class Labirinto {
    public static final int TAMANHO_CELULA = 28;
    public static final int ESCALA_PIXEL = 2; // Cada "pixel" retrô equivale a um bloco 2x2 na tela (14x14 por célula)
    public static final int LINHAS = 22;
    public static final int COLUNAS = 19;

    public static final int LINHA_INICIAL_PACMAN = 16;
    public static final int COLUNA_INICIAL_PACMAN = 9;

    // Paleta de cores retrô-moderna inspirada no arcade clássico
    private static final Color COR_FUNDO_PAREDE = new Color(6, 8, 28);
    private static final Color COR_BORDA_EXTERNA = new Color(33, 33, 255);
    private static final Color COR_BORDA_BRILHO = new Color(75, 110, 255);
    private static final Color COR_PORTA_JAULA = new Color(255, 184, 255);
    private static final Color COR_PASTILHA = new Color(255, 184, 151);
    private static final Color COR_PASTILHA_CENTRO = new Color(255, 225, 200);

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
        int colunaNormalizada = Math.floorMod(coluna, COLUNAS);
        return grade[linha][colunaNormalizada] == TipoCelula.PAREDE;
    }

    private boolean ehParedeEstrita(int linha, int coluna) {
        if (linha < 0 || linha >= LINHAS || coluna < 0 || coluna >= COLUNAS) {
            return false;
        }
        return grade[linha][coluna] == TipoCelula.PAREDE;
    }

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
                    desenharParedeConectada(g2d, linha, coluna, x, y);
                } else if (tipo == TipoCelula.PASTILHA) {
                    desenharPastilhaPixel(g2d, x, y);
                } else if (tipo == TipoCelula.POWER_UP) {
                    if ((contadorAnimacao / 10) % 2 == 0) {
                        desenharPowerUpPixel(g2d, x, y);
                    }
                }
            }
        }

        // Desenha a porta rosa clássica da casa dos fantasmas (linha 9, coluna 9)
        desenharPortaJaula(g2d);
    }

    /**
     * Desenha as paredes conectadas continuamente (sem divisórias internas entre blocos adjacentes),
     * gerando o visual autêntico de labirinto de fliperama.
     */
    private void desenharParedeConectada(Graphics2D g2d, int linha, int col, int x, int y) {
        int t = TAMANHO_CELULA;
        boolean cima = ehParedeEstrita(linha - 1, col);
        boolean baixo = ehParedeEstrita(linha + 1, col);
        boolean esq = ehParedeEstrita(linha, col - 1);
        boolean dir = ehParedeEstrita(linha, col + 1);

        int m = 4; // recuo da linha de contorno em relação ao corredor
        int x1 = esq ? x : x + m;
        int y1 = cima ? y : y + m;
        int x2 = dir ? x + t : x + t - m;
        int y2 = baixo ? y + t : y + t - m;

        // Preenchimento escuro sutil para destacar os blocos de parede do corredor preto
        g2d.setColor(COR_FUNDO_PAREDE);
        g2d.fillRect(x1, y1, x2 - x1, y2 - y1);

        // Contorno neon duplo nas bordas que fazem fronteira com os corredores
        g2d.setStroke(new BasicStroke(2f));

        // Linha principal azul arcade
        g2d.setColor(COR_BORDA_EXTERNA);
        if (!cima) {
            g2d.drawLine(x1, y + m, x2 - 1, y + m);
        }
        if (!baixo) {
            g2d.drawLine(x1, y + t - m - 1, x2 - 1, y + t - m - 1);
        }
        if (!esq) {
            g2d.drawLine(x + m, y1, x + m, y2 - 1);
        }
        if (!dir) {
            g2d.drawLine(x + t - m - 1, y1, x + t - m - 1, y2 - 1);
        }

        // Segunda linha interna (efeito parede dupla retrô)
        int m2 = m + 3;
        g2d.setStroke(new BasicStroke(1f));
        g2d.setColor(COR_BORDA_BRILHO);
        int ix1 = esq ? x : x + m2;
        int iy1 = cima ? y : y + m2;
        int ix2 = dir ? x + t : x + t - m2;
        int iy2 = baixo ? y + t : y + t - m2;

        if (ix2 > ix1 && iy2 > iy1) {
            if (!cima) {
                g2d.drawLine(ix1, y + m2, ix2 - 1, y + m2);
            }
            if (!baixo) {
                g2d.drawLine(ix1, y + t - m2 - 1, ix2 - 1, y + t - m2 - 1);
            }
            if (!esq) {
                g2d.drawLine(x + m2, iy1, x + m2, iy2 - 1);
            }
            if (!dir) {
                g2d.drawLine(x + t - m2 - 1, iy1, x + t - m2 - 1, iy2 - 1);
            }
        }

        // Cantos internos (quando dois muros se encontram em L ao redor de uma quina)
        g2d.setColor(COR_BORDA_EXTERNA);
        if (cima && esq && !ehParedeEstrita(linha - 1, col - 1)) {
            g2d.fillRect(x, y, m + 1, m + 1);
        }
        if (cima && dir && !ehParedeEstrita(linha - 1, col + 1)) {
            g2d.fillRect(x + t - m - 1, y, m + 1, m + 1);
        }
        if (baixo && esq && !ehParedeEstrita(linha + 1, col - 1)) {
            g2d.fillRect(x, y + t - m - 1, m + 1, m + 1);
        }
        if (baixo && dir && !ehParedeEstrita(linha + 1, col + 1)) {
            g2d.fillRect(x + t - m - 1, y + t - m - 1, m + 1, m + 1);
        }
    }

    private void desenharPortaJaula(Graphics2D g2d) {
        int x = 9 * TAMANHO_CELULA;
        int y = 9 * TAMANHO_CELULA;
        g2d.setColor(COR_PORTA_JAULA);
        g2d.fillRect(x - 2, y + 11, TAMANHO_CELULA + 4, 4);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(x, y + 12, TAMANHO_CELULA, 2);
    }

    /**
     * Pastilha quadrada/pixelizada estilo 8-bit.
     */
    private void desenharPastilhaPixel(Graphics2D g2d, int x, int y) {
        int centro = TAMANHO_CELULA / 2;
        g2d.setColor(COR_PASTILHA);
        g2d.fillRect(x + centro - 3, y + centro - 3, 6, 6);
        g2d.setColor(COR_PASTILHA_CENTRO);
        g2d.fillRect(x + centro - 1, y + centro - 1, 2, 2);
    }

    /**
     * Power-Up desenhado como esfera pixelizada (octógono retrô em grade 2x2).
     */
    private void desenharPowerUpPixel(Graphics2D g2d, int x, int y) {
        int p = ESCALA_PIXEL;
        int ox = x + 3 * p;
        int oy = y + 3 * p;

        g2d.setColor(COR_PASTILHA);
        // Camadas horizontais formando um círculo pixel-art 8x8
        g2d.fillRect(ox + 2 * p, oy, 4 * p, p);
        g2d.fillRect(ox + p, oy + p, 6 * p, p);
        g2d.fillRect(ox, oy + 2 * p, 8 * p, 4 * p);
        g2d.fillRect(ox + p, oy + 6 * p, 6 * p, p);
        g2d.fillRect(ox + 2 * p, oy + 7 * p, 4 * p, p);

        // Brilho central pixelizado
        g2d.setColor(COR_PASTILHA_CENTRO);
        g2d.fillRect(ox + 2 * p, oy + 2 * p, 2 * p, 2 * p);
    }
}
