package com.jukusoft.data.repository;

import com.jukusoft.data.entity.User;
import com.jukusoft.erp.lib.context.AppContext;
import com.jukusoft.erp.lib.database.AbstractMySQLRepository;
import com.jukusoft.erp.lib.database.InjectAppContext;
import com.jukusoft.erp.lib.utils.HashUtils;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
* Repository to get, create and update DAO user objects
 *
 * @link http://vertx.io/docs/vertx-sql-common/java/
*/
public class UserRepository extends AbstractMySQLRepository {

    @InjectAppContext
    protected AppContext appContext;

    /**
    * get user by id or return null, if user doesnt exists
     *
     * @param userID user id
     * @param handler handler which will be executed if result was received
    */
    public void getUserByID (long userID, Handler<AsyncResult<User>> handler) {
        //TODO: check if user is in cache

        getMySQLDatabase().getRow("SELECT * FROM `" + getMySQLDatabase().getTableName("users") + "` WHERE `userID` = '" + userID + "';", res -> {
            if (!res.succeeded()) {
                handler.handle(Future.failedFuture(res.cause()));
                return;
            }

            //create DAO user from row
            User user = new User(res.result());

            handler.handle(Future.succeededFuture(user));
        });
    }

    /**
     * get user by username or return null, if user doesnt exists
     */
    public void getUserByUsername (String username, Handler<AsyncResult<User>> handler) {
        //TODO: check if user is in cache

        JsonArray params = new JsonArray();
        params.add(username);

        getMySQLDatabase().getRow("SELECT * FROM `" + getMySQLDatabase().getTableName("users") + "` WHERE `username` = ?;", params, res -> {
            if (!res.succeeded()) {
                handler.handle(Future.failedFuture(res.cause()));
                return;
            }

            if (res.result() == null) {
                //user doesnt exists
                handler.handle(Future.succeededFuture(null));
            } else {
                //create DAO user from row
                User user = new User(res.result());

                handler.handle(Future.succeededFuture(user));
            }
        });
    }

    public void checkPassword (long userID, String password, Handler<AsyncResult<Boolean>> handler) {
        JsonArray params = new JsonArray();
        params.add(userID);

        getMySQLDatabase().query("SELECT * FROM `" + getMySQLDatabase().getTableName("users") + "` WHERE `userID` = ?; ", params, res -> {
            if (!res.succeeded()) {
                handler.handle(Future.failedFuture(res.cause()));
                return;
            }

            if (res.result().getNumRows() == 0) {
                handler.handle(Future.failedFuture("UserRepository::checkPassword user doesnt exists."));
            }

            //get row
            JsonObject row = res.result().getRows().get(0);

            //check, if row exists
            String rowPassword = row.getString("password");
            String salt = row.getString("salt");

            //hash password
            String hash = HashUtils.computeSHA256Hash(password, salt);

            //check, if password is equals
            if (rowPassword.equals(hash)) {
                //password is equals
                handler.handle(Future.succeededFuture(true));
            } else {
                handler.handle(Future.succeededFuture(false));
            }
        });
    }

}
