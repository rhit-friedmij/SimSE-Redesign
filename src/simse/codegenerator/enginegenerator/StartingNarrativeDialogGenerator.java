/*
 * This class is responsible for generating all of the code for the
 * StartingNarrativeDialog in the engine component of the simulation
 */

package simse.codegenerator.enginegenerator;

import simse.modelbuilder.startstatebuilder.CreatedObjects;

import java.awt.Dialog;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.lang.model.element.Modifier;
import javax.swing.JOptionPane;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import simse.codegenerator.*;

public class StartingNarrativeDialogGenerator 
implements CodeGeneratorConstants {
  private File directory; // directory to generate into
  private File snFile; // file to generate
  private CreatedObjects createdObjs; // start state objects

  public StartingNarrativeDialogGenerator(CreatedObjects createdObjs, 
  		File directory) {
    this.directory = directory;
    this.createdObjs = createdObjs;
  }

  // causes the class to be generated
  public void generate() {
	  ClassName eventhandler = ClassName.get("javafx.event", "EventHandler");
	  ClassName pos = ClassName.get("javafx.geometry", "Pos");
	  ClassName rectangle2d = ClassName.get("javafx.geometry", "Rectangle2D");
	  ClassName scene = ClassName.get("javafx.scene", "Scene");
	  ClassName button = ClassName.get("javafx.scene.control", "Button");
	  ClassName scrollpane = ClassName.get("javafx.scene.control", "ScrollPane");
	  ClassName textarea = ClassName.get("javafx.scene.control", "TextArea");
	  ClassName mouseevent = ClassName.get("javafx.scene.input", "MouseEvent");
	  ClassName background = ClassName.get("javafx.scene.layout", "Background");
	  ClassName backgroundfill = ClassName.get("javafx.scene.layout", "BackgroundFill");
	  ClassName borderpane = ClassName.get("javafx.scene.layout", "BorderPane");
	  ClassName color = ClassName.get("javafx.scene.paint", "Color");
	  ClassName modality = ClassName.get("javafx.stage", "Modality");
	  ClassName screen = ClassName.get("javafx.stage", "Screen");
	  ClassName stage = ClassName.get("javafx.stage", "Stage");
	  TypeName mouseHandler = ParameterizedTypeName.get(eventhandler, mouseevent);
	  
	  char[] startNarrChars = createdObjs.getStartingNarrative().
		      	replaceAll("\n", "\\\\n").replaceAll("\"", "\\\\\"").toCharArray();
	  String dialogText = "";
      for (int i = 0; i < startNarrChars.length; i++) {
        if (startNarrChars[i] == '\n') {
          dialogText += "\" + \'\\n\' + \"";
        } else {
        	dialogText += startNarrChars[i];
        }
      }
	  
	  MethodSpec narrativeConstructor = MethodSpec.constructorBuilder()
			  .addStatement("this.initModality($T.APPLICATION_MODAL)", modality)
			  .addStatement("this.setTitle($S)", "Welcome!")
			  .addCode("$L", "\n")
			  .addStatement("textArea = new TextArea()")
			  .addStatement("textArea.setWrapText(true)")
			  .addStatement("textArea.setEditable(false)")
			  .addStatement("textArea.setText($S)", dialogText)
			  .addStatement("$T scrollPane = new ScrollPane()", scrollpane)
			  .addStatement("scrollPane.hbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.NEVER)")
			  .addStatement("scrollPane.setContent(textArea)")
			  .addCode("$L", "\n")
			  .addComment("Create okButton pane and button")
			  .addStatement("okButton = new Button($S)", "OK")
			  .addStatement("okButton.addEventHandler($T.MOUSE_CLICKED, this)", mouseevent)
			  .addCode("$L", "\n")
			  .addStatement("this.root = new BorderPane()")
			  .addStatement("root.setBackground(new $T(new $T($T.WHITE, null, null)))", background, backgroundfill, color)
			  .addStatement("root.setCenter(scrollPane)")
			  .addStatement("root.setBottom(okButton)")
			  .addStatement("$T.setAlignment(okButton, $T.CENTER)", borderpane, pos)
			  .addCode("$L", "\n")
			  .addStatement("$T scene = new Scene(root)", scene)
			  .addStatement("this.setScene(scene)")
			  .addStatement("this.show()")
			  .addCode("$L", "\n")
			  .addComment("Set main window frame properties")
			  .addStatement("$T primScreenBounds = $T.getPrimary().getVisualBounds()", rectangle2d, screen)
			  .addStatement("this.setX((primScreenBounds.getWidth() - this.getWidth()) / 2)")
			  .addStatement("this.setY((primScreenBounds.getHeight() - this.getHeight()) / 2)")
			  .addStatement("this.toFront()")
			  .build();
	  
	  MethodSpec handle = MethodSpec.methodBuilder("handle")
			  .addModifiers(Modifier.PUBLIC)
			  .returns(void.class)
			  .addParameter(mouseevent, "event")
			  .addAnnotation(Override.class)
			  .addStatement("$T stage = ($T) root.getScene().getWindow()", stage, stage)
			  .addStatement("stage.close()")
			  .build();
	  
	  TypeSpec narrativeDialog = TypeSpec.classBuilder("StartingNarrativeDialog")
			  	.superclass(stage)
	  			.addModifiers(Modifier.PUBLIC)
	  			.addSuperinterface(mouseHandler)
	  			.addField(textarea, "textArea", Modifier.PRIVATE)
	  			.addField(button, "okButton", Modifier.PRIVATE)
	  			.addField(button, "cancelButton", Modifier.PRIVATE)
	  			.addMethod(narrativeConstructor)
	  			.addMethod(handle)
	  			.build();
	  
	  JavaFile javaFile = JavaFile.builder("simse.engine", narrativeDialog)
			  .addFileComment("File generated by: simse.codegenerator.enginegenerator.StartingNarrativeDialogGenerator", "")
			  .build();
	  
	  try {
		  snFile = new File(directory, ("simse\\engine\\StartingNarrativeDialog.java"));
		  if (snFile.exists()) {
			  snFile.delete(); // delete old version of file
		  }
		  System.out.println(javaFile.toString());
		  javaFile.writeTo(snFile);
	      
//	      char[] startNarrChars = createdObjs.getStartingNarrative().
//	      	replaceAll("\n", "\\\\n").replaceAll("\"", "\\\\\"").toCharArray();
//	      for (int i = 0; i < startNarrChars.length; i++) {
//	        if (startNarrChars[i] == '\n') {
//	          writer.write("\" + \'\\n\' + \"");
//	        } else {
//	          writer.write(startNarrChars[i]);
//	        }
//	      }
	      
	  } catch (IOException e) {
	      JOptionPane.showMessageDialog(null, ("Error writing file "
	          + snFile.getPath() + ": " + e.toString()), "File IO Error",
	          JOptionPane.WARNING_MESSAGE);
	  }
  	}
}