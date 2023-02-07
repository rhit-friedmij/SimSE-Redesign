/*
 * This class is responsible for generating all of the code for the ADTs derived
 * from the SimSEObjectTypes in a model
 */

package simse.codegenerator.stategenerator;

import simse.codegenerator.CodeGenerator;
import simse.codegenerator.CodeGeneratorConstants;
import simse.codegenerator.CodeGeneratorUtils;

import simse.modelbuilder.ModelOptions;
import simse.modelbuilder.actionbuilder.ActionType;
import simse.modelbuilder.actionbuilder.ActionTypeDestroyer;
import simse.modelbuilder.actionbuilder.ActionTypeParticipant;
import simse.modelbuilder.actionbuilder.DefinedActionTypes;
import simse.modelbuilder.actionbuilder.TimedActionTypeDestroyer;
import simse.modelbuilder.objectbuilder.Attribute;
import simse.modelbuilder.objectbuilder.AttributeTypes;
import simse.modelbuilder.objectbuilder.DefinedObjectTypes;
import simse.modelbuilder.objectbuilder.NumericalAttribute;
import simse.modelbuilder.objectbuilder.SimSEObjectType;
import simse.modelbuilder.objectbuilder.SimSEObjectTypeTypes;

import java.io.File;
import java.io.FileReader;
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

public class ADTGenerator implements CodeGeneratorConstants {
  private ModelOptions options;
  private DefinedObjectTypes objTypes; // holds all of the defined object types
  private DefinedActionTypes actTypes; // holds all of the defined action types

  public ADTGenerator(ModelOptions options, DefinedObjectTypes objTypes, 
      DefinedActionTypes actTypes) {
    this.options = options;
    this.objTypes = objTypes;
    this.actTypes = actTypes;
  }

  public void generate() {
	  ClassName ssObjectClass = ClassName.get("simse.adts.objects", "SSObject");
	  ClassName vector = ClassName.get("java.util", "Vector");
	  ClassName stringClass = ClassName.get("java.lang", "String");
	  ClassName trackClass = ClassName.get("simse.gui", "TrackPanel");
	  ClassName employeeClass = ClassName.get("simse.adts.objects", "Employee");
	  ClassName actionClass = ClassName.get("simse.adts.actions", "Action");
	  ClassName idGeneratorClass= ClassName.get("simse.util", "IDGenerator");
	  TypeName stringVector = ParameterizedTypeName.get(vector, stringClass);
	  TypeName ssObjectVector = ParameterizedTypeName.get(vector, ssObjectClass);
	  
    Vector<SimSEObjectType> objs = objTypes.getAllObjectTypes();
    // generate SSObject class:
    File objClass = new File(options.getCodeGenerationDestinationDirectory(),
        ("simse\\adts\\objects\\SSObject.java"));
    if (objClass.exists()) {
      objClass.delete(); // delete old version of file
    }
    
    MethodSpec clone1 = MethodSpec.methodBuilder("clone")
    		.beginControlFlow("try")
    		.addStatement("$T cl = ($T) (super.clone())", ssObjectClass, ssObjectClass)
    		.addStatement("return cl")
    		.nextControlFlow("catch ($T c)", CloneNotSupportedException.class)
    		.addStatement("System.out.println(c.getMessage())")
    		.endControlFlow()
    		.addStatement("return null")
    		.build();
    
    TypeSpec ssObject = TypeSpec.classBuilder("SSObject")
    		.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
    		.addSuperinterface(Cloneable.class)
    		.addMethod(MethodSpec.constructorBuilder().build())
    		.addMethod(clone1)
    		.build();

	  JavaFile javaFile = JavaFile.builder("SSObject", ssObject)
			    .build();
    
    try {
    	javaFile.writeTo(objClass);
    } catch (IOException e) {
        JOptionPane.showMessageDialog(null, ("Error writing file SSObject.java"),
            "File IO Error", JOptionPane.WARNING_MESSAGE);
    }

    // generate abstract object classes:
    generateAbstractObjectClass(SimSEObjectTypeTypes
        .getText(SimSEObjectTypeTypes.ARTIFACT));
    generateAbstractObjectClass(SimSEObjectTypeTypes
        .getText(SimSEObjectTypeTypes.TOOL));
    generateAbstractObjectClass(SimSEObjectTypeTypes.PROJECT);

    // generate Employee class:
    File empClass = new File(options.getCodeGenerationDestinationDirectory(),
        ("simse\\adts\\objects\\Employee.java"));
    if (empClass.exists()) {
      empClass.delete(); // delete old version of file
    }
    
    MethodSpec employeeConstructor = MethodSpec.constructorBuilder()
    		.addStatement("$N = new $T()", "menu", stringVector)
    		.addStatement("clearMenu()")
    		.addStatement("$N = new $T()", "overheadText", String.class)
    		.addStatement("$N = $T.getInstance()", "track", trackClass)
    		.build();
    
    MethodSpec clone2 = MethodSpec.methodBuilder("clone")
    		.returns(Object.class)
    		.addModifiers(Modifier.PUBLIC)
    		.addStatement("$T cl = ($T) (super.clone())", employeeClass, employeeClass)
    		.addStatement("$T clonedMenu = new $T()", stringVector, stringVector)
    		.beginControlFlow("for (int i = 0; i < $N.size(); i++)", "menu")
    		.addStatement("clonedMenu.add($N.elementAt(i))", "menu")
    		.endControlFlow()
    		.addStatement("cl.$N = clonedMenu", "menu")
    		.addStatement("cl.$N = $N", "overheadText", "overheadText")
    		.addStatement("return cl")
    		.build();
    
    MethodSpec getMenu = MethodSpec.methodBuilder("getMenu")
    		.returns(stringVector)
    		.addModifiers(Modifier.PUBLIC)
    		.addStatement("return $N", "menu")
    		.build();
    
    MethodSpec clearMenu = MethodSpec.methodBuilder("clearMenu")
    		.returns(void.class)
    		.addModifiers(Modifier.PUBLIC)
    		.addStatement("$N.removeAllElements()", "menu")
    		.addStatement("$N.add($S)", "menu", "Everyone stop what you're doing")
    		.build();
    
    MethodSpec addMenuItem = MethodSpec.methodBuilder("addMenuItem")
    		.returns(boolean.class)
    		.addModifiers(Modifier.PUBLIC)
    		.addParameter(String.class, "s")
    		.beginControlFlow("for (int i = 0; i < $N.size(); i++) ", "menu")
    		.addStatement("$T item = $N.elementAt(i)", String.class, "menu")
    		.beginControlFlow("if (item.equals(s)) ")
    		.addStatement("return false")
    		.endControlFlow()
    		.endControlFlow()
    		.addStatement("// insert at correct alpha order")
    		.beginControlFlow("for (int i = 0; i < $N.size(); i++) ", "menu")
    		.addStatement("$T item = $N.elementAt(i)", String.class, "menu")
    		.beginControlFlow("if (s.compareToIgnoreCase(item) < 0) { // should be inserted"
    				+ " before 'item'")
    		.addStatement("$N.insertElementAt(s, i)", "menu")
    		.addStatement("return true")
    		.endControlFlow()
    		.endControlFlow()
    		.addStatement("// only reaches here if menu is empty or 's' should be placed at the en")
    		.addStatement("$N.add(s)", "menu")
    		.addStatement("return true")
    		.build();
    		
    MethodSpec removeMenuItem = MethodSpec.methodBuilder("removeMenuItem")
    		.returns(boolean.class)
    		.addModifiers(Modifier.PUBLIC)
    		.addParameter(String.class, "s")
    		.beginControlFlow("for (int i = 0; i < $N.size(); i++)", "menu")
    		.addStatement("$T item = $N.elementAt(i)", String.class, "menu")
    		.beginControlFlow("if (item.equals(s))")
    		.addStatement("$N.remove(item)", "menu")
    		.addStatement("return true")
    		.endControlFlow()
    		.endControlFlow()
    		.addStatement("return false")
    		.build();
    
    MethodSpec getOverheadText = MethodSpec.methodBuilder("getOverheadText")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(String.class)
    		.addStatement("return $N", "overheadText")
    		.build();
    
    MethodSpec setOverheadText1 = MethodSpec.methodBuilder("setOverheadText")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(void.class)
    		.addParameter(String.class, "s")
    		.beginControlFlow("if ((s != null) && (s.length() > 0)) ")
    		.beginControlFlow("if (($N != null) && ($N.length() > 0)) ", "overheadText", "overheadText")
    		.beginControlFlow("if ($N.equals($N)) ", "overheadText", "IDLE_STRING")
    		.addStatement("$N = s", "overheadText")
    		.nextControlFlow(" else if (!$N.endsWith(s)) // string has not just been said", "overheadText")
    		.addStatement("$N = $N.concat($S + s)", "overheadText", "overheadText", " AND ")
    		.endControlFlow()
    		.nextControlFlow(" else ")
    		.addStatement("$N = s", "overheadText")
    		.endControlFlow()
    		.endControlFlow()
    		.build();
    
    MethodSpec setOverheadText2 = MethodSpec.methodBuilder("setOverheadText")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(void.class)
    		.addParameter(String.class, "s")
    		.addParameter(String.class, "name")
    		.addStatement("$N.addText(s, name)", "track")
    		.addStatement("$N = s", "overheadText")
    		.build();
    
    MethodSpec clearOverheadText = MethodSpec.methodBuilder("clearOverheadText")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(void.class)
    		.addStatement("$N = new $T()", "overheadText", String.class)
    		.build();
    
    FieldSpec idle = FieldSpec.builder(String.class, "IDLE_STRING")
    		.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
    		.initializer("I'm not doing anything right now")
    		.build();
    
    TypeSpec.Builder employeeBuilder = TypeSpec.classBuilder("Employee")
    		.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
    		.superclass(ssObjectClass)
    		.addSuperinterface(Cloneable.class)
    		.addField(stringVector, "menu", Modifier.PRIVATE)
    		.addField(String.class, "overheadText", Modifier.PRIVATE)
    		.addField(idle)
    		.addField(trackClass, "track", Modifier.PRIVATE)
    		.addMethod(employeeConstructor)
    		.addMethod(clone2)
    		.addMethod(getMenu)
    		.addMethod(clearMenu)
    		.addMethod(addMenuItem)
    		.addMethod(removeMenuItem)
    		.addMethod(getOverheadText)
    		.addMethod(setOverheadText1)
    		.addMethod(setOverheadText2)
    		.addMethod(clearOverheadText);
    
    if (CodeGenerator.allowHireFire) {
        // setHired function - this function will be overridden by subclasses
    	employeeBuilder.addMethod(MethodSpec.methodBuilder("setHired")
    			.returns(void.class)
    			.addModifiers(Modifier.PUBLIC)
    			.addParameter(boolean.class, "t")
    			.build());

        // getHired function - this function will be overridden by subclasses
    	employeeBuilder.addMethod(MethodSpec.methodBuilder("getHired")
    			.returns(boolean.class)
    			.addModifiers(Modifier.PUBLIC)
    			.addStatement("return true")
    			.build());
      }
    
    TypeSpec employee = employeeBuilder.build();
    
    javaFile = JavaFile.builder("Employee", employee)
		    .build();
    try {
    	javaFile.writeTo(empClass);
    } catch (IOException e) {
        JOptionPane.showMessageDialog(null, ("Error writing file "
            + empClass.getPath() + ": " + e.toString()), "File IO Error",
            JOptionPane.WARNING_MESSAGE);
    }

    // generate Customer class:
    File custClass = new File(options.getCodeGenerationDestinationDirectory(),
        ("simse\\adts\\objects\\Customer.java"));
    if (custClass.exists()) {
      custClass.delete(); // delete old version of file
    }
    try {
    	FileReader reader = new FileReader(new File("src/simse/codegenerator/"
    			+ "resources/Customer.txt"));
    	FileWriter writer = new FileWriter(custClass);
    	char[] buf = new char[256];
    	int length;
    	while ((length = reader.read(buf)) > 0) {
    		writer.write(buf, 0, length);
    	}
    	reader.close();
    	writer.close();
    } catch (IOException e) {
      JOptionPane.showMessageDialog(null, ("Error writing file "
          + custClass.getPath() + ": " + e.toString()), "File IO Error",
          JOptionPane.WARNING_MESSAGE);
    }

    // go through each object and generate code for it:
    for (int i = 0; i < objs.size(); i++) {
      generateObjectADT(objs.elementAt(i));
    }

    // generate Action class:
    File actClass = new File(options.getCodeGenerationDestinationDirectory(), 
        ("simse\\adts\\actions\\Action.java"));
    if (actClass.exists()) {
      actClass.delete(); // delete old version of file
    }

    Vector<ActionType> acts = actTypes.getAllActionTypes();
    
    MethodSpec actionConstructor = MethodSpec.constructorBuilder()
    		.addModifiers(Modifier.PUBLIC)
    		.addStatement("$N = $T.getNextID()", "id", idGeneratorClass)
    		.addStatement("$N = 0", "timeElapsed")
    		.addStatement("$N = $S", "actionName", "")
    		.build();
    
    MethodSpec clone3 = MethodSpec.methodBuilder("clone")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(Object.class)
    		.beginControlFlow("try")
    		.addStatement("$T cl = ($T) (super.clone())", actionClass, actionClass)
    		.addStatement("cl.$N = $N", "id", "id")
    		.addStatement("cl.$N = $N", "timeElapsed", "timeElapsed")
    		.addStatement("return cl")
    		.nextControlFlow("catch ($T c)", CloneNotSupportedException.class)
    		.addStatement("System.out.println(c.getMessage())")
    		.endControlFlow()
    		.addStatement("return null")
    		.build();
    
    MethodSpec getId = MethodSpec.methodBuilder("getId")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(int.class)
    		.addStatement("return $N", "id")
    		.build();
    
    MethodSpec getActionName = MethodSpec.methodBuilder("getActionName")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(String.class)
    		.addStatement("return $N", "actionName")
    		.build();
    
    MethodSpec incrementTimeElapsed = MethodSpec.methodBuilder("incrementTimeElapsed")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(void.class)
    		.addStatement("$N++", "timeElapsed")
    		.build();
    
    MethodSpec getTimeElapsed = MethodSpec.methodBuilder("getTimeElapsed")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(int.class)
    		.addStatement("return $N", "timeElapsed")
    		.build();
    
    MethodSpec getAllParticipants = MethodSpec.methodBuilder("getAllParticipants")
    		.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
    		.returns(ssObjectVector)
    		.build();
    
    MethodSpec getAllActiveParticipants = MethodSpec.methodBuilder("getAllActiveParticipants")
    		.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
    		.returns(ssObjectVector)
    		.build();
    
    MethodSpec getAllInactiveParticipants = MethodSpec.methodBuilder("getAllInactiveParticipants")
    		.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
    		.returns(ssObjectVector)
    		.build();
    
    TypeSpec.Builder actionBuilder = TypeSpec.classBuilder("Action")
    		.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
    		.addSuperinterface(Cloneable.class);
    
    for (int i = 0; i < acts.size(); i++) {
    	FieldSpec tempField = FieldSpec.builder(String.class,
    			acts.get(i).getName().toUpperCase())
    			.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
    			.initializer(acts.get(i).getName())
    			.build();
    	
    	actionBuilder.addField(tempField);
    }
    
    TypeSpec action = actionBuilder
    		.addField(int.class, "id", Modifier.PRIVATE)
    		.addField(int.class, "timeElapsed", Modifier.PRIVATE)
    		.addField(String.class, "actionName", Modifier.PROTECTED)
    		.addMethod(actionConstructor)
    		.addMethod(clone3)
    		.addMethod(getId)
    		.addMethod(getActionName)
    		.addMethod(incrementTimeElapsed)
    		.addMethod(getTimeElapsed)
    		.addMethod(getAllParticipants)
    		.addMethod(getAllActiveParticipants)
    		.addMethod(getAllInactiveParticipants)
    		.build();
    
    javaFile = JavaFile.builder("Action", action)
		    .build();
    
    try {
    	javaFile.writeTo(actClass);
    } catch (IOException e) {
        JOptionPane.showMessageDialog(null, ("Error writing file " + actClass
            .getPath()), "File IO Error", JOptionPane.WARNING_MESSAGE);
      }

    // go through each action and generate code for it:
    for (int i = 0; i < acts.size(); i++) {
      generateActionADT(acts.elementAt(i));
    }
  }

  private void generateAbstractObjectClass(SimSEObjectType objType) {
	 ClassName ssObjectClass = ClassName.get("simse.adts.objects", "SSObject");
   
	 File absClass = new File(options.getCodeGenerationDestinationDirectory(),
        ("simse\\adts\\objects\\" + className + ".java"));
    if (absClass.exists()) {
      absClass.delete(); // delete old version of file
    }
    
    TypeSpec absClassSpec = TypeSpec.classBuilder(className)
    		.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
    		.addSuperinterface(Cloneable.class)
    		.superclass(ssObjectClass)
    		
    		.build();
    
    try {
    	
    } catch (IOException e) {
        JOptionPane.showMessageDialog(null, ("Error writing file "
            + absClass.getPath() + ": " + e.toString()), "File IO Error",
            JOptionPane.WARNING_MESSAGE);
  }
      writer
          .write("/* File generated by: simse.codegenerator.stategenerator.ADTGenerator */");
      writer.write(NEWLINE);
      writer.write("package simse.adts.objects;");
      writer.write(NEWLINE);
      writer.write("public abstract class " + className + " extends SSObject implements Cloneable");
      writer.write(NEWLINE);
      writer.write(OPEN_BRACK);
      writer.write(NEWLINE);
      
      // constructor:
      writer.write("public " + className + "(){}");
      writer.write(NEWLINE);
      writer.write(NEWLINE);
      
      // "clone" method:
      writer.write("public Object clone() {");
      writer.write(NEWLINE);
      writer.write(className + " cl = (" + className + ") (super.clone());");
      writer.write(NEWLINE);
      writer.write("return cl;");
      writer.write(NEWLINE);
      writer.write(CLOSED_BRACK);
      writer.write(NEWLINE);
      
      writer.write(CLOSED_BRACK);
      writer.close();
  }

  private void generateObjectADT(SimSEObjectType objType) {
    File adtFile = new File(options.getCodeGenerationDestinationDirectory(), 
        ("simse\\adts\\objects\\" + CodeGeneratorUtils.getUpperCaseLeading(
        		objType.getName()) + ".java"));
    if (adtFile.exists()) {
      adtFile.delete(); // delete old version of file
    }
    try {
      FileWriter writer = new FileWriter(adtFile);
      writer
          .write("/* File generated by: simse.codegenerator.stategenerator.ADTGenerator */");
      writer.write(NEWLINE);
      writer.write("package simse.adts.objects;");
      writer.write(NEWLINE);

      if (objType.getType() == SimSEObjectTypeTypes.EMPLOYEE
          && CodeGenerator.allowHireFire) {
        writer.write("import java.util.Vector;");
        writer.write(NEWLINE);
      }
      writer.write("public class " + CodeGeneratorUtils.getUpperCaseLeading(
      		objType.getName()) + " extends " + SimSEObjectTypeTypes.getText(
      				objType.getType()) + " implements Cloneable");
      writer.write(NEWLINE);
      writer.write(OPEN_BRACK);
      writer.write(NEWLINE);

      // member variables/attributes:
      Vector<Attribute> attributes = objType.getAllAttributes();
      for (int i = 0; i < attributes.size(); i++) {
        Attribute att = attributes.elementAt(i);
        writer.write("private ");

        // type:
        writer.write(getTypeAsString(att) + " ");

        // variable name:
        writer.write(att.getName().toLowerCase() + ";");
        writer.write(NEWLINE);
      }
      writer.write(NEWLINE);

      // constructor:
      writer.write("public " + CodeGeneratorUtils.getUpperCaseLeading(
      		objType.getName()) + "(");
      for (int i = 0; i < attributes.size(); i++) {
        Attribute att = attributes.elementAt(i);
        writer.write(getTypeAsString(att) + " ");
        writer.write(att.getName().substring(0, 1).toLowerCase() + i);
        if (i == (attributes.size() - 1)) { // on last attribute
          writer.write(")");
        } else { // not on last attribute
          writer.write(", ");
        }
      }
      writer.write(NEWLINE);
      writer.write(OPEN_BRACK);
      writer.write(NEWLINE);
      
      // assignments:
      for (int i = 0; i < attributes.size(); i++) {
        Attribute att = attributes.elementAt(i);
        writer.write("set" + CodeGeneratorUtils.getUpperCaseLeading(
        		att.getName()) + "(" + 
        		(att.getName().substring(0, 1).toLowerCase() + i) + ");");
        writer.write(NEWLINE);
      }
      writer.write(CLOSED_BRACK);
      writer.write(NEWLINE);
      writer.write(NEWLINE);
      
      // "clone" method:
      writer.write("public Object clone() {");
      writer.write(NEWLINE);
      writer.write(CodeGeneratorUtils.getUpperCaseLeading(objType.getName()) + 
      		" cl = (" + CodeGeneratorUtils.getUpperCaseLeading(
      				objType.getName()) + ")(super.clone());");
      writer.write(NEWLINE);
      for (int i = 0; i < attributes.size(); i++) {
        Attribute att = attributes.elementAt(i);
        writer.write("cl." + att.getName().toLowerCase() + " = " +
            att.getName().toLowerCase() + ";");
        writer.write(NEWLINE);
      }
      writer.write("return cl;");
      writer.write(NEWLINE);
      writer.write(CLOSED_BRACK);
      writer.write(NEWLINE);
      writer.write(NEWLINE);

      // get and set functions:
      for (int i = 0; i < attributes.size(); i++) {
        Attribute att = attributes.elementAt(i);
        
        // "get" method:
        writer.write("public ");
        writer.write(getTypeAsString(att) + " ");
        writer.write("get" + CodeGeneratorUtils.getUpperCaseLeading(
        		att.getName()) + "()");
        writer.write(NEWLINE);
        writer.write(OPEN_BRACK);
        writer.write(NEWLINE);
        writer.write("return " + att.getName().toLowerCase() + ";");
        writer.write(NEWLINE);
        writer.write(CLOSED_BRACK);
        writer.write(NEWLINE);
        writer.write(NEWLINE);
        
        // "set" method:
        writer.write("public void set" + CodeGeneratorUtils.getUpperCaseLeading(
        		att.getName())
            + "(");
        writer.write(getTypeAsString(att) + " a)");
        writer.write(NEWLINE);
        writer.write(OPEN_BRACK);
        writer.write(NEWLINE);
        if ((att instanceof NumericalAttribute)
            && (((NumericalAttribute) att).isMinBoundless() == false)) { // has
        																																 // a
                                                                         // min
                                                                         // val
        	NumericalAttribute numAtt = (NumericalAttribute)att;
          String minVal = (numAtt).getMinValue().toString();
          writer.write("if(a < " + minVal + ")");
          writer.write(NEWLINE);
          writer.write(OPEN_BRACK);
          writer.write(NEWLINE);
          writer.write(att.getName().toLowerCase() + " = " + minVal + ";");
          writer.write(NEWLINE);
          writer.write(CLOSED_BRACK);
          writer.write(NEWLINE);
          if ((numAtt).isMaxBoundless() == false) { // has a max value
            String maxVal = (numAtt).getMaxValue().toString();
            writer.write("else if(a > " + maxVal + ")");
            writer.write(NEWLINE);
            writer.write(OPEN_BRACK);
            writer.write(NEWLINE);
            writer.write(att.getName().toLowerCase() + " = " + maxVal + ";");
            writer.write(NEWLINE);
            writer.write(CLOSED_BRACK);
            writer.write(NEWLINE);
          }
          writer.write("else");
          writer.write(NEWLINE);
          writer.write(OPEN_BRACK);
          writer.write(NEWLINE);
          writer.write(att.getName().toLowerCase() + " = a;");
          writer.write(NEWLINE);
          writer.write(CLOSED_BRACK);
          writer.write(NEWLINE);
        } else if ((att instanceof NumericalAttribute)
            && (((NumericalAttribute) att).isMaxBoundless() == false)) { // has
        																																 // a
                                                                       	 // max
                                                                         // val
        	NumericalAttribute numAtt = (NumericalAttribute)att;
          String maxVal = (numAtt).getMaxValue().toString();
          writer.write("if(a > " + maxVal + ")");
          writer.write(NEWLINE);
          writer.write(OPEN_BRACK);
          writer.write(NEWLINE);
          writer.write(att.getName().toLowerCase() + " = " + maxVal + ";");
          writer.write(NEWLINE);
          writer.write(CLOSED_BRACK);
          writer.write(NEWLINE);
          if ((numAtt).isMinBoundless() == false) { // has a min value
            String minVal = (numAtt).getMinValue().toString();
            writer.write("else if(a < " + minVal + ")");
            writer.write(NEWLINE);
            writer.write(OPEN_BRACK);
            writer.write(NEWLINE);
            writer.write(att.getName().toLowerCase() + " = " + minVal + ";");
            writer.write(NEWLINE);
            writer.write(CLOSED_BRACK);
            writer.write(NEWLINE);
          }
          writer.write("else");
          writer.write(NEWLINE);
          writer.write(OPEN_BRACK);
          writer.write(NEWLINE);
          writer.write(att.getName().toLowerCase() + " = a;");
          writer.write(NEWLINE);
          writer.write(CLOSED_BRACK);
          writer.write(NEWLINE);
        } else {
          writer.write(att.getName().toLowerCase() + " = a;");
          writer.write(NEWLINE);
        }
        writer.write(CLOSED_BRACK);
        writer.write(NEWLINE);
        writer.write(NEWLINE);
      }

      if (objType.getType() == SimSEObjectTypeTypes.EMPLOYEE
          && CodeGenerator.allowHireFire) {
        Vector<Attribute> v = objType.getAllAttributes();
        Attribute keyAtt = null;

        for (int i = 0; i < v.size(); i++) {
          Attribute att = v.elementAt(i);
          if (att.isKey())
            keyAtt = att;
        }

        writer.write("public Vector getMenu()");
        writer.write(NEWLINE);
        writer.write(OPEN_BRACK);
        writer.write(NEWLINE);
        writer.write("Vector v = new Vector();");
        writer.write(NEWLINE);
        writer.write("v.addAll(super.getMenu());");
        writer.write(NEWLINE);
        writer.write("if (getHired())");
        writer.write(NEWLINE);
        writer.write(OPEN_BRACK);
        writer.write(NEWLINE);
        writer.write("v.add(\"Fire Employee - \" + get"
            + CodeGeneratorUtils.getUpperCaseLeading(keyAtt.getName()) + 
            "());");
        writer.write(NEWLINE);
        writer.write(CLOSED_BRACK);
        writer.write(NEWLINE);
        writer.write("else");
        writer.write(NEWLINE);
        writer.write(OPEN_BRACK);
        writer.write(NEWLINE);
        writer.write("v = new Vector();");
        writer.write(NEWLINE);
        writer.write("v.add(\"Hire Employee - \" + get"
            + CodeGeneratorUtils.getUpperCaseLeading(keyAtt.getName()) + 
            "());");
        writer.write(NEWLINE);
        writer.write(CLOSED_BRACK);
        writer.write(NEWLINE);
        writer.write("return v;");
        writer.write(NEWLINE);
        writer.write(CLOSED_BRACK);
        writer.write(NEWLINE);

      }

      writer.write(CLOSED_BRACK);
      writer.close();
    } catch (IOException e) {
      JOptionPane.showMessageDialog(null, ("Error writing file "
          + adtFile.getPath() + ": " + e.toString()), "File IO Error",
          JOptionPane.WARNING_MESSAGE);
    }
  }

  private String getTypeAsString(Attribute att) {
    if (att.getType() == AttributeTypes.INTEGER) {
      return "int";
    } else if (att.getType() == AttributeTypes.DOUBLE) {
      return "double";
    } else if (att.getType() == AttributeTypes.BOOLEAN) {
      return "boolean";
    } else { //(att.getType() == AttributeTypes.STRING)
      return "String";
    }
  }

  private void generateActionADT(ActionType actType) {
    File adtFile = new File(options.getCodeGenerationDestinationDirectory(), 
        ("simse\\adts\\actions\\" + CodeGeneratorUtils.getUpperCaseLeading(
        		actType.getName()) + "Action.java"));
    if (adtFile.exists()) {
      adtFile.delete(); // delete old version of file
    }
    try {
      FileWriter writer = new FileWriter(adtFile);
      writer
          .write("/* File generated by: simse.codegenerator.stategenerator.ADTGenerator */");
      writer.write(NEWLINE);
      writer.write("package simse.adts.actions;");
      writer.write(NEWLINE);
      writer.write("import simse.adts.objects.*;");
      writer.write(NEWLINE);
      writer.write("import simse.state.ArtifactStateRepository;");
      writer.write(NEWLINE);
      writer.write("import simse.state.CustomerStateRepository;");
      writer.write(NEWLINE);
      writer.write("import simse.state.EmployeeStateRepository;");
      writer.write(NEWLINE);
      writer.write("import simse.state.ProjectStateRepository;");
      writer.write(NEWLINE);
      writer.write("import simse.state.ToolStateRepository;");
      writer.write(NEWLINE);
      writer.write("import java.util.*;");
      writer.write(NEWLINE);
      writer.write("public class " + CodeGeneratorUtils.getUpperCaseLeading(
      		actType.getName()) + "Action extends Action implements Cloneable");
      writer.write(NEWLINE);
      writer.write(OPEN_BRACK);
      writer.write(NEWLINE);

      // member variables/attributes:
      Vector<ActionTypeParticipant> participants = actType.getAllParticipants();
      for (int i = 0; i < participants.size(); i++) {
        ActionTypeParticipant tempPart = participants.elementAt(i);
        writer.write("private Hashtable<" + SimSEObjectTypeTypes.getText(
        		tempPart.getSimSEObjectTypeType()) + ", Boolean> " + 
        		tempPart.getName().toLowerCase() + "s;");
        writer.write(NEWLINE);
      }

      boolean hasTimedDestroyer = false;
      Vector<ActionTypeDestroyer> allDests = actType.getAllDestroyers();
      for (int i = 0; i < allDests.size(); i++) {
        ActionTypeDestroyer tempDest = allDests.elementAt(i);
        if (tempDest instanceof TimedActionTypeDestroyer) {
          hasTimedDestroyer = true;
          break;
        }
      }

      if (hasTimedDestroyer) { // timed destroyer
        // give it a timeToLive member variable:
        writer.write("private int timeToLive;");
        writer.write(NEWLINE);
      }

      // constructor:
      writer.write("public " + CodeGeneratorUtils.getUpperCaseLeading(
      		actType.getName())
          + "Action()");
      writer.write(NEWLINE);
      writer.write(OPEN_BRACK);
      writer.write(NEWLINE);
      for (int i = 0; i < participants.size(); i++) {
        ActionTypeParticipant tempPart = participants.elementAt(i);
        writer.write(tempPart.getName().toLowerCase() + 
        		"s = new Hashtable<" + 
        		SimSEObjectTypeTypes.getText(tempPart.getSimSEObjectTypeType()) +
        		", Boolean>();");
        writer.write(NEWLINE);
      }
      if (hasTimedDestroyer) { // timed destroyer
        // find the timed destroyer:
        for (int j = 0; j < allDests.size(); j++) {
          ActionTypeDestroyer tempDest = allDests.elementAt(j);
          if (tempDest instanceof TimedActionTypeDestroyer) {
            writer.write("timeToLive = "
                + ((TimedActionTypeDestroyer) tempDest).getTime() + ";");
            writer.write(NEWLINE);
            break;
          }
        }
      }
      writer.write(CLOSED_BRACK);
      writer.write(NEWLINE);
      writer.write(NEWLINE);

      // methods:
      
      // "clone" method:
      writer.write("public Object clone() {");
      writer.write(NEWLINE);
      writer.write(CodeGeneratorUtils.getUpperCaseLeading(actType.getName()) + 
      		"Action cl = (" + CodeGeneratorUtils.getUpperCaseLeading(
      				actType.getName()) + "Action)(super.clone());");
      writer.write(NEWLINE);
      for (int i = 0; i < participants.size(); i++) {
        ActionTypeParticipant tempPart = participants.elementAt(i);
        String typeString = new String("<" + 
        		SimSEObjectTypeTypes.getText(tempPart.getSimSEObjectTypeType()) +
        		", Boolean>");
        writer.write("Hashtable" + typeString + " cloned" + 
        		tempPart.getName().toLowerCase() + "s = new Hashtable<" + 
        		SimSEObjectTypeTypes.getText(tempPart.getSimSEObjectTypeType()) +
        		", Boolean>();");
        writer.write(NEWLINE);
        writer.write("cloned" + tempPart.getName().toLowerCase() + "s.putAll(" +
            tempPart.getName().toLowerCase() + "s);");
        writer.write(NEWLINE);
        writer.write("cl." + tempPart.getName().toLowerCase() + "s = cloned" +
            tempPart.getName().toLowerCase() + "s;");
        writer.write(NEWLINE);
      }
      writer.write("return cl;");
      writer.write(NEWLINE);
      writer.write(CLOSED_BRACK);
      writer.write(NEWLINE);
      writer.write(NEWLINE);
      
      if (hasTimedDestroyer) { // timed destroyer
        // "getTimeToLive" method:
        writer.write("public int getTimeToLive()");
        writer.write(NEWLINE);
        writer.write(OPEN_BRACK);
        writer.write(NEWLINE);
        writer.write("return timeToLive;");
        writer.write(NEWLINE);
        writer.write(CLOSED_BRACK);
        writer.write(NEWLINE);
        writer.write(NEWLINE);

        // "decrementTimeToLive" method:
        writer.write("public void decrementTimeToLive()");
        writer.write(NEWLINE);
        writer.write(OPEN_BRACK);
        writer.write(NEWLINE);
        writer.write("timeToLive--;");
        writer.write(NEWLINE);
        writer.write("if(timeToLive < 0)");
        writer.write(NEWLINE);
        writer.write(OPEN_BRACK);
        writer.write(NEWLINE);
        writer.write("timeToLive = 0;");
        writer.write(NEWLINE);
        writer.write(CLOSED_BRACK);
        writer.write(NEWLINE);
        writer.write(CLOSED_BRACK);
        writer.write(NEWLINE);
        writer.write(NEWLINE);
      }

      // "getAllParticipants" method:
      writer.write("public Vector<SSObject> getAllParticipants()");
      writer.write(NEWLINE);
      writer.write(OPEN_BRACK);
      writer.write(NEWLINE);
      writer.write("Vector<SSObject> all = new Vector<SSObject>();");
      writer.write(NEWLINE);
      for (int i = 0; i < participants.size(); i++) {
        ActionTypeParticipant tempPart = participants.elementAt(i);
        writer.write("all.addAll(getAll" + tempPart.getName() + "s());");
        writer.write(NEWLINE);
      }
      writer.write("return all;");
      writer.write(NEWLINE);
      writer.write(CLOSED_BRACK);
      writer.write(NEWLINE);

      // "getAllActiveParticipants" method:
      writer.write("public Vector<SSObject> getAllActiveParticipants()");
      writer.write(NEWLINE);
      writer.write(OPEN_BRACK);
      writer.write(NEWLINE);
      writer.write("Vector<SSObject> all = new Vector<SSObject>();");
      writer.write(NEWLINE);
      for (int i = 0; i < participants.size(); i++) {
        ActionTypeParticipant tempPart = participants.elementAt(i);
        writer.write("all.addAll(getAllActive" + tempPart.getName() + "s());");
        writer.write(NEWLINE);
      }
      writer.write("return all;");
      writer.write(NEWLINE);
      writer.write(CLOSED_BRACK);
      writer.write(NEWLINE);
      writer.write(NEWLINE);

      // "getAllInactiveParticipants" method:
      writer.write("public Vector<SSObject> getAllInactiveParticipants()");
      writer.write(NEWLINE);
      writer.write(OPEN_BRACK);
      writer.write(NEWLINE);
      writer.write("Vector<SSObject> all = new Vector<SSObject>();");
      writer.write(NEWLINE);
      for (int i = 0; i < participants.size(); i++) {
        ActionTypeParticipant tempPart = participants.elementAt(i);
        writer
            .write("all.addAll(getAllInactive" + tempPart.getName() + "s());");
        writer.write(NEWLINE);
      }
      writer.write("return all;");
      writer.write(NEWLINE);
      writer.write(CLOSED_BRACK);
      writer.write(NEWLINE);

      for (int i = 0; i < participants.size(); i++) {
        ActionTypeParticipant tempPart = participants.elementAt(i);
        String vectorTypeString = new String("<" + 
        		SimSEObjectTypeTypes.getText(tempPart.getSimSEObjectTypeType()) + 
        		">");

        // "getAll[Participant]s" method:
        writer.write("public Vector" + vectorTypeString + " getAll" + 
        		tempPart.getName() + "s()");
        writer.write(NEWLINE);
        writer.write(OPEN_BRACK);
        writer.write(NEWLINE);
        writer.write("Vector" + vectorTypeString + "a = new Vector" + 
        		vectorTypeString + "();");
        writer.write(NEWLINE);
        writer.write("Enumeration" + vectorTypeString + " e = " + 
        		tempPart.getName().toLowerCase() + "s.keys();");
        writer.write(NEWLINE);
        writer.write("for(int i=0; i<" + tempPart.getName().toLowerCase()
            + "s.size(); i++)");
        writer.write(NEWLINE);
        writer.write(OPEN_BRACK);
        writer.write(NEWLINE);
        writer.write("a.add(e.nextElement());");
        writer.write(NEWLINE);
        writer.write(CLOSED_BRACK);
        writer.write(NEWLINE);
        writer.write("return a;");
        writer.write(NEWLINE);
        writer.write(CLOSED_BRACK);
        writer.write(NEWLINE);
        writer.write(NEWLINE);

        // "getAllActive[Participant]s" method:
        writer.write("public Vector" + vectorTypeString + " getAllActive" + 
        		tempPart.getName() + "s()");
        writer.write(NEWLINE);
        writer.write(OPEN_BRACK);
        writer.write(NEWLINE);
        writer.write("Vector" + vectorTypeString + " a = new Vector" + 
        		vectorTypeString + "();");
        writer.write(NEWLINE);
        writer.write("Enumeration" + vectorTypeString + " e = " + 
        		tempPart.getName().toLowerCase() + "s.keys();");
        writer.write(NEWLINE);
        writer.write("for(int i=0; i<" + tempPart.getName().toLowerCase()
            + "s.size(); i++)");
        writer.write(NEWLINE);
        writer.write(OPEN_BRACK);
        writer.write(NEWLINE);
        writer.write(SimSEObjectTypeTypes.getText(tempPart
            .getSimSEObjectTypeType())
            + " key = e.nextElement();");
        writer.write(NEWLINE);
        writer.write("if((" + tempPart.getName().toLowerCase()
            + "s.get(key)).booleanValue() == true)");
        writer.write(NEWLINE);
        writer.write(OPEN_BRACK);
        writer.write(NEWLINE);
        writer.write("a.add(key);");
        writer.write(NEWLINE);
        writer.write(CLOSED_BRACK);
        writer.write(NEWLINE);
        writer.write(CLOSED_BRACK);
        writer.write(NEWLINE);
        writer.write("return a;");
        writer.write(NEWLINE);
        writer.write(CLOSED_BRACK);
        writer.write(NEWLINE);
        writer.write(NEWLINE);

        // "getAllInactive[Participant]s" method:
        writer.write("public Vector" + vectorTypeString + " getAllInactive" + 
        		tempPart.getName() + "s()");
        writer.write(NEWLINE);
        writer.write(OPEN_BRACK);
        writer.write(NEWLINE);
        writer.write("Vector" + vectorTypeString + " a = new Vector" +
        		vectorTypeString + "();");
        writer.write(NEWLINE);
        writer.write("Enumeration" + vectorTypeString + " e = " + 
        		tempPart.getName().toLowerCase() + "s.keys();");
        writer.write(NEWLINE);
        writer.write("for(int i=0; i<" + tempPart.getName().toLowerCase()
            + "s.size(); i++)");
        writer.write(NEWLINE);
        writer.write(OPEN_BRACK);
        writer.write(NEWLINE);
        writer.write(SimSEObjectTypeTypes.getText(tempPart
            .getSimSEObjectTypeType())
            + " key = e.nextElement();");
        writer.write(NEWLINE);
        writer.write("if((" + tempPart.getName().toLowerCase()
            + "s.get(key)).booleanValue() == false)");
        writer.write(NEWLINE);
        writer.write(OPEN_BRACK);
        writer.write(NEWLINE);
        writer.write("a.add(key);");
        writer.write(NEWLINE);
        writer.write(CLOSED_BRACK);
        writer.write(NEWLINE);
        writer.write(CLOSED_BRACK);
        writer.write(NEWLINE);
        writer.write("return a;");
        writer.write(NEWLINE);
        writer.write(CLOSED_BRACK);
        writer.write(NEWLINE);
        writer.write(NEWLINE);

        // "add" method:
        writer.write("public boolean add" + tempPart.getName() + "("
            + SimSEObjectTypeTypes.getText(tempPart.getSimSEObjectTypeType())
            + " a)");
        writer.write(NEWLINE);
        writer.write(OPEN_BRACK);
        writer.write(NEWLINE);

        if (CodeGenerator.allowHireFire
            && tempPart.getSimSEObjectTypeType() == 
            	SimSEObjectTypeTypes.EMPLOYEE) {
          // do not allow nonHired employees to be part of any actions
          writer.write("if (!a.getHired())");
          writer.write(NEWLINE);
          writer.write("return false;");
          writer.write(NEWLINE);
        }

        writer.write("if((" + tempPart.getName().toLowerCase()
            + "s.containsKey(a))");
        Vector<SimSEObjectType> types = tempPart.getAllSimSEObjectTypes();
        if (types.size() > 0) {
          writer.write(" ||");
        }
        for (int j = 0; j < types.size(); j++) {
          if (j > 0) { // not on first element
            writer.write(" &&");
          } else { // on first element
            writer.write("(");
          }
          SimSEObjectType tempType = types.elementAt(j);
          writer.write(" ((a instanceof "
              + CodeGeneratorUtils.getUpperCaseLeading(tempType.getName()) + 
              ") == false)");
        }
        if (types.size() > 0) {
          writer.write(")");
        }
        if (tempPart.getQuantity().isMaxValBoundless() == false) { 
        	// has a maximum number of participants that can be in this action
          writer.write(" || (" + tempPart.getName().toLowerCase()
              + "s.size() >= " + tempPart.getQuantity().getMaxVal().toString()
              + ")");
        }
        writer.write(")");
        writer.write(NEWLINE);
        writer.write(OPEN_BRACK);
        writer.write(NEWLINE);
        writer.write("return false;");
        writer.write(NEWLINE);
        writer.write(CLOSED_BRACK);
        writer.write(NEWLINE);
        writer.write("else");
        writer.write(NEWLINE);
        writer.write(OPEN_BRACK);
        writer.write(NEWLINE);
        writer.write(tempPart.getName().toLowerCase()
            + "s.put(a, new Boolean(true));");
        writer.write(NEWLINE);
        writer.write("return true;");
        writer.write(NEWLINE);
        writer.write(CLOSED_BRACK);
        writer.write(NEWLINE);
        writer.write(CLOSED_BRACK);
        writer.write(NEWLINE);
        writer.write(NEWLINE);

        // "remove" method:
        writer.write("public boolean remove" + tempPart.getName() + "("
            + SimSEObjectTypeTypes.getText(tempPart.getSimSEObjectTypeType())
            + " a)");
        writer.write(NEWLINE);
        writer.write(OPEN_BRACK);
        writer.write(NEWLINE);
        writer.write("if(" + tempPart.getName().toLowerCase()
            + "s.containsKey(a))");
        writer.write(NEWLINE);
        writer.write(OPEN_BRACK);
        writer.write(NEWLINE);
        writer.write(tempPart.getName().toLowerCase() + "s.remove(a);");
        writer.write(NEWLINE);
        writer.write("return true;");
        writer.write(NEWLINE);
        writer.write(CLOSED_BRACK);
        writer.write(NEWLINE);
        writer.write("return false;");
        writer.write(NEWLINE);
        writer.write(CLOSED_BRACK);
        writer.write(NEWLINE);
        writer.write(NEWLINE);

        // "setActive" method:
        writer.write("public boolean set" + tempPart.getName() + "Active("
            + SimSEObjectTypeTypes.getText(tempPart.getSimSEObjectTypeType())
            + " a)");
        writer.write(NEWLINE);
        writer.write(OPEN_BRACK);
        writer.write(NEWLINE);
        writer.write("if(" + tempPart.getName().toLowerCase()
            + "s.containsKey(a))");
        writer.write(NEWLINE);
        writer.write(OPEN_BRACK);
        writer.write(NEWLINE);
        writer.write(tempPart.getName().toLowerCase()
            + "s.put(a, new Boolean(true));");
        writer.write(NEWLINE);
        writer.write("return true;");
        writer.write(NEWLINE);
        writer.write(CLOSED_BRACK);
        writer.write(NEWLINE);
        writer.write("return false;");
        writer.write(NEWLINE);
        writer.write(CLOSED_BRACK);
        writer.write(NEWLINE);
        writer.write(NEWLINE);

        // "setInactive" method:
        writer.write("public boolean set" + tempPart.getName() + "Inactive("
            + SimSEObjectTypeTypes.getText(tempPart.getSimSEObjectTypeType())
            + " a)");
        writer.write(NEWLINE);
        writer.write(OPEN_BRACK);
        writer.write(NEWLINE);
        writer.write("if(" + tempPart.getName().toLowerCase()
            + "s.containsKey(a))");
        writer.write(NEWLINE);
        writer.write(OPEN_BRACK);
        writer.write(NEWLINE);
        writer.write(tempPart.getName().toLowerCase()
            + "s.put(a, new Boolean(false));");
        writer.write(NEWLINE);
        writer.write("return true;");
        writer.write(NEWLINE);
        writer.write(CLOSED_BRACK);
        writer.write(NEWLINE);
        writer.write("return false;");
        writer.write(NEWLINE);
        writer.write(CLOSED_BRACK);
        writer.write(NEWLINE);
      }
      
      // "refetchParticipants" method:
    	writer.write("/*");
    	writer.write(NEWLINE);
    	writer.write("* Replaces all the participants in this action with their equivalent objects");
    	writer.write(NEWLINE);
    	writer.write("* in the current state. Calling this function solves the problem that happens");
    	writer.write(NEWLINE);
    	writer.write("* when you clone actions -- their hashtables point to participant objects");
    	writer.write(NEWLINE);
    	writer.write("* that were part of the previous, non-cloned state. Hence, this function");
    	writer.write(NEWLINE);
    	writer.write("* should be called after this object is cloned.");
    	writer.write(NEWLINE);
    	writer.write("*/");
    	writer.write(NEWLINE);
    	writer.write("public void refetchParticipants(ArtifactStateRepository artifactRep, CustomerStateRepository customerRep, EmployeeStateRepository employeeRep, ProjectStateRepository projectRep, ToolStateRepository toolRep) {");
    	writer.write(NEWLINE);
    	
    	// go through each participant:
      for (int i = 0; i < participants.size(); i++) {
        ActionTypeParticipant tempPart = participants.elementAt(i);
        String metaType = SimSEObjectTypeTypes.getText(
        		tempPart.getSimSEObjectTypeType());
        String hashtableTypeString = new String("<" + metaType + ", Boolean>");
        String partNameLowerCase = tempPart.getName().toLowerCase();
        writer.write("// " + partNameLowerCase + " participants:");
        writer.write(NEWLINE);
    		writer.write("Hashtable" + hashtableTypeString + " new" + 
    				CodeGeneratorUtils.getUpperCaseLeading(tempPart.getName()) + 
    				"s = new Hashtable" + hashtableTypeString + "();");
    		writer.write(NEWLINE);
    		writer.write("Iterator<Map.Entry" + hashtableTypeString + "> " + 
    				partNameLowerCase + "sIterator = " + partNameLowerCase + 
    				"s.entrySet().iterator();");
    		writer.write(NEWLINE);
    		writer.write("while (" + partNameLowerCase + "sIterator.hasNext()) {");
    		writer.write(NEWLINE);
    		writer.write("Map.Entry" + hashtableTypeString + " entry = " +
    				partNameLowerCase + "sIterator.next();");
    		writer.write(NEWLINE);
    		writer.write(metaType + " old" + 
    				CodeGeneratorUtils.getUpperCaseLeading(tempPart.getName()) + 
    				" = entry.getKey();");
    		writer.write(NEWLINE);
    		
    		// go through all allowable types for this participant:
        Vector<SimSEObjectType> types = tempPart.getAllSimSEObjectTypes();
        for (int j = 0; j < types.size(); j++) {
          if (j > 0) { // not on first element
            writer.write("else ");
          }
          writer.write("if (");
          SimSEObjectType tempType = types.elementAt(j);
          writer.write("old" + CodeGeneratorUtils.getUpperCaseLeading(
          		tempPart.getName()) + " instanceof " + 
          		CodeGeneratorUtils.getUpperCaseLeading(tempType.getName()) + 
          		") {");
          writer.write(NEWLINE);
          writer.write(metaType + " new" + 
          		CodeGeneratorUtils.getUpperCaseLeading(tempPart.getName()) + 
          		" = " + metaType.toLowerCase() + "Rep.get" + 
          		CodeGeneratorUtils.getUpperCaseLeading(tempType.getName()) + 
          		"StateRepository().get(((" + 
          		CodeGeneratorUtils.getUpperCaseLeading(tempType.getName()) + 
          		")old" + 
          		CodeGeneratorUtils.getUpperCaseLeading(tempPart.getName()) + 
          		").get" +
          		CodeGeneratorUtils.getUpperCaseLeading(
          				tempType.getKey().getName()) + "());");
          writer.write(NEWLINE);
          writer.write("Boolean activeStatus = " + partNameLowerCase + 
          		"s.get(old" + CodeGeneratorUtils.getUpperCaseLeading(
          				tempPart.getName()) + ");");
          writer.write(NEWLINE);
          writer.write("new" + CodeGeneratorUtils.getUpperCaseLeading(
          		tempPart.getName()) + "s.put(new" + 
          		CodeGeneratorUtils.getUpperCaseLeading(tempPart.getName()) +
          		", activeStatus);");
          writer.write(NEWLINE);
          writer.write(CLOSED_BRACK);
          writer.write(NEWLINE);
        }
        writer.write(CLOSED_BRACK);
        writer.write(NEWLINE);
        writer.write(partNameLowerCase + "s.clear();");
        writer.write(NEWLINE);
        writer.write(partNameLowerCase + "s.putAll(new" + 
        		CodeGeneratorUtils.getUpperCaseLeading(tempPart.getName()) + "s);");
        writer.write(NEWLINE);
        writer.write(NEWLINE);
      }
      writer.write(CLOSED_BRACK);
      writer.write(NEWLINE);
      
      writer.write(CLOSED_BRACK);
      writer.close();
    } catch (IOException e) {
      JOptionPane.showMessageDialog(null, ("Error writing file "
          + adtFile.getPath() + ": " + e.toString()), "File IO Error",
          JOptionPane.WARNING_MESSAGE);
    }
  }
}