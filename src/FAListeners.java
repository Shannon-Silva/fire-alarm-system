import java.rmi.Remote;

public interface FAListeners extends Remote{

	public void alertMonitors(String sensorid,String status)throws java.rmi.RemoteException;	

	public void updateSensorList() throws java.rmi.RemoteException;
}
