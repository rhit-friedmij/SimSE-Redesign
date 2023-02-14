package simse.gui;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import simse.adts.objects.Artifact;
import simse.logic.Logic;
import simse.state.State;
import simse.util.RuleType;

public class ArtifactsOverviewScreen extends Stage implements EventHandler<MouseEvent>{
	
	State state;
	SimSEGUI gui;
	Logic logic;

	Label titleLabel;
	Label toolsLabel;
	
	Button moreDetail, rules;
	
	TableModel tableModel;
	TableView table;
	
	BorderPane tablePane;
	VBox mainPane;
	
	public ArtifactsOverviewScreen(State s, SimSEGUI gui, Logic l) {
		this.state = s;
		this.gui = gui;
		this.logic = l;
		
		this.setTitle("Artifact Screen");
		mainPane = new VBox();
		
		mainPane = new VBox();
		BorderPane title = new BorderPane();
		titleLabel = new Label("Artifacts");
		titleLabel.setFont(new Font(36));
		title.setCenter(titleLabel);
		mainPane.getChildren().add(title);
		
		Separator separator = new Separator();
		separator.setPadding(new Insets(10, 0, 15, 0));
		mainPane.getChildren().add(separator);
		
		tableModel = new ArtifactTableModel(s);
		table = tableModel.createTable();
		table.addEventHandler(MouseEvent.MOUSE_CLICKED, this);

		// Create panes:
		tablePane = new BorderPane(table);
		tablePane.setCenter(table);
		mainPane.getChildren().add(tablePane);
		
		HBox buttonPane = new HBox();
		
		moreDetail = new Button("More Detail on Selected Artifact");
		moreDetail.addEventHandler(MouseEvent.MOUSE_CLICKED, this);
		buttonPane.getChildren().add(moreDetail);
		
		rules = new Button("Rules for Artifacts");
		rules.addEventHandler(MouseEvent.MOUSE_CLICKED, this);
		buttonPane.getChildren().add(rules);
		mainPane.getChildren().add(buttonPane);
		
		Scene scene = new Scene(mainPane, 900, 500);
		this.setScene(scene);
	}
	
	public void update() {
		mainPane.getChildren().remove(tablePane);
		tableModel = new ArtifactTableModel(state);
		table = tableModel.createTable();
		tablePane = new BorderPane(table);
		tablePane.setCenter(table);
		mainPane.getChildren().add(2, tablePane);
	}

	@Override
	public void handle(MouseEvent e) {
		Object source = e.getSource();
		if (source == moreDetail) {
			Artifact selected = (Artifact) table.getSelectionModel().getSelectedItem();
			if (selected == null) {
				Alert alert = new Alert(AlertType.WARNING, "Please select an artifact to get detailed information on.");
				alert.show();
			} else {
				ArtifactInfoScreen info = new ArtifactInfoScreen(state, gui, logic, selected);
				info.show();
			}
		} else if (source == rules) {
			RulesInfoScreen info = new RulesInfoScreen(state, RuleType.ARTIFACT);
			info.show();
		}
	}

}
