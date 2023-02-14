package simse.gui;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Vector;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import simse.state.State;

public abstract class TableModel<T> {

	protected Vector<String> columnNames;
	protected TableView<T> table;
	protected NumberFormat numFormat;
	protected Vector<T> data;
	protected State state;
	
	public TableModel(State s) {
		this.state = s;
		this.columnNames = new Vector<String>();
		this.data = new Vector<T>();
		this.numFormat = NumberFormat.getNumberInstance(Locale.US);
		initColNames();
		update();
	}
	
	abstract void initColNames();
	abstract Vector<T> getRepository();
	abstract Object getValueAt(int row, int col);
	abstract void setValueAt(Object vaue, int row, int col);
	
	public void update() {
		if(!state.getClock().isStopped()) {
			data.clear();
			for(T gen : getRepository()) {
				data.add(gen);
			}
			fireTableDataChanged();
		}

	}
	
	public int getColumnCount() {
		return columnNames.size();
	}
	
	public void addColumnName(String name) {
		this.columnNames.add(name);
	}
	
	public State getState() {
		return this.state;
	}
	
	public Vector<T> getData() {
		return this.data;
	}
	
	public int getRowCount() {
		if (data.size() > 0) {
			return columnNames.size();
		}
		return 0;
	}

	public String getColumnName(int col) {
		return columnNames.elementAt(col);
	}

	public int getColumnIndex(String columnName) {
		for (int i = 0; i < columnNames.size(); i++) {
			String colName = columnNames.elementAt(i);
			if (colName.equals(columnName)) {
				return i;
			}
		}
		return -1;
	}
	
	public TableView<T> createTable() {
		ObservableList<T> data = FXCollections.observableArrayList();
		table = new TableView<T>();
		
		for(int i = 0; i < this.getColumnCount(); i++) {
			TableColumn<T, String> newCol = new TableColumn<T, String>(this.getColumnName(i));
		    newCol.setCellValueFactory(
		            new PropertyValueFactory<>(this.getColumnName(i)));
		    table.getColumns().add(newCol);
		}
		
		for(T gen : this.getData()) {
			data.add(gen);
		}
		
		table.setItems(data);
		table.getSelectionModel().setCellSelectionEnabled(true);
		return table;
	}
	
	public void fireTableCellUpdated(int row, int col) {
		if(table != null) table.refresh();
	}
	public void fireTableStructureChanged() {
		if(table != null) table.refresh();
	}
	public void fireTableDataChanged(){
		if(table != null) table.refresh();
	}
	
	public Class getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}
}
