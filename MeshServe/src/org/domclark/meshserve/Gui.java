/*
 *  Copyright © 2012 Dominic Clark (TheSuccessor)
 *
 *  This file is part of MeshServe.
 *
 *  MeshServe is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MeshServe is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MeshServe.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.domclark.meshserve;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

	private static final int fileVersion = 1;

	private JFrame window;
	private JTextArea log;
	private Server server;
	private JButton start;
	private JButton stop;
	private JButton evict;
	private JButton setServer;
	private JButton mute;
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

			private static final long serialVersionUID = 4517734L;
			private final String[] colNames = new String[]{
				"IP address",
				"Port number",
				"Group"
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
				if(columnIndex == 0) return c.ip();
				else if(columnIndex == 1) return c.port();
				else return c.getGroup().getName();
			}

			public Class<?> getColumnClass(int c){
				return String.class;
			}

			public boolean isCellEditable(int row, int col){
				return col == 2;
			}

			public void setValueAt(Object value, int row, int col){
				if(col != 2) return;
				Client c = server.getClients().get(row);
				if(c.getGroup().getName().equals(value)) return;
				Group newGroup = null;
				for(Group g : server.getGroups()) if(g.getName().equals(value)) newGroup = g;
				if(newGroup == null) return;
				newGroup.addClient(c);
				fireTableCellUpdated(row, col);
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
		mute = new JButton("Mute");
		mute.setEnabled(false);
		mute.addActionListener(this);
		clientControls.add(mute);
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
		readOptions();
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
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		log(sw.toString(), true);
		pw.close();
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
		int row = clients.getSelectedRow();
		boolean hasSelection = row != -1;
		setServer.setEnabled(hasSelection && server.getType() == 1);
		evict.setEnabled(hasSelection);
		mute.setEnabled(hasSelection);
		boolean muted = false;
		if(hasSelection) muted = server.getClients().get(row).isMuted();
		mute.setText((muted) ? "Unmute" : "Mute");
	}

	public void saveOptions(){
		File f = new File("meshserve.dat");
		try {
			f.createNewFile();
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(f));
			dos.writeInt(fileVersion);
			dos.writeBoolean(loggingEnabled);
			server.writeOn(dos);
			dos.close();
		} catch (IOException e) {
			log("Failed to save settings:", true);
			log(e);
		}
	}

	public void readOptions(){
		File f = new File("meshserve.dat");
		if(!f.exists()) return;
		try {
			DataInputStream dis = new DataInputStream(new FileInputStream(f));
			int version = dis.readInt();
			if(version != fileVersion) throw new IOException("Invalid settings file version! " +
					"(Found " + version + ", required " + fileVersion + ")");
			loggingEnabled = dis.readBoolean();
			server.readFrom(dis);
			dis.close();
		} catch (IOException e) {
			log("Failed to load settings:", true);
			log(e);
		}
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
			if(c.isHost() && server.getType() == 1)
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
			c.getGroup().setHost(c);
		} else if(action.equals("Send message") && server.isRunning()){
			String s = msg.getText();
			if(s.equals("")) return;
			log("Sending message \"" + s + "\" to all clients");
			server.sendToClientsExcept(s, null);
		} else if(action.equals("Mute")){
			int client = clients.getSelectedRow();
			if(client == -1) return;
			server.getClients().get(client).setMuted(true);
			updateButtons();
		} else if(action.equals("Unmute")){
			int client = clients.getSelectedRow();
			if(client == -1) return;
			server.getClients().get(client).setMuted(false);
			updateButtons();
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
