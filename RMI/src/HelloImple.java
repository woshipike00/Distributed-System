import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


public class HelloImple extends UnicastRemoteObject implements IHello{
	
	public HelloImple() throws RemoteException{}

	@Override
	public String HelloWorld() throws RemoteException {
		// TODO Auto-generated method stub
		return "hello world";
	}

	@Override
	public String Sayhellotosb(String name) throws RemoteException {
		// TODO Auto-generated method stub
		return "hello "+name;
	}
	

}
