package cam72cam.immersiverailroading.proxy;

import java.io.IOException;

import cam72cam.immersiverailroading.registry.DefinitionManager;
import cam72cam.immersiverailroading.render.StockRenderCache;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;

public class ClientResourceReloadListener implements IResourceManagerReloadListener {

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {
		try {
			DefinitionManager.initDefinitions();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		StockRenderCache.clearRenderCache();
	}
}
