package dev.oxydien.simpleModSync.modification.handler;

import com.google.gson.JsonObject;
import dev.oxydien.simpleModSync.exception.JsonValidationException;
import dev.oxydien.simpleModSync.modification.Modification;
import dev.oxydien.simpleModSync.modification.RenameModification;
import dev.oxydien.simpleModSync.utils.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RenameModificationHandler extends ModificationHandler<RenameModification> {
    @Override
    public RenameModification ParseJson(JsonObject contentObject) {
        if (!contentObject.has("result")) {
            throw new JsonValidationException("result", "String");
        }

        Modification base = super.ParseJson(contentObject);

        String result  = contentObject.get("result").getAsString();

        return new RenameModification(base.getType(), base.getTiming(), base.getPattern(), base.getPath(), result);
    }

    @Override
    public void ApplyOn(RenameModification mod, Path filePath) throws IOException {
        Path parent = filePath.getParent();
        Files.move(filePath, parent.resolve(StringUtils.sanitizeDirectory(mod.getResult())));
    }
}
