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

import com.aerospike.client.Host;
import com.aerospike.client.policy.ClientPolicy;
import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.ProtocolManager;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.redis2asp.common.BoltServer;
import org.redis2asp.common.CONNECTEventProcessor;
import org.redis2asp.common.DISCONNECTEventProcessor;
import org.redis2asp.factory.AeroSpikeClientFactory;
import org.redis2asp.protocol.RedisProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {
    private static final Logger LOGGER                    = LoggerFactory.getLogger(Server.class);

    BoltServer                  server;

    int                         port                      = 6379;

    CONNECTEventProcessor       serverConnectProcessor    = new CONNECTEventProcessor();
    DISCONNECTEventProcessor    serverDisConnectProcessor = new DISCONNECTEventProcessor();

    public void start(String... args) throws ParseException {
        Options options = new Options();
        options.addOption("th", "host", true,
            "List of seed hosts in format:\n" + "hostname1[:tlsname][:port1],...\n"
                    + "The tlsname is only used when connecting with a secure TLS enabled server. "
                    + "If the port is not specified, the default port is used.\n"
                    + "IPv6 addresses must be enclosed in square brackets.\n" + "Default: localhost\n" + "Examples:\n"
                    + "host1\n" + "host1:3000,host2:3000\n" + "192.168.1.10:cert1:3000,[2001::1111]:cert2:3000\n");
        options.addOption("tp", "targetPort", true, "Server default port (default: 3000)");
        options.addOption("TU", "targetUser", true, "User name");
        options.addOption("TP", "targetPassword", true, "Password");
        options.addOption("p", "proxyPort", true, "proxy port");
        options.addOption("st", "socketTimeout", true, "Set read and write socketTimeout in milliseconds\n"
                                                       + "for single record and batch commands.");
        options.addOption("tt", "totalTimeout", true, "Set read and write totalTimeout in milliseconds\n"
                                                      + "for single record and batch commands.");
        options.addOption("s", "set", true, "Set name. Use 'empty' for empty set (default: demoset)");
        options.addOption("n", "namespace", true, "Namespace (default: test)");
        CommandLineParser parser = new DefaultParser();
        CommandLine cl = parser.parse(options, args, false);
        port = Integer.parseInt(cl.getOptionValue("p", "6379"));
        String host = cl.getOptionValue("th", "127.0.0.1");
        int targetPort = Integer.parseInt(cl.getOptionValue("tp", "3000"));
        String targetUser = cl.getOptionValue("TU", null);
        String targetPassword = cl.getOptionValue("TP", null);
        AeroSpikeClientFactory.namespace = cl.getOptionValue("n", "test");
        AeroSpikeClientFactory.set = cl.getOptionValue("s", "demoset");
        Host[] hosts = Host.parseHosts(host, targetPort);
        ClientPolicy clientPolicy = new ClientPolicy();
        clientPolicy.user = targetUser;
        clientPolicy.password = targetPassword;
        AeroSpikeClientFactory.createInstance(clientPolicy, hosts);
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

    public void shutdown() {
        server.stop();
    }

}
