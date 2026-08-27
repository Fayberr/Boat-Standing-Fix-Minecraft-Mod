package net.fayber.boatstandingfix.mixin;

import net.fayber.boatstandingfix.BoatSupport;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fixes MC-156980: standing on a boat in water stutters, rubber-bands, or
 * launches the player. Both checks below are loosened only when
 * {@link BoatSupport#isNearBoat} is true, so normal movement anti-cheat is
 * untouched otherwise.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    /** 0.0625 = quarter-block "moved wrongly" tolerance a bobbing boat blows through every tick. */
    @ModifyConstant(
        method = "handleMovePlayer(Lnet/minecraft/network/protocol/game/ServerboundMovePlayerPacket;)V",
        constant = @Constant(doubleValue = 0.0625D)
    )
    private double boatStandingFix$relaxMovementTolerance(final double original) {
        ServerGamePacketListenerImpl self = (ServerGamePacketListenerImpl) (Object) this;
        return BoatSupport.isNearBoat(self.player) ? 4.0D : original;
    }

    /** A boat's own hitbox shift counts as a "new" collision every tick it drifts; skip that check when boat-supported. */
    @Inject(
        method = "isEntityCollidingWithAnythingNew(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;DDD)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void boatStandingFix$ignoreBoatHitboxShift(
        final LevelReader level, final Entity entity, final AABB oldAABB,
        final double newX, final double newY, final double newZ,
        final CallbackInfoReturnable<Boolean> cir
    ) {
        if (BoatSupport.isNearBoat(entity)) {
            cir.setReturnValue(false);
        }
    }
}
