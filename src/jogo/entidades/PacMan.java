package jogo.entidades;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import jogo.mapa.Labirinto;

/**
 * Representa o personagem controlado pelo jogador.
 * Aplica Herança e Polimorfismo ao especializar a classe Entidade.
 */
public class PacMan extends Entidade {
    private static final int QUADROS_TOTAL_MORTE = 85;

    private Direcao direcaoAtual;
    private Direcao proximaDirecao;
    private int anguloBoca;
    private boolean abrindoBoca;

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
        this.proximaDirecao = Direcao.PARADO;
        this.anguloBoca = 35;
        this.abrindoBoca = false;
        this.morrendo = false;
        this.quadrosMorte = 0;
    }

    public void reposicionar(int linhaInicial, int colunaInicial) {
        setX(colunaInicial * Labirinto.TAMANHO_CELULA);
        setY(linhaInicial * Labirinto.TAMANHO_CELULA);
        this.direcaoAtual = Direcao.PARADO;
        this.proximaDirecao = Direcao.PARADO;
        this.anguloBoca = 35;
        this.abrindoBoca = false;
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

        // Permite inverter a direção imediatamente ou mudar de direção ao alinhar na célula
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
            setX(getX() + direcaoAtual.getDx() * getVelocidade());
            setY(getY() + direcaoAtual.getDy() * getVelocidade());
            verificarTunelHorizontal();
            animarBoca();
        }
    }

    private boolean ehDirecaoOposta(Direcao d1, Direcao d2) {
        return (d1.getDx() + d2.getDx() == 0) && (d1.getDy() + d2.getDy() == 0) && d1 != Direcao.PARADO;
    }

    private void animarBoca() {
        if (abrindoBoca) {
            anguloBoca += 5;
            if (anguloBoca >= 42) {
                abrindoBoca = false;
            }
        } else {
            anguloBoca -= 5;
            if (anguloBoca <= 4) {
                abrindoBoca = true;
            }
        }
    }

    @Override
    public void desenhar(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        int margem = 2;
        int diametro = getTamanho() - margem * 2;
        int xDesenho = getX() + margem;
        int yDesenho = getY() + margem;

        if (morrendo) {
            desenharAnimacaoMorte(g2d, xDesenho, yDesenho, diametro);
            return;
        }

        g2d.setColor(Color.YELLOW);
        int anguloInicial = direcaoAtual.getAnguloBase() + anguloBoca;
        int arco = 360 - (anguloBoca * 2);
        g2d.fillArc(xDesenho, yDesenho, diametro, diametro, anguloInicial, arco);
    }

    private void desenharAnimacaoMorte(Graphics2D g2d, int x, int y, int diametro) {
        g2d.setColor(Color.YELLOW);

        if (quadrosMorte < 12) {
            // Breve pausa dramática no momento da colisão
            g2d.fillArc(x, y, diametro, diametro, 130, 280);
        } else if (quadrosMorte < 65) {
            // A boca abre até 360 graus encolhendo o Pac-Man para baixo (clássico do arcade)
            double progresso = (double) (quadrosMorte - 12) / 53.0;
            int meiaAbertura = (int) (15 + progresso * 165);
            int anguloInicial = 90 + meiaAbertura;
            int arco = Math.max(0, 360 - (meiaAbertura * 2));
            g2d.fillArc(x, y, diametro, diametro, anguloInicial, arco);
        } else if (quadrosMorte < 80) {
            // Pequena explosão estelar ("pop" final da morte)
            int centroX = x + diametro / 2;
            int centroY = y + diametro / 2;
            double progressoExplosao = (double) (quadrosMorte - 65) / 15.0;
            int raioInterno = (int) (3 + progressoExplosao * 6);
            int raioExterno = (int) (7 + progressoExplosao * 9);

            g2d.setColor(new Color(255, 220, 60));
            for (int i = 0; i < 8; i++) {
                double angulo = i * (Math.PI / 4.0);
                int x1 = centroX + (int) (Math.cos(angulo) * raioInterno);
                int y1 = centroY + (int) (Math.sin(angulo) * raioInterno);
                int x2 = centroX + (int) (Math.cos(angulo) * raioExterno);
                int y2 = centroY + (int) (Math.sin(angulo) * raioExterno);
                g2d.drawLine(x1, y1, x2, y2);
            }
        }
    }
}
