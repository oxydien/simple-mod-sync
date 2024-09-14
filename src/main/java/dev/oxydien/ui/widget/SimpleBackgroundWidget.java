package dev.oxydien.ui.widget;

import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.render.RenderLayer;

import java.util.function.Consumer;

public class SimpleBackgroundWidget implements Widget, Drawable, Element, Selectable {
    private int x;
    private int y;
    private int width;
    private int height;
    private int color;

    public SimpleBackgroundWidget(int x, int y, int width, int height, int color) {
        this.setPosition(x, y);
        this.color = (color == 0) ? 0xa0000000 : color;
        this.width = width;
        this.height = height;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(RenderLayer.getGuiOverlay(), this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), this.color);
    }

    @Override
    public void setFocused(boolean focused) {
        return;
    }

    @Override
    public boolean isFocused() {
        return false;
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

    public int getColor() {
        return this.color;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public ScreenRect getNavigationFocus() {
        return Widget.super.getNavigationFocus();
    }

    @Override
    public void forEachChild(Consumer<ClickableWidget> consumer) {

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
}
