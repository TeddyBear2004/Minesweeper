package de.teddy.minesweeper.game;

import de.teddy.minesweeper.Minesweeper;
import de.teddy.minesweeper.util.IsBetween;
import de.teddy.minesweeper.util.Tuple2;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum Game {
    MAP10X10(new Tuple2<>(new Location(Minesweeper.WORLD, 53, 17, -328), new Location(Minesweeper.WORLD, 58, 20, -315.5)),
            10, 10),

    MAP18X18(new Tuple2<>(new Location(Minesweeper.WORLD, 49, 16, -198), new Location(Minesweeper.WORLD, 57, 20, -177.5)),
            18, 40),

    MAP24X24(new Tuple2<>(new Location(Minesweeper.WORLD, 47, 15, -67), new Location(Minesweeper.WORLD, 58, 20, -40.5)),
            24, 99),

    MAP_SPECIAL(new Tuple2<>(new Location(Minesweeper.WORLD, 384, 3, -155),
            new Location(Minesweeper.WORLD, 385, 4, -129)),
            24, 70);

    static{
        for(Game value : values()){
            value.locations.getB().setYaw(-180);
            value.locations.getB().setPitch(30);
        }
    }

    private final Tuple2<Location, Location> locations;
    private final Map<Player, Board> gameWatched;
    private final HashMap<Player, Board> runningGames;
    private final int size;
    private final int bombCount;

    Game(Tuple2<Location, Location> location, int size, int bombCount){
        this.runningGames = new HashMap<>();
        this.gameWatched = new HashMap<>();
        
        this.locations = location;
        this.size = size;
        this.bombCount = bombCount;
    }

    public static Game getGame(Player Player){
        return Arrays.stream(Game.values()).filter(value -> value.runningGames.containsKey(Player)).findFirst().orElse(null);
    }

    public static boolean isBlockInsideGameField(Block block){
        return Arrays.stream(values())
                .anyMatch(game -> IsBetween.isBetween2D(game.locations.getA(), game.size, game.size, block)
                        && (game.locations.getA().getBlockY() == block.getY() || game.locations.getA().getBlockY() +1== block.getY()));
    }

    public Board getBoard(Player Player){
        return runningGames.get(Player);
    }

    public Location getViewingSpawn() {
    	return locations.getB();
    }
    
    public Board getRunningGame() {
    	return runningGames.values().stream().findFirst().orElse(null);
    }
    
    public void requestGame(Player p){
        requestGame(p, true);
    }

    public void requestGame(Player p, boolean shouldTeleport){
    	Board b = new Board(size, size, bombCount, locations.getA(), p);
        runningGames.put(p, b);
        gameWatched.put(p, b);

        p.getInventory().clear();
        p.getInventory().setContents(Inventories.gameInventory);
        p.setAllowFlight(true);
        if(shouldTeleport){
            p.setFlying(true);
            p.teleport(locations.getB());
        }
    }

    public void finishGame(Player p){
        Board board = runningGames.remove(p);
        Board toWatch = getRunningGame();
        if(toWatch == null) {
            board.drawBlancField();
            board.viewers.forEach(pl -> gameWatched.remove(pl));
        } else {
            board.viewers.forEach(pl -> {
            	gameWatched.put(pl, toWatch);
                toWatch.viewers.add(pl);
            });
        }
    }

	public void startViewing(Player player, Board runningGame) {
		if(!runningGames.containsValue(runningGame)) throw new IllegalStateException();
		gameWatched.put(player, runningGame);
		runningGame.viewers.add(player);
		player.teleport(getViewingSpawn());
	}
}
