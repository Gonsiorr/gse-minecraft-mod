package gse.minu.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import gse.minu.entity.F18CatamaranEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** Procedural, untextured F18 catamaran geometry with independently animated sails and rudders. */
public final class F18CatamaranRenderer extends EntityRenderer<F18CatamaranEntity> {
    public F18CatamaranRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 1.25F;
    }

    @Override
    public void render(F18CatamaranEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
        poseStack.translate(0.0D, -0.22D, 0.0D);
        VertexConsumer out = buffers.getBuffer(RenderType.debugQuads());

        // Twin hulls, crossbeams, and the trampoline deck.
        box(out, poseStack, -0.91F, -0.20F, -1.45F, -0.65F, 0.13F, 1.48F, 224, 230, 235);
        box(out, poseStack, 0.65F, -0.20F, -1.45F, 0.91F, 0.13F, 1.48F, 224, 230, 235);
        box(out, poseStack, -0.83F, 0.03F, -0.84F, 0.83F, 0.13F, -0.57F, 180, 194, 204);
        box(out, poseStack, -0.83F, 0.03F, 0.62F, 0.83F, 0.13F, 0.89F, 180, 194, 204);
        box(out, poseStack, -0.63F, 0.07F, -0.58F, 0.63F, 0.13F, 0.63F, 39, 112, 156);

        // Mast and boom.
        box(out, poseStack, -0.055F, 0.10F, -0.04F, 0.055F, 2.72F, 0.04F, 190, 198, 205);
        box(out, poseStack, -0.045F, 0.77F, -0.06F, 0.045F, 0.87F, 1.12F, 130, 139, 146);

        // Each sail rotates around its own mast stay, so the geometry moves independently.
        float sailSwing = (float) Math.sin((entity.tickCount + partialTick) * 0.055F) * 0.18F;
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotation(sailSwing));
        sail(out, poseStack, 0.0F, 0.18F, 0.0F, 0.0F, 2.63F, 0.0F, 0.78F, 1.58F, 0.68F, 186, 52, 57);
        poseStack.popPose();

        float jibSwing = (float) Math.sin((entity.tickCount + partialTick) * 0.047F + 1.2F) * 0.14F;
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotation(jibSwing));
        sail(out, poseStack, 0.0F, 0.18F, -0.10F, 0.0F, 1.78F, -0.10F, 0.0F, 0.84F, -1.31F, 235, 231, 211);
        poseStack.popPose();

        // Twin daggerboards and rudders, with their own small steering motion.
        box(out, poseStack, -0.74F, -0.61F, -0.08F, -0.66F, 0.10F, 0.08F, 70, 78, 83);
        box(out, poseStack, 0.66F, -0.61F, -0.08F, 0.74F, 0.10F, 0.08F, 70, 78, 83);
        float rudderAngle = (float) Math.sin((entity.tickCount + partialTick) * 0.12F) * 0.09F;
        poseStack.pushPose();
        poseStack.translate(-0.78D, 0.05D, 1.39D);
        poseStack.mulPose(Axis.YP.rotation(rudderAngle));
        box(out, poseStack, -0.035F, -0.62F, -0.05F, 0.035F, 0.05F, 0.05F, 79, 86, 91);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.78D, 0.05D, 1.39D);
        poseStack.mulPose(Axis.YP.rotation(rudderAngle));
        box(out, poseStack, -0.035F, -0.62F, -0.05F, 0.035F, 0.05F, 0.05F, 79, 86, 91);
        poseStack.popPose();

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffers, packedLight);
    }

    private static void sail(VertexConsumer out, PoseStack stack,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, int red, int green, int blue) {
        quad(out, stack, red, green, blue,
                ax, ay, az, bx, by, bz, cx, cy, cz, cx + (bx - ax) * 0.10F, ay, cz);
        // Back face keeps the cloth visible from either side without an image texture.
        quad(out, stack, red, green, blue,
                cx + (bx - ax) * 0.10F, ay, cz, cx, cy, cz, bx, by, bz, ax, ay, az);
    }

    private static void box(VertexConsumer out, PoseStack stack,
                            float x0, float y0, float z0, float x1, float y1, float z1,
                            int red, int green, int blue) {
        quad(out, stack, red, green, blue, x0,y0,z0, x1,y0,z0, x1,y1,z0, x0,y1,z0);
        quad(out, stack, red, green, blue, x1,y0,z1, x0,y0,z1, x0,y1,z1, x1,y1,z1);
        quad(out, stack, red, green, blue, x0,y0,z1, x0,y0,z0, x0,y1,z0, x0,y1,z1);
        quad(out, stack, red, green, blue, x1,y0,z0, x1,y0,z1, x1,y1,z1, x1,y1,z0);
        quad(out, stack, red, green, blue, x0,y1,z0, x1,y1,z0, x1,y1,z1, x0,y1,z1);
        quad(out, stack, red, green, blue, x0,y0,z1, x1,y0,z1, x1,y0,z0, x0,y0,z0);
    }

    private static void quad(VertexConsumer out, PoseStack stack, int red, int green, int blue,
                             float x0,float y0,float z0, float x1,float y1,float z1,
                             float x2,float y2,float z2, float x3,float y3,float z3) {
        PoseStack.Pose pose = stack.last();
        out.addVertex(pose, x0, y0, z0).setColor(red, green, blue, 255);
        out.addVertex(pose, x1, y1, z1).setColor(red, green, blue, 255);
        out.addVertex(pose, x2, y2, z2).setColor(red, green, blue, 255);
        out.addVertex(pose, x3, y3, z3).setColor(red, green, blue, 255);
    }

    @Override
    public ResourceLocation getTextureLocation(F18CatamaranEntity entity) {
        return ResourceLocation.withDefaultNamespace("textures/entity/boat/oak.png");
    }
}
