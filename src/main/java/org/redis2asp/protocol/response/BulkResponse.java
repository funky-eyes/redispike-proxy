package org.redis2asp.protocol.response;

import java.io.IOException;
import java.util.Arrays;
import io.netty.buffer.ByteBuf;
import org.redis2asp.protocol.RedisResponse;

public class BulkResponse implements RedisResponse<byte[]> {

    public static final BulkResponse NIL_REPLY = new BulkResponse();

    private static final char        MARKER    = '$';

    private static final char        PREFIX    = '+';

    private byte[]                   data;

    private final int                len;

    public BulkResponse() {
        this.data = null;
        this.len = -1;
    }

    public BulkResponse(byte[] data) {
        this.data = data;
        this.len = data.length;
    }

    @Override
    public byte[] data() {
        return this.data;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public void write(ByteBuf out) throws IOException {
        // 1.Write header
        out.writeByte(MARKER);
        out.writeBytes(String.valueOf(len).getBytes());
        out.writeBytes(CRLF);

        // 2.Write data
        if (len > 0) {
            out.writeBytes(data);
            out.writeBytes(CRLF);
        }
    }

    @Override
    public String toString() {
        return "BulkReply{" + "bytes=" + Arrays.toString(data) + '}';
    }
}
