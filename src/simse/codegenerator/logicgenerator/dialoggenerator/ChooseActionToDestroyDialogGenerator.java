/*
 * This class is responsible for generating all of the code for the logic's
 * ChooseActionToDestroyDialog component
 */

package simse.codegenerator.logicgenerator.dialoggenerator;

import simse.codegenerator.CodeGeneratorConstants;
import simse.codegenerator.CodeGeneratorUtils;

import simse.modelbuilder.actionbuilder.ActionType;
import simse.modelbuilder.actionbuilder.ActionTypeDestroyer;
import simse.modelbuilder.actionbuilder.ActionTypeParticipant;
import simse.modelbuilder.actionbuilder.ActionTypeParticipantAttributeConstraint;
import simse.modelbuilder.actionbuilder.ActionTypeParticipantConstraint;
import simse.modelbuilder.actionbuilder.ActionTypeParticipantDestroyer;
import simse.modelbuilder.actionbuilder.DefinedActionTypes;
import simse.modelbuilder.actionbuilder.UserActionTypeDestroyer;
import simse.modelbuilder.objectbuilder.AttributeTypes;
import simse.modelbuilder.objectbuilder.SimSEObjectType;
import simse.modelbuilder.objectbuilder.SimSEObjectTypeTypes;
import simse.modelbuilder.rulebuilder.Rule;

import java.awt.Button;
import java.awt.Checkbox;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Vector;

import javax.lang.model.element.Modifier;
import javax.swing.JOptionPane;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class ChooseActionToDestroyDialogGenerator implements
    CodeGeneratorConstants {
  private File directory; // directory to generate into
  private File catddFile; // file to generate
  private DefinedActionTypes actTypes; // holds all of the defined action types

  public ChooseActionToDestroyDialogGenerator(DefinedActionTypes actTypes, 
  		File directory) {
    this.directory = directory;
    this.actTypes = actTypes;
  }

  public void generate() {
	  ClassName eventHandler = ClassName.get("javafx.event", "EventHandler");
	  ClassName mouseEvent = ClassName.get("javafx.scene.input", "MouseEvent");
	  ClassName vector = ClassName.get("java.util", "Vector");
	  ClassName stateClass = ClassName.get("simse.state", "State");
	  ClassName ruleExecClass = ClassName.get("simse.logic", "RuleExecutor");
	  ClassName employeeClass = ClassName.get("simse.adts.objects", "Employee");
	  ClassName customerClass = ClassName.get("simse.adts.objects", "Customer");
	  ClassName actionClass = ClassName.get("simse.adts.actions", "Action");
	  ClassName stageClass = ClassName.get("javafx.stage", "Stage");
	  ClassName checkboxClass = ClassName.get("javafx.scene.control", "CheckBox");
	  ClassName buttonClass = ClassName.get("javafx.scene.control", "Button");
	  TypeName mouseHandler = ParameterizedTypeName.get(eventHandler, mouseEvent);
	  TypeName actionWildcard = WildcardTypeName.subtypeOf(actionClass);
	  TypeName actionsType = ParameterizedTypeName.get(vector, actionWildcard);
	  TypeName checkboxType = ParameterizedTypeName.get(vector, checkboxClass);
	  
	  Vector<ActionType> userDestActs = new Vector<ActionType>();
      Vector<ActionType> allActs = actTypes.getAllActionTypes();
      for (int j = 0; j < allActs.size(); j++) {
        ActionType userAct = allActs.elementAt(j);
        Vector<ActionTypeDestroyer> allDests = userAct.getAllDestroyers();
        for (int k = 0; k < allDests.size(); k++) {
          ActionTypeDestroyer tempDest = allDests.elementAt(k);
          if (tempDest instanceof UserActionTypeDestroyer) {
            userDestActs.add(userAct);
            break;
          }
        }
      }
      
      LinkedList<CodeBlock> actionTypes = new LinkedList<CodeBlock>();
      for (int j = 0; j < userDestActs.size(); j++) {
          ActionType act = userDestActs.elementAt(j);
          ClassName tempClass = ClassName.get("simse.adts.actions", CodeGeneratorUtils.getUpperCaseLeading(act.getName()) + "Action");
    	  actionTypes.add(CodeBlock.builder()
    			  .beginControlFlow("if (tempAct instanceof $TAction)", tempClass)
    			  .addStatement("actionName = $S", CodeGeneratorUtils.getUpperCaseLeading(act.getName()))
    			  .endControlFlow()
    			  .build());
      }
      
      CodeBlock actionTypeBlock = CodeBlock.join(actionTypes, " else ");
      
      LinkedList<CodeBlock> actionTypes2 = new LinkedList<CodeBlock>();
      for (int j = 0; j < userDestActs.size(); j++) {
    	  ActionType act = userDestActs.elementAt(j);
          ClassName tempClass = ClassName.get("simse.adts.actions", CodeGeneratorUtils.getUpperCaseLeading(act.getName()) + "Action");
          
          LinkedList<CodeBlock> actionTypes2_1 = new LinkedList<CodeBlock>();
          Vector<ActionTypeParticipant> parts = act.getAllParticipants();
          for (int k = 0; k < parts.size(); k++) {
        	  ActionTypeParticipant tempPart = parts.elementAt(k);
        	  ClassName tempClass2 = ClassName.get("simse.adts.objects",
        			  SimSEObjectTypeTypes.getText(tempPart.getSimSEObjectTypeType()));
        	  TypeName tempType = ParameterizedTypeName.get(vector, tempClass2);
        	  
              // go through all allowable SimSEObjectTypes for this participant:
        	  LinkedList<CodeBlock> actionTypes2_2 = new LinkedList<CodeBlock>();
              Vector<SimSEObjectType> ssObjTypes = tempPart.getAllSimSEObjectTypes();
              for (int m = 0; m < ssObjTypes.size(); m++) {
                SimSEObjectType tempType2 = ssObjTypes.elementAt(m);
                ClassName tempClass3 = ClassName.get("simse.adts.objects",
                		CodeGeneratorUtils.getUpperCaseLeading(tempType2.getName()));
                
                CodeBlock tempBlock3 = CodeBlock.builder()
                		.beginControlFlow("if (a instanceof $T)", tempClass3)
                		.addStatement("label.append($S + (($T) a).getName() + $S)",
                				CodeGeneratorUtils.getUpperCaseLeading(tempType2.getName()) + "(",
                				tempClass3, ")")
                		.endControlFlow()
                		.build();
                
                actionTypes2_2.add(tempBlock3);
              }
              
              CodeBlock tempBlock3_result = CodeBlock.join(actionTypes2_2, "else ");
        	  
        	  CodeBlock tempBlock2 = CodeBlock.builder()
        			  .addStatement("label.append($S)", tempPart.getName() + "(s); ")
        			  .addStatement("$T all$Ls = act.getAll$Ls()", tempType, tempPart.getName(),
        					  tempPart.getName())
        			  .beginControlFlow("for (int j = 0; j < all$Ls.size(); j++)", tempPart.getName())
        			  .beginControlFlow("if (j > 0)")
        			  .addStatement("label.append($S)", ", ")
        			  .endControlFlow()
        			  .addStatement("$T a = all$Ls.elementAt(j)", tempClass2, tempPart.getName())
        			  .add(tempBlock3_result)
        			  .endControlFlow()
        			  .build();
        	  
        	  actionTypes2_1.add(tempBlock2);
          }
          
          CodeBlock tempBlock3_result = CodeBlock.join(actionTypes2_1, "label.append(\"; \");\n");
          
          CodeBlock tempBlock = CodeBlock.builder()
        		  .beginControlFlow("if (tempAct instanceof $T)", tempClass)
        		  .beginControlFlow("for (int i = 0; i < $N.size(); i++)", "actions")
        		  .addStatement("$T act = ($T) $N.elementAt(i)", tempClass, tempClass, "actions")
        		  .beginControlFlow("if (act.getAllParticipants().contains(emp))")
        		  .addStatement("$T label = new $T()", StringBuffer.class, StringBuffer.class)
        		  .add(tempBlock3_result)
        		  .addStatement("$T tempPane = new $T()", BorderPane.class, BorderPane.class)
        		  .addStatement("$T tempCheckBox = new $T(label.toString())", CheckBox.class,
        				  CheckBox.class)
        		  .addStatement("tempPane.setLeft(tempCheckBox)")
        		  .addStatement("$N.add(tempCheckBox)", "checkBoxes")
        		  .addStatement("middlePane.getChildren().add(tempPane)")
        		  .endControlFlow()
        		  .endControlFlow()
        		  .build();
          
          actionTypes2.add(tempBlock);
        }
      
      CodeBlock actionType2Block = CodeBlock.join(actionTypes2, " else ");
	  
	  MethodSpec destroyConstructor = MethodSpec.constructorBuilder()
			  .addModifiers(Modifier.PUBLIC)
			  .addParameter(stageClass, "parent")
			  .addParameter(actionsType, "acts")
			  .addParameter(stateClass, "s")
			  .addParameter(employeeClass, "e")
			  .addParameter(ruleExecClass, "r")
			  .addParameter(String.class, "mText")
			  .addStatement("$N = acts", "actions")
			  .addStatement("$N = s", "state")
			  .addStatement("$N = r", "ruleExec")
			  .addStatement("$N = parent", "gui")
			  .addStatement("$N = e", "emp")
			  .addStatement("$N = mText", "menuText")
			  .addStatement("$N = new $T()", "checkBoxes", checkboxType)
			  .addStatement("setTitle($S)", "Stop Action(s)")
			  .addStatement("$T mainPane = new $T()", VBox.class, VBox.class)
			  .addStatement("$T actionName = new $T()", String.class, String.class)
			  .addStatement("$T tempAct = $N.elementAt(0)", actionClass, "actions")
			  .addCode(actionTypeBlock)
			  .addStatement("topPane.getChildren().add(new $T($S + actionName + $S))",
					  Label.class, "Choose which ", " Action to stop:")
			  .addStatement("$T middlePane = new $T()", GridPane.class, GridPane.class)
			  .addCode(actionType2Block)
			  .addStatement("$T bottomPane = new $T()", Pane.class, Pane.class)
			  .addStatement("$N = new $T($S)", "okButton", buttonClass, "OK")
			  .addStatement("$N.addEventHandler($T.MOUSE_CLICKED, this)", "okButton", mouseEvent)
			  .addStatement("bottomPane.getChildren().add($N)", "okButton")
			  .addStatement("$N = new $T($S)", "cancelButton", buttonClass, "Cancel")
			  .addStatement("$N.addEventHandler($T.MOUSE_CLICKED, this)", "cancelButton", mouseEvent)
			  .addStatement("bottomPane.getChildren().add($N)", "cancelButton")
			  .addStatement("mainPane.getChildren().addAll(topPane, middlePane, bottomPane)")
			  .addStatement("$T ownerLoc = new $T(parent.getX(), parent.getY()", Point2D.class, Point2D.class)
			  .addStatement("$T thisLoc = new $T((ownerLoc.getX() + (parent.getWidth() / 2) - (this.getWidth() / 2)),"
			  		+ "(ownerLoc.getY() + (parent.getHeight() / 2) - (this.getHeight() / 2)))", Point2D.class, Point2D.class)
			  .addStatement("this.setX(thisLoc.getX())")
			  .addStatement("this.setY(thisLoc.getY())")
			  .addStatement("show()")
			  .build();
	  
	  LinkedList<CodeBlock> actionTypes3 = new LinkedList<CodeBlock>();
	  for (int i = 0; i < userDestActs.size(); i++) {
	        ActionType tempAct = userDestActs.elementAt(i);
	        ClassName tempClass = ClassName.get("simse.adts.actions", 
	        		CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName()) + "Action");
	        String tempActName = tempAct.getName().toLowerCase() + "Act";
	        
	        Vector<ActionTypeParticipant> parts = tempAct.getAllParticipants();
	        LinkedList<CodeBlock> actionTypes3_1 = new LinkedList<CodeBlock>();
	        for (int j = 0; j < parts.size(); j++) {
	          ActionTypeParticipant part = parts.elementAt(j);
	          if (part.getSimSEObjectTypeType() == SimSEObjectTypeTypes.EMPLOYEE) {
		          CodeBlock tempBlock2 = CodeBlock.builder()
		        		  .addStatement("$L.remove$L(emp)", tempActName, part.getName())
		        		  .build();
		          actionTypes3_1.add(tempBlock2);
	          }
	        }
	        
	        CodeBlock tempBlock2_result = CodeBlock.join(actionTypes3_1, "");
	        
	        Vector<ActionTypeDestroyer> dests = tempAct.getAllDestroyers();
	        LinkedList<CodeBlock> actionTypes3_2 = new LinkedList<CodeBlock>();
	        for (int j = 0; j < dests.size(); j++) {
	          ActionTypeDestroyer tempDest = dests.elementAt(j);
	          if ((tempDest instanceof UserActionTypeDestroyer)
	              && (tempDest.getDestroyerText() != null)
	              && (tempDest.getDestroyerText().length() > 0)) {
	        	  
	        	  Vector<Rule> destRules = tempAct.getAllDestroyerRules();
	        	  LinkedList<CodeBlock> actionTypes3_2_1 = new LinkedList<CodeBlock>();
		          for (int k = 0; k < destRules.size(); k++) {
		              Rule dRule = destRules.elementAt(k);
		              if (dRule.getExecuteOnJoins() == true) {
		            	  CodeBlock tempBlock3_1 = CodeBlock.builder()
		            			  .addStatement("$N.update($N, $T.UPDATE_ONE, $S, $S)",
		            					  "ruleExec", "gui", ruleExecClass, dRule.getName(),
		            					  tempActName)
		            			  .build();
		            	  actionTypes3_2_1.add(tempBlock3_1);
		              }
		          }
		          
		        CodeBlock tempBlock3_1_result = CodeBlock.join(actionTypes3_2_1, "");
	        	  
	            CodeBlock tempBlock3 = CodeBlock.builder()
	            		.beginControlFlow("if ($N.equals($S))", "menuText",
	            				((UserActionTypeDestroyer) tempDest).getMenuText())
	            		.addStatement("emp.setOverheadText($S)", tempDest.getDestroyerText())
	            		.add(tempBlock3_1_result)
	            		.endControlFlow()
	            		.build();
	            
	            actionTypes3_2.add(tempBlock3);
	          }
	        }
	        
	        CodeBlock tempBlock3_result = CodeBlock.join(actionTypes3_2, " else ");
	        
	        LinkedList<CodeBlock> actionTypes3_3 = new LinkedList<CodeBlock>();
	        for (int j = 0; j < parts.size(); j++) {
		          ActionTypeParticipant part = parts.elementAt(j);
		          ClassName ssObject = ClassName.get("simse.adts.objects", "SSObject");
		          TypeName ssObjectType = ParameterizedTypeName.get(vector, ssObject);
		          
		          String minVal = "";
		          if (part.getQuantity().isMinValBoundless()) {
			            minVal = "0";
			      } else {
			            minVal = part.getQuantity().getMinVal().toString();
		          }
		          
		          LinkedList<CodeBlock> actionTypes3_3_1 = new LinkedList<CodeBlock>();
		          for (int k = 0; k < dests.size(); k++) {
		            ActionTypeDestroyer tempDest = dests.elementAt(k);
		            if ((tempDest instanceof UserActionTypeDestroyer)
		                && (tempDest.getDestroyerText() != null)
		                && (tempDest.getDestroyerText().length() > 0)) {
		            	CodeBlock tempBlock4_1 = CodeBlock.builder()
		            			.beginControlFlow("if ($N.equals($L))", "menuText",
		            					((UserActionTypeDestroyer) tempDest).getMenuText())
		            			.addStatement("(($T)d).setOverheadText($S)", employeeClass,
		            					tempDest.getDestroyerText())
		            			.endControlFlow()
		            			.build();
		            	actionTypes3_3_1.add(tempBlock4_1);
		            }
		          }

		          CodeBlock tempBlock4_1_result = CodeBlock.join(actionTypes3_3_1, " else ");

		          LinkedList<CodeBlock> actionTypes3_3_2 = new LinkedList<CodeBlock>();
		          for (int k = 0; k < dests.size(); k++) {
			            ActionTypeDestroyer tempDest = dests.elementAt(k);
			            if ((tempDest instanceof UserActionTypeDestroyer)
			                && (tempDest.getDestroyerText() != null)
			                && (tempDest.getDestroyerText().length() > 0)) {
			            	CodeBlock tempBlock4_2 = CodeBlock.builder()
			            			.beginControlFlow("if ($N.equals($L))", "menuText",
			            					((UserActionTypeDestroyer) tempDest).getMenuText())
			            			.addStatement("(($T)d).setOverheadText($S)", customerClass,
			            					tempDest.getDestroyerText())
			            			.endControlFlow()
			            			.build();
			            	actionTypes3_3_2.add(tempBlock4_2);
			            }
		          }

		          CodeBlock tempBlock4_2_result = CodeBlock.join(actionTypes3_3_2, " else ");

		          LinkedList<CodeBlock> actionTypes3_3_3 = new LinkedList<CodeBlock>();
		          Vector<Rule> destRules = tempAct.getAllDestroyerRules();
		          for (int k = 0; k < destRules.size(); k++) {
		            Rule dRule = destRules.elementAt(k);
		            CodeBlock tempBlock4_3 = CodeBlock.builder()
		            		.addStatement("$N.update($N, $T.UPDATE_ONE, $S, $S)", "ruleExec",
		            				"gui", ruleExecClass, dRule.getName(), tempActName)
		            		.build();
		            actionTypes3_3_3.add(tempBlock4_3);
		          }
		          
		          CodeBlock tempBlock4_3_result = CodeBlock.join(actionTypes3_3_2, "");
		          
		          CodeBlock tempBlock4;
		          if (tempAct.hasGameEndingDestroyer()) {
		            // find all game-ending user destroyers:
		            Vector<ActionTypeDestroyer> allDests = tempAct.getAllDestroyers();
		            LinkedList<CodeBlock> actionTypes3_3_4 = new LinkedList<CodeBlock>();
		            for (int k = 0; k < allDests.size(); k++) {
		              ActionTypeDestroyer tempDest = allDests.elementAt(k);
		              if ((tempDest instanceof UserActionTypeDestroyer)
		                  && (tempDest.isGameEndingDestroyer())) {
		            	  ActionTypeParticipantDestroyer scoringPartDest = null;
			                ActionTypeParticipantConstraint scoringPartConst = null;
			                ActionTypeParticipantAttributeConstraint scoringAttConst = null;
			                Vector<ActionTypeParticipantDestroyer> partDests = 
			                	tempDest.getAllParticipantDestroyers();
			                for (int m = 0; m < partDests.size(); m++) {
			                  ActionTypeParticipantDestroyer partDest = 
			                  	partDests.elementAt(m);
			                  Vector<ActionTypeParticipantConstraint> partConsts = 
			                  	partDest.getAllConstraints();
			                  for (int n = 0; n < partConsts.size(); n++) {
			                    ActionTypeParticipantConstraint partConst = 
			                    	partConsts.elementAt(n);
			                    ActionTypeParticipantAttributeConstraint[] attConsts = 
			                    	partConst.getAllAttributeConstraints();
			                    for (int p = 0; p < attConsts.length; p++) {
			                      if (attConsts[p].isScoringAttribute()) {
			                        scoringAttConst = attConsts[p];
			                        scoringPartConst = partConst;
			                        scoringPartDest = partDest;
			                        break;
			                      }
			                    }
			                  }
			                }
		            	  
			                CodeBlock tempBlock4_4;
			                if ((scoringAttConst != null) && (scoringPartConst != null)
				                    && (scoringPartDest != null)) {		
			                	Class type = null;
			                	if (scoringAttConst.getAttribute().getType() == 
				                  	AttributeTypes.INTEGER) {
				                    type = int.class;
				                  } else if (scoringAttConst.getAttribute().getType() == 
				                  	AttributeTypes.DOUBLE) {
				                    type = double.class;
				                  } else if (scoringAttConst.getAttribute().getType() == 
				                  	AttributeTypes.STRING) {
				                    type = String.class;
				                  } else if (scoringAttConst.getAttribute().getType() == 
				                  	AttributeTypes.BOOLEAN) {
				                    type = boolean.class;
				                  }
			                	
			                	ClassName simSEGUI = ClassName.get("simse.gui", "SimSEGUI");
			                	
			                	tempBlock4_4 = CodeBlock.builder()
				            			  .beginControlFlow("if ($N.equals($S))", "menuText", 
				            					  ((UserActionTypeDestroyer) tempDest).getMenuText())
				            			  .addStatement("// stop game and give score:")
				            			  .addStatement("$TAction t111 = ($TAction) $L",
				            					  tempClass, tempClass, tempActName)
				            			  .beginControlFlow("if (t111.getAll$Ls().size() > 0",
				            					  scoringPartDest.getParticipant().getName())
				            			  .addStatement("$T t = ($T)(t111.getAll$Ls().elementAt(0))",
				            					  tempClass, tempClass, scoringPartDest.getParticipant().getName())
				            			  .beginControlFlow("if (t != null)")
				            			  .addStatement("$T v = t.get$L()", type, scoringAttConst.getAttribute().getName())
				            			  .addStatement("$N.getClock().stop()", "state")
				            			  .addStatement("$N.setScore(v)", "state")
				            			  .addStatement("(($T)$N).update()", simSEGUI, "gui")
				            			  .addStatement("$T alert = new $T($T.INFORMATION, $S + v)", Alert.class, Alert.class,
				            					  AlertType.class, "Your score is ")
				            			  .addStatement("alert.setTitle($S)", "Game over!")
				            			  .addStatement("alert.show()")
				            			  .endControlFlow()
				            			  .endControlFlow()
				            			  .endControlFlow()
				            			  .build();
			                } else {
			                	tempBlock4_4 = CodeBlock.builder()
				            			  .beginControlFlow("if ($N.equals($S))", "menuText", 
				            					  ((UserActionTypeDestroyer) tempDest).getMenuText())
				            			  .addStatement("// stop game and give score:")
				            			  .addStatement("$LAction t111 = ($LAction) $L",
				            					  CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName()),
				            					  CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName()),
				            					  tempActName)
				            			  .endControlFlow()
				            			  .build();
			                }
			                
			                actionTypes3_3_4.add(tempBlock4_4);
		              }
		            }
		            
		            CodeBlock tempBlock4_4_result = CodeBlock.join(actionTypes3_3_4, " else ");
		        	  
		        	  tempBlock4 = CodeBlock.builder()
		        		  .beginControlFlow("if ($L.getAll$L.getName()s.size() < $L)",
		        				  tempActName, part.getName(), minVal)
		        		  .addStatement("$T c = $L.getAllParticipants()", ssObjectType, tempActName)
		        		  .beginControlFlow("for (int j = 0; j < c.size(); j++)")
		        		  .addStatement("$T d = c.elementAt(j)", ssObject)
		        		  .beginControlFlow("if (d instanceof $T)", employeeClass)
		        		  .add(tempBlock4_1_result)
		        		  .nextControlFlow("else if (d instanceof $T)", customerClass)
		        		  .add(tempBlock4_2_result)
		        		  .endControlFlow()
		        		  .endControlFlow()
		        		  .add(tempBlock4_3_result)
		        		  .addStatement("state.getActionStateRepository().get$LActionState"
		        		  		+ "Repository().remove($L)",
		        		  		CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName()),
		        		  		tempActName)
		        		  .add(tempBlock4_4_result)
		        		  .build();
		          } else {
		        	  tempBlock4 = CodeBlock.builder()
			        		  .beginControlFlow("if ($L.getAll$L.getName()s.size() < $L)",
			        				  tempActName, part.getName(), minVal)
			        		  .addStatement("$T c = $L.getAllParticipants()", ssObjectType, tempActName)
			        		  .beginControlFlow("for (int j = 0; j < c.size(); j++)")
			        		  .addStatement("$T d = c.elementAt(j)", ssObject)
			        		  .beginControlFlow("if (d instanceof $T)", employeeClass)
			        		  .add(tempBlock4_1_result)
			        		  .nextControlFlow("else if (d instanceof $T)", customerClass)
			        		  .add(tempBlock4_2_result)
			        		  .endControlFlow()
			        		  .endControlFlow()
			        		  .add(tempBlock4_3_result)
			        		  .addStatement("state.getActionStateRepository().get$LActionState"
			        		  		+ "Repository().remove($L)",
			        		  		CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName()),
			        		  		tempActName)
			        		  .build();
		          }
		          
		          actionTypes3_3.add(tempBlock4);
		          
		        }
	        
	        CodeBlock tempBlock4_result = CodeBlock.join(actionTypes3_3, " else ");
	        
	        CodeBlock tempBlock = CodeBlock.builder()
	        		.beginControlFlow("if (tempAct instanceof $T)", tempClass)
	        		.addStatement("$T $L = ($T) tempAct", tempClass, tempActName, tempClass)
	        		.add(tempBlock2_result)
	        		.add(tempBlock3_result)
	        		.add(tempBlock4_result)
	        		.build();
	        
	        actionTypes.add(tempBlock);
	      }

	  CodeBlock tempBlock_result = CodeBlock.join(actionTypes3, " else ");
	  
	  MethodSpec handle = MethodSpec.methodBuilder("handle")
			  .addModifiers(Modifier.PUBLIC)
			  .returns(void.class)
			  .addParameter(MouseEvent.class, "evt")
			  .addAnnotation(Override.class)
			  .addStatement("$T source = evt.getSource()", Object.class)
			  .beginControlFlow("if (source == cancelButton)")
			  .addStatement("hide()")
			  .nextControlFlow("else if (source == $N)", "okButton")
			  .addStatement("$T numChecked = 0", int.class)
			  .beginControlFlow("for (int i = 0; i < $N.size(); i++)", "checkBoxes")
			  .addStatement("$T tempCBox = $N.elementAt(i)", checkboxClass, "checkBoxes")
			  .beginControlFlow("if (tempCBox.isSelected())")
			  .addStatement("numChecked++")
			  .endControlFlow()
			  .endControlFlow()
			  .beginControlFlow("if (numChecked == 0)")
			  .addStatement("$T alert = new $T($T.WARNING, $S)", Alert.class, Alert.class,
					  AlertType.class, "You must choose at least one action")
			  .addStatement("alert.setTitle($S)", "Invalid Input")
			  .addStatement("alert.show()")
			  .nextControlFlow("else")
			  .beginControlFlow("for (int i = 0; i < $N.size(); i++", "checkBoxes")
			  .addStatement("$T cBox = $N.elementAt(i)", checkboxClass, "checkBoxes")
			  .beginControlFlow("if (cBox.isSelected())")
			  .addStatement("$T tempAct = $N.elementAt(i)", actionClass, "actions")
			  .addCode(tempBlock_result)
			  .endControlFlow()
			  .endControlFlow()
			  .addStatement("hide()")
			  .endControlFlow()
			  .endControlFlow()
			  .build();
	  
	  TypeSpec destroyDialog = TypeSpec.classBuilder("ChooseActionToDestroyDialog")
  			.superclass(Dialog.class)
  			.addSuperinterface(mouseHandler)
  			.addField(actionsType, "actions", Modifier.PRIVATE)
  			.addField(stateClass, "state", Modifier.PRIVATE)
  			.addField(ruleExecClass, "ruleExec", Modifier.PRIVATE)
  			.addField(employeeClass, "emp", Modifier.PRIVATE)
  			.addField(String.class, "menuText", Modifier.PRIVATE)
  			.addField(stageClass, "gui", Modifier.PRIVATE)
  			.addField(checkboxType, "checkBoxes", Modifier.PRIVATE)
  			.addField(buttonClass, "okButton", Modifier.PRIVATE)
  			.addField(buttonClass, "cancelButton", Modifier.PRIVATE)
  			.addMethod(destroyConstructor)
  			.addMethod(handle)
  			.build();
	  
	  JavaFile javaFile = JavaFile.builder("ChooseActionToDestroyDialog", destroyDialog)
			    .build();
	  
    try {
      catddFile = new File(directory,
          ("simse\\logic\\dialogs\\ChooseActionToDestroyDialog.java"));
      if (catddFile.exists()) {
        catddFile.delete(); // delete old version of file
      }
      
      javaFile.writeTo(catddFile);

    } catch (IOException e) {
      JOptionPane.showMessageDialog(null, ("Error writing file "
          + catddFile.getPath() + ": " + e.toString()), "File IO Error",
          JOptionPane.WARNING_MESSAGE);
    }
  }
}