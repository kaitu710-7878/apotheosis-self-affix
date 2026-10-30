package com.yutaka.apotheosis_self_affix.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyInit {

    public static final String KEY_CATEGORY = "key.categories.apotheosis_self_affix";
    public static final String KEY_OPEN_SOUL = "key.apotheosis_self_affix.open_soul_menu";

    public static final KeyMapping OPEN_SOUL_MENU_KEY = new KeyMapping(
            KEY_OPEN_SOUL,
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            KEY_CATEGORY
    );
}
