package com.crossvas.wantedomnitool.network;

import com.crossvas.wantedomnitool.WantedOmniTool;
import com.crossvas.wantedomnitool.network.packets.Packet;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

import java.util.EnumMap;

@ChannelHandler.Sharable
public class NetworkManager extends SimpleChannelInboundHandler<Packet> {

    private EnumMap<Side, FMLEmbeddedChannel> channel;
    public static NetworkManager instance = new NetworkManager();

    public void init() {
        this.channel = NetworkRegistry.INSTANCE.newChannel(WantedOmniTool.MODID, new ChannelManager(), this);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet msg) {
        try {
            INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
            final EntityPlayer player = this.getPlayer(netHandler);
            IThreadListener thread = FMLCommonHandler.instance().getWorldThread(netHandler);
            if (thread.isCallingFromMinecraftThread()) {
                thread.addScheduledTask(() -> msg.handle(player));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public EntityPlayer getPlayer(INetHandler handler) {
        return handler instanceof NetHandlerPlayServer ? ((NetHandlerPlayServer) handler).player : WantedOmniTool.proxy.getPlayer();
    }

    public void sendToServer(Packet packet) {
        this.channel.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TOSERVER);
        this.channel.get(Side.CLIENT).writeOutbound(packet);
    }
}
