package dev.oxydien.simpleModSync.modification.handler;

import com.google.gson.JsonObject;
import dev.oxydien.simpleModSync.exception.JsonValidationException;
import dev.oxydien.simpleModSync.log.Log;
import dev.oxydien.simpleModSync.modification.*;
import dev.oxydien.simpleModSync.utils.DirUtils;
import dev.oxydien.simpleModSync.utils.ListUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public abstract class ModificationHandler<T extends Modification> {
    public Modification ParseJson(JsonObject contentObject) {
        if (!contentObject.has("type")) {
            throw new JsonValidationException("type", "String (remove | rename)");
        }

        if  (!contentObject.has("pattern")) {
            throw new JsonValidationException("pattern", "String");
        }

        String typeStr = contentObject.get("type").getAsString();
        ModificationType type = ModificationTypeUtils.FromString(typeStr);

        String timingStr = contentObject.has("when") ? contentObject.get("when").getAsString() : "";
        ModificationTiming timing = ModificationTimingUtils.FromString(timingStr);

        String pattern = contentObject.get("pattern").getAsString();

        String path = contentObject.has("path") ? contentObject.get("path").getAsString() : ".";

        return new Modification(type, timing, pattern, path);
    }

    public List<Path> GetRelevantPaths(T mod, Path basePath) {
        Path workingDir = this.GetWorkingDirectory(mod, basePath);

        return DirUtils.GetFilePaths(workingDir);
    }

    public Path GetWorkingDirectory(T mod, Path basePath) {
        return DirUtils.sanitizePath(basePath, mod.getPath());
    }

    public Pattern GetPattern(T mod) {
        return Pattern.compile(mod.getPattern());
    }

    public abstract void ApplyOn(T mod, Path filePath) throws IOException;

    public void Execute(T mod, Path basePath) throws Exception {
        List<Path> relevantPaths = this.GetRelevantPaths(mod, basePath);
        Path wod = this.GetWorkingDirectory(mod, basePath);

        List<String> sanitized = new ArrayList<>();
        for (Path path : relevantPaths) {
            Path absolute = path.toAbsolutePath();
            String relativePath = wod.relativize(absolute).toString();
            sanitized.add(relativePath);
        }

        Log.debug("Running mod", mod.getPattern(), "in", mod.getPath(), "on", sanitized.size(), "possible items.", "First 5:", ListUtils.peek(sanitized));
        List<String> matches = new ArrayList<>();
        Pattern pattern = this.GetPattern(mod);

        for (var filePath : sanitized) {
            var matcher = pattern.matcher(filePath);
            if (matcher.matches()) {
                matches.add(filePath);
                Log.debug("Execute.ModificationHandler", "Found match for {} at {}", mod.getPattern(), filePath);
                break;
            }
        }

        for (var match : matches) {
            Path filePath = wod.resolve(match);
            this.ApplyOn(mod, filePath);
        }
    }
}
