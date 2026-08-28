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

// fixes MC-156980 - standing on a floating boat stutters, rubber-bands, or launches the player
// because vanilla's movement checks don't expect the ground under you to bob around. both
// tweaks below only kick in when BoatSupport.isNearBoat is true, so normal anti-cheat is
// untouched for everyone else
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    // 0.0625 is the squared-distance tolerance vanilla uses to flag "moved wrongly" - a boat
    // bobbing under your feet blows through that every tick, so bump it up just for that case
    @ModifyConstant(
        method = "handleMovePlayer(Lnet/minecraft/network/protocol/game/ServerboundMovePlayerPacket;)V",
        constant = @Constant(doubleValue = 0.0625D)
    )
    private double boatStandingFix$relaxMovementTolerance(final double original) {
        ServerGamePacketListenerImpl self = (ServerGamePacketListenerImpl) (Object) this;
        return BoatSupport.isNearBoat(self.player) ? 4.0D : original;
    }

    // a boat's hitbox shifting as it drifts looks like a "new" collision every tick, which
    // fights the player standing on it - skip that check for the same boat-standing case
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
