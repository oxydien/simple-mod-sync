package dev.oxydien.mixin;

import dev.oxydien.SimpleModSync;
import dev.oxydien.config.Config;
import dev.oxydien.data.ProgressCallback;
import dev.oxydien.enums.CallbackReason;
import dev.oxydien.enums.SyncErrorType;
import dev.oxydien.enums.SyncState;
import dev.oxydien.ui.SetSyncRemoteScreen;
import dev.oxydien.ui.widget.SimpleBackgroundWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasHolder;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen implements ProgressCallback {
	@Shadow @Final private static Text NARRATOR_SCREEN_TITLE;
	@Unique
	private SimpleBackgroundWidget simple_mod_sync$progressBar;
	@Unique
	private MultilineTextWidget simple_mod_sync$progressText;

	protected TitleScreenMixin(Text title) {
		super(title);
	}

	@Inject(at = @At("RETURN"), method = "onDisplayed")
	private void simple_mod_sync$onDisplayed(CallbackInfo ci) {
		// I LOVE MOJANG <3
		//SimpleModSync.worker.Subscribe(this);
	}

	@Inject(at = @At("RETURN"), method = "init")
	private void simple_mod_sync$init(CallbackInfo ci) {
        SimpleBackgroundWidget simple_mod_sync$background = new SimpleBackgroundWidget(0, 3, 100, 40, 0);
		simple_mod_sync$progressText = new MultilineTextWidget(5, 5, Text.literal("HELICOPTER"), this.textRenderer).setMaxWidth(90).setMaxRows(2);
		simple_mod_sync$progressBar = new SimpleBackgroundWidget(0, 0, 0, 2, 0xFFFFFFFF);

		this.addDrawableChild(simple_mod_sync$background);
		this.addDrawableChild(simple_mod_sync$progressText);
		this.addDrawableChild(simple_mod_sync$progressBar);
		this.simple_mod_sync$onProgressUpdate(CallbackReason.NONE);
	}

	@Inject(at = @At("RETURN"), method = "tick")
	private void simple_mod_sync$tick(CallbackInfo ci) {
		this.simple_mod_sync$onProgressUpdate(CallbackReason.NONE);
	}

	@Override
	public void simple_mod_sync$onProgressUpdate(CallbackReason reason) {
		SyncState state = SimpleModSync.worker.GetState();
		SyncErrorType errorType = SimpleModSync.worker.GetErrorType();
		simple_mod_sync$progressBar.setSize(SimpleModSync.worker.GetProgress(), 2);
        //SimpleModSync.LOGGER.info("Progress updated {}! State: {}, Error: {}", reason, state, errorType);

		if (state == SyncState.ERROR) {
			if (errorType == SyncErrorType.REMOTE_NOT_SET) {
				if (Config.instance.getDownloadUrl().isEmpty()) {
					MinecraftClient.getInstance().setScreen(
							new SetSyncRemoteScreen(Text.empty(), this)
					);
				}
			}

			simple_mod_sync$progressText.setMessage(Text.literal("ERROR: " + errorType));
			return;
		} else if (state == SyncState.DOWNLOADING) {
			simple_mod_sync$progressText.setMessage(Text.literal(String.format("Downloading %s...", SimpleModSync.worker.GetProgress())));
			return;
		}

		simple_mod_sync$progressText.setMessage(Text.literal(state.toString()));
	}
}
