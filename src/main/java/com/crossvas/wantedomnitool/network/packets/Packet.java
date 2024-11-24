package com.crossvas.wantedomnitool.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

public abstract class Packet {

    public void read(ByteBuf buffer) {};
    public void write(ByteBuf buffer) {};
    public void handle(EntityPlayer player) {};
}
