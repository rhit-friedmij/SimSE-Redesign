/*
 * This class is responsible for generating all of the code for the logic's
 * ChooseRoleToPlayDialog component
 */

package simse.codegenerator.logicgenerator.dialoggenerator;

import simse.codegenerator.CodeGeneratorConstants;
import simse.codegenerator.CodeGeneratorUtils;

import simse.modelbuilder.actionbuilder.ActionTypeParticipant;
import simse.modelbuilder.actionbuilder.ActionType;
import simse.modelbuilder.actionbuilder.ActionTypeTrigger;
import simse.modelbuilder.actionbuilder.DefinedActionTypes;
import simse.modelbuilder.actionbuilder.UserActionTypeTrigger;
import simse.modelbuilder.objectbuilder.SimSEObjectTypeTypes;
import simse.modelbuilder.rulebuilder.Rule;

import java.awt.Button;
import java.io.File;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ChooseRoleToPlayDialogGenerator implements CodeGeneratorConstants {
  private File directory; // directory to generate into
  private File crtpdFile; // file to generate
  private DefinedActionTypes actTypes; // holds all of the defined action types

  public ChooseRoleToPlayDialogGenerator(DefinedActionTypes actTypes, 
  		File directory) {
    this.directory = directory;
    this.actTypes = actTypes;
  }

  public void generate() {
	  ClassName eventHandler = ClassName.get("javafx.event", "EventHandler");
	  ClassName mouseEvent = ClassName.get("javafx.scene.input", "MouseEvent");
	  ClassName vector = ClassName.get("java.util", "Vector");
	  ClassName ruleExecClass = ClassName.get("simse.logic", "RuleExecutor");
	  ClassName employeeClass = ClassName.get("simse.adts.objects", "Employee");
	  ClassName actionClass = ClassName.get("simse.adts.actions", "Action");
	  ClassName buttonClass = ClassName.get("javafx.scene.control", "Button");
	  ClassName stageClass = ClassName.get("javafx.stage", "Stage");
	  ClassName dialogClass = ClassName.get("javafx.scene.control", "Dialog");
	  ClassName vBoxClass = ClassName.get("javafx.scene.layout", "VBox");
	  ClassName labelClass = ClassName.get("javafx.scene.control", "Label");
	  ClassName paneClass = ClassName.get("javafx.scene.layout", "Pane");
	  ClassName point2DClass = ClassName.get("javafx.geometry", "Point2D");
	  ClassName comboBoxClass = ClassName.get("javafx.scene.control", "ComboBox");
	  ClassName stringClass = ClassName.get(String.class);
	  TypeName mouseHandler = ParameterizedTypeName.get(eventHandler, mouseEvent);
	  TypeName stringVector = ParameterizedTypeName.get(vector, stringClass);
	  
	  MethodSpec roleConstructor = MethodSpec.constructorBuilder()
			  .addModifiers(Modifier.PUBLIC)
			  .addParameter(stageClass, "owner")
			  .addParameter(stringVector, "partNames")
			  .addParameter(employeeClass, "e")
			  .addParameter(actionClass, "act")
			  .addParameter(String.class, "menText")
			  .addParameter(ruleExecClass, "re")
			  .addStatement("$N = owner", "gui")
			  .addStatement("$N = e", "emp")
			  .addStatement("$N = act", "action")
			  .addStatement("$N = menText", "menuText")
			  .addStatement("4N = re", "ruleExec")
			  .addStatement("setTitle($S)", "Choose Action Role")
			  .addStatement("$T mainPane = new $T()", vBoxClass, vBoxClass)
			  .addStatement("$T topPane = new $T()", paneClass, paneClass)
			  .addStatement("topPane.getChildren().add(new $T($S))", labelClass, "Choose role to play:")
			  .addStatement("$T middlePane = new $T()", paneClass, paneClass)
			  .addStatement("$N = new $T(FXCollections.observableList(partNames))", "partNameList", comboBoxClass)
			  .addStatement("middlePane.getChildren().add(partNameList)")
			  .addStatement("$T bottomPane = new $T()", paneClass, paneClass)
			  .addStatement("$N = new $T($S)", "okButton", buttonClass, "OK")
			  .addStatement("$N.addEventHandler($T.MOUSE_CLICKED, this)", "okButton", mouseEvent)
			  .addStatement("bottomPane.getChildren().add($N)", "okButton")
			  .addStatement("$N = new $T($S)", "cancelButton", buttonClass, "Cancel")
			  .addStatement("$N.addEventHandler($T.MOUSE_CLICKED, this)", "cancelButton", mouseEvent)
			  .addStatement("bottomPane.getChildren().add($N)", "cancelButton")
			  .addStatement("mainPane.getChildren().addAll(topPane, middlePane, bottomPane)")
			  .addStatement("$T ownerLoc = new $T(owner.getX(), owner.getY())", point2DClass, point2DClass)
			  .addStatement("$T thisLoc = new $T((ownerLoc.getX() + (owner.getWidth() / 2)"
			  		+ " - (this.getWidth() / 2))", point2DClass, point2DClass)
			  .addStatement("(ownerLoc.getY() + (owner.getHeight() / 2) - (this.getHeight() / 2)))")
			  .addStatement("this.setX(thisLoc.getX())")
			  .addStatement("this.setY(thisLoc.getY())")
			  .addStatement("if (partNames.size() == 1) ")
			  .addStatement("onlyOneRole()")
			  .addStatement("} else ")
			  .addStatement("show()")
			  .addStatement("")
			  .build();
	  
      // make a Vector of all the action types with user triggers:
      Vector<ActionType> userTrigActs = new Vector<ActionType>();
      Vector<ActionType> allActs = actTypes.getAllActionTypes();
      for (int j = 0; j < allActs.size(); j++) {
        ActionType userAct = allActs.elementAt(j);
        Vector<ActionTypeTrigger> allTrigs = userAct.getAllTriggers();
        for (int k = 0; k < allTrigs.size(); k++) {
          ActionTypeTrigger tempTrig = allTrigs
              .elementAt(k);
          if (tempTrig instanceof UserActionTypeTrigger) {
            userTrigActs.add(userAct);
            break;
          }
        }
      }
	  
	  MethodSpec handle = MethodSpec.methodBuilder("handle")
			  .addAnnotation(Override.class)
			  .addModifiers(Modifier.PUBLIC)
			  .returns(void.class)
			  .addParameter(mouseEvent, "evt")
			  .addStatement("$T source = evt.getSource()", Object.class)
			  .beginControlFlow("if (source == $N)", "cancelButton")
			  .addStatement("close()")
			  .nextControlFlow("else if (source == $N)", "okButton")
			  .addStatement("$T partName = ($T)($N.getSelectedItem())", 
					  String.class, String.class, "partNameList")
			  .addCode(generateActionHandle(userTrigActs))
			  .addStatement("close()")
			  .endControlFlow()
			  .build();
	  
	  MethodSpec onlyOneRole = MethodSpec.methodBuilder("onlyOneRole")
			  .addModifiers(Modifier.PUBLIC)
			  .returns(void.class)
			  .addStatement("$T partName = ($T) ($N.getitems().get(0))", 
					  String.class, String.class, "partNameList")
			  .addCode(generateActionOnlyOneRole(userTrigActs))
			  .addStatement("close()")
			  .build();
	  
	  TypeSpec roleDialog = TypeSpec.classBuilder("ChooseRoleToPlayDialog")
			  .superclass(dialogClass)
			  .addSuperinterface(mouseHandler)
			  .addField(stageClass, "gui", Modifier.PRIVATE)
			  .addField(employeeClass, "emp", Modifier.PRIVATE)
			  .addField(actionClass, "action", Modifier.PRIVATE)
			  .addField(String.class, "menuText", Modifier.PRIVATE)
			  .addField(ruleExecClass, "ruleExec", Modifier.PRIVATE)
			  .addField(comboBoxClass, "partNameList", Modifier.PRIVATE)
			  .addField(buttonClass, "okButton", Modifier.PRIVATE)
			  .addField(buttonClass, "cancelButton", Modifier.PRIVATE)
			  .addMethod(roleConstructor)
			  .addMethod(handle)
			  .addMethod(onlyOneRole)
			  .build();
	  

	  ClassName actions = ClassName.get("simse.adts", "actions");
	  JavaFile javaFile = JavaFile.builder("ChooseRoleToPlayDialog", roleDialog)
			  .addStaticImport(actions, "*")  
			  .build();
	  
	  
    try {
      crtpdFile = new File(directory,
          ("simse\\logic\\dialogs\\ChooseRoleToPlayDialog.java"));
      if (crtpdFile.exists()) {
        crtpdFile.delete(); // delete old version of file
      }
      
      javaFile.writeTo(crtpdFile);
    } catch (IOException e) {
        JOptionPane.showMessageDialog(null, ("Error writing file "
            + crtpdFile.getPath() + ": " + e.toString()), "File IO Error",
            JOptionPane.WARNING_MESSAGE);
    }

      // other constructor:
      /*
       * writer.write("public ChooseRoleToPlayDialog(JFrame owner, Vector
       * partNames, Employee e, simse.adts.actions.Action act, String
       * menText)"); writer.write(NEWLINE); writer.write(OPEN_BRACK);
       * writer.write(NEWLINE); writer.write("super(owner, true);");
       * writer.write(NEWLINE); writer.write("emp = e;"); writer.write(NEWLINE);
       * writer.write("action = act;"); writer.write(NEWLINE);
       * writer.write("menuText = menText;"); writer.write(NEWLINE);
       * writer.write("setTitle(\"Choose Action Role\");");
       * writer.write(NEWLINE); // main pane: writer.write("Box mainPane =
       * Box.createVerticalBox();"); writer.write(NEWLINE); // top pane:
       * writer.write("JPanel topPane = new JPanel();"); writer.write(NEWLINE);
       * writer.write("topPane.add(new JLabel(\"Choose role to play:\"));");
       * writer.write(NEWLINE); writer.write("JPanel middlePane = new
       * JPanel();"); writer.write(NEWLINE); writer.write("partNameList = new
       * JComboBox(partNames);"); writer.write(NEWLINE);
       * writer.write("middlePane.add(partNameList);"); writer.write(NEWLINE);
       *  // bottom pane: writer.write("JPanel bottomPane = new JPanel();");
       * writer.write(NEWLINE); writer.write("okButton = new JButton(\"OK\");");
       * writer.write(NEWLINE);
       * writer.write("okButton.addActionListener(this);");
       * writer.write(NEWLINE); writer.write("bottomPane.add(okButton);");
       * writer.write(NEWLINE); writer.write("cancelButton = new
       * JButton(\"Cancel\");"); writer.write(NEWLINE);
       * writer.write("cancelButton.addActionListener(this);");
       * writer.write(NEWLINE); writer.write("bottomPane.add(cancelButton);");
       * writer.write(NEWLINE);
       *  // add panes to main pane: writer.write("mainPane.add(topPane);");
       * writer.write(NEWLINE); writer.write("mainPane.add(middlePane);");
       * writer.write(NEWLINE); writer.write("mainPane.add(bottomPane);");
       * writer.write(NEWLINE);
       *  // Set main window frame properties:
       * writer.write("setContentPane(mainPane);"); writer.write(NEWLINE);
       * writer.write("validate();"); writer.write(NEWLINE);
       * writer.write("pack();"); writer.write(NEWLINE);
       * writer.write("repaint();"); writer.write(NEWLINE);
       * writer.write("toFront();"); writer.write(NEWLINE); writer.write("Point
       * ownerLoc = owner.getLocationOnScreen();"); writer.write(NEWLINE);
       * writer.write("Point thisLoc = new Point();"); writer.write(NEWLINE);
       * writer.write("thisLoc.setLocation((ownerLoc.getX() + (owner.getWidth() /
       * 2) - (this.getWidth() / 2)), (ownerLoc.getY() + (owner.getHeight() / 2) -
       * (this.getHeight() / 2)));"); writer.write(NEWLINE);
       * writer.write("setLocation(thisLoc);"); writer.write(NEWLINE);
       * writer.write("if(partNames.size() == 1)"); writer.write(NEWLINE);
       * writer.write(OPEN_BRACK); writer.write(NEWLINE);
       * writer.write("onlyOneRole();"); writer.write(NEWLINE);
       * writer.write(CLOSED_BRACK); writer.write(NEWLINE);
       * writer.write("else"); writer.write(NEWLINE); writer.write(OPEN_BRACK);
       * writer.write(NEWLINE); writer.write("setVisible(true);");
       * writer.write(NEWLINE); writer.write(CLOSED_BRACK);
       * writer.write(NEWLINE); writer.write(CLOSED_BRACK);
       * writer.write(NEWLINE);
       */

  }
  
  private String generateActionHandle(Vector<ActionType> userTrigActs) {
	  String actions = "";
	  
      for (int i = 0; i < userTrigActs.size(); i++) {
        ActionType tempAct = userTrigActs.elementAt(i);
        actions += "if(action instanceof "
            + CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName()) + 
            "Action)\n{\n";

        // go through all the action's user triggers:
        boolean putElse = false;
        Vector<ActionTypeTrigger> allTrigs = tempAct.getAllTriggers();
        for (int j = 0; j < allTrigs.size(); j++) {
          ActionTypeTrigger outerTrig = allTrigs.elementAt(j);
          if ((outerTrig instanceof UserActionTypeTrigger)
              && (outerTrig.getTriggerText() != null)
              && (outerTrig.getTriggerText().length() > 0)) {
            if (putElse) {
              actions += "else ";
            } else {
              putElse = true;
            }
            actions += "if(menuText.equals(\""
                + ((UserActionTypeTrigger) outerTrig).getMenuText() + "\"))\n{\n";
            actions += "emp.setOverheadText(\""
                    + ((UserActionTypeTrigger) outerTrig).getTriggerText()
                    + "\");\n}\n";
          }
        }

        // go through all employee participants:
        Vector<ActionTypeParticipant> allParts = tempAct.getAllParticipants();
        int numEmpParts = 0;
        for (int j = 0; j < allParts.size(); j++) {
          ActionTypeParticipant part = allParts.elementAt(j);
          if (part.getSimSEObjectTypeType() == SimSEObjectTypeTypes.EMPLOYEE) { 
          	// employee participant
            if (numEmpParts > 0) { // not on first element
              actions += "else ";
            }
            numEmpParts++;
            actions += "if(partName.equals(\"" + part.getName() + "\"))\n{\n";
            actions += "((" + CodeGeneratorUtils.getUpperCaseLeading(
            		tempAct.getName())+ "Action)action).add" + part.getName() + 
            		"(emp);\n";

            // go through all trigger rules and execute rules that are to be
            // executed on each join:
            Vector<Rule> rules = tempAct.getAllTriggerRules();
            for (int k = 0; k < rules.size(); k++) {
              Rule tempRule = rules.elementAt(k);
              if (tempRule.getExecuteOnJoins() == true) {
                actions += "ruleExec.update(gui, RuleExecutor.UPDATE_ONE, "
                    + "\"" + tempRule.getName() + "\", action);\n";
              }
            }
            actions += "}\n";
          }
        }
        actions += "}\n";
      }
      
      return actions;
  }
  
  private String generateActionOnlyOneRole(Vector<ActionType> userTrigActs) {
	  String actions = "";
      for (int i = 0; i < userTrigActs.size(); i++) {
          ActionType tempAct = userTrigActs.elementAt(i);
          if (i > 0) { // not on first element
            actions += "else ";
          }
          actions += "if(action instanceof "
              + CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName()) + 
              "Action)\n{\n";

          // go through all the actions user triggers:
          boolean putElse = false;
          Vector<ActionTypeTrigger> allTrigs = tempAct.getAllTriggers();
          for (int j = 0; j < allTrigs.size(); j++) {
            ActionTypeTrigger outerTrig = allTrigs.elementAt(j);
            if ((outerTrig instanceof UserActionTypeTrigger)
                && (outerTrig.getTriggerText() != null)
                && (outerTrig.getTriggerText().length() > 0)) {
              if (putElse) {
                actions += "else ";
              } else {
                putElse = true;
              }
              actions += "if(menuText.equals(\""
                  + ((UserActionTypeTrigger) outerTrig).getMenuText() + "\"))\n{\n";
              actions += "emp.setOverheadText(\"" + outerTrig.getTriggerText()
                  + "\");\n}\n";
            }
          }

          // go through all employee participants:
          Vector<ActionTypeParticipant> allParts = tempAct.getAllParticipants();
          int numEmpParts = 0;
          for (int j = 0; j < allParts.size(); j++) {
            ActionTypeParticipant part = allParts.elementAt(j);
            if (part.getSimSEObjectTypeType() == SimSEObjectTypeTypes.EMPLOYEE) { 
            	// employee participant
              if (numEmpParts > 0) { // not on first element
                actions += "else ";
              }
              numEmpParts++;
              actions += "if(partName.equals(\"" + part.getName() + "\"))\n{\n";
              actions += "((" + CodeGeneratorUtils.getUpperCaseLeading(
              		tempAct.getName()) + "Action)action).add" + part.getName() + 
              		"(emp);\n";

              // go through all trigger rules and execute rules that are to be
              // executed on each join:
              Vector<Rule> rules = tempAct.getAllTriggerRules();
              for (int k = 0; k < rules.size(); k++) {
                Rule tempRule = rules.elementAt(k);
                if (tempRule.getExecuteOnJoins() == true) {
                  actions += "ruleExec.update(gui, RuleExecutor.UPDATE_ONE, "
                      + "\"" + tempRule.getName() + "\", action);\n";
                }
              }
              actions += "}\n";
            }
          }
          actions += "}\n";
        }
      
      return actions;
  }
}