package com.jukusoft.erp.server;

public class Main {

    /**
    * main method to start the application
    */
    public static void main (String[] args) {
        //load mysql driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }

        //create new server
        IServer server = new ERPServer();

        //start server
        server.start();
    }

}
