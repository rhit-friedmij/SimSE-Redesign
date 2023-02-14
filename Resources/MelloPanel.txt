package simse.gui;

import java.util.Vector;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import simse.gui.util.JavaFXHelpers;

public class MelloPanel extends Pane implements SimSEPanel{
	
	private GridPane mello;
	private ScrollPane inProgress;
	private ScrollPane complete;
	private VBox prog;
	private VBox comp;
	private static MelloPanel instance = null;
	
	private MelloPanel() {
		VBox pane = new VBox();
		HBox top = new HBox();
		top.getChildren().add(JavaFXHelpers.createImageView("src/simse/gui/icons/mello.png"));
		top.getChildren().add(new Text("Mello"));
		top.setPrefWidth(600);
		top.setBackground(JavaFXHelpers.createBackgroundColor(Color.rgb(128, 128, 128)));
		
		inProgress = new ScrollPane();
		prog = new VBox();
		prog.heightProperty().addListener(observable -> inProgress.setVvalue(1D));
		prog.setBackground(JavaFXHelpers.createBackgroundColor(Color.rgb(217, 217, 217)));
		inProgress.setBackground(JavaFXHelpers.createBackgroundColor(Color.rgb(217, 217, 217)));
		inProgress.setContent(prog);
		inProgress.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
		inProgress.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		inProgress.setVvalue(1.0);
		inProgress.setFitToWidth(true);
		
		complete = new ScrollPane();
		comp = new VBox();
		comp.heightProperty().addListener(observable -> complete.setVvalue(1D));
		comp.setBackground(JavaFXHelpers.createBackgroundColor(Color.rgb(217, 217, 217)));
		complete.setBackground(JavaFXHelpers.createBackgroundColor(Color.rgb(217, 217, 217)));
		complete.setContent(comp);
		complete.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
		complete.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		complete.setVvalue(1.0);
		complete.setFitToWidth(true);
		
		mello = new GridPane();
		mello.setBackground(JavaFXHelpers.createBackgroundColor(Color.rgb(217, 217, 217)));
		inProgress.setBackground(JavaFXHelpers.createBackgroundColor(Color.rgb(217, 217, 217)));
		complete.setBackground(JavaFXHelpers.createBackgroundColor(Color.rgb(217, 217, 217)));
		prog.setBackground(JavaFXHelpers.createBackgroundColor(Color.rgb(217, 217, 217)));
		comp.setBackground(JavaFXHelpers.createBackgroundColor(Color.rgb(217, 217, 217)));
		
		mello.add(new Text("In Progress"), 0, 0);
		mello.add(new Text("Complete"), 1, 0);
		mello.add(inProgress, 0, 1);
		mello.add(complete, 1, 1);
		
		pane.getChildren().add(top);
		pane.getChildren().add(mello);
		
		

		this.getChildren().add(pane);
	}
	
	public static MelloPanel getInstance() {
		if (instance == null) instance = new MelloPanel();
		return instance;
	}
	
	public void addTaskInProgress(String task, Vector<String> employees) {
		removeTask(task);
		prog.getChildren().add(new MelloItem(task, employees));
	}
	
	public void addEmployeeToTask(String task, String employee) {
		MelloItem inProg = findTaskInProgress(task);
		prog.getChildren().remove(inProg);
		prog.getChildren().add(inProg.addEmp(employee));
	}
	
	public void removeEmployeeFromTask(String task, String employee) {
		MelloItem inProg = findTaskInProgress(task);
		prog.getChildren().remove(inProg);
		inProg = inProg.removeEmp(employee);
		if (inProg != null) prog.getChildren().add(inProg);
	}
	
	public void stopEverything() {
		prog.getChildren().removeAll(prog.getChildren());
	}
	
	public void completeTask(String task) {
		MelloItem found = findTaskInProgress(task);
		if (found != null) prog.getChildren().remove(found);
		comp.getChildren().add(found);
	}
	
	public void removeTask(String task) {
		MelloItem inProg = findTaskInProgress(task);
		if (findTaskInProgress(task) != null) prog.getChildren().remove(inProg);
	}
	
	private MelloItem findTaskInProgress(String task) {
		for (Node n : prog.getChildren()) {
			if (n instanceof MelloItem) {
				if (((MelloItem) n).isTask(task)) return (MelloItem) n;
			}
		}
		return null;
	}

	@Override
	public Panels getPanelType() {
		return Panels.MELLO;
	}

}
