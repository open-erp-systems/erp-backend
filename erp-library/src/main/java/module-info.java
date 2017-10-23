module erp.library {
    requires hazelcast;
    requires json;
    requires vertx.core;

    exports com.jukusoft.erp.lib.module;
    exports com.jukusoft.erp.lib.gateway;
    exports com.jukusoft.erp.lib.message.request;
    exports com.jukusoft.erp.lib.message.response;
    exports com.jukusoft.erp.lib.logging;
    exports com.jukusoft.erp.lib.route;

}