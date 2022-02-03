import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.Scanner;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import javax.swing.Timer;

public class Sensor {
	Scanner scanner;
    String SID;
	BufferedReader in;
    PrintWriter out;
	
    
    int Temp;
    int Battery;
    int Smoke;
    int CO2;
    static Timer t;
    
    JFrame frame = new JFrame("Sensor");
    DefaultListModel<String> dList = new DefaultListModel<String>();
    JList<String> clientList = new JList<String>(dList);
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
	private Socket socket; 
    
    public Sensor() {
	 // Layout GUI
	    clientList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	    //clientList.clearSelection();
	    frame.setLayout(new BorderLayout());
	    frame.getContentPane().add(new JScrollPane(clientList), "Center");
	    frame.pack();
    }
    
	private String getServerAddress() {
		
		return JOptionPane.showInputDialog(
	            frame,
	            "Enter IP Address: ",
	            "Alarm Sensor : New",
	            JOptionPane.QUESTION_MESSAGE);
    }
	
	private String getNewSensor() {	
		return JOptionPane.showInputDialog(frame,"Enter Floor Number:","Register SensorID",JOptionPane.PLAIN_MESSAGE)
				+"-"+
				JOptionPane.showInputDialog(frame,"Enter Sensor Number:","Register SensorID",JOptionPane.PLAIN_MESSAGE);
	}
	
	public String statGenerator(){
		Random rand = new Random();
		Temp = rand.nextInt(70) + 10;
		Battery = rand.nextInt(101);
		Smoke = rand.nextInt(11);
		CO2 = rand.nextInt(500) + 100;
		String stat = Temp+":"+Battery+":"+Smoke+":"+CO2;
		return stat;
	}
	
	public void runTimer() {
		String stat = statGenerator();
        dList.addElement("Temp : "+Temp);
        dList.addElement("Battery : "+Battery);
        dList.addElement("Smoke : "+Smoke);
        dList.addElement("CO2 : "+CO2);
        out.println("USUAL"+SID+"|"+stat);
        
        t = new Timer(60000, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				dList.clear();
                String stat = statGenerator();
                dList.addElement("Temp : "+Temp);
                dList.addElement("Battery : "+Battery);
                dList.addElement("Smoke : "+Smoke);
                dList.addElement("CO2 : "+CO2);
                out.println("USUAL"+SID+"|"+stat);
			}
		});
        t.start();
    }

	public static void main(String[] args) throws Exception{
		Sensor sensor = new Sensor();
		sensor.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        sensor.frame.setVisible(true);
		sensor.run();
	}
	
	private void run() throws IOException {

        // Make connection and initialize streams
        String serverAddress = getServerAddress();
        socket = new Socket(serverAddress, 9001);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // Process all messages from server, according to the protocol.
        while (true) {
            String serverMsg = in.readLine();
            
            if(serverMsg.startsWith("SUBMITSENSORID")) {
            	String newSensor = getNewSensor();
            	out.println(newSensor);
            }
            else if(serverMsg.startsWith("NEWSENSORADDED")) {
            	SID = serverMsg.substring(11);
            	frame.setTitle("Sensor (ID : "+SID+")");
            	runTimer();
            }
            else if(serverMsg.startsWith("GETSTATUS")) {
            	out.println("USUAL"+SID+":"+statGenerator());
            }
            else if(serverMsg.startsWith("TEST")) {
            	out.println("main thread run");
            }
        }
    }

}
