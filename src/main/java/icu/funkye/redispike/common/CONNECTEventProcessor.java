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
package icu.funkye.redispike.common;

import com.alipay.remoting.Connection;
import com.alipay.remoting.ConnectionEventProcessor;
import icu.funkye.redispike.conts.RedisConstants;
import icu.funkye.redispike.factory.AeroSpikeClientFactory;
import org.junit.jupiter.api.Assertions;

/**
 * @author jianbin@apache.org
 */
public class CONNECTEventProcessor implements ConnectionEventProcessor {

    @Override
    public void onEvent(String remoteAddr, Connection conn) {
        Assertions.assertNotNull(remoteAddr);
        doCheckConnection(conn);
        conn.setAttribute(RedisConstants.REDIS_DB, AeroSpikeClientFactory.set);
    }

    /**
     * do check connection
     * @param conn
     */
    private void doCheckConnection(Connection conn) {
        Assertions.assertNotNull(conn);
        Assertions.assertNotNull(conn.getPoolKeys());
        Assertions.assertTrue(conn.getPoolKeys().size() > 0);
        Assertions.assertNotNull(conn.getChannel());
        Assertions.assertNotNull(conn.getUrl());
        Assertions.assertNotNull(conn.getChannel().attr(Connection.CONNECTION).get());
    }
}
