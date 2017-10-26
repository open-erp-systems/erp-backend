package com.jukusoft.erp.lib.database;

public interface DatabaseManager {

    /**
    * add repository
     *
     * @param repository instance of repository
     * @param cls class of repository
    */
    public <T extends Repository> void addRepository (T repository, Class<T> cls);

    /**
    * remove repository
     *
     * @param cls class of repository
    */
    public <T extends Repository> void removeRepository (Class<T> cls);

    /**
    * get repository by class
     *
     * @return instance of repository or null, if repository doesnt exists
    */
    public <T extends Repository> T getRepository (Class<T> cls);

}
