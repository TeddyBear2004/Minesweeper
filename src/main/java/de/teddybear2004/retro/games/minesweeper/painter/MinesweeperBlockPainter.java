package de.teddybear2004.retro.games.minesweeper.painter;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import de.teddybear2004.retro.games.game.Board;
import de.teddybear2004.retro.games.game.GameManager;
import de.teddybear2004.retro.games.game.click.ClickHandler;
import de.teddybear2004.retro.games.game.painter.BlockPainter;
import de.teddybear2004.retro.games.minesweeper.MinesweeperBoard;
import de.teddybear2004.retro.games.minesweeper.MinesweeperField;
import de.teddybear2004.retro.games.util.PacketUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MinesweeperBlockPainter extends BlockPainter<MinesweeperField> implements MinesweeperPainter {

    public static final Material[] LIGHT_MATERIALS = {
            Material.WHITE_CONCRETE_POWDER,
            Material.LIME_TERRACOTTA,
            Material.GREEN_CONCRETE,
            Material.YELLOW_TERRACOTTA,
            Material.ORANGE_TERRACOTTA,
            Material.MAGENTA_TERRACOTTA,
            Material.PINK_TERRACOTTA,
            Material.PURPLE_TERRACOTTA,
            Material.RED_TERRACOTTA};
    public static final Material[] DARK_MATERIALS = {
            Material.LIGHT_GRAY_CONCRETE_POWDER,
            Material.TERRACOTTA,
            Material.GREEN_TERRACOTTA,
            Material.BROWN_TERRACOTTA,
            Material.BLUE_TERRACOTTA,
            Material.CYAN_TERRACOTTA,
            Material.LIGHT_GRAY_TERRACOTTA,
            Material.GRAY_TERRACOTTA,
            Material.LIGHT_BLUE_TERRACOTTA};

    public MinesweeperBlockPainter(Plugin plugin, ClickHandler<? super MinesweeperField, ? super Board<MinesweeperField>> clickHandler, GameManager gameManager) {
        super(plugin, clickHandler, gameManager, MinesweeperBoard.class);
    }

    @Override
    public void drawBombs(@NotNull Board<MinesweeperField> board, List<? extends Player> players) {
        double explodeDuration = 0.5d;

        for (int[] point2D : ((MinesweeperBoard) board).getBombList()) {
            Location clone = board.getCorner().clone();

            clone.setX(board.getCorner().getBlockX() + point2D[0]);
            clone.setZ(board.getCorner().getBlockZ() + point2D[1]);

            if (getBombTask() != null)
                getBombTask().cancel();

            setBombTask(Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
                for (Player p : players) {
                    PacketUtil.sendBlockChange(p, new BlockPosition(clone.toVector()), WrappedBlockData.createData(Material.COAL_BLOCK));
                    PacketUtil.sendSoundEffect(p, Sound.BLOCK_STONE_PLACE, 1f, clone);
                }
            }, (long) (20 * explodeDuration)));

            explodeDuration *= 0.7;
        }
    }

    public Material[] getActualMaterial(@NotNull MinesweeperField field) {
        boolean lightField = Board.isLightField(field.getX(), field.getY());

        if (field.getBoard().isFinished() && field.isBomb() && (!field.isCovered() || field.getBoard().isLose()))
            return new Material[]{Material.COAL_BLOCK, field.getMark()};

        if (field.isCovered())
            return new Material[]{lightField ? LIGHT_DEFAULT : DARK_DEFAULT, field.getMark()};

        return new Material[]{(lightField ? LIGHT_MATERIALS : DARK_MATERIALS)[field.getNeighborCount()], field.getMark()};
    }

}
