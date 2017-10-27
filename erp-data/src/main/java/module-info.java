module erp.data {
    requires erp.library;
    requires json;
    requires vertx.sql.common;
    requires vertx.core;

    exports com.jukusoft.data.repository;
    exports com.jukusoft.data.entity;

}