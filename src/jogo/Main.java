package jogo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import jogo.ui.TelaJogo;

/**
 * Classe principal responsável por inicializar a janela do jogo e barra de controles.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame janela = new JFrame("Pac-Man 2D - POO");
            janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            janela.setResizable(false);
            janela.setLayout(new BorderLayout());

            TelaJogo telaJogo = new TelaJogo();
            janela.add(telaJogo, BorderLayout.CENTER);

            // Barra inferior com o botão de resetar o ranking
            JPanel barraInferior = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
            barraInferior.setBackground(new Color(6, 8, 24));
            barraInferior.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(33, 33, 255)));

            JButton botaoResetar = new JButton("RESETAR RANKING");
            botaoResetar.setFont(new Font("Monospaced", Font.BOLD, 12));
            botaoResetar.setBackground(new Color(220, 40, 40));
            botaoResetar.setForeground(Color.WHITE);
            botaoResetar.setFocusPainted(false);
            botaoResetar.setCursor(new Cursor(Cursor.HAND_CURSOR));
            botaoResetar.setToolTipText("Clique para resetar todas as pontuações e limpar o ranking arcade");

            botaoResetar.addActionListener(e -> {
                telaJogo.confirmarEResetarRanking();
            });

            barraInferior.add(botaoResetar);
            janela.add(barraInferior, BorderLayout.SOUTH);

            janela.pack();
            janela.setLocationRelativeTo(null);
            janela.setVisible(true);

            // Solicita o nome/iniciais do jogador e somente inicia a partida após o clique do botão
            telaJogo.solicitarNomeEIniciar(true);
        });
    }
}
