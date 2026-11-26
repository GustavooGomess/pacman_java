# Pac-Man 2D em Java

Projeto acadêmico desenvolvido em dupla, utilizando Java nativo, com o objetivo de aplicar e demonstrar os principais conceitos da Programação Orientada a Objetos (POO) por meio da implementação de um jogo 2D inspirado no clássico Pac-Man.

## Visão geral

Este projeto consiste no planejamento e desenvolvimento de um jogo 2D inspirado em Pac-Man, implementado em Java, utilizando recursos nativos da linguagem e bibliotecas gráficas padrão da plataforma, como Swing e AWT.

O principal objetivo acadêmico é consolidar conhecimentos de programação e, especialmente, demonstrar como os quatro pilares da Programação Orientada a Objetos podem orientar a organização de um software interativo. O jogo servirá como estudo prático de modelagem de entidades, separação de responsabilidades, controle de estado, interação entre objetos e atualização contínua de uma aplicação gráfica.

O desenvolvimento será realizado em dupla. Como o projeto ainda está em fase de planejamento, as classes, responsabilidades e decisões arquiteturais descritas neste documento representam uma proposta inicial e poderão ser ajustadas durante a implementação.

## Objetivos do projeto

- Desenvolver um jogo 2D funcional inspirado na mecânica clássica de Pac-Man.
- Aplicar abstração, herança, encapsulamento e polimorfismo em uma estrutura orientada a objetos.
- Representar o labirinto por meio de uma matriz bidimensional.
- Implementar movimentação, colisões, pontuação e gerenciamento de vidas.
- Utilizar Java Swing/AWT para desenhar os elementos e receber entradas do jogador.
- Praticar organização de código, divisão de responsabilidades e trabalho colaborativo.

## Conceitos de Programação Orientada a Objetos

A POO será o principal fundamento da arquitetura do projeto. A proposta é representar os elementos do jogo como objetos que possuem estado e comportamento, evitando concentrar toda a lógica em uma única classe.

### 1. Abstração

A abstração será aplicada por meio de uma classe-base que represente as características comuns às entidades que participam do jogo.

Uma possível classe abstrata `Entidade` poderá reunir atributos compartilhados, como:

- posição no cenário (`x` e `y`);
- dimensões ou tamanho do elemento;
- velocidade de movimentação;
- métodos comuns para atualização e desenho.

A classe não precisará representar um personagem específico. Sua finalidade será definir uma estrutura comum e estabelecer comportamentos que as subclasses deverão implementar.

Exemplo conceitual:

```java
public abstract class Entidade {
    private int x;
    private int y;
    private int velocidade;

    public abstract void mover();
    public abstract void desenhar(Graphics g);

    // Getters e setters para os atributos, conforme necessário.
}
```

Assim, detalhes específicos de cada personagem ficam nas classes especializadas, enquanto a classe-base expressa apenas o que é comum a todas as entidades.

### 2. Herança

A herança permitirá reutilizar a estrutura definida em `Entidade` e especializar seu comportamento nas classes dos personagens.

A partir da classe-base, poderão ser criadas classes como:

- **`PacMan`**: representa o personagem controlado pelo jogador. Poderá armazenar a direção atual, processar comandos do teclado e consumir itens do labirinto.
- **`Fantasma`**: representa os inimigos. Poderá conter informações próprias de perseguição, direção e estratégia de movimentação.

Exemplo conceitual:

```java
public class PacMan extends Entidade {
    @Override
    public void mover() {
        // Movimentação controlada pelas entradas do jogador.
    }

    @Override
    public void desenhar(Graphics g) {
        // Renderização específica do Pac-Man.
    }
}

public class Fantasma extends Entidade {
    @Override
    public void mover() {
        // Lógica de movimentação do fantasma.
    }

    @Override
    public void desenhar(Graphics g) {
        // Renderização específica do fantasma.
    }
}
```

Com essa estrutura, as subclasses aproveitam os atributos e o contrato definidos pela classe-base, mas implementam suas próprias regras. Isso reduz duplicação e facilita a inclusão de novos tipos de entidades.

### 3. Encapsulamento

O encapsulamento será utilizado para proteger o estado interno dos objetos e controlar como seus dados poderão ser consultados ou modificados.

Atributos como coordenadas, velocidade, pontuação e quantidade de vidas deverão ter visibilidade restrita — normalmente `private` — e ser acessados por métodos públicos quando necessário.

Por exemplo:

- `PacMan` controla sua própria posição e direção, sem permitir alterações arbitrárias por outras classes.
- Uma classe responsável pelo estado da partida, como `GerenciadorJogo`, controla a pontuação e as vidas por meio de operações específicas.
- A matriz do labirinto não deve ser modificada livremente por qualquer componente; alterações, como remover uma pastilha, devem ocorrer por métodos que validem a operação.

Exemplo conceitual:

```java
public class EstadoJogo {
    private int pontuacao;
    private int vidas;

    public int getPontuacao() {
        return pontuacao;
    }

    public int getVidas() {
        return vidas;
    }

    public void adicionarPontos(int quantidade) {
        if (quantidade > 0) {
            pontuacao += quantidade;
        }
    }

    public void perderVida() {
        if (vidas > 0) {
            vidas--;
        }
    }
}
```

Dessa forma, as regras de alteração ficam centralizadas, ajudando a preservar a consistência do estado da partida e reduzindo dependências entre as classes.

### 4. Polimorfismo

O polimorfismo permitirá tratar diferentes personagens de maneira uniforme por meio do tipo comum `Entidade`, mantendo comportamentos específicos em cada implementação.

Por exemplo, uma coleção de entidades poderá conter o Pac-Man e vários fantasmas. Durante a atualização do jogo, cada objeto responderá à chamada de `mover()` de acordo com sua própria implementação. O mesmo princípio será aplicado à renderização com `desenhar(Graphics g)`.

Exemplo conceitual:

```java
List<Entidade> entidades = new ArrayList<>();

// A lista poderá armazenar instâncias de PacMan e Fantasma.

for (Entidade entidade : entidades) {
    entidade.mover();
    entidade.desenhar(g);
}
```

Embora as chamadas sejam feitas por meio do tipo `Entidade`, o comportamento executado dependerá do tipo real do objeto. Assim, o Pac-Man poderá responder aos comandos do jogador, enquanto os fantasmas executarão suas próprias estratégias. A renderização também poderá variar conforme o personagem.

Esse modelo facilita a manutenção e a expansão do jogo, pois novas entidades poderão ser adicionadas sem exigir mudanças na estrutura geral do loop, desde que respeitem o contrato definido pela classe-base.

## Arquitetura planejada

A arquitetura inicial será dividida em componentes com responsabilidades bem definidas. A organização abaixo é uma proposta e poderá evoluir conforme as necessidades identificadas durante o desenvolvimento.

### Labirinto com matriz bidimensional

O cenário será representado por uma matriz 2D, na qual cada posição corresponderá a uma célula do labirinto. Os valores armazenados indicarão o conteúdo daquela célula, por exemplo:

| Valor simbólico | Significado              |
| --------------- | ------------------------ |
| `PAREDE`        | Bloqueia a passagem      |
| `VAZIO`         | Caminho livre            |
| `PASTILHA`      | Item que pode ser coletado |
| `POWER_UP`      | Item especial            |

A matriz permitirá consultar posições, verificar colisões e identificar itens disponíveis. A movimentação dos personagens deverá considerar os limites do cenário e impedir que atravessem células bloqueadas.

Uma representação inicial poderia utilizar constantes ou um `enum` para tornar os tipos de célula mais claros e evitar números mágicos espalhados pelo código.

### Game Loop com Java Swing/AWT

A execução será organizada em um Game Loop, responsável por manter o ciclo contínuo de atualização e desenho do jogo.

De forma simplificada, cada ciclo deverá:

1. Capturar e processar as entradas do jogador, como as teclas direcionais.
2. Atualizar a lógica das entidades, incluindo movimentação e comportamento dos fantasmas.
3. Verificar colisões com paredes, itens e personagens.
4. Atualizar pontuação, vidas e demais condições da partida.
5. Solicitar a renderização do cenário e dos personagens.

O desenho poderá ser implementado em um componente Swing, como `JPanel`, sobrescrevendo `paintComponent(Graphics g)`. A atualização periódica poderá ser controlada por `javax.swing.Timer`, mantendo a interface responsiva e integrando o ciclo à infraestrutura de eventos do Swing.

O AWT fornecerá recursos gráficos e de entrada, como `Graphics` e eventos de teclado. A implementação deverá evitar executar operações demoradas na Event Dispatch Thread (EDT), pois isso pode deixar a interface sem resposta.

### Organização inicial de classes

A estrutura abaixo é uma sugestão de organização, não uma definição final:

```text
src/
└── main/
    └── java/
        └── jogo/
            ├── Main.java
            ├── core/
            │   └── GerenciadorJogo.java
            ├── entidades/
            │   ├── Entidade.java
            │   ├── PacMan.java
            │   └── Fantasma.java
            ├── mapa/
            │   └── Labirinto.java
            └── ui/
                └── TelaJogo.java
```

### Responsabilidades previstas

| Classe            | Responsabilidade                                   |
| ----------------- | -------------------------------------------------- |
| `Main`            | Inicializar a aplicação                            |
| `GerenciadorJogo` | Coordenar o estado e o fluxo da partida            |
| `Entidade`        | Definir a estrutura comum das entidades            |
| `PacMan`          | Implementar o personagem controlado pelo jogador   |
| `Fantasma`        | Implementar os inimigos e seus comportamentos      |
| `Labirinto`       | Armazenar e consultar a matriz do cenário          |
| `TelaJogo`        | Exibir o jogo e receber entradas da interface      |

## Tecnologias previstas

- **Linguagem:** Java.
- **Interface gráfica:** Java Swing.
- **Recursos gráficos e eventos:** AWT.
- **Paradigma principal:** Programação Orientada a Objetos.
- **Representação do cenário:** matriz bidimensional.

Não estão definidos, neste estágio, frameworks externos, ferramentas de build ou versões específicas do Java. Essas escolhas poderão ser documentadas quando forem estabelecidas pela dupla.

## Status do projeto

<sub>Em planejamento — arquitetura e responsabilidades iniciais em definição.</sub>

As funcionalidades, regras do jogo, estrutura de pacotes e decisões de implementação serão detalhadas e atualizadas à medida que o projeto avançar.

## Equipe

● Gustavo Henrique Gouveia Gomes
● Nicolle Carolina da Rocha

<!-- Adicionar aqui os nomes dos integrantes, a disciplina, a instituição e o semestre, quando definidos. -->
