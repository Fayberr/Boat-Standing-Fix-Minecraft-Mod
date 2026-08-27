package net.fayber.boatstandingfix;

import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.util.math.Box;

/**
 * Shared "is this entity standing on/right next to a boat" heuristic used by
 * the mixin.
 *
 * Root cause recap (verified against the decompiled 1.21.11 server code):
 * vanilla's {@code ServerPlayNetworkHandler.onPlayerMove} compares the
 * client-reported position against the server's own recomputed position each
 * tick, and rejects (snaps back) the client's position if:
 *   1. the discrepancy exceeds a tight 0.0625 (1/4 block) squared-distance
 *      tolerance, or
 *   2. the player's new bounding box overlaps something that didn't overlap
 *      the old bounding box ("new collision", despite the misleading vanilla
 *      method name {@code isEntityNotCollidingWithBlocks} this also covers
 *      entity-vs-entity collisions, via {@code CollisionView.getCollisions}).
 * A boat bobbing/drifting in water routinely produces discrepancies and
 * hitbox-overlap changes bigger than that in a single tick, purely from the
 * boat's own motion - nothing to do with the player actually moving oddly.
 * The mixin loosens both checks, but ONLY when the entity is boat-supported,
 * so normal anti-cheat behavior is untouched everywhere else.
 *
 * {@code isEntityNotCollidingWithBlocks} is also called for the boat's own
 * vehicle-movement validation ({@code onVehicleMove}), with the boat itself
 * as {@code entity}. Unlike vanilla's own entity-collision query (which
 * takes the subject as an explicit "except" parameter and already excludes
 * it), a naive "any boat in this box" probe using the class-based query has
 * no such exclusion and would always match a boat against itself. {@link
 * #isNearBoat} filters the boat itself out of the query to prevent that
 * self-match.
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
