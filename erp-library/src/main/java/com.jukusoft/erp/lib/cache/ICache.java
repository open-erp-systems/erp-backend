package com.jukusoft.erp.lib.cache;

import io.vertx.core.json.JsonObject;

public interface ICache {

    public void put (String key, JsonObject data);

    public void remove (String key);

    public boolean contains (String key);

    public JsonObject get (String key);

    public void cleanUp ();

}
