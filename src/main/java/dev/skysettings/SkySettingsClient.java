package dev.skysettings;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SkySettingsClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("skysettings");

	private static boolean openRequested = false;

	@Override
	public void onInitializeClient() {
		SkyConfig.load();

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			for (String name : new String[] {"sky", "skysettings"}) {
				LiteralArgumentBuilder<FabricClientCommandSource> command = ClientCommandManager.literal(name)
						.executes(context -> {
							// Der Chat schliesst sich nach dem Befehl selbst, daher das Menue erst im naechsten Tick oeffnen.
							openRequested = true;
							return 1;
						});
				dispatcher.register(command);
			}
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (openRequested) {
				openRequested = false;
				client.setScreen(new SkySettingsScreen(client.currentScreen));
			}
		});
	}
}
