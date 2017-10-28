package com.jukusoft.data.entity;

import com.jukusoft.erp.lib.json.JsonSerializable;
import io.vertx.core.json.JsonObject;

/**
* Data Access Object (DAO) for rows of table "users"
*/
public class User implements JsonSerializable {

    //database row
    protected JsonObject row = null;

    /**
    * default constructor
     *
     * @param row database row of user
    */
    public User (JsonObject row) {
        if (row == null) {
            throw new NullPointerException("row cannot be null.");
        }

        if (!row.containsKey("userID")) {
            for (String colName : row.fieldNames()) {
                System.out.println("row coloum: " + colName);
            }

            throw new IllegalArgumentException("row is invalide, no column userID exists.");
        }

        this.row = row;
    }

    /**
    * get userID
     *
     * @return user id
    */
    public long getUserID () {
        return this.row.getLong("userID");
    }

    /**
    * get username
     *
     * @return username
    */
    public String getUsername () {
        return this.row.getString("username");
    }

    /**
    * get mail of user
     *
     * @return mail of user or null, if user doesnt have an mail
    */
    public String getMail () {
        //mail can be Null, so we have to check if field exists
        if (!this.row.containsKey("mail")) {
            return null;
        }

        return this.row.getString("mail");
    }

    public String getHashMethod () {
        return this.row.getString("hash_method");
    }

    public String getPrename () {
        return this.row.getString("prename");
    }

    public String getName () {
        return this.row.getString("name");
    }

    public boolean isOnline () {
        return this.row.getInteger("online") == 1;
    }

    public boolean isActivated () {
        return this.row.getInteger("activated") == 1;
    }

    @Override
    public JsonObject toJSON() {
        JsonObject json = null;

        //copy row
        json = this.row.copy();

        //add timestamp for caching
        json.put("cache_created", System.currentTimeMillis());
        json.put("cache_last_access", System.currentTimeMillis());

        return json;
    }

    public static User createFromCache (JsonObject json) {
        User user = new User(json);

        return user;
    }

}
