package dev.oxydien.ui.widget;

import dev.oxydien.data.ContentSyncProgress;
import dev.oxydien.data.SyncData;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ContentSyncProgressWidget implements Widget, Drawable, Element, Selectable {
    private int x;
    private int y;
    private final int width;
    private final int height;
    private final TextRenderer textRenderer;
    private final SyncData.Content content;
    @Nullable private ContentSyncProgress progress;

    public ContentSyncProgressWidget(int x, int y, int width, int height, TextRenderer textRenderer, SyncData.Content content, @Nullable ContentSyncProgress progress) {
        this.setPosition(x, y);
        this.width = width;
        this.height = height;
        this.textRenderer = textRenderer;
        this.content = content;
        this.progress = progress;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Background
        context.fill(RenderLayer.getGuiOverlay(), this.getX(), this.getY(), this.getX() + this.getWidth(),
                this.getY() + this.getHeight(), 0xa0000000);

        // Progress bar
        int progressNum = this.progress != null ? this.progress.getProgress() : 0;
        if (this.progress != null) {
            context.fill(this.getX(), this.getY(),
                    this.getX() + (int) (this.getWidth() * (float) (progressNum) / (float) 100), this.getY() + 1, 0xa3FFFFFF);
        }

        int textHeight = this.getY() + this.getHeight() / 2 - this.textRenderer.fontHeight / 2 + 2;

        // Outcome
        String outcomeText = progressNum >= 100 && this.progress != null && !this.progress.isError() ? "COMPLETE" :
                (this.progress != null ? this.progress.getOutcome().name() : "AWAITING_WORKER");
        context.drawText(this.textRenderer, Text.translatable(String.format("simple_mod_sync.ui.outcome.%s", outcomeText.toLowerCase())),
                this.getX()  + this.getWidth() - this.textRenderer.getWidth(outcomeText) - 3, textHeight, 0xFFF13FFF, false);

        // Outcome exception
        if (this.progress != null && this.progress.isError() && this.progress.getException() != null) {
            context.drawText(this.textRenderer, Text.translatable("simple_mod_sync.ui.error.exception").getString(),
                    this.getX() + 5, this.getY() + 2, 0xFF55FFFF, false);
        }

        // Mod name
        String modName = this.content.getModName();
        context.drawText(this.textRenderer, String.format("§l%s§r", modName.substring(0, Math.min(22, modName.length()))),
                this.getX() + 15, textHeight, 0xFF55FFFF, false);
        context.drawText(this.textRenderer, this.content.getType(), this.getX() + 180,
                textHeight, 0xFFFFFFFF, false);
    }

    public int getProgress() {
        return this.progress != null ? this.progress.getProgress() : 0;
    }

    public void setProgress(@Nullable ContentSyncProgress progress) {
        this.progress = progress;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        Element.super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return Element.super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return Element.super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return Element.super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return Element.super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return Element.super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return Element.super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return Element.super.charTyped(chr, modifiers);
    }

    @Override
    public @Nullable GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
        return Element.super.getNavigationPath(navigation);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return Element.super.isMouseOver(mouseX, mouseY);
    }

    @Override
    public void setFocused(boolean focused) {
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public @Nullable GuiNavigationPath getFocusedPath() {
        return Element.super.getFocusedPath();
    }

    @Override
    public SelectionType getType() {
        return SelectionType.NONE;
    }

    @Override
    public boolean isNarratable() {
        return Selectable.super.isNarratable();
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public ScreenRect getNavigationFocus() {
        return Widget.super.getNavigationFocus();
    }

    @Override
    public void setPosition(int x, int y) {
        Widget.super.setPosition(x, y);
    }

    @Override
    public void forEachChild(Consumer<ClickableWidget> consumer) {
    }

    @Override
    public int getNavigationOrder() {
        return Element.super.getNavigationOrder();
    }
}
