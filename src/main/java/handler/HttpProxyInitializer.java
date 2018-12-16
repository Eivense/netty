package handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

public class HttpProxyInitializer extends ChannelInitializer {

    private Channel clientChannel;

    public HttpProxyInitializer(Channel clientChannel) {
        this.clientChannel = clientChannel;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {

    }
}
