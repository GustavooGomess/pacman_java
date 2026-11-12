package jogo.entidades;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import jogo.mapa.Labirinto;

/**
 * Representa os fantasmas inimigos que percorrem o labirinto.
 * Aplica Herança e Polimorfismo ao estender a classe abstrata Entidade.
 */
public class Fantasma extends Entidade {
    private final String nome;
    private final Color cor;
    private final int linhaInicial;
    private final int colunaInicial;
    private final int atrasoSaidaQuadros;
    private final Direcao direcaoSaidaPadrao;
    private final PacMan alvoPacMan;
    private final List<Fantasma> todosFantasmas;
    private final Random random;

    private Direcao direcaoAtual;
    private int contadorAnimacaoSaia;
    private int quadrosDecorridos;
    private boolean naJaula;

    public Fantasma(
        String nome,
        Color cor,
        int linhaInicial,
        int colunaInicial,
        int atrasoSaidaQuadros,
        Direcao direcaoSaidaPadrao,
        Labirinto labirinto,
        PacMan alvoPacMan,
        List<Fantasma> todosFantasmas
    ) {
        super(
            colunaInicial * Labirinto.TAMANHO_CELULA,
            linhaInicial * Labirinto.TAMANHO_CELULA,
            2, // Velocidade que divide TAMANHO_CELULA (28) perfeitamente
            Labirinto.TAMANHO_CELULA,
            labirinto
        );
        this.nome = nome;
        this.cor = cor;
        this.linhaInicial = linhaInicial;
        this.colunaInicial = colunaInicial;
        this.atrasoSaidaQuadros = atrasoSaidaQuadros;
        this.direcaoSaidaPadrao = direcaoSaidaPadrao;
        this.alvoPacMan = alvoPacMan;
        this.todosFantasmas = todosFantasmas;
        this.random = new Random();

        this.naJaula = (linhaInicial >= 9 && linhaInicial <= 10 && colunaInicial >= 8 && colunaInicial <= 10);
        this.direcaoAtual = naJaula ? Direcao.PARADO : direcaoSaidaPadrao;
        this.contadorAnimacaoSaia = 0;
        this.quadrosDecorridos = 0;
    }

    public void reiniciarPosicao() {
        setX(colunaInicial * Labirinto.TAMANHO_CELULA);
        setY(linhaInicial * Labirinto.TAMANHO_CELULA);
        this.naJaula = (linhaInicial >= 9 && linhaInicial <= 10 && colunaInicial >= 8 && colunaInicial <= 10);
        this.direcaoAtual = naJaula ? Direcao.PARADO : direcaoSaidaPadrao;
        this.quadrosDecorridos = 0;
    }

    public String getNome() {
        return nome;
    }

    public boolean estaSaindoDaJaula() {
        int linha = getLinhaCentro();
        int coluna = getColunaCentro();
        return naJaula && quadrosDecorridos >= atrasoSaidaQuadros && coluna == 9 && linha >= 8 && linha <= 10;
    }

    @Override
    public void mover() {
        contadorAnimacaoSaia++;
        quadrosDecorridos++;

        // Enquanto aguarda sua vez na jaula, permanece esperando
        if (naJaula && quadrosDecorridos < atrasoSaidaQuadros) {
            return;
        }

        if (estaAlinhadoNaGrade()) {
            if (naJaula) {
                executarSaidaDaJaula();
            } else {
                escolherDirecaoNoCruzamento();
            }
        }

        if (direcaoAtual != Direcao.PARADO && podeMoverPara(direcaoAtual)) {
            setX(getX() + direcaoAtual.getDx() * getVelocidade());
            setY(getY() + direcaoAtual.getDy() * getVelocidade());
            verificarTunelHorizontal();
        }
    }

    /**
     * Guia o fantasma passo a passo para fora da jaula, um por vez, sem colisões.
     */
    private void executarSaidaDaJaula() {
        int linhaAtual = Math.floorDiv(getY(), Labirinto.TAMANHO_CELULA);
        int colunaAtual = Math.floorDiv(getX(), Labirinto.TAMANHO_CELULA);

        // Se já chegou na linha 8 (corredor externo acima da porta), conclui a saída da jaula
        if (linhaAtual <= 8) {
            naJaula = false;
            direcaoAtual = direcaoSaidaPadrao;
            return;
        }

        // Verifica se outro fantasma já está atravessando o corredor central de saída (coluna 9)
        if (colunaAtual != 9 && corredorSaidaOcupadoPorOutro()) {
            direcaoAtual = Direcao.PARADO;
            return;
        }

        if (colunaAtual < 9) {
            direcaoAtual = Direcao.DIREITA;
        } else if (colunaAtual > 9) {
            direcaoAtual = Direcao.ESQUERDA;
        } else {
            direcaoAtual = Direcao.CIMA;
        }
    }

    private boolean corredorSaidaOcupadoPorOutro() {
        if (todosFantasmas == null) {
            return false;
        }
        for (Fantasma outro : todosFantasmas) {
            if (outro != this && outro.estaSaindoDaJaula()) {
                return true;
            }
        }
        return false;
    }

    private void escolherDirecaoNoCruzamento() {
        int linhaAtual = Math.floorDiv(getY(), Labirinto.TAMANHO_CELULA);
        int colunaAtual = Math.floorDiv(getX(), Labirinto.TAMANHO_CELULA);

        Direcao[] candidatas = {Direcao.CIMA, Direcao.BAIXO, Direcao.ESQUERDA, Direcao.DIREITA};
        List<Direcao> opcoesValidas = new ArrayList<>();
        List<Direcao> opcoesSemOutroFantasma = new ArrayList<>();

        for (Direcao dir : candidatas) {
            int proxLinha = linhaAtual + dir.getDy();
            int proxColuna = colunaAtual + dir.getDx();

            // Impede que qualquer fantasma volte a entrar na porta da jaula (linha 9, coluna 9)
            boolean entrandoNaJaula = (proxLinha >= 9 && proxLinha <= 10 && proxColuna >= 8 && proxColuna <= 10);
            if (entrandoNaJaula) {
                continue;
            }

            if (podeMoverPara(dir) && !ehDirecaoOposta(direcaoAtual, dir)) {
                opcoesValidas.add(dir);
                if (!celulaOcupadaPorOutroFantasma(proxLinha, proxColuna)) {
                    opcoesSemOutroFantasma.add(dir);
                }
            }
        }

        List<Direcao> listaFinal = !opcoesSemOutroFantasma.isEmpty() ? opcoesSemOutroFantasma : opcoesValidas;

        // Caso esteja em um beco sem saída, permite inverter a direção
        if (listaFinal.isEmpty()) {
            for (Direcao dir : candidatas) {
                int proxLinha = linhaAtual + dir.getDy();
                int proxColuna = colunaAtual + dir.getDx();
                boolean entrandoNaJaula = (proxLinha >= 9 && proxLinha <= 10 && proxColuna >= 8 && proxColuna <= 10);
                if (!entrandoNaJaula && podeMoverPara(dir)) {
                    listaFinal.add(dir);
                }
            }
        }

        if (listaFinal.isEmpty()) {
            direcaoAtual = Direcao.PARADO;
            return;
        }

        // Mistura perseguição ao Pac-Man com movimentação variada pelos corredores
        if (alvoPacMan != null && random.nextDouble() < 0.55) {
            Direcao melhorDirecao = listaFinal.get(0);
            double menorDistancia = Double.MAX_VALUE;

            for (Direcao dir : listaFinal) {
                int proxX = getX() + dir.getDx() * Labirinto.TAMANHO_CELULA;
                int proxY = getY() + dir.getDy() * Labirinto.TAMANHO_CELULA;
                double dist = Math.hypot(proxX - alvoPacMan.getX(), proxY - alvoPacMan.getY());
                if (dist < menorDistancia) {
                    menorDistancia = dist;
                    melhorDirecao = dir;
                }
            }
            direcaoAtual = melhorDirecao;
        } else {
            direcaoAtual = listaFinal.get(random.nextInt(listaFinal.size()));
        }
    }

    private boolean celulaOcupadaPorOutroFantasma(int linhaAlvo, int colunaAlvo) {
        if (todosFantasmas == null) {
            return false;
        }
        for (Fantasma outro : todosFantasmas) {
            if (outro != this) {
                int lOutro = outro.getLinhaCentro();
                int cOutro = outro.getColunaCentro();
                if (lOutro == linhaAlvo && cOutro == colunaAlvo) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean ehDirecaoOposta(Direcao d1, Direcao d2) {
        return (d1.getDx() + d2.getDx() == 0) && (d1.getDy() + d2.getDy() == 0) && d1 != Direcao.PARADO;
    }

    @Override
    public void desenhar(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        int margem = 2;
        int x = getX() + margem;
        // Leve flutuação vertical enquanto aguarda dentro da jaula
        int oscilacaoJaula = (naJaula && quadrosDecorridos < atrasoSaidaQuadros)
            ? (int) (Math.sin(contadorAnimacaoSaia * 0.2) * 2)
            : 0;
        int y = getY() + margem + oscilacaoJaula;
        int largura = getTamanho() - margem * 2;
        int altura = getTamanho() - margem * 2;

        // Corpo e cabeça arredondada do fantasma
        g2d.setColor(cor);
        g2d.fillArc(x, y, largura, largura, 0, 180);
        g2d.fillRect(x, y + largura / 2, largura, altura / 2 - 4);

        // Ondinhas animadas na base (saia do fantasma)
        int numOndas = 3;
        int larguraOnda = largura / numOndas;
        int deslocamentoOnda = (contadorAnimacaoSaia / 8) % 2 == 0 ? 0 : 2;
        for (int i = 0; i < numOndas; i++) {
            int ox = x + i * larguraOnda;
            int oy = y + altura - 8 + ((i % 2 == 0) ? deslocamentoOnda : -deslocamentoOnda);
            g2d.fillOval(ox, oy, larguraOnda, 8);
        }

        // Olhos brancos
        g2d.setColor(Color.WHITE);
        int olhoLargura = 6;
        int olhoAltura = 8;
        int olhoY = y + 5;
        int olhoEsqX = x + 4;
        int olhoDirX = x + largura - 10;

        g2d.fillOval(olhoEsqX, olhoY, olhoLargura, olhoAltura);
        g2d.fillOval(olhoDirX, olhoY, olhoLargura, olhoAltura);

        // Pupilas azuis olhando para a direção atual do movimento
        g2d.setColor(new Color(20, 30, 160));
        int pupilaDx = direcaoAtual.getDx() * 2;
        int pupilaDy = direcaoAtual.getDy() * 2;
        g2d.fillOval(olhoEsqX + 1 + pupilaDx, olhoY + 2 + pupilaDy, 4, 4);
        g2d.fillOval(olhoDirX + 1 + pupilaDx, olhoY + 2 + pupilaDy, 4, 4);
    }
}
