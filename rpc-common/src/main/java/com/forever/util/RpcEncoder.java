package com.forever.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 编码器
 */
public class RpcEncoder extends MessageToByteEncoder {

    private Class<?> genericClass;
    public RpcEncoder(Class<?> genericClass){
        this.genericClass = genericClass;
    }

    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        if (genericClass.isInstance(o)){
            byte[] data = SerializationUitl.serialize(o);
            byteBuf.writeInt(data.length);
            byteBuf.writeBytes(data);

        }
    }
}
