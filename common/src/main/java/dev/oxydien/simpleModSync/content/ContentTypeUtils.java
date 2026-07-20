package dev.oxydien.simpleModSync.content;

public class ContentTypeUtils {
    public static String ToString(ContentType type) {
        return switch (type) {
            case Mod -> "mod";
            case ResourcePack -> "resourcepack";
            case ShaderPack -> "shader";
            case DataPack -> "datapack";
        };
    }

    public static ContentType FromString(String type) {
        return switch (type) {
            case "resourcepack" -> ContentType.ResourcePack;
            case "datapack"  -> ContentType.DataPack;
            case "shader"  -> ContentType.ShaderPack;
            default -> ContentType.Mod;
        };
    }
}
