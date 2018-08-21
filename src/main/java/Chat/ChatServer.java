package Chat;

import Util.Util;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public class ChatServer {
    private int port;

    public ChatServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        SslContext sslContext=SslContextBuilder.forServer(Util.getPrivateKey(),Util.getCertficate()).build();
        EventLoopGroup bossGroup=new NioEventLoopGroup();
        EventLoopGroup workerGroup=new NioEventLoopGroup();

        try{
            ServerBootstrap b=new ServerBootstrap();
            b.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChatServerInitializer(sslContext))
                    .option(ChannelOption.SO_BACKLOG,128 )
                    .childOption(ChannelOption.SO_KEEPALIVE,true );
            System.out.println("Server∆Ù∂Ø");
            ChannelFuture f=b.bind(port).sync();
            f.channel().closeFuture().sync();
        }finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            System.out.println("Serverπÿ±’");
        }
    }

    public static void main(String[] args)throws Exception {
        new ChatServer(8080).run();
    }
}
