package de.teddy.minesweeper.game;

import de.teddy.minesweeper.Minesweeper;
import de.teddy.minesweeper.util.IsBetween;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum Game {
    MAP10X10(new Location(Minesweeper.WORLD, 53, 17, -331),
            new Location(Minesweeper.WORLD, 58, 20, -318.5),
            10,
            10),

    MAP18X18(new Location(Minesweeper.WORLD, 49, 16, -198),
            new Location(Minesweeper.WORLD, 57, 20, -177.5),
            18,
            40),

    MAP24X24(new Location(Minesweeper.WORLD, 49, 14, -63),
            new Location(Minesweeper.WORLD, 61, 18, -37.5),
            24,
            99),

    MAP_SPECIAL(new Location(Minesweeper.WORLD, 49, 14, -63),
            new Location(Minesweeper.WORLD, 61, 18, -37.5),
            24,
            70);

    static{
        for(Game value : values()){
            value.spawnPosition.setYaw(-180);
            value.spawnPosition.setPitch(30);
        }
    }

    private static final Map<Player, Board> gameWatched = new HashMap<>();
    private static final Map<Player, Board> runningGames = new HashMap<>();
    //private static final List<Player> waiting = new LinkedList<>();

    private static final Map<Player, Game> playerLocation = new HashMap<>();

    private static void switchToMap(Player p, Game g){
        Game.stopWatching(p);
        Board b = runningGames.get(p);
        if(b != null){
            Game.finishGame(p);
        }
        playerLocation.put(p, g);
        p.setAllowFlight(true);
        p.setFlying(true);
        p.teleport(g.getViewingSpawn());
    }

    private static void startWatching(Player p, Board b){
        Game cur = playerLocation.get(p);
        if(cur != b.map){
            switchToMap(p, b.map);
        }
        b.draw(Collections.singletonList(p));
        gameWatched.put(p, b);
        b.viewers.add(p);
    }

    private static void stopWatching(Player p){
        Board b = gameWatched.remove(p);
        if(b != null){
            b.viewers.remove(p);
        }
    }

    public static void stopGames(Player p){
        Board b = runningGames.get(p);
        if(b != null){
            b.drawBlancField();
            b.finish();
            b.viewers.forEach(gameWatched::remove);
            b.viewers.clear();
        }else{
            stopWatching(p);
        }
        runningGames.remove(p);
    }

    public static Game getGame(Player player){
        return playerLocation.get(player);
    }


    private final Location corner;
    private final Location spawnPosition;
    private final int size;
    private final int bombCount;

    Game(Location corner, Location spawnPoint, int size, int bombCount){
        this.corner = corner;
        this.spawnPosition = spawnPoint;
        this.size = size;
        this.bombCount = bombCount;
    }

    public boolean isBlockOutsideGame(Block block){
        return !IsBetween.isBetween2D(corner, size, size, block)
                || !IsBetween.isBetween(corner.getBlockY(), corner.getBlockY() + 1, block.getY());
    }

    public int getFieldHeight(){
        return corner.getBlockY();
    }

    public static Board getBoard(Player Player){
        return runningGames.get(Player);
    }

    public static Board getBoardWatched(Player player){
        return gameWatched.get(player);
    }

    public Location getViewingSpawn(){
        return spawnPosition;
    }

    public Board getRunningGame(){
        return runningGames.values().stream().filter(b -> b.map == this).findFirst().orElse(null);
    }

    public void startGame(Player p){
        startGame(p, true);
    }

    public void startGame(Player p, boolean shouldTeleport){
        /*synchronized (waiting) {
        	while(!waiting.isEmpty()) {
        		Player wat = waiting.get(0);
                startWatching(wat, b);
            }
        }*/
        stopGames(p);
        Board b = new Board(this, size, size, bombCount, corner, p);
        b.drawBlancField(Collections.singletonList(p));
        startWatching(p, b);
        runningGames.put(p, b);
        Bukkit.getOnlinePlayers().forEach(onPlayer -> {
            if(gameWatched.get(onPlayer) == null && b.map == playerLocation.get(onPlayer)){
                startWatching(onPlayer, b);
            }
        });

        p.getInventory().clear();
        p.getInventory().setContents(Inventories.gameInventory);
        p.setAllowFlight(true);
        if(shouldTeleport){
            p.setFlying(true);
            p.teleport(this.getViewingSpawn());
        }
    }

    public static void finishGame(Player p, boolean quit){
        Game.getGame(p).finish(p);
    }

    public static void finishGame(Player p){
        finishGame(p, false);
    }

    private void finish(Player p){
        stopGames(p);
        Board b = getRunningGame();
        if(b != null){
            Bukkit.getOnlinePlayers().forEach(onPlayer -> {
                if(gameWatched.get(onPlayer) == null && b.map == playerLocation.get(onPlayer)){
                    startWatching(onPlayer, b);
                }
            });
        }
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
        if(runningGame == null){
            Game.switchToMap(player, Game.MAP10X10);
        }else{
            Game.startWatching(player, runningGame);
        }
    }
}
