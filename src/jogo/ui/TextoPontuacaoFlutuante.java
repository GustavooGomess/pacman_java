package jogo.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * Exibe a pontuação bônus obtida ao devorar um fantasma no local exato da colisão,
 * simulando o efeito autêntico do arcade clássico do Pac-Man.
 */
public class TextoPontuacaoFlutuante {
    private final int x;
    private int y;
    private final int pontos;
    private int quadrosRestantes;

    public TextoPontuacaoFlutuante(int x, int y, int pontos, int duracaoQuadros) {
        this.x = x;
        this.y = y;
        this.pontos = pontos;
        this.quadrosRestantes = duracaoQuadros;
    }

    public void atualizar() {
        if (quadrosRestantes > 0) {
            quadrosRestantes--;
            // Flutua suavemente para cima (1 pixel a cada 6 quadros)
            if (quadrosRestantes % 6 == 0) {
                y--;
            }
        }
    }

    public boolean estaAtivo() {
        return quadrosRestantes > 0;
    }

    public void desenhar(Graphics2D g2d) {
        String texto = String.valueOf(pontos);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 13));

        // Borda/sombra escura para alto contraste contra o fundo do labirinto
        g2d.setColor(Color.BLACK);
        g2d.drawString(texto, x + 1, y);
        g2d.drawString(texto, x - 1, y);
        g2d.drawString(texto, x, y + 1);
        g2d.drawString(texto, x, y - 1);

        // Cor ciano clássica do arcade do Pac-Man (200, 400, 800, 1600)
        g2d.setColor(new Color(0, 229, 255));
        g2d.drawString(texto, x, y);
    }
}
