package gse.minu.registry;

import gse.minu.GonsioRRSailingEngine;
import gse.minu.entity.F18Entity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class BoatRegistry {
    public static final EntityType<F18Entity> F18 = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            GonsioRRSailingEngine.id("f18"),
            EntityType.Builder.<F18Entity>of(F18Entity::new, MobCategory.MISC)
                    .sized(2.6F, 1.0F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("f18")
    );

    private BoatRegistry() {}

    public static void register() {
        GonsioRRSailingEngine.LOGGER.info("Registered GSE F18");
    }
}
