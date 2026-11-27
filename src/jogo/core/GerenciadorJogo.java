package jogo.core;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jogo.audio.GerenciadorSom;
import jogo.entidades.Direcao;
import jogo.entidades.Entidade;
import jogo.entidades.Fantasma;
import jogo.entidades.Fantasma.Personalidade;
import jogo.entidades.PacMan;
import jogo.mapa.Labirinto;
import jogo.mapa.TipoCelula;
import jogo.ui.TextoPontuacaoFlutuante;

/**
 * Coordena o estado da partida, pontuação, vidas, colisões, efeitos sonoros,
 * persistência de recordes arcade e atualização polimórfica das entidades.
 */
public class GerenciadorJogo {
    private List<HighScoreEntry> listaHighScores;
    private static final int MAX_RECORDES = 5;
    private static final String ARQUIVO_RECORDES = "highscores.dat";
    private boolean recordeProcessado = false;
    private boolean jogoIniciado = false;
    private String nomeJogador = "";

    private int temporizadorModoAssustado = 0;
    private static final int DURACAO_MODO_ASSUSTADO = 450; // ~7.5 segundos a 60 FPS
    private int fantasmasComidosSequencia = 0;
    private final List<TextoPontuacaoFlutuante> textosPontuacao = new ArrayList<>();

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
        this.jogoIniciado = false;
        // A música e o jogo só iniciam quando o jogador informar o nome e clicar no botão
    }

    private List<Fantasma> criarFantasmas() {
        List<Fantasma> lista = new ArrayList<>();
        lista.add(new Fantasma("Vermelho", new Color(255, 28, 28), 8, 9, 0, Direcao.ESQUERDA, Personalidade.PERSEGUIDOR, labirinto, pacMan));
        lista.add(new Fantasma("Amarelo", new Color(255, 184, 32), 10, 9, 90, Direcao.DIREITA, Personalidade.PATRULHEIRO, labirinto, pacMan));
        lista.add(new Fantasma("Rosa", new Color(255, 184, 255), 10, 8, 210, Direcao.ESQUERDA, Personalidade.EMBOSCADOR, labirinto, pacMan));
        lista.add(new Fantasma("Azul", new Color(0, 229, 255), 10, 10, 330, Direcao.DIREITA, Personalidade.ESTRATEGISTA, labirinto, pacMan));
        return lista;
    }

    public void iniciarPartida(String nome) {
        setNomeJogador(nome);
        this.jogoIniciado = true;
        this.recordeProcessado = false;
        this.gerenciadorSom.iniciarMusicaAmbiente();
    }

    public void pararPartida() {
        this.jogoIniciado = false;
        this.gerenciadorSom.pararMusicaAmbiente();
    }

    public void atualizar() {
        if (!jogoIniciado || gameOver || todasPastilhasColetadas()) {
            if (jogoIniciado && (gameOver || todasPastilhasColetadas()) && !recordeProcessado) {
                processarFimDeJogo();
            }
            return;
        }

        if (temporizadorModoAssustado > 0) {
            temporizadorModoAssustado--;
            if (temporizadorModoAssustado == 0) {
                fantasmasComidosSequencia = 0;
                for (Fantasma f : fantasmas) {
                    f.setAssustado(false);
                }
            }
        }

        // Atualiza os textos de pontuação flutuantes e remove os expirados
        for (TextoPontuacaoFlutuante t : textosPontuacao) {
            t.atualizar();
        }
        textosPontuacao.removeIf(t -> !t.estaAtivo());

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
                processarFimDeJogo();
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
        fantasmasComidosSequencia = 0;
        for (Fantasma f : fantasmas) {
            f.setAssustado(true);
        }
    }

    private void verificarColisaoComFantasmas() {
        int centroPacX = pacMan.getX() + pacMan.getTamanho() / 2;
        int centroPacY = pacMan.getY() + pacMan.getTamanho() / 2;
        double distanciaLimite = Labirinto.TAMANHO_CELULA * 0.62;

        for (Fantasma fantasma : fantasmas) {
            // Se o fantasma ainda estiver dentro da jaula ou saindo dela, não colide
            if (fantasma.isNaJaula()) {
                continue;
            }

            int centroFanX = fantasma.getX() + fantasma.getTamanho() / 2;
            int centroFanY = fantasma.getY() + fantasma.getTamanho() / 2;

            double distancia = Math.hypot(centroPacX - centroFanX, centroPacY - centroFanY);
            if (distancia < distanciaLimite) {
                if (fantasma.isAssustado()) {
                    fantasmasComidosSequencia++;
                    // Sequência autêntica de pontuação do arcade: 200, 400, 800, 1600
                    int pontosGanhos = 200 * (1 << Math.min(3, fantasmasComidosSequencia - 1));
                    adicionarPontos(pontosGanhos);

                    // Cria o texto de pontuação flutuante na posição onde o fantasma foi comido
                    textosPontuacao.add(new TextoPontuacaoFlutuante(
                        fantasma.getX() + 2,
                        fantasma.getY() + 18,
                        pontosGanhos,
                        60
                    ));

                    gerenciadorSom.tocarComerFantasma();
                    fantasma.enviarParaJaula();
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
            processarFimDeJogo();
        }
    }

    public void processarFimDeJogo() {
        if (!recordeProcessado) {
            recordeProcessado = true;
            verificarEAdicionarNovoRecorde(nomeJogador);
        }
    }

    private void reposicionarPersonagens() {
        pacMan.reposicionar(Labirinto.LINHA_INICIAL_PACMAN, Labirinto.COLUNA_INICIAL_PACMAN);
        for (Fantasma fantasma : fantasmas) {
            fantasma.reiniciarPosicao();
        }
        this.textosPontuacao.clear();
        this.fantasmasComidosSequencia = 0;
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

        Graphics2D g2d = (Graphics2D) g;
        for (TextoPontuacaoFlutuante texto : textosPontuacao) {
            texto.desenhar(g2d);
        }
    }

    public void alterarDirecaoPacMan(Direcao direcao) {
        if (jogoIniciado && !pacMan.estaMorrendo() && !gameOver) {
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
        this.jogoIniciado = false;
        this.gerenciadorSom.pararMusicaAmbiente();
        this.textosPontuacao.clear();
        this.fantasmasComidosSequencia = 0;
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

    public List<HighScoreEntry> getListaHighScores() {
        return Collections.unmodifiableList(listaHighScores);
    }

    public boolean verificarEAdicionarNovoRecorde(String nome) {
        if (pontuacao < 0) return false;

        String nomeNormalizado = (nome == null || nome.trim().isEmpty()) ? "JOGADOR" : nome.trim().toUpperCase();
        if (nomeNormalizado.length() > 6) {
            nomeNormalizado = nomeNormalizado.substring(0, 6);
        }

        // Se já atingiu as 5 colocações, só entra se a pontuação for maior OU IGUAL à do 5º colocado
        if (listaHighScores.size() >= MAX_RECORDES) {
            HighScoreEntry menorRecorde = listaHighScores.get(listaHighScores.size() - 1);
            if (this.pontuacao < menorRecorde.getPontuacao()) {
                // Pontuação menor que a do 5º colocado; não entra no ranking
                return false;
            }
            // Caso o score seja o mesmo do quinto lugar, substitui o anterior
            if (this.pontuacao == menorRecorde.getPontuacao()) {
                listaHighScores.remove(listaHighScores.size() - 1);
            }
        }

        // Adiciona a nova pontuação e reordena decrescente
        listaHighScores.add(new HighScoreEntry(nomeNormalizado, pontuacao));
        Collections.sort(listaHighScores);

        // Se excedeu as 5 posições, descarta quem ficou abaixo do 5º colocado (substituição)
        if (listaHighScores.size() > MAX_RECORDES) {
            listaHighScores = new ArrayList<>(listaHighScores.subList(0, MAX_RECORDES));
        }

        salvarHighScores();
        this.recorde = getRecorde();
        return true;
    }

    public void resetarRanking() {
        if (listaHighScores != null) {
            listaHighScores.clear();
        } else {
            listaHighScores = new ArrayList<>();
        }
        this.recorde = 0;
        File arquivo = new File(ARQUIVO_RECORDES);
        if (arquivo.exists()) {
            arquivo.delete();
        }
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

        // Não há ranking pré-selecionado nem fictício; é iniciado vazio e preenchido conforme os jogadores jogam
        return new ArrayList<>();
    }

    public String getNomeJogador() {
        return nomeJogador;
    }

    public void setNomeJogador(String nomeJogador) {
        if (nomeJogador != null && !nomeJogador.trim().isEmpty()) {
            String nome = nomeJogador.trim().toUpperCase();
            if (nome.length() > 6) {
                nome = nome.substring(0, 6);
            }
            this.nomeJogador = nome;
        }
    }

    public boolean isJogoIniciado() {
        return jogoIniciado;
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
