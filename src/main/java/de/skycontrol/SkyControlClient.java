package de.skycontrol;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class SkyControlClient implements ClientModInitializer {
    private static boolean openRequested = false;

    @Override
    public void onInitializeClient() {
        SkyConfig.load();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("sky").executes(ctx -> {
                    openRequested = true; // erst im naechsten Tick oeffnen, sonst schliesst der Chat das Menue wieder
                    return 1;
                })));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (openRequested) {
                openRequested = false;
                mc.setScreen(new SkyScreen());
            }
        });
    }
}
