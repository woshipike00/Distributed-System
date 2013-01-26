import java.awt.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.logging.Handler;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.SystemMenuBar;

public  class  Gateway implements Runnable{
	
	
	private int pid;
	private int port;
	private int timeStamp;
    private int requestTime;
	private ServerSocket serverSocket;
	private Socket socket;
	private State state;
	private GateType gType;
	private int num_of_cars;
	private ObjectInputStream objInput;
	private ObjectOutputStream objOutput;
	//requests to be handled
	private Queue<Message> requestQueue=new LinkedList<Message>();
	//replies recieved
	private Queue<Message> replyQueue=new LinkedList<Message>();
	private int count=0;
	private final static int totalnum=3;
	private final static int remainingCarport=2;
	private final static int[] ports={8888,8889,8890};

	
	public  Gateway(int pid,int port,int num_of_cars,GateType gType) throws IOException{
		this.pid=pid;
		this.timeStamp=0;
		this.port=port;
		this.num_of_cars=num_of_cars;
		this.gType=gType;
		this.serverSocket=new ServerSocket(port);
		//System.out.println("server in port: "+port);
		state=State.RELEASED;
	}
	
	public synchronized void carEnter(){
		
		if(gType==GateType.exit){
			System.out.println("this is a car exit!");
			System.out.println("------------------------------");

			return;
		}
		
		//update logical clock
		timeStamp++;
		
		if(!state.equals(State.RELEASED)){
			System.out.println("can't send request! The thread is already in processing");
			return;
		}
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				for (int i=0;i<ports.length;i++){
					if(ports[i]!=port){
						try {
							sendRequest(timeStamp,ports[i]);
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
						
				}

			}
		}).start();
		
	}
	
	public synchronized void carExit(){
		if(gType==GateType.entry){
			System.out.println("this is a car entry");
			System.out.println("------------------------------");

			return;
		}
		num_of_cars++;
		System.out.println("car exit");
		System.out.println("------------------------------");

	}
	
	
	
	
	
	
	public void sendRequest(int timestamp,int Port) throws UnknownHostException, IOException{

		if(state==State.RELEASED)
			state=State.WANTED;
		Message msg=new Message();

		//record the request time
		requestTime=timestamp;
		//System.out.println("send request: from "+port+" to "+Port);
		
		//send requests to all gateways
		msg.setRequest(pid, port, timestamp, MessageType.request);
		Socket socket=new Socket("127.0.0.1", Port);
		ObjectOutputStream objectOut=new ObjectOutputStream(socket.getOutputStream());
		objectOut.writeObject(msg);
		objectOut.close();
		socket.close();
	}
	
	public void sendReply(Message message,int timestamp) throws UnknownHostException, IOException{
		Message msg=new Message();		
		//System.out.println("send reply: from "+port+" to "+message.getport());
		//send reply
		msg.setReply(pid, port, timestamp, MessageType.reply, gType, num_of_cars);
		Socket socket=new Socket("127.0.0.1", message.getport());
		ObjectOutputStream objectOut=new ObjectOutputStream(socket.getOutputStream());
		objectOut.writeObject(msg);
		objectOut.close();
		socket.close();
		
	}
	
	public void onRecieve(Message msg) throws UnknownHostException, IOException{
		
		//lamport logical clock
		timeStamp=Math.max(timeStamp, msg.gettimestamp())+1;
		//System.out.println("onReceive: "+port+" receive from "+msg.getport());
		
		// handle request
	    if(msg.getmtype()==MessageType.request){
			//System.out.println("handle request from pid: "+msg.getpid());
			if(state==State.HELD || (state==State.WANTED && (msg.gettimestamp()>requestTime ||
					(msg.gettimestamp()==requestTime && msg.getpid()>pid)))){
				// record the requests
				requestQueue.add(msg);
				//System.out.println("add into request queue");
			}
			else{
				//update logical clock
				timeStamp++;
				sendReply(msg,timeStamp);
			}
		}
		
	    //handle reply
		if(msg.getmtype()==MessageType.reply){
			//System.out.println("handle reply from pid: "+msg.getpid());	
			replyQueue.add(msg);
			count++;
			
			//this gateway has received the replies from all of the other gateways 
			if(count==totalnum-1){
				state=State.HELD;
				int remain=remainingCarport;
				
				//System.out.println(port+" has received the replies from all of the other gateways");

				while(!replyQueue.isEmpty()){
					Message temp=replyQueue.poll();
					//calculate the remaining carports
					if(temp.getgtype()==GateType.entry)
						remain-=temp.getcarnum();					
					else
						remain+=temp.getcarnum();					
				}
				//add self
				if(gType==GateType.entry)
					remain-=num_of_cars;					
				else
					remain+=num_of_cars;
				
				if(remain>0){
					num_of_cars++;
					remain--;
				
				System.out.println("car entered!");
				System.out.println(remain+" carports remain");
				System.out.println("------------------------------");
				}
				else{
					System.out.println("there is no carposts");
					System.out.println("------------------------------");

				}
				
				state=State.RELEASED;
				count=0;
				//update logical clock
				timeStamp++;
				//reply to the request queue
				if (!requestQueue.isEmpty()) {
					//System.out.println("reply to request queue");
				}
				while(!requestQueue.isEmpty()){
					Message temp=requestQueue.poll();
					sendReply(temp, timeStamp);
				}
			}
		}		
	}
	

	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true){
			Message msg=null;

			//connect with other thread
			try {
				socket=serverSocket.accept();
				objInput=new ObjectInputStream(socket.getInputStream());
				objOutput=new ObjectOutputStream(socket.getOutputStream());
				//System.out.println("connected!");
				msg=(Message)objInput.readObject();
				
				//handle the message receieved
				onRecieve(msg);
				

				// close the inputstrem, outputstream and the socket 
				objInput.close();
				objOutput.close();
				socket.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

			
			
		}
	    

	}
	
	
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException{
		Gateway[] gateways=new Gateway[3];
		gateways[0]=new Gateway(0, 8888, 0, GateType.entry);
		gateways[1]=new Gateway(1, 8889, 0, GateType.entry);
		gateways[2]=new Gateway(2, 8890, 0, GateType.exit);

		new Thread(gateways[0]).start();
		new Thread(gateways[1]).start();
		new Thread(gateways[2]).start();

		
		/*gateway2.carEnter();
		gateway1.carEnter();
		Thread.sleep(3000);
		gateway3.carExit();
		gateway3.carEnter();
		gateway3.carExit();
		gateway2.carExit();
		gateway2.carEnter();*/

		System.out.println("please enter the command");
		String command=new Scanner(System.in).nextLine();
		while(!command.equals("end")){
			String[] comms=command.split(" ");
			if(comms[0].equals("enter"))
				gateways[Integer.parseInt(comms[1])].carEnter();
			else 
				gateways[Integer.parseInt(comms[1])].carExit();
			
			Thread.sleep(1000);
			System.out.println("please enter the command");
			command=new Scanner(System.in).nextLine();

		}
		
	}

}
