import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

public class FAMonitor extends UnicastRemoteObject implements 	FAListeners , Runnable {
	
	JFrame frame = new JFrame("Monitor");
	private JPanel contentPane = new JPanel();
	private JPanel contentPane2 = new JPanel();
	static DefaultListModel<String> Sensor_List = new DefaultListModel<String>();
	JList<String> sensorList = new JList<String>(Sensor_List);
	JButton btnSensorStatus = new JButton("Get Stats");
	JButton btnAllStatus = new JButton("View All Stats");
	JButton btnClients = new JButton("View Online Clients");
	static JTextArea text = new JTextArea(10,70);
	
	static FAMonitor monitor;
	static FAService service = null;

	private static String monID;
	
	public FAMonitor() throws Exception {
		// Layout GUI
		
		contentPane2.setLayout(new BorderLayout());
	    contentPane2.add(new JLabel("Active Sensors                                                              "), "West");
	    contentPane2.add(new JLabel("Stats"), "Center");
		
	    frame.getContentPane().setLayout(new BorderLayout());
	    sensorList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
	    sensorList.setSize(20, 50);
	    frame.getContentPane().add(new JScrollPane(sensorList), "West");
	    frame.getContentPane().add(new JScrollPane(text), "East");
	    
	    contentPane.setLayout(new BorderLayout());
	    contentPane.add(btnSensorStatus, "West");
	    contentPane.add(btnClients,"Center");
	    contentPane.add(btnAllStatus, "East");
	    
	    frame.getContentPane().add(contentPane, "South");
	    frame.getContentPane().add(contentPane2, "North");
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.pack();
	    service = (FAService) Naming.lookup("//localhost/AlarmService");
	    
	    updateSensorList();
	    
	    btnSensorStatus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	if(sensorList.isSelectionEmpty()) {
            		System.out.println("Sensor is not Selected!");
            	}
            	else {
            		try {
						getSensorStat(sensorList.getSelectedValue());
					} catch (RemoteException e1) {
						e1.printStackTrace();
					}
            	}
            }
        });
	    
	    btnAllStatus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	try {
            		getAllSensorStats();
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
            	
            }
        });
	    
	    btnClients.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	try {
            		int nMonitors = service.getTotalMonitors();
            		int nSensors = service.getTotalSensors();
            		text.setText(text.getText()+"***********************************************************************************************************\n"
            		+"No.of Active Sensors : "+nSensors+"\n"+"No.of Active Monitors :"+nMonitors+"\n\n");
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
            	
            }
        });
	    
	    frame.addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					service.deleteMonitor(monitor);
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
				
			}
			
			@Override
			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	public void alertMonitors(String sensid,String stat) throws java.rmi.RemoteException{
		text.setText(text.getText()+"**Alert****************************************************************************************************"
				+ "\n"+"Sensor : "+sensid+"\n"+stat+"\n\n");
		System.out.println("monitors alerted"+sensid+stat);
	}
	
	public void updateSensorList() throws java.rmi.RemoteException{
		try {
        	HashSet<String> idset = service.SensorIDs();
        	Sensor_List.clear();
        	for(String id: idset) {
        		Sensor_List.addElement(id);
    		}
    	}
    	catch(RemoteException re) {
            re.printStackTrace();
    	}
	}
	
	public static void getSensorStat(String id) throws java.rmi.RemoteException{
		try {
        	String stats = service.getSensorStatus(id);
			text.setText(text.getText()+"***********************************************************************************************************\n"+stats+"\n\n");
    	}
    	catch(RemoteException re) {
            re.printStackTrace();
    	}
	}
	
	public static void getAllSensorStats() throws java.rmi.RemoteException{
		try {
        	HashMap<String,String> stats = service.TotSensorData();
        	Iterator it = stats.entrySet().iterator();
        	text.setText(text.getText()+"***********************************************************************************************************\n\n");
			while(it.hasNext()) {
				Map.Entry<String,String> map = (Map.Entry)it.next();
				String stat = map.getKey()+" : "+map.getValue();
				text.setText(text.getText()+stat+"\n");
			}
    	}
    	catch(RemoteException re) {
            re.printStackTrace();
    	}
	}
	
	public static void main(String[] args) {
		try {

        	monitor = new FAMonitor();
        	monitor.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    		monitor.frame.setVisible(true);
        	service.insertMonitor(monitor);
        	monitor.run();
        	try {
	        	HashSet<String> idset = service.SensorIDs();
	        	Sensor_List.clear();
	        	for(String id: idset) {
	        		Sensor_List.addElement(id);
	    		}
	    	}
	    	catch(RemoteException re) {
	            re.printStackTrace();
	    	}
        	getAllSensorStats();
		}
		catch (MalformedURLException mue) {
		} 
		catch (RemoteException re) {
		} 
		catch (NotBoundException nbe) {
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void run(){
		if (System.getSecurityManager() == null)
        {
            System.setSecurityManager (new RMISecurityManager());
        }
 
       
        try {
            service = (FAService) Naming.lookup("//localhost/AlarmService");
        } catch (NotBoundException ex) {
            System.err.println(ex.getMessage());
        } catch (MalformedURLException ex) {
            System.err.println(ex.getMessage());
        } catch (RemoteException ex) {
            System.err.println(ex.getMessage());
        }
    }
}
