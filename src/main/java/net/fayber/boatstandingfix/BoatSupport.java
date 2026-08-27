package net.fayber.boatstandingfix;

import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.util.math.Box;

/**
 * "Is this entity standing on/near a boat" check used by the mixin.
 *
 * The same query also runs for a boat's own vehicle-movement validation
 * (boat as the entity), so we exclude the boat itself - the class-based
 * entity query has no built-in self-exclusion like vanilla's except-param
 * queries do. Note: {@code isEntityNotCollidingWithBlocks} covers entity
 * collisions too despite the name (see README).
 */
public final class BoatSupport {
    /** Generous probe box around the entity's feet; false positives here are harmless. */
    private static final double HORIZONTAL_MARGIN = 0.3;
    private static final double VERTICAL_MARGIN = 0.5;

    private BoatSupport() {
    }

    public static boolean isNearBoat(final Entity entity) {
        if (entity == null || entity.hasVehicle()) {
            return false;
        }
        Box probe = entity.getBoundingBox().expand(HORIZONTAL_MARGIN, VERTICAL_MARGIN, HORIZONTAL_MARGIN);
        return !entity.getEntityWorld().getEntitiesByClass(AbstractBoatEntity.class, probe, boat -> boat != entity).isEmpty();
    }
}
