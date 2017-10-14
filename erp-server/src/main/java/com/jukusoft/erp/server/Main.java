package com.jukusoft.erp.server;

public class Main {

    /**
    * main method to start the application
    */
    public static void main (String[] args) {
        //create new server
        IServer server = new ERPServer();

        //start server
        server.start();
    }

}
