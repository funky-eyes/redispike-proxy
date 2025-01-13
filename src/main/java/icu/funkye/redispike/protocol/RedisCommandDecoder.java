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
package icu.funkye.redispike.protocol;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import com.alipay.remoting.CommandDecoder;
import icu.funkye.redispike.protocol.request.AuthRequest;
import icu.funkye.redispike.protocol.request.NotSupportRequest;
import icu.funkye.redispike.protocol.request.hash.HDelRequest;
import icu.funkye.redispike.protocol.request.hash.HExistsRequest;
import icu.funkye.redispike.protocol.request.hash.HGetAllRequest;
import icu.funkye.redispike.protocol.request.hash.HGetRequest;
import icu.funkye.redispike.protocol.request.hash.HIncrbyRequest;
import icu.funkye.redispike.protocol.request.hash.HIncrbyfloatRequest;
import icu.funkye.redispike.protocol.request.hash.HKeysRequest;
import icu.funkye.redispike.protocol.request.hash.HLenRequest;
import icu.funkye.redispike.protocol.request.hash.HMgetRequest;
import icu.funkye.redispike.protocol.request.hash.HSetRequest;
import icu.funkye.redispike.protocol.request.hash.HValsRequest;
import icu.funkye.redispike.protocol.request.KeysRequest;
import icu.funkye.redispike.protocol.request.set.SAddRequest;
import icu.funkye.redispike.protocol.request.set.SCardRequest;
import icu.funkye.redispike.protocol.request.set.SMembersRequest;
import icu.funkye.redispike.protocol.request.set.SPopRequest;
import icu.funkye.redispike.protocol.request.set.SRandmemberRequest;
import icu.funkye.redispike.protocol.request.set.SRemRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import icu.funkye.redispike.protocol.request.CommandRequest;
import icu.funkye.redispike.protocol.request.DelRequest;
import icu.funkye.redispike.protocol.request.GetRequest;
import icu.funkye.redispike.protocol.request.SetRequest;
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
        List<List<String>> paramsList = new ArrayList<>();
        while (in.isReadable()) {
            int length = readParamsLen(in);
            List<String> params = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                String param = readParam(in);
                params.add(param.toLowerCase());
            }
            paramsList.add(params);
        }
        int size = paramsList.size() - 1;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("cmds: {}", paramsList);
        }
        CountDownLatch countDownLatch = null;
        if(size>0){
            countDownLatch = new CountDownLatch(size);
        }
        // convert to RedisRequest
        for (int i = 0; i < paramsList.size(); i++) {
            AbstractRedisRequest<?> request = convert2RedisRequest(paramsList.get(i), size == i);
            if (request != null) {
                Optional.ofNullable(countDownLatch).ifPresent(request::setCountDownLatch);
            }
            out.add(request);
        }
    }

    private AbstractRedisRequest<?> convert2RedisRequest(List<String> params, boolean flush) {
        String cmd = params.get(0);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("cmd: {}", params);
        }
        switch (cmd) {
            case "auth":
                return new AuthRequest(params.get(0), flush);
            case "hmget":
                params.remove(0);
                return new HMgetRequest(params.remove(0), params, flush);
            case "hdel":
                return new HDelRequest(params, flush);
            case "get":
                return new GetRequest(params.get(1), flush);
            case "command":
                return new CommandRequest(flush);
            case "hset":
            case "hsetnx":
                return new HSetRequest(params, flush);
            case "setnx":
                params.add("nx");
                return new SetRequest(params, flush);
            case "set":
                return new SetRequest(params, flush);
            case "keys":
                return new KeysRequest(params, flush);
            case "del":
                params.remove(0);
                return new DelRequest(params, flush);
            case "hkeys":
                params.remove(0);
                return new HKeysRequest(params, flush);
            case "hget":
                return new HGetRequest(params.get(1), params.size() > 2 ? params.get(2) : null, flush);
            case "hincrby":
                return new HIncrbyRequest(params.get(1), params.get(2), params.get(3), flush);
            case "hincrbyfloat":
                return new HIncrbyfloatRequest(params.get(1), params.get(2), params.get(3), flush);
            case "hgetall":
                return new HGetAllRequest(params.get(1), flush);
            case "hvals":
                return new HValsRequest(params.get(1), flush);
            case "hexists":
                return new HExistsRequest(params.get(1), params.get(2), flush);
            case "hlen":
                params.remove(0);
                return new HLenRequest(params, flush);
            case "scard":
                return new SCardRequest(params.get(1), flush);
            case "sadd":
                return new SAddRequest(params, flush);
            case "smembers":
                return new SMembersRequest(params.get(1), flush);
            case "srem":
                params.remove(0);
                return new SRemRequest(params, flush);
            case "srandmember":
                return new SRandmemberRequest(params.get(1), params.size() > 2 ? Integer.parseInt(params.get(2)) : 1,
                    flush);
            case "spop":
                params.remove(0);
                return new SPopRequest(params.remove(0), params.size() > 0 ? Integer.parseInt(params.get(0)) : null,
                    flush);
            default:
                return new NotSupportRequest(params.get(0), flush);
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
