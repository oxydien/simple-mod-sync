package dev.oxydien.simpleModSync.ui.widgets;

import dev.oxydien.simpleModSync.SimpleModSync;
import dev.oxydien.simpleModSync.content.SyncStatus;
import dev.oxydien.simpleModSync.ui.ProgressHelper;
import dev.oxydien.simpleModSync.ui.screens.ContentSyncScreen;
import dev.oxydien.simpleModSync.workers.SyncWorker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public final class TotalSyncStatus extends AbstractWidget {
    private static final int SIZE = 16;

    private final int x;
    private final int y;
    private final ProgressHelper progressHelper;
    private SyncWorker syncWorker;
    private boolean isFocused;

    private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("widget/button_disabled");

    public TotalSyncStatus(int x, int y, SyncWorker syncWorker,
                           ProgressHelper progressHelper) {
        super(x, y, SIZE, SIZE, Component.empty());
        this.x = x;
        this.y = y;
        this.syncWorker = syncWorker;
        this.progressHelper = progressHelper;
        this.setTooltip(Tooltip.create(SyncStatus.TranslatedState(SyncStatus.SyncState.UNSYNCED)));
        this.setTooltipDelay(Duration.of(200, ChronoUnit.MILLIS));
    }

    @Override
    public void extractWidgetRenderState(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float v) {
        if (this.syncWorker == null) {
            this.syncWorker = SimpleModSync.getInstance().syncWorker;
        }

        if (this.isFocused() || mouseX < this.x + SIZE && mouseX > this.x && mouseY < this.y + SIZE && mouseY > this.y)
        {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.x, this.y, SIZE, SIZE);
        }

        if (syncWorker == null) {
            this.progressHelper.drawStatusIcon(SyncStatus.ofState(SyncStatus.SyncState.UNSYNCED), guiGraphics, this.x, this.y);
            return;
        }

        this.progressHelper.drawStatusIcon(this.syncWorker.getStatus(), guiGraphics, this.x, this.y);

        if (this.syncWorker.isRunning()) {
            this.progressHelper.drawLoadingIcon(guiGraphics, this.x, this.y);
        }

        this.setTooltip(Tooltip.create(SyncStatus.TranslatedState(this.syncWorker.getStatus().getState())));
    }

    @Override
    public void onClick(@NotNull MouseButtonEvent event, boolean isDoubleClick) {
        super.onClick(event, isDoubleClick);
        Minecraft.getInstance().setScreen(new ContentSyncScreen(Component.translatable("simple_mod_sync.ui.sync_full_view.title"), null));
    }

    @Override
    public void setFocused(boolean b) {
        this.isFocused = b;
    }

    @Override
    public boolean isFocused() {
        return this.isFocused;
    }

    @Override
    public @NotNull NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.HINT, "Sync status");
    }
}
