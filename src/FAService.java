import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;

public interface FAService extends Remote{
	
	public void insertMonitor(FAListeners listener) throws java.rmi.RemoteException;
	
    public void deleteMonitor(FAListeners listener) throws java.rmi.RemoteException;
    
    public int getTotalMonitors() throws java.rmi.RemoteException;
    
    public int getTotalSensors() throws java.rmi.RemoteException;
    
    public HashSet<String> SensorIDs() throws RemoteException;
    
    public String getSensorStatus(String Sid) throws java.rmi.RemoteException;
    
    public HashMap<String,String> TotSensorData() throws java.rmi.RemoteException;
    
 
    
}
