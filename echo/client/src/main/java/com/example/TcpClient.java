package com.example;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;

public class TcpClient implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(TcpClient.class);

    public static final int REMOTE_SERVER_DEFAULT_PORT = 8080;
    public static final String REMOTE_SERVER_DEFAULT_HOST = "127.0.0.1";
    private SocketAddress remoteAddress;
    private final Charset charset = Charset.forName("GB18030");

    public Charset getCharset() {
        return charset;
    }

    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(SocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
        if (null == this.remoteAddress) {
            this.remoteAddress = new InetSocketAddress(REMOTE_SERVER_DEFAULT_HOST, REMOTE_SERVER_DEFAULT_PORT);
        }
    }

    public TcpClient(SocketAddress remoteAddress) {
        setRemoteAddress(remoteAddress);
    }

    private TcpClient() {
        this(new InetSocketAddress(REMOTE_SERVER_DEFAULT_HOST, REMOTE_SERVER_DEFAULT_PORT));
    }

    public TcpClient(String remoteServerHostNameOrIpAddress, int remoteServerPort) {
        this(new InetSocketAddress(remoteServerHostNameOrIpAddress, remoteServerPort));
    }

    @Override
    public void run() {
        Bootstrap b = new Bootstrap();
        EventLoopGroup group =  new NioEventLoopGroup();
        b.group(group);
        b.channel(NioSocketChannel.class);
        b.remoteAddress(remoteAddress);

        final ChannelHandler tcpClientHandler = new TcpClientHandler(charset);
        ChannelInitializer<SocketChannel> channelInitializer = new ChannelInitializer<SocketChannel>() {
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(tcpClientHandler);
            }
        };
        b.handler(channelInitializer);
        try {
            ChannelFuture channelFutureAfterTcpIsConnected = b.connect().sync();
            channelFutureAfterTcpIsConnected.channel().closeFuture().sync();
        } catch (InterruptedException ignored) {
            //ignored.printStackTrace();
        } catch (Exception e) {
            String exceptionName = e.getClass().getSimpleName();
            String message = e.getMessage();
            if (null == message) {
                message = "";
            }
            logger.error(String.format("网络连接失败: %s(%s)", message, exceptionName));
            if (!("".equals(message))) {
                message = ": " + message;
            }
            throw new RuntimeException("Error: 连接失败或网络超时!" + message);
        } finally {
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException ignored) {
            }
        }
    }
}
