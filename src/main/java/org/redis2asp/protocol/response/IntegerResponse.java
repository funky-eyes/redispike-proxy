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
import java.nio.charset.StandardCharsets;
import io.netty.buffer.ByteBuf;
import org.redis2asp.protocol.RedisResponse;

public class IntegerResponse implements RedisResponse<byte[]> {

    private static final char MARKER = ':';

    private byte[]            data;

    public IntegerResponse(int data) {
        this.data = String.valueOf(data).getBytes(StandardCharsets.UTF_8);
    }

    public IntegerResponse() {
    }

    @Override
    public byte[] data() {
        return this.data;
    }

    @Override
    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public void write(ByteBuf out) throws IOException {
        out.writeByte(MARKER);
        out.writeBytes(data == null ? "0".getBytes(StandardCharsets.UTF_8) : data);
        out.writeBytes(CRLF);
    }

    @Override
    public String toString() {
        return "IntegerReply{" + "data=" + data + '}';
    }

}
