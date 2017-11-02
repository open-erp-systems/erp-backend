package com.jukusoft.data.entity;

import io.vertx.core.json.JsonObject;

public class GroupMember {

    protected Group group = null;
    protected long userID = 0;
    protected JsonObject row = null;

    public GroupMember (JsonObject row, Group group) {
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
        this.group = group;
    }

    public GroupMember (JsonObject row) {
        this(row, null);
    }

    public void setGroup (Group group) {
        this.group = group;
    }

    public Group getGroup() {
        if (this.group == null) {
            throw new IllegalStateException("group instance wasnt set by repository.");
        }

        return this.group;
    }

    public boolean isOwner () {
        return row.getInteger("is_owner") == 1;
    }

    public boolean isMainGroup () {
        return row.getInteger("maingroup") == 1;
    }

}
