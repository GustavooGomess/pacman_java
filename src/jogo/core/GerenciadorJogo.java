package jogo.core;

import java.awt.Color;
import java.awt.Graphics;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JOptionPane;
import jogo.audio.GerenciadorSom;
import jogo.entidades.Direcao;
import jogo.entidades.Entidade;
import jogo.entidades.Fantasma;
import jogo.entidades.Fantasma.Personalidade;
import jogo.entidades.PacMan;
import jogo.mapa.Labirinto;
import jogo.mapa.TipoCelula;

/**
 * Representa uma entrada individual na tabela de recordes.
 */
class HighScoreEntry implements Serializable, Comparable<HighScoreEntry> {
    private static final long serialVersionUID = 1L;
    
    private final String nome;
    private final int pontuacao;

    public HighScoreEntry(String nome, int pontuacao) {
        this.nome = nome;
        this.pontuacao = pontuacao;
    }

    public String getNome() {
        return nome;
    }

    public int getPontuacao() {
        return pontuacao;
    }

    @Override
    public int compareTo(HighScoreEntry outro) {
        return Integer.compare(outro.pontuacao, this.pontuacao);
    }
}

/**
 * Coordena o estado da partida, pontuação, vidas, colisões, efeitos sonoros
 * e atualização polimórfica das entidades.
 */
public class GerenciadorJogo {
    private List<HighScoreEntry> listaHighScores;
    private static final int MAX_RECORDES = 4;
    private static final String ARQUIVO_RECORDES = "highscores.dat";
    private boolean recordeProcessado = false;

    private int temporizadorModoAssustado = 0;
    private static final int DURACAO_MODO_ASSUSTADO = 200; // ~3.3 segundos a 60 FPS

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
        this.listaHighScores = carregarHighScores();
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
        this.recorde = getRecorde();
        this.vidas = VIDAS_INICIAIS;
        this.vitoriaTocada = false;
        this.gameOver = false;

        this.gerenciadorSom.iniciarMusicaAmbiente();
    }

    private List<Fantasma> criarFantasmas() {
        List<Fantasma> lista = new ArrayList<>();
        lista.add(new Fantasma("Vermelho", new Color(255, 28, 28), 8, 9, 0, Direcao.ESQUERDA, Personalidade.PERSEGUIDOR, labirinto, pacMan));
        lista.add(new Fantasma("Amarelo", new Color(255, 184, 32), 10, 9, 90, Direcao.DIREITA, Personalidade.PATRULHEIRO, labirinto, pacMan));
        lista.add(new Fantasma("Rosa", new Color(255, 184, 255), 10, 8, 210, Direcao.ESQUERDA, Personalidade.EMBOSCADOR, labirinto, pacMan));
        lista.add(new Fantasma("Azul", new Color(0, 229, 255), 10, 10, 330, Direcao.DIREITA, Personalidade.ESTRATEGISTA, labirinto, pacMan));
        return lista;
    }

    public void atualizar() {
        if (gameOver || todasPastilhasColetadas()) {
            return;
        }

        if (temporizadorModoAssustado > 0) {
            temporizadorModoAssustado--;
            if (temporizadorModoAssustado == 0) {
                for (Fantasma f : fantasmas) {
                    f.setAssustado(false);
                }
            }
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
                ativarModoAssustadoFantasmas();
            } else {
                gerenciadorSom.tocarWaka();
            }
        }
    }

    private void ativarModoAssustadoFantasmas() {
        temporizadorModoAssustado = DURACAO_MODO_ASSUSTADO;
        for (Fantasma f : fantasmas) {
            f.setAssustado(true);
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
                if (fantasma.isAssustado()) {
                    fantasma.enviarParaJaula();
                    adicionarPontos(200);
                } else {
                    iniciarSequenciaMorte();
                }
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
        this.recordeProcessado = false;
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

    public List<HighScoreEntry> getListaHighScores() {
        return listaHighScores;
    }

    public boolean verificarEAdicionarNovoRecorde(String nome) {
        if (pontuacao <= 0) return false;
        
        listaHighScores.add(new HighScoreEntry(nome.toUpperCase(), pontuacao));
        Collections.sort(listaHighScores);
        
        if (listaHighScores.size() > MAX_RECORDES) {
            listaHighScores = new ArrayList<>(listaHighScores.subList(0, MAX_RECORDES));
        }
        
        salvarHighScores();
        return true;
    }

    public int getRecorde() {
        if (listaHighScores == null || listaHighScores.isEmpty()) return 0;
        return listaHighScores.get(0).getPontuacao();
    }

    private void salvarHighScores() {
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(new java.io.FileOutputStream(ARQUIVO_RECORDES))) {
            oos.writeObject(listaHighScores);
        } catch (Exception ignored) {}
    }

    @SuppressWarnings("unchecked")
    private List<HighScoreEntry> carregarHighScores() {
        File arquivo = new File(ARQUIVO_RECORDES);
        if (arquivo.exists()) {
            try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.FileInputStream(arquivo))) {
                return (List<HighScoreEntry>) ois.readObject();
            } catch (Exception ignored) {}
        }
        
        List<HighScoreEntry> padrao = new ArrayList<>();
        padrao.add(new HighScoreEntry("PAC", 1000));
        padrao.add(new HighScoreEntry("JIM", 750));
        padrao.add(new HighScoreEntry("BOB", 500));
        padrao.add(new HighScoreEntry("ANN", 250));
        return padrao;
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

    public boolean isRecordeProcessado() {
        return recordeProcessado;
    }

    public void setRecordeProcessado(boolean recordeProcessado) {
        this.recordeProcessado = recordeProcessado;
    }

    public boolean todasPastilhasColetadas() {
        return labirinto.getPastilhasRestantes() == 0;
    }

    public Labirinto getLabirinto() {
        return labirinto;
    }
}