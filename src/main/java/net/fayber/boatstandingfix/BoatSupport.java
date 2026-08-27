package net.fayber.boatstandingfix;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.AABB;

/**
 * Shared "is this entity standing on/right next to a boat" heuristic used by
 * both mixins.
 *
 * Root cause recap (verified against the decompiled 26.1 server code):
 * vanilla's {@code ServerGamePacketListenerImpl.handleMovePlayer} compares the
 * client-reported position against the server's own recomputed position each
 * tick, and rejects (snaps back) the client's position if:
 *   1. the discrepancy exceeds a tight 0.0625 (1/4 block) squared-distance
 *      tolerance, or
 *   2. the player's new bounding box overlaps something that didn't overlap
 *      the old bounding box ("new collision").
 * A boat bobbing/drifting in water routinely produces discrepancies and
 * hitbox-overlap changes bigger than that in a single tick, purely from the
 * boat's own motion - nothing to do with the player actually moving oddly.
 * Both mixins loosen those two checks, but ONLY when the player is
 * boat-supported, so normal anti-cheat behavior is untouched everywhere else.
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
        return !entity.level().getEntitiesOfClass(AbstractBoat.class, probe).isEmpty();
    }
}
