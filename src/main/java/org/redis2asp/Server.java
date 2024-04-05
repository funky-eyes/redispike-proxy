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
package org.redis2asp;

import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.ProtocolManager;
import org.redis2asp.common.BoltServer;
import org.redis2asp.common.CONNECTEventProcessor;
import org.redis2asp.common.DISCONNECTEventProcessor;
import org.redis2asp.protocol.RedisProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {
    private static final Logger LOGGER                    = LoggerFactory.getLogger(Server.class);

    BoltServer                  server;

    int                         port                      = 6379;

    CONNECTEventProcessor       serverConnectProcessor    = new CONNECTEventProcessor();
    DISCONNECTEventProcessor    serverDisConnectProcessor = new DISCONNECTEventProcessor();

    public void start(String[] args) {
        server = new BoltServer(port);
        server.addConnectionEventProcessor(ConnectionEventType.CONNECT, serverConnectProcessor);
        server.addConnectionEventProcessor(ConnectionEventType.CLOSE, serverDisConnectProcessor);
        ProtocolManager.registerProtocol(new RedisProtocol(), RedisProtocol.PROTOCOL_CODE);
        if (server.start()) {
            LOGGER.info("server start ok!");
        } else {
            LOGGER.error("server start failed!");
        }
    }

}
