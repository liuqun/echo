package com.example;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@ChannelHandler.Sharable
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    private Charset charset = StandardCharsets.UTF_8;

    public EchoServerHandler(Charset charset) {
        setCharset(charset);
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    private static final Logger logger = LoggerFactory.getLogger(EchoServerHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        SocketAddress clientIpAddr = channel.remoteAddress();
        logger.debug("TCP客户端IP: " + clientIpAddr);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in;
        if (!(msg instanceof io.netty.buffer.ByteBuf)) {
            logger.error("Error msg class: " + msg.getClass().getSimpleName());
            return;
        }
        in = (ByteBuf) msg;
        int readable = in.readableBytes();
        if (readable <= 0) {
            return;
        }
        int len = Math.min(500, readable);
        byte[] data = new byte[len];
        in.readBytes(data, 0, len);

        String s = new String(data, charset);
        logger.debug("msg = " + s);
        String upperCase = s.toUpperCase();
        ByteBuf out = Unpooled.wrappedBuffer(upperCase.getBytes(charset));
        ctx.write(out);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
