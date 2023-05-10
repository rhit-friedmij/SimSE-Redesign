/*
 * This class is responsible for generating all of the code for the engine
 * component of the simulation
 */

package simse.codegenerator.enginegenerator;

import simse.codegenerator.CodeGeneratorConstants;
import simse.codegenerator.CodeGeneratorUtils;
import simse.modelbuilder.ModelOptions;
import simse.modelbuilder.objectbuilder.Attribute;
import simse.modelbuilder.objectbuilder.AttributeTypes;
import simse.modelbuilder.objectbuilder.SimSEObjectTypeTypes;
import simse.modelbuilder.startstatebuilder.CreatedObjects;
import simse.modelbuilder.startstatebuilder.InstantiatedAttribute;
import simse.modelbuilder.startstatebuilder.SimSEObject;

import java.util.ArrayList;
import java.util.Vector;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.lang.model.element.Modifier;
import javax.swing.JOptionPane;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class EngineGenerator implements CodeGeneratorConstants {
	private File directory; // directory to generate into
	private File engineFile; // file to generate
	private CreatedObjects createdObjs; // start state objects
	private StartingNarrativeDialogGenerator sndg;
	
	private final int MAX_EMPLOYEES = 8;

	public EngineGenerator(ModelOptions options, CreatedObjects createdObjs) {
		directory = options.getCodeGenerationDestinationDirectory();
		this.createdObjs = createdObjs;
		sndg = new StartingNarrativeDialogGenerator(createdObjs, directory);
	}

	public int getEmployeeCount(Vector<SimSEObject> objects) {
		int count = 0;
		for(SimSEObject obj : objects) {
			if(obj.getSimSEObjectType().getType() == SimSEObjectTypeTypes.EMPLOYEE) count++;
		}
		return count - 1;
	}
	
	public Vector<SimSEObject> moveEmployeesToFront(Vector<SimSEObject> objects) {
	
		Vector<SimSEObject> newVector = new Vector<>();
		
		for(SimSEObject obj : objects) {
			if(obj.getSimSEObjectType().getType() != SimSEObjectTypeTypes.EMPLOYEE) {
				newVector.add(obj);
			}
		}
		for(SimSEObject obj : objects) {
			if(obj.getSimSEObjectType().getType() == SimSEObjectTypeTypes.EMPLOYEE) {
				newVector.insertElementAt(obj, 0);
			}
		}
		return newVector;
	}
	
	// causes the engine component to be generated
	public void generate() {
		// generate starting narrative dialog:
		sndg.generate();

		// generate Engine:
		ClassName timerTask = ClassName.get("java.util", "TimerTask");
		ClassName timeline = ClassName.get("javafx.animation", "Timeline");
		ClassName keyFrame = ClassName.get("javafx.animation", "KeyFrame");
		ClassName actionEvent = ClassName.get("javafx.event", "ActionEvent");
		ClassName eventHandler = ClassName.get("javafx.event", "EventHandler");
		ClassName duration = ClassName.get("javafx.util", "Duration");
		ClassName simseGui = ClassName.get("simse.gui", "SimSEGUI");
		ClassName logic = ClassName.get("simse.logic", "Logic");
		ClassName state = ClassName.get("simse.state", "State");
		ClassName employeeStateRepository = ClassName.get("simse.state", "EmployeeStateRepository");
		ClassName creatablePath = ClassName.get("simse.animation", "CreatablePath");
		ClassName pathData = ClassName.get("simse.animation", "PathData");
		ClassName simSECharacter = ClassName.get("simse.animation", "SimSECharacter");
		ClassName mapData = ClassName.get("simse.gui", "MapData");
		ClassName employeeStateResp = ClassName.get("simse.state", "EmployeeStateRepository");
		ClassName vector = ClassName.get("java.util", "Vector");
		
		TypeName actionHandler = ParameterizedTypeName.get(eventHandler, actionEvent);

		CodeBlock.Builder objsBuilder = CodeBlock.builder();
		objsBuilder.addStatement("$T<Employee> employees = $T.getInstance(state).getAll()", vector, employeeStateResp);
		objsBuilder.beginControlFlow("if(employees.size() == 0)");
		Vector<SimSEObject> objs = this.moveEmployeesToFront(createdObjs.getAllObjects());
		int employeeCount = this.getEmployeeCount(objs);
		for (int i = 0; i < objs.size(); i++) {
			StringBuffer strToWrite = new StringBuffer();
			SimSEObject tempObj = objs.elementAt(i);
			String objTypeName = CodeGeneratorUtils.getUpperCaseLeading(tempObj.getSimSEObjectType().getName());
			
			if(objs.get(i).getSimSEObjectType().getType() == SimSEObjectTypeTypes.EMPLOYEE) {
				if(i < MAX_EMPLOYEES) {
					strToWrite.append("this.generateNewPath(" + i + "); \n");
					strToWrite.append("$T a" + i + " = new " + objTypeName + "(");
				}
				else {
					strToWrite.append("$T a" + i + " = new " + objTypeName + "(\"Remote - \" + ");
				}
				
			}
			else {
				strToWrite.append("$T a" + i + " = new " + objTypeName + "(");
			}
			
			
			Vector<Attribute> atts = tempObj.getSimSEObjectType().getAllAttributes();
			
			// all attributes are instantiated
			if (atts.size() == tempObj.getAllAttributes().size()) { 
				boolean validObj = true;
				// go through all attributes:
				for (int j = 0; j < atts.size(); j++) {
					Attribute att = atts.elementAt(j);
					
					// get the corresponding instantiated attribute
					InstantiatedAttribute instAtt = tempObj.getAttribute(att.getName()); 
					// no corresponding instantiated attribute
					if (instAtt == null) { 
						validObj = false;
						break;
					}
					// attribute has a value
					if (instAtt.isInstantiated()) { 
						if (instAtt.getAttribute().getType() == AttributeTypes.STRING) {
							strToWrite.append("\"" + instAtt.getValue() + "\"");
						} else { 
							// boolean, int, or double
							strToWrite.append(instAtt.getValue().toString());
						}
						// not on last element
						if (j < (atts.size() - 1)) { 
							strToWrite.append(", ");
						}
					} else { 
						// attribute does not have a value -- invalidates entire object
						validObj = false;
						break;
					}
				}
				// if valid, finish writing:
				if (validObj) { 
					if(objs.get(i).getSimSEObjectType().getType() == SimSEObjectTypeTypes.EMPLOYEE) {
						if(i < MAX_EMPLOYEES) {
							strToWrite.append(", new SimSECharacter(characterPath, " + i + ", 50, 75)");
						}
						else {
							strToWrite.append(", new SimSECharacter(" + i + ", " + " 50, 75)");
						}
						
					}
					ClassName tempName = ClassName.get("simse.adts.objects", objTypeName);
					objsBuilder.addStatement(strToWrite + ")", tempName);
					if(objs.get(i).getSimSEObjectType().getType() == SimSEObjectTypeTypes.EMPLOYEE) {
						objsBuilder.addStatement(SimSEObjectTypeTypes.getText(tempObj.getSimSEObjectType().getType())
						+ "StateRepository.getInstance(state).get" + objTypeName + "StateRepository().add(a" + i + ")");
					}
					if(i == employeeCount) {
						objsBuilder.nextControlFlow("else ");
						objsBuilder.beginControlFlow("for(int i = 0; i < employees.size(); i++)");
						objsBuilder.addStatement("int characterNum = employees.get(i).getCharacterModel().getCharacterNum()");
						objsBuilder.addStatement("this.generateNewPath(characterNum)");
						objsBuilder.addStatement("employees.get(i).setCharacterModel(new $T(characterPath, characterNum, 50, 75))", simSECharacter);
						objsBuilder.endControlFlow();
						objsBuilder.endControlFlow();
					}
					else {
						objsBuilder.addStatement("state.get" + SimSEObjectTypeTypes.getText(tempObj.getSimSEObjectType().getType())
						+ "StateRepository().get" + objTypeName + "StateRepository().add(a" + i + ")");
					}
					
				}
			}
		}

		MethodSpec engineConstructor = MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
				.addParameter(logic, "l")
				.addParameter(state, "s")
				.addStatement("numSteps = 0")
				.addStatement("logic = l")
				.addStatement("state = s")
				.addCode("$L", "\n")
				.addStatement("timer = new $T(new $T($T.millis(100), this))", timeline, keyFrame, duration)
				.addStatement("timer.setCycleCount($T.INDEFINITE)", timeline)
				.addStatement("timer.setDelay($T.millis(100))", duration)
				.addStatement("timer.play()")
				.addCode("$L", "\n")
				.addCode(objsBuilder.build())
				.build();
		
		MethodSpec generateNewPath = MethodSpec.methodBuilder("generateNewPath")
				.addParameter(int.class, "characterNum")
				.addStatement("$T[][] pathDirections = $T.getStartingPath(characterNum)", double.class, pathData)
				.addStatement("this.characterPath = new $T(\r\n" + 
						"				$T.getStartingMapLocation(characterNum)[0] + 5, \r\n" + 
						"				$T.getStartingMapLocation(characterNum)[1],\r\n" + 
						"				pathDirections\r\n" +
						"				)", creatablePath, mapData, mapData)
				.build();
		
		MethodSpec giveGui = MethodSpec.methodBuilder("giveGUI")
				.addModifiers(Modifier.PUBLIC)
				.returns(void.class)
				.addParameter(simseGui, "g")
				.addStatement("gui = g")
				.addStatement("gui.show()")
				.addStatement("new StartingNarrativeDialog()")
				.build();
		
		MethodSpec running = MethodSpec.methodBuilder("isRunning")
				.addModifiers(Modifier.PUBLIC)
				.returns(boolean.class)
				.addStatement("return numSteps > 0")
				.build();
		
		MethodSpec events = MethodSpec.methodBuilder("setStopAtEvents")
				.addModifiers(Modifier.PUBLIC)
				.returns(void.class)
				.addParameter(boolean.class, "t")
				.addStatement("stopClock = false")
				.addStatement("stopAtEvents = t")
				.build();
		
		MethodSpec steps = MethodSpec.methodBuilder("setSteps")
				.addModifiers(Modifier.PUBLIC)
				.returns(void.class)
				.addParameter(int.class, "ns")
				.addStatement("restartTimer()")
				.addStatement("numSteps += ns")
				.build();
		
		MethodSpec stop = MethodSpec.methodBuilder("stop")
				.addModifiers(Modifier.PUBLIC)
				.returns(void.class)
				.addStatement("numSteps = 0")
				.addStatement("timer.stop()")
				.build();
		
		MethodSpec stopTimer = MethodSpec.methodBuilder("stopTimer")
				.addModifiers(Modifier.PUBLIC)
				.returns(void.class)
				.addStatement("timer.stop()")
				.build();
		
		MethodSpec restartTimer = MethodSpec.methodBuilder("restartTimer")
				.addModifiers(Modifier.PUBLIC)
				.returns(void.class)
				.addStatement("timer.stop()")
				.addStatement("timer.play()")
				.build();
		
		MethodSpec stopClock = MethodSpec.methodBuilder("stopClock")
				.addModifiers(Modifier.PUBLIC)
				.returns(boolean.class)
				.addStatement("return stopClock")
				.build();
		
		MethodSpec getTimer = MethodSpec.methodBuilder("getTimer")
				.addModifiers(Modifier.PUBLIC)
				.returns(timeline)
				.addStatement("return timer")
				.build();
		
		MethodSpec run = MethodSpec.methodBuilder("run")
				.addModifiers(Modifier.PUBLIC)
				.returns(void.class)
				.addAnnotation(Override.class)
				.beginControlFlow("if ($N())", running)
				.addStatement("gui.getTabPanel().getClockPanel().setAdvClockImage()")
				.beginControlFlow("if (state.getClock().isStopped())")
				.addStatement("numSteps = 0")
				.nextControlFlow("else")
				.addStatement("gui.getAttributePanel().setGUIChanged()")
				.addStatement("state.getLogger().update()")
				.addStatement("logic.update(gui)")
				.addStatement("gui.update()")
				.addStatement("numSteps--")
				.beginControlFlow("if (stopAtEvents && gui.getWorld().overheadTextDisplayed())")
				.addStatement("stopClock = true")
				.addStatement("numSteps = 0")
				.endControlFlow()
				.endControlFlow()
				.nextControlFlow("else")
				.addStatement("gui.getTabPanel().getClockPanel().resetAdvClockImage()")
				.endControlFlow()
				.build();
		
		MethodSpec handle = MethodSpec.methodBuilder("handle")
				.addModifiers(Modifier.PUBLIC)
				.returns(void.class)
				.addParameter(actionEvent, "ae")
				.addAnnotation(Override.class)
				.addStatement("this.$N()", run)
				.build();
		
		TypeSpec engine = TypeSpec.classBuilder("Engine")
				.superclass(timerTask)
				.addModifiers(Modifier.PUBLIC)
				.addSuperinterface(actionHandler)
				.addField(logic, "logic", Modifier.PRIVATE)
				.addField(state, "state", Modifier.PRIVATE)
				.addField(simseGui, "gui", Modifier.PRIVATE)
				.addField(int.class, "numSteps", Modifier.PRIVATE)
				.addField(boolean.class, "stopClock", Modifier.PRIVATE)
				.addField(boolean.class, "stopAtEvents", Modifier.PRIVATE)
				.addField(timeline, "timer", Modifier.PRIVATE)
				.addField(creatablePath, "characterPath")
				.addMethod(engineConstructor)
				.addMethod(generateNewPath)
				.addMethod(giveGui)
				.addMethod(running)
				.addMethod(events)
				.addMethod(steps)
				.addMethod(stop)
				.addMethod(stopTimer)
				.addMethod(restartTimer)
				.addMethod(stopClock)
				.addMethod(getTimer)
				.addMethod(handle)
				.addMethod(run)
				.build();

		JavaFile javaFile = JavaFile.builder("", engine)
				.addFileComment("File generated by: simse.codegenerator.enginegenerator.EngineGenerator")
				.build();
		
		
		try {
			engineFile = new File(directory, ("simse\\engine\\Engine.java"));
			if (engineFile.exists()) {
				engineFile.delete(); // delete old version of file
			}
			FileWriter writer = new FileWriter(engineFile);
	  	  	String toAppend = "/* File generated by: simse.codegenerator.enginegenerator.EngineGenerator */\n"
	  	  	  		+ "package simse.engine;\n"
	  	  	  		+ "\n"
	  	  	  		+ "import simse.adts.actions.*;\n"
	  	  	  		+ "import simse.adts.objects.*;\n"
	  	  	  		+ "import java.util.Vector;\n"
	  	  			+ "import simse.animation.SimSECharacter;";
	  	  	writer.write(toAppend + javaFile.toString());
			writer.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, ("Error writing file " + engineFile.getPath() + ": " + e.toString()),
					"File IO Error", JOptionPane.WARNING_MESSAGE);
		}
	}
}