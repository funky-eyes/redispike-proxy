package org.redis2asp.protocol.response;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import org.redis2asp.protocol.RedisResponse;

public class IntegerResponse implements RedisResponse<Integer> {

    private static final char MARKER = ':';

    private final int         data;

    public IntegerResponse(int data) {
        this.data = data;
    }

    @Override
    public Integer data() {
        return this.data;
    }

    @Override
    public void write(ByteBuf out) throws IOException {
        out.writeByte(MARKER);
        out.writeBytes(String.valueOf(data).getBytes());
        out.writeBytes(CRLF);
    }

    @Override
    public String toString() {
        return "IntegerReply{" + "data=" + data + '}';
    }

}
