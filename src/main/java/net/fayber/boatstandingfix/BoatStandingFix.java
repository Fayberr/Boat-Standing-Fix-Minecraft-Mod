package net.fayber.boatstandingfix;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entrypoint for Boat Standing Fix.
 *
 * All the actual behavior change is server-side, in
 * {@link net.fayber.boatstandingfix.mixin.ServerPlayNetworkHandlerMixin}:
 * it stops the "moved wrongly" movement-tolerance check and the
 * new-collision check from rubber-banding a player standing on a
 * moving/bobbing boat (MC-156980's root cause: those checks don't account
 * for the player's reference frame - the boat - also moving). See
 * {@link BoatSupport} for the shared boat-proximity check both hooks use.
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
