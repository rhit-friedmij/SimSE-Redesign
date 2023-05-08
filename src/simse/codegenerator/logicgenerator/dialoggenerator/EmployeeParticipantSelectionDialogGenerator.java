/*
 * This class is responsible for generating all of the code for the logic's
 * EmployeeParticipantSelectionDialog component
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

import java.awt.Checkbox;
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
import com.squareup.javapoet.WildcardTypeName;

public class EmployeeParticipantSelectionDialogGenerator implements
    CodeGeneratorConstants {
  private File directory; // directory to generate into
  private File psdFile; // file to generate
  private DefinedActionTypes actTypes; // holds all of the defined action types
  private DefinedObjectTypes objTypes; // holds all of the defined object types

  public EmployeeParticipantSelectionDialogGenerator(DefinedActionTypes 
  		actTypes, DefinedObjectTypes objTypes, File directory) {
    this.directory = directory;
    this.actTypes = actTypes;
    this.objTypes = objTypes;
  }

  public void generate() {      
	  ClassName eventHandler = ClassName.get("javafx.event", "EventHandler");
	  ClassName mouseEvent = ClassName.get("javafx.scene.input", "MouseEvent");
	  ClassName vector = ClassName.get("java.util", "Vector");
	  ClassName stateClass = ClassName.get("simse.state", "State");
	  ClassName ruleExecClass = ClassName.get("simse.logic", "RuleExecutor");
	  ClassName employeeClass = ClassName.get("simse.adts.objects", "Employee");
	  ClassName actionClass = ClassName.get("simse.adts.actions", "Action");
	  ClassName checkboxClass = ClassName.get("javafx.scene.control", "CheckBox");
	  ClassName buttonClass = ClassName.get("javafx.scene.control", "Button");
	  ClassName dialogClass = ClassName.get("javafx.scene.control", "Dialog");
	  ClassName ssObjectClass = ClassName.get("simse.adts.objects", "SSObject");
	  ClassName imageLoaderClass = ClassName.get("simse.gui", "ImageLoader");
	  ClassName tabPanelClass = ClassName.get("simse.gui", "TabPanel");
	  ClassName windowEvent = ClassName.get("javafx.stage", "WindowEvent");
	  ClassName stageClass = ClassName.get("javafx.stage", "Stage");
	  ClassName vBoxClass = ClassName.get("javafx.scene.layout", "VBox");
	  ClassName hBoxClass = ClassName.get("javafx.scene.layout", "HBox");
	  ClassName labelClass = ClassName.get("javafx.scene.control", "Label");
	  ClassName separatorClass = ClassName.get("javafx.scene.control", "Separator");
	  ClassName borderPaneClass = ClassName.get("javafx.scene.layout", "BorderPane");
	  ClassName imageViewClass = ClassName.get("javafx.scene.image", "ImageView");
	  ClassName point2DClass = ClassName.get("javafx.geometry", "Point2D");
	  ClassName alertClass = ClassName.get("javafx.scene.control", "Alert");
	  ClassName alertTypeClass = ClassName.get("javafx.scene.control.Alert", "AlertType");
	  ClassName windowClass = ClassName.get("javafx.stage", "Window");
	  ClassName employee = ClassName.get("simse.adts.objects", "Employee");
	  ClassName softwareEngineer = ClassName.get("simse.adts.objects", "SoftwareEngineer");
	  ClassName imageView = ClassName.get("javafx.scene.image", "ImageView");
	  TypeName mouseHandler = ParameterizedTypeName.get(eventHandler, mouseEvent);
	  TypeName windowHandler = ParameterizedTypeName.get(eventHandler, windowEvent);
	  TypeName dialogAction = ParameterizedTypeName.get(dialogClass, actionClass);
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
    				  .addParameter(windowEvent, "evt")
    				  .beginControlFlow("if (!$N)", "dialogAccepted")
    				  .addStatement("$N = true", "actionCancelled")
    				  .endControlFlow()
    				  .addStatement("close()")
    				  .build())
    		  .build();
	  
	  MethodSpec employeeConstructor = MethodSpec.constructorBuilder()
			  .addModifiers(Modifier.PUBLIC)
			  .addParameter(stageClass, "owner")
			  .addParameter(String.class, "pName")
			  .addParameter(ssObjectVector, "parts")
			  .addParameter(actionClass, "act")
			  .addParameter(stateClass, "s")
			  .addParameter(employeeClass, "emp")
			  .addStatement("$N = pName", "partName")
			  .addStatement("$N = parts", "participants")
			  .addStatement("$N = act", "action")
			  .addStatement("$N = s", "state")
			  .addStatement("$N = emp", "selectedEmp")
			  .addStatement("$N = false", "actionCancelled")
			  .addStatement("$N = false", "dialogAccepted")
			  .addStatement("setMinAndMax()")
			  .beginControlFlow("if ((($N != null) && ($N > 0) && "
			  		+ "($N.size() > 0)) || (($N == null) && "
			  		+ "($N.size() > $N))) ", "selectedEmp", "maxNumParts",
			  		"participants", "selectedEmp", "participants", "minNumParts")
			  .addStatement("$N = new $T()", "checkBoxes", checkboxVector)
			  .addStatement("setTitle($S)", "Participant Selection")
			  .addStatement("$T mainPane = new $T()", vBoxClass, vBoxClass)
			  .addStatement("$T topPane = new $T()", vBoxClass, vBoxClass)
			  .addStatement("$T title = $S", String.class, "Choose ")
			  .beginControlFlow("if ($N != null) ", "selectedEmp")
			  .addStatement("title = title.concat($S)", "other ")
			  .endControlFlow()
			  .addStatement("title = title.concat($N + $S)", "partName", " participant(s) (")
			  .beginControlFlow("if ($N == $N) ", "minNumParts", "maxNumParts")
			  .addStatement("title = title.concat($S + $N)", "exactly ", "minNumParts")
			  .nextControlFlow("else ")
			  .addStatement("title = title.concat($S + $N)", "at least ", "minNumParts")
			  .beginControlFlow("if ($N < 999999)", "maxNumParts")
			  .addStatement("title = title.concat($S + $N)", ", at most ", "maxNumParts")
			  .endControlFlow()
			  .endControlFlow()
			  .addStatement("title = title.concat($S)", "):")
			  .addStatement("topPane.getChildren().add(new $T(title))", labelClass)
			  .addStatement("topPane.setMinWidth(400)")
			  .addStatement("$T middlePane = new $T()", vBoxClass, vBoxClass)
			  .beginControlFlow("for (int i = 0; i < $N.size(); i++) ", "participants")
			  .addStatement("$T tempObj = $N.elementAt(i)", ssObjectClass, "participants")
			  .addStatement("$T label = new $T()", String.class, String.class)
			  .addCode(generateNames(objs))
			  .addStatement("$T tempPane = new $T()", borderPaneClass, borderPaneClass)
			  .addStatement("$T tempCheckBox = new $T(label)", checkboxClass, checkboxClass)
			  .addStatement("tempPane.setLeft(tempCheckBox)")
			  .addStatement("$N.add(tempCheckBox)", "checkBoxes")
			  .addStatement("$T<$T> allEmp = state.getEmployeeStateRepository().getAll()", vector, employee)
			  .beginControlFlow("for(int k = 0; k < allEmp.size(); k++)")
			  .beginControlFlow("if(allEmp.get(k).getKeyAsString() != null)")
			  .beginControlFlow("if(allEmp.get(k).getKeyAsString().equals(((Employee) tempObj).getKeyAsString()))")
			  .addStatement("$T icon = allEmp.get(k).getCharacterModel().getDisplayedCharacter(true)", imageView)
			  .addStatement("icon.setScaleX(1.5)")
			  .addStatement("icon.setScaleY(1.5)")
			  .addStatement("tempPane.setRight(new $T($S, icon))", labelClass, "")
			  .addStatement("middlePane.getChildren().add(tempPane)")
			  .endControlFlow()
			  .endControlFlow()
			  .endControlFlow()
			  .endControlFlow()
			  .addStatement("$T checkPane = new $T()", hBoxClass, hBoxClass)
			  .addStatement("$N = new $T($S)", "checkAllButton", buttonClass, "Check All")
			  .addStatement("$N.addEventHandler($T.MOUSE_CLICKED, this)", "checkAllButton", mouseEvent)
			  .addStatement("$N.setMinWidth(75)", "checkAllButton")
			  .addStatement("checkPane.getChildren().add($N)", "checkAllButton")
			  .addStatement("$N = new $T($S)", "clearAllButton", buttonClass, "Clear All")
			  .addStatement("$N.addEventHandler($T.MOUSE_CLICKED, this)", "clearAllButton", mouseEvent)
			  .addStatement("$N.setMinWidth(75)", "clearAllButton")
			  .addStatement("checkPane.getChildren().add($N)", "clearAllButton")
			  .addStatement("$T bottomPane = new $T()", hBoxClass, hBoxClass)
			  .addStatement("$N = new $T($S)", "okButton", buttonClass, "OK")
			  .addStatement("$N.addEventHandler($T.MOUSE_CLICKED, this)", "okButton", mouseEvent)
			  .addStatement("$N.setMinWidth(75)", "okButton")
			  .addStatement("bottomPane.getChildren().add($N)", "okButton")
			  .addStatement("$N = new $T($S)", "cancelButton", buttonClass, "Cancel")
			  .addStatement("$N.addEventHandler($T.MOUSE_CLICKED, this)", "cancelButton", mouseEvent)
			  .addStatement("$N.setMinWidth(75)", "cancelButton")
			  .addStatement("bottomPane.getChildren().add($N)", "cancelButton")
			  .addStatement("mainPane.getChildren().addAll(topPane, middlePane)")
			  .addStatement("$T separator1 = new $T()", separatorClass, separatorClass)
			  .addStatement("separator1.setMaxSize(900, 5)")
			  .addStatement("mainPane.getChildren().addAll(separator1, checkPane)")
			  .addStatement("$T separator2 = new $T()", separatorClass, separatorClass)
			  .addStatement("separator2.setMaxSize(900, 5)")
			  .addStatement("mainPane.getChildren().addAll(separator2, bottomPane)")
			  .addStatement("this.getDialogPane().getChildren().add(mainPane)")
			  .addStatement("this.getDialogPane().setPrefSize(400, middlePane.getChildren().size() * 30 + 100)")
			  .addStatement("this.getDialogPane().getScene().getWindow().setOnCloseRequest(new ExitListener())")
			  .addStatement("$T ownerLoc = new $T(owner.getX(), owner.getY())", point2DClass, point2DClass)
			  .addStatement("$T thisLoc = new $T((ownerLoc.getX() + (owner.getWidth() / 2) - (this.getWidth() / 2)),\n "
			  		+ "(ownerLoc.getY() + (owner.getHeight() / 2) - (this.getHeight() / 2)))", point2DClass, point2DClass)
			  .addStatement("this.setX(thisLoc.getX())")
			  .addStatement("this.setY(thisLoc.getY())")
			  .addStatement("showAndWait()")
			  .nextControlFlow("else if (($N == null) && ($N.size() == $N)) ",
					  "selectedEmp", "participants", "minNumParts")
			  .beginControlFlow("for (int i = 0; i < $N.size(); i++) ", "participants")
			  .addStatement("$T tempEmp = ($T) $N.elementAt(i)", employeeClass, employeeClass, "participants")
			  .addCode(addToAction(acts))
			  .endControlFlow()
			  .endControlFlow()
			  .beginControlFlow("if($N != null)", "selectedEmp")
			  .addCode(addToAction2(acts))
			  .endControlFlow()
			  .build();
	  
	  MethodSpec closeDialog = MethodSpec.methodBuilder("closeDialog")
			  .addModifiers(Modifier.PRIVATE)
			  .returns(void.class)
			  .addParameter(boolean.class, "accepted")
			  .addStatement("$N = accepted", "dialogAccepted")
			  .addStatement("$T window = this.getDialogPane().getScene().getWindow()", windowClass)
			  .addStatement("window.fireEvent(new $T(window, $T.WINDOW_CLOSE_REQUEST))",
					  windowEvent, windowEvent)
			  .build();
	  
	  MethodSpec handle = MethodSpec.methodBuilder("handle")
			  .addModifiers(Modifier.PUBLIC)
			  .returns(void.class)
			  .addAnnotation(Override.class)
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
			  .addStatement("checkedBoxes.add(tempCBox);")
			  .endControlFlow()
			  .endControlFlow()
			  .beginControlFlow("if (checkedBoxes.size() < $N) ", "minNumParts")
			  .addStatement("$T alert = new $T($T.WARNING, $S)", alertClass, alertClass,
					  alertTypeClass, "You must choose at least one action")
			  .addStatement("alert.setTitle($S)", "Invalid Input")
			  .addStatement("alert.setHeaderText(null)")
			  .addStatement("alert.showAndWait()")
			  .nextControlFlow(" else if (checkedBoxes.size() > $N) ", "maxNumParts")
			  .addStatement("$T alert = new $T($T.WARNING, $S + $N + $S)", alertClass,
					  alertClass, alertTypeClass, "You may only choose at most ",
					  "maxNumParts", " participants")
			  .addStatement("alert.setTitle($S)", "Invalid Input")
			  .addStatement("alert.setHeaderText(null)")
			  .addStatement("alert.showAndWait()")
			  .nextControlFlow(" else ")
			  .beginControlFlow("for (int i = 0; i < checkedBoxes.size(); i++) ")
			  .addStatement("$T checkedBox = checkedBoxes.elementAt(i)", checkboxClass)
			  .addStatement("$T cBoxText = checkedBox.getText()", String.class)
			  .addStatement("$T objTypeName = cBoxText.substring(0, (cBoxText"
			  		+ ".indexOf('(') - 1))", String.class)
			  .addStatement("$T keyValStr = cBoxText.substring((cBoxText"
			  		+ ".indexOf('(') + 1), cBoxText.lastIndexOf(')'))", String.class)
			  .addStatement("addParticipant(objTypeName, keyValStr);", String.class)
			  .endControlFlow()
			  .addStatement("closeDialog(true)")
			  .endControlFlow()
			  .nextControlFlow(" else if (source == $N) ", "checkAllButton")
			  .beginControlFlow("for (int i = 0; i < $N.size(); i++) ", "checkBoxes")
			  .addStatement("$N.elementAt(i).setSelected(true);", "checkBoxes")
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
	  
	  TypeSpec employeeDialog = TypeSpec.classBuilder("EmployeeParticipantSelectionDialog")
			  .addModifiers(Modifier.PUBLIC)
	  			.superclass(dialogAction)
	  			.addSuperinterface(mouseHandler)
	  			.addType(exitListener)
	  			.addField(String.class, "partName", Modifier.PRIVATE)
	  			.addField(ssObjectVector, "participants", Modifier.PRIVATE)
	  			.addField(actionClass, "action", Modifier.PRIVATE)
	  			.addField(stateClass, "state", Modifier.PRIVATE)
	  			.addField(employeeClass, "selectedEmp", Modifier.PRIVATE)
	  			.addField(int.class, "minNumParts", Modifier.PRIVATE)
	  			.addField(int.class, "maxNumParts", Modifier.PRIVATE)
	  			.addField(checkboxVector, "checkBoxes", Modifier.PRIVATE)
	  			.addField(buttonClass, "checkAllButton", Modifier.PRIVATE)
	  			.addField(buttonClass, "clearAllButton", Modifier.PRIVATE)
	  			.addField(buttonClass, "okButton", Modifier.PRIVATE)
	  			.addField(buttonClass, "cancelButton", Modifier.PRIVATE)
	  			.addField(boolean.class, "actionCancelled", Modifier.PRIVATE)
	  			.addField(boolean.class, "dialogAccepted", Modifier.PRIVATE)
	  			.addMethod(employeeConstructor)
	  			.addMethod(closeDialog)
	  			.addMethod(handle)
	  			.addMethod(addParticipant)
	  			.addMethod(setMinAndMax)
	  			.addMethod(actionCancelled)
	  			.build();
		  
	  JavaFile javaFile = JavaFile.builder("", employeeDialog)   
			  .build();
	  
    try {
      psdFile = new File(directory,
          ("simse\\logic\\dialogs\\EmployeeParticipantSelectionDialog.java"));
      if (psdFile.exists()) {
        psdFile.delete(); // delete old version of file
      }
      
      FileWriter writer = new FileWriter(psdFile);
	  String toAppend = "/* File generated by: simse.codegenerator.logicgenerator.dialoggenerator.EmployeeParticipantSelectionDialogGenerator */\n"
	  		+ "package simse.logic.dialogs;\n"
		  		+ "\n"
		  		+ "import simse.adts.actions.*;\n"
		  		+ "import simse.adts.objects.*;\n";
		  
	  writer.write(toAppend + javaFile.toString());
      writer.close();
      
    } catch (IOException e) {
        JOptionPane.showMessageDialog(null, ("Error writing file "
            + psdFile.getPath() + ": " + e.toString()), "File IO Error",
            JOptionPane.WARNING_MESSAGE);
    }
      
      /* I think I was trying to do some fancy thing here for convenience, but
       * it was causing problems by automatically adding the participant even if
       * the user didn't check it (if there was only one participant to choose from):
       */
//            writer.write("if(participants.size() == 1)");
//            writer.write(NEWLINE);
//            writer.write(OPEN_BRACK);
//            writer.write(NEWLINE);
//            writer.write("Employee e = (Employee)participants.elementAt(0);");
//            writer.write(NEWLINE);
//            boolean putElse2 = false;
//            for (int j = 0; j < acts.size(); j++) {
//              ActionType tempAct = (ActionType) acts.elementAt(j);
//              Vector trigs = tempAct.getAllTriggers();
//              // only generate code for actions w/ user triggers:
//              for (int k = 0; k < trigs.size(); k++) {
//                ActionTypeTrigger tempTrig = (ActionTypeTrigger) trigs.elementAt(k);
//                if (tempTrig instanceof UserActionTypeTrigger) {
//                  if (putElse2) // not on first element
//                  {
//                    writer.write("else ");
//                  } else {
//                    putElse2 = true;
//                  }
//                  writer.write("if(action instanceof "
//                      + getUpperCaseLeading(tempAct.getName()) + "Action)");
//                  writer.write(NEWLINE);
//                  writer.write(OPEN_BRACK);
//                  writer.write(NEWLINE);
//                  // go through all participants:
//                  Vector participants = tempAct.getAllParticipants();
//                  boolean nextOneWriteElse = false;
//                  for (int m = 0; m < participants.size(); m++) {
//                    ActionTypeParticipant tempPart = (ActionTypeParticipant) participants
//                        .elementAt(m);
//                    if (tempPart.getSimSEObjectTypeType() == SimSEObjectTypeTypes.EMPLOYEE) // Employee
//                                                                                            // participant
//                    {
//                      if (nextOneWriteElse) // not on first element
//                      {
//                        writer.write("else ");
//                      }
//                      writer.write("if(partName.equals(\"" + tempPart.getName()
//                          + "\"))");
//                      writer.write(NEWLINE);
//                      writer.write(OPEN_BRACK);
//                      writer.write(NEWLINE);
//                      writer.write("((" + getUpperCaseLeading(tempAct.getName())
//                          + "Action)action).add" + tempPart.getName() + "(e);");
//                      writer.write(NEWLINE);
//                      writer.write(CLOSED_BRACK);
//                      writer.write(NEWLINE);
//                      nextOneWriteElse = true;
//                    }
//                  }
//                  writer.write(CLOSED_BRACK);
//                  writer.write(NEWLINE);
//                  break;
//                }
//              }
//            }
//            writer.write(CLOSED_BRACK);
//            writer.write(NEWLINE);
  }
  
  private String generateNames(Vector<SimSEObjectType> objs) {
	  String names = "";
	  
      for (int i = 0; i < objs.size(); i++) {
          SimSEObjectType tempType = objs.elementAt(i);
          if (i > 0) { // not on first element
            names += "else ";
          }
          names += "if(tempObj instanceof "
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
	  String actionEmps = "";
	  boolean putElse = false;
      for (int j = 0; j < acts.size(); j++) {
        ActionType tempAct = acts.elementAt(j);
        Vector<ActionTypeTrigger> trigs = tempAct.getAllTriggers();
        // only generate code for actions w/ user triggers:
        for (int k = 0; k < trigs.size(); k++) {
          ActionTypeTrigger tempTrig = trigs.elementAt(k);
          if (tempTrig instanceof UserActionTypeTrigger) {
            if (putElse) { // not on first element
              actionEmps += "else ";
            } else {
              putElse = true;
            }
            actionEmps += "if(action instanceof "
                + CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName()) + 
                "Action)\n{\n";
            // go through all participants:
            Vector<ActionTypeParticipant> participants = 
            	tempAct.getAllParticipants();
            boolean nextOneWriteElse = false;
            for (int m = 0; m < participants.size(); m++) {
              ActionTypeParticipant tempPart = participants.elementAt(m);
              if (tempPart.getSimSEObjectTypeType() == 
              	SimSEObjectTypeTypes.EMPLOYEE) { // Employee participant
                if (nextOneWriteElse) { // not on first element
                  actionEmps += "else ";
                }
                actionEmps += "if(partName.equals(\"" + tempPart.getName()
                    + "\"))\n{\n";
                actionEmps += "((" + CodeGeneratorUtils.getUpperCaseLeading(
                		tempAct.getName()) + "Action)action).add" + 
                		tempPart.getName() + "(tempEmp);\n}\n";
                nextOneWriteElse = true;
              }
            }
            actionEmps += "}\n";
          }
        }
      }
      
      return actionEmps;
  }
  
  private String addToAction2(Vector<ActionType> acts) {
	  String actionEmps = "";
	  boolean putElse = false;
      for (int j = 0; j < acts.size(); j++) {
        ActionType tempAct = acts.elementAt(j);
        Vector<ActionTypeTrigger> trigs = tempAct.getAllTriggers();
        // only generate code for actions w/ user triggers:
        for (int k = 0; k < trigs.size(); k++) {
          ActionTypeTrigger tempTrig = trigs.elementAt(k);
          if (tempTrig instanceof UserActionTypeTrigger) {
            if (putElse) { // not on first element
              actionEmps += "else ";
            } else {
              putElse = true;
            }
            actionEmps += "if(action instanceof "
                + CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName()) + 
                "Action)\n{\n";
            // go through all participants:
            Vector<ActionTypeParticipant> participants = 
            	tempAct.getAllParticipants();
            boolean nextOneWriteElse = false;
            for (int m = 0; m < participants.size(); m++) {
              ActionTypeParticipant tempPart = participants.elementAt(m);
              if (tempPart.getSimSEObjectTypeType() == 
              	SimSEObjectTypeTypes.EMPLOYEE) { // Employee participant
                if (nextOneWriteElse) { // not on first element
                  actionEmps += "else ";
                }
                actionEmps += "if(partName.equals(\"" + tempPart.getName()
                    + "\"))\n{\n";
                actionEmps += "((" + CodeGeneratorUtils.getUpperCaseLeading(
                		tempAct.getName()) + "Action)action).add" + 
                		tempPart.getName()
                    + "(selectedEmp);\n}\n";
                nextOneWriteElse = true;
              }
            }
            actionEmps += "}\n";
          }
        }
      }
      
      return actionEmps;
  }
  
  private String generateAddParticipant(Vector<SimSEObjectType> objs, Vector<ActionType> acts) {
	  String participants = "";
	  boolean putElse9 = false;
      // go through each object type:
      for (int i = 0; i < objs.size(); i++) {
        SimSEObjectType tempType = objs.elementAt(i);
        if (tempType.getType() == SimSEObjectTypeTypes.EMPLOYEE) {
          if (putElse9) {
            participants += "else ";
          } else {
            putElse9 = true;
          }
          participants += "if(objTypeName.equals(\""
              + CodeGeneratorUtils.getUpperCaseLeading(tempType.getName()) + 
              "\"))\n{\n";
          if ((tempType.getKey().getType() == AttributeTypes.DOUBLE)
              || (tempType.getKey().getType() == AttributeTypes.INTEGER)) {
            participants += "try\n{\n";
          }
          participants += CodeGeneratorUtils.getUpperCaseLeading(
          		tempType.getName()) + " a = state.get" + 
          		SimSEObjectTypeTypes.getText(tempType.getType()) + 
          		"StateRepository().get" + CodeGeneratorUtils.getUpperCaseLeading(
          				tempType.getName()) + "StateRepository().get(";
          if (tempType.getKey().getType() == AttributeTypes.STRING) {
            participants += "keyValStr);";
          } else if (tempType.getKey().getType() == AttributeTypes.BOOLEAN) {
            participants += "(new Boolean(keyValStr)).booleanValue());";
          } else if (tempType.getKey().getType() == AttributeTypes.INTEGER) {
            participants += "(new Integer(keyValStr)).intValue());";
          } else if (tempType.getKey().getType() == AttributeTypes.DOUBLE) {
            participants += "(new Double(keyValStr)).doubleValue());";
          }
          participants += "\nif(a != null)\n{\n";
          // go through each action type:
          boolean putElse88 = false;
          for (int j = 0; j < acts.size(); j++) {
            ActionType tempAct = acts.elementAt(j);
            Vector<ActionTypeTrigger> trigs = tempAct.getAllTriggers();
            // only generate code for actions w/ user triggers:
            for (int k = 0; k < trigs.size(); k++) {
              ActionTypeTrigger tempTrig = trigs.elementAt(k);
              if (tempTrig instanceof UserActionTypeTrigger) {
                if (putElse88) { // not on first element
                  participants += "else ";
                } else {
                  putElse88 = true;
                }
                participants += "if(action instanceof "
                    + CodeGeneratorUtils.getUpperCaseLeading(
                    		tempAct.getName()) + "Action)\n{\n";
                // go through all participants:
                Vector<ActionTypeParticipant> parts = 
                	tempAct.getAllParticipants();
                boolean nextOneWriteElse = false;
                for (int m = 0; m < parts.size(); m++) {
                  ActionTypeParticipant tempPart = parts.elementAt(m);
                  if (tempPart.getSimSEObjectType(tempType.getName()) != null) { 
                  	// this SimSEObjectType is an allowable type for this
                  	// participant
                    if (nextOneWriteElse) { // not on first element
                      participants += "else ";
                    }
                    participants += "if(partName.equals(\"" + tempPart.getName()
                        + "\"))\n{\n";
                    participants += "((" + CodeGeneratorUtils.getUpperCaseLeading(
                    		tempAct.getName()) + "Action)action).add" + 
                    		tempPart.getName() + "(a);\n}\n";
                    nextOneWriteElse = true;
                  }
                }
                
                participants += "}\n";
              }
            }
          }
          participants += "}\n";
          
          if ((tempType.getKey().getType() == AttributeTypes.INTEGER)
              || (tempType.getKey().getType() == AttributeTypes.DOUBLE)) {
            participants += "}\ncatch(NumberFormatException e)\n{\n";
            participants += "System.out.println(e);\n}\n";
          }
          participants += "}\n";
        }
      }
      
      return participants;
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
            if (tempPart.getSimSEObjectTypeType() == 
            	SimSEObjectTypeTypes.EMPLOYEE) {
              parts += "if(selectedEmp == null)\n{\n";
            }
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
            if (tempPart.getSimSEObjectTypeType() == 
            	SimSEObjectTypeTypes.EMPLOYEE) {
              parts += "}\nelse\n{\n";
              parts += "minNumParts = ";
              if (tempPart.getQuantity().isMinValBoundless()) {
                parts += "0;";
              } else { // min val has a value
                parts += tempPart.getQuantity().getMinVal().toString()
                    + " - 1;";
              }
              parts += "\nmaxNumParts = ";
              if (tempPart.getQuantity().isMaxValBoundless()) {
                parts += "999999;";
              } else { // max val has a value
                parts += tempPart.getQuantity().getMaxVal().toString()
                    + " - 1;";
              }
              
              parts += "\n}\n";
            }
            parts += "\n}\n";
          }
          parts += "}\n";
        }
      return parts;
  }
}