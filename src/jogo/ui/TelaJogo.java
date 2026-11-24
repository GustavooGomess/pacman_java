package jogo.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import jogo.core.GerenciadorJogo;
import jogo.core.HighScoreEntry;
import jogo.entidades.Direcao;

/**
 * Painel gráfico responsável pelo Game Loop, exibição arcade retro e ranking de competidores.
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

        // O timer só será iniciado após a pessoa digitar o nome e clicar no botão
        this.timer = new Timer(INTERVALO_TIMER_MS, this);
    }

    /**
     * Exibe o diálogo com campo vazio e aguarda o clique do botão para iniciar a partida.
     * O jogo e a música só começam depois que o input é confirmado.
     */
    public void solicitarNomeEIniciar(boolean primeiraVez) {
        timer.stop();
        gerenciador.pararPartida();

        String nomeConfirmado = null;

        while (nomeConfirmado == null) {
            JPanel painel = new JPanel(new GridLayout(0, 1, 6, 6));
            JLabel labelMsg = new JLabel(primeiraVez
                ? "BEM-VINDO AO PAC-MAN ARCADE! Digite seu nome/iniciais:"
                : "NOVO COMPETIDOR! Digite seu nome/iniciais:");
            labelMsg.setFont(new Font("Monospaced", Font.BOLD, 13));

            JTextField campoNome = new JTextField(10);
            campoNome.setFont(new Font("Monospaced", Font.BOLD, 14));
            // Campo de texto começa 100% vazio, sem nomes pré-selecionados
            campoNome.setText("");

            painel.add(labelMsg);
            painel.add(campoNome);

            // Garante foco inicial no campo de digitação
            SwingUtilities.invokeLater(campoNome::requestFocusInWindow);

            Object[] opcoes = { "JOGAR", "CANCELAR" };
            int escolha = JOptionPane.showOptionDialog(
                this,
                painel,
                "ARCADE HIGHSCORE - ENTRADA DE COMPETIDOR",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                opcoes,
                opcoes[0]
            );

            if (escolha == 0) { // Clicou no botão "JOGAR"
                String texto = campoNome.getText().trim();
                if (!texto.isEmpty()) {
                    nomeConfirmado = texto.toUpperCase();
                } else {
                    JOptionPane.showMessageDialog(
                        this,
                        "Por favor, digite um nome ou iniciais para competir no ranking!",
                        "Entrada Obrigatória",
                        JOptionPane.WARNING_MESSAGE
                    );
                }
            } else { // Clicou em "CANCELAR" ou fechou a janela
                if (primeiraVez) {
                    System.exit(0);
                    return;
                } else {
                    // Ao reiniciar, se cancelar, mantém o competidor anterior
                    nomeConfirmado = gerenciador.getNomeJogador().isEmpty() ? "PLAYER" : gerenciador.getNomeJogador();
                }
            }
        }

        // Inicia a partida somente agora (após o clique do botão)
        gerenciador.iniciarPartida(nomeConfirmado);
        timer.start();
        requestFocusInWindow();
        repaint();
    }

    /**
     * Reseta todo o ranking arcade após confirmação do usuário.
     */
    public void confirmarEResetarRanking() {
        int escolha = JOptionPane.showConfirmDialog(
            this,
            "Deseja realmente apagar todas as pontuações e resetar o ranking arcade?",
            "CONFIRMAR RESET DO RANKING",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (escolha == JOptionPane.YES_OPTION) {
            gerenciador.resetarRanking();
            repaint();
            JOptionPane.showMessageDialog(
                this,
                "O ranking arcade foi completamente resetado!",
                "RANKING RESETADO",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
        requestFocusInWindow();
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
                    case KeyEvent.VK_R, KeyEvent.VK_N -> {
                        gerenciador.reiniciar();
                        repaint();
                        solicitarNomeEIniciar(false);
                    }
                    case KeyEvent.VK_DELETE -> confirmarEResetarRanking();
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
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        // 1. Cabeçalho arcade (jogador, pontuação, recorde e vidas)
        desenharCabecalhoArcade(g2d);

        // 2. Cenário, pastilhas, Pac-Man e Fantasmas
        Graphics2D gMapa = (Graphics2D) g2d.create();
        gMapa.translate(0, ALTURA_PAINEL_SUPERIOR);
        gerenciador.desenhar(gMapa);
        gMapa.dispose();

        // 3. Banner de Fim de Jogo (GameOver / Vitória) com o Ranking de High Scores
        desenharBannersFimDeJogo(g2d);

        // 4. Scanlines CRT retro por cima de toda a tela
        desenharScanlinesRetro(g2d);

        g2d.dispose();
        Toolkit.getDefaultToolkit().sync();
    }

    private void desenharCabecalhoArcade(Graphics2D g2d) {
        int larguraTela = gerenciador.getLabirinto().getLarguraPixels();

        g2d.setColor(new Color(33, 33, 255));
        g2d.fillRect(0, ALTURA_PAINEL_SUPERIOR - 3, larguraTela, 2);

        g2d.setFont(new Font("Monospaced", Font.BOLD, 13));
        g2d.setColor(new Color(255, 80, 80));
        String rotulo1UP = gerenciador.getNomeJogador().isEmpty() ? "1UP" : "1UP:" + gerenciador.getNomeJogador();
        g2d.drawString(rotulo1UP, 14, 17);
        g2d.drawString("HIGH SCORE", 195, 17);

        g2d.setFont(new Font("Monospaced", Font.BOLD, 16));
        g2d.setColor(new Color(255, 240, 200));
        g2d.drawString(String.format("%06d", gerenciador.getPontuacao()), 14, 37);
        g2d.drawString(String.format("%06d", gerenciador.getRecorde()), 210, 37);

        int xBaseVidas = larguraTela - 96;
        for (int i = 0; i < gerenciador.getVidas(); i++) {
            desenharMiniPacManPixel(g2d, xBaseVidas + (i * 28), 18);
        }
    }

    private void desenharBannersFimDeJogo(Graphics2D g2d) {
        if (gerenciador.isGameOver()) {
            desenharBannerRetroComRanking(g2d, "GAME OVER", "PRESSIONE [R] PARA NOVO JOGO", new Color(255, 50, 50));
        } else if (gerenciador.todasPastilhasColetadas()) {
            desenharBannerRetroComRanking(g2d, "STAGE CLEAR!", "PRESSIONE [R] PARA NOVO JOGO", new Color(0, 230, 118));
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

    private void desenharBannerRetroComRanking(Graphics2D g2d, String titulo, String subtitulo, Color corPrincipal) {
        int largura = gerenciador.getLabirinto().getLarguraPixels();
        int larguraBanner = 360;
        int alturaBanner = 230;
        int cx = (largura - larguraBanner) / 2;
        int cy = ALTURA_PAINEL_SUPERIOR + (gerenciador.getLabirinto().getAlturaPixels() - alturaBanner) / 2;

        // Fundo escuro semitransparente
        g2d.setColor(new Color(6, 6, 20, 248));
        g2d.fillRect(cx, cy, larguraBanner, alturaBanner);

        // Borda dupla estilo neon
        g2d.setColor(corPrincipal);
        g2d.drawRect(cx, cy, larguraBanner, alturaBanner);
        g2d.drawRect(cx + 2, cy + 2, larguraBanner - 4, alturaBanner - 4);

        // Título principal
        g2d.setFont(new Font("Monospaced", Font.BOLD, 22));
        int larguraTitulo = g2d.getFontMetrics().stringWidth(titulo);
        g2d.drawString(titulo, cx + (larguraBanner - larguraTitulo) / 2, cy + 28);

        // Subtítulo
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 11));
        int larguraSubtitulo = g2d.getFontMetrics().stringWidth(subtitulo);
        g2d.drawString(subtitulo, cx + (larguraBanner - larguraSubtitulo) / 2, cy + 46);

        // Divisória sutil
        g2d.setColor(new Color(60, 60, 120));
        g2d.drawLine(cx + 20, cy + 54, cx + larguraBanner - 20, cy + 54);

        // Tabela de pontuações
        desenharTabelaHighScores(g2d, cx, cy + 58, larguraBanner);
    }

    private void desenharTabelaHighScores(Graphics2D g2d, int cx, int cy, int larguraBanner) {
        g2d.setFont(new Font("Monospaced", Font.BOLD, 13));
        g2d.setColor(new Color(255, 230, 0));
        String tituloTabela = "-- TOP 5 RANKING ARCADE --";
        int largTit = g2d.getFontMetrics().stringWidth(tituloTabela);
        g2d.drawString(tituloTabela, cx + (larguraBanner - largTit) / 2, cy + 18);

        List<HighScoreEntry> lista = gerenciador.getListaHighScores();

        // Se ninguém jogou ainda, informa que o ranking está vazio
        if (lista.isEmpty()) {
            g2d.setColor(new Color(180, 185, 215));
            g2d.setFont(new Font("Monospaced", Font.BOLD, 12));
            String semRecorde = "NENHUM RECORDE REGISTRADO";
            int larg1 = g2d.getFontMetrics().stringWidth(semRecorde);
            g2d.drawString(semRecorde, cx + (larguraBanner - larg1) / 2, cy + 50);

            g2d.setColor(new Color(0, 230, 118));
            String sejaPrimeiro = "SEJA O PRIMEIRO A PONTUAR!";
            int larg2 = g2d.getFontMetrics().stringWidth(sejaPrimeiro);
            g2d.drawString(sejaPrimeiro, cx + (larguraBanner - larg2) / 2, cy + 74);
            return;
        }

        int yItem = cy + 40;

        for (int i = 0; i < lista.size(); i++) {
            HighScoreEntry entry = lista.get(i);

            // Verifica se este registro é o do competidor na partida que acabou
            boolean ehJogadorAtual = entry.getNome().equalsIgnoreCase(gerenciador.getNomeJogador())
                    && entry.getPontuacao() == gerenciador.getPontuacao()
                    && gerenciador.getPontuacao() >= 0;

            if (ehJogadorAtual) {
                g2d.setColor(new Color(0, 255, 255)); // Ciano neon brilhante para o jogador atual
            } else if (i == 0) {
                g2d.setColor(new Color(255, 215, 0)); // Dourado para o primeiro lugar
            } else {
                g2d.setColor(new Color(210, 215, 235)); // Branco prateado para os demais
            }

            String posicao = switch (i) {
                case 0 -> "1ST";
                case 1 -> "2ND";
                case 2 -> "3RD";
                default -> (i + 1) + "TH";
            };

            String textoLinha = String.format("%-3s  %-6s  %06d PTS", posicao, entry.getNome(), entry.getPontuacao());
            if (ehJogadorAtual) {
                textoLinha += " < VOCÊ";
            }

            g2d.setFont(new Font("Monospaced", Font.BOLD, 12));
            int largLinha = g2d.getFontMetrics().stringWidth(textoLinha);
            g2d.drawString(textoLinha, cx + (larguraBanner - largLinha) / 2, yItem);

            yItem += 20;
        }

        // Se o jogador terminou a partida mas não conseguiu pontuação suficiente para entrar no TOP 5
        boolean entrouNoRanking = lista.stream().anyMatch(e ->
            e.getNome().equalsIgnoreCase(gerenciador.getNomeJogador())
            && e.getPontuacao() == gerenciador.getPontuacao()
            && gerenciador.getPontuacao() >= 0
        );

        if (!entrouNoRanking && lista.size() >= 5) {
            g2d.setColor(new Color(255, 140, 140));
            g2d.setFont(new Font("Monospaced", Font.BOLD, 11));
            String aviso = "SUA PONTUAÇÃO NÃO ENTROU NO TOP 5";
            int largAviso = g2d.getFontMetrics().stringWidth(aviso);
            g2d.drawString(aviso, cx + (larguraBanner - largAviso) / 2, yItem + 5);
        }
    }

    private void desenharScanlinesRetro(Graphics2D g2d) {
        int largura = getWidth();
        int altura = getHeight();
        g2d.setColor(new Color(0, 0, 0, 38));
        for (int y = 0; y < altura; y += 4) {
            g2d.fillRect(0, y, largura, 1);
        }
    }
}
