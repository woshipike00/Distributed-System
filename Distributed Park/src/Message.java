import java.io.Serializable;


public class Message implements Serializable{
	
	private int pid;
	private int port;
	private int timeStamp;
	private MessageType mType;
	
	//0 represents entry,1 represents exit
	private GateType gType;
	private int num_of_cars;
	
	public Message(){
		
	}
	
	public int getpid(){
		return pid;
	}
	
	public int gettimestamp(){
		return timeStamp;
	}
	
	public MessageType getmtype(){
		return mType;
	}
	
	public  GateType getgtype(){
		return gType;
	}
	
	public int getcarnum(){
		return num_of_cars;
	}
	
	public int getport(){
		return port;
	}
	
	
	
	public void setRequest(int pid,int port,int timeStamp,MessageType mType){
		this.pid=pid;
		this.port=port;
		this.timeStamp=timeStamp;
		this.mType=MessageType.request;
	}
	
	public void setReply(int pid,int port,int timeStamp,MessageType mType,GateType gType,int num_of_cars){
		this.pid=pid;
		this.port=port;
		this.timeStamp=timeStamp;
		this.mType=MessageType.reply;
		this.gType=gType;
		this.num_of_cars=num_of_cars;
	}

}
