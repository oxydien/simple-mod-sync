package dev.oxydien.simpleModSync.modification.handler;

import com.google.gson.JsonObject;
import dev.oxydien.simpleModSync.exception.JsonValidationException;
import dev.oxydien.simpleModSync.modification.Modification;
import dev.oxydien.simpleModSync.modification.RenameModification;
import dev.oxydien.simpleModSync.utils.DirUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

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
        var targetPath = parent.resolve(mod.getResult());
        var resultPath = DirUtils.sanizizePath(targetPath);
        Files.move(filePath, resultPath, StandardCopyOption.REPLACE_EXISTING);
    }
}
