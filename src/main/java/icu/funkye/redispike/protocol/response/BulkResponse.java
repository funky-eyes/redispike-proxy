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
package icu.funkye.redispike.protocol.response;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import io.netty.buffer.ByteBuf;
import icu.funkye.redispike.protocol.RedisResponse;

public class BulkResponse implements RedisResponse<String> {

    private static final char MARKER       = '$';

    private static final char PREFIX       = '+';

    private static final char ERROR_PREFIX = '-';

    private static final char ARRAY_PREFIX = '*';

    private List<byte[]>      list;

    private String            data;

    public BulkResponse(List<byte[]> list) {
        this.list = list;
    }

    public BulkResponse(String data) {
        this.data = data;
    }

    public BulkResponse() {
    }

    public void appender(String data) {
        list.add(data.getBytes(StandardCharsets.UTF_8));
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public void write(ByteBuf out) throws IOException {
        if (list != null) {
            out.writeByte(ARRAY_PREFIX);
            out.writeBytes(String.valueOf(list.size()).getBytes(StandardCharsets.UTF_8));
            out.writeBytes(CRLF);
            for (byte[] data : list) {
                out.writeByte(MARKER);
                out.writeBytes(String.valueOf(data.length).getBytes(StandardCharsets.UTF_8));
                out.writeBytes(CRLF);
                out.writeBytes(data);
                out.writeBytes(CRLF);
            }
            return;
        }
        if (data == null) {
            out.writeByte(MARKER);
            out.writeBytes(String.valueOf(-1).getBytes());
            out.writeBytes(CRLF);
        } else {
            out.writeByte(PREFIX);
            out.writeBytes(data.getBytes(StandardCharsets.UTF_8));
            out.writeBytes(CRLF);
        }
    }

    @Override
    public String toString() {
        return "BulkResponse{" + "list=" + list + '}';
    }
}
