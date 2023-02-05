package de.teddybear2004.retro.games.minesweeper.exceptions;

public class BombExplodeException extends Exception {

    /**
     * Gets called when a RetroGames Bomb got clicked on.
     */
    public BombExplodeException(String s){
        super(s);
    }
}
