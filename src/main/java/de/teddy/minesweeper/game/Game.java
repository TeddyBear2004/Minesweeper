package de.teddy.minesweeper.game;

import de.teddy.minesweeper.Minesweeper;
import de.teddy.minesweeper.util.IsBetween;
import de.teddy.minesweeper.util.Tuple2;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;

public enum Game {
    MAP10X10(new Tuple2<>(new Location(Minesweeper.WORLD, 53, 17, -328), new Location(Minesweeper.WORLD, 58, 20, -315.5)),
            10, 10),

    MAP18X18(new Tuple2<>(new Location(Minesweeper.WORLD, 49, 16, -198), new Location(Minesweeper.WORLD, 57, 20, -177.5)),
            18, 40),

    MAP24X24(new Tuple2<>(new Location(Minesweeper.WORLD, 47, 15, -67), new Location(Minesweeper.WORLD, 58, 20, -40.5)),
            24, 99),

    MAP_SPECIAL(new Tuple2<>(new Location(Minesweeper.WORLD, 47, 15, -67), new Location(Minesweeper.WORLD, 58, 20, -40.5)),
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
    private final List<Player> waiting;
    private final int size;
    private final int bombCount;
    private final Material[][] defaultMaterials;

    Game(Tuple2<Location, Location> location, int size, int bombCount){
        this.runningGames = new HashMap<>();
        this.gameWatched = new HashMap<>();
        this.waiting = new LinkedList<>();
        this.defaultMaterials = Board.getBlancField(size, size);

        this.locations = location;
        this.size = size;
        this.bombCount = bombCount;
    }

    public static Game getGame(Player player){
        for(Game map : values()){
            if(map.gameWatched.containsKey(player)){
                return map;
            }
        }

        return Arrays.stream(values()).filter(map -> map.waiting.contains(player)).findFirst().orElse(null);
    }

    public static boolean isBlockInsideGameField(Block block){
        return Arrays.stream(values())
                .anyMatch(game -> IsBetween.isBetween2D(game.locations.getA(), game.size, game.size, block)
                        && (game.locations.getA().getBlockY() == block.getY() || game.locations.getA().getBlockY() + 1 == block.getY()));
    }

    public int getFieldHeight(){
        return locations.getA().getBlockY();
    }

    public Board getBoard(Player Player){
        return runningGames.get(Player);
    }

    public Board getGameWatched(Player player){
        return gameWatched.get(player);
    }

    public Material getDefaultMaterialAt(Location location){
        Tuple2<Integer, Integer> integerIntegerTuple2 = convertGlobalCordToLocal(location.getBlockX(), location.getBlockZ());
        return defaultMaterials[integerIntegerTuple2.getA()][integerIntegerTuple2.getB()];
    }

    public Tuple2<Integer, Integer> convertGlobalCordToLocal(int x, int z){
        return new Tuple2<>(Math.abs(locations.getA().getBlockX() - x), Math.abs(locations.getA().getBlockZ() - z));
    }

    public Location getViewingSpawn(){
        return locations.getB();
    }

    public Board getRunningGame(){
        return runningGames.values().stream().findFirst().orElse(null);
    }

    public void requestGame(Player p){
        requestGame(p, true);
    }

    public void requestGame(Player p, boolean shouldTeleport){
        Board b = new Board(size, size, bombCount, locations.getA(), p);
        runningGames.put(p, b);
        Board prevWatch = gameWatched.get(p);
        if(prevWatch != null) {
        	prevWatch.viewers.remove(p);
        }
        gameWatched.put(p, b);
        waiting.remove(p);
        while(!waiting.isEmpty()){
            Player rem = waiting.remove(0);
            b.viewers.add(rem);
            gameWatched.put(rem, b);
        }

        p.getInventory().clear();
        p.getInventory().setContents(Inventories.gameInventory);
        p.setAllowFlight(true);
        if(shouldTeleport){
            p.setFlying(true);
            p.teleport(locations.getB());
        }
    }

    public void finishGame(Player p, boolean quit){
        Board board = runningGames.remove(p);

        if(board == null && !quit)
        	throw new IllegalStateException("The player is not playing any game");
        Board toWatch = getRunningGame();
        if(board != null) {
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
        } else {
        	gameWatched.remove(p);
        	waiting.remove(p);
        }
    }
    
    public void finishGame(Player p) {
    	finishGame(p, false);
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
