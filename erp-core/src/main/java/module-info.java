module erp.core {
    requires java.base;

    requires java.logging;

    requires erp.library;
    requires vertx.core;

    exports com.jukusoft.erp.core.exception;
}