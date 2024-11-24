package com.crossvas.wantedomnitool.network.packets;

import com.crossvas.wantedomnitool.WantedOmniTool;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

public class KeyUpdatePacket extends Packet {
    int keyState;

    public KeyUpdatePacket() {}

    public KeyUpdatePacket(int keyState) {
        this.keyState = keyState;
    }

    @Override
    public void read(ByteBuf buffer) {
        this.keyState = buffer.readInt();
    }

    @Override
    public void write(ByteBuf buffer) {
        buffer.writeInt(this.keyState);
    }

    @Override
    public void handle(EntityPlayer player) {
        WantedOmniTool.keyboard.processKeyUpdate(player, this.keyState);
    }
}
