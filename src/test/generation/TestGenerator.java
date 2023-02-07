package test.generation;

import java.io.File;
import java.util.Vector;

import simse.codegenerator.enginegenerator.EngineGenerator;
import simse.codegenerator.enginegenerator.StartingNarrativeDialogGenerator;
import simse.codegenerator.logicgenerator.LogicGenerator;
import simse.modelbuilder.ModelOptions;
import simse.modelbuilder.actionbuilder.ActionType;
import simse.modelbuilder.actionbuilder.DefinedActionTypes;
import simse.modelbuilder.objectbuilder.Attribute;
import simse.modelbuilder.objectbuilder.DefinedObjectTypes;
import simse.modelbuilder.objectbuilder.NonNumericalAttribute;
import simse.modelbuilder.objectbuilder.SimSEObjectType;
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
		logicTest();
	}
	
	public static void setUp() {
		cObjs.setStartingNarrative("     Welcome to SimSE! Your task is to create Groceries@Home, a Web-based system that will allow people to place orders over the Internet for groceries to be delivered to their homes. The customer is the Grocery Home Delivery Service, a company who, up until now, has taken orders for groceries solely by telephone, but now wants to step into the information age. \n     Your budget is $280,000, and you have 1,350 clock ticks to complete the project. However, you should keep checking your project info to monitor this information -- the customer has the tendency to introduce new requirements, and will sometimes give you more time and/or money along with those new requirements. \n     Your final score will be out of 100 points, and it will be calculated based on how complete and error-free your code is, whether your code is integrated or not, and how well you stick to your budget and schedule. \n\nTwo notes:\n* Each hired employee is always paid every clock tick regardless of whether they're busy or not. So use them wisely!\n* If you want to use a tool that you have purchased in a task, you must specify that when you assign the task. If you have already started the task without the tool, you must stop and restart the task with the tool if you want it to be used.\n\nGood luck!");
		
		options.setCodeGenerationDestinationDirectory(directory);
		NonNumericalAttribute name = new NonNumericalAttribute("name", 1, true, true, true);
		InstantiatedAttribute ia1 = new InstantiatedAttribute(name, "Gomez");
		Vector<InstantiatedAttribute> v1 = new Vector<>();
		v1.add(ia1);
		SimSEObjectType ot1 = new SimSEObjectType(1, "Obsessed");
		ot1.addAttribute(name);
		cObjs.addObject(new SimSEObject(v1, ot1));
		objTypes.addObjectType(ot1);
		
		InstantiatedAttribute ia2 = new InstantiatedAttribute(name, "Wednesday");
		Vector<InstantiatedAttribute> v2 = new Vector<>();
		v2.add(ia2);
		SimSEObjectType ot2 = new SimSEObjectType(1, "Unobsessed");
		ot2.addAttribute(name);
		cObjs.addObject(new SimSEObject(v2, ot2));
		objTypes.addObjectType(ot2);
		
		ActionType at1 = new ActionType("Cultivate");
		actTypes.addActionType(at1);
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
}
