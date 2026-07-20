package dev.oxydien.simpleModSync.ui;

import dev.oxydien.simpleModSync.SimpleModSync;
import dev.oxydien.simpleModSync.content.Content;
import dev.oxydien.simpleModSync.content.ContentType;
import dev.oxydien.simpleModSync.content.SyncStatus;
import dev.oxydien.simpleModSync.content.handler.ContentHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import java.util.Iterator;

public class ProgressHelper {
    private static final Identifier LOADING_ICON = Identifier.tryBuild(SimpleModSync.MOD_ID, "ui/textures/icons/loading.png");
    private static final Identifier FINISHED_ICON = Identifier.tryBuild(SimpleModSync.MOD_ID, "ui/textures/icons/finished.png");
    private static final Identifier PARSING_ICON = Identifier.tryBuild(SimpleModSync.MOD_ID, "ui/textures/icons/parsing.png");
    private static final Identifier UNSYNCED_ICON = Identifier.tryBuild(SimpleModSync.MOD_ID, "ui/textures/icons/unsynced.png");
    private static final Identifier ERROR_ICON = Identifier.tryBuild(SimpleModSync.MOD_ID, "ui/textures/icons/error.png");
    private static final Identifier MODIFIED_ICON = Identifier.tryBuild(SimpleModSync.MOD_ID, "ui/textures/icons/modified.png");
    private static final Identifier PREPARING_ICON = Identifier.tryBuild(SimpleModSync.MOD_ID, "ui/textures/icons/preparing.png");
    private static final Identifier DOWNLOADING_ICON = Identifier.tryBuild(SimpleModSync.MOD_ID, "ui/textures/icons/downloading.png");

    private static final Identifier MOD_ICON = Identifier.tryBuild(SimpleModSync.MOD_ID, "ui/textures/icons/mod.png");
    private static final Identifier RESOURCEPACK_ICON = Identifier.tryBuild(SimpleModSync.MOD_ID, "ui/textures/icons/resourcepack.png");
    private static final Identifier SHADER_ICON = Identifier.tryBuild(SimpleModSync.MOD_ID, "ui/textures/icons/shader.png");
    private static final Identifier DATAPACK_ICON = Identifier.tryBuild(SimpleModSync.MOD_ID, "ui/textures/icons/datapack.png");
    private static final Identifier PACKED_ICON = Identifier.tryBuild(SimpleModSync.MOD_ID, "ui/textures/icons/packed.png");
    private static final Identifier UNKNOWN_ICON = Identifier.tryBuild(SimpleModSync.MOD_ID, "ui/textures/icons/unknown.png");

    private final SimpleModSync plugin;
    private int frameCall = 0;

    public ProgressHelper(SimpleModSync instance) {
        this.plugin = instance;
    }

    public float getOverallProgress() {
        if (plugin == null || plugin.syncSchema == null) {
            return 0;
        }

        int allElements = 0;
        float progressSum = 0;

        var present = plugin.syncSchema.getProgress();
        for (Iterator<Integer> it = present.keys().asIterator(); it.hasNext(); ) {
            int key = it.next();

            Content content = plugin.syncSchema.getContents().get(key);
            if (content == null) {
                continue;
            }

            SyncStatus progress = plugin.syncSchema.getProgress().get(key);

            ContentHandler handler = plugin.Handlers.getContentHandler(content.getType());

            if (handler == null) {
                continue;
            }

            ++allElements;
            float contentProgress = handler.GetProgress(content, progress);
            progressSum += contentProgress;
        }

        return progressSum / allElements;
    }

    public void drawStatusIcon(SyncStatus syncStatus, GuiGraphicsExtractor guiGraphics, int x, int y) {
        this.drawStatusIcon(syncStatus, guiGraphics, x, y, 3);
    }

    public void drawStatusIcon(SyncStatus syncStatus, GuiGraphicsExtractor guiGraphics, int x, int y, int margin) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, this.getStateIcon(syncStatus), x + margin, y + margin, 0, 0, 10, 10, 10, 10);
    }

    public void drawContentTypeIcon(ContentType type, GuiGraphicsExtractor guiGraphics, int x, int y, int margin) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, this.getContentTypeIcon(type), x + margin, y + margin, 0, 0, 10, 10, 10, 10);
    }

    public void drawLoadingIcon(GuiGraphicsExtractor guiGraphics, int x, int y) {
        ++this.frameCall;
        int frameOffset = (int) (double) (this.frameCall / 6);
        if (frameOffset >= 8) {
            this.frameCall = 0;
            frameOffset = 0;
        }

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, LOADING_ICON, x, y, 0, 16 * frameOffset, 16, 16, 16, 128);
    }

    private Identifier getStateIcon(SyncStatus syncStatus) {
        return switch (syncStatus.getState()) {
            case UNSYNCED -> UNSYNCED_ICON;
            case FINISHED -> FINISHED_ICON;
            case STARTING -> PREPARING_ICON;
            case PARSING -> PARSING_ICON;
            case DOWNLOADING -> DOWNLOADING_ICON;
            case MODIFIED -> MODIFIED_ICON;
            default -> ERROR_ICON;
        };
    }

    private Identifier getContentTypeIcon(ContentType type) {
        return switch (type) {
            case ContentType.Mod -> MOD_ICON;
            case ResourcePack -> RESOURCEPACK_ICON;
            case ShaderPack ->  SHADER_ICON;
            case DataPack ->   DATAPACK_ICON;
            default -> UNKNOWN_ICON;
        };
    }
}
