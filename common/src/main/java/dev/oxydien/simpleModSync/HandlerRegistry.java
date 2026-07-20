package dev.oxydien.simpleModSync;

import dev.oxydien.simpleModSync.content.ContentType;
import dev.oxydien.simpleModSync.content.ContentTypeUtils;
import dev.oxydien.simpleModSync.content.handler.ContentHandler;
import dev.oxydien.simpleModSync.content.handler.GameContentHandler;
import dev.oxydien.simpleModSync.content.handler.ModContentHandler;
import dev.oxydien.simpleModSync.modification.handler.ModificationHandler;
import dev.oxydien.simpleModSync.modification.handler.RemoveModificationHandler;
import dev.oxydien.simpleModSync.modification.handler.RenameModificationHandler;

import java.util.HashMap;

public class HandlerRegistry {
    private final HashMap<String, ContentHandler<?>> contentHandlers = new HashMap<>();
    private final HashMap<String, ModificationHandler<?>> modificationHandlers = new HashMap<>();

    public HandlerRegistry() {}

    public ContentHandler<?> getContentHandler(String id) {
        String normalized = id.trim().toLowerCase();
        return contentHandlers.get(normalized);
    }

    public ContentHandler<?> getContentHandler(ContentType id) {
        return this.getContentHandler(ContentTypeUtils.ToString(id));
    }

    public ModificationHandler<?> getModificationHandler(String id) {
        String normalized = id.trim().toLowerCase();
        return modificationHandlers.get(normalized);
    }

    public void registerContentHandler(String id, ContentHandler<?> handler) {
        contentHandlers.put(id, handler);
    }
    public void registerModificationHandler(String id, ModificationHandler<?> handler) {
        modificationHandlers.put(id, handler);
    }

    public void init() {
        this.contentHandlers.put("mod", new ModContentHandler());
        this.contentHandlers.put("resourcepack", new GameContentHandler("resourcepacks"));
        this.contentHandlers.put("datapack", new GameContentHandler("datapacks"));
        this.contentHandlers.put("shader", new GameContentHandler("shaderpacks"));

        // "packed" and "config"
        //ContentHandler packedContentHandler = new PackedContentHandler();
        //this.contentHandlers.put("packed", packedContentHandler);
        //this.contentHandlers.put("config", packedContentHandler);

        this.modificationHandlers.put("remove", new RemoveModificationHandler());
        this.modificationHandlers.put("rename", new RenameModificationHandler());
    }
}
