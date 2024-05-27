package com.example;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.SocketException;
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
        try {
            ChannelFuture channelFutureAfterTcpPortBind = b.bind().sync();
            channelFutureAfterTcpPortBind.channel().closeFuture().sync();
        } catch (InterruptedException ignored) {
            //ignored.printStackTrace();
        } catch (Exception bindException) {
            //bindException.printStackTrace();
            String reason = bindException.getMessage();
            logger.error("错误! 端口" + port + "已被其他进程占用: " + reason);
            throw new RuntimeException("无法绑定本机" + port + "端口: " + reason);
        } finally {
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException ignored) {
            }
        }
    }
}
