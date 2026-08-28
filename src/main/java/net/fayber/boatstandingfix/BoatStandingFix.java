package net.fayber.boatstandingfix;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// entrypoint - everything actually happens server-side in the mixin, this just logs on startup
public class BoatStandingFix implements ModInitializer {
    public static final String MOD_ID = "boat_standing_fix";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Boat Standing Fix initialized.");
    }
}
