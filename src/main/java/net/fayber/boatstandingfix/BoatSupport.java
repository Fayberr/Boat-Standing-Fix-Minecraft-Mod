package net.fayber.boatstandingfix;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.AABB;

// checks if a player is standing on top of a boat, used by the mixin to decide when to relax
// movement checks. also runs for a boat's own vehicle-movement validation (boat as the entity),
// so we exclude the boat itself from the query - it has no self-exclusion built in like vanilla's
// except-param queries do
public final class BoatSupport {
    private static final double HORIZONTAL_MARGIN = 0.3;
    private static final double VERTICAL_MARGIN = 0.5;

    private BoatSupport() {
    }

    public static boolean isNearBoat(final Entity entity) {
        if (entity == null || entity.isPassenger()) {
            return false;
        }
        AABB feet = entity.getBoundingBox();
        // only look in a thin slab right under the feet, not a bubble around the whole entity -
        // a boat has to actually be under the player for this to count. otherwise a boat just
        // sitting next to someone on land would trip the same relaxed movement checks
        AABB probe = new AABB(
            feet.minX - HORIZONTAL_MARGIN, feet.minY - VERTICAL_MARGIN, feet.minZ - HORIZONTAL_MARGIN,
            feet.maxX + HORIZONTAL_MARGIN, feet.minY + 0.1, feet.maxZ + HORIZONTAL_MARGIN
        );
        return !entity.level().getEntitiesOfClass(AbstractBoat.class, probe, boat -> boat != entity).isEmpty();
    }
}
