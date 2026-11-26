# Guia de Orientação a Objetos (POO) no Pac-Man Java

Este documento detalha como os quatro pilares fundamentais da Programação Orientada a Objetos (**Abstração**, **Encapsulamento**, **Herança** e **Polimorfismo**) foram concebidos e implementados na arquitetura do jogo Pac-Man desenvolvido em Java.
 
---

## Sumário
1. [Visão Geral da Arquitetura](#visão-geral-da-arquitetura)
2. [1. Abstração](#1-abstração)
   - [O que é e onde está no projeto](#o-que-é-e-onde-está-no-projeto)
   - [Trechos de Código Explicados](#trechos-de-código-explicados---abstração)
3. [2. Encapsulamento](#2-encapsulamento)
   - [O que é e onde está no projeto](#o-que-é-e-onde-está-no-projeto-1)
   - [Trechos de Código Explicados](#trechos-de-código-explicados---encapsulamento)
4. [3. Herança](#3-herança)
   - [O que é e onde está no projeto](#o-que-é-e-onde-está-no-projeto-2)
   - [Trechos de Código Explicados](#trechos-de-código-explicados---herança)
5. [4. Polimorfismo](#4-polimorfismo)
   - [O que é e onde está no projeto](#o-que-é-e-onde-está-no-projeto-3)
   - [Trechos de Código Explicados](#trechos-de-código-explicados---polimorfismo)
6. [Resumo das Classes e Responsabilidades](#resumo-das-classes-e-responsabilidades)

---

## Visão Geral da Arquitetura

O projeto foi dividido em pacotes bem definidos para separar regras de negócio, dados, interface gráfica e áudio:

- `jogo.entidades`: Classes de personagens móveis (`Entidade`, `PacMan`, `Fantasma`, `Direcao`).
- `jogo.mapa`: Matriz do labirinto, blocos e tipos de células (`Labirinto`, `TipoCelula`).
- `jogo.core`: Gerenciamento do ciclo da partida, pontuação e colisões (`GerenciadorJogo`).
- `jogo.ui`: Renderização na janela e captura de eventos do teclado (`TelaJogo`, `Main`).
- `jogo.audio`: Síntese e reprodução de efeitos sonoros e música de fundo (`GerenciadorSom`).

---

## 1. Abstração

### O que é e onde está no projeto
A **Abstração** consiste em isolar as características essenciais de um objeto ou entidade do mundo real/jogo, ocultando detalhes complexos e irrelevantes para quem apenas precisa utilizá-los. 

No projeto, a abstração está aplicada principalmente em:
1. **Classe Abstrata [`Entidade`](file:///c:/Users/gustavo.gomes/Documents/PacMan%20-%20Java/pacman_java/src/jogo/entidades/Entidade.java):** Modela o conceito genérico de qualquer objeto que se movimenta e é desenhado no labirinto. Nem `PacMan` nem `Fantasma` precisam redefinir o que é ter uma posição no mapa ou como calcular se estão alinhados à grade.
2. **Métodos Abstratos (`mover()` e `desenhar()`):** Toda entidade no jogo precisa se mover e ser desenhada, mas a classe abstrata `Entidade` não define **como** isso acontece — ela delega essa responsabilidade para as classes concretas.
3. **Enumerações Abstratas com Estado ([`Direcao`](file:///c:/Users/gustavo.gomes/Documents/PacMan%20-%20Java/pacman_java/src/jogo/entidades/Direcao.java) e [`TipoCelula`](file:///c:/Users/gustavo.gomes/Documents/PacMan%20-%20Java/pacman_java/src/jogo/mapa/TipoCelula.java)):** Abstraem vetores matemáticos `(dx, dy)` e pontuações de itens sem expor números mágicos espalhados pelo código.

### Trechos de Código Explicados - Abstração

#### A classe abstrata `Entidade`
```java
// Local: src/jogo/entidades/Entidade.java
public abstract class Entidade {
    // Atributos essenciais compartilhados
    private int x;
    private int y;
    private int velocidade;
    private int tamanho;
    private final Labirinto labirinto;

    // Contrato abstrato: quem herdar É OBRIGADO a implementar suas regras de movimento e desenho
    public abstract void mover();
    public abstract void desenhar(Graphics g);

    // Comportamentos concretos abstraídos e reutilizados
    protected boolean estaAlinhadoNaGrade() {
        return Math.floorMod(x, Labirinto.TAMANHO_CELULA) == 0
            && Math.floorMod(y, Labirinto.TAMANHO_CELULA) == 0;
    }
}
```

#### A abstração de Direção
Em vez de manipular coordenadas manuais `+1` ou `-1` na mão:
```java
// Local: src/jogo/entidades/Direcao.java
public enum Direcao {
    PARADO(0, 0, 0),
    DIREITA(1, 0, 0),
    CIMA(0, -1, 90),
    ESQUERDA(-1, 0, 180),
    BAIXO(0, 1, 270);

    private final int dx;
    private final int dy;
    private final int anguloBase;
    // ...
}
```

---

## 2. Encapsulamento

### O que é e onde está no projeto
O **Encapsulamento** é o princípio de proteger os dados internos de uma classe contra acessos externos indevidos ou modificações descontroladas, expondo apenas métodos públicos seguros (`getters`, `setters` e métodos de negócio).

No projeto, o encapsulamento é garantido por:
1. **Modificadores de Acesso (`private` e `protected`):** Atributos como as coordenadas `x, y`, vidas, pontuação e a matriz do labirinto são estritamente `private`.
2. **Imutabilidade e Validação de Estado:** Métodos como `alterarDirecaoPacMan(Direcao)` impedem que o jogador mude a rota se o Pac-Man estiver morrendo ou se o jogo estiver em Game Over.
3. **Ocultação de Lógica Complexa:** Na classe [`Labirinto`](file:///c:/Users/gustavo.gomes/Documents/PacMan%20-%20Java/pacman_java/src/jogo/mapa/Labirinto.java), a matriz `grade[22][19]` é privada. Nenhuma outra classe pode alterar diretamente uma célula da matriz. A coleta de itens é feita através do método encapsulado `coletarItem(linha, coluna)`, que remove o item, decrementa o contador e retorna o tipo do item coletado.

### Trechos de Código Explicados - Encapsulamento

#### Proteção do Labirinto e Coleta de Itens
```java
// Local: src/jogo/mapa/Labirinto.java
public class Labirinto {
    // Matriz protegida: ninguém de fora altera a estrutura do mapa diretamente
    private final TipoCelula[][] grade;
    private int pastilhasRestantes;

    // Método seguro que encapsula a leitura, remoção da pastilha e contagem
    public TipoCelula coletarItem(int linha, int coluna) {
        if (!posicaoValida(linha, coluna)) {
            return TipoCelula.VAZIO;
        }
        TipoCelula celula = grade[linha][coluna];
        if (celula == TipoCelula.PASTILHA || celula == TipoCelula.POWER_UP) {
            grade[linha][coluna] = TipoCelula.VAZIO; // Consome o item
            pastilhasRestantes--;
            return celula;
        }
        return TipoCelula.VAZIO;
    }
}
```

#### Proteção do estado no `GerenciadorJogo`
```java
// Local: src/jogo/core/GerenciadorJogo.java
public class GerenciadorJogo {
    private int pontuacao;
    private int recorde;
    private int vidas;
    private boolean gameOver;

    // Métodos de alteração protegidos contra valores negativos e inconsistências
    private void adicionarPontos(int quantidade) {
        if (quantidade > 0) {
            this.pontuacao += quantidade;
            if (this.pontuacao > this.recorde) {
                this.recorde = this.pontuacao;
            }
        }
    }

    // O mundo externo só pode LER a pontuação, nunca alterá-la diretamente
    public int getPontuacao() {
        return pontuacao;
    }
}
```

---

## 3. Herança

### O que é e onde está no projeto
A **Herança** permite que classes filhas reutilizem propriedades (atributos) e comportamentos (métodos) de uma classe pai (superclasse), evitando duplicação de código e estabelecendo uma relação do tipo **"É UM"** (*is-a*).

No projeto:
- `PacMan` **é uma** `Entidade`.
- `Fantasma` **é uma** `Entidade`.

Ambas herdam de `Entidade`:
- Os atributos: `x`, `y`, `velocidade`, `tamanho`, `labirinto`.
- Os métodos reutilizáveis: `estaAlinhadoNaGrade()`, `podeMoverPara(direcao)`, `verificarTunelHorizontal()`, `getLinhaCentro()`, `getColunaCentro()`.
- O construtor base é reutilizado através de `super(...)`.

### Trechos de Código Explicados - Herança

#### Herança em `PacMan`
```java
// Local: src/jogo/entidades/PacMan.java
public class PacMan extends Entidade {
    
    public PacMan(int linhaInicial, int colunaInicial, Labirinto labirinto) {
        // Invoca o construtor da superclasse Entidade reaproveitando toda a inicialização
        super(
            colunaInicial * Labirinto.TAMANHO_CELULA,
            linhaInicial * Labirinto.TAMANHO_CELULA,
            4, // Velocidade específica do Pac-Man
            Labirinto.TAMANHO_CELULA,
            labirinto
        );
    }
    // Herda automaticamente verificarTunelHorizontal(), getX(), getY(), etc.
}
```

#### Herança em `Fantasma`
```java
// Local: src/jogo/entidades/Fantasma.java
public class Fantasma extends Entidade {

    public Fantasma(String nome, Color cor, int linhaInicial, int colunaInicial, /* ... */) {
        super(
            colunaInicial * Labirinto.TAMANHO_CELULA,
            linhaInicial * Labirinto.TAMANHO_CELULA,
            2, // Fantasmas possuem velocidade base 2
            Labirinto.TAMANHO_CELULA,
            labirinto
        );
        // Atributos específicos da especialização do Fantasma
        this.nome = nome;
        this.cor = cor;
        // ...
    }
}
```

---

## 4. Polimorfismo

### O que é e onde está no projeto
O **Polimorfismo** ("muitas formas") é a capacidade de tratar objetos de classes filhas diferentes como se fossem da superclasse, invocando o mesmo método e obtendo comportamentos distintos em tempo de execução (*Dynamic Dispatch / Sobrescrita de Método com `@Override`*).

No projeto, o polimorfismo é aplicado de forma exemplar em:

1. **Lista Heterogênea de Entidades (`List<Entidade>`):**
   No [`GerenciadorJogo`](file:///c:/Users/gustavo.gomes/Documents/PacMan%20-%20Java/pacman_java/src/jogo/core/GerenciadorJogo.java), existe uma lista de entidades:
   ```java
   private final List<Entidade> entidades = new ArrayList<>();
   entidades.add(pacMan);    // PacMan é uma Entidade
   entidades.addAll(fantasmas); // Cada Fantasma é uma Entidade
   ```
2. **Loop Polimórfico de Atualização (`mover()`):**
   O `GerenciadorJogo` não precisa saber se o item da lista é um Pac-Man ou Fantasma:
   ```java
   for (Entidade entidade : entidades) {
       entidade.mover(); // Comportamento polimórfico!
   }
   ```
   - Quando `entidade` for `PacMan`, executa a lógica de checagem de input do jogador, fila de curva e rotação de boca.
   - Quando `entidade` for `Fantasma`, executa o algoritmo BFS de busca de rota, saída da jaula e patrulha.
3. **Loop Polimórfico de Renderização (`desenhar(Graphics g)`):**
   ```java
   for (Entidade entidade : entidades) {
       entidade.desenhar(g); // Desenha Pac-Man ou Fantasma dinamicamente
   }
   ```

### Trechos de Código Explicados - Polimorfismo

#### Comparativo da Sobrescrita do Método `mover()`

**No `PacMan`:**
```java
@Override
public void mover() {
    if (morrendo) {
        quadrosMorte++;
        return;
    }

    if (estaAlinhadoNaGrade()) {
        if (podeMoverPara(proximaDirecao)) {
            direcaoAtual = proximaDirecao;
        }
        if (!podeMoverPara(direcaoAtual)) {
            direcaoAtual = Direcao.PARADO;
        }
    }
    // Atualiza x e y baseado na entrada do teclado
    setX(getX() + direcaoAtual.getDx() * getVelocidade());
    setY(getY() + direcaoAtual.getDy() * getVelocidade());
    verificarTunelHorizontal();
}
```

**No `Fantasma`:**
```java
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
            escolherDirecaoInteligente(); // Executa o Algoritmo BFS para caçar o Pac-Man
        }
    }
    // Move o fantasma de acordo com o cálculo da IA
    setX(getX() + direcaoAtual.getDx() * getVelocidade());
    setY(getY() + direcaoAtual.getDy() * getVelocidade());
    verificarTunelHorizontal();
}
```

---

## Resumo das Classes e Responsabilidades

| Conceito de POO | Classe / Estrutura Principal | Como foi aplicado no projeto |
| :--- | :--- | :--- |
| **Abstração** | `Entidade` (abstrata), `Direcao`, `TipoCelula` | Define contratos (`mover`, `desenhar`) e unifica dados sem expor detalhes internos. |
| **Encapsulamento** | Todas as classes (`Labirinto`, `GerenciadorJogo`, etc.) | Atributos privados (`private`), métodos acessores (`get/set`) e regras de validação consistentes. |
| **Herança** | `PacMan extends Entidade`<br>`Fantasma extends Entidade` | Reutilização de código de grade, colisões básicas, dimensões e túneis de teletransporte. |
| **Polimorfismo** | Coleção `List<Entidade>` em `GerenciadorJogo` | Invocação genérica de `mover()` e `desenhar()`, executando a IA nos fantasmas e os controles no Pac-Man. |

---
*Documento gerado como material didático e arquitetural para o projeto Pac-Man Java.*
