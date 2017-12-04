module erp.library {
    requires hazelcast;
    requires json;
    requires vertx.core;

    //bouncy castle, for SSL self-signed certificate generation
    requires bcprov.jdk15on;
    requires cache.api;
    requires vertx.sql.common;
    requires vertx.jdbc.client;
    requires vertx.sync;

    exports com.jukusoft.erp.lib.module;
    exports com.jukusoft.erp.lib.gateway;
    exports com.jukusoft.erp.lib.message;
    exports com.jukusoft.erp.lib.message.request;
    exports com.jukusoft.erp.lib.message.response;
    exports com.jukusoft.erp.lib.logging;
    exports com.jukusoft.erp.lib.route;
    exports com.jukusoft.erp.lib.annotation;
    exports com.jukusoft.erp.lib.session;
    exports com.jukusoft.erp.lib.controller;
    exports com.jukusoft.erp.lib.utils;
    exports com.jukusoft.erp.lib.database;
    exports com.jukusoft.erp.lib.context;
    exports com.jukusoft.erp.lib.cache;
    exports com.jukusoft.erp.lib.exception;
    exports com.jukusoft.erp.lib.permission;
    exports com.jukusoft.erp.lib.json;

}