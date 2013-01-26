import java.rmi.Remote;
import java.rmi.RemoteException;


public interface IHello extends Remote{
	
	public String HelloWorld() throws RemoteException;
	
	public String Sayhellotosb(String name) throws RemoteException;

}
