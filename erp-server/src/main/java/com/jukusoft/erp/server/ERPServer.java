package com.jukusoft.erp.server;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.jukusoft.erp.lib.message.ResponseType;
import com.jukusoft.erp.server.message.ResponseGenerator;
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

import javax.transaction.xa.XAException;

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

    public void start() {
        //create an new hazelcast instance
        Config config = new Config();
        this.hazelcastInstance = Hazelcast.newHazelcastInstance(config);

        //http://vertx.io/docs/vertx-hazelcast/java/

        //create new vert.x cluster manager
        this.clusterManager = new HazelcastClusterManager(this.hazelcastInstance);

        //create new vertx.io options
        this.vertxOptions = new VertxOptions();

        //use clustered mode of vert.x
        this.vertxOptions.setClustered(true);

        //set cluster manager
        this.vertxOptions.setClusterManager(this.clusterManager);

        //create clustered vertx. instance
        Vertx.clusteredVertx(this.vertxOptions, res -> {
            if (res.succeeded()) {
                this.vertx = res.result();

                postStart();
            } else {
                // failed!

                System.exit(1);
            }
        });
    }

    protected void postStart () {
        //create new instance of vertx.io
        //this.vertx = Vertx.clusteredVertx(this.vertxOptions, this.vertxOptions);

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

            //TODO: do something with socket, for example send an message
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

        //create new http server
        HttpServer server = vertx.createHttpServer(options);

        server.requestHandler(request -> {

            // This handler gets called for each request that arrives on the server
            HttpServerResponse response = request.response();
            response.putHeader("content-type", "application/json");

            //get event name
            String event = request.path();

            if (request.method() == HttpMethod.POST) {
                if (request.formAttributes().contains("event")) {
                    event = request.formAttributes().get("event");
                }
            }

            //generate response string
            String str = ResponseGenerator.generateResponse("test", ResponseType.OK);

            // Write to the response and end it
            response.end(str);
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
