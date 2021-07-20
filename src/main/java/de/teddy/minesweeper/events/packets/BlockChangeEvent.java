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

import java.util.List;

public class BlockChangeEvent implements PacketListener {
    /*
    19.07 14:30:08 [Server] INFO PacketEvent[player=CraftPlayer{name=Wetterquarz},
    packet=PacketContainer[type=MAP_CHUNK[class=PacketPlayOutMapChunk, id=34],
    structureModifier=StructureModifier[fieldType=class java.lang.Object, data=[

    private int
    net.minecraft.server.v1_16_R3.PacketPlayOutMapChunk.a, ChunkX

    private int
    net.minecraft.server.v1_16_R3.PacketPlayOutMapChunk.b, ChunkZ

    private int
    net.minecraft.server.v1_16_R3.PacketPlayOutMapChunk.c, BitMaskLength

    private net.minecraft.server.v1_16_R3.NBTTagCompound
    net.minecraft.server.v1_16_R3.PacketPlayOutMapChunk.d, Primary Bit Mask

    private int[]
    net.minecraft.server.v1_16_R3.PacketPlayOutMapChunk.e, Heightmap

    private byte[]
    net.minecraft.server.v1_16_R3.PacketPlayOutMapChunk.f, Biomes length

    private java.util.List
    net.minecraft.server.v1_16_R3.PacketPlayOutMapChunk.g, Biomes

    private boolean
    net.minecraft.server.v1_16_R3.PacketPlayOutMapChunk.h, Size

    private volatile boolean
    net.minecraft.server.v1_16_R3.PacketPlayOutMapChunk.ready, Data

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
            System.out.println("ABC");

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
