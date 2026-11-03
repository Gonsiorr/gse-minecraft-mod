package gse.minu.client;

import gse.minu.boat.F18BoatDefinition;
import java.util.ArrayList;
import java.util.List;

/** Procedural cuboid geometry for the F18; all coordinates are in blocks (one block = one metre). */
public final class F18Model {
    public record Cuboid(String group, float minX, float minY, float minZ,
                         float maxX, float maxY, float maxZ, int red, int green, int blue) {}

    private final List<Cuboid> cuboids;

    public F18Model() {
        List<Cuboid> parts = new ArrayList<>();
        // Hull pair: 0.4 m wide and 0.5 m high. Bowsprit completes the 5.5 m overall length.
        add(parts, "hull_left", -1.30F, -0.50F, -2.50F, -0.90F, 0.00F, 2.75F, 225, 231, 235);
        add(parts, "hull_right", 0.90F, -0.50F, -2.50F, 1.30F, 0.00F, 2.75F, 225, 231, 235);
        add(parts, "crossbeams", -1.10F, 0.00F, -0.92F, 1.10F, 0.18F, -0.68F, 165, 174, 179);
        add(parts, "crossbeams", -1.10F, 0.00F, 0.68F, 1.10F, 0.18F, 0.92F, 165, 174, 179);
        add(parts, "trampoline", -0.90F, 0.14F, -0.68F, 0.90F, 0.22F, 0.68F, 39, 112, 156);
        add(parts, "mast", -0.07F, 0.18F, -0.07F, 0.07F, F18BoatDefinition.MAST_HEIGHT, 0.07F, 191, 199, 205);
        add(parts, "mainsail", -0.045F, 0.45F, -0.05F, 0.045F, 8.95F, 1.30F, 190, 53, 60);
        add(parts, "jib", -0.04F, 0.48F, -2.18F, 0.04F, 6.05F, -0.12F, 240, 234, 216);
        add(parts, "mainsail_pattern_a", -0.049F, 0.45F, 0.55F, 0.049F, 8.95F, 0.72F, 255, 255, 255);
        add(parts, "mainsail_pattern_b", -0.050F, 4.55F, -0.05F, 0.050F, 4.78F, 1.30F, 255, 255, 255);
        add(parts, "mainsail_pattern_c", -0.051F, 4.15F, 0.47F, 0.051F, 4.80F, 0.82F, 255, 255, 255);
        add(parts, "jib_pattern_a", -0.044F, 0.48F, -1.25F, 0.044F, 6.05F, -1.05F, 255, 255, 255);
        add(parts, "jib_pattern_b", -0.045F, 3.20F, -2.18F, 0.045F, 3.38F, -0.12F, 255, 255, 255);
        add(parts, "jib_pattern_c", -0.046F, 2.90F, -1.38F, 0.046F, 3.55F, -0.92F, 255, 255, 255);
        add(parts, "rudders_left", -1.26F, -0.92F, 2.40F, -1.12F, 0.05F, 2.56F, 78, 85, 90);
        add(parts, "rudders_right", 1.12F, -0.92F, 2.40F, 1.26F, 0.05F, 2.56F, 78, 85, 90);
        add(parts, "daggerboards", -1.27F, -1.20F, -0.12F, -1.13F, 0.04F, 0.12F, 76, 84, 90);
        add(parts, "daggerboards", 1.13F, -1.20F, -0.12F, 1.27F, 0.04F, 0.12F, 76, 84, 90);
        add(parts, "l_foils", -1.29F, -1.15F, -0.08F, -1.10F, -0.82F, 0.08F, 91, 101, 108);
        add(parts, "l_foils", 1.10F, -1.15F, -0.08F, 1.29F, -0.82F, 0.08F, 91, 101, 108);
        add(parts, "l_foils", -1.80F, -1.15F, -0.08F, -1.10F, -0.98F, 0.08F, 91, 101, 108);
        add(parts, "l_foils", 1.10F, -1.15F, -0.08F, 1.80F, -0.98F, 0.08F, 91, 101, 108);
        add(parts, "trapezes", -1.18F, 0.25F, -0.05F, -1.14F, 1.35F, 0.00F, 195, 193, 177);
        add(parts, "trapezes", 1.14F, 0.25F, -0.05F, 1.18F, 1.35F, 0.00F, 195, 193, 177);
        add(parts, "foil_ballast", -1.85F, 0.02F, -0.12F, -1.10F, 0.16F, 0.12F, 108, 116, 122);
        add(parts, "foil_ballast", -1.95F, -0.04F, -0.20F, -1.78F, 0.22F, 0.20F, 65, 70, 74);
        add(parts, "bowsprit", -0.06F, 0.18F, -2.75F, 0.06F, 0.31F, -2.38F, 158, 167, 173);
        add(parts, "gennaker", -0.035F, 1.55F, -2.67F, 0.035F, 7.35F, -1.68F, 228, 172, 64);
        this.cuboids = List.copyOf(parts);
    }

    public List<Cuboid> cuboids() {
        return cuboids;
    }

    private static void add(List<Cuboid> parts, String group, float minX, float minY, float minZ,
                            float maxX, float maxY, float maxZ, int red, int green, int blue) {
        parts.add(new Cuboid(group, minX, minY, minZ, maxX, maxY, maxZ, red, green, blue));
    }
}
