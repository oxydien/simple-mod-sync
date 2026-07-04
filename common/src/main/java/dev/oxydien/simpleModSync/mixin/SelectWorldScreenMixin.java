package dev.oxydien.simpleModSync.mixin;

import dev.oxydien.simpleModSync.ui.modals.GameNeedsRestartModalHandler;
import dev.oxydien.simpleModSync.ui.modals.SyncInProgressModalHandler;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SelectWorldScreen.class)
public class SelectWorldScreenMixin {
    @Inject(at = @At("HEAD"), method = "init", cancellable = true)
    private void sms$onInit(CallbackInfo info) {
        if (SyncInProgressModalHandler.tryOpen(SelectWorldScreen::new)) {
            info.cancel();
            return;
        }
        if (GameNeedsRestartModalHandler.tryOpen(SelectWorldScreen::new)) {
            info.cancel();
            return;
        }
    }
}
