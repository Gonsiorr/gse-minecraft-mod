package gse.minu.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.tags.FluidTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import gse.minu.physics.SailingPhysicsEngine;
import gse.minu.wind.WindManager;
import gse.minu.equipment.BoatEquipment;
import gse.minu.item.F18Item;
import java.util.Optional;
import java.util.UUID;

/** F18 rideable catamaran with server-authoritative GSE movement, independent of vanilla Boat. */
public final class F18Entity extends Entity {
    private static final EntityDataAccessor<Boolean> GENNAKER_DEPLOYED =
            SynchedEntityData.defineId(F18Entity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> SKIPPER_ID =
            SynchedEntityData.defineId(F18Entity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Float> SAIL_TRIM =
            SynchedEntityData.defineId(F18Entity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> SAILS_RAISED =
            SynchedEntityData.defineId(F18Entity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ANCHORED =
            SynchedEntityData.defineId(F18Entity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> RUDDER_ANGLE =
            SynchedEntityData.defineId(F18Entity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SPEED_MPS =
            SynchedEntityData.defineId(F18Entity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> TRANSFER_PENDING =
            SynchedEntityData.defineId(F18Entity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FOILING =
            SynchedEntityData.defineId(F18Entity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FOILING_SETUP =
            SynchedEntityData.defineId(F18Entity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FOIL_BALLAST_ACTIVE =
            SynchedEntityData.defineId(F18Entity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TRAPEZE_ACTIVE =
            SynchedEntityData.defineId(F18Entity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> MAINSAIL_COLOR =
            SynchedEntityData.defineId(F18Entity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> JIB_COLOR =
            SynchedEntityData.defineId(F18Entity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TRAMPOLINE_COLOR =
            SynchedEntityData.defineId(F18Entity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TRAPEZE_COLOR =
            SynchedEntityData.defineId(F18Entity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> MAINSAIL_PATTERN =
            SynchedEntityData.defineId(F18Entity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> JIB_PATTERN =
            SynchedEntityData.defineId(F18Entity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Float> TACK_SIDE =
            SynchedEntityData.defineId(F18Entity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> EQUIPMENT_MASK =
            SynchedEntityData.defineId(F18Entity.class, EntityDataSerializers.INT);
    private int controlBits;
    private boolean hasEverHadSkipper;
    private BoatEquipment equipment = new BoatEquipment();
    private Vec3 anchorCollisionImpulse = Vec3.ZERO;
    private int collisionSoundCooldown;
    private long lastCollisionTick = Long.MIN_VALUE;
    private int lastCollisionEntityId = -1;

    public F18Entity(EntityType<? extends F18Entity> type, Level level) {
        super(type, level);
        updateEquipmentMask();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(GENNAKER_DEPLOYED, false);
        builder.define(SKIPPER_ID, Optional.empty());
        builder.define(SAIL_TRIM, 0.70F);
        builder.define(SAILS_RAISED, true);
        builder.define(ANCHORED, false);
        builder.define(RUDDER_ANGLE, 0.0F);
        builder.define(SPEED_MPS, 0.0F);
        builder.define(TRANSFER_PENDING, false);
        builder.define(FOILING, false);
        builder.define(FOILING_SETUP, false);
        builder.define(FOIL_BALLAST_ACTIVE, false);
        builder.define(TRAPEZE_ACTIVE, false);
        builder.define(MAINSAIL_COLOR, 0xFFFFFFFF);
        builder.define(JIB_COLOR, 0xFFFFFFFF);
        builder.define(TRAMPOLINE_COLOR, 0xFFFFFFFF);
        builder.define(TRAPEZE_COLOR, 0xFFFFFFFF);
        builder.define(MAINSAIL_PATTERN, "");
        builder.define(JIB_PATTERN, "");
        builder.define(TACK_SIDE, 1.0F);
        builder.define(EQUIPMENT_MASK, 0);
    }

    public float getSailTrim() { return this.entityData.get(SAIL_TRIM); }
    public void setSailTrim(float trim) { this.entityData.set(SAIL_TRIM, Math.max(0.0F, Math.min(1.0F, trim))); }
    public boolean isSailsRaised() { return this.entityData.get(SAILS_RAISED); }
    public void setSailsRaised(boolean raised) { this.entityData.set(SAILS_RAISED, raised); }
    public boolean isAnchored() { return this.entityData.get(ANCHORED); }
    public void setAnchored(boolean anchored) { this.entityData.set(ANCHORED, anchored); }
    public float getRudderAngle() { return this.entityData.get(RUDDER_ANGLE); }
    public void setRudderAngle(float angle) { this.entityData.set(RUDDER_ANGLE, Math.max(-1.0F, Math.min(1.0F, angle))); }
    public float getSpeedMps() { return this.entityData.get(SPEED_MPS); }
    public void setSpeedMps(float speed) { this.entityData.set(SPEED_MPS, Math.max(0.0F, speed)); }
    public boolean hasEverHadSkipper() { return hasEverHadSkipper; }
    public BoatEquipment getEquipment() { return equipment; }
    public void setEquipment(BoatEquipment equipment) { this.equipment = equipment; updateEquipmentMask(); }
    public boolean hasEquipment(BoatEquipment.Slot slot) {
        return (this.entityData.get(EQUIPMENT_MASK) & (1 << slot.ordinal())) != 0;
    }
    private void updateEquipmentMask() {
        int mask = 0;
        for (BoatEquipment.Slot slot : BoatEquipment.Slot.values()) {
            if (equipment.tier(slot) > 0) mask |= 1 << slot.ordinal();
        }
        this.entityData.set(EQUIPMENT_MASK, mask);
    }
    public int getMainsailColor() { return this.entityData.get(MAINSAIL_COLOR); }
    public int getJibColor() { return this.entityData.get(JIB_COLOR); }
    public int getTrampolineColor() { return this.entityData.get(TRAMPOLINE_COLOR); }
    public int getTrapezeColor() { return this.entityData.get(TRAPEZE_COLOR); }
    public String getMainsailPattern() { return this.entityData.get(MAINSAIL_PATTERN); }
    public String getJibPattern() { return this.entityData.get(JIB_PATTERN); }
    public void setMainsailColor(int color) { this.entityData.set(MAINSAIL_COLOR, color | 0xFF000000); }
    public void setJibColor(int color) { this.entityData.set(JIB_COLOR, color | 0xFF000000); }
    public void setTrampolineColor(int color) { this.entityData.set(TRAMPOLINE_COLOR, color | 0xFF000000); }
    public void setTrapezeColor(int color) { this.entityData.set(TRAPEZE_COLOR, color | 0xFF000000); }
    public void setMainsailPattern(String pattern) { this.entityData.set(MAINSAIL_PATTERN, normalizePattern({óm¢G§²ÚîÆ­yÙ¤ì(€€€ô((€€€ÁÕ‰±¥ŒÍÑ…Ñ¥Œ%Ñ•µMÑ…¬É•…Ñ”¡	½…ÑÅÕ¥Áµ•¹Ð¹M±½ÐÍ±½Ð°¥¹ÐÑ¥•È°¥¹Ð½±½È¤ì(€€€€€€€É•ÑÕÉ¸É•…Ñ”¡Í±½Ð°Ñ¥•È°½±½È°€ˆˆ¤ì(€€€ô((€€€ÁÕ‰±¥ŒÍÑ…Ñ¥Œ%Ñ•µMÑ…¬É•…Ñ”¡	½…ÑÅÕ¥Áµ•¹Ð¹M±½ÐÍ±½Ð°¥¹ÐÑ¥•È°¥¹Ð½±½È°MÑÉ¥¹œÁ…ÑÑ•É¸¤ì(€€€€€€€%Ñ•µMÑ…¬ÍÑ…¬€ô¹•Ü%Ñ•µMÑ…¬¡%9MQ9¤ì(€€€€€€€½µÁ½Õ¹‘Q…œÑ…œ€ô¹•Ü½µÁ½Õ¹‘Q…œ ¤ì(€€€€€€€Ñ…œ¹ÁÕÑMÑÉ¥¹œ ‰M±½Ðˆ°Í±½Ð¹¹…µ” ¤¤ì(€€€€€€€Ñ…œ¹ÁÕÑ%¹Ð ‰Q¥•Èˆ°Ñ¥•È¤ì(€€€€€€€Ñ…œ¹ÁÕÑ%¹Ð ‰½±½Èˆ°½±½Èð€ÁáÀÀÀÀÀÀ¤ì(€€€€€€€Ñ…œ¹ÁÕÑMÑÉ¥¹œ ‰A…ÑÑ•É¸ˆ°Á…ÑÑ•É¸€ôô¹Õ±°€ü€ˆˆ€èÁ…ÑÑ•É¸¤ì(€€€€€€€ÍÑ…¬¹Í•Ð¡…Ñ…½µÁ½¹•¹ÑÌ¹UMQ=5}Q°ÕÍÑ½µ…Ñ„¹½˜¡Ñ…œ¤¤ì(€€€€€€€É•ÑÕÉ¸ÍÑ…¬ì(€€€ô((€€€ÁÕ‰±¥ŒÍÑ…Ñ¥Œ	½…ÑÅÕ¥Áµ•¹Ð¹M±½ÐÍ±½Ð¡%Ñ•µMÑ…¬ÍÑ…¬¤ì(€€€€€€€¥˜€ …ÍÑ…¬¹¥Ì¡%9MQ9¤¤É•ÑÕÉ¸¹Õ±°ì(€€€€€€€ÕÍÑ½µ…Ñ„‘…Ñ„€ôÍÑ…¬¹•Ð¡…Ñ…½µÁ½¹•¹ÑÌ¹UMQ=5}Q¤ì(€€€€€€€¥˜€¡‘…Ñ„€ôô¹Õ±°¤É•ÑÕÉ¸¹Õ±°ì(€€€€€€€ÑÉäìÉ•ÑÕÉ¸	½…ÑÅÕ¥Áµ•¹Ð¹M±½Ð¹Ù…±Õ•=˜¡‘…Ñ„¹½ÁåQ…œ ¤¹•ÑMÑÉ¥¹œ ‰M±½Ðˆ¤¤ìô(€€€€€€€…Ñ €¡%±±•…±ÉÕµ•¹Ñá•ÁÑ¥½¸¥¹½É•¤ìÉ•ÑÕÉ¸¹Õ±°ìô(€€€ô((€€€ÁÕ‰±¥ŒÍÑ…Ñ¥Œ¥¹ÐÑ¥•È¡%Ñ•µMÑ…¬ÍÑ…¬¤ì(€€€€€€€¥˜€ …ÍÑ…¬¹¥Ì¡%9MQ9¤¤É•ÑÕÉ¸€Àì(€€€€€€€ÕÍÑ½µ…Ñ„‘…Ñ„€ôÍÑ…¬¹•Ð¡…Ñ…½µÁ½¹•¹ÑÌ¹UMQ=5}Q¤ì(€€€€€€€É•ÑÕÉ¸‘…Ñ„€ôô¹Õ±°€ü€À€è‘…Ñ„¹½ÁåQ…œ ¤¹•Ñ%¹Ð ‰Q¥•Èˆ¤ì(€€€ô((€€€ÁÕ‰±¥ŒÍÑ…Ñ¥Œ¥¹Ð½±½È¡%Ñ•µMÑ…¬ÍÑ…¬¤ì(€€€€€€€¥˜€ …ÍÑ…¬¹¥Ì¡%9MQ9¤¤É•ÑÕÉ¸€Ááì(€€€€€€€ÕÍÑ½µ…Ñ„‘…Ñ„€ôÍÑ…¬¹•Ð¡…Ñ…½µÁ½¹•¹ÑÌ¹UMQ=5}Q¤ì(€€€€€€€¥˜€¡‘…Ñ„€ôô¹Õ±°¤É•ÑÕÉ¸€Ááì(€€€€€€€½µÁ½Õ¹‘Q…œÑ…œ€ô‘…Ñ„¹½ÁåQ…œ ¤ì(€€€€€€€É•ÑÕÉ¸Ñ…œ¹½¹Ñ…¥¹Ì ‰½±½Èˆ¤€üÑ…œ¹•Ñ%¹Ð ‰½±½Èˆ¤ð€ÁáÀÀÀÀÀÀ€è€Ááì(€€€ô((€€€ÁÕ‰±¥ŒÍÑ…Ñ¥ŒMÑÉ¥¹œÁ…ÑÑ•É¸¡%Ñ•µMÑ…¬ÍÑ…¬¤ì(€€€€€€€¥˜€ …ÍÑ…¬¹¥Ì¡%9MQ9¤¤É•ÑÕÉ¸€ˆˆì(€€€€€€€ÕÍÑ½µ…Ñ„‘…Ñ„€ôÍÑ…¬¹•Ð¡…Ñ…½µÁ½¹•¹ÑÌ¹UMQ=5}Q¤ì(€€€€€€€É•ÑÕÉ¸‘…Ñ„€ôô¹Õ±°€ü€ˆˆ€è‘…Ñ„¹½ÁåQ…œ ¤¹•ÑMÑÉ¥¹œ ‰A…ÑÑ•É¸ˆ¤ì(€€€ô((€€€=Ù•ÉÉ¥‘”(€€€ÁÕ‰±¥Œ½µÁ½¹•¹Ð•Ñ9…µ”¡%Ñ•µMÑ…¬ÍÑ…¬¤ì(€€€€€€€	½…ÑÅÕ¥Áµ•¹Ð¹M±½ÐÍ±½Ð€ôÍ±½Ð¡ÍÑ…¬¤ì(€€€€€€€¥˜€¡Í±½Ð€ôô¹Õ±°¤É•ÑÕÉ¸½µÁ½¹•¹Ð¹ÑÉ…¹Í±…Ñ…‰±” ‰¥Ñ•´¹Í”¹½µÁ½¹•¹Ð¹Õ¹­¹½Ý¸ˆ¤ì(€€€€€€€¹•Ð¹µ¥¹•É…™Ð¹¹•ÑÝ½É¬¹¡…Ð¹5ÕÑ…‰±•½µÁ½¹•¹Ð¹…µ”€ô(€€€€€€€€€€€€€€€½µÁ½¹•¹Ð¹ÑÉ…¹Í±…Ñ…‰±” ‰¥Ñ•´¹Í”¹½µÁ½¹•¹Ð¸ˆ€¬Í±½Ð¹¹…µ” ¤¹Ñ½1½Ý•É…Í”¡1½…±”¹I==P¤¤ì(€€€€€€€¥˜€¡Í±½Ð€ôô	½…ÑÅÕ¥Áµ•¹Ð¹M±½Ð¹QI5A=1%9ñðÍ±½Ð€ôô	½…ÑÅÕ¥Áµ•¹Ð¹M±½Ð¹QIAiL(€€€€€€€€€€€€€€€ñðÍ±½Ð€ôô	½…ÑÅÕ¥Áµ•¹Ð¹M±½Ð¹=%1%9}11=d¤É•ÑÕÉ¸¹…µ”ì(€€€€€€€¥¹ÐÑ¥•È€ôÑ¥•È¡ÍÑ…¬¤ì(€€€€€€€É•ÑÕÉ¸¹…µ”¹…ÁÁ•¹¡½µÁ½¹•¹Ð¹±¥Ñ•É…°¡Ñ¥•È€ôô€ÄÀ€ü€ˆ`ˆ€è€ˆ€ˆ€¬Ñ½I½µ…¸¡Ñ¥•È¤¤¤ì(€€€ô((€€€ÁÉ¥Ù…Ñ”ÍÑ…Ñ¥ŒMÑÉ¥¹œÑ½I½µ…¸¡¥¹ÐÙ…±Õ”¤ì(€€€€€€€É•ÑÕÉ¸ÍÝ¥Ñ €¡Ù…±Õ”¤ì…Í”€Ä€´ø€‰$ˆì…Í”€È€´ø€‰%$ˆì…Í”€Ì€´ø€‰%%$ˆì…Í”€Ð€´ø€‰%Xˆì…Í”€Ô€´ø€‰Xˆì‘•™…Õ±Ð€´ø€ˆüˆìôì(€€€ô)ô(