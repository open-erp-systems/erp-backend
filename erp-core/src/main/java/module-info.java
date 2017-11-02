module com.jukusoft.erp.core {
    requires java.base;

    requires java.logging;

    requires erp.library;
    requires vertx.core;
    requires erp.data;

    exports com.jukusoft.erp.core.exception;
    exports com.jukusoft.erp.core.module.base;
}