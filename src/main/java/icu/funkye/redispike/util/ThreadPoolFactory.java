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
package icu.funkye.redispike.util;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.aerospike.client.cluster.ThreadDaemonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadPoolFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPoolFactory.class);

    public static boolean isJDK21() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if (dot != -1) {
                version = version.substring(0, dot);
            }
        }
        return "21".equals(version);
    }

    public static ExecutorService newVirtualThreadPerTaskExecutor() {
        if (isJDK21()) {
            try {
                Class<Executors> clz = (Class<Executors>) Class.forName("java.util.concurrent.Executors");
                Method method = clz.getMethod("newVirtualThreadPerTaskExecutor");
                LOGGER.info("use jdk 19+ newVirtualThreadPerTaskExecutor");
                return (ExecutorService) method.invoke(null);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return new ThreadPoolExecutor(0, 200, 120L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
            new ThreadDaemonFactory());
    }

}
