package org.redis2asp.protocol.request;

import org.redis2asp.protocol.RedisRequest;
import org.redis2asp.protocol.RedisResponse;
import org.redis2asp.protocol.response.BulkResponse;

public class CommandRequest implements RedisRequest<byte[]> {

    private BulkResponse response = new BulkResponse();

    @Override
    public RedisResponse<byte[]> getResponse() {
        return response;
    }

    @Override
    public void setResponse(byte[] data) {
        response.setData(data);
    }
}
