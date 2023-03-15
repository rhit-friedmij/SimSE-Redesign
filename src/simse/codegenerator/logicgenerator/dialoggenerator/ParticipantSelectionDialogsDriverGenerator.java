/*
 * This class is responsible for generating all of the code for the logic's
 * ParticipantSelectionDialogsDriver component
 */

package simse.codegenerator.logicgenerator.dialoggenerator;

import simse.codegenerator.CodeGeneratorConstants;
import simse.codegenerator.CodeGeneratorUtils;
import simse.modelbuilder.actionbuilder.ActionType;
import simse.modelbuilder.actionbuilder.ActionTypeParticipantAttributeConstraint;
import simse.modelbuilder.actionbuilder.ActionTypeParticipantConstraint;
import simse.modelbuilder.actionbuilder.ActionTypeParticipantTrigger;
import simse.modelbuilder.actionbuilder.ActionTypeTrigger;
import simse.modelbuilder.actionbuilder.DefinedActionTypes;
import simse.modelbuilder.actionbuilder.UserActionTypeTrigger;
import simse.modelbuilder.objectbuilder.AttributeTypes;
import simse.modelbuilder.objectbuilder.DefinedObjectTypes;
import simse.modelbuilder.objectbuilder.SimSEObjectType;
import simse.modelbuilder.objectbuilder.SimSEObjectTypeTypes;
import simse.modelbuilder.rulebuilder.Rule;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class ParticipantSelectionDialogsDriverGenerator implements CodeGeneratorConstants {
	private File directory; // directory to generate into
	private File psddFile; // file to generate
	private DefinedActionTypes actTypes; // holds all of the defined action types

	public ParticipantSelectionDialogsDriverGenerator(DefinedActionTypes actTypes, File directory) {
		this.directory = directory;
		this.actTypes = actTypes;
	}

	public void generate() {
		ClassName actionClass = ClassName.get("simse.adts.actions", "Action");
		ClassName vector = ClassName.get("java.util", "Vector");
		ClassName iteratorClass = ClassName.get("java.util", "Iterator");
		ClassName stateClass = ClassName.get("simse.state", "State");
		ClassName employeeClass = ClassName.get("simse.adts.objects", "Employee");
		ClassName ruleExecClass = ClassName.get("simse.logic", "RuleExecutor");
		ClassName destroyerCheckerClass = ClassName.get("simse.logic", "DestroyerChecker");
		ClassName melloPanelClass = ClassName.get("simse.gui", "MelloPanel");
		ClassName stringClass = ClassName.get(String.class);
		ClassName ssObjectClass = ClassName.get("simse.adts.objects", "SSObject");
		ClassName empPartDialog = ClassName.get("simse.logic.dialogs", "EmployeeParticipantSelectionDialog");
		ClassName nonEmpPartDialog = ClassName.get("simse.logic.dialogs", "NonEmployeeParticipantSelectionDialog");
		ClassName stageClass = ClassName.get("javafx.stage", "Stage");
		TypeName stringVector = ParameterizedTypeName.get(vector, stringClass);
		TypeName ssObjectWildcard = WildcardTypeName.subtypeOf(ssObjectClass);
		TypeName ssWildcardVector = ParameterizedTypeName.get(vector, ssObjectWildcard);
		TypeName nestedSSVector = ParameterizedTypeName.get(vector, ssWildcardVector);
		TypeName ssObjectVector = ParameterizedTypeName.get(vector, ssObjectClass);

		// gather all of the action types w/ user triggers:
		Vector<ActionType> acts = actTypes.getAllActionTypes();
		Vector<ActionType> userActs = new Vector<ActionType>();
		for (int i = 0; i < acts.size(); i++) {
			ActionType act = acts.elementAt(i);
			Vector<ActionTypeTrigger> allTrigs = act.getAllTriggers();
			for (int j = 0; j < allTrigs.size(); j++) {
				ActionTypeTrigger tempTrig = allTrigs.elementAt(j);
				if (tempTrig instanceof UserActionTypeTrigger) {
					userActs.add(act);
					break;
				}
			}
		}

		MethodSpec participantConstructor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC)
				.addParameter(stageClass, "parent").addParameter(stringVector, "pNames")
				.addParameter(nestedSSVector, "parts").addParameter(actionClass, "act").addParameter(stateClass, "s")
				.addParameter(ruleExecClass, "re").addParameter(destroyerCheckerClass, "dc")
				.addParameter(employeeClass, "emp").addParameter(String.class, "mText")
				.addStatement("$N = pNames", "partNames").addStatement("$N = parts", "partsVector")
				.addStatement("$N = act", "action").addStatement("$N = s", "state")
				.addStatement("$N = emp", "selectedEmp").addStatement("$N = re", "ruleExec")
				.addStatement("$N = dc", "destChecker").addStatement("$N = mText", "menuText")
				.addStatement("$N = $T.getInstance()", "mello", melloPanelClass)
				.addStatement("$T actionValid = true", boolean.class)
				.beginControlFlow("for (int i = 0; i < $N.size(); i++) ", "partNames")
				.addStatement("$T participantName = $N.elementAt(i)", String.class, "partNames")
				.addStatement("$T participants = $N.elementAt(i)", ssWildcardVector, "partsVector")
				.addStatement("// check to see if any of these possible participants have alread")
				.addStatement("// been added to the action in a different role")
				.addStatement("$T allParts = $N.getAllParticipants()", ssObjectVector, "action")
				.addStatement("$T participantsEnum = participants.elements()", Enumeration.class)
				.beginControlFlow("while (participantsEnum.hasMoreElements()) ")
				.addStatement("$T tempObj = ($T) participantsEnum.nextElement()", ssObjectClass, ssObjectClass)
				.beginControlFlow("for (int k = 0; k < allParts.size(); k++) ")
				.addStatement("$T tempObj2 = allParts.elementAt(k)", ssObjectClass)
				.beginControlFlow("if (tempObj == tempObj2) ").addStatement("participants.remove(tempObj)")
				.addStatement("break").endControlFlow().endControlFlow().endControlFlow()
				.beginControlFlow("if ((participants.size() == 0) || (participants." + "elementAt(0) instanceof $T)) ",
						employeeClass)
				.beginControlFlow("for (int j = 0; j < allParts.size(); j++) ")
				.addStatement("$T tempObj = allParts.elementAt(j)", ssObjectClass)
				.beginControlFlow("if (($N != null) && (tempObj == $N)) ", "selectedEmp", "selectedEmp")
				.addStatement("$N = null", "selectedEmp").addStatement("break").endControlFlow().endControlFlow()
				.addStatement("$T participantsContainsSelEmp = false", boolean.class)
				.addStatement("$T participantsIterator = participants.iterator()", iteratorClass)
				.beginControlFlow("while (participantsIterator.hasNext()) ")
				.addStatement("$T tempObj = ($T) participantsIterator.next()", ssObjectClass, ssObjectClass)
				.beginControlFlow("if (tempObj == $N) ", "selectedEmp")
				.addStatement("participantsContainsSelEmp = true").addStatement("break").endControlFlow()
				.endControlFlow().beginControlFlow("if (($N != null) && (participantsContainsSelEmp)) ", "selectedEmp")
				.addStatement("participants.remove($N)", "selectedEmp")
				.addStatement("$T psd = new $T(parent, participantName, new $T(participants), $N," + " $N, $N)",
						empPartDialog, empPartDialog, ssObjectVector, "action", "state", "selectedEmp")
				.beginControlFlow("if (psd.actionCancelled()) ").addStatement("actionValid = false")
				.addStatement("break").endControlFlow().nextControlFlow(" else ")
				.addStatement("$T psd = new $T(parent, participantName, new $T(participants), $N," + " $N, null)",
						empPartDialog, empPartDialog, ssObjectVector, "action", "state")
				.beginControlFlow("if (psd.actionCancelled()) ").addStatement("actionValid = false")
				.addStatement("break").endControlFlow().endControlFlow().nextControlFlow(" else ")
				.addStatement("$T psd = new $T(parent, participantName, new $T(participants)," + " $N, $N)",
						nonEmpPartDialog, nonEmpPartDialog, ssObjectVector, "action", "state")
				.beginControlFlow("if (psd.actionCancelled()) ").addStatement("actionValid = false")
				.addStatement("break").endControlFlow().endControlFlow().endControlFlow()
				.beginControlFlow("if (actionValid) ").addCode(handleActions(userActs)).endControlFlow().build();

		TypeSpec participantDialog = TypeSpec.classBuilder("ParticipantSelectionDialogsDriver")
				.addModifiers(Modifier.PUBLIC).addField(stringVector, "partNames", Modifier.PRIVATE)
				.addField(nestedSSVector, "partsVector", Modifier.PRIVATE)
				.addField(actionClass, "action", Modifier.PRIVATE).addField(stateClass, "state", Modifier.PRIVATE)
				.addField(employeeClass, "selectedEmp", Modifier.PRIVATE)
				.addField(ruleExecClass, "ruleExec", Modifier.PRIVATE)
				.addField(destroyerCheckerClass, "destChecker", Modifier.PRIVATE)
				.addField(String.class, "menuText", Modifier.PRIVATE)
				.addField(melloPanelClass, "mello", Modifier.PRIVATE).addMethod(participantConstructor).build();

		JavaFile javaFile = JavaFile.builder("", participantDialog).build();

		try {
			psddFile = new File(directory, ("simse\\logic\\dialogs\\ParticipantSelectionDialogsDriver.java"));
			if (psddFile.exists()) {
				psddFile.delete(); // delete old version of file
			}

			FileWriter writer = new FileWriter(psddFile);
			String toAppend = "/* File generated by: simse.codegenerator.logicgenerator.dialoggenerator.ParticipantSelectionDialogsDriverGenerator */\n"
					+ "package simse.logic.dialogs;\n" + "\n" + "import simse.adts.actions.*;\n"
					+ "import simse.adts.objects.*;\n" + "import simse.gui.SimSEGUI;\n"
					+ "import javafx.scene.control.Alert.*;\n" + "import javafx.scene.control.Alert;\n";

			writer.write(toAppend + javaFile.toString());
			writer.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, ("Error writing file " + psddFile.getPath() + ": " + e.toString()),
					"File IO Error", JOptionPane.WARNING_MESSAGE);
		}
	}

	private String handleActions(Vector<ActionType> userActs) {
		String actions = "";
		for (int i = 0; i < userActs.size(); i++) {
			ActionType tempAct = userActs.elementAt(i);
			if (i > 0) { // not on first element
				actions += "else ";
			}
			actions += "if(action instanceof " + CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName())
					+ "Action){\n";
			actions += "Vector<SSObject> participants = action.getAllParticipants();\n";
			actions += "Vector<Object> people = new Vector<Object>();\n";

			actions += "for(int i=0; i<participants.size(); i++){\n";
			actions += "SSObject obj = participants.elementAt(i);\n";
			actions += "if(obj instanceof Employee){\n";

			// generate conditions for each user trigger:
			Vector<ActionTypeTrigger> allTrigs = tempAct.getAllTriggers();
			boolean putElse9 = false;
			for (int j = 0; j < allTrigs.size(); j++) {
				ActionTypeTrigger tempTrig = allTrigs.elementAt(j);
				if ((tempTrig instanceof UserActionTypeTrigger) && (tempTrig.getTriggerText() != null)
						&& (tempTrig.getTriggerText().length() > 0)) {
					if (putElse9) {
						actions += "else ";
					} else {
						putElse9 = true;
					}
					actions += "if(menuText.equals(\"" + ((UserActionTypeTrigger) tempTrig).getMenuText() + "\")){\n";
					actions += "((Employee)obj).setOverheadText(\"" + tempTrig.getTriggerText() + "\");\n";
					actions += "people.add(obj);\n}\n";
				}
			}
			actions += "} else if(obj instanceof Customer){\n";

			// generate conditions for each user trigger:
			boolean putElse8 = false;
			for (int j = 0; j < allTrigs.size(); j++) {
				ActionTypeTrigger tempTrig = allTrigs.elementAt(j);
				if ((tempTrig instanceof UserActionTypeTrigger) && (tempTrig.getTriggerText() != null)
						&& (tempTrig.getTriggerText().length() > 0)) {
					if (putElse8) {
						actions += "else ";
					} else {
						putElse8 = true;
					}
					actions += "if(menuText.equals(\"" + ((UserActionTypeTrigger) tempTrig).getMenuText() + "\")){\n";
					actions += "((Customer)obj).setOverheadText(\"" + tempTrig.getTriggerText() + "\");\n";
					actions += "people.add(obj);\n}\n";
				}
			}
			actions += "}\n}\nstate.getActionStateRepository().get"
					+ CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName()) + "ActionStateRepository().add(("
					+ CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName()) + "Action)action);\n";
			// execute all trigger rules:
			Vector<Rule> trigRules = tempAct.getAllTriggerRules();
			for (int j = 0; j < trigRules.size(); j++) {
				Rule tRule = trigRules.elementAt(j);
				actions += "ruleExec.update(parent, RuleExecutor.UPDATE_ONE, \"" + tRule.getName() + "\", action);\n";
			}
			actions += "destChecker.update(false, parent);\n";
			actions += "mello.addTaskInProgress(\"" + CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName())
					+ "\", people);\n";

			// game-ending:
			if (tempAct.hasGameEndingTrigger()) {
				Vector<ActionTypeTrigger> trigs = tempAct.getAllTriggers();
				boolean putElse7 = false;
				for (int j = 0; j < trigs.size(); j++) {
					ActionTypeTrigger tempTrig = trigs.elementAt(j);
					if (tempTrig.isGameEndingTrigger()) {
						if (putElse7) {
							actions += "else ";
						} else {
							putElse7 = true;
						}
						actions += "if(menuText.equals(\"" + ((UserActionTypeTrigger) tempTrig).getMenuText()
								+ "\"))\n{\n";

						actions += "// stop game and give score:\n";
						actions += CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName()) + "Action a = ("
								+ CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName()) + "Action)action;\n";
						// find the scoring attribute:
						ActionTypeParticipantTrigger scoringPartTrig = null;
						ActionTypeParticipantConstraint scoringPartConst = null;
						ActionTypeParticipantAttributeConstraint scoringAttConst = null;
						Vector<ActionTypeParticipantTrigger> partTrigs = tempTrig.getAllParticipantTriggers();
						for (int k = 0; k < partTrigs.size(); k++) {
							ActionTypeParticipantTrigger partTrig = partTrigs.elementAt(k);
							Vector<ActionTypeParticipantConstraint> partConsts = partTrig.getAllConstraints();
							for (int m = 0; m < partConsts.size(); m++) {
								ActionTypeParticipantConstraint partConst = partConsts.elementAt(m);
								ActionTypeParticipantAttributeConstraint[] attConsts = partConst
										.getAllAttributeConstraints();
								for (int n = 0; n < attConsts.length; n++) {
									if (attConsts[n].isScoringAttribute()) {
										scoringAttConst = attConsts[n];
										scoringPartConst = partConst;
										scoringPartTrig = partTrig;
										break;
									}
								}
							}
						}
						if ((scoringAttConst != null) && (scoringPartConst != null) && (scoringPartTrig != null)) {
							actions += "if(a.getAll" + scoringPartTrig.getParticipant().getName()
									+ "s().size() > 0)\n{\n";
							actions += CodeGeneratorUtils
									.getUpperCaseLeading(scoringPartConst.getSimSEObjectType().getName())
									+ " t = ("
									+ CodeGeneratorUtils.getUpperCaseLeading(
											scoringPartConst.getSimSEObjectType().getName())
									+ ")(a.getAll" + scoringPartTrig.getParticipant().getName()
									+ "s().elementAt(0));\n";
							actions += "if(t != null)\n{\n";
							if (scoringAttConst.getAttribute().getType() == AttributeTypes.INTEGER) {
								actions += "int";
							} else if (scoringAttConst.getAttribute().getType() == AttributeTypes.DOUBLE) {
								actions += "double";
							} else if (scoringAttConst.getAttribute().getType() == AttributeTypes.STRING) {
								actions += "String";
							} else if (scoringAttConst.getAttribute().getType() == AttributeTypes.BOOLEAN) {
								actions += "boolean";
							}
							actions += " v = t.get" + scoringAttConst.getAttribute().getName() + "();\n";
							actions += "state.getClock().stop();\n";
							actions += "state.setScore(v);\n";
							actions += "((SimSEGUI)parent).update();\n";
							actions += "Alert d = new Alert(AlertType.INFORMATION);\n";
							actions += "d.setContentText((\"Your score is \" + v));\n";
							actions += "d.setTitle(\"Game over!\");\n";
							actions += "d.setHeaderText(null);\n";
							actions += "d.showAndWait();\n}\n}\n}\n";
						}
					}
				}
			}
			actions += "}\n";
		}
		return actions;
	}
}