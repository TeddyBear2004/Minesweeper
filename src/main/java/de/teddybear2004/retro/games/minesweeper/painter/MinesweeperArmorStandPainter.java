package de.teddybear2004.retro.games.minesweeper.painter;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import de.teddybear2004.retro.games.game.Board;
import de.teddybear2004.retro.games.game.GameManager;
import de.teddybear2004.retro.games.game.click.ClickHandler;
import de.teddybear2004.retro.games.game.painter.ArmorStandPainter;
import de.teddybear2004.retro.games.minesweeper.MinesweeperBoard;
import de.teddybear2004.retro.games.minesweeper.MinesweeperField;
import de.teddybear2004.retro.games.util.PacketUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class MinesweeperArmorStandPainter extends ArmorStandPainter<MinesweeperField> implements MinesweeperPainter {

    public MinesweeperArmorStandPainter(Plugin plugin, ClickHandler<MinesweeperField, Board<MinesweeperField>> clickHandler, GameManager gameManager) {
        super(plugin, clickHandler, gameManager, MinesweeperBoard.class);
    }

    @Override
    public void drawBombs(@NotNull Board<MinesweeperField> board, @NotNull List<Player> players) {
        double explodeDuration = 0.5d;
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

        if (!(board instanceof MinesweeperBoard minesweeperBoard))
            return;

        for (int[] ints : minesweeperBoard.getBombList()) {
            Location clone = board.getCorner().clone();

            clone.setX(board.getCorner().getBlockX() + ints[0]);
            clone.setZ(board.getCorner().getBlockZ() + ints[1]);

            if (getBombTask() != null)
                getBombTask().cancel();

            setBombTask(Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
                if (this.getLocationEntityIds() == null)
                    return;
                Integer integer = this.getLocationEntityIds().get(ints);
                if (integer == null)
                    return;
                PacketContainer itemOnEntityHead = PacketUtil.getItemOnEntityHead(integer, new ItemStack(Material.COAL_BLOCK));

                for (Player p : players) {
                    try{
                        protocolManager.sendServerPacket(p, itemOnEntityHead);
                    }catch(InvocationTargetException e){
                        throw new RuntimeException(e);
                    }
                    PacketUtil.sendSoundEffect(p, Sound.BLOCK_STONE_PLACE, 1f, clone);
                }
            }, (long) (20 * explodeDuration)));

            explodeDuration *= 0.7;
        }
    }

    @Override
    public ItemStack getActualItemStack(@NotNull MinesweeperField field) {
        if (field.isMarked())
            return new ItemStack(Material.REDSTONE_BLOCK);

        boolean lightField = Board.isLightField(field.getX(), field.getY());

        if (field.getBoard().isFinished() && field.isBomb() && (!field.isCovered() || field.getBoard().isLose()))
            return new ItemStack(Material.COAL_BLOCK);

        if (field.isCovered())
            return new ItemStack(lightField ? LIGHT_DEFAULT : DARK_DEFAULT);

        return ITEM_STACKS[field.getNeighborCount()];
    }

}
