package dev.oxydien.simpleModSync.ui.modals;

import dev.oxydien.simpleModSync.config.Config;
import dev.oxydien.simpleModSync.ui.widgets.LabelBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.concurrent.atomic.AtomicReference;

public class SettingsModalHandler {

    public static void open(Screen parentScreen) {
        var mc = Minecraft.getInstance();
        var lh = mc.font.lineHeight;

        var title = Component.translatable("simple_mod_sync.ui.modal.settings.title").withStyle(style -> style.withBold(true));

        AtomicReference<EditBox> schemaFileUrl = new AtomicReference<>();
        AtomicReference<Checkbox> syncOnStartup = new AtomicReference<>();
        AtomicReference<Checkbox> allowSyncInProgress = new AtomicReference<>();
        AtomicReference<Checkbox> allowGameNeedsRestart = new AtomicReference<>();


        var modal = new BasicModalWindow.Builder(title, parentScreen)
                .withSize(220, 210)
                .addBodyWidget((screen, mX, mY, mW, mH) -> {
                    var padding = 5;
                    var pX = mX + padding;
                    var pY = mY + padding;
                    var pW = mW - 2 * padding;
                    var cY = pY + 20; // current Y
                    var font = screen.getFont();

                    var schemaTitle = new LabelBox(pX, cY, pW, lh, Component.translatable("simple_mod_sync.ui.modal.settings.scheme_file_url"), font);
                    cY += lh + padding;

                    // Schema file url
                    var schemaFileUrlBox = new EditBox(font, pX, cY, pW, 20, Component.empty());
                    schemaFileUrlBox.setMaxLength(368);
                    schemaFileUrlBox.setHint(Component.literal("https://example.com/sync.json"));
                    schemaFileUrlBox.setValue(Config.instance.getSchemaFileUrl());
                    schemaFileUrl.set(schemaFileUrlBox);
                    cY += 20 + padding * 2;

                    // Sync on game start
                    var syncOnStartupBox = Checkbox.builder(Component.translatable("simple_mod_sync.ui.modal.settings.sync_on_startup"), screen.getFont())
                            .pos(pX, cY).selected(Config.instance.getSyncOnStartup())
                            .build();
                    syncOnStartup.set(syncOnStartupBox);
                    cY += 20 + padding;

                    // Allow sync in progress popup
                    var allowSyncInProgressBox = Checkbox.builder(Component.translatable("simple_mod_sync.ui.modal.settings.allow_sync_in_progress_popup"), screen.getFont())
                            .pos(pX, cY).selected(Config.instance.isSyncInProgressPopupsAllowed())
                            .build();
                    allowSyncInProgress.set(allowSyncInProgressBox);
                    cY += 20 + padding;

                    // Allow game requires restart popup
                    var allowGameNeedsRestartBox = Checkbox.builder(Component.translatable("simple_mod_sync.ui.modal.settings.allow_game_needs_restart_popup"), screen.getFont())
                            .pos(pX, cY).selected(Config.instance.isSyncGameNeedsRestartAllowed())
                            .build();
                    allowGameNeedsRestart.set(allowGameNeedsRestartBox);
                    //cY += 20 + padding;

                    screen.addModalWidget(schemaTitle);
                    screen.addModalWidget(schemaFileUrlBox);
                    screen.addModalWidget(syncOnStartupBox);
                    screen.addModalWidget(allowSyncInProgressBox);
                    screen.addModalWidget(allowGameNeedsRestartBox);
                })
                .withPrimaryButton(Component.translatable("simple_mod_sync.ui.modal.save"), (_) -> {
                    var schemaUrl = schemaFileUrl.get().getValue();
                    var syncOnStart = syncOnStartup.get().selected();
                    var syncInProgress = allowSyncInProgress.get().selected();
                    var gamesNeedRestart = allowGameNeedsRestart.get().selected();

                    var cfg = Config.instance;
                    cfg.setSyncOnStartup(syncOnStart);
                    cfg.setSchemaFileUrl(schemaUrl);
                    cfg.setAllowSyncInProgressPopups(syncInProgress);
                    cfg.setAllowGameNeedsRestartPopups(gamesNeedRestart);

                    mc.setScreenAndShow(parentScreen);
                })
                .withSecondaryButton(Component.translatable("simple_mod_sync.ui.modal.cancel"), ( _) -> {
                    mc.setScreenAndShow(parentScreen);
                })
                .build();

        mc.setScreenAndShow(modal);
    }
}
