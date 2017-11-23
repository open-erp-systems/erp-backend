package com.jukusoft.erp.lib.utils;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class JsonUtils {

    public static JsonArray convertJsonObjectListToArray (List<JsonObject> rows) {
        //create new json array
        JsonArray array = new JsonArray();

        for (JsonObject row : rows) {
            array.add(row);
        }

        return array;
    }

}
