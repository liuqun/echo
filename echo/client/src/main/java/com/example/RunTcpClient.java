package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunTcpClient {
    private static final Logger logger = LoggerFactory.getLogger(RunTcpClient.class);

    public static void main(String[] args) {
        int remoteServerPort = 8080;
        String remoteServer = "127.0.0.1";
        TcpClient tcpClient = new TcpClient(remoteServer, remoteServerPort);

        logger.warn("当前所选的中文编码格式: " + tcpClient.getCharset().displayName());
        tcpClient.run();
//        Thread clientThread = new Thread(tcpClient);
//        try {
//            clientThread.start();
//            clientThread.join();
//        } catch (InterruptedException ignored) {
//            // ignored.printStackTrace();
//        }
    }

}
