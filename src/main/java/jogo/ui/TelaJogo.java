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
 * Painel gráfico responsável pelo Game Loop, renderização (Swing/AWT) e captura de teclado.
 */
public class TelaJogo extends JPanel implements ActionListener {
    private static final int ALTURA_PAINEL_SUPERIOR = 44;
    private static final int INTERVALO_TIMER_MS = 16; // ~60 FPS

    private final GerenciadorJogo gerenciador;
    private final Timer timer;

    public TelaJogo() {
        this.gerenciador = new GerenciadorJogo();

        int largura = gerenciador.getLabirinto().getLarguraPixels();
        int altura = gerenciador.getLabirinto().getAlturaPixels() + ALTURA_PAINEL_SUPERIOR;

        setPreferredSize(new Dimension(largura, altura));
        setBackground(Color.BLACK);
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
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        desenharCabecalho(g2d);

        // Desloca o eixo Y para desenhar o labirinto abaixo do cabeçalho
        g2d.translate(0, ALTURA_PAINEL_SUPERIOR);
        gerenciador.desenhar(g2d);

        g2d.dispose();
        Toolkit.getDefaultToolkit().sync();
    }

    private void desenharCabecalho(Graphics2D g2d) {
        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 17));
        g2d.drawString("SCORE: " + String.format("%05d", gerenciador.getPontuacao()), 14, 28);

        if (gerenciador.isGameOver()) {
            g2d.setColor(new Color(255, 70, 70));
            g2d.setFont(new Font("Monospaced", Font.BOLD, 15));
            g2d.drawString("GAME OVER! [R] Reiniciar", 175, 28);
        } else if (gerenciador.todasPastilhasColetadas()) {
            g2d.setColor(Color.GREEN);
            g2d.setFont(new Font("Monospaced", Font.BOLD, 15));
            g2d.drawString("VITÓRIA! [R] Reiniciar", 185, 28);
        }

        // Desenha os ícones de vidas restantes no canto superior direito
        int larguraTela = gerenciador.getLabirinto().getLarguraPixels();
        int xBaseVidas = larguraTela - 105;
        g2d.setColor(Color.YELLOW);
        for (int i = 0; i < gerenciador.getVidas(); i++) {
            g2d.fillArc(xBaseVidas + (i * 28), 12, 20, 20, 35, 290);
        }
    }
}
