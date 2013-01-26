import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;


public class HelloServer {
	public static void main(String[] args){
		try {
			IHello rhello=new HelloImple();
			LocateRegistry.createRegistry(8888);
			Naming.bind("rmi://localhost:8888/RMI", rhello);
			System.out.println("°ó¶¨³É¹¦£¡");
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AlreadyBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	} 

}
