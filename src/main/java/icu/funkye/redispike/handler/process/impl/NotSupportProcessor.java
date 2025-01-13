package icu.funkye.redispike.handler.process.impl;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import com.alipay.remoting.RemotingContext;
import icu.funkye.redispike.handler.process.AbstractRedisRequestProcessor;
import icu.funkye.redispike.protocol.RedisRequestCommandCode;
import icu.funkye.redispike.protocol.request.NotSupportRequest;
import icu.funkye.redispike.util.IntegerUtils;

/**
 * @author jianbin@apache.org
 */
public class NotSupportProcessor extends AbstractRedisRequestProcessor<NotSupportRequest> {

	public NotSupportProcessor() {
		this.cmdCode = new RedisRequestCommandCode(IntegerUtils.hashCodeToShort(NotSupportRequest.class.hashCode()));
	}

	@Override
	public void handle(RemotingContext ctx, NotSupportRequest request) {
		request.setResponse("-ERR unknown command `"+request.getCommand()+"`, with args beginning with:");
		Optional.ofNullable(request.getCountDownLatch()).ifPresent(CountDownLatch::countDown);
		write(ctx, request);
	}
}
