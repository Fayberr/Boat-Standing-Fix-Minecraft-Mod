package net.fayber.boatstandingfix;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entrypoint for Boat Standing Fix.
 *
 * All the actual behavior change is server-side, in the mixins:
 * - ServerGamePacketListenerImplMixin: stops the "moved wrongly" movement
 *   validation from rubber-banding a player standing on a moving/bobbing boat
 *   (MC-156980's root cause: the server's movement-tolerance check doesn't
 *   account for the player's reference frame - the boat - also moving).
 * - EntityMixin: stops the boat's own hitbox from wrongly blocking the
 *   player's pose/position check while standing on it.
 * - PlayerRideableJumpingMixin/VehicleMixin (dismount fix, MC-136367): places
 *   a dismounting player beside a boat instead of on top of / inside it.
 *
 * No new blocks, items, or client-only code - this only patches vanilla
 * server movement/collision logic, so it works from the server's mods folder
 * alone (and via the integrated server in singleplayer).
 */
public class BoatStandingFix implements ModInitializer {
    public static final String MOD_ID = "boat_standing_fix";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Boat Standing Fix initialized.");
    }
}
