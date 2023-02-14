/*
 * This class is responsible for generating all of the code for the logic's
 * MenuInputManager component
 */

package simse.codegenerator.logicgenerator;

import simse.codegenerator.CodeGenerator;
import simse.codegenerator.CodeGeneratorConstants;
import simse.codegenerator.CodeGeneratorUtils;

import simse.modelbuilder.ModelOptions;
import simse.modelbuilder.actionbuilder.ActionType;
import simse.modelbuilder.actionbuilder.ActionTypeDestroyer;
import simse.modelbuilder.actionbuilder.ActionTypeParticipant;
import simse.modelbuilder.actionbuilder.ActionTypeParticipantAttributeConstraint;
import simse.modelbuilder.actionbuilder.ActionTypeParticipantConstraint;
import simse.modelbuilder.actionbuilder.ActionTypeParticipantDestroyer;
import simse.modelbuilder.actionbuilder.ActionTypeParticipantTrigger;
import simse.modelbuilder.actionbuilder.ActionTypeTrigger;
import simse.modelbuilder.actionbuilder.AttributeGuard;
import simse.modelbuilder.actionbuilder.DefinedActionTypes;
import simse.modelbuilder.actionbuilder.UserActionTypeDestroyer;
import simse.modelbuilder.actionbuilder.UserActionTypeTrigger;
import simse.modelbuilder.objectbuilder.AttributeTypes;
import simse.modelbuilder.objectbuilder.DefinedObjectTypes;
import simse.modelbuilder.objectbuilder.SimSEObjectType;
import simse.modelbuilder.objectbuilder.SimSEObjectTypeTypes;
import simse.modelbuilder.rulebuilder.Rule;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
import com.squareup.javapoet.WildcardTypeName;

public class MenuInputManagerGenerator implements CodeGeneratorConstants {
	private File directory; // directory to generate into
	private File mimFile; // file to generate
	private ModelOptions options;
	private DefinedActionTypes actTypes; // holds all of the defined action types from an ssa file
	
	// for keeping track of which vectors are being used in generated code so that
	// you don't generate the same ones more than once -- e.g., Vector programmers =
	// state.getEmployeeStateRepository(). getProgrammerStateRepository().getAll()
	// will be generated more than once if you don't keep track of this.
	private Vector<String> vectors;
	private DefinedObjectTypes objTypes;
	
	private Vector<ActionType> actions;
	private Vector<UserActionTypeDestroyer> userDests;
	private Vector<UserActionTypeTrigger> userTrigs;
	
	private ClassName alert = ClassName.get("javafx.scene.control", "Alert");
	private ClassName alertType = ClassName.get("javafx.scene.control.Alert", "AlertType");
	private ClassName buttonBar = ClassName.get("javafx.scene.control", "ButtonBar");
	private ClassName buttonData = ClassName.get("javafx.scene.control.ButtonBar", "ButtonData");
	private ClassName buttonType = ClassName.get("javafx.scene.control", "ButtonType");
	private ClassName chooseActionToDestroyDialog = ClassName.get("simse.logic.dialogs", "ChooseActionToDestroyDialog");
	private ClassName customer = ClassName.get("simse.adts.objects", "Customer");
	private ClassName employee = ClassName.get("simse.adts.objects", "Employee");
	private ClassName ruleExecuter = ClassName.get("simse.logic", "RuleExecuter");
	private ClassName simseGui = ClassName.get("simse.gui", "SimSEGUI");
	private ClassName ssObject = ClassName.get("simse.adts.objects", "SSObject");
	private ClassName vector = ClassName.get("java.util", "Vector");
	private TypeName allEmps = ParameterizedTypeName.get(vector, employee);
	private TypeName vectorOfObjs = ParameterizedTypeName.get(vector, ssObject);

	public MenuInputManagerGenerator(ModelOptions options, DefinedActionTypes actTypes, DefinedObjectTypes objTypes,
			File directory) {
		this.options = options;
		this.directory = directory;
		this.actTypes = actTypes;
		this.objTypes = objTypes;
		vectors = new Vector<String>();
		
		actions = actTypes.getAllActionTypes();
		userDests = new Vector<UserActionTypeDestroyer>();
		userTrigs = new Vector<UserActionTypeTrigger>();
		setUpLists();
	}

	public void generate() {
		ClassName stage = ClassName.get("javafx.stage", "Stage");
		ClassName artifact = ClassName.get("simse.adts.objects", "Artifact");
		ClassName project = ClassName.get("simse.adts.objects", "Project");
		ClassName tool = ClassName.get("simse.adts.objects", "Tool");
		ClassName melloPanel = ClassName.get("simse.gui", "MelloPanel");
		ClassName state = ClassName.get("simse.state", "State");
		ClassName trigCheck = ClassName.get("simse.logic", "TriggerChecker");
	    ClassName destCheck = ClassName.get("simse.logic", "DestroyerChecker");
	    TypeName menu = ParameterizedTypeName.get(vector, ClassName.get(String.class));
		
		MethodSpec menuConstructor = MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
				.addParameter(state, "s")
				.addParameter(trigCheck, "t")
				.addParameter(destCheck, "d")
				.addParameter(ruleExecuter, "r")
				.addStatement("state = s")
				.addStatement("trigChecker = t")
				.addStatement("destChecker = d")
				.addStatement("ruleExec = r")
				.addStatement("mello = $T.getInstance()", melloPanel)
				.build();

		MethodSpec itemSelected = MethodSpec.methodBuilder("menuItemSelected")
				.addModifiers(Modifier.PUBLIC)
				.returns(void.class)
				.addParameter(employee, "selectedEmp")
				.addParameter(String.class, "itemText")
				.addParameter(stage, "parent")
				.addStatement("$T hasStr = false", boolean.class)
				.addStatement("$T menu = selectedEmp.getMenu()", menu)
				.beginControlFlow("for (int i = 0; i < menu.size(); i++)")
				.addStatement("$T menuItem = menu.elementAt(i)", String.class)
				.beginControlFlow("if (menuItem.equals(itemText))")
				.addStatement("hasStr = true")
				.addStatement("break")
				.endControlFlow()
				.endControlFlow()
				.beginControlFlow("if (hasStr)")
				.addCode(menuOptions().build())
				.addCode(triggersAndDestroyers().build())
				.endControlFlow()
				.addComment("update all employees' menus:")
				.addStatement("$T allEmps = state.getEmployeeStateRepository().getAll()", allEmps)
				.beginControlFlow("for (int i = 0; i < allEmps.size(); i++) {")
				.addStatement("allEmps.elementAt(i).clearMenu()")
				.endControlFlow()
				.addComment("update trigger checker:")
				.addStatement("trigChecker.update(true, parent)")
				.addComment("update destroyer checker:")
				.addStatement("destChecker.update(true, parent)")
				.addComment("update gui:")
				.addStatement("(($T) parent).update()", simseGui)
				.build();

		TypeSpec menuInput = TypeSpec.classBuilder("MenuInputManager")
				.addModifiers(Modifier.PUBLIC)
				.addField(state, "state", Modifier.PRIVATE)
				.addField(trigCheck, "trigChecker", Modifier.PRIVATE)
				.addField(destCheck, "destChecker", Modifier.PRIVATE)
				.addField(ruleExecuter, "ruleExec", Modifier.PRIVATE)
				.addField(melloPanel, "mello", Modifier.PRIVATE)
				.addMethod(menuConstructor)
				.addMethod(itemSelected)
				.build();

		JavaFile javaFile = JavaFile.builder("simse.logic", menuInput)
				.addFileComment("File generated by: simse.codegenerator.logicgenerator.MenuInputManagerGenerator")
				.build();
		
		try {
			mimFile = new File(directory, ("simse\\logic\\MenuInputManager.java"));
			if (mimFile.exists()) {
				mimFile.delete(); // delete old version of file
			}
			FileWriter writer = new FileWriter(mimFile);
			System.out.println(javaFile.toString());
			javaFile.writeTo(writer);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, ("Error writing file " + mimFile.getPath() + ": " + e.toString()),
					"File IO Error", JOptionPane.WARNING_MESSAGE);
		}
	}
	
	private void setUpLists() {
		// make a Vector of all the user triggers:
		for (int i = 0; i < actions.size(); i++) {
			ActionType act = actions.elementAt(i);
			Vector<ActionTypeTrigger> allTrigs = act.getAllTriggers();
			for (int j = 0; j < allTrigs.size(); j++) {
				ActionTypeTrigger tempTrig = allTrigs.elementAt(j);
				if (tempTrig instanceof UserActionTypeTrigger) {
					userTrigs.add((UserActionTypeTrigger) tempTrig);
				}
			}
		}
		
		// make a Vector of all the user destroyers:
		for (int j = 0; j < actions.size(); j++) {
			ActionType act = actions.elementAt(j);
			Vector<ActionTypeDestroyer> allDests = act.getAllDestroyers();
			for (int k = 0; k < allDests.size(); k++) {
				ActionTypeDestroyer tempDest = allDests.elementAt(k);
				if (tempDest instanceof UserActionTypeDestroyer) {
					userDests.add((UserActionTypeDestroyer) tempDest);
				}
			}
		}
	}	
	
	private CodeBlock.Builder menuOptions() {
		ClassName displayedEmp = ClassName.get("simse.adts.objects", "DisplayedEmployee");
		
		CodeBlock.Builder conditions = CodeBlock.builder();
		if (CodeGenerator.allowHireFire) {
			conditions.beginControlFlow("if (itemText.startsWith($S))", "Hire Employee");
			conditions.addStatement("(($T)parent).getTabPanel().setGUIChanged()", simseGui);
			conditions.addStatement("selectedEmp.setHired(true)");
			conditions.addStatement("$T sopUsers = (($T)parent).getWorld().getSopUsers()", ArrayList.class, simseGui);
			conditions.beginControlFlow("for (int i = 0; i < sopUsers.size(); i++)");
			conditions.addStatement("$T de = ($T)sopUsers.get(i)", displayedEmp, displayedEmp);
			conditions.beginControlFlow("if (de.getEmployee() == selectedEmployee)");
			conditions.addStatement("de.setActivated(true)");
			conditions.addStatement("de.setDisplayed(true)");
			conditions.endControlFlow();
			conditions.endControlFlow();

			conditions.nextControlFlow("else if (itemText.startsWith($S))", "Fire Employee");
			conditions.addStatement("(($T)parent).getTabPanel().setGUIChanged()", simseGui);
			conditions.addStatement("$T alert = new $T($T.$T.CONFIRMATION)", alert, alert, alert, alertType);
			conditions.addStatement("alert.setTitle($S)", "Confirm Firing");
			conditions.addStatement("alert.setContentText($S)", "Are you sure you wish to fire this Employee?");
			conditions.addStatement("$T okButton = new $T($S, $T.$T.YES)", buttonType, buttonType, "Yes", buttonBar, buttonData);
			conditions.addStatement("$T noButton = new $T($S, $T.$T.NO)", buttonType, buttonType, "No", buttonBar, buttonData);
			conditions.addStatement("alert.getButtonTypes().setAll(okButton, noButton)");
			conditions.beginControlFlow("alert.showAndWait().ifPresent(type ->");
			conditions.add(fireLambda().build());
			conditions.endControlFlow(")");
			conditions.endControlFlow();
		}
		
		if (options.getEveryoneStopOption()) {
			conditions.beginControlFlow("if (itemText.equals($S))", "Everyone stop what you're doing");
			conditions.addStatement("$T alert = new $T($T.$T.CONFIRMATION)", alert, alert, alert, alertType);
			conditions.addStatement("alert.setTitle($S)", "Confirm Activities Ending");
			conditions.addStatement("alert.setContentText($S)", "Are you sure you want everyone to stop what they're doing?");
			conditions.addStatement("$T okButton = new $T($S, $T.$T.YES)", buttonType, buttonType, "Yes", buttonBar, buttonData);
			conditions.addStatement("$T noButton = new $T($S, $T.$T.NO)", buttonType, buttonType, "No", buttonBar, buttonData);
			conditions.addStatement("alert.getButtonTypes().setAll(okButton, noButton)");
			conditions.beginControlFlow("alert.showAndWait().ifPresent(type ->", stopEverythingLambda());
			conditions.add(stopEverythingLambda().build());
			conditions.endControlFlow(")");
			conditions.endControlFlow();
		}
		return conditions;
	}
	
	private CodeBlock.Builder fireLambda() {
		CodeBlock.Builder lambda = CodeBlock.builder();
		lambda.beginControlFlow("if (type == okButton)");
		lambda.addStatement("selectedEmp.setHired(false)");

		Vector<SimSEObjectType> vEmp1 = objTypes.getAllObjectTypesOfType(SimSEObjectTypeTypes.EMPLOYEE);
		for (int i = 0; i < vEmp1.size(); i++) {
			SimSEObjectType sso = vEmp1.elementAt(i);
			String name = CodeGeneratorUtils.getUpperCaseLeading(sso.getName());
			ClassName objName = ClassName.get("simse.adts.objects", name);
			
			if (i == 0) {
				lambda.beginControlFlow("if (selectedEmp instanceof $T)", objName);
			} else {
				lambda.nextControlFlow("else if (selectedEmp instanceof $T)", objName);
			}
			lambda.addStatement("state.getEmployeeStateRepository().get" + name + "StateRepository().remove("
					+ "($T)selectedEmp)", objName);
		}
		lambda.endControlFlow();
		lambda.endControlFlow();
		
		return lambda;
	}
	
	private CodeBlock.Builder stopEverythingLambda() {
		CodeBlock.Builder lambda = CodeBlock.builder();
		lambda.beginControlFlow("if (type == okButton)");
		lambda.addStatement("mello.stopEverything()");
		lambda.addStatement("$T allEmps = state.getEmployeeStateRepository().getAll()", allEmps);
		lambda.beginControlFlow("for (int z = 0; z < allEmps.size(); z++)");
		lambda.addStatement("$T emp = allEmps.elementAt(z)", employee);
		
		// go through each destroyer and generate code for it:
		for (int j = 0; j < userDests.size(); j++) {
			UserActionTypeDestroyer outerDest = userDests.elementAt(j);
			ActionType act = outerDest.getActionType();
			String actType = CodeGeneratorUtils.getUpperCaseLeading(act.getName());
			String actTypeName = actType + "Action";
	    	ClassName actName = ClassName.get("simse.adts.actions", actTypeName);
	    	TypeName vectorOfActTypes = ParameterizedTypeName.get(vector, actName);
			
			lambda.add("// " + outerDest.getMenuText() + ":\n");
			lambda.addStatement("$T allActions" + j + " = state.getActionStateRepository().get"
					+ actTypeName + "StateRepository().getAllActions()", vectorOfActTypes);
			lambda.addStatement("int a" + j + " = 0");
			lambda.beginControlFlow("for (int i = 0; i < allActions" + j + ".size(); i++)");
			lambda.addStatement("$T b" + j + " = allActions" + j + ".elementAt(i)", actName);
			lambda.beginControlFlow("if (b" + j + ".getAllParticipants().contains(emp))");
			lambda.addStatement("a" + j + "++");
			lambda.endControlFlow();
			lambda.endControlFlow();
			lambda.beginControlFlow("if (a" + j + " == 1)");
			lambda.beginControlFlow("for (int i = 0; i < allActions" + j + ".size(); i++) ");
			lambda.addStatement("$T b" + j + " = allActions" + j + ".elementAt(i)", actName);
			
			// go through all participants:
			Vector<ActionTypeParticipant> parts = act.getAllParticipants();
			for (int k = 0; k < parts.size(); k++) {
				ActionTypeParticipant tempPart = parts.elementAt(k);
				if (tempPart.getSimSEObjectTypeType() == SimSEObjectTypeTypes.EMPLOYEE) {
					lambda.beginControlFlow("if(b" + j + ".getAll" + tempPart.getName() + "s().contains(emp))");

					Vector<Rule> destRules = act.getAllDestroyerRules();
					for (int i = 0; i < destRules.size(); i++) {
						Rule dRule = destRules.elementAt(i);
						lambda.addStatement("ruleExec.update(parent, $T.UPDATE_ONE, $S, b" + j + ")"
								, ruleExecuter, dRule.getName());
					}

					lambda.addStatement("b" + j + ".remove" + tempPart.getName() + "(emp)");
					if ((outerDest.getDestroyerText() != null) && (outerDest.getDestroyerText().length() > 0)) {
						lambda.addStatement("emp.setOverheadText($S)", outerDest.getDestroyerText());
					}
					
					int bound = 0;
					if (tempPart.getQuantity().isMinValBoundless()) { // no minimum
						bound = 0;
					} else { // has a minimum
						bound = tempPart.getQuantity().getMinVal().intValue();
					}
					lambda.beginControlFlow("if(b$L.getAll" + tempPart.getName() + "s().size() < $L)", j, bound);
					lambda.addStatement("$T c$L = b$L.getAllParticipants()", vectorOfObjs, j, j);
					lambda.beginControlFlow("for (int j = 0; j < c$L.size(); j++)", j);
					lambda.addStatement("$T d$L = c$L.elementAt(j)", ssObject, j, j);
					lambda.beginControlFlow("if (d$L instanceof $T) {", j, employee);
					
					if ((outerDest.getDestroyerText() != null) && (outerDest.getDestroyerText().length() > 0)) {
						lambda.addStatement("(($T) d$L).setOverheadText($S)", employee, j, outerDest.getDestroyerText());
					}
					lambda.nextControlFlow("else if (d$L instanceof $T)", j, customer);
					
					if ((outerDest.getDestroyerText() != null) && (outerDest.getDestroyerText().length() > 0)) {
						lambda.addStatement("(($T) d$L).setOverheadText($S)", customer, j, outerDest.getDestroyerText());
					}
					lambda.endControlFlow();
					lambda.endControlFlow();
					// remove action from repository:
					lambda.addStatement("state.getActionStateRepository().get$LStateRepository().remove(b$L)", actName, j);

					// game-ending:
					if (outerDest.isGameEndingDestroyer()) {
						lambda.add("// stop game and give score:\n");
						lambda.addStatement("$T t111$L = ($T)b$L", actName, j, actName, j);
						
						// find the scoring attribute:
						ActionTypeParticipantDestroyer scoringPartDest = null;
						ActionTypeParticipantConstraint scoringPartConst = null;
						ActionTypeParticipantAttributeConstraint scoringAttConst = null;
						Vector<ActionTypeParticipantDestroyer> partDests = outerDest
								.getAllParticipantDestroyers();
						for (int m = 0; m < partDests.size(); m++) {
							ActionTypeParticipantDestroyer partDest = partDests.elementAt(m);
							Vector<ActionTypeParticipantConstraint> partConsts = partDest.getAllConstraints();
							for (int n = 0; n < partConsts.size(); n++) {
								ActionTypeParticipantConstraint partConst = partConsts.elementAt(n);
								ActionTypeParticipantAttributeConstraint[] attConsts = partConst
										.getAllAttributeConstraints();
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
						
						String scoringPartVarName = scoringPartDest.getParticipant().getName() + "s";
						String scoringPartConstObj = CodeGeneratorUtils
								.getUpperCaseLeading(scoringPartConst.getSimSEObjectType().getName());
						ClassName scoringPartConstObjName = ClassName.get("simse.adts.objects", scoringPartConstObj);
						if ((scoringAttConst != null) && (scoringPartConst != null) && (scoringPartDest != null)) {
							lambda.beginControlFlow("if (t111$L.getAll" + scoringPartVarName + "().size() > 0)", j);
							lambda.addStatement("$T t$L = ($T)(t111$L.getAll" + scoringPartVarName + "().elementAt(0))"
									, scoringPartConstObjName, j, scoringPartConstObjName, j);
							lambda.beginControlFlow("if (t$L != null)", j);
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
							lambda.addStatement("$T v$L = t$L.get" + scoringAttConst.getAttribute().getName() + "()", scoreType, j, j);
							lambda.addStatement("state.getClock().stop()");
							lambda.addStatement("state.setScore(v$L)", j);
							lambda.addStatement("(($T)gui).update()", simseGui);
							lambda.addStatement("$T d = new $T($T.INFORMATION)", alert, alert, alertType);
							lambda.addStatement("d.setContentText(($S + v$L))", "Your score is ", j);
							lambda.addStatement("d.setTitle($S)", "Game over!");
							lambda.addStatement("d.setHeaderText(null)");
							lambda.addStatement("d.showAndWait()");
							lambda.endControlFlow();
							lambda.endControlFlow(); // game ending if condition
						}
					}
					lambda.endControlFlow();
					lambda.endControlFlow();
				}
			}
			lambda.endControlFlow();
			lambda.nextControlFlow("else if(a" + j + " > 1)");
			lambda.addStatement("$T b$L = new $T();", vectorOfActTypes, j, vectorOfActTypes);
			lambda.beginControlFlow("for (int i = 0; i < allActions" + j + ".size(); i++)");
			lambda.addStatement("$T c$L = ($T)allActions$L.elementAt(i)", actName, j, actName, j);
			
			// go through all participants:
			for (int k = 0; k < parts.size(); k++) {
				ActionTypeParticipant tempPart = parts.elementAt(k);
				if (tempPart.getSimSEObjectTypeType() == SimSEObjectTypeTypes.EMPLOYEE) {
					lambda.beginControlFlow("if((c$L.getAll" + tempPart.getName() + "s().contains(emp)) && "
							+ "(!(b$L.contains(c$L))))", j, j, j);
					lambda.addStatement("b$L.add(c$L)", j, j);
					lambda.endControlFlow();
				}
			}
			lambda.endControlFlow();
			lambda.addStatement("new $T(parent, b$L, state, emp, ruleExec, s)", chooseActionToDestroyDialog, j);
			lambda.endControlFlow();
		}
		lambda.endControlFlow();
		lambda.endControlFlow();	
		return lambda;
	}
	
	private CodeBlock.Builder triggersAndDestroyers() {
		ClassName participantSelectionDialogsDriver = ClassName.get("simse.logic.dialogs", "ParticipantSelectionDialogsDriver");
		ClassName chooseActionToJoinDialog = ClassName.get("simse.logic.dialogs", "ChooseActionToJoinDialog");
		
		CodeBlock.Builder effectCode = CodeBlock.builder();
		
		// go through each trigger and generate code for it:
		for (int i = 0; i < userTrigs.size(); i++) {
			CodeBlock.Builder effectCode1 = CodeBlock.builder();
			CodeBlock.Builder effectCode2 = CodeBlock.builder();
			CodeBlock.Builder effectCode3 = CodeBlock.builder();
			
			// clear vector
			vectors.removeAllElements(); 
			UserActionTypeTrigger outerTrig = (UserActionTypeTrigger) userTrigs.elementAt(i);
			ActionType act = outerTrig.getActionType();
			String actType = CodeGeneratorUtils.getUpperCaseLeading(act.getName());
			String actTypeName = actType + "Action";
	    	ClassName actName = ClassName.get("simse.adts.actions", actTypeName);
	    	TypeName vectorOfActTypes = ParameterizedTypeName.get(vector, actName);
	    	TypeName vectorOfTypeNames = ParameterizedTypeName.get(vector, ClassName.get(String.class));
	    	TypeName objWildcard = WildcardTypeName.subtypeOf(ssObject);
	  	  	TypeName objWildType = ParameterizedTypeName.get(vector, objWildcard);
	    	TypeName vectorOfWildObjTypes = ParameterizedTypeName.get(vector, objWildType);
	    	
			if (i == 0) { 
				// on first element
				effectCode1.beginControlFlow("if (itemText.equals($S))", outerTrig.getMenuText());
			} else {
				effectCode1.nextControlFlow("else if (itemText.equals($S))", outerTrig.getMenuText());
			}
			
			// Where the game-ending code should be placed
			// Due to JavaPoet semantics, it is written out of order and reordered at the end

			Vector<ActionTypeParticipantTrigger> triggers = outerTrig.getAllParticipantTriggers();
			for (int j = 0; j < triggers.size(); j++) {
				ActionTypeParticipantTrigger trig = triggers.elementAt(j);
				String metaTypeName = SimSEObjectTypeTypes.getText(trig.getParticipant().getSimSEObjectTypeType());
				int partMetaType = trig.getParticipant().getSimSEObjectTypeType();
				String partName = trig.getParticipant().getName();
				String partTypeVarName = partName.toLowerCase() + "s" + j;
				
				ClassName metaType = ClassName.get("simse.adts.objects", metaTypeName);
				TypeName vectorOfMetaType = ParameterizedTypeName.get(vector, metaType);
				
				effectCode3.addStatement("$T " + partTypeVarName + " = new $T()", vectorOfMetaType, vectorOfMetaType);
				Vector<ActionTypeParticipantConstraint> constraints = trig.getAllConstraints();
				for (int k = 0; k < constraints.size(); k++) {
					ActionTypeParticipantConstraint constraint = constraints.elementAt(k);
					String objTypeName = constraint.getSimSEObjectType().getName();
					String objTypeVarName = objTypeName.toLowerCase() + "s";
					String uCaseObjTypeName = CodeGeneratorUtils.getUpperCaseLeading(objTypeName);
					ClassName objType = ClassName.get("simse.adts.objects", uCaseObjTypeName);
					TypeName vectorOfObjTypes = ParameterizedTypeName.get(vector, objType);
					int constraintMetaType = constraint.getSimSEObjectType().getType();
					String constraintMetaTypeName = CodeGeneratorUtils.getUpperCaseLeading(
							SimSEObjectTypeTypes.getText(constraintMetaType));
					
					if (vectorContainsString(vectors, objTypeVarName) == false) { 
						// this vector has not been generated already
						effectCode3.addStatement("$T $L = state.get$LStateRepository().get$LStateRepository().getAll()"
								, vectorOfObjTypes, objTypeVarName, constraintMetaTypeName, uCaseObjTypeName);  
						// add it to the list
						vectors.add(objTypeVarName);
					}
					
					effectCode3.beginControlFlow("for (int i = 0; i < $L.size(); i++)", objTypeVarName);
					effectCode3.addStatement("$T a = $L.elementAt(i)", objType, objTypeVarName);

					if (CodeGenerator.allowHireFire && constraintMetaType == SimSEObjectTypeTypes.EMPLOYEE) {
						// if the action involves Employees, only add those that are hired
						effectCode3.beginControlFlow("if (!a.getHired())");
						effectCode3.addStatement("continue");
					}

					effectCode3.addStatement("boolean alreadyInAction = false");
					
					if ((partMetaType == SimSEObjectTypeTypes.EMPLOYEE) || (partMetaType == SimSEObjectTypeTypes.ARTIFACT)) { 
						// employees only be in one of these actions in this role at a time
						effectCode3.addStatement("$T allActions = state.getActionStateRepository()"
								+ ".get$LStateRepository().getAllActions(a)", vectorOfActTypes, actTypeName);
						effectCode3.beginControlFlow("for (int j = 0; j < allActions.size(); j++)");
						effectCode3.addStatement("$T b = allActions.elementAt(j)", actTypeName);
						effectCode3.beginControlFlow("if(b.getAll" + partName + "s().contains(a))");
						effectCode3.addStatement("alreadyInAction = true");
						effectCode3.addStatement("break");
						effectCode3.endControlFlow();
						effectCode3.endControlFlow();
					}
					
					String ifCondition = "if((alreadyInAction == false) ";

					ActionTypeParticipantAttributeConstraint[] attConstraints = constraint.getAllAttributeConstraints();
					for (int m = 0; m < attConstraints.length; m++) {
						ActionTypeParticipantAttributeConstraint attConst = attConstraints[m];
						if (attConst.isConstrained()) {
							ifCondition += " && (a.get"
									+ CodeGeneratorUtils.getUpperCaseLeading(attConst.getAttribute().getName())
									+ "() ";
							if (attConst.getGuard().equals(AttributeGuard.EQUALS)) {
								ifCondition += "== ";
							} else {
								ifCondition += attConst.getGuard() + " ";
							}
							if (attConst.getAttribute().getType() == AttributeTypes.STRING) {
								ifCondition += "\"" + attConst.getValue().toString() + "\")";
							} else {
								ifCondition += attConst.getValue().toString();
							}
							ifCondition += ")";
						}
					}
					ifCondition += ")";
					effectCode3.beginControlFlow(ifCondition);
					effectCode3.addStatement(partTypeVarName + ".add(a)");
					effectCode3.endControlFlow();
					effectCode3.endControlFlow();
				}
			}
			
			String condition = "if (";
			for (int j = 0; j < triggers.size(); j++) {
				ActionTypeParticipantTrigger trig = triggers.elementAt(j);
				if (j > 0) { 
					// not on first element
					condition += " && ";
				}
				ActionTypeParticipant part = trig.getParticipant();
				condition += "(" + part.getName().toLowerCase() + "s" + j + ".size() ";
				if (part.getQuantity().isMinValBoundless() == false) {
					condition += ">= " + part.getQuantity().getMinVal() + ")";
				} else { // min val boundless
					condition += ">= 0)";
				}
			}
			condition += ")";
			if (!condition.equals("if ()")) {
				effectCode3.beginControlFlow(condition);
			}
			effectCode3.addStatement("$T c = new $T()", vectorOfTypeNames, vectorOfTypeNames);

			// NOTE: this following stuff was commented out because it wasn't
			// working right:
			// boolean moreThan1RoleForSameEmployeeType = false;
			for (int j = 0; j < triggers.size(); j++) {
				ActionTypeParticipantTrigger trig = triggers.elementAt(j);
				ActionTypeParticipant part = trig.getParticipant();
				effectCode3.addStatement("c.add($S)", part.getName());
			}
			effectCode3.addStatement("$T d = new $T()", vectorOfWildObjTypes, vectorOfWildObjTypes);

			for (int j = 0; j < triggers.size(); j++) {
				ActionTypeParticipantTrigger trig = triggers.elementAt(j);
				ActionTypeParticipant part = trig.getParticipant();
				effectCode3.addStatement("d.add(" + part.getName().toLowerCase() + "s" + j + ")");
			}
			effectCode3.addStatement("$T f = new $T()", actName, actName);
			effectCode3.addStatement("new $T(parent, c, d, f, state, ruleExec, destChecker, selectedEmp, itemText)", participantSelectionDialogsDriver);
			if (!condition.equals("if ()")) {
				effectCode3.endControlFlow();
			}
						
			
			// Add this code to main feed now that the wrapped code is done
			if (outerTrig.isGameEndingTrigger() || outerTrig.requiresConfirmation()) {
				String title = "";
				String contentText = "";
				// game-ending triggers:
				if (outerTrig.isGameEndingTrigger()) {
					title = "Confirm Game Ending";
					contentText = "Are you sure you want to end the game?";
					
				}
				// triggers requiring confirmation:
				if (outerTrig.requiresConfirmation() && !outerTrig.isGameEndingTrigger()) {
					title = "Confirm Action";
					contentText = "Are you sure?";
				}
				
				// add normal functionality wrapped in confirmation button
				effectCode2.addStatement("$T a2 = new $T($T.$T.CONFIRMATION)", alert, alert, alert, alertType);
				effectCode2.addStatement("a2.setTitle($S)", title);
				effectCode2.addStatement("a2.setContentText($S)", contentText);
				effectCode2.addStatement("a2.setHeaderText(null)");
				effectCode2.addStatement("$T okButton = new $T($S, $T.$T.YES)", buttonType, buttonType, "Yes", buttonBar, buttonData);
				effectCode2.addStatement("$T noButton = new $T($S, $T.$T.NO)", buttonType, buttonType, "No", buttonBar, buttonData);
				effectCode2.addStatement("a2.getButtonTypes().setAll(okButton, noButton)");
				effectCode2.beginControlFlow("a2.showAndWait().ifPresent(type ->");
				effectCode2.beginControlFlow("if (type == okButton)");
				effectCode2.add(effectCode3.build());
				effectCode2.endControlFlow();
				effectCode2.endControlFlow(")");
				
				effectCode1.add(effectCode2.build());
			} else {
				effectCode1.add(effectCode3.build());
			}

			// JOINING existing actions:
			effectCode1.nextControlFlow("else if (itemText.equals($S))", "JOIN " + outerTrig.getMenuText());
			effectCode1.addStatement("$T a = state.getActionStateRepository().get$LStateRepository().getAllActions()"
					, vectorOfActTypes, actTypeName);   
			effectCode1.addStatement("$T b = new $T()", vectorOfActTypes, vectorOfActTypes);
			effectCode1.beginControlFlow("for (int i = 0; i < a.size(); i++) {");
			effectCode1.addStatement("$T c = a.elementAt(i)", actName);
			
			// go through all participants:
			for (int j = 0; j < triggers.size(); j++) {
				ActionTypeParticipantTrigger trig = triggers.elementAt(j);
				ActionTypeParticipant tempPart = trig.getParticipant();
				if (tempPart.getSimSEObjectTypeType() == SimSEObjectTypeTypes.EMPLOYEE) {
					effectCode1.addStatement("if ((c.getAll$L().contains(selectedEmp) == false) "
							+ "&& (b.contains(c) == false))", tempPart.getName());
					effectCode1.addStatement("b.add(c)");
				}
			}

			effectCode1.endControlFlow();
			effectCode1.addStatement("new $T(parent, b, selectedEmp, state, $S, ruleExec)", chooseActionToJoinDialog, outerTrig.getMenuText());
			effectCode.add(effectCode1.build());
		}
		
		// go through each destroyer and generate code for it:
		for (int j = 0; j < userDests.size(); j++) {
			UserActionTypeDestroyer outerDest = userDests.elementAt(j);
			ActionType act = outerDest.getActionType();
			String actType = CodeGeneratorUtils.getUpperCaseLeading(act.getName());
			String actTypeName = actType + "Action";
	    	ClassName actName = ClassName.get("simse.adts.actions", actTypeName);
	    	TypeName vectorOfActTypes = ParameterizedTypeName.get(vector, actName);
			
			effectCode.nextControlFlow("else if (itemText.equals($S))", outerDest.getMenuText());
			effectCode.addStatement("$T allActions = state.getActionStateRepository().get"
					+ actTypeName + "StateRepository().getAllActions()", vectorOfActTypes);
			effectCode.addStatement("int a = 0");
			effectCode.beginControlFlow("for (int i = 0; i < allActions.size(); i++)");
			effectCode.addStatement("$T b = allActions.elementAt(i)", actName);
			effectCode.beginControlFlow("if (b.getAllParticipants().contains(selectedEmp))");
			effectCode.addStatement("a++");
			effectCode.endControlFlow();
			effectCode.endControlFlow();
			effectCode.beginControlFlow("if (a == 1) {");
			effectCode.beginControlFlow("for (int i = 0; i < allActions.size(); i++) {");
			effectCode.addStatement("$T b = allActions.elementAt(i)", actName);
			
			// go through all participants:
			Vector<ActionTypeParticipant> parts = act.getAllParticipants();
			for (int k = 0; k < parts.size(); k++) {
				ActionTypeParticipant tempPart = parts.elementAt(k);
				String metaTypeName = SimSEObjectTypeTypes.getText(tempPart.getSimSEObjectTypeType());
				int objMetaType = tempPart.getSimSEObjectTypeType();
				String objName = tempPart.getName();
				
				ClassName metaType = ClassName.get("simse.adts.objects", metaTypeName);
				
				if (objMetaType == SimSEObjectTypeTypes.EMPLOYEE) { 
					// participant is of employee type
					// TODO: Figure out how to get the key attribute for each employee type, this should be objType not MetaType
					effectCode.addStatement("mello.removeEmployeeFromTask($S, (($T)selectedEmp).getName())", actType, metaType);
					effectCode.beginControlFlow("if (b.getAll" + objName + "s().contains(selectedEmp))");

					// execute all destroyer rules that have executeOnJoins == true:
					Vector<Rule> destRules = act.getAllDestroyerRules();
					for (int i = 0; i < destRules.size(); i++) {
						Rule dRule = destRules.elementAt(i);
						if (dRule.getExecuteOnJoins() == true) {
							effectCode.addStatement("ruleExec.update(parent, $T.UPDATE_ONE, $S, b)", ruleExecuter, dRule.getName());
						}
					}

					effectCode.addStatement("b.remove$L(selectedEmp)", objName);
					if ((outerDest.getDestroyerText() != null) && (outerDest.getDestroyerText().length() > 0)) {
						effectCode.addStatement("selectedEmp.setOverheadText($S)", outerDest.getDestroyerText());
					}
					
					int bound = 0;
					if (tempPart.getQuantity().isMinValBoundless()) { 
						// no minimum
						bound = 0;
					} else { 
						// has a minimum
						bound = tempPart.getQuantity().getMinVal().intValue();
					}
					effectCode.beginControlFlow("if (b.getAll$L().size() < $L)", objName, bound);
					effectCode.addStatement("$T c = b.getAllParticipants()", vectorOfObjs);
					effectCode.beginControlFlow("for (int j = 0; j < c.size(); j++)");
					effectCode.addStatement("$T d = c.elementAt(j)", ssObject);
					effectCode.beginControlFlow("if (d instanceof $T)", employee);
					
					if ((outerDest.getDestroyerText() != null) && (outerDest.getDestroyerText().length() > 0)) {
						effectCode.addStatement("(($T) d).setOverheadText($S)", employee, outerDest.getDestroyerText());
					}
					
					effectCode.addStatement("else if (d instanceof $T)", customer);
					if ((outerDest.getDestroyerText() != null) && (outerDest.getDestroyerText().length() > 0)) {
						effectCode.addStatement("(($T) d).setOverheadText($S)", customer, outerDest.getDestroyerText());
					}
					effectCode.endControlFlow();
					effectCode.endControlFlow();

					// remove action from repository:
					effectCode.addStatement("state.getActionStateRepository().get$LStateRepository().remove(b)", actTypeName);

					// game-ending:
					if (outerDest.isGameEndingDestroyer()) {
						effectCode.add("// stop game and give score:\n");
						effectCode.addStatement("$T t111 = ($T)b", actName, actName);
						
						// find the scoring attribute:
						ActionTypeParticipantDestroyer scoringPartDest = null;
						ActionTypeParticipantConstraint scoringPartConst = null;
						ActionTypeParticipantAttributeConstraint scoringAttConst = null;
						Vector<ActionTypeParticipantDestroyer> partDests = outerDest.getAllParticipantDestroyers();
						for (int m = 0; m < partDests.size(); m++) {
							ActionTypeParticipantDestroyer partDest = partDests.elementAt(m);
							Vector<ActionTypeParticipantConstraint> partConsts = partDest.getAllConstraints();
							for (int n = 0; n < partConsts.size(); n++) {
								ActionTypeParticipantConstraint partConst = partConsts.elementAt(n);
								ActionTypeParticipantAttributeConstraint[] attConsts = partConst
										.getAllAttributeConstraints();
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
						String scoringPartVarName = scoringPartDest.getParticipant().getName() + "s";
						String scoringPartConstObj = CodeGeneratorUtils
								.getUpperCaseLeading(scoringPartConst.getSimSEObjectType().getName());
						ClassName scoringPartConstObjName = ClassName.get("simse.adts.objects", scoringPartConstObj);
						if ((scoringAttConst != null) && (scoringPartConst != null) && (scoringPartDest != null)) {
							effectCode.beginControlFlow("if (t111.getAll" + scoringPartVarName + "().size() > 0)");
							effectCode.addStatement("$T t = ($T)(t111.getAll" + scoringPartVarName + "().elementAt(0))"
									, scoringPartConstObjName, scoringPartConstObjName);
							effectCode.beginControlFlow("if (t != null)");
							
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
							effectCode.addStatement("$T v = t.get" + scoringAttConst.getAttribute().getName() + "()", scoreType);
							effectCode.addStatement("state.getClock().stop()");
							effectCode.addStatement("state.setScore(v)");
							effectCode.addStatement("(($T)gui).update()", simseGui);
							effectCode.addStatement("$T d = new $T($T.INFORMATION)", alert, alert, alertType);
							effectCode.addStatement("d.setContentText(($S + v))", "Your score is ");
							effectCode.addStatement("d.setTitle($S)", "Game over!");
							effectCode.addStatement("d.setHeaderText(null)");
							effectCode.addStatement("d.showAndWait()");
							effectCode.endControlFlow();
							effectCode.endControlFlow(); // game ending if condition
						}
					}
					effectCode.endControlFlow();
					effectCode.endControlFlow();
				}
			}
			
			effectCode.nextControlFlow("else");
			effectCode.addStatement("$T b = new $T()", vectorOfActTypes, vectorOfActTypes);
			effectCode.beginControlFlow("for (int i = 0; i < allActions.size(); i++)");
			effectCode.addStatement("$T c = ($T) allActions.elementAt(i)", actName, actName);
			
			// go through all participants:
			for (int k = 0; k < parts.size(); k++) {
				ActionTypeParticipant tempPart = parts.elementAt(k);
				String objName = tempPart.getName();
				
				if (tempPart.getSimSEObjectTypeType() == SimSEObjectTypeTypes.EMPLOYEE) {
					effectCode.beginControlFlow("if ((c.getAll$L().contains(selectedEmp)) && (!(b.contains(c))))", objName);
					effectCode.addStatement("b.add(c)");
					effectCode.endControlFlow();
				}
			}
			effectCode.endControlFlow();
			effectCode.addStatement("new $T(parent, b, state, selectedEmp,ruleExec, itemText)", chooseActionToDestroyDialog);
			effectCode.endControlFlow();
			effectCode.endControlFlow();
		}
		
		return effectCode;
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
}