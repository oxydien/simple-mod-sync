package dev.oxydien.simpleModSync.mixin;

import dev.oxydien.simpleModSync.ui.modals.GameNeedsRestartModalHandler;
import dev.oxydien.simpleModSync.ui.modals.SyncInProgressModalHandler;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JoinMultiplayerScreen.class)
public class JoinMultiplayerScreenMixin {
    @Inject(at = @At("RETURN"), method = "init", cancellable = true)
    private void sms$onInit(CallbackInfo info) {
        if (SyncInProgressModalHandler.tryOpen(JoinMultiplayerScreen::new)) {
            info.cancel();
            return;
        }
        if (GameNeedsRestartModalHandler.tryOpen(JoinMultiplayerScreen::new)) {
            info.cancel();
            return;
        }
    }
}
