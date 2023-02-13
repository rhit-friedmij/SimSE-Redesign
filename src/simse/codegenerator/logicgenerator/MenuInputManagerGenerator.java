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
	
	private Vector<ActionType> actions = actTypes.getAllActionTypes();
	private Vector<UserActionTypeDestroyer> userDests = new Vector<UserActionTypeDestroyer>();
	private Vector<UserActionTypeTrigger> userTrigs = new Vector<UserActionTypeTrigger>();
	
	private ClassName buttonType = ClassName.get("javafx.scene.control", "ButtonType");
	private ClassName customer = ClassName.get("simse.adts.objects", "Customer");
	private ClassName employee = ClassName.get("simse.adts.objects", "Employee");
	private ClassName ruleExecuter = ClassName.get("simse.logic", "RuleExecuter");
	private ClassName simseGui = ClassName.get("simse.gui", "SimSEGUI");
	private ClassName vector = ClassName.get("java.util", "Vector");
	private TypeName allEmps = ParameterizedTypeName.get(vector, employee);

	public MenuInputManagerGenerator(ModelOptions options, DefinedActionTypes actTypes, DefinedObjectTypes objTypes,
			File directory) {
		this.options = options;
		this.directory = directory;
		this.actTypes = actTypes;
		this.objTypes = objTypes;
		vectors = new Vector<String>();
	}

	public void generate() {
		ClassName stage = ClassName.get("javafx.stage", "Stage");
		ClassName artifact = ClassName.get("simse.adts.objects", "Artifact");
		
		ClassName project = ClassName.get("simse.adts.objects", "Project");
		ClassName tool = ClassName.get("simse.adts.objects", "Tool");
		ClassName melloPanel = ClassName.get("simse.gui", "MelloPanel");
		ClassName chooseActionToDestroyDialog = ClassName.get("simse.logic.dialogs", "ChooseActionToDestroyDialog");
		ClassName chooseActionToJoinDialog = ClassName.get("simse.logic.dialogs", "ChooseActionToJoinDialog");
		ClassName participantSelectionDialogsDriver = ClassName.get("simse.logic.dialogs", "ParticipantSelectionDialogsDriver");
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
				.addCode(triggers().build())
				.addCode(destroyers().build())
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
			System.out.println(javaFile.toString());
			javaFile.writeTo(mimFile);
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
		ClassName alert = ClassName.get("javafx.scene.control", "Alert");
		ClassName alertType = ClassName.get("javafx.scene.control.Alert", "AlertType");
		ClassName buttonBar = ClassName.get("javafx.scene.control", "ButtonBar");
		ClassName buttonData = ClassName.get("javafx.scene.control.ButtonBar", "ButtonData");
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
			conditions.addStatement("alert.showAndWait().ifPresent($L)", fireLambda());
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
			conditions.addStatement("alert.showAndWait().ifPresent($L)", stopEverythingLambda());
		}
		return conditions;
	}
	
	private MethodSpec fireLambda() {
		CodeBlock.Builder lambda = CodeBlock.builder();

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
		
		MethodSpec fireLambda = MethodSpec.methodBuilder("")
				.addParameter(buttonType, "type")
				.beginControlFlow("if (type == okButton)")
				.addStatement("selectedEmp.setHired(false)")
				.addCode(lambda.build())
				.endControlFlow()
				.build();
		return fireLambda;
	}
	
	private MethodSpec stopEverythingLambda() {
		ClassName obj = ClassName.get("simse.adts.objects", "SSObject");
		TypeName vectorOfObjs = ParameterizedTypeName.get(vector, obj);
		
		CodeBlock.Builder lambda = CodeBlock.builder();
		
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
					+ actTypeName + "StateRepository().getAllActions();", vectorOfActTypes);
			lambda.addStatement("int a" + j + " = 0");
			lambda.beginControlFlow("for (int i = 0; i < allActions" + j + ".size(); i++)");
			lambda.addStatement("$T b" + j + " = allActions" + j + ".elementAt(i)", actTypeName);
			lambda.beginControlFlow("if (b" + j + ".getAllParticipants().contains(emp))");
			lambda.addStatement("a" + j + "++");
			lambda.endControlFlow();
			lambda.endControlFlow();
			lambda.beginControlFlow("if (a" + j + " == 1)");
			lambda.beginControlFlow("for (int i = 0; i < allActions" + j + ".size(); i++) ");
			lambda.addStatement("$T b" + j + " = allActions" + j + ".elementAt(i)", actTypeName);
			
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
					lambda.addStatement("$T d$L = c$L.elementAt(j)", obj, j, j);
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
						writer.write("// stop game and give score:");
						writer.write(NEWLINE);
						writer.write(CodeGeneratorUtils.getUpperCaseLeading(act.getName()) + "Action t111" + j
								+ " = (" + CodeGeneratorUtils.getUpperCaseLeading(act.getName()) + "Action)b"
								+ j + ";");
						writer.write(NEWLINE);
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
						if ((scoringAttConst != null) && (scoringPartConst != null)
								&& (scoringPartDest != null)) {
							writer.write("if(t111" + j + ".getAll" + scoringPartDest.getParticipant().getName()
									+ "s().size() > 0)");
							writer.write(NEWLINE);
							writer.write(OPEN_BRACK);
							writer.write(NEWLINE);
							writer.write(CodeGeneratorUtils
									.getUpperCaseLeading(scoringPartConst.getSimSEObjectType().getName()) + " t"
									+ j + " = ("
									+ CodeGeneratorUtils.getUpperCaseLeading(
											scoringPartConst.getSimSEObjectType().getName())
									+ ")(t111" + j + ".getAll" + scoringPartDest.getParticipant().getName()
									+ "s().elementAt(0));");
							writer.write(NEWLINE);
							writer.write("if(t" + j + " != null)");
							writer.write(NEWLINE);
							writer.write(OPEN_BRACK);
							writer.write(NEWLINE);
							if (scoringAttConst.getAttribute().getType() == AttributeTypes.INTEGER) {
								writer.write("int");
							} else if (scoringAttConst.getAttribute().getType() == AttributeTypes.DOUBLE) {
								writer.write("double");
							} else if (scoringAttConst.getAttribute().getType() == AttributeTypes.STRING) {
								writer.write("String");
							} else if (scoringAttConst.getAttribute().getType() == AttributeTypes.BOOLEAN) {
								writer.write("boolean");
							}
							writer.write(" v" + j + " = t" + j + ".get"
									+ scoringAttConst.getAttribute().getName() + "();");
							writer.write(NEWLINE);
							writer.write("state.getClock().stop();");
							writer.write(NEWLINE);
							writer.write("state.setScore(v);");
							writer.write(NEWLINE);
							writer.write("((SimSEGUI)parent).update();");
							writer.write(NEWLINE);
							writer.write("JOptionPane.showMessageDialog(null, (\"Your score is \" + v" + j
									+ "), \"Game over!\", JOptionPane.INFORMATION_MESSAGE);");
							writer.write(NEWLINE);
							writer.write(CLOSED_BRACK);
							writer.write(NEWLINE);
							writer.write(CLOSED_BRACK);
							writer.write(NEWLINE);
						}
					}

					writer.write(CLOSED_BRACK);
					writer.write(NEWLINE);
					writer.write(CLOSED_BRACK);
					writer.write(NEWLINE);
				}
			}
			writer.write(CLOSED_BRACK);
			writer.write(NEWLINE);
			writer.write(CLOSED_BRACK);
			writer.write(NEWLINE);
			writer.write("else if(a" + j + " > 1)");
			writer.write(NEWLINE);
			writer.write(OPEN_BRACK);
			writer.write(NEWLINE);
			writer.write("Vector<" + CodeGeneratorUtils.getUpperCaseLeading(act.getName()) + "Action> b" + j
					+ " = new Vector<" + CodeGeneratorUtils.getUpperCaseLeading(act.getName()) + "Action>();");
			writer.write(NEWLINE);
			writer.write("for(int i=0; i<allActions" + j + ".size(); i++)");
			writer.write(NEWLINE);
			writer.write(OPEN_BRACK);
			writer.write(NEWLINE);
			writer.write(CodeGeneratorUtils.getUpperCaseLeading(act.getName()) + "Action c" + j + " = ("
					+ CodeGeneratorUtils.getUpperCaseLeading(act.getName()) + "Action)allActions" + j
					+ ".elementAt(i);");
			writer.write(NEWLINE);
			// go through all participants:
			for (int k = 0; k < parts.size(); k++) {
				ActionTypeParticipant tempPart = parts.elementAt(k);
				if (tempPart.getSimSEObjectTypeType() == SimSEObjectTypeTypes.EMPLOYEE) {
					writer.write("if((c" + j + ".getAll" + tempPart.getName() + "s().contains(emp)) && (!(b" + j
							+ ".contains(c" + j + "))))");
					writer.write(NEWLINE);
					writer.write(OPEN_BRACK);
					writer.write(NEWLINE);
					writer.write("b" + j + ".add(c" + j + ");");
					writer.write(NEWLINE);
					writer.write(CLOSED_BRACK);
					writer.write(NEWLINE);
				}
			}
			writer.write(CLOSED_BRACK);
			writer.write(NEWLINE);
			writer.write("new ChooseActionToDestroyDialog(parent, b" + j + ", state, emp, ruleExec, s);");
			writer.write(NEWLINE);
			writer.write(CLOSED_BRACK);
			writer.write(NEWLINE);
		}
		writer.write(CLOSED_BRACK);
		writer.write(NEWLINE);
		writer.write(CLOSED_BRACK);
		writer.write(NEWLINE);
		writer.write(CLOSED_BRACK);
		writer.write(NEWLINE);
		
		
		MethodSpec stopLambda = MethodSpec.methodBuilder("")
				.addParameter(buttonType, "type")
				.beginControlFlow("if (type == okButton)")
				.addStatement("mello.stopEverything()")
				.addStatement("$T allEmps = state.getEmployeeStateRepository().getAll()", allEmps)
				.beginControlFlow("for (int z = 0; z < allEmps.size(); z++)")
				.addStatement("$T emp = allEmps.elementAt(z)", employee)
				.addCode(lambda.build())
				.build();
		return stopLambda;
	}
	
	private CodeBlock.Builder triggers() {
		CodeBlock.Builder trigCode = CodeBlock.builder();
		
		// go through each trigger and generate code for it:
		for (int i = 0; i < userTrigs.size(); i++) {
			vectors.removeAllElements(); // clear vector
			UserActionTypeTrigger outerTrig = (UserActionTypeTrigger) userTrigs.elementAt(i);
			ActionType act = outerTrig.getActionType();
			if (i > 0) { // not on first element
				writer.write("else ");
			}
			writer.write("if(s.equals(\"" + outerTrig.getMenuText() + "\"))");
			writer.write(NEWLINE);
			writer.write(OPEN_BRACK);
			writer.write(NEWLINE);

			// game-ending triggers:
			if (outerTrig.isGameEndingTrigger()) {
				writer.write(
						"int choice = JOptionPane.showConfirmDialog(null, (\"Are you sure you want to end the game?\"), \"Confirm Game Ending\", JOptionPane.YES_NO_OPTION);");
				writer.write(NEWLINE);
				writer.write("if(choice == JOptionPane.YES_OPTION)");
				writer.write(NEWLINE);
				writer.write(OPEN_BRACK);
				writer.write(NEWLINE);
			}

			// triggers requiring confirmation:
			if (outerTrig.requiresConfirmation() && !outerTrig.isGameEndingTrigger()) {
				writer.write(
						"int choice = JOptionPane.showConfirmDialog(null, (\"Are you sure?\"), \"Confirm Action\", JOptionPane.YES_NO_OPTION);");
				writer.write(NEWLINE);
				writer.write("if(choice == JOptionPane.YES_OPTION)");
				writer.write(NEWLINE);
				writer.write(OPEN_BRACK);
				writer.write(NEWLINE);
			}

			Vector<ActionTypeParticipantTrigger> triggers = outerTrig.getAllParticipantTriggers();
			for (int j = 0; j < triggers.size(); j++) {
				ActionTypeParticipantTrigger trig = triggers.elementAt(j);
				String metaTypeName = SimSEObjectTypeTypes.getText(trig.getParticipant().getSimSEObjectTypeType());
				writer.write("Vector<" + metaTypeName + "> " + trig.getParticipant().getName().toLowerCase() + "s"
						+ j + " = new Vector<" + metaTypeName + ">();");
				writer.write(NEWLINE);
				Vector<ActionTypeParticipantConstraint> constraints = trig.getAllConstraints();
				for (int k = 0; k < constraints.size(); k++) {
					ActionTypeParticipantConstraint constraint = constraints.elementAt(k);
					String objTypeName = constraint.getSimSEObjectType().getName();
					if (vectorContainsString(vectors, (objTypeName.toLowerCase() + "s")) == false) { // this vector
																										// has not
																										// been
																										// generated
																										// already
						writer.write("Vector<" + CodeGeneratorUtils.getUpperCaseLeading(objTypeName) + "> "
								+ objTypeName.toLowerCase() + "s = state.get"
								+ CodeGeneratorUtils.getUpperCaseLeading(
										SimSEObjectTypeTypes.getText(constraint.getSimSEObjectType().getType()))
								+ "StateRepository().get" + CodeGeneratorUtils.getUpperCaseLeading(objTypeName)
								+ "StateRepository().getAll();");
						vectors.add(objTypeName.toLowerCase() + "s"); // add it to the
																		// list
						writer.write(NEWLINE);
					}
					writer.write("for(int i=0; i<" + objTypeName.toLowerCase() + "s.size(); i++)");
					writer.write(NEWLINE);
					writer.write(OPEN_BRACK);
					writer.write(NEWLINE);
					writer.write(CodeGeneratorUtils.getUpperCaseLeading(objTypeName) + " a = "
							+ objTypeName.toLowerCase() + "s.elementAt(i);");
					writer.write(NEWLINE);

					if (CodeGenerator.allowHireFire
							&& constraint.getSimSEObjectType().getType() == SimSEObjectTypeTypes.EMPLOYEE) {
						// if the action involves Employees, only add those that are hired
						writer.write("if (!a.getHired())");
						writer.write(NEWLINE);
						writer.write("continue;");
						writer.write(NEWLINE);
					}

					writer.write("boolean alreadyInAction = false;");
					writer.write(NEWLINE);
					if ((trig.getParticipant().getSimSEObjectTypeType() == SimSEObjectTypeTypes.EMPLOYEE)
							|| (trig.getParticipant().getSimSEObjectTypeType() == SimSEObjectTypeTypes.ARTIFACT)) { 
						// employees only be in one of these actions in this role at a time
						writer.write("Vector<" + CodeGeneratorUtils.getUpperCaseLeading(act.getName())
								+ "Action> allActions = state.getActionStateRepository().get"
								+ CodeGeneratorUtils.getUpperCaseLeading(act.getName())
								+ "ActionStateRepository().getAllActions(a);");
						writer.write(NEWLINE);
						writer.write("for(int j=0; j<allActions.size(); j++)");
						writer.write(NEWLINE);
						writer.write(OPEN_BRACK);
						writer.write(NEWLINE);
						writer.write(CodeGeneratorUtils.getUpperCaseLeading(act.getName())
								+ "Action b = allActions.elementAt(j);");
						writer.write(NEWLINE);
						writer.write("if(b.getAll" + trig.getParticipant().getName() + "s().contains(a))");
						writer.write(NEWLINE);
						writer.write(OPEN_BRACK);
						writer.write(NEWLINE);
						writer.write("alreadyInAction = true;");
						writer.write(NEWLINE);
						writer.write("break;");
						writer.write(NEWLINE);
						writer.write(CLOSED_BRACK);
						writer.write(NEWLINE);
						writer.write(CLOSED_BRACK);
						writer.write(NEWLINE);
					}
					writer.write("if((alreadyInAction == false)");
					ActionTypeParticipantAttributeConstraint[] attConstraints = constraint
							.getAllAttributeConstraints();
					for (int m = 0; m < attConstraints.length; m++) {
						ActionTypeParticipantAttributeConstraint attConst = attConstraints[m];
						if (attConst.isConstrained()) {
							writer.write(" && (a.get"
									+ CodeGeneratorUtils.getUpperCaseLeading(attConst.getAttribute().getName())
									+ "() ");
							if (attConst.getGuard().equals(AttributeGuard.EQUALS)) {
								writer.write("== ");
							} else {
								writer.write(attConst.getGuard() + " ");
							}
							if (attConst.getAttribute().getType() == AttributeTypes.STRING) {
								writer.write("\"" + attConst.getValue().toString() + "\"");
							} else {
								writer.write(attConst.getValue().toString());
							}
							writer.write(")");
						}
					}
					writer.write(")");
					writer.write(NEWLINE);
					writer.write(OPEN_BRACK);
					writer.write(NEWLINE);
					writer.write(trig.getParticipant().getName().toLowerCase() + "s" + j + ".add(a);");
					writer.write(NEWLINE);
					writer.write(CLOSED_BRACK);
					writer.write(NEWLINE);
					writer.write(CLOSED_BRACK);
					writer.write(NEWLINE);
				}
			}
			writer.write("if(");
			for (int j = 0; j < triggers.size(); j++) {
				ActionTypeParticipantTrigger trig = triggers.elementAt(j);
				if (j > 0) { // not on first element
					writer.write(" && ");
				}
				ActionTypeParticipant part = trig.getParticipant();
				writer.write("(" + part.getName().toLowerCase() + "s" + j + ".size() ");
				if (part.getQuantity().isMinValBoundless() == false) {
					writer.write(">= " + part.getQuantity().getMinVal() + ")");
				} else { // min val boundless
					writer.write(">= 0)");
				}
			}
			writer.write(")");
			writer.write(NEWLINE);
			writer.write(OPEN_BRACK);
			writer.write(NEWLINE);
			writer.write("Vector<String> c = new Vector<String>();");
			writer.write(NEWLINE);

			// NOTE: this following stuff was commented out because it wasn't
			// working right:
			// boolean moreThan1RoleForSameEmployeeType = false;
			for (int j = 0; j < triggers.size(); j++) {
				ActionTypeParticipantTrigger trig = triggers.elementAt(j);
				ActionTypeParticipant part = trig.getParticipant();
				writer.write("c.add(\"" + part.getName() + "\");");
				writer.write(NEWLINE);
			}
			writer.write("Vector<Vector<? extends SSObject>> d = new Vector<Vector<? extends SSObject>>();");
			writer.write(NEWLINE);

			for (int j = 0; j < triggers.size(); j++) {
				ActionTypeParticipantTrigger trig = triggers.elementAt(j);
				ActionTypeParticipant part = trig.getParticipant();
				writer.write("d.add(" + part.getName().toLowerCase() + "s" + j + ");");
				writer.write(NEWLINE);
			}
			writer.write(CodeGeneratorUtils.getUpperCaseLeading(act.getName()) + "Action f = new "
					+ CodeGeneratorUtils.getUpperCaseLeading(act.getName()) + "Action();");
			writer.write(NEWLINE);
			writer.write(
					"new ParticipantSelectionDialogsDriver(parent, c, d, f, state, ruleExec, destChecker, e, s);");

			writer.write(NEWLINE);
			writer.write(CLOSED_BRACK);
			writer.write(NEWLINE);

			if (outerTrig.isGameEndingTrigger() || outerTrig.requiresConfirmation()) {
				// add extra closed brack
				writer.write(CLOSED_BRACK);
				writer.write(NEWLINE);
			}

			writer.write(CLOSED_BRACK);
			writer.write(NEWLINE);

			// JOINING existing actions:
			writer.write("else if(s.equals(\"JOIN " + outerTrig.getMenuText() + "\"))");
			writer.write(NEWLINE);
			writer.write(OPEN_BRACK);
			writer.write(NEWLINE);
			writer.write("Vector<" + CodeGeneratorUtils.getUpperCaseLeading(act.getName())
					+ "Action> a = state.getActionStateRepository().get"
					+ CodeGeneratorUtils.getUpperCaseLeading(act.getName())
					+ "ActionStateRepository().getAllActions();");
			writer.write(NEWLINE);
			writer.write(
					"Vector<" + CodeGeneratorUtils.getUpperCaseLeading(act.getName()) + "Action> b = new Vector<"
							+ CodeGeneratorUtils.getUpperCaseLeading(act.getName()) + "Action>();");
			writer.write(NEWLINE);
			writer.write("for(int i=0; i<a.size(); i++)");
			writer.write(NEWLINE);
			writer.write(OPEN_BRACK);
			writer.write(NEWLINE);
			writer.write(CodeGeneratorUtils.getUpperCaseLeading(act.getName()) + "Action c = a.elementAt(i);");
			writer.write(NEWLINE);
			// go through all participants:
			for (int j = 0; j < triggers.size(); j++) {
				ActionTypeParticipantTrigger trig = triggers.elementAt(j);
				ActionTypeParticipant tempPart = trig.getParticipant();
				if (tempPart.getSimSEObjectTypeType() == SimSEObjectTypeTypes.EMPLOYEE) {
					writer.write("if((c.getAll" + tempPart.getName()
							+ "s().contains(e) == false) && (b.contains(c) == false))");
					writer.write(NEWLINE);
					writer.write(OPEN_BRACK);
					writer.write(NEWLINE);
					writer.write("b.add(c);");
					writer.write(NEWLINE);
					writer.write(CLOSED_BRACK);
					writer.write(NEWLINE);
				}
			}
			writer.write(CLOSED_BRACK);
			writer.write(NEWLINE);
			writer.write("new ChooseActionToJoinDialog(parent, b, e, state, \"" + outerTrig.getMenuText()
					+ "\", ruleExec);");
			writer.write(NEWLINE);
			writer.write(CLOSED_BRACK);
			writer.write(NEWLINE);
		}
		return trigCode;
	}
	
	private CodeBlock.Builder destroyers() {
		CodeBlock.Builder destCode = CodeBlock.builder();
		// go through each destroyer and generate code for it:
		for (int j = 0; j < userDests.size(); j++) {
			UserActionTypeDestroyer outerDest = userDests.elementAt(j);
			ActionType act = outerDest.getActionType();
			writer.write("else if(s.equals(\"" + outerDest.getMenuText() + "\"))");
			writer.write(NEWLINE);
			writer.write(OPEN_BRACK);
			writer.write(NEWLINE);
			writer.write("Vector<" + CodeGeneratorUtils.getUpperCaseLeading(act.getName())
					+ "Action> allActions = state.getActionStateRepository().get"
					+ CodeGeneratorUtils.getUpperCaseLeading(act.getName())
					+ "ActionStateRepository().getAllActions();");
			writer.write(NEWLINE);
			writer.write("int a = 0;");
			writer.write(NEWLINE);
			writer.write("for(int i=0; i<allActions.size(); i++)");
			writer.write(NEWLINE);
			writer.write(OPEN_BRACK);
			writer.write(NEWLINE);
			writer.write(
					CodeGeneratorUtils.getUpperCaseLeading(act.getName()) + "Action b = allActions.elementAt(i);");
			writer.write(NEWLINE);
			writer.write("if(b.getAllParticipants().contains(e))");
			writer.write(NEWLINE);
			writer.write(OPEN_BRACK);
			writer.write(NEWLINE);
			writer.write("a++;");
			writer.write(NEWLINE);
			writer.write(CLOSED_BRACK);
			writer.write(NEWLINE);
			writer.write(CLOSED_BRACK);
			writer.write(NEWLINE);
			writer.write("if(a == 1)");
			writer.write(NEWLINE);
			writer.write(OPEN_BRACK);
			writer.write(NEWLINE);
			writer.write("for(int i=0; i<allActions.size(); i++)");
			writer.write(NEWLINE);
			writer.write(OPEN_BRACK);
			writer.write(NEWLINE);
			writer.write(
					CodeGeneratorUtils.getUpperCaseLeading(act.getName()) + "Action b = allActions.elementAt(i);");
			writer.write(NEWLINE);
			// go through all participants:
			Vector<ActionTypeParticipant> parts = act.getAllParticipants();
			for (int k = 0; k < parts.size(); k++) {
				ActionTypeParticipant tempPart = parts.elementAt(k);
				if (tempPart.getSimSEObjectTypeType() == SimSEObjectTypeTypes.EMPLOYEE) { 
					// participant is of employee type
					writer.write("if(b.getAll" + tempPart.getName() + "s().contains(e))");
					writer.write(NEWLINE);
					writer.write(OPEN_BRACK);
					writer.write(NEWLINE);

					// execute all destroyer rules that have executeOnJoins == true:
					Vector<Rule> destRules = act.getAllDestroyerRules();
					for (int i = 0; i < destRules.size(); i++) {
						Rule dRule = destRules.elementAt(i);
						if (dRule.getExecuteOnJoins() == true) {
							writer.write("ruleExec.update(parent, RuleExecutor.UPDATE_ONE, \"" + dRule.getName()
									+ "\", b);");
							writer.write(NEWLINE);
						}
					}

					writer.write("b.remove" + tempPart.getName() + "(e);");
					writer.write(NEWLINE);
					if ((outerDest.getDestroyerText() != null) && (outerDest.getDestroyerText().length() > 0)) {
						writer.write("e.setOverheadText(\"" + outerDest.getDestroyerText() + "\");");
						writer.write(NEWLINE);
					}
					writer.write("if(b.getAll" + tempPart.getName() + "s().size() < ");
					if (tempPart.getQuantity().isMinValBoundless()) { // no minimum
						writer.write("0)");
					} else { // has a minimum
						writer.write(tempPart.getQuantity().getMinVal().intValue() + ")");
					}
					writer.write(NEWLINE);
					writer.write(OPEN_BRACK);
					writer.write(NEWLINE);
					writer.write("Vector<SSObject> c = b.getAllParticipants();");
					writer.write(NEWLINE);
					writer.write("for(int j=0; j<c.size(); j++)");
					writer.write(NEWLINE);
					writer.write(OPEN_BRACK);
					writer.write(NEWLINE);
					writer.write("SSObject d = c.elementAt(j);");
					writer.write(NEWLINE);
					writer.write("if(d instanceof Employee)");
					writer.write(NEWLINE);
					writer.write(OPEN_BRACK);
					writer.write(NEWLINE);
					if ((outerDest.getDestroyerText() != null) && (outerDest.getDestroyerText().length() > 0)) {
						writer.write("((Employee)d).setOverheadText(\"" + outerDest.getDestroyerText() + "\");");
						writer.write(NEWLINE);
					}
					writer.write(CLOSED_BRACK);
					writer.write(NEWLINE);
					writer.write("else if(d instanceof Customer)");
					writer.write(NEWLINE);
					writer.write(OPEN_BRACK);
					writer.write(NEWLINE);
					if ((outerDest.getDestroyerText() != null) && (outerDest.getDestroyerText().length() > 0)) {
						writer.write("((Customer)d).setOverheadText(\"" + outerDest.getDestroyerText() + "\");");
						writer.write(NEWLINE);
					}
					writer.write(CLOSED_BRACK);
					writer.write(NEWLINE);
					writer.write(CLOSED_BRACK);
					writer.write(NEWLINE);

					// remove action from repository:
					writer.write("state.getActionStateRepository().get"
							+ CodeGeneratorUtils.getUpperCaseLeading(act.getName())
							+ "ActionStateRepository().remove(b);");
					writer.write(NEWLINE);

					// game-ending:
					if (outerDest.isGameEndingDestroyer()) {
						writer.write("// stop game and give score:");
						writer.write(NEWLINE);
						writer.write(CodeGeneratorUtils.getUpperCaseLeading(act.getName()) + "Action t111 = ("
								+ CodeGeneratorUtils.getUpperCaseLeading(act.getName()) + "Action)b;");
						writer.write(NEWLINE);
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
						if ((scoringAttConst != null) && (scoringPartConst != null) && (scoringPartDest != null)) {
							writer.write("if(t111.getAll" + scoringPartDest.getParticipant().getName()
									+ "s().size() > 0)");
							writer.write(NEWLINE);
							writer.write(OPEN_BRACK);
							writer.write(NEWLINE);
							writer.write(CodeGeneratorUtils
									.getUpperCaseLeading(scoringPartConst.getSimSEObjectType().getName())
									+ " t = ("
									+ CodeGeneratorUtils.getUpperCaseLeading(
											scoringPartConst.getSimSEObjectType().getName())
									+ ")(t111.getAll" + scoringPartDest.getParticipant().getName()
									+ "s().elementAt(0));");
							writer.write(NEWLINE);
							writer.write("if(t != null)");
							writer.write(NEWLINE);
							writer.write(OPEN_BRACK);
							writer.write(NEWLINE);
							if (scoringAttConst.getAttribute().getType() == AttributeTypes.INTEGER) {
								writer.write("int");
							} else if (scoringAttConst.getAttribute().getType() == AttributeTypes.DOUBLE) {
								writer.write("double");
							} else if (scoringAttConst.getAttribute().getType() == AttributeTypes.STRING) {
								writer.write("String");
							} else if (scoringAttConst.getAttribute().getType() == AttributeTypes.BOOLEAN) {
								writer.write("boolean");
							}
							writer.write(" v = t.get" + scoringAttConst.getAttribute().getName() + "();");
							writer.write(NEWLINE);
							writer.write("state.getClock().stop();");
							writer.write(NEWLINE);
							writer.write("state.setScore(v);");
							writer.write(NEWLINE);
							writer.write("((SimSEGUI)parent).update();");
							writer.write(NEWLINE);
							writer.write(
									"JOptionPane.showMessageDialog(null, (\"Your score is \" + v), \"Game over!\", JOptionPane.INFORMATION_MESSAGE);");
							writer.write(NEWLINE);
							writer.write(CLOSED_BRACK);
							writer.write(NEWLINE);
							writer.write(CLOSED_BRACK);
							writer.write(NEWLINE);
						}
					}

					writer.write(CLOSED_BRACK);
					writer.write(NEWLINE);
					writer.write(CLOSED_BRACK);
					writer.write(NEWLINE);
				}
			}
			writer.write(CLOSED_BRACK);
			writer.write(NEWLINE);
			writer.write(CLOSED_BRACK);
			writer.write(NEWLINE);
			writer.write("else");
			writer.write(NEWLINE);
			writer.write(OPEN_BRACK);
			writer.write(NEWLINE);
			writer.write(
					"Vector<" + CodeGeneratorUtils.getUpperCaseLeading(act.getName()) + "Action> b = new Vector<"
							+ CodeGeneratorUtils.getUpperCaseLeading(act.getName()) + "Action>();");
			writer.write(NEWLINE);
			writer.write("for(int i=0; i<allActions.size(); i++)");
			writer.write(NEWLINE);
			writer.write(OPEN_BRACK);
			writer.write(NEWLINE);
			writer.write(CodeGeneratorUtils.getUpperCaseLeading(act.getName()) + "Action c = ("
					+ CodeGeneratorUtils.getUpperCaseLeading(act.getName()) + "Action)allActions.elementAt(i);");
			writer.write(NEWLINE);
			// go through all participants:
			for (int k = 0; k < parts.size(); k++) {
				ActionTypeParticipant tempPart = parts.elementAt(k);
				if (tempPart.getSimSEObjectTypeType() == SimSEObjectTypeTypes.EMPLOYEE) {
					writer.write("if((c.getAll" + tempPart.getName() + "s().contains(e)) && (!(b.contains(c))))");
					writer.write(NEWLINE);
					writer.write(OPEN_BRACK);
					writer.write(NEWLINE);
					writer.write("b.add(c);");
					writer.write(NEWLINE);
					writer.write(CLOSED_BRACK);
					writer.write(NEWLINE);
				}
			}
			writer.write(CLOSED_BRACK);
			writer.write(NEWLINE);
			writer.write("new ChooseActionToDestroyDialog(parent, b, state, e, ruleExec, s);");
			writer.write(NEWLINE);
			writer.write(CLOSED_BRACK);
			writer.write(NEWLINE);
			writer.write(CLOSED_BRACK);
			writer.write(NEWLINE);
		}
		return destCode;
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