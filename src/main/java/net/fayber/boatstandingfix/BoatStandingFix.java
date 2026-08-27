package net.fayber.boatstandingfix;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Entrypoint. Everything happens server-side in {@link net.fayber.boatstandingfix.mixin.ServerGamePacketListenerImplMixin}. */
public class BoatStandingFix implements ModInitializer {
    public static final String MOD_ID = "boat_standing_fix";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Boat Standing Fix initialized.");
    }
}
