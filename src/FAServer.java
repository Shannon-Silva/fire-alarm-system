import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;


import javax.management.monitor.Monitor;

public class FAServer extends UnicastRemoteObject implements FAService {

	protected FAServer() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	private static final int PORT = 9001;
	

	
	public static HashMap<String,PrintWriter> Sensors = new HashMap<String,PrintWriter>();

	public static HashMap<String,String> sensorStatus = new HashMap<String,String>();
	
	private static ArrayList<FAMonitor> monList=new ArrayList<FAMonitor>();
	
	
	
	@Override
	public void insertMonitor(FAListeners listener) throws RemoteException {
		// TODO Auto-generated method stub
		
		monList.add(listener);
		System.out.println("adding monitor - "+listener);
	}

	@Override
	public void deleteMonitor(FAListeners listener) throws RemoteException {
		// TODO Auto-generated method stub
		monList.remove(listener);
		System.out.println("removing monitor - "+listener);
	}

	@Override
	public int getTotalMonitors() throws RemoteException {
		// TODO Auto-generated method stub
		System.out.println("Total Monitors - " + monList.size());
		return monList.size();
			
	}

	@Override
	public int getTotalSensors() throws RemoteException {
		// TODO Auto-generated method stub
		System.out.println("Total Sensors - " + Sensors.size());
		return Sensors.size();
		
	}

	@Override
	public HashSet<String> SensorIDs() throws RemoteException {
		// TODO Auto-generated method stub
		HashSet<String> Sensorids = new HashSet<String>();
    	for ( String key : Sensors.keySet() ) {
    	    Sensorids.add(key);
    	}
    	return Sensorids;
	
	}

	@Override
	public String getSensorStatus(String Sid) throws RemoteException {
		// TODO Auto-generated method stub
		Iterator iterator = sensorStatus.entrySet().iterator();
		
		while(iterator.hasNext()) {
			Map.Entry<String,String> map = (Map.Entry)iterator.next();
			
			if(map.getKey().equals(Sid)){
				String stat = map.getKey()+" : "+map.getValue();
				return stat;
			}
		}
		return "Sensor Status not available";
		
	}

	@Override
	public HashMap<String, String> TotSensorData() throws RemoteException {
		// TODO Auto-generated method stub
		System.out.println("Get Total Sensor Data");
		return sensorStatus;
	}
	
	private static void alertListeners(String sid, String status) {
		try{
			for(FAListeners fal : monList){
				fal.alertMonitors(sid,status);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void updateMonitors() {
		try{
			for(FAListeners fal : monList){
				fal.updateSensorList();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	
	public static void main(String[] args) throws Exception {
        System.out.println("The Fire Alarm server is running.");
        ServerSocket listener = new ServerSocket(PORT);
        
        if (System.getSecurityManager() == null)
            System.setSecurityManager ( new RMISecurityManager() );
        try{
            LocateRegistry.createRegistry(2099); 
            FAServer svr = new FAServer();
            Naming.bind ("Fire Alarm Service", svr);
            System.out.println ("Service started....");
            
            try {
            	 while (true) {
                     new Handler(listener.accept()).start();
                 }
            }catch(Exception e) {
            	
            }
           
           
        }
        catch(RemoteException re){
            System.err.println(re.getMessage());
        }
        catch(AlreadyBoundException abe){
            System.err.println(abe.getMessage());
        }
        catch(MalformedURLException mue){
            System.err.println(mue.getMessage());
        }
        finally {
            listener.close();
        }
    }
	
	private static class Handler extends Thread {
		private String sensorID; 
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        
        public Handler(Socket socket) {
            this.socket = socket; }
            
        public void run() {
        	try {
        		in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                
                while(true) {
                	out.println("SUBMITSENSORID");
                    sensorID = in.readLine();
                    if (sensorID == null) {
                        return;
                    }
                    synchronized (Sensors) {
                        if (!Sensors.containsKey(sensorID)) {
                            Sensors.put(sensorID, out);                           
                            break;
                        }
                        
                    }
                }
                
                out.println("NEW SENSOR ADDED");
                Sensors.put(sensorID,out);
                	
                while(true) {
                	String input = in.readLine();
                    if (input == null) {
                        return;
                    }
                    else if(input.startsWith("USUAL")){
                    	String output=input.substring(12);
                    	System.out.println(output);
                    	
                    	StringTokenizer token= new StringTokenizer(output,"|");
                    	
                    	String sensor_id = token.nextToken();
                    	String status= token.nextToken();
                    	StringTokenizer token2= new StringTokenizer(status,":");
                    	
                    	int co2 = Integer.parseInt(token2.nextToken());
                    	int temp = Integer.parseInt(token2.nextToken());
                    	int battery = Integer.parseInt(token2.nextToken());
                    	int smoke = Integer.parseInt(token2.nextToken());
                    	
                    	String status_val = "CO2 LEVEL : "+co2+
                    						"\tTemperature : "+temp+
                    						"\tBattery Level :"+battery+
                    						"\tSmoke Level : "+smoke+"\n";
                    	
                    	if(sensorStatus.containsKey(sensor_id)) {
                    		sensorStatus.remove(sensor_id);
                    	}
                    	sensorStatus.put(sensor_id, status_val);
                    	if(temp>50 || smoke>7) {
                    		alertListeners(sensor_id, status_val);
                    	}
                    			
                    }
                }
                                   
        	}
        	catch(Exception e) {
        		System.out.println(e);
        	}
        	finally {
        		// This client is going down!  Remove its name and its print
                // writer from the sets, and close its socket.
        		
        			if (sensorID != null) {
                    Sensors.remove(sensorID);
                    updateMonitors();   /**/
                    System.out.println(sensorID+ " is Removed.");
                    System.out.println("No.of Sensors active: "+ Sensors.size()); /**/
                    
                    if (out != null) {
                        Sensors.remove(out);
                    }
                    
                    try {
                        socket.close();
                    } catch (IOException e) {
                    	System.out.println(e);
                    }
                    }
        	}
        }
	}


}
