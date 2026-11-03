package gse.minu.client;

import com.mojang.blaze3d.platform.InputConstants;
import gse.minu.entity.F18Entity;
import gse.minu.network.F18BoardPayload;
import gse.minu.network.F18HelmPayload;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import org.lwjgl.glfw.GLFW;

/** Routes Ctrl + right-click on an F18 to the server as the helm-seat action. */
public final class F18HelmInput {
    private F18HelmInput() {}

    public static void register() {
        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (!level.isClientSide || hand != InteractionHand.MAIN_HAND || !(entity instanceof F18Entity f18)) {
                return InteractionResult.PASS;
            }
            long window = Minecraft.getInstance().getWindow().getWindow();
            boolean controlDown = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_CONTROL)
                    || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_CONTROL);
            if (controlDown) {
                if (!ClientPlayNetworking.canSend(F18HelmPayload.TYPE)) return InteractionResult.PASS;
                ClientPlayNetworking.send(new F18HelmPayload(f18.getId()));
                return InteractionResult.SUCCESS;
            }
            // Preserve shift + empty-hand recovery through F18Entity.interact().
            if (player.isShiftKeyDown() && player.getItemInHand(hand).isEmpty()) return InteractionResult.PASS;
            if (player.getVehicle() == f18 || player.isPassenger() || f18.getPassengers().size() >= 2) {
                return InteractionResult.PASS;
            }
            if (!ClientPlayNetworking.canSend(F18BoardPayload.TYPE)) return InteractionResult.PASS;
            ClientPlayNetworking.send(new F18BoardPayload(f18.getId()));
            return InteractionResult.SUCCESS;
        });
    }
}
