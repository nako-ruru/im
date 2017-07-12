package com.mycompany.im.connector;

public class Launcher {

    public static void main(String[] args) throws Exception {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 6060;
        }
        new Server(port).start();
    }

}