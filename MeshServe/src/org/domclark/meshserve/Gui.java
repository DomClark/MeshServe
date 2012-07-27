package org.domclark.meshserve;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

public class Gui implements WindowListener, ActionListener, ListSelectionListener {

	private JFrame window;
	private JTextArea log;
	private Server server;
	private JButton start;
	private JButton stop;
	private JButton evict;
	private JButton setServer;
	private JButton send;
	private JTextField msg;
	private JTable clients;
	private AbstractTableModel clientModel;
	private OptionsDialog options;
	private boolean loggingEnabled;

	public Gui(){
		server = new Server(this);
		window = new JFrame("MeshServe");
		options = new OptionsDialog(this, window, server);
		loggingEnabled = true;
		window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		window.addWindowListener(this);
		window.setPreferredSize(new Dimension(320, 680));
		window.setLayout(new BoxLayout(window.getContentPane(), BoxLayout.Y_AXIS));
		JPanel controls = new JPanel();
		start = new JButton("Start");
		start.addActionListener(this);
		stop = new JButton("Stop");
		stop.setEnabled(false);
		stop.addActionListener(this);
		JButton config = new JButton("Configure");
		config.addActionListener(this);
		controls.add(start);
		controls.add(stop);
		controls.add(config);
		window.add(controls);
		clients = new JTable();
		clientModel = new AbstractTableModel(){

			private static final long serialVersionUID = 5318008L;
			private final String[] colNames = new String[]{
				"ID",
				"IP address",
				"Port number"
			};

			public int getRowCount() {
				return server.getClients().size();
			}

			public int getColumnCount() {
				return colNames.length;
			}

			public String getColumnName(int col){
				return colNames[col];
			}

			public Object getValueAt(int rowIndex, int columnIndex) {
				Client c = server.getClients().get(rowIndex);
				if(columnIndex == 1) return c.ip();
				if(columnIndex == 2) return c.port();
				return (rowIndex + 1) + ((server.getType() == 1 && server.getHost() == c) ? " (server)" : "");
			}
			
		};
		clients.setModel(clientModel);
		clients.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		clients.getSelectionModel().addListSelectionListener(this);
		JScrollPane clientsScroller = new JScrollPane(clients);
		window.add(clientsScroller);
		JPanel clientControls = new JPanel();
		setServer = new JButton("Set as server");
		setServer.setEnabled(false);
		setServer.addActionListener(this);
		clientControls.add(setServer);
		evict = new JButton("Evict");
		evict.setEnabled(false);
		evict.addActionListener(this);
		clientControls.add(evict);
		window.add(clientControls);
		JPanel msgPanel = new JPanel();
		msg = new JTextField(12);
		send = new JButton("Send message");
		send.addActionListener(this);
		send.setEnabled(false);
		msgPanel.add(msg);
		msgPanel.add(send);
		window.add(msgPanel);
		log = new JTextArea();
		log.setEditable(false);
		log.setRows(20);
		JScrollPane logScroller = new JScrollPane(log);
		window.add(logScroller);
		window.pack();
		window.setVisible(true);
	}

	private void log(String s, boolean override){
		if(!(loggingEnabled || override)) return;
		log.append(s);
		log.append("\n");
	}

	public void log(String s){
		log(s, false);
	}

	public void log(Throwable e){
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		log(sw.toString(), true);
	}

	public void clearLog(){
		log.setText(null);
	}

	public void clientUpdated(boolean added){
		if(added){
			int row = server.getClients().size();
			clientModel.fireTableRowsInserted(row, row);
		} else {
			clientModel.fireTableDataChanged();
		}
	}

	public void updateButtons(){
		boolean isRunning = server.isRunning();
		start.setEnabled(!isRunning);
		stop.setEnabled(isRunning);
		send.setEnabled(isRunning);
		boolean hasSelection = clients.getSelectedRow() != -1;
		setServer.setEnabled(hasSelection && server.getType() == 1);
		evict.setEnabled(hasSelection);
	}

	public void setLogging(boolean enabled){
		loggingEnabled = enabled;
	}

	public boolean isLoggingEnabled(){
		return loggingEnabled;
	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		if(action.equals("Start") && !server.isRunning()){
			server.start();
		} else if(action.equals("Stop") && server.isRunning()){
			server.stop();
		} else if(action.equals("Configure")) {
			options.setVisible(true);
		} else if(action.equals("Evict")){
			int client = clients.getSelectedRow();
			if(client == -1) return;
			Client c = server.getClients().get(client);
			if(server.getHost() == c && server.getType() == 1)
				if(!((JOptionPane.showConfirmDialog(
						window,
						"You are trying to evict the host of a " + Server.TYPES[1] + " type server. Continue?",
						"Confirm eviction",
						JOptionPane.YES_NO_OPTION)) == JOptionPane.YES_OPTION)) return;
			server.removeConnection(c);
		} else if(action.equals("Set as server")){
			int client = clients.getSelectedRow();
			if(client == -1) return;
			Client c = server.getClients().get(client);
			server.setHost(c);
		} else if(action.equals("Send message") && server.isRunning()){
			String s = msg.getText();
			if(s.equals("")) return;
			log("Sending message \"" + s + "\" to all clients");
			server.sendToClientsExcept(s, null);
		}
	}

	public void windowClosing(WindowEvent e) {
		if(server.isRunning()){
			if(!((JOptionPane.showConfirmDialog(
					window,
					"The server is still running - really close?",
					"Confirm close",
					JOptionPane.YES_NO_OPTION)) == JOptionPane.YES_OPTION)) return;
		}
		log("Closing!");
		server.stop();
		window.dispose();
	}

	public void windowClosed(WindowEvent e) {
		System.gc();
		System.exit(0);
	}

	public void windowIconified(WindowEvent e) { }
	public void windowDeiconified(WindowEvent e) { }
	public void windowActivated(WindowEvent e) { }
	public void windowDeactivated(WindowEvent e) { }
	public void windowOpened(WindowEvent e) { }

	public void valueChanged(ListSelectionEvent e) {
		updateButtons();
	}

	public static void main(String[] args){
		new Gui();
	}

}
