package gse.minu.client;

import gse.minu.workshop.SailingCraftingMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import gse.minu.network.SailingBoatNamePayload;

/** Compact 5x5 workshop interface with crafting, component service, and personalization tabs. */
public final class SailingCraftingScreen extends AbstractContainerScreen<SailingCraftingMenu> {
    private int tab;
    private Button installButton;
    private Button removeButton;
    private Button mainsailButton;
    private Button jibButton;
    private Button trampolineButton;
    private Button trapezeButton;
    private Button nameButton;
    private EditBox nameField;

    public SailingCraftingScreen(SailingCraftingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 320;
        this.imageHeight = 246;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.translatable("screen.gse.craft"), b -> setTab(0))
                .bounds(leftPos + 8, topPos + 4, 58, 18).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.gse.service"), b -> setTab(1))
                .bounds(leftPos + 69, topPos + 4, 58, 18).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.gse.customize"), b -> setTab(2))
                .bounds(leftPos + 130, topPos + 4, 76, 18).build());
        installButton = addRenderableWidget(Button.builder(Component.translatable("screen.gse.install"), b -> sendButton(1))
                .bounds(leftPos + 194, topPos + 42, 60, 18).build());
        removeButton = addRenderableWidget(Button.builder(Component.translatable("screen.gse.remove"), b -> sendButton(2))
                .bounds(leftPos + 194, topPos + 63, 60, 18).build());
        mainsailButton = addRenderableWidget(Button.builder(Component.translatable("screen.gse.mainsail"), b -> sendButton(3))
                .bounds(leftPos + 194, topPos + 88, 112, 16).build());
        jibButton = addRenderableWidget(Button.builder(Component.translatable("screen.gse.jib"), b -> sendButton(4))
                .bounds(leftPos + 194, topPos + 106, 112, 16).build());
        trampolineButton = addRenderableWidget(Button.builder(Component.translatable("screen.gse.trampoline"), b -> sendButton(5))
                .bounds(leftPos + 194, topPos + 124, 112, 16).build());
        trapezeButton = addRenderableWidget(Button.builder(Component.translatable("screen.gse.trapezes"), b -> sendButton(6))
                .bounds(leftPos + 194, topPos + 142, 112, 16).build());
        nameField = addRenderableWidget(new EditBox(font, leftPos + 194, topPos + 164, 112, 18,
                Component.translatable("screen.gse.boat_name")));
        nameField.setMaxLength(32);
        nameField.setValue(menu.getBoatName());
        nameButton = addRenderableWidget(Button.builder(Component.translatable("screen.gse.set_name"), b -> sendBoatName())
                .bounds(leftPos + 194, topPos + 184, 112, 18).build());
        updateControls();
    }

    private void setTab(int tab) {
        this.tab = tab;
        menu.setClientTab(tab);
        sendButton(10 + tab);
        updateControls();
    }
    private void updateControls() {
        if (installButton == null) return;
        installButton.visible = tab == 1;
        removeButton.visible = tab == 1;
        mainsailButton.visible = tab == 2;
        jibButton.visible = tab == 2;
        trampolineButton.visible = tab == 2;
        trapezeButton.visible = tab == 2;
        nameField.visible = tab == 2;
        nameButton.visible = tab == 2;
    }
    private void sendButton(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    private void sendBoatName() {
        if (minecraft != null && ClientPlayNetworking.canSend(SailingBoatNamePayload.TYPE)) {
            ClientPlayNetworking.send(new SailingBoatNamePayload(menu.containerId, nameField.getValue()));
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF20252A);
        graphics.fill(leftPos + 6, topPos + 30, leftPos + 188, topPos + 130, 0xFF343A40);
        graphics.fill(leftPos + 190, topPos + 30, leftPos + 314, topPos + 210, 0xFF343A40);
        graphics.fill(leftPos + 6, topPos + 144, leftPos + 184, topPos + 244, 0xFF343A40);
        String title = switch (tab) {
            case 1 -> Component.translatable("screen.gse.service_title").getString();
            case 2 -> Component.translatable("screen.gse.customize_title").getString();
            default -> Component.translatable("screen.gse.craft_title").getString();
        };
        graphics.drawString(font, title, leftPos + 8, topPos + 26, 0xFFFFFFFF, false);
        graphics.drawString(font, Component.translatable("screen.gse.boat_slot"), leftPos + 146, topPos + 30, 0xFFE6F3F8, false);
        graphics.drawString(font, Component.translatable("screen.gse.input_slot"), leftPos + 130, topPos + 106, 0xFFCCCCCC, false);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, Component.translatable("screen.gse.inventory"), 8, 139, 0xFFE0E0E0, false);
    }
}
