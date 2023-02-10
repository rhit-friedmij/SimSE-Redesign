/*
 * This class is responsible for generating all of the code for the destroyer
 * checker component in the Logic component of SimSE
 */

package simse.codegenerator.logicgenerator;

import simse.codegenerator.CodeGeneratorConstants;
import simse.codegenerator.CodeGeneratorUtils;

import simse.modelbuilder.objectbuilder.AttributeTypes;
import simse.modelbuilder.objectbuilder.SimSEObjectTypeTypes;
import simse.modelbuilder.actionbuilder.ActionType;
import simse.modelbuilder.actionbuilder.ActionTypeDestroyer;
import simse.modelbuilder.actionbuilder.ActionTypeParticipant;
import simse.modelbuilder.actionbuilder.ActionTypeParticipantAttributeConstraint;
import simse.modelbuilder.actionbuilder.ActionTypeParticipantConstraint;
import simse.modelbuilder.actionbuilder.ActionTypeParticipantDestroyer;
import simse.modelbuilder.actionbuilder.AttributeGuard;
import simse.modelbuilder.actionbuilder.AutonomousActionTypeDestroyer;
import simse.modelbuilder.actionbuilder.DefinedActionTypes;
import simse.modelbuilder.actionbuilder.RandomActionTypeDestroyer;
import simse.modelbuilder.actionbuilder.TimedActionTypeDestroyer;
import simse.modelbuilder.actionbuilder.UserActionTypeDestroyer;
import simse.modelbuilder.rulebuilder.Rule;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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

public class DestroyerCheckerGenerator implements CodeGeneratorConstants {
	private File directory; // directory to save generated code into
	private DefinedActionTypes actTypes;
	private FileWriter writer;
	private File destFile;
	private Vector<ActionTypeDestroyer> nonPrioritizedDestroyers;
	private Vector<ActionTypeDestroyer> prioritizedDestroyers;
	private Vector<ActionTypeDestroyer> allDestroyers;
	
	private ClassName vector = ClassName.get("java.util", "Vector");
	private ClassName ruleExecuter = ClassName.get("simse.logic", "RuleExecuter");
	private MethodSpec update = null;
	

	public DestroyerCheckerGenerator(DefinedActionTypes actTypes, File directory) {
		this.actTypes = actTypes;
		this.directory = directory;
		initializeDestroyerLists();
	}

	public void generate() {
		ClassName random = ClassName.get("java.util", "Random");
		ClassName stage = ClassName.get("javafx.stage", "Stage");
		ClassName action = ClassName.get("simse.adts.actions", "Action");
		ClassName breakAction = ClassName.get("simse.adts.actions", "BreakAction");
		ClassName changePayRateAction = ClassName.get("simse.adts.actions", "ChangePayRateAction");
		ClassName correctCodeAction = ClassName.get("simse.adts.actions", "CorrectCodeAction");
		ClassName correctDesignAction = ClassName.get("simse.adts.actions", "CorrectDesignAction");
		ClassName correctRequirementsAction = ClassName.get("simse.adts.actions", "CorrectRequirementsAction");
		ClassName correctSystemTestPlanAction = ClassName.get("simse.adts.actions", "CorrectSystemTestPlanAction");
		ClassName createCodeAction = ClassName.get("simse.adts.actions", "CreateCodeAction");
		ClassName createDesignAction = ClassName.get("simse.adts.actions", "CreateDesignAction");
		ClassName createRequirementsAction = ClassName.get("simse.adts.actions", "CreateRequirementsAction");
		ClassName createSystemTestPlanAction = ClassName.get("simse.adts.actions", "CreateSystemTestPlanAction");
		ClassName fireAction = ClassName.get("simse.adts.actions", "FireAction");
		ClassName getSickAction = ClassName.get("simse.adts.actions", "GetSickAction");
		ClassName giveBonusAction = ClassName.get("simse.adts.actions", "GiveBonusAction");
		ClassName inspectCodeAction = ClassName.get("simse.adts.actions", "InspectCodeAction");
		ClassName integrateCodeAction = ClassName.get("simse.adts.actions", "IntegrateCodeAction");
		ClassName introduceNewRequirementsAction = ClassName.get("simse.adts.actions", "IntroduceNewRequirementsAction");
		ClassName purchaseToolAction = ClassName.get("simse.adts.actions", "PurchaseToolAction");
		ClassName quitAction = ClassName.get("simse.adts.actions", "QuitAction");
		ClassName reviewDesignAction = ClassName.get("simse.adts.actions", "ReviewDesignAction");
		ClassName reviewRequirementsAction = ClassName.get("simse.adts.actions", "ReviewRequirementsAction");
		ClassName reviewSystemTestPlanAction = ClassName.get("simse.adts.actions", "ReviewSystemTestPlanAction");
		ClassName suggestedDesignPhaseDurationAction = ClassName.get("simse.adts.actions", "SuggestedDesignPhaseDurationAction");
		ClassName suggestedImplIntegrationPhaseDurationAction = ClassName.get("simse.adts.actions", "SuggestedImplIntegrationPhaseDurationAction");
		ClassName suggestedRequirementsPhaseDurationAction = ClassName.get("simse.adts.actions", "SuggestedRequirementsPhaseDurationAction");
		ClassName suggestedTestingPhaseDurationAction = ClassName.get("simse.adts.actions", "SuggestedTestingPhaseDurationAction");
		ClassName systemTestAction = ClassName.get("simse.adts.actions", "SystemTestAction");
		ClassName artifact = ClassName.get("simse.adts.objects", "Artifact");
		ClassName automatedTestingTool = ClassName.get("simse.adts.objects", "AutomatedTestingTool");
		ClassName code = ClassName.get("simse.adts.objects", "Code");
		ClassName designDocument = ClassName.get("simse.adts.objects", "DesignDocument");
		ClassName designEnvironment = ClassName.get("simse.adts.objects", "DesignEnvironment");
		
		ClassName iDE = ClassName.get("simse.adts.objects", "IDE");
		ClassName project = ClassName.get("simse.adts.objects", "Project");
		ClassName requirementsCaptureTool = ClassName.get("simse.adts.objects", "RequirementsCaptureTool");
		ClassName requirementsDocument = ClassName.get("simse.adts.objects", "RequirementsDocument");
		ClassName seProject = ClassName.get("simse.adts.objects", "SEProject");
		
		ClassName softwareEngineer = ClassName.get("simse.adts.objects", "SoftwareEngineer");
		ClassName systemTestPlan = ClassName.get("simse.adts.objects", "SystemTestPlan");
		ClassName tool = ClassName.get("simse.adts.objects", "Tool");
		ClassName melloPanel = ClassName.get("simse.gui", "MelloPanel");
		ClassName state = ClassName.get("simse.state", "State");
		ClassName menuInput = ClassName.get("simse.logic", "MenuInputManager");
		ClassName trigCheck = ClassName.get("simse.logic", "TriggerChecker");
		ClassName destCheck = ClassName.get("simse.logic", "DestroyerChecker");
		
		ClassName miscUpdater = ClassName.get("simse.logic", "MiscUpdater");
		TypeName vectorOfActions = ParameterizedTypeName.get(vector, action);

		MethodSpec destroyerConstructor = MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
				.addParameter(state, "s")
				.addParameter(ruleExecuter, "r")
				.addParameter(trigCheck, "t")
				.addStatement("state = s")
				.addStatement("ruleExec = r")
				.addStatement("trigCheck = t")
				.addStatement("ranNumGen = new $T()", random)
				.addStatement("mello = $T.getInstance()", melloPanel)
				.build();
		
		 MethodSpec update = MethodSpec.methodBuilder("update")
					.addModifiers(Modifier.PUBLIC)
					.returns(void.class)
					.addParameter(boolean.class, "updateUserDestsOlnly")
					.addParameter(stage, "gui")
					.addStatement("$T actions = state.getActionStateRepository().getAllActions()", vectorOfActions)
					.beginControlFlow("for (int i = 0 i < actions.size() i++) {")
					.addStatement("$T tempAct = actions.elementAt(i)", action)
					.addCode(getDestroyerConditions().build())
					.endControlFlow()
					.addComment("update trigger checker:")
					.addStatement("trigCheck.update(true, gui)")
					.build();
		 
		 TypeSpec destroyer = TypeSpec.classBuilder("DestroyerChecker")
					.addModifiers(Modifier.PUBLIC)
					.addField(state, "state", Modifier.PRIVATE)
					.addMethod(destroyerConstructor)
					.addMethod(update)
					.build();
		  
		  JavaFile javaFile = JavaFile.builder("simse.logic", destroyer)
					.addFileComment("File generated by: simse.codegenerator.logicgenerator.DestroyerCheckerGenerator")
					.build();

		try {
			destFile = new File(directory, ("simse\\logic\\DestroyerChecker.java"));
			if (destFile.exists()) {
				destFile.delete(); // delete old version of file
			}
			
			System.out.println(javaFile.toString());
		    javaFile.writeTo(destFile);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, ("Error writing file " + destFile.getPath() + ": " + e.toString()),
					"File IO Error", JOptionPane.WARNING_MESSAGE);
		}
	}
	
	private CodeBlock.Builder getDestroyerConditions() {
		ClassName alert = ClassName.get("javafx.scene.control", "Alert");
		ClassName alertType = ClassName.get("javafx.scene.control.Alert", "AlertType");
		ClassName customer = ClassName.get("simse.adts.objects", "Customer");
		ClassName employee = ClassName.get("simse.adts.objects", "Employee");
		ClassName simseGui = ClassName.get("simse.gui", "SimSEGUI");
		ClassName ssObject = ClassName.get("simse.adts.objects", "SSObject");
		TypeName vectorOfObjects = ParameterizedTypeName.get(vector, ssObject);
		
		CodeBlock.Builder conditions = CodeBlock.builder();
		// go through each destroyer:
		for (int i = 0; i < allDestroyers.size(); i++) {
			ActionTypeDestroyer tempDest = allDestroyers.elementAt(i);
			ActionType tempAct = tempDest.getActionType();
			String tempActName = tempAct.getName().toLowerCase() + "TempAct";
			String actTypeName = CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName()) + "Action";
	    	ClassName actName = ClassName.get("simse.adts.actions", actTypeName);
	    	
			conditions.beginControlFlow("if((tempAct instanceof " + actTypeName + ") && (state.getActionStateRepository().get"
					+ actTypeName + "StateRepository().getAllActions().contains(tempAct)))");
			conditions.addStatement("$T " + tempActName + " = (" + actTypeName + ")tempAct", actName);

			// timed destroyer
			if (tempDest instanceof TimedActionTypeDestroyer) { 
				conditions.beginControlFlow("if(!updateUserDestsOnly)");
				conditions.beginControlFlow("if(" + tempActName + ".getTimeToLive() == 0)");
				conditions.addStatement("$T b = tempAct.getAllParticipants()", vectorOfObjects);
				conditions.beginControlFlow("for (int j = 0; j < b.size(); j++)");
				conditions.addStatement("$T c = b.elementAt(j)", ssObject);
				conditions.beginControlFlow("if(c instanceof $T)", employee);
				
				if ((tempDest.getDestroyerText() != null) && (tempDest.getDestroyerText().length() > 0)) {
					conditions.addStatement("(($T)c).setOverheadText(\"" + tempDest.getDestroyerText() + "\")", employee);
				}
				
				conditions.nextControlFlow("else if(c instanceof $T)", customer);
				if ((tempDest.getDestroyerText() != null) && (tempDest.getDestroyerText().length() > 0)) {
					conditions.addStatement("(($T)c).setOverheadText(\"" + tempDest.getDestroyerText() + "\")", customer);
				}
				conditions.endControlFlow(); // ObjectType
				conditions.endControlFlow(); // For loop

				// execute all destroyer rules:
				Vector<Rule> destRules = tempAct.getAllDestroyerRules();
				for (int k = 0; k < destRules.size(); k++) {
					Rule dRule = destRules.elementAt(k);
					conditions.addStatement("ruleExec.update(gui, $T.UPDATE_ONE, \"" + dRule.getName() + "\", tempAct)", ruleExecuter);
				}
				conditions.addStatement("state.getActionStateRepository().get" + actTypeName 
						+ "StateRepository().remove(" + tempActName + ")");
				conditions.addStatement("trigCheck.update(true, gui)");
				conditions.addStatement("$N(false, gui)", update);

				// game-ending:
				if (tempDest.isGameEndingDestroyer()) {
					conditions.add("// stop game and give score:");
					conditions.addStatement("$T t111 = (" + actTypeName + ")tempAct", actName);
					// find the scoring attribute:
					ActionTypeParticipantDestroyer scoringPartDest = null;
					ActionTypeParticipantConstraint scoringPartConst = null;
					ActionTypeParticipantAttributeConstraint scoringAttConst = null;
					Vector<ActionTypeParticipantDestroyer> partDests = tempDest.getAllParticipantDestroyers();
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
					String scoringPartConstObj = CodeGeneratorUtils
							.getUpperCaseLeading(scoringPartConst.getSimSEObjectType().getName());
					ClassName scoringPartConstObjName = ClassName.get("simse.adts.objects", scoringPartConstObj);
					if ((scoringAttConst != null) && (scoringPartConst != null) && (scoringPartDest != null)) {
						conditions.beginControlFlow("if(t111.getAll" + scoringPartDest.getParticipant().getName() 
								+ "s().size() > 0)");
						conditions.addStatement("$T t = ($T)(t111.getAll" + scoringPartDest.getParticipant().getName()
								+ "s().elementAt(0))", scoringPartConstObjName, scoringPartConstObjName);
						if (scoringAttConst.getAttribute().getType() == AttributeTypes.INTEGER) {
							conditions.addStatement("$T v = t.get" + scoringAttConst.getAttribute().getName() + "()", int.class);
						} else if (scoringAttConst.getAttribute().getType() == AttributeTypes.DOUBLE) {
							conditions.addStatement("$T v = t.get" + scoringAttConst.getAttribute().getName() + "()", double.class);
						} else if (scoringAttConst.getAttribute().getType() == AttributeTypes.STRING) {
							conditions.addStatement("$T v = t.get" + scoringAttConst.getAttribute().getName() + "()", String.class);
						} else if (scoringAttConst.getAttribute().getType() == AttributeTypes.BOOLEAN) {
							conditions.addStatement("$T v = t.get" + scoringAttConst.getAttribute().getName() + "()", boolean.class);
						}
						conditions.addStatement("state.getClock().stop()");
						conditions.addStatement("state.setScore(v)");
						conditions.addStatement("(($T)gui).update()", simseGui);
						conditions.addStatement("$T d = new $T($T.INFORMATION)", alert, alert, alertType);
						conditions.addStatement("d.setContentText(($S + v))", "Your score is ");
						conditions.addStatement("d.setTitle($S)", "Game over!");
						conditions.addStatement("d.setHeaderText(null)");
						conditions.addStatement("d.showAndWait()");
						conditions.endControlFlow(); // game ending if condition
					}
				}

				conditions.endControlFlow(); // time to live condition
				conditions.endControlFlow(); // update destroyers
				conditions.endControlFlow(); // ActionType
			} else { 
				// random, user, or autonomous destroyer
				if ((tempDest instanceof RandomActionTypeDestroyer) || (tempDest instanceof AutonomousActionTypeDestroyer)) {
					conditions.beginControlFlow("if(!updateUserDestsOnly)");
				}
				conditions.addStatement("$T destroy = true", boolean.class);

				Vector<ActionTypeParticipantDestroyer> destroyers = tempDest.getAllParticipantDestroyers();
				// go through each participant destroyer:
				for (int j = 0; j < destroyers.size(); j++) {
					ActionTypeParticipantDestroyer dest = destroyers.elementAt(j);
					ActionTypeParticipant part = dest.getParticipant();
					String objTypeType = SimSEObjectTypeTypes.getText(part.getSimSEObjectTypeType());
					ClassName objTypeTypeName = ClassName.get("simse.adts.objects", objTypeType);
					TypeName vectorOfObjTypeTypes = ParameterizedTypeName.get(vector, objTypeTypeName);
					String partName = part.getName();
					String partNameVar = partName.toLowerCase() + "s";
					
					conditions.addStatement("$T " + partNameVar + " = " + tempActName
							+ ".getAll" + partName + "s()", vectorOfObjTypeTypes);
					conditions.beginControlFlow("for(int j=0; j<" + partNameVar + ".size(); j++)");
					conditions.addStatement("$T a = " + partNameVar + ".elementAt(j)", objTypeTypeName);
					
					// go through all participant constraints:
					Vector<ActionTypeParticipantConstraint> constraints = dest.getAllConstraints();
					for (int k = 0; k < constraints.size(); k++) {
						ActionTypeParticipantConstraint constraint = constraints.elementAt(k);
						String objTypeName = constraint.getSimSEObjectType().getName();
						ClassName objTypeClassName = ClassName.get("simse.adts.objects", 
								CodeGeneratorUtils.getUpperCaseLeading(objTypeName));
						if (k == 0) { // on first element
							conditions.beginControlFlow("if(a instanceof $T)", objTypeClassName);
						}
						conditions.nextControlFlow("else if(a instanceof $T)", objTypeClassName);
						// go through all attribute constraints:
						ActionTypeParticipantAttributeConstraint[] attConstraints = constraint.getAllAttributeConstraints();
						int numAttConsts = 0;
						String ifConditional = "";
						Vector<ClassName> classes = new Vector<>();
						for (int m = 0; m < attConstraints.length; m++) {
							ActionTypeParticipantAttributeConstraint tempAttConst = attConstraints[m];
							if (tempAttConst.isConstrained()) {
								if (numAttConsts == 0) { 
									// this is the first attribute that we've come across that's constrained
									ifConditional += "if(";
								} else {
									ifConditional += " || ";
								}
								ifConditional += "(!((($T)a).get" + tempAttConst.getAttribute().getName() + "()";
								classes.add(objTypeClassName);
								if (tempAttConst.getAttribute().getType() == AttributeTypes.STRING) {
									ifConditional += ".equals(" + "\"" + tempAttConst.getValue().toString() + "\")";
								} else {
									if (tempAttConst.getGuard().equals(AttributeGuard.EQUALS)) {
										ifConditional += " == ";
									} else {
										ifConditional += " " + tempAttConst.getGuard() + " ";
									}
									ifConditional += tempAttConst.getValue().toString();
								}
								ifConditional += "))";
								numAttConsts++;
							}
						}
						if (numAttConsts > 0) { // there is at least one constraint
							ifConditional += ")";
							conditions.beginControlFlow(ifConditional, classes);
							conditions.addStatement("destroy = false");
							conditions.addStatement("break");
							conditions.endControlFlow();
						}
						conditions.endControlFlow();
					}
					conditions.endControlFlow();
				}
				if (tempDest instanceof RandomActionTypeDestroyer) {
					conditions.beginControlFlow("if ((destroy) && ((ranNumGen.nextDouble() * 100.0) < \"\r\n" + 
							+ ((RandomActionTypeDestroyer) (tempDest)).getFrequency() + "))");
				} else { 
					// user, action or autonomous
					conditions.beginControlFlow("if (destroy)");
				}
				
				conditions.addStatement("$T b = tempAct.getAllParticipants()", vectorOfObjects);
				conditions.beginControlFlow("for (int j = 0 j < b.size() j++) {");
				conditions.addStatement("$T c = b.elementAt(j)", ssObject);
				conditions.beginControlFlow("if (c instanceof $T) {", employee);
				if ((tempDest instanceof AutonomousActionTypeDestroyer) || (tempDest instanceof RandomActionTypeDestroyer)) {
					
					if ((tempDest.getDestroyerText() != null) && (tempDest.getDestroyerText().length() > 0)) {
						conditions.addStatement("(($T)c).setOverheadText(\"" + tempDest.getDestroyerText() + "\")", employee);
					}

					// For each user destroyer for this action, remove the menu item:
					for (int k = 0; k < allDestroyers.size(); k++) {
						ActionTypeDestroyer tempDest2 = allDestroyers.elementAt(k);
						if ((tempDest2 instanceof UserActionTypeDestroyer) && (tempDest2 != tempDest) && (tempDest2
								.getActionType().getName().equals(tempDest.getActionType().getName()))) {
							writer.write("((Employee) c).removeMenuItem(\""
									+ ((UserActionTypeDestroyer) tempDest2).getMenuText() + "\");");
							writer.write(NEWLINE);
						}
					}

					conditions.nextControlFlow("else if(c instanceof $T)", customer);
					if ((tempDest.getDestroyerText() != null) && (tempDest.getDestroyerText().length() > 0)) {
						conditions.addStatement("(($T)c).setOverheadText(\"" + tempDest.getDestroyerText() + "\")", customer);
					}
					conditions.endControlFlow();
					conditions.endControlFlow();

					// execute all destroyer rules:
					Vector<Rule> destRules = tempAct.getAllDestroyerRules();
					for (int k = 0; k < destRules.size(); k++) {
						Rule dRule = destRules.elementAt(k);
						writer.write("ruleExec.update(gui, RuleExecutor.UPDATE_ONE, \"" + dRule.getName()
								+ "\", tempAct);");
						writer.write(NEWLINE);
					}
					writer.write("state.getActionStateRepository().get"
							+ CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName())
							+ "ActionStateRepository().remove(" + tempAct.getName().toLowerCase() + "TempAct);");
					writer.write(NEWLINE);
					writer.write("trigCheck.update(true, gui);");
					writer.write(NEWLINE);
					writer.write("update(false, gui);");
					writer.write(NEWLINE);

					// game-ending:
					if (tempDest.isGameEndingDestroyer()) {
						writer.write("// stop game and give score:");
						writer.write(NEWLINE);
						writer.write(CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName()) + "Action t111 = ("
								+ CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName()) + "Action)tempAct;");
						writer.write(NEWLINE);
						// find the scoring attribute:
						ActionTypeParticipantDestroyer scoringPartDest = null;
						ActionTypeParticipantConstraint scoringPartConst = null;
						ActionTypeParticipantAttributeConstraint scoringAttConst = null;
						Vector<ActionTypeParticipantDestroyer> partDests = tempDest.getAllParticipantDestroyers();
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
							writer.write("((SimSEGUI)gui).update();");
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
				} else { // user destroyer
					writer.write("((Employee)c).addMenuItem(\""
							+ ((UserActionTypeDestroyer) (tempDest)).getMenuText() + "\");");
					writer.write(NEWLINE);
					writer.write(CLOSED_BRACK);
					writer.write(NEWLINE);
					writer.write(CLOSED_BRACK);
					writer.write(NEWLINE);
					writer.write(CLOSED_BRACK);
					writer.write(NEWLINE);
				}
				writer.write(CLOSED_BRACK);
				writer.write(NEWLINE);
			}
		}
		return dconditions;
	}

	/*
	 * gets the destroyers in prioritized order according to their priority
	 */
	private void initializeDestroyerLists() {
		// initialize lists:
		nonPrioritizedDestroyers = new Vector<ActionTypeDestroyer>();
		prioritizedDestroyers = new Vector<ActionTypeDestroyer>();
		Vector<ActionType> allActions = actTypes.getAllActionTypes();
		// go through all action types and get their destroyers:
		for (int i = 0; i < allActions.size(); i++) {
			ActionType tempAct = allActions.elementAt(i);
			Vector<ActionTypeDestroyer> dests = tempAct.getAllDestroyers();
			for (int j = 0; j < dests.size(); j++) {
				ActionTypeDestroyer tempDest = dests.elementAt(j);
				int priority = tempDest.getPriority();
				if (priority == -1) { // destroyer is not prioritized
					nonPrioritizedDestroyers.addElement(tempDest);
				} else { // priority >= 0
					if (prioritizedDestroyers.size() == 0) { // no elements have been
																// added yet to the
																// prioritized destroyer list
						prioritizedDestroyers.add(tempDest);
					} else {
						// find the correct position to insert the destroyer at:
						for (int k = 0; k < prioritizedDestroyers.size(); k++) {
							ActionTypeDestroyer tempA = prioritizedDestroyers.elementAt(k);
							if (priority <= tempA.getPriority()) {
								prioritizedDestroyers.insertElementAt(tempDest, k); // insert
																					// the
																					// destroyer
								break;
							} else if (k == (prioritizedDestroyers.size() - 1)) { // on the
																					// last
																					// element
								prioritizedDestroyers.add(tempDest); // add the destroyer to the
																		// end of the list
								break;
							}
						}
					}
				}
			}
		}
		// make it all into one:
		allDestroyers = new Vector<ActionTypeDestroyer>();
		for (int i = 0; i < prioritizedDestroyers.size(); i++) {
			allDestroyers.add(prioritizedDestroyers.elementAt(i));
		}
		for (int i = 0; i < nonPrioritizedDestroyers.size(); i++) {
			allDestroyers.add(nonPrioritizedDestroyers.elementAt(i));
		}
	}
}