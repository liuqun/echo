package com.example;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

public class EchoServer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(EchoServer.class);

    private final Charset charset;
    private final int port;

    public EchoServer(int port) {
        this(port, Charset.defaultCharset());
    }

    public EchoServer(int port, Charset charset) {
        this.port = port;
        this.charset = charset;
    }

    public Charset getCharset() {
        return charset;
    }

    public int getPort() {
        return port;
    }

    @Override
    public void run() {
        ServerBootstrap b = new ServerBootstrap();
        EventLoopGroup group =  new NioEventLoopGroup();
        b.group(group);
        b.localAddress(new InetSocketAddress(port));
        b.channel(NioServerSocketChannel.class);

        final ChannelHandler serverHandler = new EchoServerHandler(charset);
        ChannelInitializer<SocketChannel> channelInitializer = new ChannelInitializer<SocketChannel>() {
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(serverHandler);
            }
        };
        b.childHandler(channelInitializer);

        ChannelFutureListener tcpPortBindEventListener = new ChannelFutureListener() {
            // 服务器端口号
            private final String portStr = String.valueOf(port);

            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (!channelFuture.isSuccess()) {
                    Throwable cause = channelFuture.cause();
                    logger.debug("服务器端口绑定失败(端口号portStr=" + portStr + "), 错误原因: " + cause.getMessage());
                    return;
                }
                logger.info("服务器端口" + portStr + "绑定成功");
            }
        };
        ChannelFuture channelFuture = b.bind();
        channelFuture.addListener(tcpPortBindEventListener);
        try {
            channelFuture.sync();
            Channel channel = channelFuture.channel();
            channel.closeFuture().sync();
        } catch (InterruptedException ignored) {
            //ignored.printStackTrace();
        } catch (Exception e) {
            String reason = e.getMessage();
            logger.error("无法绑定TCP端口! 端口号" + port + "被占用: " + reason);
            //noinspection ConstantConditions
            if (!(e instanceof BindException)) {
                logger.debug("程序调试信息: " + e.getClass().getName());
                e.printStackTrace();
            }
        } finally {
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException ignored) {
            }
        }
    }
}
