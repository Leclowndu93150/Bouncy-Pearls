package com.leclowndu93150.bouncy_pearls.mixin;

import com.leclowndu93150.bouncy_pearls.PearlConfig;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownEnderpearl.class)
public abstract class ThrownEnderpearlMixin {

    @Unique
    private static final double BOUNCE_DAMPING = 0.7;
    @Unique
    private static final double MIN_VELOCITY = 0.2;
    @Unique
    private static final double WALL_BOUNCE_DAMPING = 0.8;
    @Unique
    private static final double FLUID_BOUNCE_DAMPING = 0.9;

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        ThrownEnderpearl self = (ThrownEnderpearl) (Object) this;

        if (PearlConfig.getInstance().getCanRide() && self.getOwner() instanceof Player player) {
            player.startRiding(self, true);
        }
    }

    @Inject(method = "onHit", at = @At("HEAD"), cancellable = true)
    private void onHit(HitResult hitResult, CallbackInfo ci) {
        ThrownEnderpearl self = (ThrownEnderpearl) (Object) this;
        Level level = self.level();
        Vec3 velocity = self.getDeltaMovement();

        if (hitResult.getType() == HitResult.Type.BLOCK && hitResult instanceof BlockHitResult) {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            Direction hitDirection = blockHitResult.getDirection();

            FluidState fluidState = level.getFluidState(blockHitResult.getBlockPos());
            boolean hitFluid = !fluidState.isEmpty();

            if (velocity.lengthSqr() > MIN_VELOCITY * MIN_VELOCITY) {
                level.playSound(null, self.getX(), self.getY(), self.getZ(),
                        hitFluid ? SoundEvents.PLAYER_SPLASH : SoundEvents.SLIME_SQUISH,
                        SoundSource.PLAYERS, 2.0F, 1.0F);

                Vec3 newVelocity;
                double dampingFactor = hitFluid ? FLUID_BOUNCE_DAMPING :
                        (hitDirection.getAxis().isVertical() ? BOUNCE_DAMPING : WALL_BOUNCE_DAMPING);

                if (hitDirection == Direction.UP || hitDirection == Direction.DOWN) {
                    newVelocity = new Vec3(velocity.x, -velocity.y * dampingFactor, velocity.z);
                } else if (hitDirection == Direction.NORTH || hitDirection == Direction.SOUTH) {
                    newVelocity = new Vec3(velocity.x, velocity.y, -velocity.z * dampingFactor);
                } else {
                    newVelocity = new Vec3(-velocity.x * dampingFactor, velocity.y, velocity.z);
                }

                self.setDeltaMovement(newVelocity);

                if (newVelocity.lengthSqr() <= MIN_VELOCITY * MIN_VELOCITY && self.getFirstPassenger() instanceof Player) {
                    self.getFirstPassenger().stopRiding();
                }

                ci.cancel();
            }
        }
    }
}