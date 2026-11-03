package gse.minu.client;

import gse.minu.entity.F18Entity;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import java.util.Locale;

/** Compact helm-only GSE sailing instruments. */
public final class SailingHUD {
    private SailingHUD() {}

    public static void register() {
        HudRenderCallback.EVENT.register(SailingHUD::render);
    }

    private static void render(GuiGraphics graphics, net.minecraft.client.DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !(minecraft.player.getVehicle() instanceof F18Entity f18)
                || f18.getControllingPassenger() != minecraft.player) return;
        int width = minecraft.getWindow().getGuiScaledWidth();
        int x = width / 2 - 104;
        int y = 8;
        graphics.fill(x, y, x + 208, y + 63, 0xA8000000);
        graphics.drawString(minecraft.font, String.format(Locale.ROOT, "F18   %.1f m/s", f18.getSpeedMps()), x + 7, y + 5, 0xFFFFFFFF, false);
        float wind = WindHudState.knots();
        int bft = WindHudState.beaufort();
        float windFrom = WindHudState.directionFrom();
        float heading = (f18.getYRot() + 180.0F + 360.0F) % 360.0F;
        float angle = Math.abs(Mth.wrapDegrees(heading - windFrom));
        graphics.drawString(minecraft.font, String.format(Locale.ROOT, "WIATR  %.1f kt  Bft %d  %.0f°   KURS  %.0f° / %.0f°",
                wind, bft, windFrom, heading, angle), x + 7, y + 17, 0xFFE6F3F8, false);

        int barX = x + 7, barY = y + 33, barWidth = 194;
        graphics.drawString(minecraft.font, "TRYM", barX, barY-10, 0xFFFFFFFF, false);
        graphics.fill(barX, barY, barX + barWidth, barY + 6, 0xFF555555);
        if (angle > 7.5F) {
            double optimum = optimalTrim(angle);
            double halfBand = angle < 45.0F || angle > 150.0F ? 5.0D : 10.0D;
            int greenStart = barX + (int) (barWidth * Mth.clamp((optimum-halfBand)/100.0D, 0.0D, 1.0D));
            int greenEnd = barX + (int) (barWidth * Mth.clamp((optimum+halfBand)/100.0D, 0.0D, 1.0D));
            graphics.fill(greenStart, barY, Math.max(greenStart + 2, greenEnd), barY + 6, 0xFF48B36A);
        }
        int marker = barX + (int) (barWidth * f18.getSailTrim());
        graphics.fill(marker - 2, barY + 1, marker + 3, barY + 6, 0xFFFFFFFF);

        String state = (f18.isSailsRaised() ? "ŻAGLE" : "ŻAGLE OPUSZCZONE")
                + (f18.isGennakerDeployed() ? "  GENNAKER" : "")
                + (f18.isAnchored() ? "  KOTWICA" : "");
        graphics.drawString(minecraft.font, state, x + 7, y + 40, 0xFFFFD27A, false);
        int rudderY = y + 54;
        int rudderX = x + 36;
        int rudderWidth = 164;
        graphics.drawString(minecraft.font, "STER", x + 7, rudderY - 3, 0xFFE6F3F8, false);
        graphics.fill(rudderX, rudderY, rudderX + rudderWidth, rudderY + 4, 0xFF555555);
        int rudderCenter = rudderX + rudderWidth / 2;
        graphics.fill(rudderCenter - 1, rudderY - 1, rudderCenter + 2, rudderY + 5, 0xFFAAAAAA);
        int rudderMarker = rudderCenter + Math.round(f18.getRudderAngle() * (rudderWidth / 2.0F));
        graphics.fill(rudderMarker - 2, rudderY - 2, rudderMarker + 3, rudderY + 6, 0xFFFFFFFF);
    }

    private static double optimalTrim(double a) {
        double[][] points={{0,100},{7.5,100},{15,100},{30,95},{45,85},{70,70},{90,60},{120,40},{150,25},{180,15}};
        if (a <= points[0][0]) return points[0][1];
        for (int i=1;i<points.length;i++) {
            if (a <= points[i][0]) {
                double t=(a-points[i-1][0])/(points[i][0]-points[i-1][0]);
                return points[i-1][1]+(points[i][1]-points[i-1][1])*t;
            }
        }
        return 15.0D;
    }
}
