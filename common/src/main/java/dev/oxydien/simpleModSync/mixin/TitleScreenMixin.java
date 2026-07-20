package dev.oxydien.simpleModSync.mixin;

import dev.oxydien.simpleModSync.SimpleModSync;
import dev.oxydien.simpleModSync.config.Config;
import dev.oxydien.simpleModSync.ui.ProgressHelper;
import dev.oxydien.simpleModSync.ui.modals.CurseforgeConstraintsModalHandler;
import dev.oxydien.simpleModSync.ui.screens.InitScreen;
import dev.oxydien.simpleModSync.ui.widgets.TotalSyncProgress;
import dev.oxydien.simpleModSync.ui.widgets.TotalSyncStatus;
import dev.oxydien.simpleModSync.workers.SyncWorker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(at = @At("HEAD"), method = "init", cancellable = true)
    private void simple_mod_sync$initHead(CallbackInfo ci) {
        if (!Config.instance.hasVisitedInitScreen()) {
            Minecraft.getInstance().setScreenAndShow(new InitScreen());
            ci.cancel();
        }
        if (!Config.instance.hasVisitedCurseforgeConstraints()) {
            CurseforgeConstraintsModalHandler.open(this);
            Config.instance.setHasVisitedCurseforgeConstraints(true);
            ci.cancel();
        }
    }

    @Inject(at = @At("RETURN"), method = "init")
    private void simple_mod_sync$init(CallbackInfo ci) {
        ProgressHelper progressHelper = new ProgressHelper(SimpleModSync.getInstance());

        final int heightOffset = 3;

        TotalSyncProgress barWidget = new TotalSyncProgress(0, 0, this.width, heightOffset, progressHelper);
        this.addRenderableOnly(barWidget);

        SyncWorker worker = SimpleModSync.getInstance().syncWorker;

        this.addRenderableWidget(new TotalSyncStatus(0, heightOffset, worker, progressHelper));
    }
}