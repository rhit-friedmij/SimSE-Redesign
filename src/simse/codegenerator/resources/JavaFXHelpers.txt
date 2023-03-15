package simse.gui.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;

public final class JavaFXHelpers {

	public static Background createBackgroundColor(Color color) {
		return new Background(new BackgroundFill(color, null, null));
	}
	
	public static ImageView createImageView(String path) {
		ImageView img = null;
		try {
			img = new ImageView(new Image(new FileInputStream(path)));
		} catch (FileNotFoundException e) {
			System.out.println("Image not found at " + path);
		}
		return img;
	}
	
	public static Image createImage(String path) {
		Image img = null;
		try {
			img = new Image(new FileInputStream(path));
		} catch (FileNotFoundException e) {
			System.out.println("Image not found at " + path);
		}
		return img;
	}
	
	public static void gPaneConstraints(Node child, int columnIndex, int rowIndex, int columnspan, 
			int rowspan, HPos hAlignment, VPos vAlignment, Priority hGrow, Priority vGrow, Insets margin) {
		GridPane.setConstraints(child, columnIndex, rowIndex, columnspan, rowspan, hAlignment, vAlignment, 
				hGrow, vGrow, margin);
	}
}
