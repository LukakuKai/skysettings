package dev.skysettings.mixin;

import dev.skysettings.SkyEffects;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.WeightedAttributeList;
import net.minecraft.world.attribute.WorldEnvironmentAttributeAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldEnvironmentAttributeAccess.class)
public class WorldEnvironmentAttributeAccessMixin {
	private static final String ONE_ARG =
			"getAttributeValue(Lnet/minecraft/world/attribute/EnvironmentAttribute;)Ljava/lang/Object;";
	private static final String THREE_ARGS =
			"getAttributeValue(Lnet/minecraft/world/attribute/EnvironmentAttribute;"
					+ "Lnet/minecraft/util/math/Vec3d;"
					+ "Lnet/minecraft/world/attribute/WeightedAttributeList;)Ljava/lang/Object;";

	// Beide Überladungen werden abgefangen. Ein Tiefenzähler sorgt dafür, dass der Wert
	// nur einmal verändert wird, falls eine Überladung die andere aufruft.

	@Inject(method = ONE_ARG, at = @At("HEAD"))
	private void skysettings$enterOne(EnvironmentAttribute<?> attribute, CallbackInfoReturnable<Object> cir) {
		SkyEffects.enter();
	}

	@Inject(method = ONE_ARG, at = @At("RETURN"), cancellable = true)
	private void skysettings$exitOne(EnvironmentAttribute<?> attribute, CallbackInfoReturnable<Object> cir) {
		if (SkyEffects.exit()) {
			cir.setReturnValue(SkyEffects.apply(attribute, cir.getReturnValue()));
		}
	}

	@Inject(method = THREE_ARGS, at = @At("HEAD"))
	private void skysettings$enterThree(EnvironmentAttribute<?> attribute, Vec3d pos, WeightedAttributeList pool,
			CallbackInfoReturnable<Object> cir) {
		SkyEffects.enter();
	}

	@Inject(method = THREE_ARGS, at = @At("RETURN"), cancellable = true)
	private void skysettings$exitThree(EnvironmentAttribute<?> attribute, Vec3d pos, WeightedAttributeList pool,
			CallbackInfoReturnable<Object> cir) {
		if (SkyEffects.exit()) {
			cir.setReturnValue(SkyEffects.apply(attribute, cir.getReturnValue()));
		}
	}
}
