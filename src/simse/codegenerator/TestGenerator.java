package simse.codegenerator;

import java.io.File;

import simse.codegenerator.enginegenerator.StartingNarrativeDialogGenerator;
import simse.modelbuilder.startstatebuilder.CreatedObjects;

public class TestGenerator {

	private static File directory = new File("C:\\Users\\jurgenkr\\Documents\\GitHub\\SeniorProject\\CodeGenTest");
	private static CreatedObjects cObjs = new CreatedObjects();
	
	public static void main(String[] args) {
		cObjs.setStartingNarrative("     Welcome to SimSE! Your task is to create Groceries@Home, a Web-based system that will allow people to place orders over the Internet for groceries to be delivered to their homes. The customer is the Grocery Home Delivery Service, a company who, up until now, has taken orders for groceries solely by telephone, but now wants to step into the information age. \n     Your budget is $280,000, and you have 1,350 clock ticks to complete the project. However, you should keep checking your project info to monitor this information -- the customer has the tendency to introduce new requirements, and will sometimes give you more time and/or money along with those new requirements. \n     Your final score will be out of 100 points, and it will be calculated based on how complete and error-free your code is, whether your code is integrated or not, and how well you stick to your budget and schedule. \n\nTwo notes:\n* Each hired employee is always paid every clock tick regardless of whether they're busy or not. So use them wisely!\n* If you want to use a tool that you have purchased in a task, you must specify that when you assign the task. If you have already started the task without the tool, you must stop and restart the task with the tool if you want it to be used.\n\nGood luck!");
		StartingNarrativeDialogGenerator sndg = new StartingNarrativeDialogGenerator(cObjs, directory);
		sndg.generate();
		
	}

}
