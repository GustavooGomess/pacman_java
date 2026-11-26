package jogo.entidades;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import jogo.mapa.Labirinto;

/**
 * Representa o personagem controlado pelo jogador em Pixel-Art autêntica (14x14).
 * Aplica Herança e Polimorfismo ao especializar a classe Entidade.
 */
public class PacMan extends Entidade {
    private static final int QUADROS_TOTAL_MORTE = 85;
    private static final Color COR_PACMAN = new Color(255, 238, 0);

    // Quadro 0: Boca fechada (14x14)
    private static final String[] SPRITE_FECHADO = {
        "....######....",
        "..##########..",
        ".############.",
        ".############.",
        "##############",
        "##############",
        "##############",
        "##############",
        "##############",
        "##############",
        ".############.",
        ".############.",
        "..##########..",
        "....######...."
    };

    // Quadro 1: Boca semiaberta para a direita (14x14)
    private static final String[] SPRITE_SEMI = {
        "....######....",
        "..##########..",
        ".############.",
        ".############.",
        "##########....",
        "#######.......",
        "#####.........",
        "#####.........",
        "#######.......",
        "##########....",
        ".############.",
        ".############.",
        "..##########..",
        "....######...."
    };

    // Quadro 2: Boca totalmente aberta para a direita (14x14)
    private static final String[] SPRITE_ABERTO = {
        "....######....",
        "..#########...",
        ".#########....",
        ".#######......",
        "######........",
        "####..........",
        "###...........",
        "###...........",
        "####..........",
        "######........",
        ".#######......",
        ".#########....",
        "..#########...",
        "....######...."
    };

    private Direcao direcaoAtual;
    private Direcao ultimaDirecaoOlhar;
    private Direcao proximaDirecao;
    private int contadorAnimacao;

    private boolean morrendo;
    private int quadrosMorte;

    public PacMan(int linhaInicial, int colunaInicial, Labirinto labirinto) {
        super(
            colunaInicial * Labirinto.TAMANHO_CELULA,
            linhaInicial * Labirinto.TAMANHO_CELULA,
            4,
            Labirinto.TAMANHO_CELULA,
            labirinto
        );
        this.direcaoAtual = Direcao.PARADO;
        this.ultimaDirecaoOlhar = Direcao.ESQUERDA;
        this.proximaDirecao = Direcao.PARADO;
        this.contadorAnimacao = 0;
        this.morrendo = false;
        this.quadrosMorte = 0;
    }

    public void reposicionar(int linhaInicial, int colunaInicial) {
        setX(colunaInicial * Labirinto.TAMANHO_CELULA);
        setY(linhaInicial * Labirinto.TAMANHO_CELULA);
        this.direcaoAtual = Direcao.PARADO;
        this.ultimaDirecaoOlhar = Direcao.ESQUERDA;
        this.proximaDirecao = Direcao.PARADO;
        this.contadorAnimacao = 0;
        this.morrendo = false;
        this.quadrosMorte = 0;
    }

    public void iniciarAnimacaoMorte() {
        this.morrendo = true;
        this.quadrosMorte = 0;
        this.direcaoAtual = Direcao.PARADO;
    }

    public boolean estaMorrendo() {
        return morrendo;
    }

    public boolean animacaoMorteConcluida() {
        return morrendo && quadrosMorte >= QUADROS_TOTAL_MORTE;
    }

    public Direcao getDirecaoAtual() {
        return direcaoAtual != Direcao.PARADO ? direcaoAtual : ultimaDirecaoOlhar;
    }

    public void setProximaDirecao(Direcao novaDirecao) {
        if (novaDirecao != null) {
            this.proximaDirecao = novaDirecao;
        }
    }

    @Override
    public void mover() {
        if (morrendo) {
            quadrosMorte++;
            return;
        }

        if (estaAlinhadoNaGrade()) {
            if (podeMoverPara(proximaDirecao)) {
                direcaoAtual = proximaDirecao;
            }
            if (!podeMoverPara(direcaoAtual)) {
                direcaoAtual = Direcao.PARADO;
            }
        } else if (ehDirecaoOposta(direcaoAtual, proximaDirecao)) {
            direcaoAtual = proximaDirecao;
        }

        if (direcaoAtual != Direcao.PARADO) {
            ultimaDirecaoOlhar = direcaoAtual;
            setX(getX() + direcaoAtual.getDx() * getVelocidade());
            setY(getY() + direcaoAtual.getDy() * getVelocidade());
            verificarTunelHorizontal();
            contadorAnimacao++;
        }
    }

    private boolean ehDirecaoOposta(Direcao d1, Direcao d2) {
        return (d1.getDx() + d2.getDx() == 0) && (d1.getDy() + d2.getDy() == 0) && d1 != Direcao.PARADO;
    }

    @Override
    public void desenhar(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        int p = Labirinto.ESCALA_PIXEL;

        if (morrendo) {
            desenharAnimacaoMortePixel(g2d, getX(), getY(), p);
            return;
        }

        String[] spriteAtual = selecionarSpriteBoca();
        Direcao dirOlhar = (direcaoAtual != Direcao.PARADO) ? direcaoAtual : ultimaDirecaoOlhar;

        g2d.setColor(COR_PACMAN);
        for (int r = 0; r < 14; r++) {
            for (int c = 0; c < 14; c++) {
                char pixel = obterPixelRotacionado(spriteAtual, r, c, dirOlhar);
                if (pixel == '#') {
                    g2d.fillRect(getX() + c * p, getY() + r * p, p, p);
                }
            }
        }
    }

    private String[] selecionarSpriteBoca() {
        if (direcaoAtual == Direcao.PARADO) {
            return SPRITE_SEMI;
        }
        int fase = (contadorAnimacao / 3) % 4;
        return switch (fase) {
            case 0 -> SPRITE_FECHADO;
            case 1, 3 -> SPRITE_SEMI;
            default -> SPRITE_ABERTO;
        };
    }

    private char obterPixelRotacionado(String[] sprite, int r, int c, Direcao direcao) {
        return switch (direcao) {
            case DIREITA, PARADO -> sprite[r].charAt(c);
            case ESQUERDA -> sprite[r].charAt(13 - c);
            case CIMA -> sprite[c].charAt(13 - r);
            case BAIXO -> sprite[c].charAt(r);
        };
    }

    /**
     * Animação de morte renderizada inteiramente em blocos de Pixel-Art (14x14).
     */
    private void desenharAnimacaoMortePixel(Graphics2D g2d, int baseX, int baseY, int p) {
        g2d.setColor(COR_PACMAN);

        if (quadrosMorte < 65) {
            double progresso = Math.max(0.0, (quadrosMorte - 10) / 55.0);
            double anguloAberturaRad = progresso * Math.PI; // abre de 0 até 180 graus para cada lado

            for (int r = 0; r < 14; r++) {
                for (int c = 0; c < 14; c++) {
                    if (SPRITE_FECHADO[r].charAt(c) == '#') {
                        double dx = c - 6.5;
                        double dy = 6.5 - r; // eixo Y apontando para cima
                        double anguloDoTopo = Math.atan2(Math.abs(dx), dy); // 0 no topo, PI na base
                        if (anguloDoTopo >= anguloAberturaRad) {
                            g2d.fillRect(baseX + c * p, baseY + r * p, p, p);
                        }
                    }
                }
            }
        } else if (quadrosMorte < 82) {
            // Explosão final ("pop") em pixels retrô
            int dist = 2 + (quadrosMorte - 65) / 3;
            int[][] direcoesEstrela = {
                {-1, 0}, {1, 0}, {0, -1}, {0, 1},
                {-1, -1}, {1, -1}, {-1, 1}, {1, 1}
            };
            g2d.setColor(new Color(255, 200, 50));
            for (int[] d : direcoesEstrela) {
                int px = 7 + d[0] * dist;
                int py = 7 + d[1] * dist;
                if (px >= 0 && px < 14 && py >= 0 && py < 14) {
                    g2d.fillRect(baseX + px * p, baseY + py * p, p, p);
                }
            }
        }
    }
}
