package gse.minu.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import org.lwjgl.glfw.GLFW;

public final class F18KeyBindings {
    public static final KeyMapping LEFT = key("key.gse.turn_left", GLFW.GLFW_KEY_LEFT);
    public static final KeyMapping RIGHT = key("key.gse.turn_right", GLFW.GLFW_KEY_RIGHT);
    public static final KeyMapping TRIM_IN = key("key.gse.trim_in", GLFW.GLFW_KEY_UP);
    public static final KeyMapping TRIM_OUT = key("key.gse.trim_out", GLFW.GLFW_KEY_DOWN);
    public static final KeyMapping SAILS = key("key.gse.sails", GLFW.GLFW_KEY_R);
    public static final KeyMapping GENNAKER = key("key.gse.gennaker", GLFW.GLFW_KEY_G);
    public static final KeyMapping ANCHOR = key("key.gse.anchor", GLFW.GLFW_KEY_H);

    private F18KeyBindings() {}

    private static KeyMapping key(String translationKey, int glfwKey) {
        return KeyBindingHelper.registerKeyBinding(new KeyMapping(
                translationKey, InputConstants.Type.KEYSYM, glfwKey, "key.categories.gse"));
    }

    public static void register() {}
}
