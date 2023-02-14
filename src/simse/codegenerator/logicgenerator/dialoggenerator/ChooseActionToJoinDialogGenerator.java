/*
 * This class is responsible for generating all of the code for the logic's
 * ChooseActionToDestroyJoinDialog component
 */

package simse.codegenerator.logicgenerator.dialoggenerator;

import simse.codegenerator.CodeGeneratorConstants;
import simse.codegenerator.CodeGeneratorUtils;

import simse.modelbuilder.actionbuilder.ActionType;
import simse.modelbuilder.actionbuilder.ActionTypeParticipant;
import simse.modelbuilder.actionbuilder.ActionTypeParticipantAttributeConstraint;
import simse.modelbuilder.actionbuilder.ActionTypeTrigger;
import simse.modelbuilder.actionbuilder.DefinedActionTypes;
import simse.modelbuilder.actionbuilder.AttributeGuard;
import simse.modelbuilder.actionbuilder.UserActionTypeTrigger;
import simse.modelbuilder.objectbuilder.AttributeTypes;
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
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ChooseActionToJoinDialogGenerator implements
    CodeGeneratorConstants {
  private File directory; // directory to generate into
  private File catjdFile; // file to generate
  private DefinedActionTypes actTypes; // holds all of the defined action types

  public ChooseActionToJoinDialogGenerator(DefinedActionTypes actTypes, 
  		File directory) {
    this.directory = directory;
    this.actTypes = actTypes;
  }

  public void generate() {
	  ClassName eventHandler = ClassName.get("javafx.event", "EventHandler");
	  ClassName mouseEvent = ClassName.get("javafx.scene.input", "MouseEvent");
	  ClassName vector = ClassName.get("java.util", "Vector");
	  ClassName actionClass = ClassName.get("simse.adts.actions", "Action");
	  ClassName stateClass = ClassName.get("simse.state", "State");
	  ClassName ruleExecClass = ClassName.get("simse.logic", "RuleExecutor");
	  ClassName employeeClass = ClassName.get("simse.adts.objects", "Employee");
	  ClassName radioButton = ClassName.get("javafx.scene.control", "RadioButton");
	  ClassName stringClass = ClassName.get("java.lang", "String");
	  ClassName roleDialog = ClassName.get("simse.logic.dialogs", "ChooseRoleToPlayDialog");
	  ClassName buttonClass = ClassName.get("javafx.scene.control", "Button");
	  ClassName stageClass = ClassName.get("javafx.stage", "Stage");
	  ClassName dialogClass = ClassName.get("javafx.scene.control", "Dialog");
	  ClassName vBoxClass = ClassName.get("javafx.scene.layout", "VBox");
	  ClassName labelClass = ClassName.get("javafx.scene.control", "Label");
	  ClassName paneClass = ClassName.get("javafx.scene.layout", "Pane");
	  ClassName gridPaneClass = ClassName.get("javafx.scene.layout", "GridPane");
	  ClassName point2DClass = ClassName.get("javafx.geometry", "Point2D");
	  ClassName alertClass = ClassName.get("javafx.scene.control", "Alert");
	  ClassName alertTypeClass = ClassName.get("javafx.scene.control.Alert", "AlertType");
	  ClassName toggleGroupClass = ClassName.get("javafx.scene.control", "ToggleGroup");
	  ClassName radioButtonClass = ClassName.get("javafx.scene.control", "RadioButton");
	  TypeName mouseHandler = ParameterizedTypeName.get(eventHandler, mouseEvent);
	  TypeName actionsVector = ParameterizedTypeName.get(vector, actionClass);
	  TypeName radioVector = ParameterizedTypeName.get(vector, radioButton);
	  TypeName stringVector = ParameterizedTypeName.get(vector, stringClass);
	  
	// make a Vector of all the action types with user triggers:
      Vector<ActionType> userTrigActs = new Vector<ActionType>();
      Vector<ActionType> allActs = actTypes.getAllActionTypes();
      for (int j = 0; j < allActs.size(); j++) {
        ActionType userAct = allActs.elementAt(j);
        Vector<ActionTypeTrigger> allTrigs = userAct.getAllTriggers();
        for (int k = 0; k < allTrigs.size(); k++) {
          ActionTypeTrigger tempTrig = allTrigs.elementAt(k);
          if (tempTrig instanceof UserActionTypeTrigger) {
            userTrigActs.add(userAct);
            break;
          }
        }
      }
      
      MethodSpec joinConstructor = MethodSpec.constructorBuilder()
			  .addModifiers(Modifier.PUBLIC)
			  .addParameter(stageClass, "parent")
			  .addParameter(actionsVector, "acts")
			  .addParameter(employeeClass, "e")
			  .addParameter(stateClass, "s")
			  .addParameter(String.class, "menText")
			  .addParameter(ruleExecClass, "re")
			  .addStatement("$N = parent", "gui")
			  .addStatement("$N = acts", "actions")
			  .addStatement("$N = e", "emp")
			  .addStatement("$N = s", "state")
			  .addStatement("$N = menText", "menuText")
			  .addStatement("$N = re", "ruleExec")
			  .addStatement("$N = new $T()", "radioButtons", radioVector)
			  .addStatement("$N = new $T()", "radioButtonsGroup", toggleGroupClass)
			  .addStatement("setTitle($S)", "Join Action")
			  .addStatement("$T mainPane = new $T()", vBoxClass, vBoxClass)
			  .addStatement("$T topPane = new $T", paneClass, paneClass)
			  .addStatement("$T actionName = new $T()", String.class, String.class)
			  .addStatement("$T tempAct = $N.elementAt(0)", actionClass, "actions")
			  .addCode(generateNames(userTrigActs))
			  .addStatement("topPane.getChildren().add(new $T($S + actionName + $S))",
					  labelClass, "Choose which ", " Action to join:")
			  .addStatement("$T middlePane = new $T()", gridPaneClass, gridPaneClass)
			  .addCode(generateActionConstructor(userTrigActs))
			  .addStatement("$T bottomPane = new $T()", paneClass, paneClass)
			  .addStatement("$N = new $T($S)", "okButton", buttonClass, "OK")
			  .addStatement("$N.addEventHandler($T.MOUSE_CLICKED, this)", "okButton", mouseEvent)
			  .addStatement("bottomPane.getChildren.add($N)", "okButton")
			  .addStatement("$N = new $T($S)", "cancelButton", buttonClass, "Cancel")
			  .addStatement("$N.addEventHandler($T.MOUSE_CLICKED, this)", "cancelButton", mouseEvent)
			  .addStatement("bottomPane.getChildren.add($N)", "cancelButton")
			  .addStatement("mainPane.getChildren.addAll(topPane, middlePane, bottomPane)")
			  .addStatement("$T ownerLoc = new $T(parent.getX(), parent.getY()", point2DClass, point2DClass)
			  .addStatement("$T thisLoc = new $T((ownerLoc.getX() + (parent.getWidth() / 2) - (this.getWidth() / 2)),"
			  		+ "(ownerLoc.getY() + (parent.getHeight() / 2) - (this.getHeight() / 2)))", point2DClass, point2DClass)
			  .addStatement("this.setX(thisLoc.getX())")
			  .addStatement("this.setY(thisLoc.getY())")
			  .beginControlFlow("if ($N.size() == 1)", "radioButtons")
			  .addStatement("onlyOneChoice(parent)")
			  .nextControlFlow("else ")
			  .addStatement("show()")
			  .endControlFlow()
			  .build();
      
	  
	  MethodSpec handle = MethodSpec.methodBuilder("handle")
			  .addModifiers(Modifier.PUBLIC)
			  .returns(void.class)
			  .addParameter(mouseEvent, "evt")
			  .addAnnotation(Override.class)
			  .addStatement("$T source = evt.getSource()", Object.class)
			  .beginControlFlow("if (source == cancelButton)")
			  .addStatement("close()")
			  .nextControlFlow("else if (source == $N)", "okButton")
			  .addStatement("$T anySelected = false", boolean.class)
			  .beginControlFlow("for (int i = 0; i < $N.size(); i++)", "radioButtons")
			  .addStatement("$T tempRButt = $N.elementAt(i)", radioButtonClass, "radioButtons")
			  .beginControlFlow("if (tempRButt.isSelected()")
			  .addStatement("anySelected = true")
			  .addStatement("break")
			  .endControlFlow()
			  .endControlFlow()
			  .beginControlFlow("if (!anySelected)")
			  .addStatement("$T alert = new $T($T.WARNING, $S)", alertClass, alertClass,
					  alertTypeClass, "You must choose at least one action")
			  .addStatement("alert.setTitle($S)", "Invalid Input")
			  .addStatement("alert.show()")
			  .nextControlFlow(" else ")
			  .beginControlFlow("for(int i=0; i<$N.size(); i++)", "radioButtons")
			  .addStatement("$T rButt = $N.elementAt(i)", radioButtonClass, "radioButtons")
			  .beginControlFlow("if (rButt.isSelected()")
			  .addStatement("$T tempAct = $N.elementAt(i)", actionClass, "actions")
			  .addStatement("$T participantNames = new $T()", stringVector, stringVector)
			  .addCode(generateActionHandle(userTrigActs))
			  .addStatement("new $T($N, participantNames, $N, tempAct, $N, $N)",
					  roleDialog, "gui", "emp", "menuText", "ruleExec")
			  .addStatement("close()")
			  .addStatement("break")
			  .endControlFlow()
			  .endControlFlow()
			  .endControlFlow()
			  .endControlFlow()
			  .build();
	  
	  MethodSpec onlyOneChoice = MethodSpec.methodBuilder("onlyOneChoice")
			  .addModifiers(Modifier.PRIVATE)
			  .returns(void.class)
			  .addParameter(stageClass, "owner")
			  .beginControlFlow("for(int i=0; i<$N.size(); i++)", "radioButtons")
			  .addStatement("$T rButt = $N.elementAt(i)", radioButtonClass, "radioButtons")
			  .addStatement("$T tempAct = $N.elementAt(i)", actionClass, "actions")
			  .addStatement("$T participantNames = new $T()", stringVector, stringVector)
			  .addCode(generateActionOnlyOneChoice(userTrigActs))
			  .addStatement("new $T(owner, participantNames, $N, tempAct, $N, $N)",
					  roleDialog, "emp", "menuText", "ruleExec")
			  .addStatement("close()")
			  .addStatement("break")
			  .endControlFlow()
			  .build();
	  
	  TypeSpec joinDialog = TypeSpec.classBuilder("ChooseActionToJoinDialog")
			  .superclass(dialogClass)
			  .addSuperinterface(mouseHandler)
			  .addField(stageClass, "gui", Modifier.PRIVATE)
			  .addField(actionsVector, "actions", Modifier.PRIVATE)
			  .addField(stateClass, "state", Modifier.PRIVATE)
			  .addField(employeeClass, "emp", Modifier.PRIVATE)
			  .addField(radioVector, "radioButtons", Modifier.PRIVATE)
			  .addField(toggleGroupClass, "radioButtonGroup", Modifier.PRIVATE)
			  .addField(buttonClass, "okButton", Modifier.PRIVATE)
			  .addField(buttonClass, "cancelButton", Modifier.PRIVATE)
			  .addField(String.class, "menuText", Modifier.PRIVATE)
			  .addField(ruleExecClass, "ruleExec", Modifier.PRIVATE)
			  .addMethod(joinConstructor)
			  .addMethod(handle)
			  .addMethod(onlyOneChoice)
			  .build();

	  ClassName actions = ClassName.get("simse.adts", "actions");
	  JavaFile javaFile = JavaFile.builder("simse.logic.dialogs", joinDialog)
			  .addStaticImport(actions, "*")  
			  .build();
	  
    try {
      catjdFile = new File(directory,
          ("simse\\logic\\dialogs\\ChooseActionToJoinDialog.java"));
      if (catjdFile.exists()) {
        catjdFile.delete(); // delete old version of file
      }
      
      FileWriter writer = new FileWriter(catjdFile);
      
      javaFile.writeTo(writer);
      writer.close();
      
    } catch (IOException e) {
      JOptionPane.showMessageDialog(null, ("Error writing file "
          + catjdFile.getPath() + ": " + e.toString()), "File IO Error",
          JOptionPane.WARNING_MESSAGE);
    }
  }
  
  private String generateNames(Vector<ActionType> userTrigActs) {
	  String names = "";
      for (int j = 0; j < userTrigActs.size(); j++) {
          ActionType act = userTrigActs.elementAt(j);
          if (j > 0) { // not on first element
            names += "else ";
          }
          names += "if(tempAct instanceof "
              + CodeGeneratorUtils.getUpperCaseLeading(act.getName()) + "Action)\n{\n";
          names += "actionName = \"" + CodeGeneratorUtils.getUpperCaseLeading(
          		act.getName()) + "\";\n}\n";
        }
      
      return names;
  }
  
  private String generateActionConstructor(Vector<ActionType> userTrigActs) {
	  String actions = "";
	  
      for (int j = 0; j < userTrigActs.size(); j++) {
          ActionType act = userTrigActs.elementAt(j);
          if (j > 0) { // not on first element
            actions += "else ";
          }
          actions += "if(tempAct instanceof "
              + CodeGeneratorUtils.getUpperCaseLeading(act.getName()) + 
              "Action)\n{\n";
          actions += "for(int i=0; i<actions.size(); i++)\n{\n";
          actions += CodeGeneratorUtils.getUpperCaseLeading(act.getName()) + 
          		"Action act = ("
              + CodeGeneratorUtils.getUpperCaseLeading(act.getName())
              + "Action)actions.elementAt(i);\n";
          actions += "StringBuffer label = new StringBuffer(\"<html>\");\n";
          // go through all participants:
          Vector<ActionTypeParticipant> parts = act.getAllParticipants();
          for (int k = 0; k < parts.size(); k++) {
            if (k > 0) { // not on first element
              actions += "label.append(\"; <br>\");\n";
            }
            ActionTypeParticipant tempPart = parts.elementAt(k);
            String metaTypeName = CodeGeneratorUtils.getUpperCaseLeading(
            		SimSEObjectTypeTypes.getText(tempPart.getSimSEObjectTypeType()));
            actions += "label.append(\"" + tempPart.getName() + "(s): \");\n";
            actions += "Vector<" + metaTypeName + "> all" + tempPart.getName() +
            		"s = act.getAll" + tempPart.getName() + "s();\n";
            actions += "for(int j=0; j<all" + tempPart.getName()
                + "s.size(); j++)\n{\n";
            actions += "if(j > 0)\n{\n";
            actions += "label.append(\", \");\n}\n";
            actions += SimSEObjectTypeTypes.getText(tempPart
                .getSimSEObjectTypeType())
                + " a = all" + tempPart.getName() + "s.elementAt(j);\n";
            // go through all allowable SimSEObjectTypes for this participant:
            Vector<SimSEObjectType> ssObjTypes = tempPart.getAllSimSEObjectTypes();
            for (int m = 0; m < ssObjTypes.size(); m++) {
              SimSEObjectType tempType = ssObjTypes.elementAt(m);
              if (m > 0) { // not on first element
                actions += "else ";
              }
              actions += "if(a instanceof "
                  + CodeGeneratorUtils.getUpperCaseLeading(tempType.getName()) + 
                  ")\n{\n";
              actions += "label.append(\"" + tempType.getName() + "(\" + (("
                  + CodeGeneratorUtils.getUpperCaseLeading(tempType.getName()) + 
                  ")a).get" + 
                  CodeGeneratorUtils.getUpperCaseLeading(
                  		tempType.getKey().getName()) + "() + \")\");\n}\n";
            }
            actions += "}\n";
          }
          actions += "label.append(\"</HTML>\");\n";
          actions += "JPanel tempPane = new JPanel(new BorderLayout());\n";
          actions += "JRadioButton tempRadioButton = new "
          		+ "JRadioButton(label.toString());\n";
          actions += "radioButtonGroup.add(tempRadioButton);\n";
          actions += "tempPane.add(tempRadioButton, BorderLayout.WEST);\n";
          actions += "radioButtons.add(tempRadioButton);\n";
          actions += "middlePane.add(tempPane);\n}\n}\n";
        }
      
      return actions;
  }
  
  private String generateActionHandle(Vector<ActionType> userTrigActs) {
	  String actions = "";
      for (int i = 0; i < userTrigActs.size(); i++) {
          ActionType tempAct = userTrigActs.elementAt(i);
          if (i > 0) { // not on first element
            actions += "else ";
          }
          actions += "if(tempAct instanceof "
              + CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName()) + 
              "Action)\n{\n";

          // go through all employee participants:
          Vector<ActionTypeParticipant> allParts = tempAct.getAllParticipants();
          for (int j = 0; j < allParts.size(); j++) {
            ActionTypeParticipant part = allParts.elementAt(j);
            if (part.getSimSEObjectTypeType() == SimSEObjectTypeTypes.EMPLOYEE) { 
            	// employee participant
              actions += "Vector all" + part.getName() + "s = (("
                  + CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName())
                  + "Action)tempAct).getAll" + part.getName() + "s();\n";
              actions += "if((all" + part.getName()
                  + "s.contains(emp) == false) && (all" + part.getName()
                  + "s.size() < ";
              if (part.getQuantity().isMaxValBoundless()) {
                actions += "999999";
              } else { // max val has a value
                actions += part.getQuantity().getMaxVal().toString();
              }
              actions += "))\n{\n";

              // collect all the user triggers for this action:
              Vector<UserActionTypeTrigger> userTrigsTemp = 
              	new Vector<UserActionTypeTrigger>();
              Vector<ActionTypeTrigger> allTriggers = tempAct.getAllTriggers();
              for (int k = 0; k < allTriggers.size(); k++) {
                ActionTypeTrigger tempTrig = allTriggers.elementAt(k);
                if (tempTrig instanceof UserActionTypeTrigger) {
                  userTrigsTemp.add((UserActionTypeTrigger)tempTrig);
                }
              }

              // go through all of the user triggers:
              for (int k = 0; k < userTrigsTemp.size(); k++) {
                UserActionTypeTrigger userTrig = userTrigsTemp.elementAt(k);
                if (k > 0) { // not on first element
                  actions += "else ";
                }
                actions += "if(menuText.equals(\"" + userTrig.getMenuText()
                    + "\"))\n{\n";

                // go through all allowable types:
                Vector<SimSEObjectType> types = part.getAllSimSEObjectTypes();
                for (int m = 0; m < types.size(); m++) {
                  SimSEObjectType type = types.elementAt(m);
                  if (m > 0) { // not on first element
                    actions += "else ";
                  }
                  actions += "if((emp instanceof "
                      + CodeGeneratorUtils.getUpperCaseLeading(type.getName()) + 
                      ")";

                  // go through all attribute constraints:
                  ActionTypeParticipantAttributeConstraint[] attConstraints = 
                  	userTrig.getParticipantTrigger(part.getName()).getConstraint(
                          type.getName()).getAllAttributeConstraints();
                  for (int n = 0; n < attConstraints.length; n++) {
                    ActionTypeParticipantAttributeConstraint attConst = 
                    	attConstraints[n];
                    if (attConst.isConstrained()) {
                      actions += " && ((("
                              + CodeGeneratorUtils.getUpperCaseLeading(
                              		type.getName()) + ")emp).get" + 
                              		CodeGeneratorUtils.getUpperCaseLeading(
                              				attConst.getAttribute().getName()) + "() ";
                      if (attConst.getAttribute().getType() == 
                      	AttributeTypes.STRING) {
                        actions += ".equals(" + "\""
                            + attConst.getValue().toString() + "\")";
                      } else {
                        if (attConst.getGuard().equals(AttributeGuard.EQUALS)) {
                          actions += "== ";
                        } else {
                          actions += attConst.getGuard() + " ";
                        }
                        actions += attConst.getValue().toString();
                      }
                      actions += ")";
                    }
                  }
                  actions += ")\n{\n";
                  actions += "participantNames.add(\""
                      + CodeGeneratorUtils.getUpperCaseLeading(part.getName()) + 
                      "\");\n}\n";
                }
                actions += "}\n";
              }
              actions += "}\n";
            }
          }
          actions += "}\n";
        }
      
      return actions;
  }
  
  private String generateActionOnlyOneChoice(Vector<ActionType> userTrigActs) {
	  String actions = "";
	  
	  for (int i = 0; i < userTrigActs.size(); i++) {
	        ActionType tempAct = userTrigActs.elementAt(i);
	        if (i > 0) { // not on first element
	          actions += "else ";
	        }
	        actions += "if(tempAct instanceof "
	            + CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName()) + 
	            "Action)\n{\n";

	        // go through all employee participants:
	        Vector<ActionTypeParticipant> allParts = tempAct.getAllParticipants();
	        for (int j = 0; j < allParts.size(); j++) {
	          ActionTypeParticipant part = allParts.elementAt(j);
	          if (part.getSimSEObjectTypeType() == SimSEObjectTypeTypes.EMPLOYEE) { 
	          	// employee participant
	            actions += "Vector all" + part.getName() + "s = (("
	                + CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName())
	                + "Action)tempAct).getAll" + part.getName() + "s();\n";
	            actions += "if((all" + part.getName()
	                + "s.contains(emp) == false) && (all" + part.getName()
	                + "s.size() < ";
	            if (part.getQuantity().isMaxValBoundless()) {
	              actions += "999999";
	            } else { // max val has a value
	              actions += part.getQuantity().getMaxVal().toString();
	            }
	            actions += "))\n{\n";

	            // collect all the user triggers for this action:
	            Vector<UserActionTypeTrigger> userTrigsTemp = 
	            	new Vector<UserActionTypeTrigger>();
	            Vector<ActionTypeTrigger> allTriggers = tempAct.getAllTriggers();
	            for (int k = 0; k < allTriggers.size(); k++) {
	              ActionTypeTrigger tempTrig = allTriggers.elementAt(k);
	              if (tempTrig instanceof UserActionTypeTrigger) {
	                userTrigsTemp.add((UserActionTypeTrigger)tempTrig);
	              }
	            }

	            // go through all of the user triggers:
	            for (int k = 0; k < userTrigsTemp.size(); k++) {
	              UserActionTypeTrigger userTrig = userTrigsTemp.elementAt(k);
	              if (k > 0) { // not on first element
	                actions += "else ";
	              }
	              actions += "if(menuText.equals(\"" + userTrig.getMenuText()
	                  + "\"))\n{\n";

	              // go through all allowable types:
	              Vector<SimSEObjectType> types = part.getAllSimSEObjectTypes();
	              for (int m = 0; m < types.size(); m++) {
	                SimSEObjectType type = types.elementAt(m);
	                if (m > 0) { // not on first element
	                  actions += "else ";
	                }
	                actions += "if((emp instanceof "
	                    + CodeGeneratorUtils.getUpperCaseLeading(type.getName()) + 
	                    ")";

	                // go through all attribute constraints:
	                ActionTypeParticipantAttributeConstraint[] attConstraints = 
	                	userTrig.getParticipantTrigger(part.getName()).getConstraint(
	                        type.getName()).getAllAttributeConstraints();
	                for (int n = 0; n < attConstraints.length; n++) {
	                  ActionTypeParticipantAttributeConstraint attConst = 
	                  	attConstraints[n];
	                  if (attConst.isConstrained()) {
	                    actions += " && ((("
	                            + CodeGeneratorUtils.getUpperCaseLeading(
	                            		type.getName()) + ")emp).get" + 
	                            		CodeGeneratorUtils.getUpperCaseLeading(
	                            				attConst.getAttribute().getName()) + "() ";
	                    if (attConst.getAttribute().getType() == 
	                    	AttributeTypes.STRING) {
	                      actions += ".equals(" + "\""
	                          + attConst.getValue().toString() + "\")";
	                    } else {
	                      if (attConst.getGuard().equals(AttributeGuard.EQUALS)) {
	                        actions += "== ";
	                      } else {
	                        actions += attConst.getGuard() + " ";
	                      }
	                      actions += attConst.getValue().toString();
	                    }
	                    actions += ")";
	                  }
	                }
	                actions += ")\n{\n";
	                actions += "participantNames.add(\""
	                    + CodeGeneratorUtils.getUpperCaseLeading(part.getName()) + 
	                    "\");\n}\n";
	              }
	              actions += "}\n";
	            }
	            actions += "}\n";
	          }
	        }
            actions += "}\n";
      }
	  
	  return actions;
  }
}