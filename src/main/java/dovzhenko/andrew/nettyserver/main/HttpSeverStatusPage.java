package dovzhenko.andrew.nettyserver.main;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

public class HttpSeverStatusPage {
	
	private final static String NEWLINE = "\n";
	
	public static ByteBuf getStatusPage(String countRequest, String countUniqueRequest,String activeConnection,
		String IpRequestTableRows, String RedirectsTableRows, String ConnectionsTableRows){
		
		ByteBuf result = Unpooled.copiedBuffer(
				"<html><head><title>status page</title></head>" + NEWLINE + "<body>"+ NEWLINE + 
				"<h1>This is Status page</h1>" + NEWLINE +
				// result 1
				"<table><td>total number of requests:</td><td>"+countRequest+"</td></table>" + NEWLINE +
				// result 2
				"<table><td>the number of unique requests:</td><td>"+countUniqueRequest+"</td></table>" + NEWLINE +
				// result 3
				" <p/><table border=1>" + NEWLINE + 
				" <tr bgcolor = gray><th>IP</th><th>requests</th><th>last connection</th>" + NEWLINE +
				// generete rows
				IpRequestTableRows +
				" </table>" + NEWLINE + 
				// result 4			
				" <p/><table border=1>" + NEWLINE +
				" <tr bgcolor = gray><th>URL</th><th>requests</th></tr>"+NEWLINE+
				// generete rows
				RedirectsTableRows+NEWLINE+
				" </table>" + NEWLINE + 
				// result 5
				" <p/><table>" + NEWLINE + "<td>open connection:</td><td>"+activeConnection+"</td>" + NEWLINE + 
				" </table>" + NEWLINE + 
				// result 6
				" <p/><table border=1>"+ NEWLINE + 
				" <tr bgcolor = gray>" + NEWLINE + 
				" <th>src_ip</th>"+ NEWLINE + 
				" <th>URI</th>"+ NEWLINE +
				" <th>timestamp</th>"+ NEWLINE +
				" <th>sent_bytes</th>"+ NEWLINE +
				" <th>received_bytes</th>"+ NEWLINE +
				" <th>speed (bytes/sec)</th>"+ NEWLINE +
				" </tr>"+ NEWLINE +
				// generete rows
				ConnectionsTableRows+
				"</table>" + NEWLINE + 
				"</body>" + NEWLINE + 
				"<html>"
				
				
				,CharsetUtil.UTF_8);
		 return result;
	}

	public static ByteBuf getLogPage(String LogTableRows) {
		ByteBuf result = Unpooled.copiedBuffer(
				"<html><head><title>log page</title></head>" + NEWLINE + "<body>"+ NEWLINE + 
				"<h1>This is log page</h1>" + NEWLINE +
				"<p/><table border=1>" + NEWLINE + 
				"<tr bgcolor = gray><th>DATE TIME</th><th>IP</th><th>URI</th><th>URL</th><th>SPEED (byte\\s)</th><th>SEND BYTE</th><th>RESIVED BYTE</th>" + NEWLINE +
				// generete rows
				LogTableRows +
				"</table>" + NEWLINE + 
				"</body>" + NEWLINE + 
				"<html>"
				,CharsetUtil.UTF_8);
		 return result;
	}
}
