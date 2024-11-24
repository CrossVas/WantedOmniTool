package com.crossvas.wantedomnitool.keyboard;

import net.minecraft.entity.player.EntityPlayer;

import java.util.HashMap;
import java.util.Map;

public class Keyboard {

    private final Map<EntityPlayer, Boolean> toolModeKeyState = new HashMap<EntityPlayer, Boolean>();
    private final Map<EntityPlayer, Boolean> altKeyState = new HashMap<EntityPlayer, Boolean>();

    public boolean isToolModeKeyDown(EntityPlayer player) {
        return toolModeKeyState.containsKey(player) ? toolModeKeyState.get(player) : false;
    }

    public boolean isAltKeyDown(EntityPlayer player) {
        return altKeyState.containsKey(player) ? altKeyState.get(player) : false;
    }

    public void init() {}

    public void sendKeyUpdate() {}

    public void processKeyUpdate(EntityPlayer player, int keyState) {
        this.toolModeKeyState.put(player, (keyState & 1) != 0);
        this.altKeyState.put(player, (keyState & 2) != 0);
    }
}
