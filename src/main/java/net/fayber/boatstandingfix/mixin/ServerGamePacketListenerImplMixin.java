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
 * Fixes MC-156980 (stuttering / rubber-banding / being pushed or shoved while
 * standing on top of a boat in water).
 *
 * Two independent checks inside {@code handleMovePlayer} reject the client's
 * reported position when a boat is the thing actually moving under the
 * player's feet. Both are loosened only when {@link BoatSupport#isNearBoat}
 * is true for the moving entity, so ordinary movement anti-cheat is
 * unaffected everywhere else. See {@link BoatSupport} for the full
 * explanation.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    /**
     * The "moved wrongly" tolerance: vanilla rejects the client's position
     * when the squared distance between the client-reported and
     * server-recomputed position exceeds this. 0.0625 = a quarter of a
     * block, which a bobbing/drifting boat blows through most ticks.
     */
    @ModifyConstant(
        method = "handleMovePlayer(Lnet/minecraft/network/protocol/game/ServerboundMovePlayerPacket;)V",
        constant = @Constant(doubleValue = 0.0625D)
    )
    private double boatStandingFix$relaxMovementTolerance(final double original) {
        ServerGamePacketListenerImpl self = (ServerGamePacketListenerImpl) (Object) this;
        return BoatSupport.isNearBoat(self.player) ? 4.0D : original;
    }

    /**
     * Whether the player's new bounding box overlaps something that did not
     * overlap the old bounding box. A boat shifting under the player counts
     * as a "new" overlap every time it drifts, which alone is enough to
     * reject the movement even if the tolerance check above passes. Skip
     * that specifically for boat-supported entities; the boat's own vehicle
     * movement handling (a different call site of this same method) is
     * untouched because {@code entity} there is the boat itself, not a
     * player standing on one.
     */
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
