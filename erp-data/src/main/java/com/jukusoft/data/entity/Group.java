package com.jukusoft.data.entity;

import com.jukusoft.erp.lib.json.JsonSerializable;
import io.vertx.core.json.JsonObject;

/**
 * Data Access Object (DAO) for rows of table "groups"
 */
public class Group implements JsonSerializable {

    //database row
    protected JsonObject row = null;

    /**
     * default constructor
     *
     * @param row database row of user
     */
    public Group (JsonObject row) {
        if (row == null) {
            throw new NullPointerException("row cannot be null.");
        }

        if (!row.containsKey("groupID")) {
            for (String colName : row.fieldNames()) {
                System.out.println("row coloum: " + colName);
            }

            throw new IllegalArgumentException("row is invalide, no column groupID exists.");
        }

        this.row = row;
    }

    public long getGroupID () {
        return this.row.getLong("groupID");
    }

    public String getName () {
        return this.row.getString("name");
    }

    public String getDescription () {
        return this.row.getString("description");
    }

    public String getColorHex () {
        return this.row.getString("color");
    }

    public boolean isAutoJoin () {
        return this.row.getInteger("auto_join") == 1;
    }

    public boolean isActivated () {
        return this.row.getInteger("activated") == 1;
    }

    @Override
    public JsonObject toJSON() {
        return this.row;
    }

    @Override
    public String toString () {
        return toJSON().encode();
    }
}
