package test.html5.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.catalina.websocket.WsOutbound;

import com.sun.org.apache.regexp.internal.recompile;

/**
 * 测试websocket
 * 
 * @author yqj
 * 
 */
@WebServlet("/chatServlet")
public class TestWebSocketServlet extends WebSocketServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public TestWebSocketServlet() {
		super();
	}

	@SuppressWarnings("deprecation")
	@Override
	protected StreamInbound createWebSocketInbound(String subProtocol, HttpServletRequest request) {
		return new WebSocketMessageInbound(this.getUser(request));
	}

	private String getUser(HttpServletRequest request) {
		return request.getParameter("user");
	}
}

/**
 * send:
 * <p>
 * type:user_join,user_leave,msg,user_list
 * </p>
 * <p>
 * user:
 * </p>
 * 
 * <p>
 * msg:
 * </p>
 * 
 * recive:
 * <p>
 * type:all, single
 * <p>
 * user:
 * <p>
 * msg:
 * <p>
 * tuser:
 * 
 * @author yqj
 * 
 */
class WebSocketMessageInbound extends MessageInbound {

	// 当前连接的用户名称
	private final String user;

	public WebSocketMessageInbound(String user) {
		if(user==null)
			user="user"+WebSocketMessageInboundPool.getCount();
		this.user = user;
	}

	private String setToJson(Set<String> users) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		if (!users.isEmpty()) {
			for (String user : users) {
				sb.append("\"" + user + "\",");
			}
			sb.deleteCharAt( sb.length() - 1);
		}
		sb.append("]");
		return sb.toString();
	}

	@Override
	protected void onBinaryMessage(ByteBuffer message) throws IOException {

	}

	@Override
	protected void onTextMessage(CharBuffer message) throws IOException {
		if (message == null)
			return;
		String m = message.toString();
		Map<String, String> mm = messageToMap(m);
		String type = mm.get("type");
		String tuser = mm.get("tuser");
		String msg = mm.get("msg");
		if (type.equals("all")) {
			WebSocketMessageInboundPool.sendMessage("{type:'msg',user:'" + this.user + "',msg:'" + msg + "'}");
		}else if(type.equals("single")){
			WebSocketMessageInboundPool.sendMessageToUser(tuser,"{type:'msg',user:'" + this.user + "',msg:'" + msg + "'}");
		}
	}



	@Override
	protected void onOpen(WsOutbound outbound) {
		// 向所有在线用户推送当前用户上线的消息
		WebSocketMessageInboundPool.sendMessage("{type:'user_join',user:'" + user + "',msg:''}");
		// 向连接池添加当前的连接对象
		

		String userStrs = setToJson(WebSocketMessageInboundPool.getOnlineUser());
		WebSocketMessageInboundPool.addMessageInbound(this);
		//发送当前用户名
		WebSocketMessageInboundPool.sendMessageToUser(this.user, "{cuser:\""+this.user+"\"}");
		// 向当前连接发送当前在线用户的列表
		WebSocketMessageInboundPool.sendMessageToUser(this.user, "{type:'user_list',user:'" + user + "',msg:"+userStrs+"}");
	}

	@Override
	protected void onClose(int status) {
		// 触发关闭事件，在连接池中移除连接
		WebSocketMessageInboundPool.removeMessageInbound(this);
		// 向在线用户发送当前用户退出的消息
		WebSocketMessageInboundPool.sendMessage("{type:'user_leave',user:'" + user + "',msg:''}");
	}

	public String getUser() {
		return user;
	}
	
	private Map<String, String> messageToMap(String m) {
		Map<String, String> map = new HashMap<String, String>();
		m = m.trim();
		String[] strings = m.split(";");
		for (String str : strings) {
			String[] split = str.split("=");
			if (split.length == 1) {
				map.put(split[0], "");
			} else if (split.length > 1) {
				map.put(split[0], split[1]);
			}
		}
		return map;
	}
}

/**
 * 连接池
 * 
 * @author yqj
 * 
 */
class WebSocketMessageInboundPool {

	// 保存连接的MAP容器
	private static final Map<String, WebSocketMessageInbound> connections = new ConcurrentHashMap<String, WebSocketMessageInbound>();
	private static int count;

	// 向连接池中添加连接
	public static void addMessageInbound(WebSocketMessageInbound inbound) {
		// 添加连接
		System.out.println("user : " + inbound.getUser() + " join..");
		connections.put(inbound.getUser(), inbound);
		count++;
	}

	// 获取所有的在线用户
	public static Set<String> getOnlineUser() {
		return connections.keySet();
	}
	
	public static int getCount() {
		return count;
	}

	public static void removeMessageInbound(WebSocketMessageInbound inbound) {
		// 移除连接
		System.out.println("user : " + inbound.getUser() + " exit..");
		connections.remove(inbound.getUser());
	}

	public static void sendMessageToUser(String user, String message) {
		try {
			// 向特定的用户发送数据
			System.out.println("send message to user : " + user + " ,message content : " + message);
			WebSocketMessageInbound inbound = connections.get(user);
			if (inbound != null) {
				inbound.getWsOutbound().writeTextMessage(CharBuffer.wrap(message));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 向所有的用户发送消息
	public static void sendMessage(String message) {
		try {
			Set<String> keySet = connections.keySet();
			for (String key : keySet) {
				WebSocketMessageInbound inbound = connections.get(key);
				if (inbound != null) {
					System.out.println("send message to user : " + key + " ,message content : " + message);
					inbound.getWsOutbound().writeTextMessage(CharBuffer.wrap(message));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}