package test.generation;

import java.io.File;
import java.util.Vector;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import simse.codegenerator.CodeGeneratorUtils;
import simse.codegenerator.enginegenerator.EngineGenerator;
import simse.codegenerator.enginegenerator.StartingNarrativeDialogGenerator;
import simse.codegenerator.logicgenerator.DestroyerCheckerGenerator;
import simse.codegenerator.logicgenerator.LogicGenerator;
import simse.codegenerator.logicgenerator.MenuInputManagerGenerator;
import simse.codegenerator.logicgenerator.MiscUpdaterGenerator;
import simse.codegenerator.logicgenerator.RuleExecutorGenerator;
import simse.codegenerator.logicgenerator.TriggerCheckerGenerator;
import simse.modelbuilder.ModelOptions;
import simse.modelbuilder.actionbuilder.ActionType;
import simse.modelbuilder.actionbuilder.ActionTypeDestroyer;
import simse.modelbuilder.actionbuilder.ActionTypeParticipant;
import simse.modelbuilder.actionbuilder.ActionTypeParticipantAttributeConstraint;
import simse.modelbuilder.actionbuilder.ActionTypeParticipantConstraint;
import simse.modelbuilder.actionbuilder.ActionTypeParticipantDestroyer;
import simse.modelbuilder.actionbuilder.AutonomousActionTypeTrigger;
import simse.modelbuilder.actionbuilder.DefinedActionTypes;
import simse.modelbuilder.actionbuilder.RandomActionTypeDestroyer;
import simse.modelbuilder.actionbuilder.RandomActionTypeTrigger;
import simse.modelbuilder.actionbuilder.TimedActionTypeDestroyer;
import simse.modelbuilder.actionbuilder.UserActionTypeDestroyer;
import simse.modelbuilder.actionbuilder.UserActionTypeTrigger;
import simse.modelbuilder.objectbuilder.AttributeTypes;
import simse.modelbuilder.objectbuilder.DefinedObjectTypes;
import simse.modelbuilder.objectbuilder.NonNumericalAttribute;
import simse.modelbuilder.objectbuilder.SimSEObjectType;
import simse.modelbuilder.rulebuilder.DestroyObjectsRule;
import simse.modelbuilder.rulebuilder.EffectRule;
import simse.modelbuilder.rulebuilder.ParticipantRuleEffect;
import simse.modelbuilder.rulebuilder.Rule;
import simse.modelbuilder.startstatebuilder.CreatedObjects;
import simse.modelbuilder.startstatebuilder.InstantiatedAttribute;
import simse.modelbuilder.startstatebuilder.SimSEObject;

public class TestGenerator {

	private static File directory = new File("C:\\Users\\jurgenkr\\Documents\\GitHub\\SeniorProject\\CodeGenTest");
	private static CreatedObjects cObjs = new CreatedObjects();
	private static ModelOptions options = new ModelOptions();
	private static DefinedObjectTypes objTypes = new DefinedObjectTypes();
	private static DefinedActionTypes actTypes = new DefinedActionTypes();
	
	public static void main(String[] args) {
		setUp();
//		startingNarrativeDialogTest();
//		engineTest();
//		logicTest();
//		miscUpdaterTest();
//		destroyerCheckerTest();
//		triggerCheckerTest();
//		menuInputManagerTest();
		ruleExecutorTest();
//		testConditionalFlows();
	}
	
	public static void setUp() {
		cObjs.setStartingNarrative("     Welcome to SimSE! Your task is to create Groceries@Home, a Web-based system that will allow people to place orders over the Internet for groceries to be delivered to their homes. The customer is the Grocery Home Delivery Service, a company who, up until now, has taken orders for groceries solely by telephone, but now wants to step into the information age. \n     Your budget is $280,000, and you have 1,350 clock ticks to complete the project. However, you should keep checking your project info to monitor this information -- the customer has the tendency to introduce new requirements, and will sometimes give you more time and/or money along with those new requirements. \n     Your final score will be out of 100 points, and it will be calculated based on how complete and error-free your code is, whether your code is integrated or not, and how well you stick to your budget and schedule. \n\nTwo notes:\n* Each hired employee is always paid every clock tick regardless of whether they're busy or not. So use them wisely!\n* If you want to use a tool that you have purchased in a task, you must specify that when you assign the task. If you have already started the task without the tool, you must stop and restart the task with the tool if you want it to be used.\n\nGood luck!");
		
		options.setCodeGenerationDestinationDirectory(directory);
		options.setEveryoneStopOption(true);
		NonNumericalAttribute name = new NonNumericalAttribute("name", 1, true, true, true);
		InstantiatedAttribute ia1 = new InstantiatedAttribute(name, "Gomez");
		NonNumericalAttribute dancing = new NonNumericalAttribute("dancing", 2, true, true, true);
		InstantiatedAttribute ia2 = new InstantiatedAttribute(dancing, false);
		Vector<InstantiatedAttribute> v1 = new Vector<>();
		v1.add(ia1);
		v1.add(ia2);
		SimSEObjectType ot1 = new SimSEObjectType(1, "Obsessed");
		ot1.addAttribute(name);
		ot1.addAttribute(dancing);
		cObjs.addObject(new SimSEObject(v1, ot1));
		objTypes.addObjectType(ot1);
		
		InstantiatedAttribute ia3 = new InstantiatedAttribute(name, "Wednesday");
		Vector<InstantiatedAttribute> v2 = new Vector<>();
		v2.add(ia3);
		SimSEObjectType ot2 = new SimSEObjectType(1, "Unobsessed");
		ot2.addAttribute(name);
		cObjs.addObject(new SimSEObject(v2, ot2));
		objTypes.addObjectType(ot2);
		
		ActionType at1 = new ActionType("Cultivate");
		ActionType at2 = new ActionType("Research");
		ActionType at3 = new ActionType("Dance");
		ActionType at4 = new ActionType("Rain");
		at1.addDestroyer(new UserActionTypeDestroyer("CultivateDest", at1, "Stop Cultivating"));
		at3.addDestroyer(new TimedActionTypeDestroyer("AutoDest", at3));
		at4.addDestroyer(new RandomActionTypeDestroyer("RandomDest", at4));
		at1.addTrigger(new UserActionTypeTrigger("CultivateEffect", at1, "Start Cultivating", true));
		at3.addTrigger(new AutonomousActionTypeTrigger("TrigA", at3));
		at4.addTrigger(new RandomActionTypeTrigger("RandomTrig", at4));
		
		EffectRule r1 = new EffectRule("CultivateEffectA", at1);
		r1.setTiming(1);
		r1.setVisibilityInExplanatoryTool(true);
		at1.addRule(r1);
		
		DestroyObjectsRule r2 = new DestroyObjectsRule("DestroyCultivate", at1);
		r1.setTiming(2);
		at1.addRule(r2);
		
		actTypes.addActionType(at1);
		actTypes.addActionType(at2);
		actTypes.addActionType(at3);
		actTypes.addActionType(at4);
	}
	
	public static void startingNarrativeDialogTest() {
		StartingNarrativeDialogGenerator sndg = new StartingNarrativeDialogGenerator(cObjs, directory);
		sndg.generate();
	}
	
	public static void engineTest() {
		EngineGenerator eGen = new EngineGenerator(options, cObjs);
		eGen.generate();
	}
	
	public static void logicTest() {
		LogicGenerator lGen = new LogicGenerator(options, objTypes, actTypes);
		lGen.generate();
	}
	
	public static void miscUpdaterTest() {
		MiscUpdaterGenerator muGen = new MiscUpdaterGenerator(directory, actTypes);
		muGen.generate();
	}
	
	public static void destroyerCheckerTest() {
		DestroyerCheckerGenerator dGen = new DestroyerCheckerGenerator(actTypes, directory);
		dGen.generate();
	}
	
	public static void triggerCheckerTest() {
		TriggerCheckerGenerator tGen = new TriggerCheckerGenerator(actTypes, directory);
		tGen.generate();
	}
	
	public static void menuInputManagerTest() {
		MenuInputManagerGenerator mimGen = new MenuInputManagerGenerator(options, actTypes, objTypes, directory);
		mimGen.generate();
	}
	
	public static void ruleExecutorTest() {
		RuleExecutorGenerator mimGen = new RuleExecutorGenerator(actTypes, directory);
		mimGen.generate();
	}
	
	public static void testConditionalFlows() {
		CodeBlock.Builder conditions = CodeBlock.builder();
		for (int i = 0; i < 5; i++) {
			if (i == 0) { 
				// on first element
				conditions.beginControlFlow("if (i == 0)", i);
			} else {
				conditions.nextControlFlow("else if (i > 0)", i);
			}
			
			conditions.addStatement("On loop $L", i);
			
			if (i == 4) {
				conditions.endControlFlow();
			}
		}
		System.out.println(conditions.build().toString());
	}
}
