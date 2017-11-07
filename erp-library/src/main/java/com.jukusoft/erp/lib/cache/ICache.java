package com.jukusoft.erp.lib.cache;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public interface ICache {

    public void put (String key, JsonObject data);

    default void putArray (String key, JsonArray array) {
        JsonObject obj = new JsonObject();
        obj.put("array", array);

        put(key, obj);
    }

    public void remove (String key);

    public boolean contains (String key);

    public JsonObject get (String key);

    default JsonArray getArray (String key) {
        JsonObject obj = get(key);

        if (obj == null) {
            return null;
        }

        if (!obj.containsKey("array")) {
            throw new IllegalStateException("cached object isnt an array.");
        }

        return obj.getJsonArray("array");
    }

    public void cleanUp ();

}
