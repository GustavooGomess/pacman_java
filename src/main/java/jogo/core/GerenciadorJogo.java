package jogo.core;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import jogo.audio.GerenciadorSom;
import jogo.entidades.Direcao;
import jogo.entidades.Entidade;
import jogo.entidades.Fantasma;
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
        this.vidas = VIDAS_INICIAIS;
        this.vitoriaTocada = false;
        this.gameOver = false;

        this.gerenciadorSom.iniciarMusicaAmbiente();
    }

    private List<Fantasma> criarFantasmas() {
        List<Fantasma> lista = new ArrayList<>();
        // 1º: Vermelho - já inicia fora da jaula indo para a esquerda
        lista.add(new Fantasma("Vermelho", new Color(255, 55, 55), 8, 9, 0, Direcao.ESQUERDA, labirinto, pacMan, lista));
        // 2º: Amarelo - sai após ~2 segundos indo para a direita
        lista.add(new Fantasma("Amarelo", new Color(255, 205, 40), 10, 9, 120, Direcao.DIREITA, labirinto, pacMan, lista));
        // 3º: Verde - sai após ~4.5 segundos indo para a esquerda
        lista.add(new Fantasma("Verde", new Color(45, 220, 95), 10, 8, 270, Direcao.ESQUERDA, labirinto, pacMan, lista));
        // 4º: Azul - sai após ~7 segundos indo para a direita
        lista.add(new Fantasma("Azul", new Color(55, 185, 255), 10, 10, 420, Direcao.DIREITA, labirinto, pacMan, lista));
        return lista;
    }

    public void atualizar() {
        if (gameOver || todasPastilhasColetadas()) {
            return;
        }

        // Se o Pac-Man está executando a animação de morte, aguarda ela terminar
        if (pacMan.estaMorrendo()) {
            pacMan.mover();
            if (pacMan.animacaoMorteConcluida()) {
                processarPosMorte();
            }
            return;
        }

        // Polimorfismo: atualiza o Pac-Man e todos os fantasmas uniformemente como Entidade
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

        // Durante a animação de morte, exibe apenas o Pac-Man se desfazendo (fiel ao arcade)
        if (pacMan.estaMorrendo()) {
            pacMan.desenhar(g);
            return;
        }

        // Polimorfismo: desenha Pac-Man e Fantasmas a partir da mesma lista de Entidade
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
