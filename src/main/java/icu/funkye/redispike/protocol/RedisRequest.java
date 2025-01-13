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

import java.util.concurrent.atomic.AtomicInteger;
import com.alipay.remoting.CommandCode;
import com.alipay.remoting.InvokeContext;
import com.alipay.remoting.ProtocolCode;
import com.alipay.remoting.RemotingCommand;
import com.alipay.remoting.config.switches.ProtocolSwitch;
import com.alipay.remoting.exception.DeserializationException;
import com.alipay.remoting.exception.SerializationException;
import icu.funkye.redispike.util.IntegerUtils;

public interface RedisRequest<T> extends RemotingCommand  {
    AtomicInteger ID_GENERATOR = new AtomicInteger(0);

    RedisResponse<T> getResponse();

    void setResponse(T data);

    default void setErrorResponse(T data){}

    default ProtocolCode getProtocolCode() {
        return null;
    }

    /**
     * Get the command code for this command
     *
     * @return command code
     */
    default CommandCode getCmdCode() {
        return new RedisRequestCommandCode(IntegerUtils.hashCodeToShort(this.getClass().hashCode()));
    }

    /**
     * Get the id of the command
     *
     * @return an int value represent the command id
     */
    default int getId() {
        return ID_GENERATOR.incrementAndGet();
    }

    /**
     * Get invoke context for this command
     *
     * @return context
     */
    default InvokeContext getInvokeContext() {
        return null;
    }

    /**
     * Get serializer type for this command
     *
     * @return
     */
    default byte getSerializer() {
        return 0;
    };

    /**
     * Get the protocol switch status for this command
     *
     * @return
     */
    default ProtocolSwitch getProtocolSwitch() {
        return null;
    }

    /**
     * Serialize all parts of remoting command
     *
     * @throws SerializationException
     */
    default void serialize() throws SerializationException {};

    /**
     * Deserialize all parts of remoting command
     *
     * @throws DeserializationException
     */
    default void deserialize() throws DeserializationException {}

    /**
     * Serialize content of remoting command
     *
     * @param invokeContext
     * @throws SerializationException
     */
    default void serializeContent(InvokeContext invokeContext) throws SerializationException {}

    /**
     * Deserialize content of remoting command
     *
     * @param invokeContext
     * @throws DeserializationException
     */
    default void deserializeContent(InvokeContext invokeContext) throws DeserializationException {}

}
