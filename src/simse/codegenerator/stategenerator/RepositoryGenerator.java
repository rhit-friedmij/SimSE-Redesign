/*
 * This class is responsible for generating all of the code for the repositories
 * for the ADTs derived from the SimSEObjectTypes in an .sso file
 */

package simse.codegenerator.stategenerator;

import simse.codegenerator.CodeGeneratorConstants;
import simse.codegenerator.CodeGeneratorUtils;

import simse.modelbuilder.ModelOptions;
import simse.modelbuilder.actionbuilder.ActionType;
import simse.modelbuilder.actionbuilder.ActionTypeParticipant;
import simse.modelbuilder.actionbuilder.DefinedActionTypes;
import simse.modelbuilder.objectbuilder.Attribute;
import simse.modelbuilder.objectbuilder.AttributeTypes;
import simse.modelbuilder.objectbuilder.DefinedObjectTypes;
import simse.modelbuilder.objectbuilder.SimSEObjectType;
import simse.modelbuilder.objectbuilder.SimSEObjectTypeTypes;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import javax.lang.model.element.Modifier;
import javax.swing.JOptionPane;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class RepositoryGenerator implements CodeGeneratorConstants {
  private ModelOptions options;
  private DefinedObjectTypes objTypes; // holds all of the defined object types
  private DefinedActionTypes actTypes; // holds all of the defined action types

  public RepositoryGenerator(ModelOptions options, DefinedObjectTypes objTypes, 
      DefinedActionTypes actTypes) {
    this.options = options;
    this.objTypes = objTypes;
    this.actTypes = actTypes;
  }

  public void generate() {
	  ClassName actionStateRepoClass = ClassName.get("simse.state", "ActionStateRepository");
	  ClassName artifactStateRepoClass = ClassName.get("simse.state", "ArtifactStateRepository");
	  ClassName customerStateRepoClass = ClassName.get("simse.state", "CustomerStateRepository");
	  ClassName employeeStateRepoClass = ClassName.get("simse.state", "EmployeeStateRepository");
	  ClassName projectStateRepoClass = ClassName.get("simse.state", "ProjectStateRepository");
	  ClassName toolStateRepoClass = ClassName.get("simse.state", "ToolStateRepository");
	  ClassName actionClass = ClassName.get("simse.adts.actions", "Action");
	  ClassName ssObjectClass = ClassName.get("simse.adts.objects", "SSObject");
	  ClassName vector = ClassName.get("java.util", "Vector");
	  TypeName actionVector = ParameterizedTypeName.get(vector, actionClass);
	  TypeName ssObjectVector = ParameterizedTypeName.get(vector, ssObjectClass);
    Vector<SimSEObjectType> objs = objTypes.getAllObjectTypes();
    // go through each object and generate a repository for it:
    for (int i = 0; i < objs.size(); i++) {
      generateObjectRepository(objs.elementAt(i));
    }

    // generate meta-object type repositories:
    generateMetaObjectTypeRepository(SimSEObjectTypeTypes
        .getText(SimSEObjectTypeTypes.EMPLOYEE));
    generateMetaObjectTypeRepository(SimSEObjectTypeTypes
        .getText(SimSEObjectTypeTypes.ARTIFACT));
    generateMetaObjectTypeRepository(SimSEObjectTypeTypes
        .getText(SimSEObjectTypeTypes.TOOL));
    generateMetaObjectTypeRepository(SimSEObjectTypeTypes
        .getText(SimSEObjectTypeTypes.CUSTOMER));
    generateMetaObjectTypeRepository(SimSEObjectTypeTypes
        .getText(SimSEObjectTypeTypes.PROJECT));

    // generate action repositories for each action type:
    Vector<ActionType> acts = actTypes.getAllActionTypes();
    for (int i = 0; i < acts.size(); i++) {
      generateActionRepository(acts.elementAt(i));
    }

    // generate ActionStateRepository:
    File asrFile = new File(options.getCodeGenerationDestinationDirectory(),
        ("simse\\state\\ActionStateRepository.java"));
    if (asrFile.exists()) {
      asrFile.delete(); // delete old version of file
    }
    
    MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
    		.addModifiers(Modifier.PUBLIC);
    
    for (int i = 0; i < acts.size(); i++) {
        ActionType tempAct = acts.elementAt(i);
        ClassName tempClass = ClassName.get("simse.state", CodeGeneratorUtils.
        		getUpperCaseLeading(tempAct.getName()) + "ActionStateRepository");
        constructorBuilder.addStatement("$L = new $T()", tempAct.getName().
        		substring(0, 1).toLowerCase() + i, tempClass);
      }
    
    MethodSpec constructor = constructorBuilder.build();
    
    MethodSpec.Builder cloneBuilder = MethodSpec.methodBuilder("clone")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(Object.class)
    		.beginControlFlow("try")
    		.addStatement("$T cl = ($T) (super.clone())", actionStateRepoClass, actionStateRepoClass);
    
    for (int i = 0; i < acts.size(); i++) {
        ActionType tempAct = acts.elementAt(i);
        ClassName tempClass = ClassName.get("simse.state", CodeGeneratorUtils.
        		getUpperCaseLeading(tempAct.getName()) + "ActionStateRepository");
        String name = tempAct.getName().substring(0, 1).toLowerCase() + i;
        cloneBuilder.addStatement("cl.$L = ($T) ($L.clone())", name, tempClass, name);
      }
    
    MethodSpec clone = cloneBuilder.addStatement("return cl")
    		.nextControlFlow("catch ($T c)", CloneNotSupportedException.class)
    		.addStatement("System.out.println(c.getMessage())")
    		.endControlFlow()
    		.addStatement("return null")
    		.build();
    
    MethodSpec.Builder getAllActionsBuilder1 = MethodSpec.methodBuilder("getAllActions")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(actionVector)
    		.addStatement("$T all = new $T()", actionVector, actionVector);
    
    for (int i = 0; i < acts.size(); i++) {
        ActionType tempAct = acts.elementAt(i);
        getAllActionsBuilder1.addStatement("all.addAll($L.getAllActions())",
        		tempAct.getName().substring(0, 1).toLowerCase() + i);
      }
    
    MethodSpec getAllActions1 = getAllActionsBuilder1
    		.addStatement("return all")
    		.build();
    
    MethodSpec getAllActions2 = MethodSpec.methodBuilder("getAllActions")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(actionVector)
    		.addParameter(ssObjectClass, "a")
    		.addStatement("$T all = new $T()", actionVector, actionVector)
    		.addStatement("$T actions = getAllActions()", actionVector)
    		.beginControlFlow("for (int i = 0; i < actions.size(); i++) ")
    		.addStatement("$T b = actions.elementAt(i)", actionClass)
    		.addStatement("$T parts = b.getAllParticipants()", ssObjectVector)
    		.beginControlFlow("for (int j = 0; j < parts.size(); j++) ")
    		.beginControlFlow("if (parts.elementAt(j).equals(a)) ")
    		.addStatement("all.add(b)")
    		.addStatement("break")
    		.endControlFlow()
    		.endControlFlow()
    		.endControlFlow()
    		.addStatement("return all")
    		.build();
    
    MethodSpec getAllActiveActions = MethodSpec.methodBuilder("getAllActiveActions")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(actionVector)
    		.addParameter(ssObjectClass, "a")
    		.addStatement("$T all = new $T()", actionVector, actionVector)
    		.addStatement("$T actions = getAllActions()", actionVector)
    		.beginControlFlow("for (int i = 0; i < actions.size(); i++) ")
    		.addStatement("$T b = actions.elementAt(i)", actionClass)
    		.addStatement("$T parts = b.getAllActiveParticipants()", ssObjectVector)
    		.beginControlFlow("for (int j = 0; j < parts.size(); j++) ")
    		.beginControlFlow("if (parts.elementAt(j).equals(a)) ")
    		.addStatement("all.add(b)")
    		.addStatement("break")
    		.endControlFlow()
    		.endControlFlow()
    		.endControlFlow()
    		.addStatement("return all")
    		.build();
    
    MethodSpec getAllInactiveActions = MethodSpec.methodBuilder("getAllInactiveActions")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(actionVector)
    		.addParameter(ssObjectClass, "a")
    		.addStatement("$T all = new $T()", actionVector, actionVector)
    		.addStatement("$T actions = getAllActions()", actionVector)
    		.beginControlFlow("for (int i = 0; i < actions.size(); i++) ")
    		.addStatement("$T b = actions.elementAt(i)", actionClass)
    		.addStatement("$T parts = b.getAllInactiveParticipants()", ssObjectVector)
    		.beginControlFlow("for (int j = 0; j < parts.size(); j++) ")
    		.beginControlFlow("if (parts.elementAt(j).equals(a)) ")
    		.addStatement("all.add(b)")
    		.addStatement("break")
    		.endControlFlow()
    		.endControlFlow()
    		.endControlFlow()
    		.addStatement("return all")
    		.build();
    
    MethodSpec removeFromAllActions = MethodSpec.methodBuilder("removeFromAllActions")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(void.class)
    		.addParameter(ssObjectClass, "a")
    		.addCode(removeFromActions(acts))
    		.build();
    
    MethodSpec getActionWithId = MethodSpec.methodBuilder("getActionWithId")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(actionClass)
    		.addParameter(int.class, "id")
    		.addCode(getActionWithId(acts))
    		.addStatement("return null")
    		.build();
    
    MethodSpec.Builder refetchParticipantsBuilder = MethodSpec.methodBuilder("refetchParticipants")
    		.addComment("Replaces all the participants in this action with their equivalent")
			.addComment("objects in the current state. Calling this function solves the problem")
			.addComment("that happens when you clone actions -- their hashtables point to")
			.addComment("participant objects that were part of the previous, non-cloned state.")
			.addComment("Hence, this function should be called after this object is cloned.")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(void.class)
    		.addParameter(artifactStateRepoClass, "artifactRep")
    		.addParameter(customerStateRepoClass, "customerRep")
    		.addParameter(employeeStateRepoClass, "employeeRep")
    		.addParameter(projectStateRepoClass, "projectRep")
    		.addParameter(toolStateRepoClass, "toolRep");
    
    for (int i = 0; i < acts.size(); i++) {
        ActionType tempAct = acts.elementAt(i);
        refetchParticipantsBuilder.addStatement("$L.refetchParticipants(artifactRep,"
        		+ " customerRep, employeeRep, projectRep, toolRep)", 
        		tempAct.getName().substring(0, 1).toLowerCase() + i);
      }
    
    MethodSpec refetchParticipants = refetchParticipantsBuilder.build();
    
    TypeSpec.Builder actionRepoBuilder = TypeSpec.classBuilder("ActionStateRepository")
    		.addModifiers(Modifier.PUBLIC)
    		.addSuperinterface(Cloneable.class);
    
    for (int i = 0; i < acts.size(); i++) {
        ActionType tempAct = acts.elementAt(i);
        ClassName tempClass = ClassName.get("simse.state", CodeGeneratorUtils.
        		getUpperCaseLeading(tempAct.getName()) + "ActionStateRepository");
        FieldSpec tempField = FieldSpec.builder(tempClass, tempAct.getName().
        		substring(0, 1).toLowerCase() + i)
        		.build();
        actionRepoBuilder.addField(tempField);
    }
    
    actionRepoBuilder
    		.addMethod(constructor)
    		.addMethod(clone)
    		.addMethod(getAllActions1)
    		.addMethod(getAllActions2)
    		.addMethod(getAllActiveActions)
    		.addMethod(getAllInactiveActions)
    		.addMethod(removeFromAllActions);
    
    for (int i = 0; i < acts.size(); i++) {
        ActionType tempAct = acts.elementAt(i);
        ClassName tempClass = ClassName.get("simse.state", CodeGeneratorUtils.
        		getUpperCaseLeading(tempAct.getName()) + "ActionStateRepository");
        MethodSpec tempMethod = MethodSpec.methodBuilder("get" + CodeGeneratorUtils.
        		getUpperCaseLeading(tempAct.getName()) + "ActionStateRepository")
        		.addModifiers(Modifier.PUBLIC)
        		.returns(tempClass)
        		.addStatement("return $L", tempAct.getName().substring(0, 1).toLowerCase() + i)
        		.build();
        actionRepoBuilder.addMethod(tempMethod);
      }
    
    TypeSpec actionRepo = actionRepoBuilder
    		.addMethod(getActionWithId)
    		.addMethod(refetchParticipants)
    		.build();
    
	JavaFile javaFile = JavaFile.builder("", actionRepo)
		    .build();
    
    try {
    	FileWriter writer = new FileWriter(asrFile);
  	  String toAppend = "/* File generated by: simse.codegenerator.stategenerator.RepositoryGenerator */\n"
  	  		+ "package simse.state;\n"
  	  		+ "\n"
  	  		+ "import simse.adts.actions.*;\n"
  	  		+ "import simse.adts.objects.*;\n";
  	  
        writer.write(toAppend + javaFile.toString());
    	writer.close();
    } catch (IOException e) {
        JOptionPane.showMessageDialog(null, ("Error writing file "
            + asrFile.getPath() + ": " + e.toString()), "File IO Error",
            JOptionPane.WARNING_MESSAGE);
    }
  }

  private void generateObjectRepository(SimSEObjectType objType) {
    String uCaseName = CodeGeneratorUtils.getUpperCaseLeading(objType.getName());
    String lCaseName = objType.getName().toLowerCase();
    String uCaseNameFunc = CodeGeneratorUtils.getUpperCaseLeading(objType.getKey().getName());
    ClassName objClass = ClassName.get("simse.adts.objects", uCaseName);
    ClassName vector = ClassName.get("java.util", "Vector");
    ClassName objRepo = ClassName.get("simse.state", uCaseName + "StateRepository");
    TypeName objVector = ParameterizedTypeName.get(vector, objClass);
    File repFile = new File(options.getCodeGenerationDestinationDirectory(), 
        ("simse\\state\\" + uCaseName + "StateRepository.java"));
    if (repFile.exists()) {
      repFile.delete(); // delete old version of file
    }
    
    MethodSpec constructor = MethodSpec.constructorBuilder()
    		.addModifiers(Modifier.PUBLIC)
    		.addStatement("$Ls = new $T()", lCaseName, objVector)
    		.build();
    
    MethodSpec clone = MethodSpec.methodBuilder("clone")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(objRepo)
    		.beginControlFlow("try")
    		.addStatement("$T cl = ($T) super.clone()", objRepo, objRepo)
    		.addStatement("$T cloned$Ls = new $T()", objVector, lCaseName, objVector)
    		.beginControlFlow("for (int i = 0; i < $Ls.size(); i++)", lCaseName)
    		.addStatement("cloned$Ls.addElement(($T) ($Ls.elementAt(i).clone()))",
    				lCaseName, objClass, lCaseName)
    		.endControlFlow()
    		.addStatement("cl.$Ls = cloned$Ls", lCaseName, lCaseName)
    		.addStatement("return cl")
    		.nextControlFlow("catch ($T c)", CloneNotSupportedException.class)
    		.addStatement("System.out.println(c.getMessage())")
    		.endControlFlow()
    		.addStatement("return null")
    		.build();
    
    MethodSpec.Builder addBuilder = MethodSpec.methodBuilder("add")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(void.class)
    		.addParameter(objClass, "a")
    		.addStatement("$T add = true", boolean.class)
    		.beginControlFlow("for (int i = 0; i < $Ls.size(); i++)", lCaseName)
    		.addStatement("$T $L = $Ls.elementAt(i)", objClass, lCaseName, lCaseName);
    
    if (objType.getKey().getType() == AttributeTypes.STRING) { // string key attribute
    	addBuilder.beginControlFlow("if ($L.get$L().equals(a.get$L()))", lCaseName,
    			uCaseNameFunc, uCaseNameFunc);
	} else { // boolean or numerical key attribute
		addBuilder.beginControlFlow("if ($L.get$L() == a.get$L())", lCaseName,
    			uCaseNameFunc, uCaseNameFunc);
	}
    
    MethodSpec add = addBuilder
    		.addStatement("add = false")
    		.addStatement("break")
    		.endControlFlow()
    		.endControlFlow()
    		.beginControlFlow("if (add)")
    		.addStatement("$Ls.add(a)", lCaseName)
    		.endControlFlow()
    		.build();
    
    Attribute keyAtt = objType.getKey();
    Class attType;
    if (keyAtt.getType() == AttributeTypes.INTEGER) {
      attType = int.class;
    } else if (keyAtt.getType() == AttributeTypes.DOUBLE) {
      attType = double.class;
    } else if (keyAtt.getType() == AttributeTypes.BOOLEAN) {
      attType = boolean.class;
    } else { //(keyAtt.getType() == AttributeTypes.STRING)
      attType = String.class;
    }
    
    
    MethodSpec.Builder getBuilder = MethodSpec.methodBuilder("get")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(objClass)
    		.addParameter(attType, keyAtt.getName().toLowerCase())
    		.beginControlFlow("for (int i = 0; i <  $Ls.size(); i++)", lCaseName);
    
    if (keyAtt.getType() == AttributeTypes.STRING) { // string key attribute
    	getBuilder.beginControlFlow("if($Ls.elementAt(i).get$L().equals($L))", lCaseName, 
        				CodeGeneratorUtils.getUpperCaseLeading(keyAtt.getName()),
        				keyAtt.getName().toLowerCase());
      } else { // int, double, or boolean key attribute
        getBuilder.beginControlFlow("if($Ls.elementAt(i).get$L() == $L)", lCaseName, 
				CodeGeneratorUtils.getUpperCaseLeading(keyAtt.getName()),
				keyAtt.getName().toLowerCase());
      }
    
    MethodSpec get = getBuilder
    		.addStatement("return $Ls.elementAt(i)", lCaseName)
    		.endControlFlow()
    		.endControlFlow()
    		.addStatement("return null")
    		.build();
    
    MethodSpec getAll = MethodSpec.methodBuilder("getAll")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(objVector)
    		.addStatement("return $Ls", lCaseName)
    		.build();
    
    MethodSpec remove = MethodSpec.methodBuilder("remove")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(boolean.class)
    		.addParameter(objClass, "a")
    		.addStatement("return $Ls.remove(a)", lCaseName)
    		.build();
    		
    TypeSpec repStateRepo = TypeSpec.classBuilder(uCaseName + "StateRepository")
    		.addModifiers(Modifier.PUBLIC)
    		.addSuperinterface(Cloneable.class)
    		.addField(objVector, lCaseName + "s")
    		.addMethod(constructor)
    		.addMethod(clone)
    		.addMethod(add)
    		.addMethod(get)
    		.addMethod(getAll)
    		.addMethod(remove)
    		.build();
    
    JavaFile javaFile = JavaFile.builder("simse.state", repStateRepo)
    		.addFileComment("/* File generated by: simse.codegenerator.stategenerator.RepositoryGenerator */")
		    .build();
    
    try {
    	FileWriter writer = new FileWriter(repFile);
    	
    	javaFile.writeTo(writer);
    	writer.close();
    } catch (IOException e) {
        JOptionPane.showMessageDialog(null, ("Error writing file "
            + repFile.getPath() + ": " + e.toString()), "File IO Error",
            JOptionPane.WARNING_MESSAGE);
      }
  }

  private void generateMetaObjectTypeRepository(String typeName) {
	  ClassName typeStateRepo = ClassName.get("simse.state", typeName + "StateRepository");
	  ClassName type = ClassName.get("simse.adts.objects", typeName);
	  ClassName vector = ClassName.get("java.util", "Vector");
	  TypeName typeVector = ParameterizedTypeName.get(vector, type);
    File repFile = new File(options.getCodeGenerationDestinationDirectory(),
        ("simse\\state\\" + typeName + "StateRepository.java"));
    if (repFile.exists()) {
      repFile.delete(); // delete old version of file
    }
    
    Vector<SimSEObjectType> objs = 
          	objTypes.getAllObjectTypesOfType(
          			SimSEObjectTypeTypes.getIntRepresentation(typeName));
    
    MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder();
    
    ClassName employeeStateRepoClass = ClassName.get("simse.state", "EmployeeStateRepository");
    ClassName state = ClassName.get("simse.state", "State");
    ClassName hashMap = ClassName.get("java.util", "HashMap");
    
    for (int i = 0; i < objs.size(); i++) {
        SimSEObjectType tempType = objs.elementAt(i);
        ClassName tempClass = ClassName.get("simse.state", CodeGeneratorUtils.
        		getUpperCaseLeading(tempType.getName() + "StateRepository"));
        constructorBuilder.addStatement("$L = new $T()", tempType.getName().
        		substring(0, 1).toLowerCase() + i, tempClass);
      }
    		
    MethodSpec constructor = constructorBuilder.build();
    
    MethodSpec.Builder cloneBuilder = MethodSpec.methodBuilder("clone")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(Object.class)
    		.beginControlFlow("try")
    		.addStatement("$T cl = ($T) (super.clone())", typeStateRepo, typeStateRepo);
    
    for (int i = 0; i < objs.size(); i++) {
        SimSEObjectType tempType = objs.get(i);
        ClassName tempClass = ClassName.get("simse.state", CodeGeneratorUtils.
        		getUpperCaseLeading(tempType.getName() + "StateRepository"));
        String name = tempType.getName().substring(0, 1).toLowerCase() + i;
        cloneBuilder.addStatement("cl.$L = ($T) ($L.clone())", name, tempClass, name);
      }
    
    MethodSpec clone = cloneBuilder
    		.addStatement("return cl")
    		.nextControlFlow("catch ($T c)", CloneNotSupportedException.class)
    		.addStatement("System.out.println(c.getMessage())")
    		.endControlFlow()
    		.addStatement("return null")
    		.build();
    
    MethodSpec.Builder getAllBuilder = MethodSpec.methodBuilder("getAll")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(typeVector)
    		.addStatement("$T all = new $T()", typeVector, typeVector);
    
    for (int i = 0; i < objs.size(); i++) {
        SimSEObjectType tempType = objs.elementAt(i);
        getAllBuilder.addStatement("all.addAll($L.getAll())", tempType.getName().
        		substring(0, 1).toLowerCase() + i);
      }
    
    MethodSpec getAll = getAllBuilder
    		.addStatement("return all")
    		.build();
    

    		
    TypeSpec.Builder repStateRepoBuilder = TypeSpec.classBuilder(typeName + "StateRepository")
    		.addModifiers(Modifier.PUBLIC)
    		.addSuperinterface(Cloneable.class);
    
    for (int i = 0; i < objs.size(); i++) {
        SimSEObjectType tempType = objs.elementAt(i);
        ClassName tempClass = ClassName.get("simse.state", CodeGeneratorUtils.
        		getUpperCaseLeading(tempType.getName() + "StateRepository"));
        repStateRepoBuilder.addField(tempClass, tempType.getName().substring(0, 1)
        		.toLowerCase() + i);
      }
    
    repStateRepoBuilder.addMethod(constructor)
    		.addMethod(clone)
    		.addMethod(getAll);
    
    for (int i = 0; i < objs.size(); i++) {
        SimSEObjectType tempType = objs.elementAt(i);
        ClassName tempClass = ClassName.get("simse.state", CodeGeneratorUtils.
        		getUpperCaseLeading(tempType.getName() + "StateRepository"));
        MethodSpec tempMethod = MethodSpec.methodBuilder("get" + CodeGeneratorUtils.
        		getUpperCaseLeading(tempType.getName()) + "StateRepository")
        		.addModifiers(Modifier.PUBLIC)
        		.returns(tempClass)
        		.addStatement("return $L", tempType.getName().substring(0, 1).
        				toLowerCase() + i)
        		.build();
        
        repStateRepoBuilder.addMethod(tempMethod);
      }
    
    MethodSpec getInstance = MethodSpec.methodBuilder("getInstance")
    		.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
    		.addParameter(state, "state")
    		.returns(employeeStateRepoClass)
    		.addStatement("return instances.get(state)")
    		.build();
    
    MethodSpec createInstanceOneState = MethodSpec.methodBuilder("createInstance")
    		.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
    		.addParameter(state, "state")
    		.addStatement("instances.put(state, new $T())", employeeStateRepoClass)
    		.build();
    		
    MethodSpec createInstanceOldState = MethodSpec.methodBuilder("createInstance")
    		.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
    		.addParameter(state, "newState")
    		.addParameter(state, "oldState")
    		.addStatement("$T oldInstance = instances.get(oldState)", employeeStateRepoClass)
    		.addStatement("instances.put(newState, ($T)oldInstance.clone())", employeeStateRepoClass)
    		.build();
    
    MethodSpec stateExists = MethodSpec.methodBuilder("stateExists")
    		.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
    		.returns(boolean.class)
    		.addStatement("return instances.containsKey(state)")
    		.build();
    
    for (int i = 0; i < objs.size(); i++) {
        SimSEObjectType tempType = objs.elementAt(i);
        if(tempType.equals("Employee")) {
        	repStateRepoBuilder.addMethod(getInstance)
        					.addMethod(createInstanceOneState)
        					.addMethod(createInstanceOldState)
        					.addMethod(stateExists)
        					.addField(FieldSpec.builder(
        							ParameterizedTypeName.get(hashMap, state, employeeStateRepoClass), "instances", Modifier.PRIVATE, Modifier.STATIC)
        							.initializer("new $T<>()", hashMap).build());
        							
        }
      }
    
    TypeSpec repStateRepo = repStateRepoBuilder.build();
    
    JavaFile javaFile = JavaFile.builder("simse.state", repStateRepo)
    		.addFileComment("/* File generated by: simse.codegenerator.stategenerator.RepositoryGenerator */")
		    .build();
    
    try {
    	FileWriter writer = new FileWriter(repFile);
    	
    	javaFile.writeTo(writer);
    	writer.close();
    } catch (IOException e) {
        JOptionPane.showMessageDialog(null, ("Error writing file "
            + repFile.getPath() + ": " + e.toString()), "File IO Error",
            JOptionPane.WARNING_MESSAGE);
      }	
  }

  private void generateActionRepository(ActionType actType) {
    String uCaseName = CodeGeneratorUtils.getUpperCaseLeading(
    		actType.getName());
    ClassName actionClass = ClassName.get("simse.adts.actions", uCaseName + "Action");
    ClassName actionRepoClass = ClassName.get("simse.state", uCaseName + "ActionStateRepository");
    ClassName vector = ClassName.get("java.util", "Vector");
    ClassName ssObjectClass = ClassName.get("simse.adts.objects", "SSObject");
	  ClassName artifactStateRepoClass = ClassName.get("simse.state", "ArtifactStateRepository");
	  ClassName customerStateRepoClass = ClassName.get("simse.state", "CustomerStateRepository");
	  ClassName employeeStateRepoClass = ClassName.get("simse.state", "EmployeeStateRepository");
	  ClassName projectStateRepoClass = ClassName.get("simse.state", "ProjectStateRepository");
	  ClassName toolStateRepoClass = ClassName.get("simse.state", "ToolStateRepository");
    TypeName actionVector = ParameterizedTypeName.get(vector, actionClass);
    TypeName ssObjectVector = ParameterizedTypeName.get(vector, ssObjectClass);
    File repFile = new File(options.getCodeGenerationDestinationDirectory(),
        ("simse\\state\\" + uCaseName + "ActionStateRepository.java"));
    if (repFile.exists()) {
      repFile.delete(); // delete old version of file
    }
    
    MethodSpec constructor = MethodSpec.constructorBuilder()
    		.addModifiers(Modifier.PUBLIC)
    		.addStatement("$N = new $T()", "actions", actionVector)
    		.build();
    
    MethodSpec clone = MethodSpec.methodBuilder("clone")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(Object.class)
    		.beginControlFlow("try")
    		.addStatement("$T cl = ($T) (super.clone())", actionRepoClass, actionRepoClass)
    		.addStatement("$T clonedActions = new $T()", actionVector, actionVector)
    		.beginControlFlow("for (int i = 0; i < $N.size(); i++)", "actions")
    		.addStatement("clonedActions.add(($T) $N.elementAt(i).clone())", actionClass, "actions")
    		.endControlFlow()
    		.addStatement("cl.$N = clonedActions", "actions")
    		.addStatement("return cl")
    		.nextControlFlow("catch ($T c)", CloneNotSupportedException.class)
    		.addStatement("System.out.println(c.getMessage())")
    		.endControlFlow()
    		.addStatement("return null")
    		.build();
    
    MethodSpec add = MethodSpec.methodBuilder("add")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(boolean.class)
    		.addParameter(actionClass, "a")
    		.beginControlFlow("if ($N.contains(a) == false)", "actions")
    		.addStatement("$N.add(a)", "actions")
    		.addStatement("return true")
    		.endControlFlow()
    		.addStatement("return false")
    		.build();
    
    MethodSpec remove = MethodSpec.methodBuilder("remove")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(boolean.class)
    		.addParameter(actionClass, "a")
    		.beginControlFlow("if ($N.contains(a))", "actions")
    		.addStatement("$N.remove(a)", "actions")
    		.addStatement("return true")
    		.endControlFlow()
    		.addStatement("return false")
    		.build();
    
    MethodSpec getAllActions1 = MethodSpec.methodBuilder("getAllActions")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(actionVector)
    		.addStatement("return $N", "actions")
    		.build();
    
    MethodSpec getAllActions2 = MethodSpec.methodBuilder("getAllActions")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(actionVector)
    		.addParameter(ssObjectClass, "a")
    		.addStatement("$T all = new $T()", actionVector, actionVector)
    		.beginControlFlow("for (int i = 0; i < $N.size(); i++)", "actions")
    		.addStatement("$T b = $N.elementAt(i)", actionClass, "actions")
    		.addStatement("$T parts = b.getAllParticipants()", ssObjectVector)
    		.beginControlFlow("for (int j = 0; j < parts.size(); j++)")
    		.beginControlFlow("if (parts.elementAt(j).equals(a))")
    		.addStatement("all.add(b)")
    		.addStatement("break")
    		.endControlFlow()
    		.endControlFlow()
    		.endControlFlow()
    		.addStatement("return all")
    		.build();
    
    MethodSpec getAllActiveActions = MethodSpec.methodBuilder("getAllActiveActions")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(actionVector)
    		.addParameter(ssObjectClass, "a")
    		.addStatement("$T all = new $T()", actionVector, actionVector)
    		.beginControlFlow("for (int i = 0; i < $N.size(); i++)", "actions")
    		.addStatement("$T b = $N.elementAt(i)", actionClass, "actions")
    		.addStatement("$T parts = b.getAllActiveParticipants()", ssObjectVector)
    		.beginControlFlow("for (int j = 0; j < parts.size(); j++)")
    		.beginControlFlow("if (parts.elementAt(j).equals(a))")
    		.addStatement("all.add(b)")
    		.addStatement("break")
    		.endControlFlow()
    		.endControlFlow()
    		.endControlFlow()
    		.addStatement("return all")
    		.build();
    
    MethodSpec getAllInactiveActions = MethodSpec.methodBuilder("getAllInactiveActions")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(actionVector)
    		.addParameter(ssObjectClass, "a")
    		.addStatement("$T all = new $T()", actionVector, actionVector)
    		.beginControlFlow("for (int i = 0; i < $N.size(); i++)", "actions")
    		.addStatement("$T b = $N.elementAt(i)", actionClass, "actions")
    		.addStatement("$T parts = b.getAllInactiveParticipants()", ssObjectVector)
    		.beginControlFlow("for (int j = 0; j < parts.size(); j++)")
    		.beginControlFlow("if (parts.elementAt(j).equals(a))")
    		.addStatement("all.add(b)")
    		.addStatement("break")
    		.endControlFlow()
    		.endControlFlow()
    		.endControlFlow()
    		.addStatement("return all")
    		.build();
    
    MethodSpec getActionWithId = MethodSpec.methodBuilder("getActionWithId")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(actionClass)
    		.addParameter(int.class, "id")
    		.beginControlFlow("for (int i = 0; i < $N.size(); i++)", "actions")
    		.addStatement("$T act = $N.get(i)", actionClass, "actions")
    		.beginControlFlow("if (act.getId() == id)")
    		.addStatement("return act")
    		.endControlFlow()
    		.endControlFlow()
    		.addStatement("return null")
    		.build();
    
    MethodSpec refetchParticipants = MethodSpec.methodBuilder("refetchParticipants")
    		.addComment("Replaces all the participants in this action with their equivalent")
			.addComment("objects in the current state. Calling this function solves the problem")
			.addComment("that happens when you clone actions -- their hashtables point to")
			.addComment("participant objects that were part of the previous, non-cloned state.")
			.addComment("Hence, this function should be called after this object is cloned.")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(void.class)
    		.addParameter(artifactStateRepoClass, "artifactRep")
    		.addParameter(customerStateRepoClass, "customerRep")
    		.addParameter(employeeStateRepoClass, "employeeRep")
    		.addParameter(projectStateRepoClass, "projectRep")
    		.addParameter(toolStateRepoClass, "toolRep")
    		.beginControlFlow("for (int i = 0; i < $N.size(); i++)", "actions")
    		.addStatement("$T act = $N.elementAt(i)", actionClass, "actions")
    		.addStatement("act.refetchParticipants(artifactRep, customerRep, employeeRep,"
    				+ " projectRep, toolRep)")
    		.endControlFlow()
    		.build();
    
    TypeSpec repStateRepo = TypeSpec.classBuilder(uCaseName + "ActionStateRepository")
    		.addModifiers(Modifier.PUBLIC)
    		.addSuperinterface(Cloneable.class)
    		.addField(actionVector, "actions")
    		.addMethod(constructor)
    		.addMethod(clone)
    		.addMethod(add)
    		.addMethod(remove)
    		.addMethod(getAllActions1)
    		.addMethod(getAllActions2)
    		.addMethod(getAllActiveActions)
    		.addMethod(getAllInactiveActions)
    		.addMethod(getActionWithId)
    		.addMethod(refetchParticipants)
    		.build();
    
    JavaFile javaFile = JavaFile.builder("simse.state", repStateRepo)
    		.addFileComment("/* File generated by: simse.codegenerator.stategenerator.RepositoryGenerator */")
		    .build();
    
    try {
    	FileWriter writer = new FileWriter(repFile);
    	
    	javaFile.writeTo(writer);
    	writer.close();
    } catch (IOException e) {
        JOptionPane.showMessageDialog(null, ("Error writing file "
                + repFile.getPath() + ": " + e.toString()), "File IO Error",
                JOptionPane.WARNING_MESSAGE);
    }
  }
  
  private String removeFromActions(Vector<ActionType> acts) {
	  String actions = "";
      for (int i = 0; i < acts.size(); i++) {
          ActionType tempAct = acts.elementAt(i);
          String uCaseTempActName = 
          	CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName());
          actions += "Vector <" + uCaseTempActName + "Action> " + 
          		tempAct.getName().toLowerCase() + "actions = " + 
          		tempAct.getName().substring(0, 1).toLowerCase() + i + 
          		".getAllActions();\n";
          actions += "for(int i=0; i<" + tempAct.getName().toLowerCase()
              + "actions.size(); i++){\n";
          actions += uCaseTempActName + "Action b = " + 
          		tempAct.getName().toLowerCase() + "actions.elementAt(i);\n";
          // go through all participants:
          Vector<ActionTypeParticipant> participants = 
          	tempAct.getAllParticipants();
          for (int j = 0; j < participants.size(); j++) {
            ActionTypeParticipant part = participants.elementAt(j);
            actions += "if(a instanceof "
                + SimSEObjectTypeTypes.getText(part.getSimSEObjectTypeType())
                + "){\n";
            actions += "b.remove" + part.getName() + "(("
                + SimSEObjectTypeTypes.getText(part.getSimSEObjectTypeType())
                + ")a);\n}\n";
          }
          actions += "}\n";
        }
      return actions;
  }
  
  private String getActionWithId(Vector<ActionType> acts) {
	  	String actions = "";
	  
	    for (int i =0; i < acts.size(); i++) {
	        ActionType tempAct = acts.get(i);
	        if (i > 0) {
	          actions += "else ";
	        }
	        actions += "if (" + tempAct.getName().substring(0, 1).toLowerCase()
	            + i + ".getActionWithId(id) != null) {\n";
	        actions += "return " + tempAct.getName().substring(0, 1).toLowerCase()
	            + i + ".getActionWithId(id);\n}\n";
	      }
	    
	    return actions;
  }
}