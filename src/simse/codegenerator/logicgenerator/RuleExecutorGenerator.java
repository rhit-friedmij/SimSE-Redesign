/*
 * This class is responsible for generating all of the code for the logic's rule
 * executor component in SimSE
 */

package simse.codegenerator.logicgenerator;

import simse.modelbuilder.objectbuilder.AttributeTypes;
import simse.modelbuilder.objectbuilder.SimSEObjectTypeTypes;
import simse.modelbuilder.objectbuilder.WarningListDialog;
import simse.modelbuilder.actionbuilder.ActionType;
import simse.modelbuilder.actionbuilder.ActionTypeDestroyer;
import simse.modelbuilder.actionbuilder.ActionTypeParticipant;
import simse.modelbuilder.actionbuilder.ActionTypeParticipantAttributeConstraint;
import simse.modelbuilder.actionbuilder.ActionTypeParticipantConstraint;
import simse.modelbuilder.actionbuilder.ActionTypeParticipantDestroyer;
import simse.modelbuilder.actionbuilder.AttributeGuard;
import simse.modelbuilder.actionbuilder.DefinedActionTypes;
import simse.modelbuilder.rulebuilder.CreateObjectsRule;
import simse.modelbuilder.rulebuilder.DestroyObjectsRule;
import simse.modelbuilder.rulebuilder.DestroyObjectsRuleParticipantCondition;
import simse.modelbuilder.rulebuilder.EffectRule;
import simse.modelbuilder.rulebuilder.InputType;
import simse.modelbuilder.rulebuilder.OtherActionsEffect;
import simse.modelbuilder.rulebuilder.ParticipantAttributeRuleEffect;
import simse.modelbuilder.rulebuilder.ParticipantRuleEffect;
import simse.modelbuilder.rulebuilder.ParticipantTypeRuleEffect;
import simse.modelbuilder.rulebuilder.Rule;
import simse.modelbuilder.rulebuilder.RuleInput;
import simse.modelbuilder.rulebuilder.RuleTiming;
import simse.modelbuilder.startstatebuilder.InstantiatedAttribute;
import simse.modelbuilder.startstatebuilder.SimSEObject;
import simse.codegenerator.CodeGeneratorConstants;
import simse.codegenerator.CodeGeneratorUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
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

public class RuleExecutorGenerator implements CodeGeneratorConstants {
	private File directory; // directory to save generated code into
	private DefinedActionTypes actTypes; // holds all of the defined action types from an ssa file
	private FileWriter writer;
	private File ruleExFile;
	private Vector<Rule> nonPrioritizedRules;
	private Vector<Rule> prioritizedRules;
	private Vector<String> warnings; // holds warning messages about any errors that are run into during generation
	private Vector<MethodSpec> genMethods;
	
	private ClassName action = ClassName.get("simse.adts.actions", "Action");
	private ClassName alert = ClassName.get("javafx.scene.control", "Alert");
	private ClassName alertType = ClassName.get("javafx.scene.control.Alert", "AlertType");
	private ClassName customer = ClassName.get("simse.adts.objects", "Customer");
	private ClassName employee = ClassName.get("simse.adts.objects", "Employee");
	private ClassName simseGui = ClassName.get("simse.gui", "SimSEGUI");
	private ClassName stage = ClassName.get("javafx.stage", "Stage");
	private ClassName textInputDialog = ClassName.get("javafx.scene.control", "TextInputDialog");
	private ClassName vector = ClassName.get("java.util", "Vector");
	private TypeName vectorOfActions = ParameterizedTypeName.get(vector, action);

	public RuleExecutorGenerator(DefinedActionTypes actTypes, File directory) {
		this.actTypes = actTypes;
		this.directory = directory;
		warnings = new Vector<String>();
		genMethods = new Vector<>();
		initializeRuleLists();
	}

	// returns true if generation successful, false otherwise
	public boolean generate() {
		ClassName random = ClassName.get("java.util", "Random");
		ClassName project = ClassName.get("simse.adts.objects", "Project");
		ClassName tool = ClassName.get("simse.adts.objects", "Tool");
		ClassName state = ClassName.get("simse.state", "State");
		ClassName trigCheck = ClassName.get("simse.logic", "TriggerChecker");
		ClassName destCheck = ClassName.get("simse.logic", "DestroyerChecker");
		
		MethodSpec ruleConstructer = MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
				.addParameter(state, "s")
				.addStatement("state = s")
				.addStatement("ranNumGen = new $T()", random)
				.build();
		
		MethodSpec setTrigCheck = MethodSpec.methodBuilder("setTriggerChecker")
				.addModifiers(Modifier.PUBLIC)
				.addParameter(trigCheck, "t")
				.addStatement("triggerChecker = t")
				.build();
		
		MethodSpec setDestCheck = MethodSpec.methodBuilder("setDestroyerChecker")
				.addModifiers(Modifier.PUBLIC)
				.addParameter(destCheck, "d")
				.addStatement("destroyerChecker = d")
				.build();
		
		MethodSpec update = MethodSpec.methodBuilder("update")
				.addModifiers(Modifier.PUBLIC)
				.addParameter(stage, "gui")
				.addParameter(int.class, "updateInstructions")
				.addParameter(String.class, "ruleName")
				.addParameter(action, "action")
				.addCode(ruleMethodCalls().build())
				.build();
		
		// generate rule function bodies:
		for (int i = 0; i < prioritizedRules.size(); i++) {
			generateRuleExecutorFunctionBody(prioritizedRules.elementAt(i));
		}
		// generate non-prioritized rule function calls:
		for (int i = 0; i < nonPrioritizedRules.size(); i++) {
			generateRuleExecutorFunctionBody(nonPrioritizedRules.elementAt(i));
		}
		
		MethodSpec checkMin = MethodSpec.methodBuilder("checkAllMins")
				.addModifiers(Modifier.PRIVATE)
				.addParameter(stage, "parent")
				.addStatement("$T actions = state.getActionStateRepository().getAllActions()", vectorOfActions)
				.beginControlFlow("for (int i = 0; i < actions.size(); i++)")
				.addStatement("$T act = actions.elementAt(i)", action)
				.addCode(minConditions().build())
				.endControlFlow()
				.build();
		
		FieldSpec continuous = FieldSpec.builder(int.class, "UPDATE_ALL_CONTINUOUS")
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
				.initializer("$L", 0)
				.build();
		FieldSpec one = FieldSpec.builder(int.class, "UPDATE_ONE")
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
				.initializer("$L", 1)
				.build();
		
		TypeSpec ruleExecutor = TypeSpec.classBuilder("RuleExecutor")
				.addModifiers(Modifier.PUBLIC)
				.addField(continuous)
				.addField(one)
				.addField(state, "state", Modifier.PRIVATE)
				.addField(random, "ranNumGen", Modifier.PRIVATE)
				.addField(trigCheck, "triggerChecker", Modifier.PRIVATE)
				.addField(destCheck, "destroyerChecker", Modifier.PRIVATE)
				.addMethod(ruleConstructer)
				.addMethod(setTrigCheck)
				.addMethod(setDestCheck)
				.addMethod(update)
				.addMethods(genMethods)
				.addMethod(checkMin)				
				.build();

		JavaFile javaFile = JavaFile.builder("simse.logic", ruleExecutor)
				.addFileComment("File generated by: simse.codegenerator.logicgenerator.RuleExecutorGenerator")
				.build();
		
		try {
			ruleExFile = new File(directory, ("simse\\logic\\RuleExecutor.java"));
			if (ruleExFile.exists()) {
				ruleExFile.delete(); // delete old version of file
			}
			writer = new FileWriter(ruleExFile);
			javaFile.writeTo(writer);
			writer.close();
			
			// generate warnings, if any:
			if (warnings.size() > 0) {
				warnings.add(0, "ERROR! Incomplete simulation generated!!");
				new WarningListDialog(warnings, "Code Generation Errors");
				return false;
			}
			return true;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, ("Error writing file " + ruleExFile.getPath() + ": " + e.toString()),
					"File IO Error", JOptionPane.WARNING_MESSAGE);
			return false;
		}
	}
	
	private CodeBlock.Builder ruleMethodCalls() {
		CodeBlock.Builder methods = CodeBlock.builder();
		// generate prioritized rule function calls:
		for (int i = 0; i < prioritizedRules.size(); i++) {
			methods.add(generateRuleExecutorFunctionCall(prioritizedRules.elementAt(i)).build());
		}
		// generate non-prioritized rule function calls:
		for (int i = 0; i < nonPrioritizedRules.size(); i++) {
			methods.add(generateRuleExecutorFunctionCall(nonPrioritizedRules.elementAt(i)).build());
		}
		methods.addStatement("(($T) gui).update()", simseGui);
		return methods;
	}

	private CodeBlock.Builder generateRuleExecutorFunctionCall(Rule rule) {
		CodeBlock.Builder methods = CodeBlock.builder();
		String methodName = CodeGeneratorUtils.getLowerCaseLeading(rule.getName());
		methods.addStatement(methodName + "(gui, updateInstructions, ruleName, action)");
		return methods;
	}
	
	private CodeBlock.Builder warningPopup(String title, String contentText) {
		CodeBlock.Builder methodBody = CodeBlock.builder();
		methodBody.addStatement("$T alert = new $T($T.WARNING)", alert, alert, alertType);
		methodBody.addStatement("alert.setTitle($S)", title);
		methodBody.addStatement("alert.setContentText($S)", contentText);
		methodBody.addStatement("alert.setHeaderText(null)");
		methodBody.addStatement("alert.showAndWait()");
		return methodBody;
	}
	
	private CodeBlock.Builder textInputPopup(String title,  String contentText) {
		CodeBlock.Builder methodBody = CodeBlock.builder();
		methodBody.addStatement("$T dialog = new $T()", textInputDialog, textInputDialog);
		methodBody.addStatement("dialog.setTitle($S)", title);
		methodBody.addStatement("dialog.setContentText($S)", contentText);
		methodBody.addStatement("dialog.setHeaderText(null)");
		methodBody.addStatement("dialog.showAndWait()");
		methodBody.addStatement("String response = dialog.getResult()");
		return methodBody;
	}

	private void generateRuleExecutorFunctionBody(Rule rule) {
		CodeBlock.Builder methodBody = CodeBlock.builder();
		String ruleName = rule.getName();
		String methodName = CodeGeneratorUtils.getLowerCaseLeading(ruleName);
		
		ActionType actType = rule.getActionType();
		String actTypeName = actType.getName();
		String uCaseActionName = CodeGeneratorUtils.getUpperCaseLeading(actType.getName()) + "Action";
		String actTypeVar = actTypeName.toLowerCase() + "Acts";
		String oneActTypeVar = actTypeName.toLowerCase() + "Act";
		
		ClassName actClass = ClassName.get("simse.adts.actions", uCaseActionName);
		TypeName vectorOfActTypes = ParameterizedTypeName.get(vector, actClass);
		
		methodBody.addStatement("$T $L = state.getActionStateRepository().get$LStateRepository().getAllActions()"
				, vectorOfActTypes, actTypeVar, uCaseActionName);
		if (rule instanceof EffectRule) { 
			// EFFECT RULE
			EffectRule effRule = (EffectRule) rule;
			int ruleTiming = rule.getTiming();

			String ruleType = "";
			if (ruleTiming == RuleTiming.CONTINUOUS) { 
				// continuous rule
				ruleType = "UPDATE_ALL_CONTINUOUS";
			} else if (ruleTiming == RuleTiming.TRIGGER || ruleTiming == RuleTiming.DESTROYER) {
				// trigger/destroyer rule
				ruleType = "UPDATE_ONE) && (ruleName.equals(\"" + ruleName + "\")";
			}
			methodBody.beginControlFlow("if ((updateInstructions == $L))", ruleType);
			methodBody.beginControlFlow("for (int i = 0; i < $L.size(); i++)", actTypeVar);  
			methodBody.addStatement("$T $L = $L.elementAt(i)", actClass, oneActTypeVar, actTypeVar);
			
			if ((rule.getTiming() == RuleTiming.TRIGGER) || (rule.getTiming() == RuleTiming.DESTROYER)) {
				methodBody.beginControlFlow("if($L == action)", oneActTypeVar);
			}
			// code to make sure it has the min num of participants:
			String minCondition = "if (";
			Vector<ActionTypeParticipant> parts = actType.getAllParticipants();
			for (int i = 0; i < parts.size(); i++) {
				ActionTypeParticipant part = parts.elementAt(i);
				if (i > 0) { 
					// not on first element
					minCondition += " && ";
				}
				minCondition += "(" + oneActTypeVar + ".getAll" + part.getName() + "s().size() >= ";
				if (part.getQuantity().isMinValBoundless()) { 
					// min val boundless
					minCondition += "0";
				} else { 
					// has min val
					minCondition += part.getQuantity().getMinVal().toString();
				}
				minCondition += ")";
			}
			minCondition += ")";
			if (!minCondition.equals("if ()")) {
				methodBody.beginControlFlow(minCondition);
			}

			// get rule input(s), if any:
			Vector<RuleInput> rInputs = effRule.getAllRuleInputs();
			for (int j = 0; j < rInputs.size(); j++) {
				RuleInput input = rInputs.elementAt(j);
				String inputName = "input" + input.getName();
				String inputType = input.getType();

				if ((inputType.equals(InputType.DOUBLE)) || (inputType.equals(InputType.INTEGER))) { 
					// numerical rule input type
					// have to initialize it to something or else it doesn't work!
					methodBody.addStatement("double $L = 0", inputName);
					methodBody.addStatement("boolean gotValidInput$L = false", j);
					if (j == 0) { 
						// on first rule input
						methodBody.addStatement("boolean cancel = false");
					}
					methodBody.beginControlFlow("if (!cancel)");
					methodBody.beginControlFlow("while (!gotValidInput$L)", j);
					
					String contentText = input.getPrompt() + ": (" + input.getType();
					if (input.getCondition().isConstrained()) { 
						// input has a condition
						contentText += " " + input.getCondition().getGuard() + " " + input.getCondition().getValue();
					}
					contentText += ")";
					methodBody.add(textInputPopup("Input", contentText).build());
					
					methodBody.beginControlFlow("if (response != null)");
					methodBody.beginControlFlow("try");
					methodBody.addStatement("$T temp = new $T(response)", inputType, inputType);
					
					String tempTypeStr = new String();
					if (input.getType().equals(InputType.INTEGER)) {
						tempTypeStr = "int";
					} else if (input.getType().equals(InputType.DOUBLE)) {
						tempTypeStr = "double";
					}
					if (input.getCondition().isConstrained()) { 
						// input has a condition
						methodBody.beginControlFlow("if (temp.$LValue() $L $L)", tempTypeStr
								, input.getCondition().getGuard(), input.getCondition().getValue());
					}
					
					methodBody.addStatement("$T = (double) (temp.$LValue())", inputName, tempTypeStr);
					methodBody.addStatement("gotValidInput$L = true", j);
							
					if (input.getCondition().isConstrained()) {
						methodBody.nextControlFlow("else");
						methodBody.add(warningPopup("Invalid Input", "Invalid Input -- Please try again!").build());
						methodBody.endControlFlow();
					}
					
					methodBody.nextControlFlow("catch (NumberFormatException e)");
					methodBody.add(warningPopup("Invalid Input", "Invalid Input -- Please try again!").build());
					methodBody.endControlFlow();
					methodBody.nextControlFlow("else");
					methodBody.add("// action cancelled\n");
					
					
					if (input.isCancelable()) {
						methodBody.addStatement("state.getActionStateRepository().get$LStateRepository().remove($T)"
								, uCaseActionName, oneActTypeVar);
						methodBody.addStatement("cancel = true");
						methodBody.addStatement("break");
					} else { 
						// not cancelable
						methodBody.add(warningPopup("Invalid Input", "Invalid Input -- Please try again!").build());
					}
					methodBody.endControlFlow();
					methodBody.endControlFlow();
					methodBody.endControlFlow();
				} else { 
					// boolean or string rule input type
					if (inputType.equals(InputType.STRING)) { 
						// string rule input type
						methodBody.addStatement("String $L = new String()", inputName);
						methodBody.addStatement("boolean gotValidInput$L = false", j);
						if (j == 0) { 
							// on first rule input
							methodBody.addStatement("boolean cancel = false");
						}
						methodBody.beginControlFlow("if (!cancel)");
						methodBody.beginControlFlow("while (!gotValidInput$L)", j);
						
						String contentText = input.getPrompt() + ": (String)";
						methodBody.add(textInputPopup("Input", contentText).build());
						
						methodBody.beginControlFlow("if ((response != null) && (response.length() > 0))");
						methodBody.addStatement("$L = response", inputName);
						methodBody.addStatement("gotValidInput$L = true", j);
						methodBody.nextControlFlow("else");
						methodBody.add("// action cancelled\n");
						
						
						if (input.isCancelable()) {
							methodBody.addStatement("state.getActionStateRepository().get$LStateRepository().remove($T)"
									, uCaseActionName, oneActTypeVar);
							methodBody.addStatement("cancel = true");
							methodBody.addStatement("break");
						} else { 
							// not cancelable
							methodBody.add(warningPopup("Invalid Input", "You must enter input -- Please try again!").build());
						}
						methodBody.endControlFlow();
						methodBody.endControlFlow();
						methodBody.endControlFlow();
					} else if (input.getType().equals(InputType.BOOLEAN)) { 
						// boolean rule input type
						methodBody.addStatement("boolean $L = false", inputName);
						methodBody.addStatement("boolean gotValidInput$L = false", j);
						if (j == 0) { 
							// on first rule input
							methodBody.addStatement("boolean cancel = false");
						}
						methodBody.beginControlFlow("if (!cancel)");
						methodBody.beginControlFlow("while (!gotValidInput$L)", j);
						
						String contentText = input.getPrompt() + ": (true or false)";
						methodBody.add(textInputPopup("Input", contentText).build());
						
						methodBody.beginControlFlow("if (response != null)");
						methodBody.beginControlFlow("if (response.equalsIgnoreCase($S))", "true");
						methodBody.addStatement("$L = true", inputName);
						methodBody.addStatement("gotValidInput$L = true", j);
						methodBody.nextControlFlow("else if(response.equalsIgnoreCase($S))", "false");
						methodBody.addStatement("gotValidInput$L = true", j);
						methodBody.nextControlFlow("else");
						methodBody.add(warningPopup("Invalid Input", "Invalid Input -- Please try again!").build());
						methodBody.endControlFlow();
						methodBody.nextControlFlow("else");
						methodBody.add("// action cancelled\n");
						
						if (input.isCancelable()) {
							methodBody.addStatement("state.getActionStateRepository().get$LStateRepository().remove($T)"
									, uCaseActionName, oneActTypeVar);
							methodBody.addStatement("cancel = true");
							methodBody.addStatement("break");
						} else { 
							// not cancelable
							methodBody.add(warningPopup("Invalid Input", "You must enter input -- Please try again!").build());
						}
						methodBody.endControlFlow();
						methodBody.endControlFlow();
						methodBody.endControlFlow();
					}
				}
			}

			String inputConds = "";
			for (int j = 0; j < rInputs.size(); j++) {
				if (j == 0) { 
					// first element
					inputConds += "if(gotValidInput" + j;
				} else { 
					// not on first element
					inputConds += " && gotValidInput" + j;
				}
				if (j == (rInputs.size() - 1)) { 
					// on last rule input
					inputConds += ")";
				}
			}
			if (rInputs.size() > 0) {
				methodBody.beginControlFlow(inputConds);
			}
			
			// record-keeping
			Vector<String> ruleVariables = new Vector<String>(); 
			// go through all participant rule effects:
			Vector<ParticipantRuleEffect> partRuleEffects = effRule.getAllParticipantRuleEffects();
			for (int j = 0; j < partRuleEffects.size(); j++) {
				ParticipantRuleEffect partRuleEff = partRuleEffects.elementAt(j);
				int partType = partRuleEff.getParticipant().getSimSEObjectTypeType();
				String metaTypeName = SimSEObjectTypeTypes.getText(partType);
				String partTypeName = partRuleEff.getParticipant().getName();
				String partTypeVar = partTypeName.toLowerCase() + "s";
				String onePartTypeVar = partTypeName.toLowerCase() + "2";
				
				ClassName metaType = ClassName.get("simse.adts.objects", metaTypeName);
				TypeName vectorOfMetaType = ParameterizedTypeName.get(vector, metaType);
				
				if (vectorContainsString(ruleVariables, partTypeVar) == false) { 
					// this variable has not been generated yet
					// add the variable name to the record-keeping Vector
					ruleVariables.add(partTypeVar); 
					methodBody.addStatement("$T $L = $L.getAllActive$Ls()", vectorOfMetaType, partTypeVar, oneActTypeVar, partTypeName);
				}
				methodBody.beginControlFlow("for (int j = 0; j < $L.size(); j++)", partTypeVar);
				methodBody.addStatement("$T $L = $L.elementAt(j)", metaType, onePartTypeVar, partTypeVar);
				
				// go through all participant type rule effects:
				Vector<ParticipantTypeRuleEffect> partTypeEffects = partRuleEff.getAllParticipantTypeEffects();
				// for keeping track of which variables are generated
				Vector<String> variables = new Vector<String>(); 

				for (int k = 0; k < partTypeEffects.size(); k++) {
					ParticipantTypeRuleEffect partTypeRuleEff = partTypeEffects.elementAt(k);
					String objType = partTypeRuleEff.getSimSEObjectType().getName();
					String uCaseObjType = CodeGeneratorUtils.getUpperCaseLeading(objType);
					String objTypeVar = objType.toLowerCase();
					
					ClassName objTypeClass = ClassName.get("simse.adts.objects", objType);
					if (k == 0) { 
						// on first element
						methodBody.beginControlFlow("if ($L instanceof $T)", onePartTypeVar, objTypeClass);
					} else {
						methodBody.nextControlFlow("else if ($L instanceof $T)", onePartTypeVar, objTypeClass);
					}
					methodBody.addStatement("$T $L = ($T) $L", objTypeClass, objTypeVar, objTypeClass, onePartTypeVar);

					// effect on participants' other actions:
					if (partTypeRuleEff.getOtherActionsEffect().getEffect().equals(OtherActionsEffect.ACTIVATE_ALL)
							|| partTypeRuleEff.getOtherActionsEffect().getEffect().equals(OtherActionsEffect.DEACTIVATE_ALL)) {
						boolean activateAll = true;
						String active = "";
						if (partTypeRuleEff.getOtherActionsEffect().getEffect()
								.equals(OtherActionsEffect.DEACTIVATE_ALL)) {
							activateAll = false;
							active = "Active";
						} else if (partTypeRuleEff.getOtherActionsEffect().getEffect()
								.equals(OtherActionsEffect.ACTIVATE_ALL)) {
							activateAll = true;
							active = "Inactive";
						}
						methodBody.addStatement("$T otherActs = state.getActionStateRepository().getAll$LActions($L)"
								, vectorOfActions, active, objTypeVar);
						methodBody.beginControlFlow("for (int k = 0; k < otherActs.size(); k++)");
						methodBody.addStatement("Action tempAct = otherActs.elementAt(k)");
						
						// go through all action types:
						Vector<ActionType> allActTypes = actTypes.getAllActionTypes();
						for (int m = 0; m < allActTypes.size(); m++) {
							ActionType tempActType = allActTypes.elementAt(m);
							if (m == 0) { 
								// on first element
								methodBody.beginControlFlow("if (tempAct instanceof $T)", onePartTypeVar, actClass);
							} else {
								methodBody.nextControlFlow("else if (tempAct instanceof $T)", onePartTypeVar, actClass);
							}
							if (tempActType.getName().equals(actType.getName())) {
								methodBody.beginControlFlow("if(tempAct.equals($L) == false)", oneActTypeVar);
							}
							// go through all participants:
							Vector<ActionTypeParticipant> allParts = tempActType.getAllParticipants();

							for (int n = 0; n < allParts.size(); n++) {
								ActionTypeParticipant tempPart = allParts.elementAt(n);
								if (tempPart.getSimSEObjectTypeType() == partTypeRuleEff.getSimSEObjectType().getType()) {
									
									String active2 = "";
									if (activateAll) {
										active2 = "Active";
									} else {
										active2 = "Inactive";
									}
									methodBody.addStatement("((" + 
											CodeGeneratorUtils.getUpperCaseLeading(tempActType.getName())
													+ "Action)tempAct).set" + tempPart.getName() + active2 + "(" + objTypeVar + ")");
								}
							}
							if (tempActType.getName().equals(actType.getName())) {
								methodBody.endControlFlow();
							}
							if (m == allActTypes.size() - 1) { 
								methodBody.endControlFlow();
							}
						}
						methodBody.endControlFlow();
					} else if (partTypeRuleEff.getOtherActionsEffect().getEffect()
							.equals(OtherActionsEffect.ACTIVATE_DEACTIVATE_SPECIFIC_ACTIONS)) {

						// actions to activate:
						Vector<ActionType> actsToActivate = partTypeRuleEff.getOtherActionsEffect()
								.getActionsToActivate();
						for (int m = 0; m < actsToActivate.size(); m++) {
							ActionType tempAct = actsToActivate.elementAt(m);
							methodBody.addStatement("Vector<" + CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName())
									+ "Action> " + tempAct.getName().toLowerCase()
									+ "actionsActivate = state.getActionStateRepository().get"
									+ CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName())
									+ "ActionStateRepository().getAllInactiveActions("
									+ objType.toLowerCase() + ")");
							methodBody.beginControlFlow("for (int k = 0; k < " + tempAct.getName().toLowerCase()
									+ "actionsActivate.size(); k++)");
							methodBody.addStatement(
									CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName()) + "Action tempAct = "
											+ tempAct.getName().toLowerCase() + "actionsActivate.elementAt(k)");

							// go through all participants:
							Vector<ActionTypeParticipant> allParts = tempAct.getAllParticipants();
							for (int n = 0; n < allParts.size(); n++) {
								ActionTypeParticipant tempPart = allParts.elementAt(n);
								if (tempPart.getSimSEObjectTypeType() == partTypeRuleEff.getSimSEObjectType()
										.getType()) {
									methodBody.addStatement("tempAct.set" + tempPart.getName() + "Active("
											+ objType.toLowerCase() + ")");
								}
							}
							methodBody.endControlFlow();
						}

						// actions to deactivate:
						Vector<ActionType> actsToDeactivate = partTypeRuleEff.getOtherActionsEffect()
								.getActionsToDeactivate();
						for (int m = 0; m < actsToDeactivate.size(); m++) {
							ActionType tempAct = actsToDeactivate.elementAt(m);
							methodBody.addStatement("Vector<" + CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName())
									+ "Action> " + tempAct.getName().toLowerCase()
									+ "actionsDeactivate = state.getActionStateRepository().get"
									+ CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName())
									+ "ActionStateRepository().getAllActiveActions("
									+ objType.toLowerCase() + ")");
							methodBody.beginControlFlow("for (int k = 0; k < " + tempAct.getName().toLowerCase()
									+ "actionsDeactivate.size(); k++)");
							methodBody.addStatement(
									CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName()) + "Action tempAct = "
											+ tempAct.getName().toLowerCase() + "actionsDeactivate.elementAt(k)");

							// go through all participants:
							Vector<ActionTypeParticipant> allParts = tempAct.getAllParticipants();
							for (int n = 0; n < allParts.size(); n++) {
								ActionTypeParticipant tempPart = allParts.elementAt(n);
								if (tempPart.getSimSEObjectTypeType() == partTypeRuleEff.getSimSEObjectType()
										.getType()) {
									methodBody.addStatement("tempAct.set" + tempPart.getName() + "Inactive("
											+ objType.toLowerCase() + ")");
								}
							}
							methodBody.endControlFlow();
						}
					}

					// go through all participant attribute rule effects:
					Vector<ParticipantAttributeRuleEffect> partAttRuleEffects = partTypeRuleEff
							.getAllAttributeEffects();
					for (int m = 0; m < partAttRuleEffects.size(); m++) {
						ParticipantAttributeRuleEffect partAttRuleEff = partAttRuleEffects.elementAt(m);
						if ((partAttRuleEff.getEffect().equals(null) == false)
								&& (partAttRuleEff.getEffect().length() > 0)) {
							if ((partAttRuleEff.getAttribute().getType() == AttributeTypes.INTEGER)
									|| (partAttRuleEff.getAttribute().getType() == AttributeTypes.DOUBLE)) { 
								// numerical attributes
								/*
								 * go through the effect once to collect all of the information you need:
								 */
								String effect = partAttRuleEff.getEffect();
								boolean finished = false;
								int counter = 0;
								while (!finished) {
									counter++;
									String nextToken = getNextToken(effect);

									// attributes other participants:
									if (nextToken.startsWith("all") || nextToken.startsWith("-all")) {
										if (effect.trim().length() == nextToken.trim().length()) {
											// on last token
											effect = null;
										} else { // not on last token
											effect = effect.trim().substring(nextToken.length()).trim();
										}
										if (nextToken.startsWith("-")) {
											// remove the minus sign for now
											nextToken = nextToken.substring(1); 
										}
										// get whether it's all, allActive, or allInactive
										String activeInactiveToken = nextToken.substring(0, nextToken.indexOf('-')); 
										nextToken = nextToken.substring(activeInactiveToken.length() + 1).trim();
										// get the participant name
										String partName = nextToken.substring(0, nextToken.indexOf('-')); 
										// check for validity of participant name:
										if (actType.getParticipant(partName) == null) {
											// invalid participant name
											warnings.add("Invalid participant name: \"" + partName
													+ "\" in effect rule " + ruleName + " for "
													+ partRuleEff.getParticipant().getName() + " "
													+ objType + " "
													+ SimSEObjectTypeTypes
															.getText(partTypeRuleEff.getSimSEObjectType().getType())
													+ " " + partAttRuleEff.getAttribute().getName()
													+ " attribute effect");
										}
										nextToken = nextToken.substring(partName.length() + 1).trim();
										// get the SimSEObjectType
										String ssObjType = nextToken.substring(0, nextToken.indexOf('-')); 
										if (actType.getParticipant(partName) != null) {
											// valid participant name
											// check for validity of object type name:
											if (actType.getParticipant(partName)
													.getSimSEObjectType(ssObjType) == null) { 
												// invalid SimSEObjectType
												warnings.add("Invalid object type: \"" + ssObjType
														+ "\" in effect rule " + ruleName + " for "
														+ partRuleEff.getParticipant().getName() + " "
														+ objType + " "
														+ SimSEObjectTypeTypes.getText(
																partTypeRuleEff.getSimSEObjectType().getType())
														+ " " + partAttRuleEff.getAttribute().getName()
														+ " attribute effect");
											}
										}
										nextToken = nextToken.substring(ssObjType.length() + 1).trim();
										String attName = nextToken.substring(nextToken.indexOf(':') + 1).trim();
										if ((actType.getParticipant(partName) != null) && (actType
												.getParticipant(partName).getSimSEObjectType(ssObjType) != null)) {
											// valid participant name and SimSEObjectType
											// check for validity of attribute name:
											if (actType.getParticipant(partName).getSimSEObjectType(ssObjType)
													.getAttribute(attName) == null) {
												// invalid attribute
												warnings.add("Invalid attribute name: \"" + attName
														+ "\" in effect rule " + ruleName + " for "
														+ partRuleEff.getParticipant().getName() + " "
														+ objType + " "
														+ SimSEObjectTypeTypes.getText(
																partTypeRuleEff.getSimSEObjectType().getType())
														+ " " + partAttRuleEff.getAttribute().getName()
														+ " attribute effect");
											} else { 
												// valid attribute
												// check for validity of attribute type:
												if ((actType.getParticipant(partName).getSimSEObjectType(ssObjType)
														.getAttribute(attName).getType() != AttributeTypes.INTEGER)
														&& (actType.getParticipant(partName)
																.getSimSEObjectType(ssObjType).getAttribute(attName)
																.getType() != AttributeTypes.DOUBLE)) {
													// non-numerical attribute -- invalid
													warnings.add("Invalid (non-numerical) attribute type: \""
															+ attName + "\" in effect rule " + ruleName
															+ " for " + partRuleEff.getParticipant().getName() + " "
															+ objType + " "
															+ SimSEObjectTypeTypes.getText(
																	partTypeRuleEff.getSimSEObjectType().getType())
															+ " " + partAttRuleEff.getAttribute().getName()
															+ " attribute effect");
												}
											}
										}

										if (vectorContainsString(variables,
												(activeInactiveToken + partName + ssObjType + attName)) == false) {
											// this variable has not been generated yet
											// add to the record-keeping
											variables.add(new String(activeInactiveToken + partName + ssObjType + attName)); 
											// Vector
											methodBody.addStatement("double " + activeInactiveToken + partName + ssObjType
													+ attName + " = 0");
											if (vectorContainsString(variables,
													(activeInactiveToken + partName + "s")) == false) {
												// this variable has not been generated yet
												// add the variable name to the record-keeping
												variables.add(new String(activeInactiveToken + partName + "s")); 
												// Vector
												String active = "";
												if (activeInactiveToken.indexOf("Active") > 0) {
													active = "Active";
												} else if (activeInactiveToken.indexOf("Inactive") > 0) {
													active = "Inactive";
												}
												methodBody.addStatement("Vector " + activeInactiveToken + partName + "s = "
														+ actType.getName().toLowerCase() + "Act.getAll" + active + partName + "s()");
											}
											methodBody.beginControlFlow("for (int k = 0; k<" + activeInactiveToken + partName
													+ "s.size(); k++)");
											methodBody.addStatement("Object " + partName.toLowerCase() + "3 = "
													+ activeInactiveToken + partName + "s.elementAt(k)");
											methodBody.beginControlFlow("if(" + partName.toLowerCase() + "3 instanceof "
													+ ssObjType + ")");
											methodBody.addStatement(activeInactiveToken + partName + ssObjType + attName
													+ " += (double)(((" + ssObjType + ")" + partName.toLowerCase()
													+ "3).get" + CodeGeneratorUtils.getUpperCaseLeading(attName)
													+ "())");
											methodBody.endControlFlow();
											methodBody.endControlFlow();
										}
									} else if (nextToken.startsWith("num") || nextToken.startsWith("-num")) {
										// num actions or num participants:
										if (effect.trim().length() == nextToken.trim().length()) {
											// on last token
											effect = null;
										} else { // not on last token
											effect = effect.trim().substring(nextToken.length()).trim();
										}
										if (nextToken.startsWith("-")) {
											// remove the minus sign for now
											nextToken = nextToken.substring(1); 
										}
										String firstWord = nextToken.substring(0, nextToken.indexOf('-'));

										if (firstWord.endsWith("This")) { 
											// num actions this participant
											ActionTypeParticipant part = partRuleEff.getParticipant();
											String actionName = nextToken.substring(nextToken.indexOf(':') + 1).trim();
											// check for validity of action name:
											if ((!(actionName.equals("*")))
													&& (actTypes.getActionType(actionName) == null)) {
												warnings.add("Invalid action name: \"" + actionName
														+ "\" in effect rule " + ruleName + " for "
														+ partRuleEff.getParticipant().getName() + " "
														+ objType + " "
														+ SimSEObjectTypeTypes.getText(
																partTypeRuleEff.getSimSEObjectType().getType())
														+ " " + partAttRuleEff.getAttribute().getName()
														+ " attribute effect");
											}
											StringBuffer variableName = new StringBuffer("num");
											if (firstWord.indexOf("Active") >= 0) {
												variableName.append("Active");
											} else if (firstWord.indexOf("Inactive") >= 0) {
												variableName.append("Inactive");
											}
											variableName.append("ActionsThisPart");
											if (actionName.equals("*")) { // wild card character --
																			// all actions
												variableName.append("A");
											} else { // action name
												variableName.append(actionName);
											}
											if (vectorContainsString(variables, variableName.toString()) == false) { 
												// variable has not been generated yet
												// add the variable name to the record-keeping vector
												variables.add(variableName.toString()); 
												String repos = "";
												if (actionName.equals("*") == false) { 
													// not the wild card character -- an actual action name
													repos = "get"
															+ CodeGeneratorUtils.getUpperCaseLeading(actionName)
															+ "ActionStateRepository().";
												}
												repos += "getAll";
												if (variableName.indexOf("Active") >= 0) {
													repos += "Active";
												} else if (variableName.indexOf("Inactive") >= 0) {
													repos += "Inactive";
												}
												repos += "Actions(" + part.getName().toLowerCase() + "2).size())";
												methodBody.addStatement("double " + variableName
														+ " = (double)(state.getActionStateRepository()." + repos);
											}
										} else if (firstWord.indexOf("All") >= 0) { 
											// num actions other participants
											StringBuffer variableName = new StringBuffer("numActionsAll");
											if (firstWord.indexOf("Active") >= 0) {
												variableName.append("Active");
											} else if (firstWord.indexOf("Inactive") >= 0) {
												variableName.append("Inactive");
											}
											// take off the first word
											String tempStr = nextToken.substring(nextToken.indexOf('-') + 1); 
											String partName = new String();
											String objTypeName = new String();
											if (tempStr.indexOf('-') >= 0) { 
												// includes SimSEObjectType and meta type
												// get the participant name:
												partName = tempStr.substring(0, tempStr.indexOf('-'));
												// check for validity of part name:
												if (actType.getParticipant(partName) == null) {
													// invalid participant name
													warnings.add("Invalid participant name: \"" + partName
															+ "\" in effect rule " + ruleName + " for "
															+ partRuleEff.getParticipant().getName() + " "
															+ objType + " "
															+ SimSEObjectTypeTypes.getText(
																	partTypeRuleEff.getSimSEObjectType().getType())
															+ " " + partAttRuleEff.getAttribute().getName()
															+ " attribute effect");
												}
												variableName.append(partName);
												// take off the participant name:
												tempStr = tempStr.substring(tempStr.indexOf('-') + 1);
												// get the SimSEObjectType name:
												objTypeName = tempStr.substring(0, tempStr.indexOf('-'));
												if (actType.getParticipant(partName) != null) {
													// valid participant name
													// check for validity of SimSEObjectType name:
													if (actType.getParticipant(partName)
															.getSimSEObjectType(objTypeName) == null) {
														// invalid SimSEObjectType name
														warnings.add("Invalid object type name: \"" + objTypeName
																+ "\" in effect rule " + ruleName + " for "
																+ partRuleEff.getParticipant().getName() + " "
																+ objType
																+ " "
																+ SimSEObjectTypeTypes.getText(partTypeRuleEff
																		.getSimSEObjectType().getType())
																+ " " + partAttRuleEff.getAttribute().getName()
																+ " attribute effect");
													}
												}
												variableName.append(objTypeName);
											} else { 
												// does not include SimSEObjectType and meta type
												// get the participant name:
												partName = tempStr.substring(0, tempStr.indexOf(':'));
												// check for validity of part name:
												if (actType.getParticipant(partName) == null) {
													// invalid participant name
													warnings.add("Invalid participant name: \"" + partName
															+ "\" in effect rule " + ruleName + " for "
															+ partRuleEff.getParticipant().getName() + " "
															+ objType + " "
															+ SimSEObjectTypeTypes.getText(
																	partTypeRuleEff.getSimSEObjectType().getType())
															+ " " + partAttRuleEff.getAttribute().getName()
															+ " attribute effect");
												}
												variableName.append(partName);
											}
											// get the action name:
											String actionName = nextToken.substring(nextToken.indexOf(':') + 1).trim();
											// check for validity of action name:
											if ((!(actionName.equals("*")))
													&& (actTypes.getActionType(actionName) == null)) {
												// invalid action name
												warnings.add("Invalid action name: \"" + actionName
														+ "\" in effect rule " + ruleName + " for "
														+ partRuleEff.getParticipant().getName() + " "
														+ objType + " "
														+ SimSEObjectTypeTypes.getText(
																partTypeRuleEff.getSimSEObjectType().getType())
														+ " " + partAttRuleEff.getAttribute().getName()
														+ " attribute effect");
											}
											if (actionName.equals("*")) { 
												// wildcard character -- any action
												variableName.append("A");
											} else { 
												// specific action name
												variableName.append(actionName);
											}
											if (vectorContainsString(variables, variableName.toString()) == false) { 
												// variable has not been generated yet
												// add the variable name to the record-keeping vector:
												variables.add(variableName.toString());
												methodBody.addStatement("double " + variableName + " = 0");
											}
											StringBuffer variableName2 = new StringBuffer();
											if ((variableName.indexOf("Active") >= 0)
													|| (variableName.indexOf("Inactive") >= 0)) {
												if (variableName.indexOf("Active") >= 0) { 
													// active
													variableName2.append("allActive" + partName + "s");
												} else if (variableName.indexOf("Inactive") >= 0) {
													// inactive
													variableName2.append("allInactive" + partName + "s");
												}
												if (vectorContainsString(variables,
														variableName2.toString()) == false) { 
													// variable has not been generated yet
													// add the variable name to the record-keeping vector
													variables.add(variableName2.toString()); 
													String active = "";
													if (variableName2.indexOf("Active") >= 0) {
														active = "Active";
													} else {
														active = "Inactive";
													}
													active += partName + "s()";
													methodBody.addStatement("Vector " + variableName2 + " = "
															+ actType.getName().toLowerCase() + "Act.getAll" + active);
												}
											} else {
												variableName2.append(partName.toLowerCase() + "s");
												if (vectorContainsString(variables,
														variableName2.toString()) == false) { 
													// variable has not been generated yet
													variables.add(variableName2.toString());
													methodBody.addStatement("Vector " + variableName2 + " = "
															+ actType.getName().toLowerCase() + "Act.getAll"
															+ partName + "s()");
												}
											}
											methodBody.beginControlFlow("for(int k=0; k<" + variableName2 + ".size(); k++)");
											methodBody.addStatement("Object " + partName.toLowerCase() + "3 = " + variableName2
													+ ".elementAt(k)");
											boolean objTypeNameSpecified = false;
											if ((objTypeName.equals(null) == false) && (objTypeName.length() > 0)) { 
												// an object type name was specified
												objTypeNameSpecified = true;
												methodBody.beginControlFlow("if(" + partName.toLowerCase() + "3 instanceof "
														+ objTypeName + ")");
											}
											String vectorCreation = "Vector ";
											String vectorName = new String();
											if (actionName.equals("*")) { 
												// wildcard
												vectorName = "actions";
												vectorCreation += vectorName
														+ " = state.getActionStateRepository().getAllActions()";
											} else { // action name specified
												vectorName = (actionName.toLowerCase() + "Actions");
												vectorCreation += vectorName + " = state.getActionStateRepository().get"
														+ CodeGeneratorUtils.getUpperCaseLeading(actionName)
														+ "ActionStateRepository().getAllActions()";
											}
											methodBody.addStatement(vectorCreation);
											methodBody.beginControlFlow("for(int m=0; m<" + vectorName + ".size(); m++)");
											String actionCond = "";
											if (actionName.equals("*")) {
												methodBody.addStatement("Action action = (Action)" + vectorName + ".elementAt(m)");
												actionCond += "if(action";
											} else { 
												// action name specified
												methodBody.addStatement(CodeGeneratorUtils.getUpperCaseLeading(actionName)
														+ "Action " + actionName.toLowerCase() + "Action = ("
														+ CodeGeneratorUtils.getUpperCaseLeading(actionName)
														+ "Action)" + actionName.toLowerCase()
														+ "Actions.elementAt(m)");
												actionCond += "if(" + actionName.toLowerCase() + "Action";
											}
											actionCond += ".getAllParticipants().contains(" + partName.toLowerCase() + "3))";
											methodBody.beginControlFlow(actionCond);
											methodBody.addStatement(variableName + "++;");
											methodBody.endControlFlow();
											methodBody.endControlFlow();
											if (objTypeNameSpecified) {
												methodBody.endControlFlow();
											}
											methodBody.endControlFlow();
										} else { 
											// num participants
											StringBuffer variableName = new StringBuffer("num");
											if (firstWord.indexOf("Active") >= 0) {
												variableName.append("Active");
											} else if (firstWord.indexOf("Inactive") >= 0) {
												variableName.append("Inactive");
											}
											// take off the first word:
											String tempStr = nextToken.substring(nextToken.indexOf('-') + 1); 
											String partName = new String();
											String ssObjType = new String();
											if (tempStr.indexOf('-') >= 0) { 
												// contains SimSEObjectType and meta type
												partName = tempStr.substring(0, tempStr.indexOf('-'));
												// check for validity of participant name:
												if (actType.getParticipant(partName) == null) {
													// invalid participant name
													warnings.add("Invalid participant name: \"" + partName
															+ "\" in effect rule " + ruleName + " for "
															+ partRuleEff.getParticipant().getName() + " "
															+ objType + " "
															+ SimSEObjectTypeTypes.getText(
																	partTypeRuleEff.getSimSEObjectType().getType())
															+ " " + partAttRuleEff.getAttribute().getName()
															+ " attribute effect");
												}
												variableName.append(partName);
												// take off the part name
												String tempStr2 = tempStr.substring(tempStr.indexOf('-') + 1); 
												ssObjType = tempStr2.substring(0, tempStr2.indexOf('-'));
												if (actType.getParticipant(partName) != null) {
													// valid participant name
													// check for validity of SimSEObjectType name:
													if (actType.getParticipant(partName)
															.getSimSEObjectType(ssObjType) == null) {
														// invalid SimSEObjectType name
														warnings.add("Invalid object type name: \"" + ssObjType
																+ "\" in effect rule " + ruleName + " for "
																+ partRuleEff.getParticipant().getName() + " "
																+ objType
																+ " "
																+ SimSEObjectTypeTypes.getText(partTypeRuleEff
																		.getSimSEObjectType().getType())
																+ " " + partAttRuleEff.getAttribute().getName()
																+ " attribute effect");
													}
												}
												variableName.append(ssObjType);
												if (vectorContainsString(variables,
														variableName.toString()) == false) { 
													// variable has not been generated yet
													// add the variable name to the record-keeping vector
													variables.add(variableName.toString()); 
													methodBody.addStatement("double " + variableName + " = 0");
													String variableName2 = new String();
													if (firstWord.indexOf("Active") >= 0) {
														variableName2 = ("allActive" + partName + "s");
													} else if (firstWord.indexOf("Inactive") >= 0) {
														variableName2 = ("allInactive" + partName + "s");
													} else { // active/inactive status not specified
														variableName2 = (partName.toLowerCase() + "s");
													}
													if ((vectorContainsString(ruleVariables,
															variableName2) == false)
															&& (vectorContainsString(variables,
																	variableName2) == false)) { // variable has
																								// not been
																								// generated yet
														/*
														 * dont' add it to the outer vector because if it's needed
														 * in the outer loop later, it won't be able to access it
														 * because this is being declared in the inner loop. Only
														 * add it to the inner one:
														 */
														variables.add(variableName2);
														String active = "";
														if (firstWord.indexOf("Active") >= 0) {
															active = "Active";
														} else if (firstWord.indexOf("Inactive") >= 0) {
															active = "Inactive";
														}
														active += partName + "s()";
														methodBody.addStatement("Vector " + variableName2 + " = "
																+ actType.getName().toLowerCase() + "Act.getAll" + active);
													}
													methodBody.beginControlFlow(
															"for(int k=0; k<" + variableName2 + ".size(); k++)");
													methodBody.addStatement("SSObject " + partName.toLowerCase()
															+ "2 = (SSObject)" + variableName2 + ".elementAt(k)");
													methodBody.beginControlFlow("if(" + partName.toLowerCase() + "2 instanceof "
															+ ssObjType + ")");
													methodBody.addStatement(variableName + "++");
													methodBody.endControlFlow();
													methodBody.endControlFlow();
												}
											} else { 
												// no SimSEObjectType and meta type specified
												partName = tempStr;
												// check for validity of participant name:
												if (actType.getParticipant(partName) == null) {
													// invalid participant name
													warnings.add("Invalid participant name: \"" + partName
															+ "\" in effect rule " + ruleName + " for "
															+ partRuleEff.getParticipant().getName() + " "
															+ objType + " "
															+ SimSEObjectTypeTypes.getText(
																	partTypeRuleEff.getSimSEObjectType().getType())
															+ " " + partAttRuleEff.getAttribute().getName()
															+ " attribute effect");
												}
												variableName.append(partName);
												if (vectorContainsString(variables,
														variableName.toString()) == false) { 
													// variable has not been generated yet
													// add the variable name to the record-keeping vector
													variables.add(variableName.toString()); 
													
													String active = "";
													if (firstWord.indexOf("Active") >= 0) {
														active = "Active";
													} else if (firstWord.indexOf("Inactive") >= 0) {
														active = "Inactive";
													}
													active += partName + "s().size())";
													methodBody.addStatement("double " + variableName + " = (double)("
															+ actType.getName().toLowerCase() + "Act.getAll" + active);
												}
											}
										}
									} else if ((nextToken.startsWith("input"))
											|| (nextToken.startsWith("-input"))) { // rule input
										if (effect.trim().length() == nextToken.trim().length()) {
											// on last token
											effect = null;
										} else { 
											// not on last token
											effect = effect.trim().substring(nextToken.length()).trim();
										}
										if (nextToken.startsWith("-")) {
											// remove the minus sign for now
											nextToken = nextToken.substring(1); 
										}
										// get the input name
										String inputName = nextToken.substring(nextToken.indexOf('-') + 1); 
										// check for validity of rule input name:
										if (effRule.getRuleInput(inputName) == null) { 
											// invalid rule input name
											warnings.add("Invalid rule input name: \"" + inputName
													+ "\" in effect rule " + ruleName + " for "
													+ partRuleEff.getParticipant().getName() + " "
													+ objType + " "
													+ SimSEObjectTypeTypes
															.getText(partTypeRuleEff.getSimSEObjectType().getType())
													+ " " + partAttRuleEff.getAttribute().getName()
													+ " attribute effect");
										} else { 
											// valid rule input name
											// check for validity of rule input type:
											if ((effRule.getRuleInput(inputName).getType()
													.equals(InputType.INTEGER) == false)
													&& (effRule.getRuleInput(inputName).getType()
															.equals(InputType.DOUBLE) == false)) {
												// non-numerical type (invalid)
												warnings.add("Invalid (non-numerical) rule input type: \""
														+ inputName + "\" in effect rule " + ruleName
														+ " for " + partRuleEff.getParticipant().getName() + " "
														+ objType + " "
														+ SimSEObjectTypeTypes.getText(
																partTypeRuleEff.getSimSEObjectType().getType())
														+ " " + partAttRuleEff.getAttribute().getName()
														+ " attribute effect");
											}
										}
									}

									else if (effect.trim().length() == nextToken.trim().length()) { 
										// on last token
										effect = null;
									} else {
										effect = effect.trim().substring(nextToken.length()).trim();
									}

									if ((effect == null) || (effect.trim().length() == 0)) {
										// that was the last token
										finished = true;
									}
								}

								// *********************SECOND
								// RUN-THROUGH***************************
								// go through the effect again to write out the whole
								// expression:
								effect = partAttRuleEff.getEffect();
								finished = false;
								StringBuffer expression = new StringBuffer();
								while (!finished) {
									String nextToken = getNextToken(effect);

									// attributes other participants:
									if (nextToken.startsWith("all") || nextToken.startsWith("-all")) {
										String token = nextToken;
										boolean isNegative = false;
										if (token.startsWith("-")) {
											isNegative = true;
											expression.append("(-1 * (");
											token = token.substring(1); // remove the minus sign for now
										}
										// get whether it's all, allActive, or allInactive:
										String activeInactiveToken = token.substring(0, token.indexOf('-'));
										token = token.substring(activeInactiveToken.length() + 1).trim();
										// get the participant name:
										String partName = token.substring(0, token.indexOf('-'));
										token = token.substring(partName.length() + 1).trim();
										// get the SimSEObjectType:
										String ssObjType = token.substring(0, token.indexOf('-'));
										token = token.substring(ssObjType.length() + 1).trim();
										String attName = token.substring(token.indexOf(':') + 1).trim();
										// append this variable name onto the expression:
										expression.append(activeInactiveToken + partName + ssObjType + attName);
										if (isNegative) {
											expression.append("))");
										}
										expression.append(" ");
									}

									// num actions or num participants:
									else if (nextToken.startsWith("num") || nextToken.startsWith("-num")) {
										String token = nextToken;
										boolean isNegative = false;
										if (token.startsWith("-")) {
											isNegative = true;
											expression.append("(-1 * (");
											token = token.substring(1); // remove the minus sign for now
										}
										String firstWord = token.substring(0, token.indexOf('-'));

										if (firstWord.endsWith("This")) { // num actions this participant
											String actionName = token.substring(token.indexOf(':') + 1).trim();
											StringBuffer variableName = new StringBuffer("num");
											if (firstWord.indexOf("Active") >= 0) {
												variableName.append("Active");
											} else if (firstWord.indexOf("Inactive") >= 0) {
												variableName.append("Inactive");
											}
											variableName.append("ActionsThisPart");
											if (actionName.equals("*")) { // wild card character -- all actions
												variableName.append("A");
											} else { // action name
												variableName.append(actionName);
											}
											// append the variable name to the expression:
											expression.append(variableName);
											if (isNegative) {
												expression.append("))");
											}
											expression.append(" ");
										} else if (firstWord.indexOf("All") >= 0) { 
											// num actions other participants
											StringBuffer variableName = new StringBuffer("numActionsAll");
											if (firstWord.indexOf("Active") >= 0) {
												variableName.append("Active");
											} else if (firstWord.indexOf("Inactive") >= 0) {
												variableName.append("Inactive");
											}
											// take off the first word
											String tempStr = token.substring(token.indexOf('-') + 1); 
											String partName = new String();
											String objTypeName = new String();
											if (tempStr.indexOf('-') >= 0) { 
												// includes SimSEObjectType and meta type
												// get the participant name:
												partName = tempStr.substring(0, tempStr.indexOf('-'));
												variableName.append(partName);
												// take off the participant name:
												tempStr = tempStr.substring(tempStr.indexOf('-') + 1);
												// get the SimSEObjectType name
												objTypeName = tempStr.substring(0, tempStr.indexOf('-')); 
												variableName.append(objTypeName);
											} else { 
												// does not include SimSEObjectType and meta-type get the participant name:
												partName = tempStr.substring(0, tempStr.indexOf(':'));
												variableName.append(partName);
											}
											// get the action name
											String actionName = token.substring(token.indexOf(':') + 1).trim(); 
											if (actionName.equals("*")) { 
												// wildcard character -- any action
												variableName.append("A");
											} else { 
												// specific action name
												variableName.append(actionName);
											}
											// append the variable name to the expression:
											expression.append(variableName);
											if (isNegative) {
												expression.append("))");
											}
											expression.append(" ");
										} else { 
											// num participants
											StringBuffer variableName = new StringBuffer("num");
											if (firstWord.indexOf("Active") >= 0) {
												variableName.append("Active");
											} else if (firstWord.indexOf("Inactive") >= 0) {
												variableName.append("Inactive");
											}
											// take off the first word
											String tempStr = token.substring(token.indexOf('-') + 1); 
											String partName = new String();
											String ssObjType = new String();
											if (tempStr.indexOf('-') >= 0) { 
												// contains SimSEObjectType and meta type
												partName = tempStr.substring(0, tempStr.indexOf('-'));
												variableName.append(partName);
												// take off the part name:
												String tempStr2 = tempStr.substring(tempStr.indexOf('-') + 1);
												ssObjType = tempStr2.substring(0, tempStr2.indexOf('-'));
												variableName.append(ssObjType);
												// append the variable name to the expression:
												expression.append(variableName);
												if (isNegative) {
													expression.append("))");
												}
												expression.append(" ");
											} else { // no SimSEObjectType and meta type specified
												partName = tempStr;
												variableName.append(partName);
												// append the variable name to the expression:
												expression.append(variableName);
												if (isNegative) {
													expression.append("))");
												}
												expression.append(" ");
											}
										}
									}

									// rule input:
									else if ((nextToken.startsWith("input")) || (nextToken.startsWith("-input"))) {
										String token = nextToken;
										boolean isNegative = false;
										if (token.startsWith("-")) {
											isNegative = true;
											expression.append("(-1 * (");
											// remove the minus sign for now:
											token = token.substring(1);
										}
										// get the input name:
										String inputName = token.substring(token.indexOf('-') + 1);
										// append the variable name to the expression:
										expression.append("input" + inputName);
										if (isNegative) {
											expression.append("))");
										}
										expression.append(" ");
									}

									// attributes this participant:
									else if ((nextToken.startsWith("this")) || (nextToken.startsWith("-this"))) {
										String token = nextToken;
										boolean isNegative = false;
										if (token.startsWith("-")) {
											isNegative = true;
											expression.append("(-1 * (");
											// remove the minus sign for now:
											token = token.substring(1);
										}
										// append to expression:
										expression.append("((double)("
												+ objType.toLowerCase()
												+ ".get" + CodeGeneratorUtils.getUpperCaseLeading(
														token.substring(token.indexOf(':') + 1))
												+ "()))");
										if (isNegative) {
											expression.append("))");
										}
										expression.append(" ");
									}

									// total time elapsed:
									else if ((nextToken.startsWith("totalTimeElapsed"))
											|| (nextToken.startsWith("-totalTimeElapsed"))) {
										boolean isNegative = false;
										if (nextToken.startsWith("-")) {
											isNegative = true;
											expression.append("(-1 * (");
										}
										// append to expression:
										expression.append("((double)(state.getClock().getTime()))");
										if (isNegative) {
											expression.append("))");
										}
										expression.append(" ");
									}

									// action time elapsed:
									else if ((nextToken.startsWith("actionTimeElapsed"))
											|| (nextToken.startsWith("-actionTimeElapsed"))) {
										boolean isNegative = false;
										if (nextToken.startsWith("-")) {
											isNegative = true;
											expression.append("(-1 * (");
										}
										// append to expression:
										expression.append("((double)(" + actType.getName().toLowerCase()
												+ "Act.getTimeElapsed()))");
										if (isNegative) {
											expression.append("))");
										}
										expression.append(" ");
									}

									// random:
									else if ((nextToken.startsWith("random"))
											|| (nextToken.startsWith("-random"))) {
										String token = nextToken;
										boolean isNegative = false;
										if (token.startsWith("-")) {
											isNegative = true;
											expression.append("(-1 * (");
											// remove the minus sign for now:
											token = token.substring(1);
										}
										// get min & max vals:
										try {
											Integer minVal = Integer.valueOf(token.substring((token.indexOf(':') + 1), token.indexOf(',')));
											Integer maxVal = Integer.valueOf(token.substring(token.indexOf(',') + 1));
											// append to expression:
											expression.append("((double)((ranNumGen.nextInt(" + maxVal + " - "
													+ minVal + " + 1) + " + minVal + ")))");
											if (isNegative) {
												expression.append("))");
											}
											expression.append(" ");
										} catch (NumberFormatException e) {
											JOptionPane.showMessageDialog(null,
													("Error reading random value in expression for  "
															+ ruleName + " effect rule: " + e.toString()),
													"Malformed Effect Rule Expression",
													JOptionPane.WARNING_MESSAGE);
										}
									} else {
										// other token:
										expression.append(nextToken + " ");
									}

									if (effect.trim().length() == nextToken.trim().length()) {
										// on last token
										effect = null;
									} else { 
										// not on last token
										effect = effect.trim().substring(nextToken.length());
									}

									if ((effect == null) || (effect.trim().length() == 0)) {
										// that was the last token
										finished = true;
									}
								}
								String attType = new String();
								if (partAttRuleEff.getAttribute().getType() == AttributeTypes.INTEGER) {
									attType = "int";
								} else if (partAttRuleEff.getAttribute().getType() == AttributeTypes.DOUBLE) {
									attType = "double";
								}
								methodBody.addStatement(objType.toLowerCase() + ".set"
										+ CodeGeneratorUtils
												.getUpperCaseLeading(partAttRuleEff.getAttribute().getName())
										+ "((" + attType + ")(" + expression.toString().trim() + "))");
							} else { 
								// string or boolean attribute
								String effect = partAttRuleEff.getEffect();
								String nextToken = getNextToken(effect);

								// attribute this participant:
								if (nextToken.startsWith("this")) {
									// get the attribute name:
									String attName = nextToken.substring(nextToken.indexOf(':') + 1);
									// check for validity of attribute name:
									if (partTypeRuleEff.getSimSEObjectType().getAttribute(attName) == null) { 
										// invalid attribute name
										warnings.add("Invalid attribute name: \"" + attName + "\" in effect rule "
												+ ruleName + " for "
												+ partRuleEff.getParticipant().getName() + " "
												+ objType + " "
												+ SimSEObjectTypeTypes.getText(
														partTypeRuleEff.getSimSEObjectType().getType())
												+ " " + partAttRuleEff.getAttribute().getName()
												+ " attribute effect");
									} else { 
										// valid attribute name
										// check for validity of attribute type:
										if (partTypeRuleEff.getSimSEObjectType().getAttribute(attName)
												.getType() != partAttRuleEff.getAttribute().getType()) { 
											// invalid attribute type
											warnings.add("Invalid attribute type ("
													+ AttributeTypes.getText(partTypeRuleEff.getSimSEObjectType()
															.getAttribute(attName).getType())
													+ "): \"" + attName + "\" in effect rule " + ruleName
													+ " for " + partRuleEff.getParticipant().getName() + " "
													+ objType + " "
													+ SimSEObjectTypeTypes
															.getText(partTypeRuleEff.getSimSEObjectType().getType())
													+ " " + partAttRuleEff.getAttribute().getName()
													+ " attribute effect");
										}
									}
									methodBody.addStatement(objType.toLowerCase()
											+ ".set" + partAttRuleEff.getAttribute().getName() + "("
											+ objType.toLowerCase() + ".get"
											+ attName + "())");
								}

								// rule input:
								else if (nextToken.startsWith("input")) {
									// get the input name:
									String inputName = nextToken.substring(nextToken.indexOf('-') + 1);
									// check for validity of rule input name:
									if (effRule.getRuleInput(inputName) == null) { 
										// invalid rule input name
										warnings.add("Invalid rule input name: \"" + inputName
												+ "\" in effect rule " + ruleName + " for "
												+ partRuleEff.getParticipant().getName() + " "
												+ objType + " "
												+ SimSEObjectTypeTypes.getText(
														partTypeRuleEff.getSimSEObjectType().getType())
												+ " " + partAttRuleEff.getAttribute().getName()
												+ " attribute effect");
									} else { 
										// valid rule input name
										// check for validity of rule input type:
										if (partAttRuleEff.getAttribute().getType() == AttributeTypes.STRING) { 
											// string attribute
											if (effRule.getRuleInput(inputName).getType()
													.equals(InputType.STRING) == false) { 
												// type doesn't match (invalid)
												warnings.add("Invalid rule input type (non-String): \"" + inputName
														+ "\" in effect rule " + ruleName + " for "
														+ partRuleEff.getParticipant().getName() + " "
														+ objType + " "
														+ SimSEObjectTypeTypes.getText(
																partTypeRuleEff.getSimSEObjectType().getType())
														+ " " + partAttRuleEff.getAttribute().getName()
														+ " attribute effect");
											}
										} else if (partAttRuleEff.getAttribute()
												.getType() == AttributeTypes.BOOLEAN) { 
											// boolean attribute
											if (effRule.getRuleInput(inputName).getType()
													.equals(InputType.BOOLEAN) == false) { 
												// type doesn't match -- invalid
												warnings.add("Invalid rule input type (non-Boolean): \"" + inputName
														+ "\" in effect rule " + ruleName + " for "
														+ partRuleEff.getParticipant().getName() + " "
														+ objType + " "
														+ SimSEObjectTypeTypes.getText(
																partTypeRuleEff.getSimSEObjectType().getType())
														+ " " + partAttRuleEff.getAttribute().getName()
														+ " attribute effect");
											}
										}

										// write the expression:
										methodBody.addStatement(objType.toLowerCase()
												+ ".set" + partAttRuleEff.getAttribute().getName() + "(input"
												+ inputName + ")");
									}
								} else if (nextToken.startsWith("\"")) {
									// literal string:
									if (partAttRuleEff.getAttribute().getType() != AttributeTypes.STRING) { 
										// invalid
										warnings.add("Invalid expression (wrong type) in effect rule "
												+ ruleName + " for "
												+ partRuleEff.getParticipant().getName() + " "
												+ objType + " "
												+ SimSEObjectTypeTypes.getText(
														partTypeRuleEff.getSimSEObjectType().getType())
												+ " " + partAttRuleEff.getAttribute().getName()
												+ " attribute effect");
									}
									// write the expression:
									methodBody.addStatement(objType.toLowerCase()
											+ ".set" + partAttRuleEff.getAttribute().getName() + "(" + effect.trim()
											+ ")");
								} else if (nextToken.startsWith("true")) {
									// boolean val:
									if (partAttRuleEff.getAttribute().getType() != AttributeTypes.BOOLEAN) { // invalid
										warnings.add("Invalid expression (wrong type) in effect rule "
												+ ruleName + " for "
												+ partRuleEff.getParticipant().getName() + " "
												+ objType + " "
												+ SimSEObjectTypeTypes.getText(
														partTypeRuleEff.getSimSEObjectType().getType())
												+ " " + partAttRuleEff.getAttribute().getName()
												+ " attribute effect");
									}
									// write the expression:
									methodBody.addStatement(objType.toLowerCase()
											+ ".set" + partAttRuleEff.getAttribute().getName() + "(true)");
								} else if (nextToken.startsWith("false")) {
									if (partAttRuleEff.getAttribute().getType() != AttributeTypes.BOOLEAN) { 
										// invalid
										warnings.add("Invalid expression (wrong type) in effect rule "
												+ ruleName + " for "
												+ partRuleEff.getParticipant().getName() + " "
												+ objType + " "
												+ SimSEObjectTypeTypes.getText(
														partTypeRuleEff.getSimSEObjectType().getType())
												+ " " + partAttRuleEff.getAttribute().getName()
												+ " attribute effect");
									}
									// write the expression:
									methodBody.addStatement(objType.toLowerCase()
											+ ".set" + partAttRuleEff.getAttribute().getName() + "(false)");
								}
							}
						}
					}
					if (k == partTypeEffects.size() - 1) { 
						methodBody.endControlFlow();
					}
				}
				methodBody.endControlFlow();
			}
			if (rInputs.size() > 0) {
				methodBody.endControlFlow();
			}
			if (!minCondition.equals("if ()")) {
				methodBody.endControlFlow();
			}
			if ((rule.getTiming() == RuleTiming.TRIGGER) || (rule.getTiming() == RuleTiming.DESTROYER)) {
				methodBody.endControlFlow();
			}
			methodBody.endControlFlow();
			methodBody.endControlFlow();
		} else if (rule instanceof CreateObjectsRule) { 
			// CREATE OBJECTS RULE
			CreateObjectsRule coRule = (CreateObjectsRule) rule;
			String ruleCond = "if((updateInstructions ==";
			if (rule.getTiming() == RuleTiming.CONTINUOUS) { 
				// continuous rule
				ruleCond += "UPDATE_ALL_CONTINUOUS))";
			} else if ((rule.getTiming() == RuleTiming.TRIGGER) || (rule.getTiming() == RuleTiming.DESTROYER)) { 
				// trigger/destroyer rule
				ruleCond += "UPDATE_ONE) && (ruleName.equals(\"" + ruleName + "\")))";
			}
			methodBody.beginControlFlow(ruleCond);
			methodBody.beginControlFlow("for(int i=0; i<" + actType.getName().toLowerCase() + "Acts.size(); i++)");
			methodBody.addStatement(CodeGeneratorUtils.getUpperCaseLeading(actType.getName()) + "Action "
					+ actType.getName().toLowerCase() + "Act = ("
					+ CodeGeneratorUtils.getUpperCaseLeading(actType.getName()) + "Action)"
					+ actType.getName().toLowerCase() + "Acts.elementAt(i)");
			if ((rule.getTiming() == RuleTiming.TRIGGER) || (rule.getTiming() == RuleTiming.DESTROYER)) {
				methodBody.beginControlFlow("if(" + actType.getName().toLowerCase() + "Act == action)");
			}
			// code to make sure it has the min num of participants:
			String minCondition = "if (";
			Vector<ActionTypeParticipant> parts = actType.getAllParticipants();
			for (int i = 0; i < parts.size(); i++) {
				ActionTypeParticipant part = parts.elementAt(i);
				if (i > 0) { 
					// not on first element
					minCondition += " && ";
				}
				minCondition += "(" + oneActTypeVar + ".getAll" + part.getName() + "s().size() >= ";
				if (part.getQuantity().isMinValBoundless()) { 
					// min val boundless
					minCondition += "0";
				} else { 
					// has min val
					minCondition += part.getQuantity().getMinVal().toString();
				}
				minCondition += ")";
			}
			minCondition += ")";
			if (!minCondition.equals("if ()")) {
				methodBody.beginControlFlow(minCondition);
			}

			// go through all of the objects to create:
			Vector<SimSEObject> objsToCreate = coRule.getAllSimSEObjects();
			for (int i = 0; i < objsToCreate.size(); i++) {
				StringBuffer lineToCreateObj = new StringBuffer();
				SimSEObject obj = objsToCreate.elementAt(i);
				lineToCreateObj.append(CodeGeneratorUtils.getUpperCaseLeading(obj.getSimSEObjectType().getName())
						+ " " + obj.getSimSEObjectType().getName().toLowerCase() + i + " = new "
						+ CodeGeneratorUtils.getUpperCaseLeading(obj.getSimSEObjectType().getName()) + "(");
				boolean createObj = true;
				// go through all instantiated attributes:
				Vector<InstantiatedAttribute> instAtts = obj.getAllAttributes();
				if (instAtts.size() >= obj.getSimSEObjectType().getAllAttributes().size()) { 
					// all attributes are instantiated
					for (int j = 0; j < instAtts.size(); j++) {
						InstantiatedAttribute att = instAtts.elementAt(j);
						if (att.isInstantiated() == false) { // not instantiated
							warnings.add("Not all attributes have been assigned starting values for the "
									+ obj.getSimSEObjectType().getName() + " "
									+ SimSEObjectTypeTypes.getText(obj.getSimSEObjectType().getType())
									+ " created in the " + coRule.getName() + " Create Objects Rule");
							createObj = false;
							break;
						} else { 
							// instantiated
							if (att.getAttribute().getType() == AttributeTypes.STRING) {
								// string attribute
								lineToCreateObj.append("\"");
							}
							lineToCreateObj.append(att.getValue());
							if (att.getAttribute().getType() == AttributeTypes.STRING) {
								// string attribute
								lineToCreateObj.append("\"");
							}
							if (j < (instAtts.size() - 1)) { 
								// not on last iteration
								lineToCreateObj.append(", ");
							}
						}
					}
				} else { 
					// not all atts are instantiated
					warnings.add("Not all attributes have been assigned starting values for the "
							+ obj.getSimSEObjectType().getName() + " "
							+ SimSEObjectTypeTypes.getText(obj.getSimSEObjectType().getType()) + " created in the "
							+ coRule.getName() + " Create Objects Rule");
					createObj = false;
				}
				if (createObj) {
					lineToCreateObj.append(")");
					methodBody.addStatement(lineToCreateObj.toString());
					methodBody.addStatement("state.get" + SimSEObjectTypeTypes.getText(obj.getSimSEObjectType().getType())
							+ "StateRepository().get"
							+ CodeGeneratorUtils.getUpperCaseLeading(obj.getSimSEObjectType().getName())
							+ "StateRepository().add(" + obj.getSimSEObjectType().getName().toLowerCase() + i
							+ ")");
				}
			}
			methodBody.addStatement("(($T) gui).forceGUIUpdate()", simseGui);
			if (!minCondition.equals("if ()")) {
				methodBody.endControlFlow();
			}
			if ((rule.getTiming() == RuleTiming.TRIGGER) || (rule.getTiming() == RuleTiming.DESTROYER)) {
				methodBody.endControlFlow();
			}
			methodBody.endControlFlow();
			methodBody.endControlFlow();
		} else if (rule instanceof DestroyObjectsRule) { 
			// DESTROY OBJECTS RULE
			DestroyObjectsRule doRule = (DestroyObjectsRule) rule;
			String ruleCond = "if((updateInstructions ==";
			if (rule.getTiming() == RuleTiming.CONTINUOUS) { 
				// continuous rule
				ruleCond += "UPDATE_ALL_CONTINUOUS))";
			} else if ((rule.getTiming() == RuleTiming.TRIGGER) || (rule.getTiming() == RuleTiming.DESTROYER)) { 
				// trigger/destroyer rule
				ruleCond += "UPDATE_ONE) && (ruleName.equals(\"" + ruleName + "\")))";
			}
			methodBody.beginControlFlow(ruleCond);
			methodBody.beginControlFlow("for(int i=0; i<" + actType.getName().toLowerCase() + "Acts.size(); i++)");
			methodBody.addStatement(CodeGeneratorUtils.getUpperCaseLeading(actType.getName()) + "Action "
					+ actType.getName().toLowerCase() + "Act = ("
					+ CodeGeneratorUtils.getUpperCaseLeading(actType.getName()) + "Action)"
					+ actType.getName().toLowerCase() + "Acts.elementAt(i)");
			if ((rule.getTiming() == RuleTiming.TRIGGER) || (rule.getTiming() == RuleTiming.DESTROYER)) {
				methodBody.beginControlFlow("if(" + actType.getName().toLowerCase() + "Act == action)");
			}
			// code to make sure it has the min num of participants:
			String minCondition = "if (";
			Vector<ActionTypeParticipant> parts = actType.getAllParticipants();
			for (int i = 0; i < parts.size(); i++) {
				ActionTypeParticipant part = parts.elementAt(i);
				if (i > 0) { 
					// not on first element
					minCondition += " && ";
				}
				minCondition += "(" + oneActTypeVar + ".getAll" + part.getName() + "s().size() >= ";
				if (part.getQuantity().isMinValBoundless()) { 
					// min val boundless
					minCondition += "0";
				} else { 
					// has min val
					minCondition += part.getQuantity().getMinVal().toString();
				}
				minCondition += ")";
			}
			minCondition += ")";
			if (!minCondition.equals("if ()")) {
				methodBody.beginControlFlow(minCondition);
			}

			// go through each participant condition:
			Vector<DestroyObjectsRuleParticipantCondition> partConditions = doRule.getAllParticipantConditions();
			for (int j = 0; j < partConditions.size(); j++) {
				DestroyObjectsRuleParticipantCondition cond = partConditions.elementAt(j);
				ActionTypeParticipant part = cond.getParticipant();

				methodBody.addStatement("Vector " + part.getName().toLowerCase() + "s = (("
						+ CodeGeneratorUtils.getUpperCaseLeading(actType.getName()) + "Action)"
						+ actType.getName().toLowerCase() + "Act).getAll" + part.getName() + "s()");
				methodBody.beginControlFlow("for(int j=0; j<" + part.getName().toLowerCase() + "s.size(); j++)");
				methodBody.addStatement(SimSEObjectTypeTypes.getText(part.getSimSEObjectTypeType()) + " a = ("
						+ SimSEObjectTypeTypes.getText(part.getSimSEObjectTypeType()) + ")"
						+ part.getName().toLowerCase() + "s.elementAt(j)");
				// go through all participant constraints:
				Vector<ActionTypeParticipantConstraint> constraints = cond.getAllConstraints();
				for (int k = 0; k < constraints.size(); k++) {
					ActionTypeParticipantConstraint constraint = constraints.elementAt(k);
					String objTypeName = constraint.getSimSEObjectType().getName();
					if (k == 0) { 
						// on first element
						methodBody.beginControlFlow("if (a instanceof " + CodeGeneratorUtils.getUpperCaseLeading(objTypeName) + ")");
					} else {
						methodBody.nextControlFlow("if (a instanceof " + CodeGeneratorUtils.getUpperCaseLeading(objTypeName) + ")");
					}
					// go through all attribute constraints:
					ActionTypeParticipantAttributeConstraint[] attConstraints = constraint.getAllAttributeConstraints();
					int numAttConsts = 0;
					String ifCond = "";
					for (int m = 0; m < attConstraints.length; m++) {
						ActionTypeParticipantAttributeConstraint tempAttConst = attConstraints[m];
						if (tempAttConst.isConstrained()) {
							if (numAttConsts == 0) { 
								// this is the first attribute that we've come across that's constrained
								ifCond += "if (";
							} else {
								ifCond += " && ";
							}
							ifCond += "(((" + CodeGeneratorUtils.getUpperCaseLeading(objTypeName) + ")a).get"
									+ CodeGeneratorUtils.getUpperCaseLeading(tempAttConst.getAttribute().getName())
									+ "()";
							if (tempAttConst.getAttribute().getType() == AttributeTypes.STRING) {
								ifCond += ".equals(" + "\"" + tempAttConst.getValue().toString() + "\")";
							} else {
								if (tempAttConst.getGuard().equals(AttributeGuard.EQUALS)) {
									ifCond += " == ";
								} else {
									ifCond += " " + tempAttConst.getGuard() + " ";
								}
								ifCond += tempAttConst.getValue().toString();
							}
							ifCond += ")";
							numAttConsts++;
						}
					}
					if (numAttConsts > 0) { 
						// there is at least one constraint
						ifCond += ")";
						methodBody.beginControlFlow(ifCond);
						methodBody.addStatement("state.get"
								+ SimSEObjectTypeTypes.getText(constraint.getSimSEObjectType().getType())
								+ "StateRepository().get"
								+ CodeGeneratorUtils.getUpperCaseLeading(constraint.getSimSEObjectType().getName())
								+ "StateRepository().remove(("
								+ CodeGeneratorUtils.getUpperCaseLeading(constraint.getSimSEObjectType().getName())
								+ ")a)");
						methodBody.addStatement("state.getActionStateRepository().removeFromAllActions(a)");
						methodBody.addStatement("checkAllMins(gui)");
						methodBody.endControlFlow();
					} else { 
						// no constraints -- destroy object
						methodBody.addStatement("state.get"
								+ SimSEObjectTypeTypes.getText(constraint.getSimSEObjectType().getType())
								+ "StateRepository().get"
								+ CodeGeneratorUtils.getUpperCaseLeading(constraint.getSimSEObjectType().getName())
								+ "StateRepository().remove(("
								+ CodeGeneratorUtils.getUpperCaseLeading(constraint.getSimSEObjectType().getName())
								+ ")a)");
						methodBody.addStatement("state.getActionStateRepository().removeFromAllActions(a)");
						methodBody.addStatement("checkAllMins(gui)");
					}
					methodBody.addStatement("(($T) gui).forceGUIUpdate()", simseGui);
					if (k == constraints.size() - 1) { 
						methodBody.endControlFlow();
					}
				}
				methodBody.endControlFlow();
			}
			if (!minCondition.equals("if ()")) {
				methodBody.endControlFlow(minCondition);
			}
			if ((rule.getTiming() == RuleTiming.TRIGGER) || (rule.getTiming() == RuleTiming.DESTROYER)) {
				methodBody.endControlFlow();
			}
			methodBody.endControlFlow();
			methodBody.endControlFlow();
		}
		
		MethodSpec method = MethodSpec.methodBuilder(methodName)
				.addModifiers(Modifier.PRIVATE)
				.addJavadoc("// $L rule ($L Action):", ruleName, actTypeName)
				.addParameter(stage, "gui")
				.addParameter(int.class, "updateInstructions")
				.addParameter(String.class, "ruleName")
				.addParameter(action, "action")
				.addCode(methodBody.build())
				.build();
		genMethods.add(method);
	}
	
	private CodeBlock.Builder minConditions() {
		ClassName ssObject = ClassName.get("simse.adts.objects", "SSObject");
		TypeName vectorOfObjs = ParameterizedTypeName.get(vector, ssObject);
		
		CodeBlock.Builder conditions = CodeBlock.builder();
		
		// checkAllMins method:
		Vector<ActionType> actions = actTypes.getAllActionTypes();
		// go through all action types:
		for (int i = 0; i < actions.size(); i++) {
			ActionType actType = actions.elementAt(i);
			String actTypeName = actType.getName();
			String uCaseActionName = CodeGeneratorUtils.getUpperCaseLeading(actType.getName()) + "Action";
			String actTypeVar = actTypeName.toLowerCase() + "Acts";
			String oneActTypeVar = actTypeName.toLowerCase() + "Act";
			
			ClassName actClass = ClassName.get("simse.adts.actions", uCaseActionName);
			TypeName vectorOfActTypes = ParameterizedTypeName.get(vector, actClass);
			if (i == 0) { 
				// on first element
				conditions.beginControlFlow("if (act instanceof $T)", actClass);
			} else {
				conditions.nextControlFlow("else if (act instanceof $T)", actClass);
			}
			conditions.addStatement("$T b = ($T) act", actClass, actClass);
			
			String ifCond = "if (";
			// go through all participants:
			Vector<ActionTypeParticipant> parts = actType.getAllParticipants();
			for (int j = 0; j < parts.size(); j++) {
				if (j > 0) { 
					// not on first element
					ifCond += " || ";
				}
				ActionTypeParticipant part = parts.elementAt(j);
				ifCond += "(b.getAll" + part.getName() + "s().size() < ";
				if (part.getQuantity().isMinValBoundless()) { 
					// min val boundless
					ifCond += "-999999";
				} else { 
					// min val has a value
					ifCond += "" + part.getQuantity().getMinVal().intValue();
				}
				ifCond += ")";
			}
			ifCond += ")";
			if (!ifCond.equals("if ()")) {
				conditions.beginControlFlow(ifCond);
			}

			// get the destroyer text from the highest priority destroyer:
			String destText = new String();
			if (actType.getAllDestroyers().size() > 0) { // has at least one destroyer
				ActionTypeDestroyer highestPriDest = (ActionTypeDestroyer) actType.getAllDestroyers().elementAt(0);
				Vector<ActionTypeDestroyer> allDests = actType.getAllDestroyers();
				for (int j = 0; j < allDests.size(); j++) {
					ActionTypeDestroyer tempDest = allDests.elementAt(j);
					if (tempDest.getPriority() > highestPriDest.getPriority()) {
						highestPriDest = tempDest;
					}
				}
				destText = highestPriDest.getDestroyerText();
				if ((destText != null) && (destText.length() > 0)) { 
					// has destroyer text
					conditions.addStatement("$T c = b.getAllParticipants()", vectorOfObjs);
					conditions.beginControlFlow("for (int j = 0; j < c.size(); j++)");
					conditions.addStatement("$T d = c.elementAt(j)", ssObject);
					conditions.beginControlFlow("if (d instanceof $T)", employee);
					conditions.addStatement("(($T) d).setOverheadText($S)", employee, destText);
					conditions.nextControlFlow("else if (d instanceof $T)", customer);
					conditions.addStatement("(($T) d).setOverheadText($S)", customer, destText);
					conditions.endControlFlow();
					conditions.endControlFlow();
				}
			}

			// get any destroyer rules:
			Vector<Rule> destRules = actType.getAllDestroyerRules();
			for (int j = 0; j < destRules.size(); j++) {
				Rule r = destRules.elementAt(j);
				conditions.addStatement("update(parent, UPDATE_ONE, \"" + r.getName() + "\", b)");
			}
			conditions.addStatement("state.getActionStateRepository().get$LStateRepository().remove(b)", uCaseActionName);

			// game-ending?:
			// get the highest priority destroyer:
			if (actType.getAllDestroyers().size() > 0) { // has at least one destroyer
				ActionTypeDestroyer highestPriDest = actType.getAllDestroyers().elementAt(0);
				Vector<ActionTypeDestroyer> allDests = actType.getAllDestroyers();
				for (int j = 0; j < allDests.size(); j++) {
					ActionTypeDestroyer tempDest = allDests.elementAt(j);
					if (tempDest.getPriority() > highestPriDest.getPriority()) {
						highestPriDest = tempDest;
					}
				}

				if (highestPriDest.isGameEndingDestroyer()) {
					conditions.add("// stop game and give score:\n");
					conditions.addStatement("$T t111 = ($T)b", actClass, actClass);
					
					// find the scoring attribute:
					ActionTypeParticipantDestroyer scoringPartDest = null;
					ActionTypeParticipantConstraint scoringPartConst = null;
					ActionTypeParticipantAttributeConstraint scoringAttConst = null;
					Vector<ActionTypeParticipantDestroyer> partDests = highestPriDest.getAllParticipantDestroyers();
					for (int j = 0; j < partDests.size(); j++) {
						ActionTypeParticipantDestroyer partDest = partDests.elementAt(j);
						Vector<ActionTypeParticipantConstraint> partConsts = partDest.getAllConstraints();
						for (int k = 0; k < partConsts.size(); k++) {
							ActionTypeParticipantConstraint partConst = partConsts.elementAt(k);
							ActionTypeParticipantAttributeConstraint[] attConsts = partConst
									.getAllAttributeConstraints();
							for (int m = 0; m < attConsts.length; m++) {
								if (attConsts[m].isScoringAttribute()) {
									scoringAttConst = attConsts[m];
									scoringPartConst = partConst;
									scoringPartDest = partDest;
									break;
								}
							}
						}
					}
					String scoringPartVarName = scoringPartDest.getParticipant().getName() + "s";
					String scoringPartConstObj = CodeGeneratorUtils
							.getUpperCaseLeading(scoringPartConst.getSimSEObjectType().getName());
					ClassName scoringPartConstObjName = ClassName.get("simse.adts.objects", scoringPartConstObj);
					if ((scoringAttConst != null) && (scoringPartConst != null) && (scoringPartDest != null)) {
						conditions.beginControlFlow("if (t111.getAll" + scoringPartVarName + "().size() > 0)");
						conditions.addStatement("$T t = ($T)(t111.getAll" + scoringPartVarName + "().elementAt(0))"
								, scoringPartConstObjName, scoringPartConstObjName);
						conditions.beginControlFlow("if (t != null)");
						
						ClassName scoreType = null;
						if (scoringAttConst.getAttribute().getType() == AttributeTypes.INTEGER) {
							scoreType = ClassName.get(int.class);
						} else if (scoringAttConst.getAttribute().getType() == AttributeTypes.DOUBLE) {
							scoreType = ClassName.get(double.class);
						} else if (scoringAttConst.getAttribute().getType() == AttributeTypes.STRING) {
							scoreType = ClassName.get(String.class);
						} else if (scoringAttConst.getAttribute().getType() == AttributeTypes.BOOLEAN) {
							scoreType = ClassName.get(boolean.class);
						}
						conditions.addStatement("$T v = t.get" + scoringAttConst.getAttribute().getName() + "()", scoreType);
						conditions.addStatement("state.getClock().stop()");
						conditions.addStatement("state.setScore(v)");
						conditions.addStatement("(($T)parent).update()", simseGui);
						conditions.addStatement("$T d = new $T($T.INFORMATION)", alert, alert, alertType);
						conditions.addStatement("d.setContentText(($S + v))", "Your score is ");
						conditions.addStatement("d.setTitle($S)", "Game over!");
						conditions.addStatement("d.setHeaderText(null)");
						conditions.addStatement("d.showAndWait()");
						conditions.endControlFlow();
						conditions.endControlFlow(); // game ending if condition
					}
				}
			}
			if (!ifCond.equals("if ()")) {
				conditions.endControlFlow();
			}
			if (i == actions.size() - 1) {
				conditions.endControlFlow();
			}
		}
		
		return conditions;
	}

	private boolean vectorContainsString(Vector<String> v, String s) {
		for (int i = 0; i < v.size(); i++) {
			String temp = v.elementAt(i);
			if (temp.equals(s)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * returns the next token in the token string
	 */
	private String getNextToken(String tokenString) {
		// System.out.println("********** At the beginning of getLastToken for
		// tokenString = *" + tokenString + "*");
		// System.out.println("(tokenString.trim().indexOf(' ') = " +
		// tokenString.trim().indexOf(' '));
		// System.out.println("(tokenString.trim().indexOf('(') = " +
		// tokenString.trim().indexOf('('));
		// System.out.println("(tokenString.trim().indexOf(')') = " +
		// tokenString.trim().indexOf(')'));
		if ((tokenString.equals(null)) || (tokenString.length() == 0)) { // empty
																			// text
																			// field
			// System.out.println("Empty tokenString!");
			return null;
		} else if ((tokenString.trim().indexOf(' ') < 0) && (tokenString.indexOf('(') < 0)
				&& (tokenString.indexOf(')') < 0)) {
			// no spaces & no parentheses -- must be only one token
			// System.out.println("Only one token!");
			// System.out.println("********** At the end of getLastToken for
			// tokenString = *" + tokenString +
			// "* and About to return tokenString.trim(), which = *" +
			// tokenString.trim() + "*");
			if (tokenString.trim().startsWith("-")) {
				if (tokenString.trim().length() > 1) { // just the minus sign -- means
														// it's an operator
					return tokenString.trim().substring(0); // return just neg. sign
				} else { // more stuff after the minus sign -- it's a negative sign
					return tokenString.trim();
				}
			} else {
				return tokenString.trim();
			}
		} else if (tokenString.startsWith("(")) {
			return "(";
		} else if (tokenString.startsWith(")")) {
			return ")";
		} else { // multiple tokens
			if (tokenString.trim().startsWith("(")) {
				// System.out.println("********** At the end of getLastToken for
				// tokenString = *" + tokenString +
				// "* and About to return lastBlock.substring(1), which = *" +
				// lastBlock.substring(1) + "*");
				return "(";
			} else if (tokenString.trim().startsWith(")")) {
				return ")";
			}
			// System.out.println("Multiple tokens!");
			// System.out.println("tokenString here = *" + tokenString + "*");
			String firstBlock = new String();
			if (tokenString.trim().indexOf(' ') >= 0) { // contains a space
				firstBlock = tokenString.trim().substring(0, tokenString.trim().indexOf(' '));
			} else if (tokenString.indexOf(')') >= 0) { // contains a closing paren
														// but no spaces
				firstBlock = tokenString.trim().substring(0, tokenString.trim().indexOf(')'));
			}

			if (firstBlock.endsWith(")")) {
				return getNextToken(firstBlock.substring(0, (firstBlock.length() - 1)));
			} else if (firstBlock.startsWith("-")) {
				if (firstBlock.length() == 1) { // space after the minus sign -- means
												// it's an operator
					return tokenString.trim().substring(0, 1); // return just neg. sign
				} else { // no space after the minus sign -- means it's a negative sign
					return firstBlock;
				}
			} else { // no parentheses or minus signs
				// System.out.println("************No parentheses, at the end of
				// getLastToken for tokenString = *" + tokenString +
				// " and about to return lastBlock, which = *" + lastBlock + "*");
				return firstBlock;
			}
		}
	}

	private void initializeRuleLists() { // gets the rules in prioritized order
		// initialize lists:
		nonPrioritizedRules = new Vector<Rule>();
		prioritizedRules = new Vector<Rule>();
		Vector<ActionType> allActions = actTypes.getAllActionTypes();
		// go through all action types and get their rules:
		for (int i = 0; i < allActions.size(); i++) {
			ActionType tempAct = allActions.elementAt(i);
			Vector<Rule> trigRules = tempAct.getAllTriggerRules();
			Vector<Rule> contRules = tempAct.getAllContinuousRules();
			Vector<Rule> destRules = tempAct.getAllDestroyerRules();
			Vector<Rule> rules = new Vector<Rule>();
			// add all rules in order to vector:
			for (int j = 0; j < trigRules.size(); j++) {
				rules.add(trigRules.elementAt(j));
			}
			for (int j = 0; j < contRules.size(); j++) {
				rules.add(contRules.elementAt(j));
			}
			for (int j = 0; j < destRules.size(); j++) {
				rules.add(destRules.elementAt(j));
			}
			// go through each rule and add it to the list:
			for (int j = 0; j < rules.size(); j++) {
				Rule tempRule = rules.elementAt(j);
				int priority = tempRule.getPriority();
				if (priority == -1) { // rule is not prioritized yet
					nonPrioritizedRules.addElement(tempRule);
				} else { // priority >= 0
					if (prioritizedRules.size() == 0) { // no elements have been added yet
														// to the prioritized rule list
						prioritizedRules.add(tempRule);
					} else {
						// find the correct position to insert the rule at:
						for (int k = 0; k < prioritizedRules.size(); k++) {
							Rule tempR = prioritizedRules.elementAt(k);
							if (priority <= tempR.getPriority()) {
								prioritizedRules.insertElementAt(tempRule, k); // insert the
																				// rule info
								break;
							} else if (k == (prioritizedRules.size() - 1)) { // on the last
																				// element
								prioritizedRules.add(tempRule); // add the rule info to the end
																// of the list
								break;
							}
						}
					}
				}
			}
		}
	}
}