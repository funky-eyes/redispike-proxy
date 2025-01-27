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

import icu.funkye.redispike.protocol.AbstractRedisRequest;
import icu.funkye.redispike.protocol.RedisResponse;
import icu.funkye.redispike.protocol.response.BulkResponse;

public class AuthRequest extends AbstractRedisRequest<String> {

    String       command;

    String       password;

    BulkResponse response = new BulkResponse();

    public AuthRequest(String command, String password, boolean flush) {
        this.flush = flush;
        this.command = command;
        this.password = password;
    }

    public String getCommand() {
        return command;
    }

    @Override
    public void setResponse(String data) {
        this.response.setData(data);
    }

    @Override
    public void setErrorResponse(String data) {
        this.response.setError(data);
    }

    @Override
    public RedisResponse<String> getResponse() {
        return response;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "AuthRequest{" + "key='" + command + '\'' + ", response=" + response + '}';
    }
}
