package com.crossvas.wantedomnitool.network;

import com.crossvas.wantedomnitool.network.packets.KeyUpdatePacket;

public class NetworkHandlerClient extends NetworkHandler {

    @Override
    public void sendKeyUpdate(int keyState) {
        NetworkManager.instance.sendToServer(new KeyUpdatePacket(keyState));
    }
}
