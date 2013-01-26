import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;


public class HelloClient {
	
	public static void main(String[] args){
		try {
			IHello rhello=(IHello)Naming.lookup("rmi://localhost:8888/RMI");
			System.out.println(rhello.HelloWorld());
			System.out.println(rhello.Sayhellotosb("zrf"));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
