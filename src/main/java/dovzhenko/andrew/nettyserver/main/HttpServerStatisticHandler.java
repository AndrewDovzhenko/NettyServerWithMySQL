package dovzhenko.andrew.nettyserver.main;

import java.time.LocalDateTime;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
//import io.netty.handler.traffic.TrafficCounter;

public class HttpServerStatisticHandler extends ChannelTrafficShapingHandler {
	   private final HttpServerStatisticMySQLTable connectionInfo;

	    public HttpServerStatisticHandler(long checkInterval, HttpServerStatisticMySQLTable connectionInfo) {
	        super(checkInterval);
	        this.connectionInfo = connectionInfo;
	    }

	    @Override
	    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
	        super.handlerAdded(ctx);
	        this.trafficCounter().start();
	    }

	    @Override
		public synchronized void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
			super.handlerRemoved(ctx);
			this.trafficCounter().stop();
			connectionInfo.setResivedByte(this.trafficCounter().cumulativeReadBytes());
			connectionInfo.setSendByte(this.trafficCounter().cumulativeWrittenBytes());
			connectionInfo.setTimestampValue(LocalDateTime.now());
			if (connectionInfo.getUri()==null){
				connectionInfo.setUri(" ");
			}
			HttpServerStatistics.getInstance().addCurrentConnectionInfoToDB(connectionInfo);
		}

	    @Override
	    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
	        super.exceptionCaught(ctx, cause);
	        cause.printStackTrace();
	    }

}
