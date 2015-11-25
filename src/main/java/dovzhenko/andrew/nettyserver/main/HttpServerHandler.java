package dovzhenko.andrew.nettyserver.main;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
//import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
//import static io.netty.handler.codec.http.HttpHeaders.Values;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

//import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpMethod.*;

public class HttpServerHandler extends SimpleChannelInboundHandler<Object> {
	//Static page
	private final ByteBuf HelloPage = Unpooled.copiedBuffer("<html>\n<head>\n<meta charset=\"utf-8\">\n<title>HELLO</title>\n</head>\n"
			+ "<body><h1>Hello World</h1></body></html>",CharsetUtil.UTF_8);
	private final ByteBuf Page404 = Unpooled.copiedBuffer("<html>\n<head>\n<meta charset=\"utf-8\">\n<title>ERROR</title>\n</head>\n"
			+ "<body><h1>404 PAGE NOT FOUND</h1></body></html>",CharsetUtil.UTF_8);
   
	private final HttpServerStatistics serverStatistics = HttpServerStatistics.getInstance();
    private final HttpServerStatisticMySQLTable connectionInfo;
    
    private HttpRequest req;
 	
    public HttpServerHandler(HttpServerStatisticMySQLTable connectionInfo) {
		this.connectionInfo = connectionInfo;
	}
    
    class InnerRequestHandler{
       private HttpMethod method;
       private ChannelHandlerContext ctx;	
		
        public InnerRequestHandler(ChannelHandlerContext ctx) {
        	this.ctx = ctx;
        	method = req.getMethod();      	
		}
        
        public void carrentRequestHandler(){
        	if (method != GET){
        		sendNotImplementedPage501(ctx);
        		return;
        	}
        	
        	String uri = req.getUri();
            String[] pathCompElements = uri.replaceFirst("^/", "").split("/");
 
        	Map<String, List<String>> parameters = new QueryStringDecoder(uri).parameters();
        	if (parameters.size() > 0 ){
        		pathCompElements[pathCompElements.length - 1] = pathCompElements[pathCompElements.length - 1].split("\\?")[0];
        		if (pathCompElements[0].equalsIgnoreCase("redirect") && parameters.get("url").size() == 1){
        			connectionInfo.setUri("redirect");
        			sendRedirectionPage(ctx, parameters.get("url").get(0));
        			return;
        		}
        		
        	}else{
               	if (pathCompElements[0].equalsIgnoreCase("hello")){
        			connectionInfo.setUri("hello");
               		sendHelloPage(ctx);
        			return;
               	}
               	
               	if (pathCompElements[0].equalsIgnoreCase("status")){
        			connectionInfo.setUri("status");
               		sendStatusPage(ctx);
              		return;
               	}
               	
               	if (pathCompElements[0].equalsIgnoreCase("log")){
        			connectionInfo.setUri("log");
               		sendLogPage(ctx);
              		return;
               	}
        	}
        	connectionInfo.setUri("other");
        	sendNotFoundPage404(ctx);
        }
	}
   
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
    	super.channelRegistered(ctx);
    	
        serverStatistics.addChannel(ctx.channel());
            	
    }
    
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
        
        /*if (req != null) {
            connectionInfo.setUri(req.getUri());
        }*/

    }

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            req = (HttpRequest) msg;

            if (HttpHeaders.is100ContinueExpected(req)) {
                send100Continue(ctx);
            }          	 

           // call method
           new InnerRequestHandler(ctx).carrentRequestHandler();
        }
	}
    
	private void send100Continue(ChannelHandlerContext ctx) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
		ctx.write(response);
	}
    
	private void sendNotImplementedPage501(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, NOT_IMPLEMENTED);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE); 
    }
	 
	private void sendNotFoundPage404(ChannelHandlerContext ctx) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND, Page404);
		response.headers().set(CONTENT_TYPE, "text/html; charset=utf-8");
		writeResponse(req, ctx, response);
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	 	}
	
	private void sendHelloPage(ChannelHandlerContext ctx){
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, HelloPage);
		response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
		ctx.executor().schedule(() -> writeResponse(req, ctx, response),10,  TimeUnit.SECONDS);
	}
	
	private void sendRedirectionPage(ChannelHandlerContext ctx, String url) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, FOUND);
        response.headers().set(LOCATION, "http://" + url);
        connectionInfo.setUrl(url);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

	private void sendStatusPage(ChannelHandlerContext ctx){
		String connectionCountString 	= ""+serverStatistics.getConnectionCount();
		String countOfRequests 			= 	 serverStatistics.getCountOfRequests();
		String numberOfUniqueRequests 	=  	 serverStatistics.getCountOfUniqueRequests(); 
		String ipRequestTableRows 		= "";
		String redirectsTableRows 		= "";
		String connectionsTableRows 	= "";
		TableBilder tableBilder = new TableBilder();
		
		//Build ip request table
		List<Object[]> ipRequestsList 	= serverStatistics.getIpRequestsList();
		if (ipRequestsList.size() != 0) {
			ipRequestsList.forEach(tableBilder::addRowToTable);
			ipRequestTableRows = tableBilder.toString();
		}		
		tableBilder.clean();
		
		//Build redirect table
		List<Object[]> redirectList 	= serverStatistics.getRedirectsList();	
		if (redirectList.size() != 0) {
			redirectList.forEach(tableBilder::addRowToTable);
			redirectsTableRows = tableBilder.toString();
		}
		tableBilder.clean();
	       
		//Build connections table
		List<Object[]> connectionsList 	= serverStatistics.getLast16Connections();
		if (connectionsList.size() != 0) {
			connectionsList.forEach(tableBilder::addRowToTable);
			connectionsTableRows = tableBilder.toString();
		}
		
       		
		ByteBuf statusPage = HttpSeverStatusPage.getStatusPage(countOfRequests, numberOfUniqueRequests, connectionCountString,
				ipRequestTableRows, redirectsTableRows, connectionsTableRows);
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, statusPage);
		
		writeResponse(req, ctx, response);
		
	}
	
	private void sendLogPage(ChannelHandlerContext ctx) {
		String logTableRows = "";
		
		List<Object[]> fullStatisticTable 	= serverStatistics.getFullStatisticTable();
		if (fullStatisticTable.size()!=0){
			TableBilder tableBilder = new TableBilder();
			fullStatisticTable.forEach(tableBilder::addRowToTable);
			logTableRows = tableBilder.toString();
		}

		
		ByteBuf logPage = HttpSeverStatusPage.getLogPage(logTableRows);
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, logPage);
		
		writeResponse(req, ctx, response);
		
	}
	
	private void writeResponse(HttpObject currentObj, ChannelHandlerContext ctx, FullHttpResponse response) {
		
		boolean keepAliveLocal = HttpHeaders.isKeepAlive(req);
		
		if (keepAliveLocal) {
			response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
	
			response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
		}
		
		ctx.write(response);

		if (!keepAliveLocal) {
			ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
		} else {
			ctx.flush();
		}
        
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	  	super.exceptionCaught(ctx, cause);
	}

}
