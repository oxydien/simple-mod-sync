package dev.oxydien.simpleModSync.ui.modals;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import net.minecraft.client.gui.components.AbstractWidget;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BasicModalWindow extends Screen {
    private final Screen parentScreen;

    private final int modalWidth;
    private final int modalHeight;
    private int topLeftX;
    private int topLeftY;

    private final Component primaryBtnText;
    private final Button.OnPress primaryBtnAction;
    @Nullable
    private final Component secondaryBtnText;
    @Nullable
    private final Button.OnPress secondaryBtnAction;
    private final List<Consumer<BasicModalWindow>> widgetInitializers;

    protected BasicModalWindow(Builder builder) {
        super(builder.title);
        this.parentScreen = builder.parentScreen;
        this.modalWidth = builder.width;
        this.modalHeight = builder.height;

        this.primaryBtnText = builder.primaryBtnText;
        this.primaryBtnAction = builder.primaryBtnAction != null ? builder.primaryBtnAction : (_) -> this.onClose();

        this.secondaryBtnText = builder.secondaryBtnText;
        this.secondaryBtnAction = builder.secondaryBtnAction;

        this.widgetInitializers = builder.widgetInitializers;
    }

    public int getTopLeftX() { return topLeftX; }
    public int getTopLeftY() { return topLeftY; }
    public int getModalWidth() { return modalWidth; }
    public int getModalHeight() { return modalHeight; }

    public <T extends AbstractWidget> T addModalWidget(T widget) {
        return this.addRenderableWidget(widget);
    }

    @Override
    protected void init() {
        this.topLeftX = (this.width - this.modalWidth) / 2;
        this.topLeftY = (this.height - this.modalHeight) / 2;

        int buttonY = this.topLeftY + this.modalHeight - 30;

        if (this.secondaryBtnAction != null && this.secondaryBtnText != null) {
            int buttonWidth = 80;
            int gap = 10;
            int startX = this.topLeftX + (this.modalWidth - (buttonWidth * 2 + gap)) / 2;

            this.addRenderableWidget(Button.builder(this.secondaryBtnText, this.secondaryBtnAction)
                    .bounds(startX, buttonY, buttonWidth, 20)
                    .build());

            this.addRenderableWidget(Button.builder(this.primaryBtnText, this.primaryBtnAction)
                    .bounds(startX + buttonWidth + gap, buttonY, buttonWidth, 20)
                    .build());
        } else {
            int buttonWidth = 100;
            int startX = this.topLeftX + (this.modalWidth - buttonWidth) / 2;

            this.addRenderableWidget(Button.builder(this.primaryBtnText, this.primaryBtnAction)
                    .bounds(startX, buttonY, buttonWidth, 20)
                    .build());
        }

        for (Consumer<BasicModalWindow> initializer : this.widgetInitializers) {
            initializer.accept(this);
        }
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        int borderThickness = 1;
        guiGraphics.fill(topLeftX, topLeftY, topLeftX + modalWidth, topLeftY + modalHeight, 0xFF808080);
        guiGraphics.fill(topLeftX + borderThickness, topLeftY + borderThickness,
                topLeftX + modalWidth - borderThickness, topLeftY + modalHeight - borderThickness, 0xFF2D2D2D);

        guiGraphics.centeredText(this.font, this.title, this.width / 2, topLeftY + 10, 0xFFFFFFFF);

        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parentScreen);
    }


    public static class Builder {
        private final Component title;
        private final Screen parentScreen;

        private int width = 250;
        private int height = 150;

        private Component primaryBtnText = Component.translatable("simple_mod_sync.ui.modal.close_btn");
        @Nullable
        private Button.OnPress primaryBtnAction = null;
        @Nullable
        private Component secondaryBtnText = null;
        @Nullable
        private Button.OnPress secondaryBtnAction = null;

        private final List<Consumer<BasicModalWindow>> widgetInitializers = new ArrayList<>();

        public Builder(Component title, Screen parentScreen) {
            this.title = title;
            this.parentScreen = parentScreen;
        }

        public Builder withSize(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder withPrimaryButton(Component text, Button.OnPress action) {
            this.primaryBtnText = text;
            this.primaryBtnAction = action;
            return this;
        }

        public Builder withSecondaryButton(Component text, Button.OnPress action) {
            this.secondaryBtnText = text;
            this.secondaryBtnAction = action;
            return this;
        }

        public Builder addBodyWidget(WidgetPlacer placer) {
            this.widgetInitializers.add(screen ->
                    placer.place(screen, screen.getTopLeftX(), screen.getTopLeftY(), screen.getModalWidth(), screen.getModalHeight())
            );
            return this;
        }

        public BasicModalWindow build() {
            return new BasicModalWindow(this);
        }
    }

    @FunctionalInterface
    public interface WidgetPlacer {
        void place(BasicModalWindow screen, int mX, int mY, int mW, int mH);
    }
}
