package dev.oxydien.simpleModSync.ui.widgets;

import dev.oxydien.simpleModSync.log.Log;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LabelBox extends AbstractWidget {
    public enum Alignment { LEFT, CENTER, RIGHT }

    private final Font font;
    private Alignment alignment = Alignment.LEFT;
    private int color = 0xFFFFFFFF;
    private int lineHeight;
    private boolean dropShadow = false;

    private List<FormattedCharSequence> cachedLines;

    public LabelBox(int x, int y, int width, int height, Component message, Font font) {
        super(x, y, width, height, message);
        this.font = font;
        this.lineHeight = font.lineHeight + 1;
        this.wrapText();
    }


    public LabelBox setAlignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public LabelBox setColor(int color) {
        this.color = color;
        return this;
    }

    public LabelBox setLineHeight(int height) {
        this.lineHeight = height;
        return this;
    }

    public LabelBox setDropShadow(boolean dropShadow) {
        this.dropShadow = dropShadow;
        return this;
    }

    @Override
    public void setMessage(Component message) {
        super.setMessage(message);
        this.wrapText();
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        this.wrapText();
    }

    private void wrapText() {
        this.cachedLines = this.font.split(this.getMessage(), this.width);
    }

    @Override
    protected void extractWidgetRenderState(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.cachedLines == null || this.cachedLines.isEmpty()) return;

        int currentY = this.getY();

        for (FormattedCharSequence line : this.cachedLines) {
            int lineWidth = this.font.width(line);
            int currentX = this.getX();

            switch (this.alignment) {
                case CENTER -> currentX += (this.width - lineWidth) / 2;
                case RIGHT -> currentX += (this.width - lineWidth);
                case LEFT -> {}
            }

            guiGraphics.text(this.font, line, currentX, currentY, this.color, this.dropShadow);
            currentY += this.lineHeight;

            if (currentY + this.lineHeight > this.getY() + this.height) {
                break;
            }
        }
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
    }
}
