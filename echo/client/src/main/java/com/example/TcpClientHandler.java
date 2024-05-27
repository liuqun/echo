package com.example;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;

@ChannelHandler.Sharable
public class TcpClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private Charset charset;

    private TcpClientHandler() {
        this(StandardCharsets.UTF_8);
    }

    public TcpClientHandler(Charset charset) {
        this.charset = charset;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    private static final Logger logger = LoggerFactory.getLogger(TcpClientHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        SocketAddress serverAddr = channel.remoteAddress();
        if (serverAddr instanceof InetSocketAddress) {
            InetSocketAddress ipAndPort = (InetSocketAddress) serverAddr;
            logger.info("已连接到服务器: " + ipAndPort.getHostString() + " 端口:" + ipAndPort.getPort());
        }
        String msgStr = "Hello 你好世界! 【中文编码格式:" + charset.displayName() + "】";
        ByteBuf msgByteBuf = Unpooled.copiedBuffer(msgStr, charset);
        ctx.writeAndFlush(msgByteBuf);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        int nBytes = in.readableBytes();
        byte[] origData = new byte[nBytes];
        in.readBytes(origData, 0, nBytes);
        java.nio.ByteBuffer bb = ByteBuffer.wrap(origData);
        CharsetDecoder charsetDecoder = charset.newDecoder();
        try {
            CharBuffer unicode16 = charsetDecoder.decode(bb);
            System.out.println("服务器回传信息如下:");
            System.out.println(String.format("%s", unicode16.toString()));
        } catch (CharacterCodingException decodeErr) {
            logger.warn("Error: 中文编码格式不兼容:" + decodeErr.getMessage());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String err = cause.getMessage();
        String exceptionName = cause.getClass().getSimpleName();
        logger.error("错误" + exceptionName + ": " + err);
        ctx.close();
    }
}
