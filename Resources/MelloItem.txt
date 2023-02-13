package simse.gui;

import java.util.Vector;

import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import simse.gui.util.JavaFXHelpers;

public class MelloItem extends Pane{
	
	String task;
	Vector<String> employees;
	
	public MelloItem(String item, Vector<String> workers) {
		this.task = item;
		this.employees = workers;
		HBox box = new HBox();
		
		HBox textBox = new HBox();
		textBox.setBackground(JavaFXHelpers.createBackgroundColor(Color.rgb(128, 128, 128)));
		textBox.getChildren().add(new Text(task));
		
		box.getChildren().add(textBox);
		
		for (String employee : employees) {
			ImageView img = JavaFXHelpers.createImageView("src/simse/gui/icons/" + employee + ".gif");
			if (img == null) {
				img = JavaFXHelpers.createImageView("src/simse/gui/icons/alex.gif");
			}
			box.getChildren().add(img);
		}
		
		this.getChildren().add(box);
	}
	
	public MelloItem(String item) {
		this.task = item;
		this.employees = new Vector<String>();
		HBox box = new HBox();
		
		HBox textBox = new HBox();
		textBox.setBackground(JavaFXHelpers.createBackgroundColor(Color.rgb(128, 128, 128)));
		textBox.getChildren().add(new Text(task));
		
		box.getChildren().add(textBox);
		
		this.getChildren().add(box);
	}
	
	public MelloItem addEmp(String emp) {
		employees.add(emp);
		return new MelloItem(task, employees);
	}
	
	public MelloItem removeEmp(String emp) {
		employees.remove(emp);
		if (employees.isEmpty()) return null;
		return new MelloItem(task, employees);
	}
	

	public boolean isTask(String task) {
		return this.task.equals(task);
	}

}
