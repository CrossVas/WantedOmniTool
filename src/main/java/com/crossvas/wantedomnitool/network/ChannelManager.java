package com.crossvas.wantedomnitool.network;

import com.crossvas.wantedomnitool.network.packets.Packet;
import com.crossvas.wantedomnitool.network.packets.KeyUpdatePacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraftforge.fml.common.network.FMLIndexedMessageToMessageCodec;

@ChannelHandler.Sharable
public class ChannelManager extends FMLIndexedMessageToMessageCodec<Packet> {

    public ChannelManager() {
        this.addDiscriminator(0, KeyUpdatePacket.class);
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, Packet msg, ByteBuf target) throws Exception {
        try {
            msg.write(target);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf source, Packet msg) {
        try {
            msg.read(source);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
