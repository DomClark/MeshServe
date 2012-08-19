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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

public class GroupOptions extends OptionsPanel implements ActionListener, ListSelectionListener {

	private static final long serialVersionUID = 378806L;
	private static final String[] types = new String[]{
		"Public",
		"Invite"
	};

	private Gui gui;
	private Server target;
	private JTable groups;
	private AbstractTableModel groupsModel;
	private JButton delete;
	private JButton setdefault;
	private JComboBox gtype;
	private List<Object[]> data;
	private int defaultGroup;

	public GroupOptions(Gui gui, Server server){
		this.gui = gui;
		target = server;
		data = new ArrayList<Object[]>();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		groups = new JTable();
		groupsModel = new AbstractTableModel(){

			private static final long serialVersionUID = 3200L;
			private final String[] colNames = new String[]{
					"Name",
					"Max clients",
					"Number of clients",
					"Properties"
			};

			public int getRowCount() {
				return data.size();
			}

			public int getColumnCount() {
				return 4;
			}

			public String getColumnName(int col){
				return colNames[col];
			}

			public Object getValueAt(int rowIndex, int columnIndex) {
				if(columnIndex != 3) return data.get(rowIndex)[columnIndex];
				return (defaultGroup == rowIndex ? "Default, " : "") + (types[fastToInt(data.get(rowIndex)[3])]);
			}

			public Class<?> getColumnClass(int c){
				if(0 < c && c < 3) return Integer.class;
				return String.class;
			}

			public boolean isCellEditable(int row, int col){
				return col < 2;
			}

			public void setValueAt(Object value, int row, int col){
				if(col >= 2) return;
				if(col == 1 && row == defaultGroup && fastToInt(value) < target.getMaxConnections()) return;
				if(col == 0){
					for(Object[] o : data) if(o[0].equals(value)) return;
				}
				data.get(row)[col] = value;
				fireTableCellUpdated(row, col);
				valueChanged(null);
			}

		};
		groups.setModel(groupsModel);
		groups.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		groups.getSelectionModel().addListSelectionListener(this);
		JScrollPane groupsScroller = new JScrollPane(groups);
		add(groupsScroller);
		JPanel buttons = new JPanel();
		JButton add = new JButton("Add new");
		add.addActionListener(this);
		buttons.add(add);
		delete = new JButton("Delete");
		delete.addActionListener(this);
		delete.setEnabled(false);
		buttons.add(delete);
		setdefault = new JButton("Set as default");
		setdefault.addActionListener(this);
		setdefault.setEnabled(false);
		buttons.add(setdefault);
		gtype = new JComboBox(types);
		gtype.addActionListener(this);
		gtype.setEnabled(false);
		buttons.add(gtype);
		add(buttons);
	}

	private int fastToInt(Object intOrString){
		return (intOrString instanceof Integer) ? (Integer) intOrString : Integer.parseInt((String) intOrString);
	}

	public void onShow() {
		List<Group> grouplist = target.getGroups();
		int numRows = grouplist.size();
		data.clear();
		for(int i = 0; i < numRows; i++){
			Group g = grouplist.get(i);
			data.add(new Object[]{
					g.getName(),
					g.getMaxClients(),
					g.getClients().size(),
					g.getType()
			});
		}
		defaultGroup = grouplist.indexOf(target.getDefaultGroup());
		groupsModel.fireTableDataChanged();
	}

	public void saveOptions() {
		int numRows = data.size();
		List<Group> grouplist = target.getGroups();
		grouplist.clear();
		for(int i = 0; i < numRows; i++){
			Object[] datum = data.get(i);
			Group g = new Group((String) datum[0]);
			g.setMaxClients(fastToInt(datum[1]));
			g.setType(fastToInt(datum[3]));
			grouplist.add(g);
		}
		target.setDefaultGroup(grouplist.get(defaultGroup));
		gui.clientUpdated(false);
	}

	public String getTitle(){
		return "Groups";
	}

	public void valueChanged(ListSelectionEvent e) {
		int row = groups.getSelectedRow();
		boolean hasSelection = row != -1;
		delete.setEnabled(hasSelection && groups.getRowCount() > 1 && row != defaultGroup);
		setdefault.setEnabled(hasSelection);
		gtype.setEnabled(hasSelection && row != defaultGroup);
		if(hasSelection){
			Object[] datum = data.get(row);
			gtype.setSelectedIndex(fastToInt(datum[3]));
			setdefault.setEnabled(row != defaultGroup &&
					fastToInt(datum[1]) >= target.getMaxConnections() &&
					fastToInt(datum[3]) == 0);
		}
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if(cmd.equals("Add new")){
			String newName = "New group";
			int i = 1;
			List<String> badStrings = new ArrayList<String>();
			for(Object[] o : data){
				String s = (String) o[0];
				if(s.startsWith(newName)) badStrings.add(s.substring(newName.length()));
			}
			while(badStrings.contains(" " + i)) i++;
			newName += " " + i;
			data.add(new Object[]{newName, 20, 0, 0});
			int newSize = data.size();
			groupsModel.fireTableRowsInserted(newSize, newSize);
		} else if(cmd.equals("Delete")){
			int group = groups.getSelectedRow();
			if(group == -1 || groups.getRowCount() <= 1 || group == defaultGroup) return;
			data.remove(group);
			if(group < defaultGroup) defaultGroup--;
			groupsModel.fireTableRowsDeleted(++group, group);
			valueChanged(null);
		} else if(cmd.equals("Set as default")){
			int group = groups.getSelectedRow();
			if(group == -1) return;
			int oldGroup = defaultGroup;
			defaultGroup = group;
			groupsModel.fireTableCellUpdated(group, 3);
			groupsModel.fireTableCellUpdated(oldGroup, 3);
			valueChanged(null);
		} else if(e.getSource() == gtype){
			int group = groups.getSelectedRow();
			if(group == -1 || group == defaultGroup) return;
			data.get(group)[3] = gtype.getSelectedIndex();
			groupsModel.fireTableCellUpdated(group, 3);
			valueChanged(null);
		}
	}

}
