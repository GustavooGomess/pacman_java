package jogo;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import jogo.ui.TelaJogo;

/**
 * Classe principal responsável por inicializar a janela do jogo.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame janela = new JFrame("Pac-Man 2D - POO");
            janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            janela.setResizable(false);

            TelaJogo telaJogo = new TelaJogo();
            janela.add(telaJogo);
            janela.pack();
            janela.setLocationRelativeTo(null);
            janela.setVisible(true);

            telaJogo.requestFocusInWindow();
        });
    }
}

