package dev.oxydien.simpleModSync.ui.widgets;

import dev.oxydien.simpleModSync.SimpleModSync;
import dev.oxydien.simpleModSync.content.*;
import dev.oxydien.simpleModSync.content.handler.ContentHandler;
import dev.oxydien.simpleModSync.ui.ProgressHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class ContentProgressWidget extends AbstractWidget {
    private static final int WIDGET_DEFAULT_HEIGHT = 28;
    private static final int DEFAULT_PADDING = 4;

    private final Font font;
    private final ProgressHelper helper;

    private final int index;
    private final SyncSchema schema;
    private ContentInfo contentInfo;
    private ContentHandler handler;
    private SyncStatus status;

    public ContentProgressWidget(int x, int y, int width, Font font, ProgressHelper helper, SyncSchema schema, int index) {
        super(x, y, width, WIDGET_DEFAULT_HEIGHT, Component.empty());

        this.font = font;
        this.helper = helper;

        this.index = index;
        this.schema = schema;

        this.setTooltipDelay(Duration.of(500, ChronoUnit.MILLIS));

        this.fetchData();
    }

    private void fetchData() {
        if (schema.getProgress().containsKey(this.index)) {
            this.status = schema.getProgress().get(index);
        }

        if (schema.getContents().containsKey(index)) {
            Content content = schema.getContents().get(index);
            this.handler = SimpleModSync.getInstance().Handlers.getContentHandler(content.getType());
            this.contentInfo = this.handler.GetInfo(content);
        }

        this.updateTooltip();
    }

    private void updateTooltip() {
        var builder = Component.empty();
        if (this.contentInfo != null) {
            builder.append(Component.translatable(String.format("simple_mod_sync.ui.content_type.%s", ContentTypeUtils.ToString(this.contentInfo.GetContentType()))));
            builder.append(" ");
        }
        builder.append(this.getModName());
        builder.append(": ");
        if (this.status != null) {
            builder.append("\n");
            builder.append(SyncStatus.TranslatedState(this.status.getState()));
        }
        this.setTooltip(Tooltip.create(builder));
    }

    @Override
    public int getHeight() {
        int base = super.getHeight();
        if (this.status != null && this.status.getState() == SyncStatus.SyncState.ERROR) {
            return base + this.font.lineHeight + DEFAULT_PADDING;
        }
        return base;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int i, int i1, float v) {
        this.fetchData(); // This might not be the most optimized way

        guiGraphics.fill(RenderPipelines.GUI, this.getX(), this.getY(), this.getX() + this.getWidth(),
                this.getY() + this.getHeight(), 0xa0000000);

        if (this.status != null) {
            // Progress bar
            if (contentInfo != null) {
                guiGraphics.fill(
                        this.getX(),
                        this.getY(),
                        (int) (this.getX() + (((float) this.width) * this.handler.GetProgress(this.contentInfo.GetContent(), this.status))),
                        this.getY() + 3,
                        0xFFFFFFFF
                );
            }

            // Status icon
            this.helper.drawStatusIcon(this.status, guiGraphics, this.getX() + DEFAULT_PADDING, this.getY() + DEFAULT_PADDING + 2, 0);

            if (this.status.getState() == SyncStatus.SyncState.ERROR) {
                String msg = this.status.getErrorMessage();
                guiGraphics.text(this.font, String.format("§c%s§r", msg), this.getX() + DEFAULT_PADDING + 12,
                        this.getY() + DEFAULT_PADDING * 2 + this.font.lineHeight, 0xFF55FFFF, false);
            }
        }

        if (this.contentInfo != null) {
            this.helper.drawContentTypeIcon(this.contentInfo.GetContentType(), guiGraphics,
                    this.getX() + DEFAULT_PADDING, this.getY() + DEFAULT_PADDING + 12, 0);
        }

        guiGraphics.text(this.font, String.format("§l%s§r", this.getModName()),
                this.getX() + 20 + DEFAULT_PADDING, this.getY() + DEFAULT_PADDING + this.font.lineHeight / 2, 0xFF55FFFF, false);
    }

    private String getModName() {
        return this.contentInfo == null ? this.index + " ???" : this.contentInfo.GetTitle();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
