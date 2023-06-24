package name.lowtechcrafting;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class LowTechCraftingClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		HandledScreens.register(LowTechCrafting.AUTOCRAFTING_TABLE_SCREEN_HANDLER, AutoCraftingScreen::new);
		HandledScreens.register(LowTechCrafting.BOX_SCREEN_HANDLER, BoxScreen::new);
	}
}