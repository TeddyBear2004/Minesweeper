package de.teddybear2004.retro.games.game;

import de.teddybear2004.retro.games.game.painter.Painter;
import de.teddybear2004.retro.games.minesweeper.exceptions.BombExplodeException;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface Board<F extends Field> extends Comparable<Board<F>> {

    static short convertToLocal(int x, int y, int z) {
        x &= 0xF;
        y &= 0xF;
        z &= 0xF;
        return (short) (x << 8 | z << 4 | y);
    }

    static boolean isLightField(int x, int y) {
        return Math.abs(x + y) % 2 == 0;
    }

    void draw();

    void draw(@NotNull List<Player> players);

    @NotNull Map<Painter<F>, List<Player>> getCurrentPlayerPainters(@NotNull List<Player> viewers);

    boolean isFinished();

    boolean isLose();

    F[][] getBoard();

    Player getPlayer();

    @NotNull List<Player> getViewers();

    void addViewer(@NotNull Player player);

    void setScoreBoard(@NotNull Player player);

    void removeViewer(Player player);

    void clearViewers();

    void drawBlancField();

    void drawBlancField(@NotNull List<Player> players);

    boolean isGenerated();

    void checkField(int x, int y) throws BombExplodeException;

    void checkField(int x, int y, boolean b) throws BombExplodeException;

    void startStarted();

    Game getGame();

    @NotNull List<Player> getAllPlayers();

    @Nullable F getField(@NotNull Location location);

    @Nullable F getField(int x, int y);

    int getWidth();

    int getHeight();

    Location getCorner();

    void checkIfWon();

    void win();

    @NotNull
    String getActualTimeNeededString(long now);

    long getActualTimeNeeded(long now);

    long finish(boolean won, boolean saveStats, long time);

    void lose();

    long finish(boolean won);

    @NotNull Map<Painter<F>, List<Player>> getCurrentPlayerPainters();

    long finish(boolean won, boolean saveStats);

    void breakGame();

    void updateScoreBoard();

    boolean isBlockOutsideGame(@NotNull Location location);

    void removeScoreBoard(@NotNull Player player);

    void highlightBlocksAround(F f);

    @Override
    int compareTo(@NotNull Board other);

    boolean isWin();

    Long getDuration();

    Class<F> getFieldClass();

    Class<? extends Painter<?>> getPainterClass();
}
