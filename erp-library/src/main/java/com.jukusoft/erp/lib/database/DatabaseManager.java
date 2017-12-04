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

    /**
     * get repository by class
     *
     * @return instance of repository or null, if repository doesnt exists
     */
    public Object getRepositoryAsObject (Class<?> cls);

    /**
    * get main database
     *
     * @return database instance
    */
    public Database getMainDatabase ();

    /**
    * check if database manager contains repository type
     *
     * @param cls repository class
    */
    public boolean contains (Class<?> cls);

}
