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

import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Vector;

import javax.lang.model.element.Modifier;
import javax.swing.JOptionPane;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
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
      // go through each action type and generate code for it:
      LinkedList<CodeBlock> actionTypes1 = new LinkedList<CodeBlock>();
      for (int j = 0; j < userTrigActs.size(); j++) {
        ActionType act = userTrigActs.elementAt(j);
        ClassName tempClass = ClassName.get("simse.adts.actions",
        		CodeGeneratorUtils.getUpperCaseLeading(act.getName()) + "Action");
        CodeBlock tempBlock = CodeBlock.builder()
        		.beginControlFlow("if (tempAct instanceof $T)", tempClass)
        		.addStatement("actionName = $S", CodeGeneratorUtils.getUpperCaseLeading(act.getName()))
        		.endControlFlow()
        		.build();
        
        actionTypes1.add(tempBlock);
      }
      
      CodeBlock actionTypes1_results = CodeBlock.join(actionTypes1, " else ");
      
      LinkedList<CodeBlock> actionTypes2 = new LinkedList<CodeBlock>();
      for (int j = 0; j < userTrigActs.size(); j++) {
          ActionType act = userTrigActs.elementAt(j);
          ClassName tempClass1 = ClassName.get("simse.adts.actions",
          		CodeGeneratorUtils.getUpperCaseLeading(act.getName()) + "Action");
          
          Vector<ActionTypeParticipant> parts = act.getAllParticipants();
          LinkedList<CodeBlock> actionTypes2_1 = new LinkedList<CodeBlock>();
          for (int k = 0; k < parts.size(); k++) {
            ActionTypeParticipant tempPart = parts.elementAt(k);
            String metaTypeName = CodeGeneratorUtils.getUpperCaseLeading(
            		SimSEObjectTypeTypes.getText(tempPart.getSimSEObjectTypeType()));
            ClassName tempClass2 = ClassName.get("simse.adts.objects", metaTypeName);
            TypeName tempType = ParameterizedTypeName.get(vector, tempClass2);

            Vector<SimSEObjectType> ssObjTypes = tempPart.getAllSimSEObjectTypes();
            LinkedList<CodeBlock> actionTypes2_1_1 = new LinkedList<CodeBlock>();
            for (int m = 0; m < ssObjTypes.size(); m++) {
                SimSEObjectType tempInternalType = ssObjTypes.elementAt(m);
                ClassName tempClass3 = ClassName.get("simse.adts.objects", CodeGeneratorUtils.
                		getUpperCaseLeading(tempInternalType.getName()));
                
                CodeBlock tempBlock3 = CodeBlock.builder()
                		.beginControlFlow("if (a instanceof $T)", tempClass3)
                		.addStatement("label.append($S + (($T) a).get$L() + $S)",
                				tempInternalType.getName() + "(", tempClass3, 
                				 CodeGeneratorUtils.getUpperCaseLeading(tempInternalType.getKey().getName()),
                				 ")")
                		.build();
                actionTypes2_1_1.add(tempBlock3);
              }
            
            CodeBlock actionTypes2_1_1_result = CodeBlock.join(actionTypes2_1_1, " else ");
            
            CodeBlock tempBlock2 = CodeBlock.builder()
            		.addStatement("label.append($S)", tempPart.getName() + "(s): ")
            		.addStatement("$T all$Ls = act.getAll$Ls()", tempType,
            				tempPart.getName(), tempPart.getName())
            		.beginControlFlow("for (int j = 0; j < all$Ls.size; j++)", tempPart.getName())
            		.beginControlFlow("if (j > 0)")
            		.addStatement("label.append($S)", ", ")
            		.endControlFlow()
            		.addStatement("$T a = all$Ls.elementAt(j)", tempClass2, tempPart.getName())
            		.add(actionTypes2_1_1_result)
            		.endControlFlow()
            		.endControlFlow()
            		.build();
            
            actionTypes2_1.add(tempBlock2);
          }
          
          CodeBlock actionTypes2_1_results = CodeBlock.join(actionTypes2_1, "label.append(\"; <br>\");");

          CodeBlock tempBlock1 = CodeBlock.builder()
        		  .beginControlFlow("if (tempAct instanceof $T)", tempClass1)
        		  .beginControlFlow("for(int i=0; i<$N.size(); i++)", "actions")
        		  .addStatement("$T act = ($T) $N.elementAt(i)", tempClass1, tempClass1, "actions")
        		  .addStatement("$T label = new $T($S)", StringBuffer.class, StringBuffer.class,
        				  "<html>")
        		  .add(actionTypes2_1_results)
        		  .addStatement("label.append($S)", "</HTML>")
        		  .addStatement("$T tempPane = new $T()", BorderPane.class, BorderPane.class)
        		  .addStatement("$T tempRadioButton = new $T(label.toString())",
        				  RadioButton.class, RadioButton.class)
        		  .addStatement("tempRadioButton.setToggleGroup($N)", "radioButtonGroup")
        		  .addStatement("tempPane.setLeft(tempRadioButton)")
        		  .addStatement("$N.add(tempRadioButton)", "radioButtons")
        		  .addStatement("middlePane.getChildren().add(tempPane)")
        		  .endControlFlow()
        		  .endControlFlow()
        		  .build();
          
          actionTypes2.add(tempBlock1);
        }
      
      CodeBlock actionTypes2_results = CodeBlock.join(actionTypes2, " else ");
	  
	  MethodSpec joinConstructor = MethodSpec.constructorBuilder()
			  .addModifiers(Modifier.PUBLIC)
			  .addParameter(Stage.class, "parent")
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
			  .addStatement("$N = new $T()", "radioButtonsGroup", ToggleGroup.class)
			  .addStatement("setTitle($S)", "Join Action")
			  .addStatement("$T mainPane = new $T()", VBox.class, VBox.class)
			  .addStatement("$T topPane = new $T", Pane.class, Pane.class)
			  .addStatement("$T actionName = new $T()", String.class, String.class)
			  .addStatement("$T tempAct = $N.elementAt(0)", actionClass, "actions")
			  .addCode(actionTypes1_results)
			  .addStatement("topPane.getChildren().add(new $T($S + actionName + $S))",
					  Label.class, "Choose which ", " Action to join:")
			  .addStatement("$T middlePane = new $T()", GridPane.class, GridPane.class)
			  .addCode(actionTypes2_results)
			  .addStatement("$T bottomPane = new $T()", Pane.class, Pane.class)
			  .addStatement("$N = new $T($S)", "okButton", Button.class, "OK")
			  .addStatement("$N.addEventHandler($T.MOUSE_CLICKED, this)", "okButton", MouseEvent.class)
			  .addStatement("bottomPane.getChildren.add($N)", "okButton")
			  .addStatement("$N = new $T($S)", "cancelButton", Button.class, "Cancel")
			  .addStatement("$N.addEventHandler($T.MOUSE_CLICKED, this)", "cancelButton", MouseEvent.class)
			  .addStatement("bottomPane.getChildren.add($N)", "cancelButton")
			  .addStatement("mainPane.getChildren.addAll(topPane, middlePane, bottomPane)")
			  .addStatement("$T ownerLoc = new $T(parent.getX(), parent.getY()", Point2D.class, Point2D.class)
			  .addStatement("$T thisLoc = new $T((ownerLoc.getX() + (parent.getWidth() / 2) - (this.getWidth() / 2)),"
			  		+ "(ownerLoc.getY() + (parent.getHeight() / 2) - (this.getHeight() / 2)))", Point2D.class, Point2D.class)
			  .addStatement("this.setX(thisLoc.getX())")
			  .addStatement("this.setY(thisLoc.getY())")
			  .beginControlFlow("if ($N.size() == 1)", "radioButtons")
			  .addStatement("onlyOneChoice(parent)")
			  .nextControlFlow("else ")
			  .addStatement("show()")
			  .endControlFlow()
			  .build();

      LinkedList<CodeBlock> actionTypes3 = new LinkedList<CodeBlock>();
      for (int i = 0; i < userTrigActs.size(); i++) {
          ActionType tempAct = userTrigActs.elementAt(i);
          ClassName tempClass = ClassName.get("simse.adts.actions",
        		  CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName()) + "Action");
          
          LinkedList<CodeBlock> actionTypes3_1 = new LinkedList<CodeBlock>();
          Vector<ActionTypeParticipant> allParts = tempAct.getAllParticipants();
          for (int j = 0; j < allParts.size(); j++) {
            ActionTypeParticipant part = allParts.elementAt(j);
            if (part.getSimSEObjectTypeType() == SimSEObjectTypeTypes.EMPLOYEE) {
            	String max;
                if (part.getQuantity().isMaxValBoundless()) {
                    max = "999999";
                  } else { // max val has a value
                    max = part.getQuantity().getMaxVal().toString();
                  }
                
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
                LinkedList<CodeBlock> actionTypes3_1_1 = new LinkedList<CodeBlock>();
                for (int k = 0; k < userTrigsTemp.size(); k++) {
                  UserActionTypeTrigger userTrig = userTrigsTemp.elementAt(k);
                  
                  // go through all allowable types:

                  LinkedList<CodeBlock> actionTypes3_1_1_1 = new LinkedList<CodeBlock>();
                  Vector<SimSEObjectType> types = part.getAllSimSEObjectTypes();
                  for (int m = 0; m < types.size(); m++) {
                    SimSEObjectType type = types.elementAt(m);
                    ClassName tempClass2 = ClassName.get("simse.adts.objects",
                    		CodeGeneratorUtils.getUpperCaseLeading(type.getName()));

                    String attributeConstraints = "if ((emp instanceof $T)";
                    
                    // go through all attribute constraints:
                    ActionTypeParticipantAttributeConstraint[] attConstraints = 
                    	userTrig.getParticipantTrigger(part.getName()).getConstraint(
                            type.getName()).getAllAttributeConstraints();
                    for (int n = 0; n < attConstraints.length; n++) {
                      ActionTypeParticipantAttributeConstraint attConst = 
                      	attConstraints[n];
                      if (attConst.isConstrained()) {
                        attributeConstraints += " && ((("
                                + CodeGeneratorUtils.getUpperCaseLeading(
                                		type.getName()) + ")emp).get" + 
                                		CodeGeneratorUtils.getUpperCaseLeading(
                                				attConst.getAttribute().getName()) + "() ";
                        if (attConst.getAttribute().getType() == 
                        	AttributeTypes.STRING) {
                        	attributeConstraints += ".equals(" + "\""
                              + attConst.getValue().toString() + "\")";
                        } else {
                          if (attConst.getGuard().equals(AttributeGuard.EQUALS)) {
                        	  attributeConstraints += "== ";
                          } else {
                        	  attributeConstraints += attConst.getGuard() + " ";
                          }
                          attributeConstraints += attConst.getValue().toString();
                        }
                        attributeConstraints += ")";
                      }
                    }
                    
                    attributeConstraints += ")";
                    
                    CodeBlock tempBlock4 = CodeBlock.builder()
                    		.beginControlFlow(attributeConstraints, tempClass2)
                    		.addStatement("participantNames.add($S)",
                    				CodeGeneratorUtils.getUpperCaseLeading(part.getName()))
                    		.endControlFlow()
                    		.build();
                    
                    actionTypes3_1_1_1.add(tempBlock4);
                  }
                  
                  CodeBlock actionTypes3_1_1_1_result = CodeBlock.join(actionTypes3_1_1_1, " else ");
                  
                  CodeBlock tempBlock3 = CodeBlock.builder()
                		  .beginControlFlow("if ($N.equals($S))", "menuText", userTrig.getMenuText())
                		  .add(actionTypes3_1_1_1_result)
                		  .endControlFlow()
                		  .build();
                  
                  actionTypes3_1_1.add(tempBlock3);
                }
            	
                CodeBlock actionTypes3_1_1_result = CodeBlock.join(actionTypes3_1_1, " else ");
                
            	CodeBlock tempBlock2 = CodeBlock.builder()
            			.addStatement("$T all$Ls = (($T) tempAct).getAll$Ls()",
            					Vector.class, part.getName(), tempClass, part.getName())
            			.beginControlFlow("if((all$Ls.contains($N) == false)"
            					+ "&& (all$Ls.size() < $L))", part.getName(), "emp",
            					part.getName(), max)
            			.add(actionTypes3_1_1_result)
            			.endControlFlow()
            			.build();
            	
            	actionTypes3_1.add(tempBlock2);
            }
          }
          
          CodeBlock actionTypes3_1_result = CodeBlock.join(actionTypes3_1, "");
          
          CodeBlock tempBlock = CodeBlock.builder()
        		  .beginControlFlow("if (tempAct instanceof $T)", tempClass)
        		  .add(actionTypes3_1_result)
        		  .endControlFlow()
        		  .build();
          
          actionTypes3.add(tempBlock);
        }
      
      CodeBlock actionTypes3_result = CodeBlock.join(actionTypes3, " else ");
	  
	  MethodSpec handle = MethodSpec.methodBuilder("handle")
			  .addModifiers(Modifier.PUBLIC)
			  .returns(void.class)
			  .addParameter(MouseEvent.class, "evt")
			  .addAnnotation(Override.class)
			  .addStatement("$T source = evt.getSource()", Object.class)
			  .beginControlFlow("if (source == cancelButton)")
			  .addStatement("close()")
			  .nextControlFlow("else if (source == $N)", "okButton")
			  .addStatement("$T anySelected = false", boolean.class)
			  .beginControlFlow("for (int i = 0; i < $N.size(); i++)", "radioButtons")
			  .addStatement("$T tempRButt = $N.elementAt(i)", RadioButton.class, "radioButtons")
			  .beginControlFlow("if (tempRButt.isSelected()")
			  .addStatement("anySelected = true")
			  .addStatement("break")
			  .endControlFlow()
			  .endControlFlow()
			  .beginControlFlow("if (!anySelected)")
			  .addStatement("$T alert = new $T($T.WARNING, $S)", Alert.class, Alert.class,
					  AlertType.class, "You must choose at least one action")
			  .addStatement("alert.setTitle($S)", "Invalid Input")
			  .addStatement("alert.show()")
			  .nextControlFlow(" else ")
			  .beginControlFlow("for(int i=0; i<$N.size(); i++)", "radioButtons")
			  .addStatement("$T rButt = $N.elementAt(i)", RadioButton.class, "radioButtons")
			  .beginControlFlow("if (rButt.isSelected()")
			  .addStatement("$T tempAct = $N.elementAt(i)", actionClass, "actions")
			  .addStatement("$T participantNames = new $T()", stringVector, stringVector)
			  .addCode(actionTypes3_result)
			  .addStatement("new $T($N, participantNames, $N, tempAct, $N, $N)",
					  roleDialog, "gui", "emp", "menuText", "ruleExec")
			  .addStatement("close()")
			  .addStatement("break")
			  .endControlFlow()
			  .endControlFlow()
			  .endControlFlow()
			  .endControlFlow()
			  .build();
	  
	  // go through all action types w/ user triggers:
      LinkedList<CodeBlock> actionTypes4 = new LinkedList<CodeBlock>();
      for (int i = 0; i < userTrigActs.size(); i++) {
        ActionType tempAct = userTrigActs.elementAt(i);
        ClassName tempClass = ClassName.get("simse.adts.actions",
        		CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName()) + "Action");
        
     // go through all employee participants:
        Vector<ActionTypeParticipant> allParts = tempAct.getAllParticipants();
        LinkedList<CodeBlock> actionTypes4_1 = new LinkedList<CodeBlock>();
        for (int j = 0; j < allParts.size(); j++) {
          ActionTypeParticipant part = allParts.elementAt(j);
          if (part.getSimSEObjectTypeType() == SimSEObjectTypeTypes.EMPLOYEE) {
        	  String max;
        	  if (part.getQuantity().isMaxValBoundless()) {
                  max = "999999";
                } else { // max val has a value
                  max = part.getQuantity().getMaxVal().toString();
                }
        	  
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
              LinkedList<CodeBlock> actionTypes4_1_1 = new LinkedList<CodeBlock>();
              for (int k = 0; k < userTrigsTemp.size(); k++) {
                UserActionTypeTrigger userTrig = userTrigsTemp.elementAt(k);
                
             // go through all allowable types:
                Vector<SimSEObjectType> types = part.getAllSimSEObjectTypes();
                LinkedList<CodeBlock> actionTypes4_1_1_1 = new LinkedList<CodeBlock>();
                for (int m = 0; m < types.size(); m++) {
                  SimSEObjectType type = types.elementAt(m);
                  
                  ClassName tempClass2 = ClassName.get("simse.adts.objects",
                  		CodeGeneratorUtils.getUpperCaseLeading(type.getName()));
                  
                  
                  String attributeConstraints = "if((emp instanceof $T)";

                  // go through all attribute constraints:
                  ActionTypeParticipantAttributeConstraint[] attConstraints = 
                  	userTrig.getParticipantTrigger(part.getName()).getConstraint(
                          type.getName()).getAllAttributeConstraints();
                  for (int n = 0; n < attConstraints.length; n++) {
                    ActionTypeParticipantAttributeConstraint attConst = 
                    	attConstraints[n];
                    if (attConst.isConstrained()) {
                      attributeConstraints += " && ((("
                              + CodeGeneratorUtils.getUpperCaseLeading(
                              		type.getName()) + ")emp).get" + 
                              		CodeGeneratorUtils.getUpperCaseLeading(
                              				attConst.getAttribute().getName()) + "() ";
                      if (attConst.getAttribute().getType() == 
                      	AttributeTypes.STRING) {
                    	  attributeConstraints += ".equals(" + "\""
                            + attConst.getValue().toString() + "\")";
                      } else {
                        if (attConst.getGuard().equals(AttributeGuard.EQUALS)) {
                          attributeConstraints += "== ";
                        } else {
                        	attributeConstraints += attConst.getGuard() + " ";
                        }
                        attributeConstraints += attConst.getValue().toString();
                      }
                      attributeConstraints += ")";
                    }
                  }
                  
                  attributeConstraints += ")";
                  
                  CodeBlock tempBlock4 = CodeBlock.builder()
                  		.beginControlFlow(attributeConstraints, tempClass2)
                  		.addStatement("participantNames.add($S)",
                  				CodeGeneratorUtils.getUpperCaseLeading(part.getName()))
                  		.endControlFlow()
                  		.build();
                  
                  actionTypes4_1_1_1.add(tempBlock4);
                }
                
                CodeBlock actionTypes4_1_1_1_result = CodeBlock.join(actionTypes4_1_1_1, " else ");
                
                
                CodeBlock tempBlock3 = CodeBlock.builder()
                		.beginControlFlow("if ($N.equals($S))", "menuText", userTrig.getMenuText())
                		.add(actionTypes4_1_1_1_result)
                		.endControlFlow()
                		.build();
                
                actionTypes4_1_1.add(tempBlock3);
              }
              
              CodeBlock actionTypes4_1_1_result = CodeBlock.join(actionTypes4_1_1, " else ");
        	  
        	  CodeBlock tempBlock2 = CodeBlock.builder()
        			  .addStatement("$T all$Ls = (($T) tempAct).getAll$Ls()",
        					  Vector.class, part.getName(), tempClass, part.getName())
        			  .beginControlFlow("if ((all$Ls.contains(emp) == false) && "
        			  		+ "(all$Ls.size() < $L))", part.getName(), part.getName(), max)
        			  .add(actionTypes4_1_1_result)
        			  .endControlFlow()
        			  .build();
              
              actionTypes4_1.add(tempBlock2);
          }
        }
        
        CodeBlock actionTypes4_1_result = CodeBlock.join(actionTypes4_1, "");
        
        CodeBlock tempBlock = CodeBlock.builder()
        		.beginControlFlow("if (tempAct instanceof $T)", tempClass)
        		.add(actionTypes4_1_result)
        		.endControlFlow()
        		.build();
        
        actionTypes4.add(tempBlock);
      }
      
      CodeBlock actionTypes4_result = CodeBlock.join(actionTypes4, " else ");
	  
	  MethodSpec onlyOneChoice = MethodSpec.methodBuilder("onlyOneChoice")
			  .addModifiers(Modifier.PRIVATE)
			  .returns(void.class)
			  .addParameter(Stage.class, "owner")
			  .beginControlFlow("for(int i=0; i<$N.size(); i++)", "radioButtons")
			  .addStatement("$T rButt = $N.elementAt(i)", RadioButton.class, "radioButtons")
			  .addStatement("$T tempAct = $N.elementAt(i)", actionClass, "actions")
			  .addStatement("$T participantNames = new $T()", stringVector, stringVector)
			  .addCode(actionTypes4_result)
			  .addStatement("new $T(owner, participantNames, $N, tempAct, $N, $N)",
					  roleDialog, "emp", "menuText", "ruleExec")
			  .addStatement("close()")
			  .addStatement("break")
			  .endControlFlow()
			  .build();
	  
	  TypeSpec joinDialog = TypeSpec.classBuilder("ChooseActionToJoinDialog")
			  .superclass(Dialog.class)
			  .addSuperinterface(mouseHandler)
			  .addField(Stage.class, "gui", Modifier.PRIVATE)
			  .addField(actionsVector, "actions", Modifier.PRIVATE)
			  .addField(stateClass, "state", Modifier.PRIVATE)
			  .addField(employeeClass, "emp", Modifier.PRIVATE)
			  .addField(radioVector, "radioButtons", Modifier.PRIVATE)
			  .addField(ToggleGroup.class, "radioButtonGroup", Modifier.PRIVATE)
			  .addField(Button.class, "okButton", Modifier.PRIVATE)
			  .addField(Button.class, "cancelButton", Modifier.PRIVATE)
			  .addField(String.class, "menuText", Modifier.PRIVATE)
			  .addField(ruleExecClass, "ruleExec", Modifier.PRIVATE)
			  .addMethod(joinConstructor)
			  .addMethod(handle)
			  .addMethod(onlyOneChoice)
			  .build();
	  
	  JavaFile javaFile = JavaFile.builder("ChooseActionToJoinDialog", joinDialog)
			    .build();
	  
    try {
      catjdFile = new File(directory,
          ("simse\\logic\\dialogs\\ChooseActionToJoinDialog.java"));
      if (catjdFile.exists()) {
        catjdFile.delete(); // delete old version of file
      }
      
      javaFile.writeTo(catjdFile);
      
    } catch (IOException e) {
      JOptionPane.showMessageDialog(null, ("Error writing file "
          + catjdFile.getPath() + ": " + e.toString()), "File IO Error",
          JOptionPane.WARNING_MESSAGE);
    }
  }
}