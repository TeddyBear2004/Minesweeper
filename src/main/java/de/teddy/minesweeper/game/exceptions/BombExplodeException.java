package de.teddy.minesweeper.game.exceptions;

public class BombExplodeException extends Exception {

    /**
     * Gets called when a Minesweeper Bomb got clicked on.
     */
    public BombExplodeException(String s){
        super(s);
    }
}
