package dev.oxydien.mixin;

import dev.oxydien.SimpleModSync;
import dev.oxydien.config.Config;
import dev.oxydien.enums.SyncErrorType;
import dev.oxydien.enums.SyncState;
import dev.oxydien.ui.SetSyncRemoteScreen;
import dev.oxydien.ui.widget.SyncOverviewWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
	protected TitleScreenMixin(Text title) {
		super(title);
	}

	@Inject(at = @At("RETURN"), method = "init")
	private void simple_mod_sync$init(CallbackInfo ci) {
		SyncOverviewWidget simple_mod_sync$overview = new SyncOverviewWidget(this.textRenderer);
		this.addDrawableChild(simple_mod_sync$overview);

		if (SimpleModSync.worker.GetState() == SyncState.ERROR) {
			if (SimpleModSync.worker.GetErrorType() == SyncErrorType.REMOTE_NOT_SET) {
				if (Config.instance.getDownloadUrl().isEmpty()) {
					MinecraftClient.getInstance().setScreen(
							new SetSyncRemoteScreen(Text.empty(), this)
					);
				}
			}
		}
	}
}
