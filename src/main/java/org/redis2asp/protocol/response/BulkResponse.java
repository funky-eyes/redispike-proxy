/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    private int                      len;

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
        this.len = data.length;
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
