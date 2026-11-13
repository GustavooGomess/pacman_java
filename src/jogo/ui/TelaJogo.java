package jogo.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JPanel;
import javax.swing.Timer;
import jogo.core.GerenciadorJogo;
import jogo.entidades.Direcao;

/**
 * Painel gráfico responsável pelo Game Loop e pela estética Retro Pixel-Art moderna.
 */
public class TelaJogo extends JPanel implements ActionListener {
    private static final int ALTURA_PAINEL_SUPERIOR = 50;
    private static final int INTERVALO_TIMER_MS = 16; // ~60 FPS

    private static final String[] ICONE_VIDA_PIXEL = {
        "..#####..",
        ".#######.",
        "#####....",
        "###......",
        "#####....",
        ".#######.",
        "..#####.."
    };

    private final GerenciadorJogo gerenciador;
    private final Timer timer;

    public TelaJogo() {
        this.gerenciador = new GerenciadorJogo();

        int largura = gerenciador.getLabirinto().getLarguraPixels();
        int altura = gerenciador.getLabirinto().getAlturaPixels() + ALTURA_PAINEL_SUPERIOR;

        setPreferredSize(new Dimension(largura, altura));
        setBackground(new Color(4, 4, 12));
        setFocusable(true);

        configurarTeclado();

        this.timer = new Timer(INTERVALO_TIMER_MS, this);
        this.timer.start();
    }

    private void configurarTeclado() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP, KeyEvent.VK_W -> gerenciador.alterarDirecaoPacMan(Direcao.CIMA);
                    case KeyEvent.VK_DOWN, KeyEvent.VK_S -> gerenciador.alterarDirecaoPacMan(Direcao.BAIXO);
                    case KeyEvent.VK_LEFT, KeyEvent.VK_A -> gerenciador.alterarDirecaoPacMan(Direcao.ESQUERDA);
                    case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> gerenciador.alterarDirecaoPacMan(Direcao.DIREITA);
                    case KeyEvent.VK_R -> gerenciador.reiniciar();
                }
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        gerenciador.atualizar();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();
        // Desativa o suavizado borrado ("massinha") para preservar os pixels nítidos
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        desenharCabecalhoArcade(g2d);

        Graphics2D gMapa = (Graphics2D) g2d.create();
        gMapa.translate(0, ALTURA_PAINEL_SUPERIOR);
        gerenciador.desenhar(gMapa);
        gMapa.dispose();

        desenharScanlinesRetro(g2d);

        g2d.dispose();
        Toolkit.getDefaultToolkit().sync();
    }

    private void desenharCabecalhoArcade(Graphics2D g2d) {
        int larguraTela = gerenciador.getLabirinto().getLarguraPixels();

        // Linha divisória azul neon sob o placar
        g2d.setColor(new Color(33, 33, 255));
        g2d.fillRect(0, ALTURA_PAINEL_SUPERIOR - 3, larguraTela, 2);

        g2d.setFont(new Font("Monospaced", Font.BOLD, 14));
        g2d.setColor(new Color(255, 80, 80));
        g2d.drawString("1UP", 18, 17);
        g2d.drawString("HIGH SCORE", 185, 17);

        g2d.setFont(new Font("Monospaced", Font.BOLD, 17));
        g2d.setColor(new Color(255, 240, 200));
        g2d.drawString(String.format("%06d", gerenciador.getPontuacao()), 14, 37);
        g2d.drawString(String.format("%06d", gerenciador.getRecorde()), 200, 37);

        // Ícones de vidas em Pixel-Art no canto superior direito
        int xBaseVidas = larguraTela - 102;
        for (int i = 0; i < gerenciador.getVidas(); i++) {
            desenharMiniPacManPixel(g2d, xBaseVidas + (i * 30), 18);
        }

        // Banner central de Fim de Jogo ou Vitória estilo Fliperama
        if (gerenciador.isGameOver()) {
            desenharBannerRetro(g2d, "GAME  OVER", "PRESSIONE [R] PARA REINICIAR", new Color(255, 50, 50));
        } else if (gerenciador.todasPastilhasColetadas()) {
            desenharBannerRetro(g2d, "STAGE CLEAR!", "PRESSIONE [R] PARA JOGAR NOVAMENTE", new Color(0, 230, 118));
        }
    }

    private void desenharMiniPacManPixel(Graphics2D g2d, int x, int y) {
        int p = 2;
        g2d.setColor(new Color(255, 238, 0));
        for (int r = 0; r < ICONE_VIDA_PIXEL.length; r++) {
            String linha = ICONE_VIDA_PIXEL[r];
            for (int c = 0; c < linha.length(); c++) {
                if (linha.charAt(c) == '#') {
                    g2d.fillRect(x + c * p, y + r * p, p, p);
                }
            }
        }
    }

    private void desenharBannerRetro(Graphics2D g2d, String titulo, String subtitulo, Color corPrincipal) {
        int largura = gerenciador.getLabirinto().getLarguraPixels();
        int cx = largura / 2 - 145;
        int cy = ALTURA_PAINEL_SUPERIOR + 11 * 28 - 32;

        g2d.setColor(new Color(4, 4, 16, 235));
        g2d.fillRect(cx, cy, 290, 68);
        g2d.setColor(corPrincipal);
        g2d.drawRect(cx, cy, 290, 68);
        g2d.drawRect(cx + 2, cy + 2, 286, 64);

        g2d.setFont(new Font("Monospaced", Font.BOLD, 20));
        g2d.drawString(titulo, cx + 82, cy + 30);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 12));
        g2d.drawString(subtitulo, cx + 24, cy + 52);
    }

    /**
     * Efeito sutil de scanlines de monitor de fliperama (CRT moderno).
     */
    private void desenharScanlinesRetro(Graphics2D g2d) {
        int largura = getWidth();
        int altura = getHeight();
        g2d.setColor(new Color(0, 0, 0, 38));
        for (int y = 0; y < altura; y += 4) {
            g2d.fillRect(0, y, largura, 1);
        }
    }
}
