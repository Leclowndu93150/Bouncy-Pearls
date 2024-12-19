package com.leclowndu93150.bouncy_pearls.mixin;

import com.leclowndu93150.bouncy_pearls.PearlConfig;
import net.minecraft.world.item.EnderpearlItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(EnderpearlItem.class)
public class EnderPearlItemMixin {
    @ModifyConstant(method = "use", constant = @Constant(intValue = 20))
    private int modifyCooldown(int original) {
        return PearlConfig.getInstance().getPearlCooldown();
    }
}