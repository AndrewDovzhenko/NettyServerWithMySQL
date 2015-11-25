package dovzhenko.andrew.nettyserver.main;

import java.net.InetSocketAddress;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import io.netty.channel.Channel;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class HttpServerStatistics extends HttpServerInitializer {
	private static final HttpServerStatistics INSTANCE = new HttpServerStatistics();
	private transient DefaultChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

	private EntityManagerFactory emf;
	private EntityManager em;
	
	private HttpServerStatistics() {
		emf = Persistence.createEntityManagerFactory("NettyServer");
		em 	= emf.createEntityManager();
	}

	public static HttpServerStatistics getInstance() {
		return INSTANCE;
	}
	
	public static String getIpFromChannel(Channel channel) {
		return ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress().replaceFirst("^/", "");
	}

	public void addChannel(Channel c) {
		channels.add(c);
	}

	public int getConnectionCount() {
		return channels.size();
	}
	
	public String getCountOfRequests(){
		em.getTransaction().begin();
		Query q = em.createQuery("SELECT COUNT(s) FROM HttpServerStatisticMySQLTable s");
		String result = q.getSingleResult().toString();
		em.getTransaction().commit();
		return result;
	}
	
	
	public String getCountOfUniqueRequests(){
		em.getTransaction().begin();
		Query q = em.createQuery("SELECT COUNT(DISTINCT s.src_ip) FROM HttpServerStatisticMySQLTable s");
		String result = q.getSingleResult().toString();
		em.getTransaction().commit();
		return result;
	}
	
	public List<Object[]> getRedirectsList() {
		return getResultObjectsList(
				"SELECT s.url, COUNT(s.src_ip) "
				+ "FROM HttpServerStatisticMySQLTable s "
				+ "WHERE s.url IS NOT NULL GROUP BY s.url");
	}
	
	public List<Object[]> getIpRequestsList() {
		return getResultObjectsList(
				"SELECT s.src_ip, COUNT(s), MAX(s.timestampValue) "
				+ "FROM HttpServerStatisticMySQLTable s GROUP BY s.src_ip");
	}
	
	public List<Object[]> getLast16Connections(){
		return getResultObjectsList(
		 "SELECT s.src_ip, s.uri, s.timestampValue, s.sendByte, s.resivedByte,"
		 +"s.speed FROM HttpServerStatisticMySQLTable s ORDER BY "
		 +"s.timestampValue DESC", 16);
	}
	
	public List<Object[]> getFullStatisticTable(){
		return getResultObjectsList(
		 "SELECT s.timestampValue, s.src_ip, s.uri, s.url, "
		 +"s.speed, s.sendByte, s.resivedByte "
		 +"FROM HttpServerStatisticMySQLTable s ORDER BY "
		 +"s.timestampValue DESC");
	}
	
	public List<Object[]> getResultObjectsList(String textQuery, int maxResults){
		em.getTransaction().begin();
		Query q = em.createQuery(textQuery);
		if (maxResults != 0){
			q.setMaxResults(maxResults);
		}
		List<Object[]> result = q.getResultList();
		em.getTransaction().commit();
		
		return result;
	}
	
	public List<Object[]> getResultObjectsList(String textQuery){
		em.getTransaction().begin();
		Query q = em.createQuery(textQuery);

		List<Object[]> result = q.getResultList();
		em.getTransaction().commit();
		
		return result;
	}
	
	public synchronized void addCurrentConnectionInfoToDB(HttpServerStatisticMySQLTable currentConnectionInfo){
		em.getTransaction().begin();
		em.persist(currentConnectionInfo);
		em.getTransaction().commit();
		
	}

}
