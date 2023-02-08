package de.teddybear2004.retro.games.game.painter;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import de.teddybear2004.retro.games.game.Board;
import de.teddybear2004.retro.games.game.Field;
import de.teddybear2004.retro.games.game.Game;
import de.teddybear2004.retro.games.scheduler.RemoveMarkerScheduler;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Painter<F extends Field> {


    Map<Class<? extends Painter<?>>, Painter<?>> PAINTER_MAP = new HashMap<>();


    String getName();

    void drawBlancField(Board<F> board, List<? extends Player> players);

    void drawField(Board<? extends F> board, List<? extends Player> players);

    Set<PacketType> getRightClickPacketType();

    Set<PacketType> getLeftClickPacketType();

    void onRightClick(Player player, PacketEvent event, Game game, PacketContainer packet);

    void onLeftClick(Player player, PacketEvent event, Game game, PacketContainer packet);

    void highlightFields(List<? extends F> field, List<? extends Player> players, RemoveMarkerScheduler removeMarkerScheduler);

}
