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
package org.redis2asp.protocol;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import com.alipay.remoting.CommandDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import org.redis2asp.protocol.request.CommandRequest;
import org.redis2asp.protocol.request.DelRequest;
import org.redis2asp.protocol.request.GetRequest;
import org.redis2asp.protocol.request.SetRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisCommandDecoder implements CommandDecoder {

    private final static Logger LOGGER   = LoggerFactory.getLogger(RedisCommandDecoder.class);

    private final static int    CR       = '\r';
    private final static int    LF       = '\n';
    private final static int    DOLLAR   = '$';
    private final static int    ASTERISK = '*';

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        int length = readParamsLen(in);
        List<String> params = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            String param = readParam(in);
            params.add(param.toLowerCase());
        }
        // convert to RedisRequest
        out.add(convert2RedisRequest(params));
    }

    private RedisRequest<?> convert2RedisRequest(List<String> params) {
        String cmd = params.get(0);
        LOGGER.info("cmd: {}", params);
        switch (cmd) {
            case "get":
                return new GetRequest(params.get(1));
            case "command":
                return new CommandRequest();
            case "setnx":
                params.add("nx");
                return new SetRequest(params);
            case "set":
                return new SetRequest(params);
            case "del":
                params.remove(0);
                return new DelRequest(params);
            default:
                return null;
        }
    }

    private int readParamsLen(ByteBuf in) {
        int c = in.readByte();
        if (c != ASTERISK) {
            throw new DecoderException("expect character *");
        }
        // max 999 params
        int len = readLen(in, 3);
        if (len == 0) {
            throw new DecoderException("expect non-zero params");
        }
        return len;
    }

    private String readParam(ByteBuf in) {
        int len = readStrLen(in);
        return readStr(in, len);
    }

    private String readStr(ByteBuf in, int len) {
        if (len == 0) {
            return "";
        }
        byte[] cs = new byte[len];
        in.readBytes(cs);
        skipCrlf(in);
        return new String(cs, StandardCharsets.UTF_8);
    }

    private int readStrLen(ByteBuf in) {
        int c = in.readByte();
        if (c != DOLLAR) {
            throw new DecoderException("expect character *");
        }
        // string maxlen 999999
        return readLen(in, 6);
    }

    private int readLen(ByteBuf in, int maxBytes) {
        byte[] digits = new byte[maxBytes];
        int len = 0;
        while (true) {
            byte d = in.getByte(in.readerIndex());
            if (!Character.isDigit(d)) {
                break;
            }
            in.readByte();
            digits[len] = d;
            len++;
            if (len > maxBytes) {
                throw new DecoderException("params length too large");
            }
        }
        skipCrlf(in);
        if (len == 0) {
            throw new DecoderException("expect digit");
        }
        return Integer.parseInt(new String(digits, 0, len));
    }

    private void skipCrlf(ByteBuf in) {
        int c = in.readByte();
        if (c == CR) {
            c = in.readByte();
            if (c == LF) {
                return;
            }
        }
        throw new DecoderException("expect cr ln");
    }
}