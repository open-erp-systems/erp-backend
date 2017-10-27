package com.jukusoft.erp.lib.json;

import io.vertx.core.json.JsonObject;
import org.json.JSONObject;

/**
 * Created by Justin on 10.02.2017.
 */
public interface JsonLoadable {

    public void loadFromJSON(JsonObject json);

}
