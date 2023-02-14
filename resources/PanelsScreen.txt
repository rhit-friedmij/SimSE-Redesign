package simse.gui;

import java.util.List;

import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import simse.logic.Logic;
import simse.state.State;

public class PanelsScreen extends Stage implements EventHandler<MouseEvent> {
	State state;
	SimSEGUI gui;
	Logic logic;
	
	GridPane mainPane;
	
	Button infoButton;
	Button employeeButton;
	Button trackButton;
	Button melloButton;
	Button progGraphButton;

	@Override
	public void handle(MouseEvent e) {
		Object src = e.getSource();
		if (src == infoButton) {
			if (infoButton.getText().equals("Add")) {
				gui.addBottomPanel(Panels.INFORMATION);
			} else {
				gui.removePanel(Panels.INFORMATION);
				infoButton.setText("Add");
			}
		} else if (src == employeeButton) {
			if (employeeButton.getText().equals("Add")) {
				gui.addSidePanel(Panels.EMPLOYEES);
			} else {
				gui.removePanel(Panels.EMPLOYEES);
				employeeButton.setText("Add");
			}
		} else if (src == trackButton) {
			if (trackButton.getText().equals("Add")) {
				gui.addBottomPanel(Panels.TRACK);
			} else {
				gui.removePanel(Panels.TRACK);
				trackButton.setText("Add");
			}
		} else if (src == melloButton) {
			if (melloButton.getText().equals("Add")) {
				gui.addBottomPanel(Panels.MELLO);
			} else {
				gui.removePanel(Panels.MELLO);
				melloButton.setText("Add");
			}
		} else if (src == progGraphButton) {
			if (progGraphButton.getText().equals("Add")) {
				gui.addBottomPanel(Panels.GRAPH);
			} else {
				gui.removePanel(Panels.GRAPH);
				progGraphButton.setText("Add");
			}
		}
	}

	public PanelsScreen(State state, SimSEGUI gui, Logic logic) {
		super();
		this.state = state;
		this.gui = gui;
		this.logic = logic;
		
		this.setTitle("Windows Screen");

		
		mainPane = new GridPane();
		mainPane.setGridLinesVisible(true);
		mainPane.getColumnConstraints().add(new ColumnConstraints(150, 150, 150, Priority.SOMETIMES,
				HPos.CENTER, false));
		mainPane.getColumnConstraints().add(new ColumnConstraints(300, 300, 300, Priority.SOMETIMES,
				HPos.CENTER, false));
		mainPane.getColumnConstraints().add(new ColumnConstraints(70, 70, 70, Priority.SOMETIMES,
				HPos.CENTER, false));
		mainPane.getColumnConstraints().add(new ColumnConstraints(100, 100, 100, Priority.SOMETIMES,
				HPos.CENTER, false));
		
		mainPane.add(new Label("Window Name"), 0, 0);
		mainPane.add(new Label("Description"), 1, 0);
		mainPane.add(new Label("Location"), 2, 0);
		mainPane.add(new Label("Add/Remove"), 3, 0);
		
		mainPane.add(new Label("Detailed Information"), 0, 1);
		Label infoLabel = new Label("Provides detailed quick reference information on some game elements");
		infoLabel.setWrapText(true);
		GridPane.setMargin(infoLabel, new Insets(5));
		mainPane.add(infoLabel, 1, 1);
		mainPane.add(new Label("Bottom"), 2, 1);
		infoButton = new Button("Add");
		infoButton.addEventHandler(MouseEvent.MOUSE_CLICKED, this);
		mainPane.add(infoButton, 3, 1);
		
		mainPane.add(new Label("Employee Reference"), 0, 2);
		Label employeeLabel = new Label("Provides quick access to employees to assign them to tasks"
				+ " and display their information on the detailed information panel");
		employeeLabel.setWrapText(true);
		GridPane.setMargin(employeeLabel, new Insets(5));
		mainPane.add(employeeLabel, 1, 2);
		mainPane.add(new Label("Side"), 2, 2);
		employeeButton = new Button("Add");
		employeeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, this);
		mainPane.add(employeeButton, 3, 2);
		
		mainPane.add(new Label("Track"), 0, 3);
		Label trackLabel = new Label("Employee chat application where task completion is reported "
				+ "and random events are shown");
		trackLabel.setWrapText(true);
		GridPane.setMargin(trackLabel, new Insets(5));
		mainPane.add(trackLabel, 1, 3);
		mainPane.add(new Label("Bottom"), 2, 3);
		trackButton = new Button("Add");
		trackButton.addEventHandler(MouseEvent.MOUSE_CLICKED, this);
		mainPane.add(trackButton, 3, 3);
		
		mainPane.add(new Label("Mello"), 0, 4);
		Label melloLabel = new Label("Quick reference for task progress and employees assigned to "
				+ "each task");
		melloLabel.setWrapText(true);
		GridPane.setMargin(melloLabel, new Insets(5));
		mainPane.add(melloLabel, 1, 4);
		mainPane.add(new Label("Bottom"), 2, 4);
		melloButton = new Button("Add");
		melloButton.addEventHandler(MouseEvent.MOUSE_CLICKED, this);
		mainPane.add(melloButton, 3, 4);
		
		mainPane.add(new Label("Project Progress Graph"), 0, 5);
		Label progGraphLabel = new Label("Graph displaying the current progress of the project");
		progGraphLabel.setWrapText(true);
		GridPane.setMargin(progGraphLabel, new Insets(5));
		mainPane.add(progGraphLabel, 1, 5);
		mainPane.add(new Label("Bottom"), 2, 5);
		progGraphButton = new Button("Add");
		progGraphButton.addEventHandler(MouseEvent.MOUSE_CLICKED, this);
		mainPane.add(progGraphButton, 3, 5);
		
//		mainPane.setPrefSize(300, 400);
		this.setScene(new Scene(mainPane));
	}
	
	public void update() {
		List<Panels> panels = gui.getPresentPanels();
		for (Panels panel : panels) {
			switch (panel) {
			
			case INFORMATION:
				infoButton.setText("Remove");
				break;
				
			case EMPLOYEES:
				employeeButton.setText("Remove");
				break;
				
			case TRACK:
				trackButton.setText("Remove");
				break;
				
			case MELLO:
				melloButton.setText("Remove");
				break;
				
			case GRAPH:
				progGraphButton.setText("Remove");
				
			default:
				break;
				
			}
				
		}
	}

}
