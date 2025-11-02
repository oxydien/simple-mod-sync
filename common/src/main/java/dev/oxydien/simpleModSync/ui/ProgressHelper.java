package dev.oxydien.simpleModSync.ui;

import dev.oxydien.simpleModSync.SimpleModSync;
import dev.oxydien.simpleModSync.content.Content;
import dev.oxydien.simpleModSync.content.ContentType;
import dev.oxydien.simpleModSync.content.SyncStatus;
import dev.oxydien.simpleModSync.content.handler.ContentHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.Iterator;

public class ProgressHelper {
    private static final ResourceLocation LOADING_ICON = ResourceLocation.tryBuild(SimpleModSync.MOD_ID, "ui/textures/icons/loading.png");
    private static final ResourceLocation FINISHED_ICON = ResourceLocation.tryBuild(SimpleModSync.MOD_ID, "ui/textures/icons/finished.png");
    private static final ResourceLocation PARSING_ICON = ResourceLocation.tryBuild(SimpleModSync.MOD_ID, "ui/textures/icons/parsing.png");
    private static final ResourceLocation UNSYNCED_ICON = ResourceLocation.tryBuild(SimpleModSync.MOD_ID, "ui/textures/icons/unsynced.png");
    private static final ResourceLocation ERROR_ICON = ResourceLocation.tryBuild(SimpleModSync.MOD_ID, "ui/textures/icons/error.png");
    private static final ResourceLocation MODIFIED_ICON = ResourceLocation.tryBuild(SimpleModSync.MOD_ID, "ui/textures/icons/modified.png");
    private static final ResourceLocation PREPARING_ICON = ResourceLocation.tryBuild(SimpleModSync.MOD_ID, "ui/textures/icons/preparing.png");
    private static final ResourceLocation DOWNLOADING_ICON = ResourceLocation.tryBuild(SimpleModSync.MOD_ID, "ui/textures/icons/downloading.png");

    private static final ResourceLocation MOD_ICON = ResourceLocation.tryBuild(SimpleModSync.MOD_ID, "ui/textures/icons/mod.png");
    private static final ResourceLocation RESOURCEPACK_ICON = ResourceLocation.tryBuild(SimpleModSync.MOD_ID, "ui/textures/icons/resourcepack.png");
    private static final ResourceLocation SHADER_ICON = ResourceLocation.tryBuild(SimpleModSync.MOD_ID, "ui/textures/icons/shader.png");
    private static final ResourceLocation DATAPACK_ICON = ResourceLocation.tryBuild(SimpleModSync.MOD_ID, "ui/textures/icons/datapack.png");
    private static final ResourceLocation PACKED_ICON = ResourceLocation.tryBuild(SimpleModSync.MOD_ID, "ui/textures/icons/packed.png");
    private static final ResourceLocation UNKNOWN_ICON = ResourceLocation.tryBuild(SimpleModSync.MOD_ID, "ui/textures/icons/unknown.png");

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

    public void drawStatusIcon(SyncStatus syncStatus, GuiGraphics guiGraphics, int x, int y) {
        this.drawStatusIcon(syncStatus, guiGraphics, x, y, 3);
    }

    public void drawStatusIcon(SyncStatus syncStatus, GuiGraphics guiGraphics, int x, int y, int margin) {
        guiGraphics.blit(this.getStateIcon(syncStatus), x + margin, y + margin, 0, 0, 10, 10, 10, 10);
    }

    public void drawContentTypeIcon(ContentType type, GuiGraphics guiGraphics, int x, int y, int margin) {
        guiGraphics.blit(this.getContentTypeIcon(type), x + margin, y + margin, 0, 0, 10, 10, 10, 10);
    }

    public void drawLoadingIcon(GuiGraphics guiGraphics, int x, int y) {
        ++this.frameCall;
        int frameOffset = (int) (double) (this.frameCall / 6);
        if (frameOffset >= 8) {
            this.frameCall = 0;
            frameOffset = 0;
        }

        guiGraphics.blit(LOADING_ICON, x, y, 0, 16 * frameOffset, 16, 16, 16, 128);
    }

    private ResourceLocation getStateIcon(SyncStatus syncStatus) {
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

    private ResourceLocation getContentTypeIcon(ContentType type) {
        return switch (type) {
            case ContentType.Mod -> MOD_ICON;
            case ResourcePack -> RESOURCEPACK_ICON;
            case ShaderPack ->  SHADER_ICON;
            case DataPack ->   DATAPACK_ICON;
            case Packed, Config ->  PACKED_ICON;
            default -> UNKNOWN_ICON;
        };
    }
}
