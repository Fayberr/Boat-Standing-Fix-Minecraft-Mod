package net.fayber.boatstandingfix;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.AABB;

/**
 * "Is this entity standing on/near a boat" check shared by both mixins.
 *
 * The same query also runs for a boat's own vehicle-movement validation
 * (boat as the entity), so we exclude the boat itself - the class-based
 * entity query has no built-in self-exclusion like vanilla's except-param
 * queries do.
 */
public final class BoatSupport {
    /** Generous probe box around the entity's feet; false positives here are harmless. */
    private static final double HORIZONTAL_MARGIN = 0.3;
    private static final double VERTICAL_MARGIN = 0.5;

    private BoatSupport() {
    }

    public static boolean isNearBoat(final Entity entity) {
        if (entity == null || entity.isPassenger()) {
            return false;
        }
        AABB probe = entity.getBoundingBox().inflate(HORIZONTAL_MARGIN, VERTICAL_MARGIN, HORIZONTAL_MARGIN);
        return !entity.level().getEntitiesOfClass(AbstractBoat.class, probe, boat -> boat != entity).isEmpty();
    }
}
