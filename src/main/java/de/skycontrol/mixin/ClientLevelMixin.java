package de.skycontrol.mixin;

import de.skycontrol.SkyConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {

    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true, require = 0)
    private void skycontrol$skyColor(Vec3 pos, float partialTick, CallbackInfoReturnable<Vec3> cir) {
        if (SkyConfig.D.enabled) cir.setReturnValue(SkyConfig.apply(cir.getReturnValue(), SkyConfig.D.sky));
    }

    @Inject(method = "getCloudColor", at = @At("RETURN"), cancellable = true, require = 0)
    private void skycontrol$cloudColor(float partialTick, CallbackInfoReturnable<Vec3> cir) {
        if (SkyConfig.D.enabled) cir.setReturnValue(SkyConfig.apply(cir.getReturnValue(), SkyConfig.D.clouds));
    }

    // Je nach Version liegt diese Methode in ClientLevel oder Level -> require = 0
    @Inject(method = "getStarBrightness", at = @At("RETURN"), cancellable = true, require = 0)
    private void skycontrol$stars(float partialTick, CallbackInfoReturnable<Float> cir) {
        if (SkyConfig.D.enabled) cir.setReturnValue(Math.min(1f, cir.getReturnValue() * SkyConfig.D.stars));
    }
}
