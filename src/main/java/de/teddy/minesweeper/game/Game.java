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

    private static final Map<Player, Board> gameWatched = new HashMap<>();
    private static final Map<Player, Board> runningGames = new HashMap<>();
    //private static final List<Player> waiting = new LinkedList<>();
    
    private static final Map<Player, Game> playerLocation = new HashMap<>();

    private static void switchToMap(Player p, Game g) {
    	Game.stopWatching(p);
    	Board b = runningGames.get(p);
    	if(b != null) {
    		Game.finishGame(p);
    	}
    	playerLocation.put(p, g);
        p.setAllowFlight(true);
        p.setFlying(true);
        p.teleport(g.getViewingSpawn());
    }
    
    private static void startWatching(Player p, Board b) {
    	Game cur = playerLocation.get(p);
    	if(cur != b.map) {
    		switchToMap(p, b.map);
    	}
    	gameWatched.put(p, b);
    	b.viewers.add(p);
    }
    
    private static void stopWatching(Player p) {
    	Board b = gameWatched.remove(p);
    	if(b != null) {
    		b.viewers.remove(p);
    	}
    }
    private static void stopWatching(Player p, Board b) {
    	b.viewers.remove(p);
    	gameWatched.remove(p);
    }
    
    public static void stopGames(Player p) {
    	Board b = runningGames.get(p);
    	if(b != null) {
    		b.finish();
    		stopWatching(p, b);
    		b.viewers.clear();
    	} else {
    		stopWatching(p);
    	}
    }
    
	private static void distributeViewers(Board b) {
		
	}

	public static Game getGame(Player player) {
		return playerLocation.get(player);
	}


    private final Tuple2<Location, Location> locations;
    private final int size;
    private final int bombCount;
    Game(Tuple2<Location, Location> location, int size, int bombCount){
        this.locations = location;
        this.size = size;
        this.bombCount = bombCount;
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
    	stopGames(p);
        Board b = new Board(this, size, size, bombCount, locations.getA(), p);
        runningGames.put(p, b);
        startWatching(p, b);
        
        p.getInventory().clear();
        p.getInventory().setContents(Inventories.gameInventory);
        p.setAllowFlight(true);
        p.setFlying(true);
        p.teleport(this.getViewingSpawn());
    }
    
    @Deprecated
    public void startGame(Player p, boolean shouldTeleport) {
        /*synchronized (waiting) {
        	while(!waiting.isEmpty()) {
        		Player wat = waiting.get(0);
                startWatching(wat, b);
            }
        }*/
    	startGame(p);
    }
    
    public static void finishGame(Player p, boolean quit) {
    	Game.getGame(p).finish(p);
    }
    
    public static void finishGame(Player p) {
    	finishGame(p, false);
    }
    
    private void finish(Player p){
    	stopGames(p);
        /*Board board = runningGames.remove(p);

        if(board == null)
        	throw new IllegalStateException("The player is not playing any game");
        
        Board toWatch = getRunningGame();
        board.finish();
        if(toWatch == null){
            board.drawBlancField();
            while(!board.viewers.isEmpty()) {
            	Player pl = board.viewers.remove(0);
            	stopWatching(pl, board);
                waiting.add(pl);
            }
        }else{
            while(!board.viewers.isEmpty()) {
            	Player pl = board.viewers.remove(0);
            	stopWatching(pl, board);
                toWatch.viewers.add(pl);
            }
        }*/
    }

    public void startViewing(Player player, Board runningGame){
    	if(runningGame == null) {
        	Game.switchToMap(player, Game.MAP10X10);
    	} else {
    		Game.startWatching(player, runningGame);
    	}
    }
}
