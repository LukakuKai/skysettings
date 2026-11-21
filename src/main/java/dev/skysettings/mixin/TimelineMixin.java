package dev.skysettings.mixin;

import dev.skysettings.SkyConfig;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;
import net.minecraft.world.attribute.timeline.Timeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Setzt die Tageszeit fuer die 24000-Tick-Zeitleiste (Tag/Nacht). Sonne, Mond, Sterne,
 * Himmels-, Wolken- und Nebelfarbe lesen ihre Werte aus dieser Zeitleiste.
 * Wirkt nur im Client und nur in der Oberwelt.
 */
@Mixin(Timeline.class)
public abstract class TimelineMixin {
	@Inject(method = "getEffectiveTimeOfDay", at = @At("RETURN"), cancellable = true)
	private void skysettings$customTime(World world, CallbackInfoReturnable<Long> cir) {
		SkyConfig c = SkyConfig.get();
		if (!c.enabled || !c.customTime) {
			return;
		}
		if (!(world instanceof ClientWorld) || world.getRegistryKey() != World.OVERWORLD) {
			return;
		}
		Timeline self = (Timeline) (Object) this;
		if (self.getPeriod().isPresent() && self.getPeriod().get() == 24000) {
			cir.setReturnValue(c.clockTicks());
		}
	}
}
