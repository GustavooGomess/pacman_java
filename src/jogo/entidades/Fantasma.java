package jogo.entidades;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import jogo.mapa.Labirinto;

/**
 * Representa os fantasmas inimigos com renderização em Pixel-Art autêntica (14x14)
 * e Inteligência Artificial baseada em Busca em Largura (BFS).
 */
public class Fantasma extends Entidade {

    public enum Personalidade {
        PERSEGUIDOR, // Vermelho: segue o menor caminho direto até o Pac-Man
        EMBOSCADOR,  // Verde: mira 4 células à frente da direção do Pac-Man
        ESTRATEGISTA,// Azul: tenta cercar pelas diagonais/flancos do Pac-Man
        PATRULHEIRO  // Amarelo: alterna entre caçar o Pac-Man e patrulhar os cantos
    }

    // Sprite base 14x14 em Pixel-Art (Quadro 1 da saia)
    private static final String[] SPRITE_QUADRO_1 = {
        "....######....",
        "..##########..",
        ".############.",
        ".############.",
        "##############",
        "##############",
        "##############",
        "##############",
        "##############",
        "##############",
        "##############",
        "##############",
        "####.####.####",
        "##....##....##"
    };

    // Sprite base 14x14 em Pixel-Art (Quadro 2 da saia)
    private static final String[] SPRITE_QUADRO_2 = {
        "....######....",
        "..##########..",
        ".############.",
        ".############.",
        "##############",
        "##############",
        "##############",
        "##############",
        "##############",
        "##############",
        "##############",
        "##############",
        ".###..##..###.",
        "..##..##..##.."
    };

    private final String nome;
    private final Color cor;
    private final Color corBrilho;
    private final int linhaInicial;
    private final int colunaInicial;
    private final int atrasoSaidaQuadros;
    private final Direcao direcaoSaidaPadrao;
    private final Personalidade personalidade;
    private final PacMan alvoPacMan;
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
        Personalidade personalidade,
        Labirinto labirinto,
        PacMan alvoPacMan
    ) {
        super(
            colunaInicial * Labirinto.TAMANHO_CELULA,
            linhaInicial * Labirinto.TAMANHO_CELULA,
            2, // Divide TAMANHO_CELULA (28) exatamente em 14 passos por célula
            Labirinto.TAMANHO_CELULA,
            labirinto
        );
        this.nome = nome;
        this.cor = cor;
        this.corBrilho = clarearCor(cor);
        this.linhaInicial = linhaInicial;
        this.colunaInicial = colunaInicial;
        this.atrasoSaidaQuadros = atrasoSaidaQuadros;
        this.direcaoSaidaPadrao = direcaoSaidaPadrao;
        this.personalidade = personalidade;
        this.alvoPacMan = alvoPacMan;
        this.random = new Random();

        this.naJaula = estaEmCoordenadaDaJaula(linhaInicial, colunaInicial);
        this.direcaoAtual = naJaula ? Direcao.PARADO : direcaoSaidaPadrao;
        this.contadorAnimacaoSaia = 0;
        this.quadrosDecorridos = 0;
    }

    private Color clarearCor(Color base) {
        return new Color(
            Math.min(255, base.getRed() + 70),
            Math.min(255, base.getGreen() + 70),
            Math.min(255, base.getBlue() + 70)
        );
    }

    private boolean estaEmCoordenadaDaJaula(int linha, int coluna) {
        return linha >= 9 && linha <= 10 && coluna >= 8 && coluna <= 10;
    }

    public void reiniciarPosicao() {
        setX(colunaInicial * Labirinto.TAMANHO_CELULA);
        setY(linhaInicial * Labirinto.TAMANHO_CELULA);
        this.naJaula = estaEmCoordenadaDaJaula(linhaInicial, colunaInicial);
        this.direcaoAtual = naJaula ? Direcao.PARADO : direcaoSaidaPadrao;
        this.quadrosDecorridos = 0;
    }

    public String getNome() {
        return nome;
    }

    @Override
    public void mover() {
        contadorAnimacaoSaia++;
        quadrosDecorridos++;

        if (naJaula && quadrosDecorridos < atrasoSaidaQuadros) {
            return;
        }

        if (estaAlinhadoNaGrade()) {
            if (naJaula) {
                executarSaidaDaJaula();
            } else {
                escolherDirecaoInteligente();
            }

            if (!podeMoverPara(direcaoAtual)) {
                escolherQualquerDirecaoLivre();
            }
        }

        if (direcaoAtual != Direcao.PARADO) {
            setX(getX() + direcaoAtual.getDx() * getVelocidade());
            setY(getY() + direcaoAtual.getDy() * getVelocidade());
            verificarTunelHorizontal();
        }
    }

    private void executarSaidaDaJaula() {
        int linhaAtual = Math.floorDiv(getY(), Labirinto.TAMANHO_CELULA);
        int colunaAtual = Math.floorDiv(getX(), Labirinto.TAMANHO_CELULA);

        if (linhaAtual <= 8) {
            naJaula = false;
            if (podeMoverPara(direcaoSaidaPadrao)) {
                direcaoAtual = direcaoSaidaPadrao;
            } else {
                escolherDirecaoInteligente();
            }
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

    private void escolherDirecaoInteligente() {
        int linhaAtual = Math.floorDiv(getY(), Labirinto.TAMANHO_CELULA);
        int colunaAtual = Math.floorMod(Math.floorDiv(getX(), Labirinto.TAMANHO_CELULA), Labirinto.COLUNAS);

        Direcao[] todas = {Direcao.CIMA, Direcao.BAIXO, Direcao.ESQUERDA, Direcao.DIREITA};
        List<Direcao> opcoesValidas = new ArrayList<>();

        for (Direcao dir : todas) {
            int proxLinha = linhaAtual + dir.getDy();
            int proxColuna = Math.floorMod(colunaAtual + dir.getDx(), Labirinto.COLUNAS);

            if (estaEmCoordenadaDaJaula(proxLinha, proxColuna)) {
                continue;
            }

            if (podeMoverPara(dir) && !ehDirecaoOposta(direcaoAtual, dir)) {
                opcoesValidas.add(dir);
            }
        }

        if (opcoesValidas.isEmpty()) {
            escolherQualquerDirecaoLivre();
            return;
        }

        if (opcoesValidas.size() == 1) {
            direcaoAtual = opcoesValidas.get(0);
            return;
        }

        int[] alvo = calcularCelulaAlvo(linhaAtual, colunaAtual);

        if (random.nextDouble() < 0.15) {
            direcaoAtual = opcoesValidas.get(random.nextInt(opcoesValidas.size()));
            return;
        }

        Direcao melhorDirecao = opcoesValidas.get(0);
        int menorPassos = Integer.MAX_VALUE;

        for (Direcao dir : opcoesValidas) {
            int proxLinha = linhaAtual + dir.getDy();
            int proxColuna = Math.floorMod(colunaAtual + dir.getDx(), Labirinto.COLUNAS);
            int passos = calcularMenorDistanciaBFS(proxLinha, proxColuna, alvo[0], alvo[1], linhaAtual, colunaAtual);
            if (passos < menorPassos) {
                menorPassos = passos;
                melhorDirecao = dir;
            }
        }

        direcaoAtual = melhorDirecao;
    }

    private int[] calcularCelulaAlvo(int minhaLinha, int minhaColuna) {
        if (alvoPacMan == null) {
            return new int[]{1, 1};
        }

        int pacLinha = alvoPacMan.getLinhaCentro();
        int pacColuna = alvoPacMan.getColunaCentro();
        Direcao pacDir = alvoPacMan.getDirecaoAtual();

        return switch (personalidade) {
            case PERSEGUIDOR -> new int[]{pacLinha, pacColuna};
            case EMBOSCADOR -> new int[]{
                limitarLinha(pacLinha + pacDir.getDy() * 4),
                limitarColuna(pacColuna + pacDir.getDx() * 4)
            };
            case ESTRATEGISTA -> new int[]{
                limitarLinha(pacLinha + pacDir.getDy() * 2 + (pacColuna > 9 ? -2 : 2)),
                limitarColuna(pacColuna + pacDir.getDx() * 2 + (pacLinha > 11 ? -2 : 2))
            };
            case PATRULHEIRO -> {
                double dist = Math.hypot(minhaLinha - pacLinha, minhaColuna - pacColuna);
                if (dist > 6.0) {
                    yield new int[]{pacLinha, pacColuna};
                } else {
                    yield new int[]{Labirinto.LINHAS - 2, 1};
                }
            }
        };
    }

    private int calcularMenorDistanciaBFS(
        int linhaIni,
        int colIni,
        int linhaAlvo,
        int colAlvo,
        int linhaAnterior,
        int colAnterior
    ) {
        if (linhaIni == linhaAlvo && colIni == colAlvo) {
            return 0;
        }

        boolean[][] visitado = new boolean[Labirinto.LINHAS][Labirinto.COLUNAS];
        visitado[linhaAnterior][colAnterior] = true;
        visitado[linhaIni][colIni] = true;

        Queue<int[]> fila = new ArrayDeque<>();
        fila.add(new int[]{linhaIni, colIni, 0});

        Direcao[] direcoes = {Direcao.CIMA, Direcao.BAIXO, Direcao.ESQUERDA, Direcao.DIREITA};

        while (!fila.isEmpty()) {
            int[] atual = fila.poll();
            int l = atual[0];
            int c = atual[1];
            int dist = atual[2];

            if (l == linhaAlvo && c == colAlvo) {
                return dist;
            }

            if (dist >= 32) {
                continue;
            }

            for (Direcao d : direcoes) {
                int nl = l + d.getDy();
                int nc = Math.floorMod(c + d.getDx(), Labirinto.COLUNAS);

                if (nl >= 0 && nl < Labirinto.LINHAS
                    && !visitado[nl][nc]
                    && !estaEmCoordenadaDaJaula(nl, nc)
                    && !getLabirinto().ehParede(nl, nc)) {
                    visitado[nl][nc] = true;
                    fila.add(new int[]{nl, nc, dist + 1});
                }
            }
        }

        return Math.abs(linhaIni - linhaAlvo) + Math.abs(colIni - colAlvo) + 50;
    }

    private void escolherQualquerDirecaoLivre() {
        int linhaAtual = Math.floorDiv(getY(), Labirinto.TAMANHO_CELULA);
        int colunaAtual = Math.floorMod(Math.floorDiv(getX(), Labirinto.TAMANHO_CELULA), Labirinto.COLUNAS);

        Direcao[] todas = {Direcao.CIMA, Direcao.ESQUERDA, Direcao.DIREITA, Direcao.BAIXO};
        for (Direcao dir : todas) {
            int proxLinha = linhaAtual + dir.getDy();
            int proxColuna = Math.floorMod(colunaAtual + dir.getDx(), Labirinto.COLUNAS);
            if (!estaEmCoordenadaDaJaula(proxLinha, proxColuna) && podeMoverPara(dir)) {
                direcaoAtual = dir;
                return;
            }
        }
        direcaoAtual = Direcao.PARADO;
    }

    private int limitarLinha(int linha) {
        return Math.max(1, Math.min(Labirinto.LINHAS - 2, linha));
    }

    private int limitarColuna(int coluna) {
        return Math.max(1, Math.min(Labirinto.COLUNAS - 2, coluna));
    }

    private boolean ehDirecaoOposta(Direcao d1, Direcao d2) {
        return (d1.getDx() + d2.getDx() == 0) && (d1.getDy() + d2.getDy() == 0) && d1 != Direcao.PARADO;
    }

    @Override
    public void desenhar(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        int p = Labirinto.ESCALA_PIXEL; // 2px por "pixel" retrô

        int oscilacaoJaula = (naJaula && quadrosDecorridos < atrasoSaidaQuadros)
            ? ((contadorAnimacaoSaia / 10) % 2 == 0 ? -p : p)
            : 0;

        int baseX = getX();
        int baseY = getY() + oscilacaoJaula;

        String[] sprite = ((contadorAnimacaoSaia / 8) % 2 == 0) ? SPRITE_QUADRO_1 : SPRITE_QUADRO_2;

        // Desenha o corpo do fantasma pixel a pixel (grade 14x14)
        g2d.setColor(cor);
        for (int linha = 0; linha < 14; linha++) {
            String lStr = sprite[linha];
            for (int col = 0; col < 14; col++) {
                if (lStr.charAt(col) == '#') {
                    g2d.fillRect(baseX + col * p, baseY + linha * p, p, p);
                }
            }
        }

        // Brilho retrô no topo esquerdo da cabeça
        g2d.setColor(corBrilho);
        g2d.fillRect(baseX + 3 * p, baseY + 1 * p, 2 * p, p);
        g2d.fillRect(baseX + 2 * p, baseY + 2 * p, p, p);

        // Desenha os 2 olhos em pixel-art (cruz 4x5 pixels cada)
        int dirX = direcaoAtual.getDx();
        int dirY = direcaoAtual.getDy();
        int olhoBaseX = 2 + dirX;
        int olhoBaseY = 3 + (dirY < 0 ? -1 : dirY);

        desenharOlhoPixel(g2d, baseX, baseY, olhoBaseX, olhoBaseY, dirX, dirY, p);
        desenharOlhoPixel(g2d, baseX, baseY, olhoBaseX + 6, olhoBaseY, dirX, dirY, p);
    }

    private void desenharOlhoPixel(
        Graphics2D g2d,
        int baseX,
        int baseY,
        int ox,
        int oy,
        int dirX,
        int dirY,
        int p
    ) {
        // Esclera branca (4x5 em formato oval pixelado)
        g2d.setColor(Color.WHITE);
        g2d.fillRect(baseX + (ox + 1) * p, baseY + oy * p, 2 * p, p);
        g2d.fillRect(baseX + ox * p, baseY + (oy + 1) * p, 4 * p, 3 * p);
        g2d.fillRect(baseX + (ox + 1) * p, baseY + (oy + 4) * p, 2 * p, p);

        // Pupila azul (2x2 pixels direcionada para onde o fantasma olha)
        g2d.setColor(new Color(33, 33, 255));
        int px = ox + 1 + dirX;
        int py = oy + 2 + dirY;
        g2d.fillRect(baseX + px * p, baseY + py * p, 2 * p, 2 * p);
    }
}
