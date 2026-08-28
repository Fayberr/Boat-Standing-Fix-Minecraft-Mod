package net.fayber.boatstandingfix.mixin;

import net.fayber.boatstandingfix.BoatSupport;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.Box;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// fixes MC-156980 - standing on a floating boat stutters, rubber-bands, or launches the player
// because vanilla's movement checks don't expect the ground under you to bob around. both
// tweaks below only kick in when BoatSupport.isNearBoat is true, so normal anti-cheat is
// untouched for everyone else
@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

    // 0.0625 is the squared-distance tolerance vanilla uses to flag "moved wrongly" - a boat
    // bobbing under your feet blows through that every tick, so bump it up just for that case
    @ModifyConstant(
        method = "onPlayerMove(Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket;)V",
        constant = @Constant(doubleValue = 0.0625D)
    )
    private double boatStandingFix$relaxMovementTolerance(final double original) {
        ServerPlayNetworkHandler self = (ServerPlayNetworkHandler) (Object) this;
        return BoatSupport.isNearBoat(self.player) ? 4.0D : original;
    }

    // a boat's hitbox shifting as it drifts looks like a "new" collision every tick, which
    // fights the player standing on it - skip that check for the same boat-standing case
    @Inject(
        method = "isEntityNotCollidingWithBlocks(Lnet/minecraft/world/WorldView;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;DDD)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void boatStandingFix$ignoreBoatHitboxShift(
        final WorldView world, final Entity entity, final Box oldBox,
        final double newX, final double newY, final double newZ,
        final CallbackInfoReturnable<Boolean> cir
    ) {
        if (BoatSupport.isNearBoat(entity)) {
            cir.setReturnValue(false);
        }
    }
}
