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
        if (vertx == null) {
            throw new NullPointerException("vertx instance cannot be null.");
        }

        if (mySQLDatabase == null) {
            throw new NullPointerException("mysql database cannot be null.");
        }

        if (!mySQLDatabase.isConnected()) {
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
        if (repository instanceof MySQLRepository || repository instanceof AbstractMySQLRepository) {
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

    @Override
    public Object getRepositoryAsObject(Class<?> cls) {
        Repository repository = this.repositories.get(cls);

        if (repository == null) {
            return null;
        }

        return repository;
    }

    @Override
    public Database getMainDatabase() {
        return this.mySQLDatabase;
    }

    @Override
    public boolean contains(Class<?> cls) {
        if (!cls.isInstance(Repository.class)) {
            //check, if parent classes implements this interface
            Class<?> cls2 = cls;
            boolean interfaceFound = false;

            while (cls2.getSuperclass() != null) {
                cls2 = cls2.getSuperclass();

                //check, if interface is implemented
                if (cls2.isInstance(Repository.class)) {
                    interfaceFound = true;
                    break;
                }
            }

            if (!interfaceFound) {
                throw new IllegalArgumentException("class has to implements interface Repository.");
            }
        }

        return this.repositories.containsKey(cls);
    }

}
