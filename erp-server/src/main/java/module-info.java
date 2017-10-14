module erp.server {
    requires java.base;

    //dependencies
    requires vertx.core;

    //requires jdk.unsupported;
    requires hazelcast;
    requires vertx.hazelcast;
    requires java.sql;

    //exports
    exports com.jukusoft.erp.server;
}