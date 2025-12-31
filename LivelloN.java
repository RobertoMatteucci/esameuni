package com.game;

/**
 * Manages level progression and difficulty settings.
 * Determines how many windows need to be fixed per level.
 */
public class LivelloN {

    private int numeroLivello;
    private int finestreDaAggiustare;

    /**
     * Initializes the level manager.
     * @param numeroLivello The starting level (usually 1).
     */
    public LivelloN(int numeroLivello) {
        this.numeroLivello = numeroLivello;
        this.finestreDaAggiustare = calcolaFinestre(numeroLivello);
    }

    /**
     * Calculates required windows based on level parity.
     * Modified to require only 1 window for faster level progression.
     * @param livello The level number.
     * @return Number of windows to fix.
     */
    private int calcolaFinestre(int livello) {
        return 1;
    }

    public int getNumeroLivello() {
        return numeroLivello;
    }

    public int getFinestreDaAggiustare() {
        return finestreDaAggiustare;
    }

    /**
     * Advances to the next level and recalculates requirements.
     */
    public void prossimoLivello() {
        numeroLivello++;
        finestreDaAggiustare = calcolaFinestre(numeroLivello);
    }

    /**
     * Checks if the level objective is met.
     * @param finestreRiparate Total windows currently fixed.
     * @return true if the level is complete.
     */
    public boolean isCompletato(int finestreRiparate) {
        return finestreRiparate >= finestreDaAggiustare;
    }
}