package com.jukusoft.erp.server;

import com.hazelcast.config.CacheSimpleConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IdGenerator;
import com.jukusoft.erp.lib.database.DatabaseManager;
import com.jukusoft.erp.lib.database.MySQLDatabase;
import com.jukusoft.erp.lib.database.impl.DatabaseManagerImpl;
import com.jukusoft.erp.lib.gateway.ApiGateway;
import com.jukusoft.erp.lib.gateway.ResponseHandler;
import com.jukusoft.erp.lib.keystore.KeyStoreGenerator;
import com.jukusoft.erp.lib.logging.ILogging;
import com.jukusoft.erp.lib.message.StatusCode;
import com.jukusoft.erp.lib.message.request.ApiRequest;
import com.jukusoft.erp.lib.message.response.ApiResponse;
import com.jukusoft.erp.lib.session.SessionManager;
import com.jukusoft.erp.lib.session.Session;
import com.jukusoft.erp.server.gateway.DefaultApiGateway;
import com.jukusoft.erp.lib.logger.HzLogger;
import com.jukusoft.erp.server.message.ResponseGenerator;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ERPServer implements IServer {

    //vert.x options
    protected VertxOptions vertxOptions = null;

    //instance of vert.x
    protected Vertx vertx = null;

    //vert.x network server
    protected NetServer netServer = null;

    //instance of hazelcast
    protected HazelcastInstance hazelcastInstance = null;

    //vert.x cluster manager
    protected ClusterManager clusterManager = null;

    //api gateway
    protected ApiGateway gateway = null;

    protected ILogging logger = null;

    //hazelcast ID generator for cluster-wide unique IDs
    protected IdGenerator idGenerator = null;

    //instance of session manager
    protected SessionManager sessionManager = null;

    //instance of database
    protected MySQLDatabase database = null;

    //database manager
    protected DatabaseManager dbManager = null;

    //number of threads
    protected int eventLoopPoolSize = 1;
    protected int workerPoolSize = 1;

    public void start() {
        //create an new hazelcast instance
        Config config = new Config();

        //disable hazelcast logging
        config.setProperty("hazelcast.logging.type", "none");

        CacheSimpleConfig cacheConfig = new CacheSimpleConfig();
        config.getCacheConfigs().put("session-cache", cacheConfig);

        this.hazelcastInstance = Hazelcast.newHazelcastInstance(config);

        //create new session manager
        this.sessionManager = SessionManager.createHzMapSessionManager(hazelcastInstance);//new HzJCacheSessionManager(this.hazelcastInstance);

        //http://vertx.io/docs/vertx-hazelcast/java/

        this.idGenerator = this.hazelcastInstance.getIdGenerator("message-id-generator");

        //create new vert.x cluster manager
        this.clusterManager = new HazelcastClusterManager(this.hazelcastInstance);

        //create new vertx.io options
        this.vertxOptions = new VertxOptions();

        //use clustered mode of vert.x
        this.vertxOptions.setClustered(true);

        //set cluster manager
        this.vertxOptions.setClusterManager(this.clusterManager);

        //set high availability flag
        this.vertxOptions.setHAEnabled(true);

        //set number of threads to use in thread pools
        this.vertxOptions.setEventLoopPoolSize(this.eventLoopPoolSize);
        this.vertxOptions.setWorkerPoolSize(this.workerPoolSize);

        //create clustered vertx. instance
        Vertx.clusteredVertx(this.vertxOptions, res -> {
            if (res.succeeded()) {
                this.vertx = res.result();

                //create logger
                this.logger = new HzLogger(this.hazelcastInstance, this.clusterManager.getNodeID());

                //create database client and connect to database
                try {
                    this.connectToMySQL(res1 -> {
                        if (!res1.succeeded()) {
                            logger.error("database_error", "Couldnt connect to database: " + res1.cause());
                            System.exit(1);
                        }

                        logger.info("database_connection", "Connected to database successfully.");

                        //create database manager
                        this.dbManager = new DatabaseManagerImpl(this.vertx, this.database, this.hazelcastInstance);

                        postStart();
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            } else {
                // failed!

                System.exit(1);
            }
        });
    }

    @Override
    public void setEventLoopPoolSize(int nOfThreads) {
        this.eventLoopPoolSize = nOfThreads;
    }

    @Override
    public void setWorkerPoolSize(int nOfThreads) {
        this.workerPoolSize = nOfThreads;
    }

    protected void postStart () {
        //create api gateway
        this.gateway = new DefaultApiGateway(this.vertx, this.logger);

        //create options for TCP network server
        NetServerOptions netServerOptions = new NetServerOptions();

        //TODO: replace this later
        int port = 2200;

        //set port
        netServerOptions.setPort(port);

        //create new instance of TCP network server
        this.netServer = this.vertx.createNetServer(netServerOptions);

        //add connection handler
        netServer.connectHandler(socket -> {
            System.out.println("new connection accepted, ip: " + socket.remoteAddress().host() + ", port: " + socket.remoteAddress().port());

            logger.debug("new_tcp_connection", "ip: " + socket.remoteAddress().host() + ", port: " + socket.remoteAddress().port());

            //set close handler
            socket.closeHandler(v -> {
                logger.debug("close_tcp_connection", "ip: " + socket.remoteAddress().host() + ", port: " + socket.remoteAddress().port());
            });

            //set message handler
            socket.handler(buffer -> {
                //convert to string and json object
                String str = buffer.toString(StandardCharsets.UTF_8);

                //generate cluster-wide unique message id
                final long messageID = generateMessageID();

                //remove whitespaces at begin and end
                str = str.trim();

                //check, if message is an json message
                if (!str.startsWith("{") || !str.endsWith("}")) {
                    //no json message
                    logger.warn(messageID, "bad_request", "invalide json message: " + str);

                    //generate response string
                    String str1 = ResponseGenerator.generateResponse("error", "", "none", StatusCode.BAD_REQUEST);

                    //write to the response and end it
                    socket.write(str1);

                    return;
                }

                JSONObject json = new JSONObject(str);

                //get external ID, if available
                String externalID = "";

                if (json.has("messageID")) {
                    externalID = json.getString("messageID");
                }

                //check, if event name exists
                if (!json.has("event")) {
                    logger.warn(messageID, "bad_request", "event doesnt exists in message: " + json.toString());

                    //generate response string
                    String str1 = ResponseGenerator.generateResponse("error", "", externalID, StatusCode.BAD_REQUEST);

                    //write to the response and end it
                    socket.write(str1);

                    return;
                }

                //get event name
                String event = json.getString("event");

                //get data
                JSONObject data = json.getJSONObject("data");

                String sessionID = "";

                if (data.has("ssid")) {
                    sessionID = data.getString("ssid");
                }

                Session session = null;

                if (sessionID.isEmpty()) {
                    //create new session
                    session = this.sessionManager.generateNewSession();
                } else {
                    //get session by session manager
                    session = this.sessionManager.getSession(sessionID);

                    if (session == null) {
                        //generate response string
                        String str1 = ResponseGenerator.generateResponse(event, sessionID, externalID, StatusCode.BAD_REQUEST);

                        //write to the response and end it
                        socket.write(str1);

                        logger.warn(messageID, "wrong_session_id", "Couldnt find session ID: " + sessionID + " (IP: " + socket.remoteAddress().host() + ":"  + socket.remoteAddress().port() + ").");

                        throw new IllegalStateException("cannot find session ID: " + sessionID);
                    }
                }

                sessionID = session.getSessionID();

                //create api request
                ApiRequest req = new ApiRequest(event, data, messageID, externalID, sessionID, session.isLoggedIn(), session.getUserID());

                //add meta information
                req.getMeta().put("host", socket.remoteAddress().host());
                req.getMeta().put("port", socket.remoteAddress().port());

                //log request
                this.logger.debug(messageID, "new_tcp_request", req.toString());

                this.gateway.handleRequestAsync(req, new ResponseHandler() {
                    @Override
                    public void handleResponse(ApiResponse res) {
                        if (res.getType() == ApiResponse.RESPONSE_TYPE.JSON) {
                            //send response
                            String str = ResponseGenerator.generateResponse(res.getEvent(), res.getData(), res.getSessionID(), res.getExternalID(), res.getStatusCode());

                            //write to the response and end it
                            socket.write(str);

                            logger.debug(messageID, "request_succedded", res.toString());
                        } else {
                            socket.write(res.getData().getString("content"));
                        }
                    }

                    @Override
                    public void responseFailed() {
                        //generate response string
                        String str = ResponseGenerator.generateResponse(event, req.getSessionID(), req.getExternalID(), StatusCode.SERVICE_UNAVAILABLE);

                        //write to the response and end it
                        socket.write(str);

                        logger.warn(messageID, "request_failed", req.toString() + ", cause: " + StatusCode.SERVICE_UNAVAILABLE.name() + ".");
                    }
                });
            });
        });

        //start network server
        this.netServer.listen(res -> {
            if (res.succeeded()) {
                System.out.println("ERP Server is now listening on port " + res.result().actualPort());
            } else {
                System.err.println("Couldnt start network server: " + res.cause());

                System.exit(1);
            }
        });

        //start http server (for debugging and rest api)
        this.startHTTPServer(8080);
    }

    public void startHTTPServer (int port) {
        HttpServerOptions options = new HttpServerOptions();
        options.setPort(port);

        //use application layer protocol negotiation (only HTTP/2!)
        //https://de.wikipedia.org/wiki/Application-Layer_Protocol_Negotiation
        //options.setUseAlpn(true);

        String certPassword = "test";
        String certPath = ".keystore.jks";

        //check if certificate exists
        if (!new File(certPath).exists()) {
            //generate new certificate
            try {
                logger.warn("ssl_encryption", "no key store exists, generate an new one.");
                KeyStoreGenerator.generateJKSKeyStore(certPath, certPassword, 1024);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        //use SSL encryption
        //options.setKeyCertOptions(new JksOptions().setPath(certPath).setPassword(certPath));
        //options.setSsl(true);

        //use gzip compression
        options.setCompressionSupported(true);
        options.setCompressionLevel(9);

        //create new http server
        HttpServer server = vertx.createHttpServer(options);

        server.requestHandler(request -> {
            final long startTime = System.currentTimeMillis();

            request.setExpectMultipart(true);
            request.endHandler(handler -> {
                // This handler gets called for each request that arrives on the server
                HttpServerResponse response = request.response();

                response.putHeader("content-type", "application/json");

                //do not allow proxies to cache the data
                response.putHeader("Cache-Control", "no-store, no-cache");
                // prevents Internet Explorer from MIME - sniffing a
                //response away from the declared content-type
                response.putHeader("X-Content-Type-Options", "nosniff");
                // Strict HTTPS (for about ~6Months)
                //.putHeader("Strict-Transport-Security", "max-age=" + 15768000)
                // IE8+ do not allow opening of attachments in the context of this resource
                response.putHeader("X-Download-Options", "noopen");
                // enable XSS for IE
                response.putHeader("X-XSS-Protection", "1; mode=block");
                // deny frames
                response.putHeader("X-FRAME-OPTIONS", "DENY");

                //get event name
                String event = request.path();

                JSONObject data = new JSONObject();

                if (request.method() == HttpMethod.POST) {
                    if (request.formAttributes().contains("event")) {
                        event = request.formAttributes().get("event");
                    }
                }

                final String eventName = event;

                //generate cluster-wide unique message id
                final long messageID = generateMessageID();

                String sessionID = "";

                //check for cookies
                if (request.headers().contains("Cookie")) {
                    for (String str : request.headers().getAll("Cookie")) {
                        logger.info(messageID, "cookie_found", "cookie found: " + str);

                        String[] array = str.split("=");

                        if (array[0].equals("sessionid")) {
                            //session ID found
                            sessionID = array[1];

                            logger.info(messageID, "session", "session ID found: " + sessionID);
                        }
                    }
                }

                //parse GET attributes
                String[] array1 = request.absoluteURI().split("\\?");

                if (array1.length > 1) {
                    //request has GET attributes

                    for (String str : array1[1].split("&")) {
                        String[] array2 = str.split("=");

                        if (array2.length < 2) {
                            //there is a key, but no value set
                            continue;
                        }

                        String key = array2[0];
                        String value = array2[1];

                        //TODO: find a better solution
                        request.formAttributes().add(key, value);
                    }
                }

                //converts all form attributes to json object
                for (Map.Entry<String,String> entry : request.formAttributes().entries()) {
                    data.put(entry.getKey(), entry.getValue());
                }

                //check, if session ID exists
                if (request.formAttributes().contains("ssid")) {
                    sessionID = request.formAttributes().get("ssid");
                }

                Session session = null;

                if (sessionID.isEmpty()) {
                    //generate new session
                    session = this.sessionManager.generateNewSession();
                } else {
                    session = this.sessionManager.getSession(sessionID);

                    if (session == null) {
                        //generate new session
                        session = this.sessionManager.generateNewSession();
                    }
                }

                sessionID = session.getSessionID();

                //HTTP requests doesnt use external IDs (ack IDs)
                String externalID = "none";

                response.putHeader("Set-Cookie", "sessionid=" + sessionID + "; HttpOnly; Path=/");

                //create api request
                ApiRequest req = new ApiRequest(event, data, messageID, externalID, sessionID, session.isLoggedIn(), session.getUserID());

                //add meta information
                req.getMeta().put("host", request.remoteAddress().host());
                req.getMeta().put("port", request.remoteAddress().port());
                //req.getMeta().put("path", request.remoteAddress().path());

                //log request
                this.logger.debug(messageID, "new_http_request", req.toString());

                this.gateway.handleRequestAsync(req, new ResponseHandler() {
                    @Override
                    public void handleResponse(ApiResponse res) {
                        if (res.getType() == ApiResponse.RESPONSE_TYPE.JSON) {
                            //send response
                            String str = ResponseGenerator.generateResponse(res.getEvent(), res.getData(), res.getSessionID(), res.getExternalID(), res.getStatusCode());

                            response.putHeader("content-type", "application/json");

                            //write to the response and end it
                            response.end(str);

                            logger.debug(messageID, "request_succedded", res.toString());
                        } else {
                            response.putHeader("content-type", "text/html; charset=utf-8");

                            response.end(res.getData().getString("content"));

                            logger.debug(messageID, "request_succedded", res.getData().getString("content"));
                        }

                        long endTime = System.currentTimeMillis();
                        long diff = endTime - startTime;
                        logger.debug(req.getMessageID(), "execution_time", "request needed " + diff + "ms to execute.");
                    }

                    @Override
                    public void responseFailed() {
                        //generate response string
                        String str = ResponseGenerator.generateResponse(eventName, req.getSessionID(), req.getExternalID(), StatusCode.SERVICE_UNAVAILABLE);

                        //write to the response and end it
                        response.end(str);

                        logger.warn(messageID, "request_failed", req.toString() + ", cause: " + StatusCode.SERVICE_UNAVAILABLE.name() + ".");

                        long endTime = System.currentTimeMillis();
                        long diff = endTime - startTime;
                        logger.debug(req.getMessageID(), "execution_time", "request needed " + diff + "ms to execute.");
                    }
                });
            });
        });

        //start http server
        server.listen(res -> {
            if (res.succeeded()) {
                System.out.println("HTTP Server is now listening on port " + res.result().actualPort());
            } else {
                System.err.println("Couldnt start HTTP server: " + res.cause());

                System.exit(1);
            }
        });
    }

    protected long generateMessageID () {
        return this.idGenerator.newId();
    }

    protected void connectToMySQL (Handler<Future<Void>> handler) throws IOException {
        this.database = new MySQLDatabase(this.vertx);

        logger.info("database_connection", "try to connect to database...");

        //connect to database asynchronous
        this.database.connect("./config/mysql.cfg", handler);
    }

    public void stutdown() {
        //close network server
        netServer.close(res -> {
            if (res.succeeded()) {
                System.out.println("Server was shutdown now.");

                //close vertx.io
                vertx.close();
            } else {
                System.out.println("Server couldnt be closed.");
            }
        });
    }

}
