package com.jukusoft.erp.lib.json;

import io.vertx.core.json.JsonObject;

/**
 * Created by Justin on 08.02.2017.
 */
public interface JsonSerializable {

    public JsonObject toJSON();

}
