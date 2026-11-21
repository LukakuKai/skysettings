package de.skycontrol.mixin;

import de.skycontrol.SkyConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class LevelMixin {

    /** Steuert Sonnen-/Mondstand, Himmelsfarbe, Sterne und Daemmerung ueber die Uhrzeit. */
    @Inject(method = "getTimeOfDay", at = @At("HEAD"), cancellable = true)
    private void skycontrol$time(float partialTick, CallbackInfoReturnable<Float> cir) {
        Level self = (Level) (Object) this;
        if (SkyConfig.D.enabled && SkyConfig.D.customTime
                && self instanceof ClientLevel level && level.dimension() == Level.OVERWORLD) {
            cir.setReturnValue(level.dimensionType().timeOfDay(SkyConfig.ticks()));
        }
    }

    @Inject(method = "getStarBrightness", at = @At("RETURN"), cancellable = true, require = 0)
    private void skycontrol$stars(float partialTick, CallbackInfoReturnable<Float> cir) {
        if (SkyConfig.D.enabled) cir.setReturnValue(Math.min(1f, cir.getReturnValue() * SkyConfig.D.stars));
    }
}
