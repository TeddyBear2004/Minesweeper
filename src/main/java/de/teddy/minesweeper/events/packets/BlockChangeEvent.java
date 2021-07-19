package de.teddy.minesweeper.events.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.BlockPosition;
import de.teddy.minesweeper.Minesweeper;
import de.teddy.minesweeper.game.Game;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;

public class BlockChangeEvent implements PacketListener {
    /*
    19.07 14:30:08 [Server] INFO PacketEvent[player=CraftPlayer{name=Wetterquarz},
    packet=PacketContainer[type=MAP_CHUNK[class=PacketPlayOutMapChunk, id=34],
    structureModifier=StructureModifier[fieldType=class java.lang.Object, data=[

    private int
    net.minecraft.server.v1_16_R3.PacketPlayOutMapChunk.a,

    private int
    net.minecraft.server.v1_16_R3.PacketPlayOutMapChunk.b,

    private int
    net.minecraft.server.v1_16_R3.PacketPlayOutMapChunk.c,

    private net.minecraft.server.v1_16_R3.NBTTagCompound
    net.minecraft.server.v1_16_R3.PacketPlayOutMapChunk.d,

    private int[]
    net.minecraft.server.v1_16_R3.PacketPlayOutMapChunk.e,

    private byte[] net.minecraft.server.v1_16_R3.PacketPlayOutMapChunk.f,

    private java.util.List
    net.minecraft.server.v1_16_R3.PacketPlayOutMapChunk.g,

    private boolean
    net.minecraft.server.v1_16_R3.PacketPlayOutMapChunk.h,

    private volatile boolean
    net.minecraft.server.v1_16_R3.PacketPlayOutMapChunk.ready,

    private final java.util.List
    net.minecraft.server.v1_16_R3.PacketPlayOutMapChunk.extraPackets]]]]
     */
    @Override
    public void onPacketSending(PacketEvent event){
        PacketContainer packet = event.getPacket();
        if(event.getPacketType() == PacketType.Play.Server.BLOCK_CHANGE){
            StructureModifier<BlockPosition> blockPositionModifier = packet.getBlockPositionModifier();
            if(blockPositionModifier == null)
                return;
            List<BlockPosition> values = blockPositionModifier.getValues();
            if(values == null)
                return;
            BlockPosition blockPosition = values.get(0);

            if(Game.isBlockInsideGameField(blockPosition.toLocation(event.getPlayer().getWorld()).getBlock()))
                event.setCancelled(true);
        }else{
            /*
            19.07 16:06:48 [Server] INFO true
            19.07 16:06:48 [Server] INFO true
            19.07 16:06:48 [Server] INFO [B@96899de
            19.07 16:06:48 [Server] INFO [I@16aaf7b9
            19.07 16:06:48 [Server] INFO {"name": "null", "MOTION_BLOCKING": [J@267da77e, "WORLD_SURFACE": [J@46be02cb}
            19.07 16:06:48 [Server] INFO 63
            19.07 16:06:48 [Server] INFO -24
            19.07 16:06:48 [Server] INFO 10
             */

            System.out.println("abc");
            packet.getIntegers()
                    .getValues().forEach(System.out::println);
            /*
            10
            -24
            63
            */

            packet.getNbtModifier()
                    .getValues().forEach(System.out::println);
            /*
            {"name": "null", "MOTION_BLOCKING": [J@267da77e, "WORLD_SURFACE": [J@46be02cb}
             */

            packet.getIntegerArrays()
                    .getValues().forEach(ints -> System.out.println(Arrays.toString(ints)));

            packet.getByteArrays()
                    .getValues().forEach(bytes -> System.out.println(Arrays.toString(bytes)));

            packet.getBooleans()
                    .getValues().forEach(System.out::println);
        }

    }

    @Override
    public void onPacketReceiving(PacketEvent event){}

    @Override
    public ListeningWhitelist getSendingWhitelist(){
        return ListeningWhitelist
                .newBuilder()
                .gamePhase(GamePhase.PLAYING)
                .types(PacketType.Play.Server.BLOCK_CHANGE, PacketType.Play.Server.MAP_CHUNK)
                .high()
                .build();
    }

    @Override
    public ListeningWhitelist getReceivingWhitelist(){
        return ListeningWhitelist.EMPTY_WHITELIST;
    }

    @Override
    public Plugin getPlugin(){
        return Minesweeper.INSTANCE;
    }
}
