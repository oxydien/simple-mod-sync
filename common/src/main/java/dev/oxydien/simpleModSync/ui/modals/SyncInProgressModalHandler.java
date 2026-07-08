package dev.oxydien.simpleModSync.ui.modals;

import dev.oxydien.simpleModSync.SimpleModSync;
import dev.oxydien.simpleModSync.config.Config;
import dev.oxydien.simpleModSync.ui.widgets.LabelBox;
import dev.oxydien.simpleModSync.workers.SyncWorker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

import java.util.concurrent.atomic.AtomicReference;

public class SyncInProgressModalHandler {
    private static boolean ignored = false;
    private static boolean shouldOpen = false;

    private final SyncWorker syncWorker;

    public SyncInProgressModalHandler() {
        this.syncWorker = SimpleModSync.getInstance().syncWorker;
        this.syncWorker.subscribeUpdateCallback(this::handleSyncStateUpdate);
    }

    private void handleSyncStateUpdate() {
        shouldOpen = this.syncWorker.getStatus().isInProgress();
    }

    public static boolean tryOpen(ModalHandlerUtil.OnIgnoredCallback onIgnoredCallback) {
        if (ignored) return false;
        if (!shouldOpen) return false;
        if (!Config.instance.isSyncInProgressPopupsAllowed()) return false;

        var mc = Minecraft.getInstance();
        var scr = mc.screen;
        var font = mc.font;
        var lh = mc.font.lineHeight;
        AtomicReference<Checkbox> dontShowEver = new AtomicReference<>();
        var title = Component.translatable("simple_mod_sync.ui.modal.sync_in_progress.title")
                .withStyle(style -> style.withBold(true));

        var modal = new BasicModalWindow.Builder(title, scr)
                .withSize(200, 160)
                .addBodyWidget((screen, modalX, modalY, modalWidth, modalHeight) -> {
                    var warning = new LabelBox(modalX, modalY + 30, modalWidth, lh * 5,
                            Component.translatable("simple_mod_sync.ui.modal.sync_in_progress.body"),
                            font).setAlignment(LabelBox.Alignment.CENTER);

                    dontShowEver.set(Checkbox.builder(
                                    Component.translatable("simple_mod_sync.ui.modal.dont_show_check"),
                                    font)
                            .pos(modalX + 10, modalY + 50 + lh * 5)
                            .selected(false)
                            .build());

                    screen.addModalWidget(warning);
                    screen.addModalWidget(dontShowEver.get());
                })
                .withPrimaryButton(Component.translatable("simple_mod_sync.ui.modal.go_back"), (btn) -> {
                    mc.setScreen(new TitleScreen());
                })
                .withSecondaryButton(Component.translatable("simple_mod_sync.ui.modal.ignore"), (btn) -> {
                    ignored = true;
                    if (dontShowEver.get() != null && dontShowEver.get().selected()) {
                        Config.instance.setAllowSyncInProgressPopups(false);
                    }

                    var newScreen = onIgnoredCallback.createScreen(scr);
                    mc.setScreen(newScreen);
                })
                .build();
        mc.setScreen(modal);
        return true;
    }
}
