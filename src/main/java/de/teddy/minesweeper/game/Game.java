package de.teddy.minesweeper.game;

import de.teddy.minesweeper.Minesweeper;
import de.teddy.minesweeper.util.IsBetween;
import de.teddy.minesweeper.util.Tuple2;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;

public enum Game {
    MAP10X10(new Tuple2<>(new Location(Minesweeper.WORLD, 53, 17, -331), new Location(Minesweeper.WORLD, 58, 20, -318.5)),
            10, 10),

    MAP18X18(new Tuple2<>(new Location(Minesweeper.WORLD, 49, 16, -198), new Location(Minesweeper.WORLD, 57, 20, -177.5)),
            18, 40),

    MAP24X24(new Tuple2<>(new Location(Minesweeper.WORLD, 49, 14, -63), new Location(Minesweeper.WORLD, 61, 18, -37.5)),
            24, 99),

    MAP_SPECIAL(new Tuple2<>(new Location(Minesweeper.WORLD, 49, 14, -63), new Location(Minesweeper.WORLD, 61, 18, -37.5)),
            24, 70);

    static{
        for(Game value : values()){
            value.locations.getB().setYaw(-180);
            value.locations.getB().setPitch(30);
        }
    }

    private final Tuple2<Location, Location> locations;
    private final int size;
    private final int bombCount;

    private static final Map<Player, Board> gameWatched = new HashMap<>();
    private static final Map<Player, Board> runningGames = new HashMap<>();
    private final List<Player> waiting;

    Game(Tuple2<Location, Location> location, int size, int bombCount){
        this.waiting = new LinkedList<>();

        this.locations = location;
        this.size = size;
        this.bombCount = bombCount;
    }

	public static Game getGame(Player player) {
		Board watched = gameWatched.get(player);
		if (watched != null) {
			return watched.map;
		}

		return Arrays.stream(values()).filter(map -> map.waiting.contains(player)).findFirst().orElse(null);
	}

    public static boolean isBlockInsideGameField(Block block){
        return Arrays.stream(values())
                .anyMatch(game -> IsBetween.isBetween2D(game.locations.getA(), game.size, game.size, block)
                        && IsBetween.isBetween(game.locations.getA().getBlockY(), game.locations.getA().getBlockY() + 1, block.getY()));
    }

    public boolean isBlockInsideGame(Block block){
        return IsBetween.isBetween2D(locations.getA(), size, size, block)
                && IsBetween.isBetween(locations.getA().getBlockY(), locations.getA().getBlockY() + 1, block.getY());
    }

    public int getFieldHeight(){
        return locations.getA().getBlockY();
    }

    public static Board getBoard(Player Player){
        return runningGames.get(Player);
    }

    public static Board getGameWatched(Player player){
        return gameWatched.get(player);
    }

    public Location getViewingSpawn(){
        return locations.getB();
    }

    public Board getRunningGame(){
        return runningGames.values().stream().filter(b -> b.map == this).findFirst().orElse(null);
    }

    
    public void startGame(Player p) {
    	startGame(p, true);
    }
    
    public void startGame(Player p, boolean shouldTeleport) {
        if(runningGames.get(p) != null) {
            finishGame(p);
        }
        if(gameWatched.get(p) != null) {
            gameWatched.get(p).viewers.remove(p);
        }
        Board b = new Board(this, size, size, bombCount, locations.getA(), p);
        runningGames.put(p, b);
        gameWatched.put(p, b);
        synchronized (waiting) {
        	while(!waiting.isEmpty()) {
        		Player wat = waiting.remove(0);
                gameWatched.put(wat, b);
                b.viewers.add(wat);
            }
        }

        p.getInventory().clear();
        p.getInventory().setContents(Inventories.gameInventory);
        p.setAllowFlight(true);
        if(shouldTeleport){
            p.setFlying(true);
            p.teleport(locations.getB());
        }
    }

    public static void finishGame(Player p, boolean quit) {
    	Game.getGame(p).finish(p, quit);
    }
    
    public static void finishGame(Player p) {
    	finishGame(p, false);
    }
    
    private void finish(Player p, boolean quit){
    	if(quit) {
    		if(runningGames.get(p) != null)
    			finishGame(p, false);
    		gameWatched.remove(p);
    		waiting.remove(p);
    		return;
    	}
        Board board = runningGames.remove(p);

        if(board == null)
        	throw new IllegalStateException("The player is not playing any game");
        
        Board toWatch = getRunningGame();
        board.finish();
        if(toWatch == null){
            board.drawBlancField();
            board.viewers.forEach(pl -> {
                gameWatched.remove(pl);
                waiting.add(pl);
            });
        }else{
            board.viewers.forEach(pl -> {
                gameWatched.put(pl, toWatch);
                toWatch.viewers.add(pl);
            });
        }
    }

    public void startViewing(Player player, Board runningGame){
        if(runningGame == null || !runningGames.containsValue(runningGame)){
            this.waiting.add(player);
        }else{
            gameWatched.put(player, runningGame);
            runningGame.viewers.add(player);
        }
        player.teleport(getViewingSpawn());
    }
}
