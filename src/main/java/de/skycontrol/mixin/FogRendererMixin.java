package de.skycontrol.mixin;

import de.skycontrol.SkyConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
    @Shadow private static float fogRed;
    @Shadow private static float fogGreen;
    @Shadow private static float fogBlue;

    @Inject(method = "setupColor", at = @At("RETURN"))
    private static void skycontrol$fog(Camera camera, float partialTick, ClientLevel level,
                                       int renderDistance, float darkenWorldAmount, CallbackInfo ci) {
        if (!SkyConfig.D.enabled) return;
        if (camera.getFluidInCamera() != FogType.NONE) return; // unter Wasser/Lava nicht veraendern
        Vec3 c = SkyConfig.apply(new Vec3(fogRed, fogGreen, fogBlue), SkyConfig.D.fog);
        fogRed = (float) c.x;
        fogGreen = (float) c.y;
        fogBlue = (float) c.z;
    }
}
