package com.jukusoft.erp.lib.database.impl;

import com.hazelcast.core.HazelcastInstance;
import com.jukusoft.erp.lib.database.*;
import io.vertx.core.Vertx;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseManagerImpl implements DatabaseManager {

    //map with all repositories
    protected Map<Class,Repository> repositories = new ConcurrentHashMap<>();

    //instance of vertx
    protected Vertx vertx = null;

    //instance of mysql database
    protected MySQLDatabase mySQLDatabase = null;

    //instance of hazelcast
    protected HazelcastInstance hazelcastInstance = null;

    /**
    * default constructor
     *
     * @param vertx instance of vertx
     * @param mySQLDatabase instance of mysql database
     * @param hazelcastInstance instance of hazelcast
    */
    public DatabaseManagerImpl (Vertx vertx, MySQLDatabase mySQLDatabase, HazelcastInstance hazelcastInstance) {
        if (this.vertx == null) {
            throw new NullPointerException("vertx instance cannot be null.");
        }

        if (this.mySQLDatabase == null) {
            throw new NullPointerException("mysql database cannot be null.");
        }

        if (!this.mySQLDatabase.isConnected()) {
            throw new IllegalStateException("mysql connection isnt established yet.");
        }

        if (hazelcastInstance == null) {
            throw new NullPointerException("hazelcast instance cannot be null.");
        }

        this.vertx = vertx;
        this.mySQLDatabase = mySQLDatabase;
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public <T extends Repository> void addRepository(T repository, Class<T> cls) {
        //check, if repository already exists
        if (this.repositories.containsKey(cls)) {
            throw new IllegalArgumentException("repository of type " + cls + " does already exists.");
        }

        //initialize repository
        if (repository instanceof MySQLRepository) {
            ((MySQLRepository) repository).init(this.vertx, this.mySQLDatabase);
        }

        if (repository instanceof HazelcastRepository) {
            ((HazelcastRepository) repository).init(this.vertx, this.hazelcastInstance);
        }

        this.repositories.put(cls, repository);
    }

    @Override
    public <T extends Repository> void removeRepository(Class<T> cls) {
        this.repositories.remove(cls);
    }

    @Override
    public <T extends Repository> T getRepository(Class<T> cls) {
        Repository repository = this.repositories.get(cls);

        if (repository == null) {
            return null;
        }

        return cls.cast(repository);
    }

}
