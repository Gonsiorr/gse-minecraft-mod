package gse.minu.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;

/** Rideable F18 catamaran using vanilla boat movement and passenger handling. */
public final class F18CatamaranEntity extends Boat {
    public F18CatamaranEntity(EntityType<? extends Boat> type, Level level) {
        super(type, level);
    }
}
