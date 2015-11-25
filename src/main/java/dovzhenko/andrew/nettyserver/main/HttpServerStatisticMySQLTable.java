package dovzhenko.andrew.nettyserver.main;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "statistic")
public class HttpServerStatisticMySQLTable{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private int id;		
	@Column(name = "src_ip", nullable = false)
	private String src_ip;	
	@Column(name = "uri", nullable = false)
	private String uri;	
	@Column(name = "timestamp_value", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date timestampValue;	
	@Column(name = "send_byte", nullable = false)
	private float sendByte;	
	@Column(name = "resived_byte", nullable = false)
	private float resivedByte;	
	@Column(name = "speed", nullable = false)
	private float speed;	
	@Column(name = "url", nullable = true)
	private String url;
	
	
	public HttpServerStatisticMySQLTable(){		
	}
	
	//set block
	//table values
	public synchronized void setId(int id){
		this.id = id;
	}
	public synchronized void setSrc_ip(String src_ip){
		this.src_ip = src_ip;
	}
	public synchronized void setUri(String uri){
		this.uri = uri;
	}
	public synchronized void setTimestampValue(LocalDateTime timestampValue){
		if (this.timestampValue == null){
			this.timestampValue = Timestamp.valueOf(timestampValue);
		} else {
			Timestamp endTime = Timestamp.valueOf(timestampValue);
			long seconds = (endTime.getTime() - this.timestampValue.getTime());
			float speedCalculate = (getSendByte()/seconds)*1000;
			setSpeed(speedCalculate);
		}
	}
	public synchronized void setSendByte(float sendByte){
		this.sendByte = sendByte;
	}
	public synchronized void setResivedByte(float resivedByte){
		this.resivedByte = resivedByte;
	}
	public synchronized void setSpeed(float speed){
		
		this.speed = speed;		
	}
	public synchronized void setUrl(String url){
		this.url =  url;
	}
	

	//get block
	//table values
	public synchronized int getId() {
		return id;
	}

	public synchronized String getSrc_ip() {
		return src_ip;
	}

	public synchronized String getUri() {
		return uri;
	}

	public synchronized Date getTimestampValue() {
		return timestampValue;
	}

	public synchronized float getSendByte() {
		return sendByte;
	}

	public synchronized float getResivedByte() {
		return resivedByte;
	}

	public synchronized float getSpeed() {
		return speed;
	}

	public synchronized String getUrl() {
		return url;
	}


	//other block
	@Override
	public String toString() {
		return "HttpServerStatisticMySQLTable [id=" + id + ", src_ip=" + src_ip + ", uri=" + uri + ", timestampValue="
				+ timestampValue + ", sendByte=" + sendByte + ", resivedByte=" + resivedByte + ", speed=" + speed
				+ ", url=" + url + "]";
	}

}