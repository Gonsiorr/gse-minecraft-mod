package gse.minu.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import gse.minu.entity.F18Entity;
import gse.minu.equipment.BoatEquipment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Untextured renderer for the procedural, grouped F18 model. */
public final class F18Renderer extends EntityRenderer<F18Entity> {
    private final F18Model model = new F18Model();

    public F18Renderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 2.8F;
    }

    @Override
    public void render(F18Entity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
        poseStack.translate(0.0D, 0.60D + entity.getFoilProgress() * 0.25D, 0.0D);
        if (entity.getFoilProgress() > 0.0F) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(entity.getFoilHeelDegrees()));
        }
        VertexConsumer out = buffers.getBuffer(RenderType.debugQuads());
        float heading = (entity.getYRot() + 180.0F + 360.0F) % 360.0F;
        float relativeWind = Mth.wrapDegrees(WindHudState.directionFrom() - heading);
        float windAngle = Math.abs(relativeWind);
        float windSide = Math.signum(relativeWind);
        float eased = 1.0F - entity.getSailTrim();
        float mainSailAngle = windSide * Mth.clamp(windAngle * 0.006F + eased * 0.65F, 0.0F, 1.15F);
        float jibAngle = windSide * Mth.clamp(windAngle * 0.005F + eased * 0.48F, 0.0F, 0.95F);
        float gennakerAngle = windSide * Mth.clamp(windAngle * 0.004F, 0.0F, 0.70F);
        float rudderAngle = entity.getRudderAngle() * 0.45F;

        for (F18Model.Cuboid part : model.cuboids()) {
            String group = part.group();
            boolean mainsailGroup = group.equals("mainsail") || group.startsWith("mainsail_pattern");
            boolean jibGroup = group.equals("jib") || group.startsWith("jib_pattern");
            if (group.startsWith("mainsail_pattern")
                    && !patternVariationVisible(entity.getMainsailPattern(), group)) continue;
            if (group.startsWith("jib_pattern")
                    && !patternVariationVisible(entity.getJibPattern(), group)) continue;
            if (part.group().startsWith("hull_") && !entity.hasEquipment(BoatEquipment.Slot.HULL)) continue;
            if (part.group().equals("mast") && !entity.hasEquipment(BoatEquipment.Slot.MAST)) continue;
            if (mainsailGroup && (!entity.isSailsRaised()
                    || !entity.hasEquipment(BoatEquipment.Slot.MAINSAIL))) continue;
            if (jibGroup && (!entity.isSailsRaised()
                    || !entity.hasEquipment(BoatEquipment.Slot.JIB))) continue;
            if (part.group().startsWith("rudders_") && !entity.hasEquipment(BoatEquipment.Slot.RUDDER)) continue;
            if (part.group().equals("daggerboards")
                    && !entity.hasEquipment(BoatEquipment.Slot.DAGGERBOARDS)) continue;
            if (part.group().equals("trampoline") && !entity.hasEquipment(BoatEquipment.Slot.TRAMPOLINE)) continue;
            if (part.group().equals("bowsprit") && !entity.hasEquipment(BoatEquipment.Slot.BOWSPRIT)) continue;
            if (part.group().equals("gennaker") && (!entity.isGennakerDeployed()
                    || !entity.hasEquipment(BoatEquipment.Slot.GENNAKER))) continue;
            if (part.group().equals("trapezes") && !entity.isTrapezeActive()) continue;
            if (part.group().equals("foil_ballast") && !entity.isFoilBallastActive()) continue;
            if (part.group().equals("daggerboards") && entity.hasFoilingSetup()) continue;
            if (part.group().equals("l_foils") && !entity.hasFoilingSetup()) continue;
            poseStack.pushPose();
            if (part.group().startsWith("hull_") && entity.hasFoilingSetup()) {
                float tack = Mth.clamp(entity.getTackSide(), -1.0F, 1.0F);
                float sideLift = part.group().equals("hull_left")
                        ? 0.20F * (1.0F + tack) : 0.20F * (1.0F - tack);
                poseStack.translate(0.0D, entity.getFoilProgress() * sideLift, 0.0D);
            }
            if (part.group().equals("foil_ballast")) {
                poseStack.translate(1.5D * (1.0D - Mth.clamp(entity.getTackSide(), -1.0F, 1.0F)), 0.0D, 0.0D);
            }
            if (mainsailGroup) {
                poseStack.translate(0.0D, 0.45D, 0.0D);
                poseStack.mulPose(Axis.YP.rotation(mainSailAngle));
                poseStack.translate(0.0D, -0.45D, 0.0D);
            } else if (jibGroup) {
                poseStack.translate(0.0D, 0.48D, -1.2D);
                poseStack.mulPose(Axis.YP.rotation(jibAngle));
                poseStack.translate(0.0D, -0.48D, 1.2D);
        çÏm¢G§²ÚîÆ­yÒ&—fFR7FF–2–çB6öçG&7B†–çB6öÆ÷"’°¢–çB&VBÒ†6öÆ÷"ãâb’b„dbÂw&VVâÒ†6öÆ÷"ãâ‚’b„dbÂ&ÇVRÒ6öÆ÷"b„dc°¢–çBÇVÖ–ææ6RÒ‡&VB¢#“’²w&VVâ¢Sƒr²&ÇVR¢B’ò°¢&WGW&âÇVÖ–ææ6Râ#‚ò„dc333¢„dddddddc°¢Ğ ¢&—fFR7FF–2fö–B&÷‚…fW'FW„6öç7VÖW"÷WBÂ÷6U7F6²7F6²Âc„ÖöFVÂä7V&ö–B"Â–çBF–çB’°¢fÆöBƒÖ"æÖ–å‚‚’Â“Ö"æÖ–å’‚’Â£Ö"æÖ–å¢‚’ÂƒÖ"æÖ…‚‚’Â“Ö"æÖ…’‚’Â£Ö"æÖ…¢‚“°¢VB†÷WBÂ7F6²Â"ÂF–çBÂƒÇ“Ç£ÂƒÇ“Ç£ÂƒÇ“Ç£ÂƒÇ“Ç£“°¢VB†÷WBÂ7F6²Â"ÂF–çBÂƒÇ“Ç£ÂƒÇ“Ç£ÂƒÇ“Ç£ÂƒÇ“Ç£“°¢VB†÷WBÂ7F6²Â"ÂF–çBÂƒÇ“Ç£ÂƒÇ“Ç£ÂƒÇ“Ç£ÂƒÇ“Ç£“°¢VB†÷WBÂ7F6²Â"ÂF–çBÂƒÇ“Ç£ÂƒÇ“Ç£ÂƒÇ“Ç£ÂƒÇ“Ç£“°¢VB†÷WBÂ7F6²Â"ÂF–çBÂƒÇ“Ç£ÂƒÇ“Ç£ÂƒÇ“Ç£ÂƒÇ“Ç£“°¢VB†÷WBÂ7F6²Â"ÂF–çBÂƒÇ“Ç£ÂƒÇ“Ç£ÂƒÇ“Ç£ÂƒÇ“Ç£“°¢Ğ ¢&—fFR7FF–2fö–BVB…fW'FW„6öç7VÖW"÷WBÂ÷6U7F6²7F6²Âc„ÖöFVÂä7V&ö–B"Â–çBF–çBÀ¢fÆöBƒÆfÆöB“ÆfÆöB£ÂfÆöBƒÆfÆöB“ÆfÆöB£À¢fÆöBƒ"ÆfÆöB“"ÆfÆöB£"ÂfÆöBƒ2ÆfÆöB“2ÆfÆöB£2’°¢÷6U7F6²å÷6R÷6RÒ7F6²æÆ7B‚“°¢–çB&VBÒ"ç&VB‚’¢‚‡F–çBãâb’b„db’ò#SS°¢–çBw&VVâÒ"æw&VVâ‚’¢‚‡F–çBãâ‚’b„db’ò#SS°¢–çB&ÇVRÒ"æ&ÇVR‚’¢‡F–çBb„db’ò#SS°¢÷WBæFEfW'FW‚‡÷6RÂƒÇ“Ç£’ç6WD6öÆ÷"‡&VBÂw&VVâÂ&ÇVRÂ#SR“°¢÷WBæFEfW'FW‚‡÷6RÂƒÇ“Ç£’ç6WD6öÆ÷"‡&VBÂw&VVâÂ&ÇVRÂ#SR“°¢÷WBæFEfW'FW‚‡÷6RÂƒ"Ç“"Ç£"’ç6WD6öÆ÷"‡&VBÂw&VVâÂ&ÇVRÂ#SR“°¢÷WBæFEfW'FW‚‡÷6RÂƒ2Ç“2Ç£2’ç6WD6öÆ÷"‡&VBÂw&VVâÂ&ÇVRÂ#SR“°¢Ğ ¢÷fW'&–FP¢V&Æ–2&W6÷W&6TÆö6F–öâvWEFW‡GW&TÆö6F–öâ„c„VçF—G’VçF—G’’°¢&WGW&â&W6÷W&6TÆö6F–öâçv—F„FVfVÇDæÖW76R‚'FW‡GW&W2öVçF—G’ö&öBöö²çær"“°¢Ğ§Ğ 