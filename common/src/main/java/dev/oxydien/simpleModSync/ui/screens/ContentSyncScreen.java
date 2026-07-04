package dev.oxydien.simpleModSync.ui.screens;

import dev.oxydien.simpleModSync.SimpleModSync;
import dev.oxydien.simpleModSync.content.SyncSchema;
import dev.oxydien.simpleModSync.ui.ProgressHelper;
import dev.oxydien.simpleModSync.ui.modals.SettingsModalHandler;
import dev.oxydien.simpleModSync.ui.widgets.ContentProgressWidget;
import dev.oxydien.simpleModSync.ui.widgets.TotalSyncProgress;
import dev.oxydien.simpleModSync.workers.SyncWorker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ContentSyncScreen extends Screen {
    private static final int WIDGET_GAP = 4;
    private static final int CONTENT_WIDTH = 300;

    private SyncSchema schema;
    private SyncWorker worker;
    private final Screen parent;
    private ScrollableContentList contentList;
    private final ProgressHelper progressHelper;
    private String errorMessage;

    public ContentSyncScreen(Component title, @Nullable Screen parent) {
        super(title);
        this.schema = SimpleModSync.getInstance().syncSchema;
        this.worker = SimpleModSync.getInstance().syncWorker;
        this.parent = parent;
        this.progressHelper = new ProgressHelper(SimpleModSync.getInstance());
        this.errorMessage = "";
    }

    @Override
    protected void init() {
        super.init();
        final int heightOffset = 3;

        // Progress bar
        TotalSyncProgress barWidget = new TotalSyncProgress(0, 0, this.width, heightOffset, this.progressHelper);
        this.addRenderableOnly(barWidget);


        // Title
        Component titleText = Component.translatable("simple_mod_sync.ui.content_screen.title").withColor(0xFF3DF6B4);
        this.addRenderableOnly(
                new MultiLineTextWidget(this.width / 2 - titleText.getString().length() - 30, 10, titleText, this.font));


        // Initialize scrollable content list
        int contentLeft = this.width / 2 - 150;
        int listTop = 30;
        int listBottom = this.height - 35;

        this.contentList = new ScrollableContentList(
                CONTENT_WIDTH,
                listBottom - listTop,
                listTop,
                contentLeft
        );
        this.addWidget(this.contentList);
        this.initContent();

        // Navigation buttons
        var btnWidth = 92;
        var btnHalfWidth = btnWidth / 2;
        var btnHeight = 20;

        var btnY = this.height - btnHeight - 5;
        // - Sync button
        this.addRenderableWidget(new Button.Builder(Component.translatable("simple_mod_sync.ui.content_screen.sync_button"), (_) -> this.startSync())
                .pos(this.width / 2 - btnWidth - btnHalfWidth - 5, btnY).size(btnWidth, btnHeight).build());

        // - Settings button
        this.addRenderableWidget(new Button.Builder(Component.translatable("simple_mod_sync.ui.content_screen.settings_button"), (_) -> SettingsModalHandler.open(this))
                .pos(this.width / 2 - btnHalfWidth, btnY).size(btnWidth, btnHeight).build());

        // - Back button
        this.addRenderableWidget(new Button.Builder(Component.translatable("simple_mod_sync.ui.content_screen.back_button"), (_) -> Minecraft.getInstance().setScreen(this.parent))
                .pos(this.width / 2 + btnHalfWidth + 5, btnY).size(btnWidth, btnHeight).build());
    }

    private void initContent() {
        if (this.schema == null || this.contentList == null) return;
        this.contentList.clean(false);

        var progress = this.schema.getProgress();

        for (var iterator = progress.keys().asIterator(); iterator.hasNext();) {
            int key = iterator.next();

            ContentProgressWidget widget = new ContentProgressWidget(
                    0, 0, CONTENT_WIDTH, this.font, this.progressHelper, this.schema, key
            );
            this.contentList.addEntry(widget);
        }
    }

    private void updateState() {
        this.initContent();

        if (this.worker != null)
            if (this.worker.getStatus().isError()) {
                this.errorMessage = this.worker.getStatus().getErrorMessage();
            } else {
                this.errorMessage = "";
            }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        if (this.worker == null && SimpleModSync.getInstance().syncWorker != null) {
            this.schema = SimpleModSync.getInstance().syncSchema;
            this.worker = SimpleModSync.getInstance().syncWorker;
            this.worker.subscribeUpdateCallback(this::updateState);
        }

        if (this.contentList != null) {
            this.contentList.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        }

        if (this.worker != null && this.worker.getStatus().isError()) {
            guiGraphics.centeredText(this.font, this.worker.getStatus().getErrorMessage(), this.width / 2, 20, 0xFFFF1C1C);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.contentList != null && this.contentList.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }


    private void startSync() {
        SimpleModSync.getInstance().start();
        this.worker = null;
        this.schema = null;
    }

    // Inner class for scrollable list
    private static class ScrollableContentList extends AbstractContainerEventHandler implements Renderable, GuiEventListener, NarratableEntry {
        private final List<ContentProgressWidget> entries = new ArrayList<>();
        private final int width;
        private final int height;
        private final int top;
        private final int left;
        private double scrollAmount = 0;
        private boolean scrolling = false;

        public ScrollableContentList(int width, int height, int top, int left) {
            this.width = width;
            this.height = height;
            this.top = top;
            this.left = left;
        }

        public void addEntry(ContentProgressWidget widget) {
            this.entries.add(widget);
            this.updatePositions();
        }

        public void clean(boolean updatePositions) {
            this.entries.clear();
            if (updatePositions)
                this.updatePositions();
        }

        private void updatePositions() {
            int yPos = 0;
            for (ContentProgressWidget widget : this.entries) {
                widget.setX(this.left);
                widget.setY(this.top + yPos - (int) this.scrollAmount);
                yPos += widget.getHeight() + WIDGET_GAP;
            }
        }

        private int getContentHeight() {
            int total = 0;
            for (ContentProgressWidget widget : this.entries) {
                total += widget.getHeight() + WIDGET_GAP;
            }
            return Math.max(0, total - WIDGET_GAP);
        }

        private int getMaxScroll() {
            return Math.max(0, this.getContentHeight() - this.height);
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
            // Update positions in case heights changed
            this.updatePositions();

            // Enable scissor for clipping
            guiGraphics.enableScissor(this.left, this.top, this.left + this.width, this.top + this.height);

            for (ContentProgressWidget widget : this.entries) {
                if (this.isWidgetVisible(widget)) {
                    widget.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
                }
            }

            guiGraphics.disableScissor();

            // Draw scrollbar if needed
            if (this.getMaxScroll() > 0) {
                this.renderScrollbar(guiGraphics);
            }
        }

        private boolean isWidgetVisible(ContentProgressWidget widget) {
            int widgetTop = widget.getY();
            int widgetBottom = widgetTop + widget.getHeight();
            return widgetBottom >= this.top && widgetTop <= this.top + this.height;
        }

        private void renderScrollbar(GuiGraphicsExtractor guiGraphics) {
            int scrollbarX = this.left + this.width + 2;
            int scrollbarWidth = 6;

            // Scrollbar background
            guiGraphics.fill(scrollbarX, this.top, scrollbarX + scrollbarWidth,
                    this.top + this.height, 0xFF000000);

            // Scrollbar thumb
            int maxScroll = this.getMaxScroll();
            int thumbHeight = Math.max(20, (int) ((float) this.height / this.getContentHeight() * this.height));
            int thumbY = this.top + (int) ((this.scrollAmount / maxScroll) * (this.height - thumbHeight));

            guiGraphics.fill(scrollbarX, thumbY, scrollbarX + scrollbarWidth,
                    thumbY + thumbHeight, 0xFF808080);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            if (this.isMouseOver(mouseX, mouseY)) {
                this.scrollAmount = Mth.clamp(this.scrollAmount - scrollY * 10, 0, this.getMaxScroll());
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseDragged(@NotNull MouseButtonEvent event, double mouseX, double mouseY) {
            if (this.scrolling) {
                this.scrollAmount = Mth.clamp(this.scrollAmount - event.x(), 0, this.getMaxScroll());
                return true;
            }
            return super.mouseDragged(event, mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(@NotNull MouseButtonEvent event, boolean isDoubleClick) {
            this.scrolling = event.button() == 0 && this.isMouseOverScrollbar(event.x(), event.y());
            return super.mouseClicked(event, isDoubleClick);
        }

        @Override
        public boolean mouseReleased(@NotNull MouseButtonEvent event) {
            if (event.button() == 0) {
                this.scrolling = false;
            }
            return super.mouseReleased(event);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return mouseX >= this.left && mouseX <= this.left + this.width &&
                    mouseY >= this.top && mouseY <= this.top + this.height;
        }

        private boolean isMouseOverScrollbar(double mouseX, double mouseY) {
            int scrollbarX = this.left + this.width + 2;
            return mouseX >= scrollbarX && mouseX <= scrollbarX + 6 &&
                    mouseY >= this.top && mouseY <= this.top + this.height;
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return this.entries;
        }

        @Override
        public NarrationPriority narrationPriority() {
            return NarrationPriority.NONE;
        }

        @Override
        public void updateNarration(NarrationElementOutput narrationElementOutput) {

        }
    }
}