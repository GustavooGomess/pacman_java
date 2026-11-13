package jogo.core;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import jogo.audio.GerenciadorSom;
import jogo.entidades.Direcao;
import jogo.entidades.Entidade;
import jogo.entidades.Fantasma;
import jogo.entidades.Fantasma.Personalidade;
import jogo.entidades.PacMan;
import jogo.mapa.Labirinto;
import jogo.mapa.TipoCelula;

/**
 * Coordena o estado da partida, pontuação, vidas, colisões, efeitos sonoros
 * e atualização polimórfica das entidades.
 */
public class GerenciadorJogo {
    private static final int VIDAS_INICIAIS = 3;

    private final Labirinto labirinto;
    private final PacMan pacMan;
    private final List<Fantasma> fantasmas;
    private final List<Entidade> entidades;
    private final GerenciadorSom gerenciadorSom;

    private int pontuacao;
    private int recorde;
    private int vidas;
    private boolean vitoriaTocada;
    private boolean gameOver;

    public GerenciadorJogo() {
        this.labirinto = new Labirinto();
        this.pacMan = new PacMan(
            Labirinto.LINHA_INICIAL_PACMAN,
            Labirinto.COLUNA_INICIAL_PACMAN,
            labirinto
        );

        this.fantasmas = criarFantasmas();
        this.entidades = new ArrayList<>();
        this.entidades.add(pacMan);
        this.entidades.addAll(fantasmas);

        this.gerenciadorSom = new GerenciadorSom();
        this.pontuacao = 0;
        this.recorde = 0;
        this.vidas = VIDAS_INICIAIS;
        this.vitoriaTocada = false;
        this.gameOver = false;

        this.gerenciadorSom.iniciarMusicaAmbiente();
    }

    private List<Fantasma> criarFantasmas() {
        List<Fantasma> lista = new ArrayList<>();
        // 1º: Vermelho (Perseguidor) - cor vibrante estilo arcade
        lista.add(new Fantasma(
            "Vermelho", new Color(255, 28, 28), 8, 9, 0,
            Direcao.ESQUERDA, Personalidade.PERSEGUIDOR, labirinto, pacMan
        ));
        // 2º: Amarelo/Laranja (Patrulheiro) - sai após ~1.5s
        lista.add(new Fantasma(
            "Amarelo", new Color(255, 184, 32), 10, 9, 90,
            Direcao.DIREITA, Personalidade.PATRULHEIRO, labirinto, pacMan
        ));
        // 3º: Verde (Emboscador) - sai após ~3.5s
        lista.add(new Fantasma(
            "Verde", new Color(0, 230, 118), 10, 8, 210,
            Direcao.ESQUERDA, Personalidade.EMBOSCADOR, labirinto, pacMan
        ));
        // 4º: Azul/Ciano (Estrategista) - sai após ~5.5s
        lista.add(new Fantasma(
            "Azul", new Color(0, 229, 255), 10, 10, 330,
            Direcao.DIREITA, Personalidade.ESTRATEGISTA, labirinto, pacMan
        ));
        return lista;
    }

    public void atualizar() {
        if (gameOver || todasPastilhasColetadas()) {
            return;
        }

        if (pacMan.estaMorrendo()) {
            pacMan.mover();
            if (pacMan.animacaoMorteConcluida()) {
                processarPosMorte();
            }
            return;
        }

        for (Entidade entidade : entidades) {
            entidade.mover();
        }

        verificarColetaItens();
        verificarColisaoComFantasmas();
    }

    private void verificarColetaItens() {
        TipoCelula itemColetado = labirinto.coletarItem(pacMan.getLinhaCentro(), pacMan.getColunaCentro());
        if (itemColetado != TipoCelula.VAZIO) {
            adicionarPontos(itemColetado.getPontos());

            if (todasPastilhasColetadas() && !vitoriaTocada) {
                vitoriaTocada = true;
                gerenciadorSom.tocarVitoria();
            } else if (itemColetado == TipoCelula.POWER_UP) {
                gerenciadorSom.tocarPowerUp();
            } else {
                gerenciadorSom.tocarWaka();
            }
        }
    }

    private void verificarColisaoComFantasmas() {
        int centroPacX = pacMan.getX() + pacMan.getTamanho() / 2;
        int centroPacY = pacMan.getY() + pacMan.getTamanho() / 2;
        double distanciaLimite = Labirinto.TAMANHO_CELULA * 0.62;

        for (Fantasma fantasma : fantasmas) {
            int centroFanX = fantasma.getX() + fantasma.getTamanho() / 2;
            int centroFanY = fantasma.getY() + fantasma.getTamanho() / 2;

            double distancia = Math.hypot(centroPacX - centroFanX, centroPacY - centroFanY);
            if (distancia < distanciaLimite) {
                iniciarSequenciaMorte();
                break;
            }
        }
    }

    private void iniciarSequenciaMorte() {
        gerenciadorSom.pararMusicaAmbiente();
        gerenciadorSom.tocarMorte();
        pacMan.iniciarAnimacaoMorte();
    }

    private void processarPosMorte() {
        perderVida();
        if (vidas > 0) {
            reposicionarPersonagens();
            gerenciadorSom.iniciarMusicaAmbiente();
        } else {
            gameOver = true;
        }
    }

    private void reposicionarPersonagens() {
        pacMan.reposicionar(Labirinto.LINHA_INICIAL_PACMAN, Labirinto.COLUNA_INICIAL_PACMAN);
        for (Fantasma fantasma : fantasmas) {
            fantasma.reiniciarPosicao();
        }
    }

    public void desenhar(Graphics g) {
        labirinto.desenhar(g);

        if (pacMan.estaMorrendo()) {
            pacMan.desenhar(g);
            return;
        }

        for (Entidade entidade : entidades) {
            entidade.desenhar(g);
        }
    }

    public void alterarDirecaoPacMan(Direcao direcao) {
        if (!pacMan.estaMorrendo() && !gameOver) {
            pacMan.setProximaDirecao(direcao);
        }
    }

    public void reiniciar() {
        labirinto.carregarLayoutPadrao();
        reposicionarPersonagens();
        this.pontuacao = 0;
        this.vidas = VIDAS_INICIAIS;
        this.vitoriaTocada = false;
        this.gameOver = false;
        this.gerenciadorSom.iniciarMusicaAmbiente();
    }

    private void adicionarPontos(int quantidade) {
        if (quantidade > 0) {
            this.pontuacao += quantidade;
            if (this.pontuacao > this.recorde) {
                this.recorde = this.pontuacao;
            }
        }
    }

    private void perderVida() {
        if (this.vidas > 0) {
            this.vidas--;
        }
    }

    public int getPontuacao() {
        return pontuacao;
    }

    public int getRecorde() {
        return recorde;
    }

    public int getVidas() {
        return vidas;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean todasPastilhasColetadas() {
        return labirinto.getPastilhasRestantes() == 0;
    }

    public Labirinto getLabirinto() {
        return labirinto;
    }
}
