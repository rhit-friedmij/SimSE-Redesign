/*
 * This class is responsible for generating all of the code for the logic's
 * NonEmployeeParticipantSelectionDialog component
 */

package simse.codegenerator.logicgenerator.dialoggenerator;

import simse.codegenerator.CodeGeneratorConstants;
import simse.codegenerator.CodeGeneratorUtils;

import simse.modelbuilder.actionbuilder.ActionType;
import simse.modelbuilder.actionbuilder.ActionTypeParticipant;
import simse.modelbuilder.actionbuilder.ActionTypeTrigger;
import simse.modelbuilder.actionbuilder.DefinedActionTypes;
import simse.modelbuilder.actionbuilder.UserActionTypeTrigger;
import simse.modelbuilder.objectbuilder.AttributeTypes;
import simse.modelbuilder.objectbuilder.DefinedObjectTypes;
import simse.modelbuilder.objectbuilder.SimSEObjectType;
import simse.modelbuilder.objectbuilder.SimSEObjectTypeTypes;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import javax.lang.model.element.Modifier;
import javax.swing.JOptionPane;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

public class NonEmployeeParticipantSelectionDialogGenerator implements
    CodeGeneratorConstants {
  private File directory; // directory to generate into
  private File psdFile; // file to generate
  private DefinedActionTypes actTypes; // holds all of the defined action types
  private DefinedObjectTypes objTypes; // holds all of the defined object types

  public NonEmployeeParticipantSelectionDialogGenerator(
      DefinedActionTypes actTypes, DefinedObjectTypes objTypes, 
      File directory) {
    this.directory = directory;
    this.actTypes = actTypes;
    this.objTypes = objTypes;
  }

  public void generate() {
	  ClassName eventHandler = ClassName.get("javafx.event", "EventHandler");
	  ClassName mouseEvent = ClassName.get("javafx.scene.input", "MouseEvent");
	  ClassName dialogClass = ClassName.get("javafx.scene.control", "Dialog");
	  ClassName actionClass = ClassName.get("simse.adts.actions", "Action");
	  ClassName vector = ClassName.get("java.util", "Vector");
	  ClassName stateClass = ClassName.get("simse.state", "State");
	  ClassName checkboxClass = ClassName.get("javafx.scene.control", "CheckBox");
	  ClassName ssObjectClass = ClassName.get("simse.adts.objects", "SSObject");
	  ClassName imageLoaderClass = ClassName.get("simse.gui", "ImageLoader");
	  ClassName tabPanelClass = ClassName.get("simse.gui", "TabPanel");
	  ClassName windowEvent = ClassName.get("javafx.stage", "WindowEvent");
	  TypeName actionDialog = ParameterizedTypeName.get(dialogClass, actionClass);
	  TypeName mouseHandler = ParameterizedTypeName.get(eventHandler, mouseEvent);
	  TypeName windowHandler = ParameterizedTypeName.get(eventHandler, windowEvent);
	  TypeName checkboxVector = ParameterizedTypeName.get(vector, checkboxClass);
	  TypeName ssObjectVector = ParameterizedTypeName.get(vector, ssObjectClass);
	  
	  Vector<SimSEObjectType> objs = objTypes.getAllObjectTypes();
      Vector<ActionType> acts = actTypes.getAllActionTypes();
      
      TypeSpec exitListener = TypeSpec.classBuilder("ExitListener")
    		  .addModifiers(Modifier.PUBLIC)
    		  .addSuperinterface(windowHandler)
    		  .addMethod(MethodSpec.methodBuilder("handle")
    				  .addAnnotation(Override.class)
    				  .addModifiers(Modifier.PUBLIC)
    				  .returns(void.class)
    				  .addParameter(WindowEvent.class, "evt")
    				  .beginControlFlow("if (!$N)", "dialogAccepted")
    				  .addStatement("$N = true", "actionCancelled")
    				  .endControlFlow()
    				  .addStatement("close()")
    				  .build())
    		  .build();
	  
	  MethodSpec nonEmployeeConstructor = MethodSpec.constructorBuilder()
			  .addModifiers(Modifier.PUBLIC)
			  .addParameter(Stage.class, "owner")
			  .addParameter(String.class, "pName")
			  .addParameter(ssObjectVector, "parts")
			  .addParameter(actionClass, "act")
			  .addParameter(stateClass, "s")
			  .addStatement("$N = pName", "partName")
			  .addStatement("$N = parts", "participants")
			  .addStatement("$N = act", "action")
			  .addStatement("$N = s", "state")
			  .addStatement("$N = false", "actionCancelled")
			  .addStatement("$N = false", "dialogAccepted")
			  .addStatement("setMinAndMax()")
			  .beginControlFlow("if (($N == 0) || ($N.size() > 1)) ", "minNumParts", "participants")
			  .addStatement("checkBoxes = new $T()", checkboxClass)
			  .addStatement("setTitle($S)", "Participant Selection")
			  .addStatement("$T mainPane = new $T()", VBox.class, VBox.class)
			  .addStatement("$T topPane = new $T()", VBox.class, VBox.class)
			  .addStatement("$T title = $S", String.class, "Choose ")
			  .addStatement("title = title.concat($N + $S)", "partName", " participant(s) (")
			  .beginControlFlow("if ($N == $N) ", "minNumParts", "maxNumParts")
			  .addStatement("title = title.concat($S + $N)", "exactly ", "minNumParts")
			  .nextControlFlow("else ")
			  .addStatement("title = title.concat($S + $N)", "at least ", "minNumParts")
			  .beginControlFlow("if ($N < 999999) // not boundle", "maxNumParts")
			  .addStatement("title = title.concat($S + $N)", ", at most ", "maxNumParts")
			  .endControlFlow()
			  .endControlFlow()
			  .addStatement("title = title.concat($S)", "):")
			  .addStatement("topPane.getChildren().add(new $T(title))", Label.class)
			  .addStatement("topPane.setMinWidth(400)")
			  .addStatement("$T middlePane = new $T()", VBox.class, VBox.class)
			  .beginControlFlow("for (int i = 0; i < participants.size(); i++) ")
			  .addStatement("$T tempObj = $N.elementAt(i)", ssObjectClass, "participants")
			  .addStatement("$T label = new $T()", String.class, String.class)
			  .addCode(generateNames(objs))
			  .addStatement("$T tempPane = new $T()", BorderPane.class, BorderPane.class)
			  .addStatement("$T tempCheckBox = new $T(label)", checkboxClass, checkboxClass)
			  .addStatement("tempPane.setLeft(tempCheckBox)")
			  .addStatement("$N.add(tempCheckBox)", "checkBoxes")
			  .addStatement("$T icon = $T.getImageFromURL($T.getImage(tempObj))",
					  ImageView.class, imageLoaderClass, tabPanelClass)
			  .addStatement("tempPane.setRight(new $T($S, icon))", Label.class, "")
			  .addStatement("middlePane.getChildren().add(tempPane)")
			  .endControlFlow()
			  .addStatement("$T checkPane = new $T()", HBox.class, HBox.class)
			  .addStatement("$N = new Button($S)", "checkAllButton", "Check All")
			  .addStatement("$N.addEventHandler($T.MOUSE_CLICKED, this)", "checkAllButton", mouseEvent)
			  .addStatement("$N.setMinWidth(75)", "checkAllButton")
			  .addStatement("checkPane.getChildren().add($N)", "checkAllButton")
			  .addStatement("$N = new Button($S)", "clearAllButton", "Clear All")
			  .addStatement("$N.addEventHandler($T.MOUSE_CLICKED, this)", "clearAllButton", mouseEvent)
			  .addStatement("$N.setMinWidth(75)", "clearAllButton")
			  .addStatement("checkPane.getChildren().add($N)", "clearAllButton")
			  .addStatement("$T bottomPane = new $T()", HBox.class, HBox.class)
			  .addStatement("$N = new Button($S)", "okButton", "OK")
			  .addStatement("$N.addEventHandler($T.MOUSE_CLICKED, this)", "okButton", mouseEvent)
			  .addStatement("$N.setMinWidth(75)", "okButton")
			  .addStatement("bottomPane.getChildren().add($N)", "okButton")
			  .addStatement("$N = new Button($S)", "cancelButton", "Cancel")
			  .addStatement("$N.addEventHandler($T.MOUSE_CLICKED, this)", "cancelButton", mouseEvent)
			  .addStatement("$N.setMinWidth(75)", "cancelButton")
			  .addStatement("bottomPane.getChildren().add($N)", "cancelButton")
			  .addStatement("mainPane.getChildren().addAll(topPane, middlePane)")
			  .addStatement("$T separator1 = new $T()", Separator.class, Separator.class)
			  .addStatement("separator1.setMaxSize(900, 5)")
			  .addStatement("mainPane.getChildren().addAll(separator1, checkPane)")
			  .addStatement("$T separator2 = new $T()", Separator.class, Separator.class)
			  .addStatement("separator2.setMaxSize(900, 5)")
			  .addStatement("mainPane.getChildren().addAll(separator2, bottomPane)")
			  .addStatement("this.getDialogPane().getChildren().add(mainPane)")
			  .addStatement("this.getDialogPane().setPrefSize(400, 400)")
			  .addStatement("this.getDialogPane().getScene().getWindow().setOnCloseRequest(new ExitListener())")
			  .addStatement("$T ownerLoc = new $T(owner.getX(), owner.getY())", Point2D.class, Point2D.class)
			  .addStatement("$T thisLoc = new $T((ownerLoc.getX() + (owner.getWidth() / 2) - "
			  		+ "(this.getWidth() / 2)), (ownerLoc.getY() + (owner.getHeight() / 2) - "
			  		+ "(this.getHeight() / 2)))", Point2D.class, Point2D.class)
			  .addStatement("this.setX(thisLoc.getX())")
			  .addStatement("this.setY(thisLoc.getY())")
			  .addStatement("showAndWait()")
			  .nextControlFlow(" else ")
			  .addCode(addToAction(acts))
			  .endControlFlow()
			  .build();
	  
	  MethodSpec closeDialog = MethodSpec.methodBuilder("closeDialog")
			  .addModifiers(Modifier.PRIVATE)
			  .returns(void.class)
			  .addParameter(boolean.class, "accepted")
			  .addStatement("$N = accepted", "dialogAccepted")
			  .addStatement("$T window = this.getDialogPane().getScene().getWindow()", Window.class)
			  .addStatement("window.fireEvent(new $T(window, $T.WINDOW_CLOSE_REQUEST))",
					  WindowEvent.class, WindowEvent.class)
			  .build();
	  
	  MethodSpec handle = MethodSpec.methodBuilder("handle")
			  .addModifiers(Modifier.PUBLIC)
			  .addAnnotation(Override.class)
			  .returns(void.class)
			  .addParameter(mouseEvent, "evt")
			  .addStatement("$T source = evt.getSource()", Object.class)
			  .beginControlFlow("if (source == $N) ", "cancelButton")
			  .addStatement("$N = true", "actionCancelled")
			  .addStatement("closeDialog(false)")
			  .nextControlFlow(" else if (source == $N) ", "okButton")
			  .addStatement("$T checkedBoxes = new $T()", checkboxVector, checkboxVector)
			  .beginControlFlow("for (int i = 0; i < $N.size(); i++) ", "checkBoxes")
			  .addStatement("$T tempCBox = $N.elementAt(i)", checkboxClass, "checkBoxes")
			  .beginControlFlow("if (tempCBox.isSelected()) ")
			  .addStatement("checkedBoxes.add(tempCBox)")
			  .endControlFlow()
			  .endControlFlow()
			  .beginControlFlow("if (checkedBoxes.size() < $N) ", "minNumParts")
			  .addStatement("$T alert = new $T($T.WARNING, $S)", Alert.class,
					  Alert.class, AlertType.class, "You must choose at least one action")
			  .addStatement("alert.setTitle($S)", "Invalid Input")
			  .addStatement("alert.setHeaderText(null)")
			  .addStatement("alert.show()")
			  .nextControlFlow(" else if (checkedBoxes.size() > $N) ", "maxNumParts")
			  .addStatement("$T alert = new $T($T.WARNING, $S + $N + $S)",
					  Alert.class, Alert.class, AlertType.class,
					  "You may only choose at most ", "maxNumParts", " participants")
			  .addStatement("alert.setTitle($S)", "Invalid Input")
			  .addStatement("alert.setHeaderText(null)")
			  .addStatement("alert.show()")
			  .nextControlFlow(" else ")
			  .beginControlFlow("for (int i = 0; i < checkedBoxes.size(); i++) ")
			  .addStatement("$T checkedBox = checkedBoxes.elementAt(i)", checkboxClass)
			  .addStatement("$T cBoxText = checkedBox.getText()", String.class)
			  .addStatement("$T objTypeName = cBoxText.substring(0, "
			  		+ "(cBoxText.indexOf('(') - 1))", String.class)
			  .addStatement("String keyValStr = cBoxText.substring(cBoxText."
			  		+ "indexOf('(') + 1), cBoxText.lastIndexOf(')'))")
			  .addStatement("addParticipant(objTypeName, keyValStr)")
			  .endControlFlow()
			  .addStatement("closeDialog(true)")
			  .endControlFlow()
			  .nextControlFlow(" else if (source == $N) ", "checkAllButton")
			  .beginControlFlow("for (int i = 0; i < $N.size(); i++) ", "checkBoxes")
			  .addStatement("$N.elementAt(i).setSelected(true)", "checkBoxes")
			  .endControlFlow()
			  .nextControlFlow(" else if (source == $N) ", "clearAllButton")
			  .beginControlFlow("for (int i = 0; i < $N.size(); i++) ", "checkBoxes")
			  .addStatement("$N.elementAt(i).setSelected(false)", "checkBoxes")
			  .endControlFlow()
			  .endControlFlow()
			  .build();
	  
	  MethodSpec addParticipant = MethodSpec.methodBuilder("addParticipant")
			  .addModifiers(Modifier.PRIVATE)
			  .returns(void.class)
			  .addParameter(String.class, "objTypeName")
			  .addParameter(String.class, "keyValStr")
			  .addCode(generateAddParticipant(objs, acts))
			  .build();
	  
	  MethodSpec setMinAndMax = MethodSpec.methodBuilder("setMinAndMax")
			  .addModifiers(Modifier.PRIVATE)
			  .returns(void.class)
			  .addCode(generateSetMinAndMax(acts))
			  .build();
	  
	  MethodSpec actionCancelled = MethodSpec.methodBuilder("actionCancelled")
			  .addModifiers(Modifier.PUBLIC)
			  .returns(boolean.class)
			  .addStatement("return $N", "actionCancelled")
			  .build();
	  
	  TypeSpec nonEmployeeDialog = TypeSpec.classBuilder("NonEmployeeParticipantSelectionDialog")
			  .superclass(actionDialog)
			  .addSuperinterface(mouseHandler)
			  .addType(exitListener)
			  .addField(String.class, "partName", Modifier.PRIVATE)
			  .addField(ssObjectVector, "participants", Modifier.PRIVATE)
			  .addField(actionClass, "action", Modifier.PRIVATE)
			  .addField(stateClass, "state", Modifier.PRIVATE)
			  .addField(int.class, "minNumParts", Modifier.PRIVATE)
			  .addField(int.class, "maxNumParts", Modifier.PRIVATE)
			  .addField(checkboxVector, "checkBoxes", Modifier.PRIVATE)
			  .addField(Button.class, "checkAllButton", Modifier.PRIVATE)
			  .addField(Button.class, "clearAllButton", Modifier.PRIVATE)
			  .addField(Button.class, "okButton", Modifier.PRIVATE)
			  .addField(Button.class, "cancelButton", Modifier.PRIVATE)
			  .addField(boolean.class, "actionCancelled", Modifier.PRIVATE)
			  .addField(boolean.class, "dialogAccepted", Modifier.PRIVATE)
			  .addMethod(nonEmployeeConstructor)
			  .addMethod(closeDialog)
			  .addMethod(handle)
			  .addMethod(addParticipant)
			  .addMethod(setMinAndMax)
			  .addMethod(actionCancelled)
			  .build();
	  

	  ClassName actions = ClassName.get("simse.adts", "actions");
	  JavaFile javaFile = JavaFile.builder("NonEmployeeParticipantSelectionDialog", nonEmployeeDialog)
			  .addStaticImport(actions, "*")  
			  .build();
	  
    try {
      psdFile = new File(directory,
          ("simse\\logic\\dialogs\\NonEmployeeParticipantSelectionDialog.java"));
      if (psdFile.exists()) {
        psdFile.delete(); // delete old version of file
      }
      
      javaFile.writeTo(psdFile);
      
    } catch (IOException e) {
        JOptionPane.showMessageDialog(null, ("Error writing file "
            + psdFile.getPath() + ": " + e.toString()), "File IO Error",
            JOptionPane.WARNING_MESSAGE);
    }
  }
  
  private String generateNames(Vector<SimSEObjectType> objs) {
	  String names = "";
	  for (int i = 0; i < objs.size(); i++) {
	        SimSEObjectType tempType = objs.elementAt(i);
	        if (i > 0) { // not on first element
	          names += "else ";
	        }
	        names +=  "if(tempObj instanceof "
	            + CodeGeneratorUtils.getUpperCaseLeading(tempType.getName()) + ")\n{\n";
	        names += "label = (\"" + CodeGeneratorUtils.getUpperCaseLeading(
	            		tempType.getName()) + " (\" + ((" + 
	            		CodeGeneratorUtils.getUpperCaseLeading(tempType.getName()) + 
	            		")tempObj).get" + CodeGeneratorUtils.getUpperCaseLeading(
	            				tempType.getKey().getName()) + "() + \")\");\n}\n";
	      }
	  
	  return names;
  }
  
  private String addToAction(Vector<ActionType> acts) {
	  String actions = "";
	  
	  boolean putElse = false;
      for (int j = 0; j < acts.size(); j++) {
        ActionType tempAct = acts.elementAt(j);
        if (putElse) { // not on first element
          actions += "else ";
        } else {
          putElse = true;
        }
        actions += "if(action instanceof "
            + CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName()) + 
            "Action)\n{\n";
        // go through all participants:
        Vector<ActionTypeParticipant> participants = 
        	tempAct.getAllParticipants();
        boolean nextOneWriteElse = false;
        for (int k = 0; k < participants.size(); k++) {
          ActionTypeParticipant tempPart = participants.elementAt(k);
          if (tempPart.getSimSEObjectTypeType() != 
          	SimSEObjectTypeTypes.EMPLOYEE) { // Non-Employee participant
            if (nextOneWriteElse) { // not on first element
              actions += "else ";
            }
            actions += "if(partName.equals(\"" + tempPart.getName() + "\"))\n{\n";
            actions += "(("
                + CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName())
                + "Action)action).add"
                + tempPart.getName()
                + "(("
                + CodeGeneratorUtils.getUpperCaseLeading(
                		SimSEObjectTypeTypes.getText(
                				tempPart.getSimSEObjectTypeType()))
                + ")participants.elementAt(0));\n}\n";
            nextOneWriteElse = true;
          }
        }
        actions += "}\n";
      }
      
      return actions;
  }
  
  private String generateAddParticipant(Vector<SimSEObjectType> objs, Vector<ActionType> acts) {
	  String parts = "";
	  
      for (int i = 0; i < objs.size(); i++) {
          SimSEObjectType tempType = objs.elementAt(i);
          if (i > 0) { // not on first element
            parts += "else ";
          }
          parts += "if(objTypeName.equals(\""
              + CodeGeneratorUtils.getUpperCaseLeading(tempType.getName()) + 
              "\"))\n{\n";
          if ((tempType.getKey().getType() == AttributeTypes.DOUBLE)
              || (tempType.getKey().getType() == AttributeTypes.INTEGER)) {
            parts += "try\n{\n";
          }
          parts += CodeGeneratorUtils.getUpperCaseLeading(
          		tempType.getName()) + " a = state.get" + 
          		SimSEObjectTypeTypes.getText(tempType.getType()) + 
          		"StateRepository().get" + CodeGeneratorUtils.getUpperCaseLeading(
          				tempType.getName()) + "StateRepository().get(";
          if (tempType.getKey().getType() == AttributeTypes.STRING) {
            parts += "keyValStr);";
          } else if (tempType.getKey().getType() == AttributeTypes.BOOLEAN) {
            parts += "(new Boolean(keyValStr)).booleanValue());";
          } else if (tempType.getKey().getType() == AttributeTypes.INTEGER) {
            parts += "(new Integer(keyValStr)).intValue());";
          } else if (tempType.getKey().getType() == AttributeTypes.DOUBLE) {
            parts += "(new Double(keyValStr)).doubleValue());";
          }
          parts += "\nif(a != null)\n{\n";
          // go through each action type:
          boolean putElse2 = false;
          for (int j = 0; j < acts.size(); j++) {
            ActionType tempAct = acts.elementAt(j);
            Vector<ActionTypeTrigger> trigs = tempAct.getAllTriggers();
            // only generate code for actions w/ user triggers:
            for (int k = 0; k < trigs.size(); k++) {
              ActionTypeTrigger tempTrig = trigs.elementAt(k);
              if (tempTrig instanceof UserActionTypeTrigger) {
                if (putElse2) // not on first element
                {
                  parts += "else ";
                } else {
                  putElse2 = true;
                }
                parts += "if(action instanceof "
                    + CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName()) + 
                    "Action)\n{\n";
                // go through all participants:
                Vector<ActionTypeParticipant> participants = 
                	tempAct.getAllParticipants();
                boolean nextOneWriteElse = false;
                for (int m = 0; m < participants.size(); m++) {
                  ActionTypeParticipant tempPart = participants.elementAt(m);
                  if (tempPart.getSimSEObjectType(tempType.getName()) != null) {
                  	// this SimSEObjectType is an allowable type for this 
                  	// participant
                    if (nextOneWriteElse) { // not on first element
                      parts += "else ";
                    }
                    parts += "if(partName.equals(\"" + tempPart.getName()
                        + "\"))\n{\n";
                    parts += "((" + CodeGeneratorUtils.getUpperCaseLeading(
                    		tempAct.getName()) + "Action)action).add" + 
                    		tempPart.getName() + "((" + SimSEObjectTypeTypes.getText(
                    				tempType.getType()) + ")a);\n}\n";
                    nextOneWriteElse = true;
                  }
                }
                parts += "}\n";
              }
            }
          }
          parts += "}\n";
          if ((tempType.getKey().getType() == AttributeTypes.INTEGER)
              || (tempType.getKey().getType() == AttributeTypes.DOUBLE)) {
            parts += "}\ncatch(NumberFormatException e)\n{\n";
            parts += "System.out.println(e);\n}\n";
          }
          parts += "}\n";
        }
      return parts;
  }
  
  private String generateSetMinAndMax(Vector<ActionType> acts) {
	  String parts = "";
	  
	  for (int i = 0; i < acts.size(); i++) {
	        ActionType tempAct = acts.elementAt(i);
	        if (i > 0) { // not on first element
	          parts += "else ";
	        }
	        parts += "if(action instanceof "
	            + CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName()) + 
	            "Action)\n{\n";
	        // go through each participant:
	        Vector<ActionTypeParticipant> participants = 
	        	tempAct.getAllParticipants();
	        for (int j = 0; j < participants.size(); j++) {
	          if (j > 0) { // not on first element
	            parts += "else ";
	          }
	          ActionTypeParticipant tempPart = participants.elementAt(j);
	          parts += "if(partName.equals(\"" + tempPart.getName() + "\"))\n{\n";
	          parts += "minNumParts = ";
	          if (tempPart.getQuantity().isMinValBoundless()) {
	            parts += "0;";
	          } else { // min val has a value
	            parts += tempPart.getQuantity().getMinVal().toString() + ";";
	          }
	          parts += "\nmaxNumParts = ";
	          if (tempPart.getQuantity().isMaxValBoundless()) {
	            parts += "999999;";
	          } else { // max val has a value
	            parts += tempPart.getQuantity().getMaxVal().toString() + ";";
	          }
	          parts += "\n}\n";
	        }
	        parts += "}\n";
	      }
	  return parts;
  }
}