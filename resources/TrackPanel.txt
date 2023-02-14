package simse.gui;

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import simse.gui.util.JavaFXHelpers;
import simse.state.State;

public class TrackPanel extends Pane implements SimSEPanel {
	
	VBox track;
	ScrollPane scroll;
	static TrackPanel instance = null;
	
	private TrackPanel() {
		
		VBox pane = new VBox();
		HBox top = new HBox();
		top.getChildren().add(JavaFXHelpers.createImageView("src/simse/gui/icons/track.png"));
		top.getChildren().add(new Text("Track"));
		top.setMaxHeight(50);
		top.setPrefWidth(500);
		top.setBackground(JavaFXHelpers.createBackgroundColor(Color.rgb(128, 128, 128)));
		
		scroll = new ScrollPane();
		track = new VBox();
		track.heightProperty().addListener(observable -> scroll.setVvalue(1D));
		track.setBackground(JavaFXHelpers.createBackgroundColor(Color.rgb(217, 217, 217)));
		scroll.setBackground(JavaFXHelpers.createBackgroundColor(Color.rgb(217, 217, 217)));
		scroll.setContent(track);
		scroll.setMaxHeight(100);
		scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
		scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		scroll.setVvalue(1.0);
		
		pane.getChildren().add(top);
		pane.getChildren().add(scroll);
		
		

		this.getChildren().add(pane);
	}
	
	public static TrackPanel getInstance() {
		if (instance == null) {
			instance = new TrackPanel();
		}
		return instance;
	}
	
	public void addText(String message, String name) {
		track.getChildren().add(new TrackMessage(name, message));
	}

	@Override
	public Panels getPanelType() {
		return Panels.TRACK;
	}

}
