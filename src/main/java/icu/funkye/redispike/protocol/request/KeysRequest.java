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
package icu.funkye.redispike.protocol.request;

import java.util.ArrayList;
import java.util.List;

import icu.funkye.redispike.protocol.AbstractRedisRequest;
import icu.funkye.redispike.protocol.RedisResponse;
import icu.funkye.redispike.protocol.response.BulkResponse;

public class KeysRequest extends AbstractRedisRequest<String> {

    final String originalCommand;

    String       pattern;

    BulkResponse response;

    public KeysRequest(List<String> params, boolean flush) {
        this.flush = flush;
        this.originalCommand = params.get(0);
        if (params.size() != 2) {
            this.response = new BulkResponse();
            this.response.setError("ERR wrong number of arguments for 'keys' command");
        } else {
            this.response = new BulkResponse(new ArrayList<>());
            this.pattern = params.get(1);
        }
    }

    @Override
    public void setResponse(String data) {
        this.response.appender(data);
    }

    @Override
    public RedisResponse<String> getResponse() {
        return response;
    }

    public String getOriginalCommand() {
        return originalCommand;
    }

    public String getPattern() {
        return pattern;
    }

    @Override
    public String toString() {
        return "KeysRequest{" + "originalCommand='" + originalCommand + '\'' + ", pattern='" + pattern + '\''
               + ", response=" + response + '}';
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}
