package de.teddy.minesweeper.game;

import de.teddy.minesweeper.game.exceptions.BombExplodeException;

import java.util.ArrayList;
import java.util.List;

public class SurfaceDiscoverer {
    public static void uncoverFields(Board board, int width, int height) throws BombExplodeException{
        uncoverFields(board, width, height, new ArrayList<>());
    }

    public static void uncoverFieldsNextToNumber(Board board, int width, int height) throws BombExplodeException{
        if((board.getBoard().length <= width
                || board.getBoard()[0].length <= height)
                && (0 >= width
                || 0 >= height))
            throw new IllegalArgumentException();

        if(!board.getBoard()[width][height].isCovered()){
            int markedFields = 0;

            for(int i = -1; i < 2; i++){
                for(int j = -1; j < 2; j++){
                    if(!(i == 0 && j == 0)){
                        try{
                            if(board.getBoard()[width + i][height + j].isMarked())
                                markedFields++;
                        }catch(ArrayIndexOutOfBoundsException ignore){
                        }
                    }
                }
            }

            if(board.getBoard()[width][height].getNeighborCount() == markedFields){
                for(int i = -1; i < 2; i++){
                    for(int j = -1; j < 2; j++){
                        if(!(i == 0 && j == 0)){
                            try{
                                if(!board.getBoard()[width + i][height + j].isMarked()){
                                    uncoverFields(board, width + i, height + j);
                                }
                            }catch(ArrayIndexOutOfBoundsException ignored){
                            }
                        }
                    }
                }
            }
        }
    }

    private static void uncoverFields(Board board, int width, int height, List<String> strings) throws BombExplodeException{
        if(board.getBoard().length <= width || board.getBoard()[0].length <= height)
            throw new IllegalArgumentException();

        Board.Field field = board.getField(width, height);
        field.setUncover();

        if(field.isBomb())
            throw new BombExplodeException("Bomb at " + width + " and " + height + " is exploded.");

        if(field.getNeighborCount() == 0){
            for(int i = -1; i < 2; i++){
                for(int j = -1; j < 2; j++){
                    if(!(i == 0 && j == 0)){
                        try{
                            String s = String.format("%d/%d", width + i, height + j);
                            if(board.getBoard()[width + i][height + j].isCovered() && !strings.contains(s)){
                                strings.add(s);
                                uncoverFields(board, width + i, height + j, strings);
                            }
                        }catch(ArrayIndexOutOfBoundsException ignore){
                        }
                    }
                }
            }
        }
    }
}
