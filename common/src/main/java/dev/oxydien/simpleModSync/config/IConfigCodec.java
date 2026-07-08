package dev.oxydien.simpleModSync.config;

import com.google.gson.JsonObject;

public interface IConfigCodec {
    public void writeTo(JsonObject parent);
    public void readFrom(JsonObject parent);
}
