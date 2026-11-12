package jogo.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

/**
 * Gerenciador de áudio 16-bit polifônico com síntese suave (melodia + baixo + envelope ADSR).
 * Gera trilhas contínuas sem engasgos entre as notas.
 */
public class GerenciadorSom {
    private static final float TAXA_AMOSTRAGEM = 44100f;
    private static final AudioFormat FORMATO_16BIT = new AudioFormat(TAXA_AMOSTRAGEM, 16, 1, true, false);

    private boolean alternarWaka = false;
    private volatile boolean musicaFundoAtiva = false;
    private Thread threadMusicaFundo;

    /**
     * Inicia uma trilha musical moderna (synthwave/arcade suave com melodia e linha de baixo)
     * em um único fluxo contínuo de áudio 16-bit para não travar entre as notas.
     */
    public void iniciarMusicaAmbiente() {
        pararMusicaAmbiente();
        musicaFundoAtiva = true;

        threadMusicaFundo = new Thread(() -> {
            // Sequência melódica inspirada no tema clássico em versão synth moderna
            // {frequenciaMelodiaHz, frequenciaBaixoHz, duracaoMs}
            double[][] compasso = {
                // Frase 1 (Si maior / Mi menor synth)
                {493.88, 123.47, 150}, {987.77, 123.47, 150}, {739.99, 185.00, 150}, {622.25, 155.56, 150},
                {987.77, 123.47, 100}, {739.99, 185.00, 200}, {622.25, 155.56, 300},
                // Frase 2 (Dó maior synth)
                {523.25, 130.81, 150}, {1046.50, 130.81, 150}, {783.99, 196.00, 150}, {659.25, 164.81, 150},
                {1046.50, 130.81, 100}, {783.99, 196.00, 200}, {659.25, 164.81, 300},
                // Frase 3 (Si maior synth)
                {493.88, 123.47, 150}, {987.77, 123.47, 150}, {739.99, 185.00, 150}, {622.25, 155.56, 150},
                {987.77, 123.47, 100}, {739.99, 185.00, 200}, {622.25, 155.56, 300},
                // Subida final suave
                {622.25, 155.56, 100}, {659.25, 164.81, 100}, {698.46, 174.61, 150},
                {698.46, 174.61, 100}, {739.99, 185.00, 100}, {783.99, 196.00, 150},
                {783.99, 196.00, 100}, {880.00, 220.00, 150}, {987.77, 246.94, 350},
                // Groove de transição suave
                {493.88, 123.47, 200}, {587.33, 146.83, 200}, {659.25, 164.81, 200}, {587.33, 146.83, 200},
                {493.88, 123.47, 200}, {440.00, 110.00, 200}, {392.00, 98.00, 400}
            };

            try (SourceDataLine linha = AudioSystem.getSourceDataLine(FORMATO_16BIT)) {
                linha.open(FORMATO_16BIT, 8820);
                linha.start();

                double faseMelodia = 0.0;
                double faseBaixo = 0.0;

                while (musicaFundoAtiva) {
                    for (double[] nota : compasso) {
                        if (!musicaFundoAtiva) {
                            break;
                        }
                        double freqMelodia = nota[0];
                        double freqBaixo = nota[1];
                        int duracaoMs = (int) nota[2];

                        int amostras = (int) ((duracaoMs / 1000.0) * TAXA_AMOSTRAGEM);
                        byte[] buffer = new byte[amostras * 2];

                        for (int i = 0; i < amostras; i++) {
                            double t = (double) i / amostras;

                            // Envelope ADSR suave para som moderno e sem estalos
                            double envelopeMelodia;
                            if (t < 0.08) {
                                envelopeMelodia = t / 0.08;
                            } else if (t < 0.35) {
                                envelopeMelodia = 1.0 - 0.25 * ((t - 0.08) / 0.27);
                            } else {
                                envelopeMelodia = 0.75 * (1.0 - ((t - 0.35) / 0.65));
                            }

                            double envelopeBaixo = 1.0 - (0.5 * t);

                            faseMelodia += freqMelodia / TAXA_AMOSTRAGEM;
                            faseBaixo += freqBaixo / TAXA_AMOSTRAGEM;

                            // Timbre Synth moderno (combinação de senoides e harmônicos suaves)
                            double anguloM = 2.0 * Math.PI * faseMelodia;
                            double vozMelodia = (Math.sin(anguloM) * 0.65
                                + Math.sin(anguloM * 2.0) * 0.25
                                + Math.sin(anguloM * 3.0) * 0.10) * envelopeMelodia;

                            double anguloB = 2.0 * Math.PI * faseBaixo;
                            double vozBaixo = (Math.sin(anguloB) * 0.75
                                + Math.sin(anguloB * 2.0) * 0.25) * envelopeBaixo;

                            double mix = (vozMelodia * 0.14) + (vozBaixo * 0.12);
                            escreverAmostra16Bit(buffer, i * 2, mix);
                        }

                        linha.write(buffer, 0, buffer.length);
                    }
                }
                linha.drain();
            } catch (Exception ignored) {
                // Ignora caso o dispositivo de áudio não esteja disponível
            }
        });

        threadMusicaFundo.setDaemon(true);
        threadMusicaFundo.start();
    }

    public void pararMusicaAmbiente() {
        musicaFundoAtiva = false;
    }

    /**
     * Toca o efeito "Waka" suave e rápido ao comer uma pastilha.
     */
    public void tocarWaka() {
        final boolean subir = alternarWaka;
        alternarWaka = !alternarWaka;

        executarAssincrono(() -> {
            if (subir) {
                tocarSweepSuave(320, 580, 50, 0.18);
            } else {
                tocarSweepSuave(580, 320, 50, 0.18);
            }
        });
    }

    /**
     * Toca efeito moderno ao coletar Power-Up.
     */
    public void tocarPowerUp() {
        executarAssincrono(() -> tocarSweepSuave(350, 920, 160, 0.24));
    }

    /**
     * Toca o efeito clássico descendente de morte do Pac-Man ("wah-wah-wah-plop").
     */
    public void tocarMorte() {
        executarAssincrono(() -> {
            try (SourceDataLine linha = AudioSystem.getSourceDataLine(FORMATO_16BIT)) {
                linha.open(FORMATO_16BIT, 8820);
                linha.start();

                double[][] descidas = {
                    {680, 480, 140},
                    {600, 400, 140},
                    {520, 330, 140},
                    {440, 260, 150},
                    {360, 180, 160},
                    {260, 95, 250}
                };

                double fase = 0.0;
                for (double[] trecho : descidas) {
                    double fIni = trecho[0];
                    double fFim = trecho[1];
                    int duracaoMs = (int) trecho[2];
                    int amostras = (int) ((duracaoMs / 1000.0) * TAXA_AMOSTRAGEM);
                    byte[] buffer = new byte[amostras * 2];

                    for (int i = 0; i < amostras; i++) {
                        double p = (double) i / amostras;
                        double freq = fIni + (fFim - fIni) * p;
                        // Vibrato clássico da morte do Pac-Man
                        freq += Math.sin(p * Math.PI * 6.0) * 18.0;
                        fase += freq / TAXA_AMOSTRAGEM;

                        double env = Math.sin(p * Math.PI);
                        double ang = 2.0 * Math.PI * fase;
                        double sinal = (Math.sin(ang) * 0.6 + Math.sin(ang * 2.0) * 0.3 + Math.sin(ang * 3.0) * 0.1) * env * 0.28;
                        escreverAmostra16Bit(buffer, i * 2, sinal);
                    }
                    linha.write(buffer, 0, buffer.length);
                }
                linha.drain();
            } catch (Exception ignored) {
            }
        });
    }

    /**
     * Toca fanfarra quando todas as bolinhas são comidas.
     */
    public void tocarVitoria() {
        pararMusicaAmbiente();
        executarAssincrono(() -> {
            try (SourceDataLine linha = AudioSystem.getSourceDataLine(FORMATO_16BIT)) {
                linha.open(FORMATO_16BIT, 8820);
                linha.start();

                double[][] notas = {
                    {523.25, 130}, {659.25, 130}, {783.99, 130}, {1046.50, 260},
                    {783.99, 130}, {1046.50, 420}
                };
                double fase = 0.0;
                for (double[] nota : notas) {
                    int amostras = (int) ((nota[1] / 1000.0) * TAXA_AMOSTRAGEM);
                    byte[] buffer = new byte[amostras * 2];
                    for (int i = 0; i < amostras; i++) {
                        double p = (double) i / amostras;
                        double env = 1.0 - (p * 0.6);
                        fase += nota[0] / TAXA_AMOSTRAGEM;
                        double ang = 2.0 * Math.PI * fase;
                        double sinal = (Math.sin(ang) * 0.7 + Math.sin(ang * 2.0) * 0.3) * env * 0.25;
                        escreverAmostra16Bit(buffer, i * 2, sinal);
                    }
                    linha.write(buffer, 0, buffer.length);
                }
                linha.drain();
            } catch (Exception ignored) {
            }
        });
    }

    private void tocarSweepSuave(double freqInicial, double freqFinal, int duracaoMs, double volume) {
        int totalAmostras = (int) ((duracaoMs / 1000.0) * TAXA_AMOSTRAGEM);
        byte[] buffer = new byte[totalAmostras * 2];
        double fase = 0.0;

        for (int i = 0; i < totalAmostras; i++) {
            double p = (double) i / totalAmostras;
            double freq = freqInicial + (freqFinal - freqInicial) * p;
            fase += freq / TAXA_AMOSTRAGEM;

            double env = Math.sin(p * Math.PI);
            double ang = 2.0 * Math.PI * fase;
            double onda = (Math.sin(ang) * 0.75 + Math.sin(ang * 2.0) * 0.25) * volume * env;
            escreverAmostra16Bit(buffer, i * 2, onda);
        }

        try (SourceDataLine linha = AudioSystem.getSourceDataLine(FORMATO_16BIT)) {
            linha.open(FORMATO_16BIT, buffer.length);
            linha.start();
            linha.write(buffer, 0, buffer.length);
            linha.drain();
        } catch (Exception ignored) {
        }
    }

    private void escreverAmostra16Bit(byte[] buffer, int indiceByte, double valorNormalizado) {
        double limitado = Math.max(-1.0, Math.min(1.0, valorNormalizado));
        short amostra = (short) (limitado * Short.MAX_VALUE);
        buffer[indiceByte] = (byte) (amostra & 0xFF);
        buffer[indiceByte + 1] = (byte) ((amostra >> 8) & 0xFF);
    }

    private void executarAssincrono(Runnable tarefa) {
        Thread thread = new Thread(tarefa);
        thread.setDaemon(true);
        thread.start();
    }
}
