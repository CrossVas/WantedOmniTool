package com.crossvas.wantedomnitool.keyboard;

import com.crossvas.wantedomnitool.WantedOmniTool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

@SideOnly(Side.CLIENT)
public class KeyboardClient extends Keyboard {

    public static KeyBinding modeKeyBinding;
    public static KeyBinding altKeyBinding;
    private int lastKeyState = 0;

    public KeyboardClient() {}

    public void init() {
        modeKeyBinding = new KeyBinding("key.modeswitch.desc", org.lwjgl.input.Keyboard.KEY_F, "key." + WantedOmniTool.MODID + ".category");
        altKeyBinding = new KeyBinding("key.alt.desc", org.lwjgl.input.Keyboard.KEY_LMENU, "key." + WantedOmniTool.MODID + ".category");
        ClientRegistry.registerKeyBinding(modeKeyBinding);
        ClientRegistry.registerKeyBinding(altKeyBinding);
        WantedOmniTool.logger.log(Level.INFO, "Keybindings Initialization done!");
    }

    @Override
    public void sendKeyUpdate() {
        int currentKeyState = (isKeyPressed(modeKeyBinding) ? 1 : 0) << 0 | (isKeyPressed(altKeyBinding) ? 1 : 0) << 1;
        if (currentKeyState != this.lastKeyState) {
            WantedOmniTool.network.sendKeyUpdate(currentKeyState);
            processKeyUpdate(Minecraft.getMinecraft().player, currentKeyState);
            this.lastKeyState = currentKeyState;
        }
    }

    public boolean isKeyPressed(KeyBinding binding) {
        return GameSettings.isKeyDown(binding) && (binding.getKeyConflictContext().isActive()) && binding.getKeyModifier().isActive(binding.getKeyConflictContext());
    }
}
