package gse.minu.registry;

import gse.minu.GonsioRRSailingEngine;
import gse.minu.entity.F18CatamaranEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.core.Registry;

public final class GSEEntities {
    public static final EntityType<F18CatamaranEntity> F18_CATAMARAN = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            GonsioRRSailingEngine.id("f18_catamaran"),
            EntityType.Builder.<F18CatamaranEntity>of(F18CatamaranEntity::new, MobCategory.MISC)
                    .sized(2.6F, 0.75F)
                    .clientTrackingRange(10)
                    .updateInterval(3)
                    .build("f18_catamaran")
    );

    private GSEEntities() {}

    public static void register() {
        GonsioRRSailingEngine.LOGGER.info("Registered GSE entities");
    }
}
