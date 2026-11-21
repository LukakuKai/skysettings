package dev.skysettings.mixin;

import dev.skysettings.SkyConfig;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Ueberschreibt die Tageszeit, die der Client fuer Sonne, Mond, Sterne, Himmels-, Wolken-
 * und Nebelfarbe verwendet. Tageszaehler (und damit die Mondphase) bleiben erhalten,
 * nur die Uhrzeit innerhalb des Tages wird ersetzt. Wirkt nur im Client und nur in der Oberwelt.
 */
@Mixin(World.class)
public abstract class WorldTimeMixin {
	@Inject(method = "getTimeOfDay", at = @At("RETURN"), cancellable = true)
	private void skysettings$customTime(CallbackInfoReturnable<Long> cir) {
		SkyConfig c = SkyConfig.get();
		if (!c.enabled || !c.customTime) {
			return;
		}
		World self = (World) (Object) this;
		if (!(self instanceof ClientWorld) || self.getRegistryKey() != World.OVERWORLD) {
			return;
		}
		long real = cir.getReturnValue();
		cir.setReturnValue(real - Math.floorMod(real, 24000L) + c.clockTicks());
	}
}
