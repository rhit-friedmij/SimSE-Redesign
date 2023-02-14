package simse.gui;

import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import simse.gui.util.JavaFXHelpers;

public class TrackMessage extends Pane{
	
	String senderName;
	String message;
	
	public TrackMessage(String sender, String text) {
		this.senderName = sender;
		this.message = text;
		
		GridPane pane = new GridPane();
		
		ImageView img = JavaFXHelpers.createImageView("src/simse/gui/icons/" + senderName + ".gif");
		if (img == null) {
			img = JavaFXHelpers.createImageView("src/simse/gui/icons/alex.gif");
		}
		pane.add(img, 0, 0);
		pane.add(new Text(senderName), 1, 0);
		
		HBox textBox = new HBox();
		textBox.setBackground(JavaFXHelpers.createBackgroundColor(Color.rgb(255, 255, 255)));
		textBox.getChildren().add(new Text(message));
		pane.add(textBox, 1, 1);
		
		this.getChildren().add(pane);
	}

}
