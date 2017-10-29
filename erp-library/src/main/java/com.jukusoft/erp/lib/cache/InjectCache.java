package com.jukusoft.erp.lib.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectCache {

    String name ();
    CacheTypes type ();
    boolean nullable () default true;

}
