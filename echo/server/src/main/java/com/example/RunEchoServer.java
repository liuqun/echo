package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Arrays;

public class RunEchoServer {
    private static final String ECHO_SERVER_DEFAULT_TCP_PORT = "8080";
    private static final Logger logger = LoggerFactory.getLogger(RunEchoServer.class);

    public static void main(String[] args) {
        Charset charset = Charset.forName("GB18030");
        String strPort = ECHO_SERVER_DEFAULT_TCP_PORT;
        if (args.length < 1) {
            strPort = ECHO_SERVER_DEFAULT_TCP_PORT;
        }
        if (args.length >= 1) {
            strPort = args[0];
        }
        if (args.length >= 2) {
            charset = Charset.forName(args[1]);
        }
        if (args.length >= 3) {
            System.err.println("Usage: " + RunEchoServer.class.getSimpleName() + " 端口号 UTF-8");
            logger.error("无效启动参数太多: " + Arrays.toString(args));
        }
        int nPort = Integer.parseInt(strPort);
        EchoServer echoServer = new EchoServer(nPort, charset);
        logger.debug("当前汉字使用" + echoServer.getCharset().displayName() + "字符集");

        echoServer.run();
    }
}
