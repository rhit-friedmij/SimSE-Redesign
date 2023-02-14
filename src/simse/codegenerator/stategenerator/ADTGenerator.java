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
import java.util.Enumeration;
import java.util.Hashtable;
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
	  ClassName customerClass = ClassName.get("simse.adts.objects", "Customer");
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
    	FileWriter writer = new FileWriter(objClass);
    	
    	javaFile.writeTo(writer);
    	writer.close();
    } catch (IOException e) {
        JOptionPane.showMessageDialog(null, ("Error writing file SSObject.java"),
            "File IO Error", JOptionPane.WARNING_MESSAGE);
    }

    // generate abstract object classes:
    generateAbstractObjectClass(SimSEObjectTypeTypes.ARTIFACT, objs);
    generateAbstractObjectClass(SimSEObjectTypeTypes.TOOL, objs);
    generateAbstractObjectClass(SimSEObjectTypeTypes.PROJECT, objs);

    // generate Employee class:
    File empClass = new File(options.getCodeGenerationDestinationDirectory(),
        ("simse\\adts\\objects\\Employee.java"));
    if (empClass.exists()) {
      empClass.delete(); // delete old version of file
    }
    
    Vector<SimSEObjectType> employeeTypes = new Vector<>();
    for (SimSEObjectType type: objs) {
    	if (type.getType() == SimSEObjectTypeTypes.EMPLOYEE) {
    		employeeTypes.add(type);
    	}
    }
    
    Vector<Attribute> compareAttributes = new Vector<>();
    if (employeeTypes.size() > 0) {
    	SimSEObjectType compareType = employeeTypes.get(0);
    	compareAttributes = compareType.getAllVisibleAttributes();
    	for (SimSEObjectType employee: employeeTypes) {
    		if (compareAttributes.size() == 0) {
    			break;
    		}
    		Vector<Attribute> toRemove = new Vector<>();
    		for (Attribute compare1: compareAttributes) {
    			boolean isShared = false;
    			for (Attribute compare2: employee.getAllVisibleAttributes()) {
    				if (isShared) break;
    				if (compare1.attributeEquals(compare2)) isShared = true;
    			}
    			
    			if (!isShared) toRemove.add(compare1);
    		}
    		
			for (Attribute remove: toRemove) {
				compareAttributes.remove(remove);
			}
    	}
	}
    
    MethodSpec.Builder employeeConstructorBuilder = MethodSpec.constructorBuilder()
    		.addStatement("$N = new $T()", "menu", stringVector)
    		.addStatement("clearMenu()")
    		.addStatement("$N = new $T()", "overheadText", String.class)
    		.addStatement("$N = $T.getInstance()", "track", trackClass);
    
    for (Attribute compare: compareAttributes) {
    	employeeConstructorBuilder.addParameter(getTypeAsClass(compare), compare.getName())
    			.addStatement("this.$N = $N", compare.getName(), compare.getName());
    }
    
    MethodSpec employeeConstructor = employeeConstructorBuilder.build();
    
    MethodSpec.Builder clone2Builder = MethodSpec.methodBuilder("clone")
    		.returns(Object.class)
    		.addModifiers(Modifier.PUBLIC)
    		.addStatement("$T cl = ($T) (super.clone())", employeeClass, employeeClass)
    		.addStatement("$T clonedMenu = new $T()", stringVector, stringVector)
    		.beginControlFlow("for (int i = 0; i < $N.size(); i++)", "menu")
    		.addStatement("clonedMenu.add($N.elementAt(i))", "menu")
    		.endControlFlow()
    		.addStatement("cl.$N = clonedMenu", "menu")
    		.addStatement("cl.$N = $N", "overheadText", "overheadText");
    
    for (Attribute compare: compareAttributes) {
    	clone2Builder.addStatement("cl.$N = $N", compare.getName(),	compare.getName());
    }
    	
    MethodSpec clone2 = clone2Builder.addStatement("return cl")
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
    		.addField(trackClass, "track", Modifier.PRIVATE);

    	
	for (Attribute compare: compareAttributes) {
		employeeBuilder.addField(this.getTypeAsClass(compare), compare.getName(), Modifier.PRIVATE);
	}
    
    
    employeeBuilder.addMethod(employeeConstructor)
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
    
    for (Attribute compare: compareAttributes) {
    	employeeBuilder.addMethod(MethodSpec.methodBuilder("get" + compare.getName())
    			.addModifiers(Modifier.PUBLIC)
    			.returns(getTypeAsClass(compare))
    			.addStatement("return $N", compare.getName())
    			.build());
    	
    	MethodSpec.Builder tempSetBuilder = MethodSpec.methodBuilder("set" + compare.getName())
    			.addModifiers(Modifier.PUBLIC)
    			.returns(void.class);
    	
    	if ((compare instanceof NumericalAttribute)
                && (((NumericalAttribute) compare).isMinBoundless() == false)) { // has min val
            	NumericalAttribute numAtt = (NumericalAttribute)compare;
              String minVal = (numAtt).getMinValue().toString();
              tempSetBuilder.beginControlFlow("if (a < $L)", minVal)
              				.addStatement("$L = $L", compare.getName().toLowerCase(), minVal);
              
              if ((numAtt).isMaxBoundless() == false) { // has a max value
                String maxVal = (numAtt).getMaxValue().toString();
          	  tempSetBuilder.nextControlFlow("else if (a > $L)", maxVal)
          	  				.addStatement("$L = $L", compare.getName().toLowerCase(), maxVal);
              }
              
              tempSetBuilder.nextControlFlow("else")
              				.addStatement("$L = a", compare.getName().toLowerCase())
              				.endControlFlow();
        } else if ((compare instanceof NumericalAttribute)
                && (((NumericalAttribute) compare).isMaxBoundless() == false)) { // has max val
            	NumericalAttribute numAtt = (NumericalAttribute)compare;
              String maxVal = (numAtt).getMaxValue().toString();
              tempSetBuilder.beginControlFlow("if (a > $L)", maxVal)
              				.addStatement("$L = $L", compare.getName().toLowerCase(), maxVal);
              if ((numAtt).isMinBoundless() == false) { // has a min value
                String minVal = (numAtt).getMinValue().toString();
                tempSetBuilder.nextControlFlow("else if (a < $L)", minVal)
                			.addStatement("$L = $L", compare.getName().toLowerCase(), minVal);
              }
              tempSetBuilder.nextControlFlow("else")
              				.addStatement("$L = a", compare.getName().toLowerCase())
              				.endControlFlow();
        } else {
        	tempSetBuilder.addStatement("$L = a", compare.getName().toLowerCase());
        }
    	
    	employeeBuilder.addMethod(tempSetBuilder.build());
    }
    
    TypeSpec employee = employeeBuilder.build();
    
    javaFile = JavaFile.builder("Employee", employee)
		    .build();
    try {
    	FileWriter writer = new FileWriter(empClass);
    	
    	javaFile.writeTo(writer);
    	writer.close();
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
    
    Vector<SimSEObjectType> customerTypes = new Vector<>();
    for (SimSEObjectType type: objs) {
    	if (type.getType() == SimSEObjectTypeTypes.CUSTOMER) {
    		employeeTypes.add(type);
    	}
    }
    
    compareAttributes = new Vector<>();
    if (customerTypes.size() > 0) {
    	SimSEObjectType compareType = customerTypes.get(0);
    	compareAttributes = compareType.getAllVisibleAttributes();
    	for (SimSEObjectType customer: customerTypes) {
    		if (compareAttributes.size() == 0) {
    			break;
    		}
    		Vector<Attribute> toRemove = new Vector<>();
    		for (Attribute compare1: compareAttributes) {
    			boolean isShared = false;
    			for (Attribute compare2: customer.getAllVisibleAttributes()) {
    				if (isShared) break;
    				if (compare1.attributeEquals(compare2)) isShared = true;
    			}
    			
    			if (!isShared) toRemove.add(compare1);
    		}
    		
    		for (Attribute remove: toRemove) {
    			compareAttributes.remove(remove);
    		}
    	}
    }
    
    MethodSpec.Builder customerConstructorBuilder = MethodSpec.constructorBuilder()
    		.addStatement("$N = new $T()", "overheadText", String.class)
    		.addStatement("$N = $T.getInstance()", "track", trackClass);
    
    for (Attribute compare: compareAttributes) {
		customerConstructorBuilder.addParameter(getTypeAsClass(compare), compare.getName())
								.addStatement("this.$N = $N", compare.getName(), 
										compare.getName());
    }
    
    MethodSpec customerConstructor = customerConstructorBuilder.build();
    
    MethodSpec.Builder clone3Builder = MethodSpec.methodBuilder("clone")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(Object.class)
    		.addStatement("$T cl = ($T) (super.clone())", customerClass, customerClass)
    		.addStatement("cl.$N = $N", "overheadText", "overheadText");
    
    for (Attribute compare: compareAttributes) {
		customerConstructorBuilder.addStatement("cl.$N = $N", compare.getName(), 
										compare.getName());
    }
    
    MethodSpec clone3 = clone3Builder.addStatement("return cl").build();
    
    MethodSpec getOverheadText2 = MethodSpec.methodBuilder("getOverheadText")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(String.class)
    		.addStatement("$T temp = $N", String.class, "overheadText")
    		.addStatement("$N = new $T()", "overheadText", String.class)
    		.addStatement("return temp")
    		.build();
    
    MethodSpec setOverheadText3 = MethodSpec.methodBuilder("setOverheadText")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(void.class)
    		.addParameter(String.class, "s")
    		.addStatement("$N = s", "overheadText")
    		.build();
    
    MethodSpec setOverheadText4 = MethodSpec.methodBuilder("setOverheadText")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(void.class)
    		.addParameter(String.class, "s")
    		.addParameter(String.class, "name")
    		.addStatement("$N.addText(s, name)", "track")
    		.build();
    
    MethodSpec hasOverheadText2 = MethodSpec.methodBuilder("hasOverheadText")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(boolean.class)
    		.beginControlFlow("if ($N == null)", "overheadText")
    		.addStatement("return false")
    		.nextControlFlow("else")
    		.addStatement("return true")
    		.endControlFlow()
    		.build();
    
    TypeSpec.Builder customerBuilder = TypeSpec.classBuilder("Customer")
    		.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
    		.superclass(ssObjectClass)
    		.addSuperinterface(Cloneable.class)
    		.addField(String.class, "overheadText")
    		.addField(trackClass, "track");
    
	for (Attribute compare: compareAttributes) {
		customerBuilder.addField(this.getTypeAsClass(compare), compare.getName(), Modifier.PRIVATE);
	}
	
	customerBuilder.addMethod(customerConstructor)
					.addMethod(clone3)
					.addMethod(setOverheadText3)
					.addMethod(setOverheadText4)
					.addMethod(getOverheadText2)
					.addMethod(hasOverheadText2);
	
	
	for (Attribute compare: compareAttributes) {
    	customerBuilder.addMethod(MethodSpec.methodBuilder("get" + compare.getName())
    			.addModifiers(Modifier.PUBLIC)
    			.returns(getTypeAsClass(compare))
    			.addStatement("return $N", compare.getName())
    			.build());
    	
    	MethodSpec.Builder tempSetBuilder = MethodSpec.methodBuilder("set" + compare.getName())
    			.addModifiers(Modifier.PUBLIC)
    			.returns(void.class);
    	
    	if ((compare instanceof NumericalAttribute)
                && (((NumericalAttribute) compare).isMinBoundless() == false)) { // has min val
            	NumericalAttribute numAtt = (NumericalAttribute)compare;
              String minVal = (numAtt).getMinValue().toString();
              tempSetBuilder.beginControlFlow("if (a < $L)", minVal)
              				.addStatement("$L = $L", compare.getName().toLowerCase(), minVal);
              
              if ((numAtt).isMaxBoundless() == false) { // has a max value
                String maxVal = (numAtt).getMaxValue().toString();
          	  tempSetBuilder.nextControlFlow("else if (a > $L)", maxVal)
          	  				.addStatement("$L = $L", compare.getName().toLowerCase(), maxVal);
              }
              
              tempSetBuilder.nextControlFlow("else")
              				.addStatement("$L = a", compare.getName().toLowerCase())
              				.endControlFlow();
        } else if ((compare instanceof NumericalAttribute)
                && (((NumericalAttribute) compare).isMaxBoundless() == false)) { // has max val
            	NumericalAttribute numAtt = (NumericalAttribute)compare;
              String maxVal = (numAtt).getMaxValue().toString();
              tempSetBuilder.beginControlFlow("if (a > $L)", maxVal)
              				.addStatement("$L = $L", compare.getName().toLowerCase(), maxVal);
              if ((numAtt).isMinBoundless() == false) { // has a min value
                String minVal = (numAtt).getMinValue().toString();
                tempSetBuilder.nextControlFlow("else if (a < $L)", minVal)
                			.addStatement("$L = $L", compare.getName().toLowerCase(), minVal);
              }
              tempSetBuilder.nextControlFlow("else")
              				.addStatement("$L = a", compare.getName().toLowerCase())
              				.endControlFlow();
        } else {
        	tempSetBuilder.addStatement("$L = a", compare.getName().toLowerCase());
        }
    	
    	customerBuilder.addMethod(tempSetBuilder.build());
    }
	
	TypeSpec customer = customerBuilder.build();
	
	javaFile = JavaFile.builder("Customer", customer)
		    .build();
    
    try {
    	FileWriter writer = new FileWriter(custClass);
    	
    	javaFile.writeTo(writer);
    	writer.close();
    } catch (IOException e) {
      JOptionPane.showMessageDialog(null, ("Error writing file "
          + custClass.getPath() + ": " + e.toString()), "File IO Error",
          JOptionPane.WARNING_MESSAGE);
    }

    // go through each object and generate code for it:
    for (int i = 0; i < objs.size(); i++) {
      generateObjectADT(objs.elementAt(i), objs);
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
    
    MethodSpec clone4 = MethodSpec.methodBuilder("clone")
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
    		.addMethod(clone4)
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
    	FileWriter writer = new FileWriter(actClass);
    	
    	javaFile.writeTo(writer);
    	writer.close();
    } catch (IOException e) {
        JOptionPane.showMessageDialog(null, ("Error writing file " + actClass
            .getPath()), "File IO Error", JOptionPane.WARNING_MESSAGE);
      }

    // go through each action and generate code for it:
    for (int i = 0; i < acts.size(); i++) {
      generateActionADT(acts.elementAt(i));
    }
  }

  private void generateAbstractObjectClass(int classType, Vector<SimSEObjectType> objs) {
	  String className = SimSEObjectTypeTypes.getText(classType);
	 ClassName ssObjectClass = ClassName.get("simse.adts.objects", "SSObject");
	 ClassName objClass = ClassName.get("simse.adts.objects", className);
   
	 File absClass = new File(options.getCodeGenerationDestinationDirectory(),
        ("simse\\adts\\objects\\" + className + ".java"));
    if (absClass.exists()) {
      absClass.delete(); // delete old version of file
    }
    
    Vector<SimSEObjectType> objSpecificTypes = new Vector<>();
    for (SimSEObjectType type: objs) {
    	if (type.getType() == classType) {
    		objSpecificTypes.add(type);
    	}
    }
    
    Vector<Attribute> compareAttributes = new Vector<>();
    if (objSpecificTypes.size() > 0) {
    	SimSEObjectType compareType = objSpecificTypes.get(0);
    	compareAttributes = compareType.getAllVisibleAttributes();
    	for (SimSEObjectType obj: objSpecificTypes) {
    		if (compareAttributes.size() == 0) {
    			break;
    		}
    		Vector<Attribute> toRemove = new Vector<>();
    		for (Attribute compare1: compareAttributes) {
    			boolean isShared = false;
    			for (Attribute compare2: obj.getAllVisibleAttributes()) {
    				if (isShared) break;
    				if (compare1.attributeEquals(compare2)) isShared = true;
    			}
    			
    			if (!isShared) toRemove.add(compare1);
    		}
    		
			for (Attribute remove: toRemove) {
				compareAttributes.remove(remove);
			}
    	}
	}
    
    MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder();
    
    for (Attribute compare: compareAttributes) {
    	constructorBuilder.addParameter(getTypeAsClass(compare), compare.getName())
    			.addStatement("this.$N = $N", compare.getName(), compare.getName());
    }
    
    MethodSpec constructor = constructorBuilder.build();
    
    MethodSpec.Builder cloneBuilder = MethodSpec.methodBuilder("clone")
    		.returns(Object.class)
    		.addModifiers(Modifier.PUBLIC)
    		.addStatement("$T cl = ($T) (super.clone())", objClass, objClass);
    
    for (Attribute compare: compareAttributes) {
    	cloneBuilder.addStatement("cl.$N = $N", compare.getName(),	compare.getName());
    }
    	
    MethodSpec clone = cloneBuilder.addStatement("return cl")
    		.build();
    
    
    TypeSpec.Builder absClassSpecBuilder = TypeSpec.classBuilder(className)
    		.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
    		.addSuperinterface(Cloneable.class)
    		.superclass(ssObjectClass);
    
	for (Attribute compare: compareAttributes) {
		absClassSpecBuilder.addField(getTypeAsClass(compare), compare.getName(),
				Modifier.PRIVATE);
	}
	
	absClassSpecBuilder.addMethod(constructor)
						.addMethod(clone);
	
	for (Attribute compare: compareAttributes) {
		absClassSpecBuilder.addMethod(MethodSpec.methodBuilder("get" + compare.getName())
    			.addModifiers(Modifier.PUBLIC)
    			.returns(getTypeAsClass(compare))
    			.addStatement("return $N", compare.getName())
    			.build());
    	
    	MethodSpec.Builder tempSetBuilder = MethodSpec.methodBuilder("set" + compare.getName())
    			.addModifiers(Modifier.PUBLIC)
    			.returns(void.class);
    	
    	if ((compare instanceof NumericalAttribute)
                && (((NumericalAttribute) compare).isMinBoundless() == false)) { // has min val
            	NumericalAttribute numAtt = (NumericalAttribute)compare;
              String minVal = (numAtt).getMinValue().toString();
              tempSetBuilder.beginControlFlow("if (a < $L)", minVal)
              				.addStatement("$L = $L", compare.getName().toLowerCase(), minVal);
              
              if ((numAtt).isMaxBoundless() == false) { // has a max value
                String maxVal = (numAtt).getMaxValue().toString();
          	  tempSetBuilder.nextControlFlow("else if (a > $L)", maxVal)
          	  				.addStatement("$L = $L", compare.getName().toLowerCase(), maxVal);
              }
              
              tempSetBuilder.nextControlFlow("else")
              				.addStatement("$L = a", compare.getName().toLowerCase())
              				.endControlFlow();
        } else if ((compare instanceof NumericalAttribute)
                && (((NumericalAttribute) compare).isMaxBoundless() == false)) { // has max val
            	NumericalAttribute numAtt = (NumericalAttribute)compare;
              String maxVal = (numAtt).getMaxValue().toString();
              tempSetBuilder.beginControlFlow("if (a > $L)", maxVal)
              				.addStatement("$L = $L", compare.getName().toLowerCase(), maxVal);
              if ((numAtt).isMinBoundless() == false) { // has a min value
                String minVal = (numAtt).getMinValue().toString();
                tempSetBuilder.nextControlFlow("else if (a < $L)", minVal)
                			.addStatement("$L = $L", compare.getName().toLowerCase(), minVal);
              }
              tempSetBuilder.nextControlFlow("else")
              				.addStatement("$L = a", compare.getName().toLowerCase())
              				.endControlFlow();
        } else {
        	tempSetBuilder.addStatement("$L = a", compare.getName().toLowerCase());
        }
    	
    	absClassSpecBuilder.addMethod(tempSetBuilder.build());
    }
    
	TypeSpec absClassSpec = absClassSpecBuilder.build();
	
	JavaFile javaFile = JavaFile.builder(className, absClassSpec)
		    .build();
	
    try {
    	FileWriter writer = new FileWriter(absClass);
    	
    	javaFile.writeTo(writer);
    	writer.close();
    } catch (IOException e) {
        JOptionPane.showMessageDialog(null, ("Error writing file "
            + absClass.getPath() + ": " + e.toString()), "File IO Error",
            JOptionPane.WARNING_MESSAGE);
    }
  }

  private void generateObjectADT(SimSEObjectType objType, Vector<SimSEObjectType> objs) {
	  String name =  CodeGeneratorUtils.getUpperCaseLeading(objType.getName());
	  ClassName vectorClass = ClassName.get("java.util", "Vector");
	  ClassName superClass = ClassName.get("simse.adts.objects",
				SimSEObjectTypeTypes.getText(objType.getType()));
	  ClassName thisClass = ClassName.get("simse.adts.objects", name);
	  
    File adtFile = new File(options.getCodeGenerationDestinationDirectory(), 
        ("simse\\adts\\objects\\" + CodeGeneratorUtils.getUpperCaseLeading(
        		objType.getName()) + ".java"));
    if (adtFile.exists()) {
      adtFile.delete(); // delete old version of file
    }
    
    Vector<Attribute> attributes = objType.getAllAttributes();
    
    Vector<SimSEObjectType> objSpecificTypes = new Vector<>();
    for (SimSEObjectType type: objs) {
    	if (type.getType() == objType.getType()) {
    		objSpecificTypes.add(type);
    	}
    }
    
    Vector<Attribute> compareAttributes = new Vector<>();
    if (objSpecificTypes.size() > 0) {
    	SimSEObjectType compareType = objSpecificTypes.get(0);
    	compareAttributes = compareType.getAllVisibleAttributes();
    	for (SimSEObjectType obj: objSpecificTypes) {
    		if (compareAttributes.size() == 0) {
    			break;
    		}
    		Vector<Attribute> toRemove = new Vector<>();
    		for (Attribute compare1: compareAttributes) {
    			boolean isShared = false;
    			for (Attribute compare2: obj.getAllVisibleAttributes()) {
    				if (isShared) break;
    				if (compare1.attributeEquals(compare2)) isShared = true;
    			}
    			
    			if (!isShared) toRemove.add(compare1);
    		}
    		
			for (Attribute remove: toRemove) {
				compareAttributes.remove(remove);
			}
    	}
	}
    
    MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder();

    Vector<String> superConstructorAtts = new Vector<>();
    for (int i = 0; i < attributes.size(); i++) {
        Attribute att = attributes.elementAt(i);
        for (Attribute compare: compareAttributes) {
        	if (att.attributeEquals(compare)) {
        		superConstructorAtts.add(att.getName().substring(0, 1).toLowerCase() + i);
        	}
        }
    }
    
    String superConstructor = "super(";
    for (int i = 0; i < superConstructorAtts.size() - 1; i++) {
    	superConstructor += superConstructorAtts.get(i) + ", ";
    }
    if (superConstructorAtts.size() > 0) {
    	superConstructor += superConstructorAtts.get(superConstructorAtts.size() - 1);
    }
    superConstructor += ");";
    
    constructorBuilder.addCode(superConstructor);
    		
    for (int i = 0; i < attributes.size(); i++) {
        Attribute att = attributes.elementAt(i);
        constructorBuilder.addParameter(getTypeAsClass(att), att.getName().substring(0, 1)
        		.toLowerCase() + i);
        constructorBuilder.addStatement("set$L($L)", CodeGeneratorUtils.getUpperCaseLeading(
        		att.getName()), att.getName().substring(0, 1).toLowerCase() + i);
    }
    
    MethodSpec constructor = constructorBuilder.build();
    
    MethodSpec.Builder cloneBuilder = MethodSpec.methodBuilder("clone")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(Object.class)
    		.addStatement("$T cl = ($T) (super.clone())", thisClass, thisClass);
    
    for (int i = 0; i < attributes.size(); i++) {
        Attribute att = attributes.elementAt(i);
        cloneBuilder.addStatement("cl.$L = $L", att.getName().toLowerCase(),
        		att.getName().toLowerCase());
      }
    
    MethodSpec clone = cloneBuilder.addStatement("return cl").build();
    
    TypeSpec.Builder adtBuilder = TypeSpec.classBuilder(name)
    		.addModifiers(Modifier.PUBLIC)
    		.superclass(superClass)
    		.addSuperinterface(Cloneable.class);
    
    for (int i = 0; i < attributes.size(); i++) {
        Attribute att = attributes.elementAt(i);
        adtBuilder.addField(getTypeAsClass(att), att.getName().toLowerCase(),
        		Modifier.PRIVATE);
    }
    
    adtBuilder.addMethod(constructor).addMethod(clone);
    
    for (Attribute att: attributes) {
		adtBuilder.addMethod(MethodSpec.methodBuilder("get" + att.getName())
    			.addModifiers(Modifier.PUBLIC)
    			.returns(getTypeAsClass(att))
    			.addStatement("return $N", att.getName())
    			.build());
    	
    	MethodSpec.Builder tempSetBuilder = MethodSpec.methodBuilder("set" + att.getName())
    			.addModifiers(Modifier.PUBLIC)
    			.returns(void.class);
    	
    	if ((att instanceof NumericalAttribute)
                && (((NumericalAttribute) att).isMinBoundless() == false)) { // has min val
            	NumericalAttribute numAtt = (NumericalAttribute) att;
              String minVal = (numAtt).getMinValue().toString();
              tempSetBuilder.beginControlFlow("if (a < $L)", minVal)
              				.addStatement("$L = $L", att.getName().toLowerCase(), minVal);
              
              if ((numAtt).isMaxBoundless() == false) { // has a max value
                String maxVal = (numAtt).getMaxValue().toString();
          	  tempSetBuilder.nextControlFlow("else if (a > $L)", maxVal)
          	  				.addStatement("$L = $L", att.getName().toLowerCase(), maxVal);
              }
              
              tempSetBuilder.nextControlFlow("else")
              				.addStatement("$L = a", att.getName().toLowerCase())
              				.endControlFlow();
        } else if ((att instanceof NumericalAttribute)
                && (((NumericalAttribute) att).isMaxBoundless() == false)) { // has max val
            	NumericalAttribute numAtt = (NumericalAttribute) att;
              String maxVal = (numAtt).getMaxValue().toString();
              tempSetBuilder.beginControlFlow("if (a > $L)", maxVal)
              				.addStatement("$L = $L", att.getName().toLowerCase(), maxVal);
              if ((numAtt).isMinBoundless() == false) { // has a min value
                String minVal = (numAtt).getMinValue().toString();
                tempSetBuilder.nextControlFlow("else if (a < $L)", minVal)
                			.addStatement("$L = $L", att.getName().toLowerCase(), minVal);
              }
              tempSetBuilder.nextControlFlow("else")
              				.addStatement("$L = a", att.getName().toLowerCase())
              				.endControlFlow();
        } else {
        	tempSetBuilder.addStatement("$L = a", att.getName().toLowerCase());
        }
    	
    	adtBuilder.addMethod(tempSetBuilder.build());
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

          MethodSpec getMenu = MethodSpec.methodBuilder("getMenu")
        		  .addModifiers(Modifier.PUBLIC)
        		  .returns(vectorClass)
        		  .addStatement("$T v = new $T()", vectorClass, vectorClass)
        		  .addStatement("v.addAll(super.getMenu())")
        		  .beginControlFlow("if (getHired())")
        		  .addStatement("v.add($S + get$L())", "Fire Employee - ", 
        				  CodeGeneratorUtils.getUpperCaseLeading(keyAtt.getName()))
        		  .nextControlFlow("else")
        		  .addStatement("v = new $T()", vectorClass)
        		  .addStatement("v.add($S + get$L()", "Hire Employee - ", 
        				  CodeGeneratorUtils.getUpperCaseLeading(keyAtt.getName()))
        		  .endControlFlow()
        		  .addStatement("return v")
        		  .build();

          adtBuilder.addMethod(getMenu);
    }
    
    TypeSpec adt = adtBuilder.build();
    
    JavaFile javaFile = JavaFile.builder(name, adt)
		    .build();
    
    try {
    	FileWriter writer = new FileWriter(adtFile);
    	
        javaFile.writeTo(writer);
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
  
  private Class getTypeAsClass(Attribute att) {
	    if (att.getType() == AttributeTypes.INTEGER) {
	      return int.class;
	    } else if (att.getType() == AttributeTypes.DOUBLE) {
	      return double.class;
	    } else if (att.getType() == AttributeTypes.BOOLEAN) {
	      return boolean.class;
	    } else { //(att.getType() == AttributeTypes.STRING)
	      return String.class;
	    }
  }

  private void generateActionADT(ActionType actType) {
	  String name = CodeGeneratorUtils.getUpperCaseLeading(actType.getName()) + "Action";
	  ClassName toolStateRepoClass = ClassName.get("simse.state", "ToolStateRepository");
	  ClassName artifactStateRepoClass = ClassName.get("simse.state", "ArtifactStateRepository");
	  ClassName customerStateRepoClass = ClassName.get("simse.state", "CustomerStateRepository");
	  ClassName employeeStateRepoClass = ClassName.get("simse.state", "EmployeeStateRepository");
	  ClassName projectStateRepoClass = ClassName.get("simse.state", "ProjectStateRepository");
	  ClassName ssObjectClass = ClassName.get("simse.adts.objects", "SSObject");
	  ClassName actionClass = ClassName.get("simse.adts.actions", "Action");
	  ClassName thisClass = ClassName.get("simse.adts.actions", name);
	  ClassName vector = ClassName.get("java.util", "Vector");
	  ClassName hashtable = ClassName.get("java.util", "Hashtable");
	  ClassName enumeration = ClassName.get("java.util", "Enumeration");
	  ClassName booleanClass = ClassName.get(Boolean.class);
	  TypeName ssObjectVector = ParameterizedTypeName.get(vector, ssObjectClass);
    File adtFile = new File(options.getCodeGenerationDestinationDirectory(), 
        ("simse\\adts\\actions\\" + CodeGeneratorUtils.getUpperCaseLeading(
        		actType.getName()) + "Action.java"));
    if (adtFile.exists()) {
      adtFile.delete(); // delete old version of file
    }
    Vector<ActionTypeParticipant> participants = actType.getAllParticipants();
    
    boolean hasTimedDestroyer = false;
    Vector<ActionTypeDestroyer> allDests = actType.getAllDestroyers();
    for (int i = 0; i < allDests.size(); i++) {
      ActionTypeDestroyer tempDest = allDests.elementAt(i);
      if (tempDest instanceof TimedActionTypeDestroyer) {
        hasTimedDestroyer = true;
        break;
      }
    }
    
    MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder();

    for (int i = 0; i < participants.size(); i++) {
        ActionTypeParticipant tempPart = participants.elementAt(i);
        ClassName tempClass = ClassName.get("simse.adts.objects", SimSEObjectTypeTypes.getText(
        		tempPart.getSimSEObjectTypeType()));
        TypeName tempType = ParameterizedTypeName.get(hashtable, tempClass, booleanClass);
        constructorBuilder.addStatement("$Ls = new $T()", tempPart.getName().toLowerCase(),
        		tempType);
    }
    
    if (hasTimedDestroyer) { // timed destroyer
        // find the timed destroyer:
    	for (int j = 0; j < allDests.size(); j++) {
          ActionTypeDestroyer tempDest = allDests.elementAt(j);
          if (tempDest instanceof TimedActionTypeDestroyer) {
        	  constructorBuilder.addStatement("timeToLive = $L", 
        			  ((TimedActionTypeDestroyer) tempDest).getTime());
            break;
          }
        }
    }
    
    MethodSpec constructor = constructorBuilder.addStatement("actionName = $T.$L", 
    		actionClass, actType.getName().toUpperCase())
    		.build();
    
    MethodSpec.Builder cloneBuilder = MethodSpec.methodBuilder("clone")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(Object.class)
    		.addStatement("$T cl = ($T) (super.clone)", thisClass, thisClass);
    
    for (int i = 0; i < participants.size(); i++) {
        ActionTypeParticipant tempPart = participants.elementAt(i);
        ClassName tempClass = ClassName.get("simse.adts.objects", SimSEObjectTypeTypes.getText(
        		tempPart.getSimSEObjectTypeType()));
        TypeName tempType = ParameterizedTypeName.get(hashtable, tempClass, booleanClass);
        cloneBuilder.addStatement("$T cloned$Ls = new $T()", tempType,
        				tempPart.getName().toLowerCase(), tempType)
        			.addStatement("cloned$Ls.putAll($Ls)", tempPart.getName().toLowerCase(),
        					tempPart.getName().toLowerCase())
        			.addStatement("cl.$Ls = cloned$Ls", tempPart.getName().toLowerCase(), 
        					tempPart.getName().toLowerCase());
    }
    
    MethodSpec clone = cloneBuilder.addStatement("return cl").build();
    
    MethodSpec.Builder getAllParticipantsBuilder = MethodSpec.methodBuilder("getAllParticipants")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(ssObjectVector)
    		.addStatement("$T all = new $T()", ssObjectVector, ssObjectVector);
 
    for (int i = 0; i < participants.size(); i++) {
      ActionTypeParticipant tempPart = participants.elementAt(i);
      getAllParticipantsBuilder.addStatement("all.addAll(getAll$Ls())",  tempPart.getName());
    }
    
    MethodSpec getAllParticipants = getAllParticipantsBuilder.addStatement("return all").build();
    
    MethodSpec.Builder getAllActiveParticipantsBuilder = MethodSpec.methodBuilder("getAllActiveParticipants")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(ssObjectVector)
    		.addStatement("$T all = new $T()", ssObjectVector, ssObjectVector);
 
    for (int i = 0; i < participants.size(); i++) {
      ActionTypeParticipant tempPart = participants.elementAt(i);
      getAllActiveParticipantsBuilder.addStatement("all.addAll(getAllActive$Ls())",  tempPart.getName());
    }
    
    MethodSpec getAllActiveParticipants = getAllParticipantsBuilder.addStatement("return all").build();
    
    MethodSpec.Builder getAllInactiveParticipantsBuilder = MethodSpec.methodBuilder("getAllParticipants")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(ssObjectVector)
    		.addStatement("$T all = new $T()", ssObjectVector, ssObjectVector);
 
    for (int i = 0; i < participants.size(); i++) {
      ActionTypeParticipant tempPart = participants.elementAt(i);
      getAllInactiveParticipantsBuilder.addStatement("all.addAll(getAllInactive$Ls())",  tempPart.getName());
    }
    
    MethodSpec getAllInactiveParticipants = getAllParticipantsBuilder.addStatement("return all").build();
    
    // "refetchParticipants" method:
    MethodSpec refetchParticipants = MethodSpec.methodBuilder("refetchParticipants")
    		.addComment("Replaces all the participants in this action with their equivalent\r\n"
    				+ "objects in the current state. Calling this function solves the problem\r\n"
    				+ "that happens when you clone actions -- their hashtables point to\r\n"
    				+ "participant objects that were part of the previous, non-cloned state.\r\n"
    				+ "Hence, this function should be called after this object is cloned.")
    		.addModifiers(Modifier.PUBLIC)
    		.returns(void.class)
    		.addParameter(artifactStateRepoClass, "artifactRep")
    		.addParameter(customerStateRepoClass, "customerRep")
    		.addParameter(employeeStateRepoClass, "employeeRep")
    		.addParameter(projectStateRepoClass, "projectRep")
    		.addParameter(toolStateRepoClass, "toolRep")
    		.addCode(generateRefetchParticipants(participants))
    		.build();
    
    TypeSpec.Builder adtBuilder = TypeSpec.classBuilder(name)
    		.addModifiers(Modifier.PUBLIC)
    		.superclass(actionClass)
    		.addSuperinterface(Cloneable.class);
    
    // member variables/attributes:
    for (int i = 0; i < participants.size(); i++) {
      ActionTypeParticipant tempPart = participants.elementAt(i);
      ClassName tempClass = ClassName.get("simse.adts.objects", SimSEObjectTypeTypes.getText(
      		tempPart.getSimSEObjectTypeType()));
      TypeName tempType = ParameterizedTypeName.get(hashtable, tempClass, booleanClass);
      adtBuilder.addField(tempType, tempPart.getName().toLowerCase() + "s", Modifier.PRIVATE);
    }

    if (hasTimedDestroyer) { // timed destroyer
      // give it a timeToLive member variable:
      adtBuilder.addField(int.class, "timeToLive", Modifier.PRIVATE);
    }
    
    adtBuilder.addMethod(constructor).addMethod(clone);
    
    if (hasTimedDestroyer) { // timed destroyer
        // "getTimeToLive" method:
    	MethodSpec getTimeToLive = MethodSpec.methodBuilder("getTimeToLive")
    			.addModifiers(Modifier.PUBLIC)
    			.returns(int.class)
    			.addStatement("return $N", "timeToLive")
    			.build();
    	
    	MethodSpec decTimeToLive = MethodSpec.methodBuilder("decrementTimeToLive")
    			.addModifiers(Modifier.PUBLIC)
    			.returns(void.class)
    			.addStatement("$N--", "timeToLive")
    			.beginControlFlow("if ($N < 0)", "timeToLive")
    			.addStatement("$N = 0", "timeToLive")
    			.endControlFlow()
    			.build();
    	
    	adtBuilder.addMethod(getTimeToLive).addMethod(decTimeToLive);
      }
    
    adtBuilder.addMethod(getAllParticipants)
    			.addMethod(getAllActiveParticipants)
    			.addMethod(getAllInactiveParticipants);
    
    for (int i = 0; i < participants.size(); i++) {
        ActionTypeParticipant tempPart = participants.elementAt(i);
        String lcName = tempPart.getName().toLowerCase();
        ClassName tempClass = ClassName.get("simse.adts.objects", SimSEObjectTypeTypes.getText(
        		tempPart.getSimSEObjectTypeType()));
        TypeName partVector = ParameterizedTypeName.get(vector, tempClass);
        TypeName partEnum = ParameterizedTypeName.get(enumeration, tempClass);

        // "getAll[Participant]s" method:
        MethodSpec getAll = MethodSpec.methodBuilder("getAll" + tempPart.getName() + "s")
        		.addStatement("$T a = new $T()", partVector, partVector)
        		.addStatement("$T e = $Ls.keys()", partEnum, lcName)
        		.beginControlFlow("for (int i = 0; i < $Ls.size(); i++)", lcName)
        		.addStatement("a.add(e.nextElement())")
        		.endControlFlow()
        		.addStatement("return a")
        		.build();

        // "getAllActive[Participant]s" method:
        MethodSpec getAllActive = MethodSpec.methodBuilder("getAllActive" + tempPart.getName() + "s")
        		.addStatement("$T a = new $T()", partVector, partVector)
        		.addStatement("$T e = $Ls.keys()", partEnum, lcName)
        		.beginControlFlow("for (int i = 0; i < $Ls.size(); i++)", lcName)
        		.addStatement("$T key = e.nextElement()", tempClass)
        		.beginControlFlow("if (($Ls.get(key)).booleanValue() == true)", lcName)
        		.addStatement("a.add(key)")
        		.endControlFlow()
        		.endControlFlow()
        		.addStatement("return a")
        		.build();

        // "getAllInactive[Participant]s" method:
        MethodSpec getAllInActive = MethodSpec.methodBuilder("getAllInActive" + tempPart.getName() + "s")
        		.addStatement("$T a = new $T()", partVector, partVector)
        		.addStatement("$T e = $Ls.keys()", partEnum, lcName)
        		.beginControlFlow("for (int i = 0; i < $Ls.size(); i++)", lcName)
        		.addStatement("$T key = e.nextElement()", tempClass)
        		.beginControlFlow("if (($Ls.get(key)).booleanValue() == false)", lcName)
        		.addStatement("a.add(key)")
        		.endControlFlow()
        		.endControlFlow()
        		.addStatement("return a")
        		.build();

        // "add" method:
        MethodSpec.Builder addBuilder = MethodSpec.methodBuilder("add" + tempPart.getName())
        		.addModifiers(Modifier.PUBLIC)
        		.returns(boolean.class)
        		.addParameter(tempClass, "a");

        if (CodeGenerator.allowHireFire
            && tempPart.getSimSEObjectTypeType() == 
            	SimSEObjectTypeTypes.EMPLOYEE) {
          // do not allow nonHired employees to be part of any actions
        	addBuilder.beginControlFlow("if (!a.getHired())")
        	.addStatement("return false")
        	.endControlFlow();
        }

        String variableAddTemp = "";
        variableAddTemp += "if((" + tempPart.getName().toLowerCase()
            + "s.containsKey(a))";
        Vector<SimSEObjectType> types = tempPart.getAllSimSEObjectTypes();
        if (types.size() > 0) {
        	variableAddTemp += " ||";
        }
        for (int j = 0; j < types.size(); j++) {
          if (j > 0) { // not on first element
        	  variableAddTemp += " &&";
          } else { // on first element
        	  variableAddTemp += "(";
          }
          SimSEObjectType tempType = types.elementAt(j);
          variableAddTemp += " ((a instanceof "
              + CodeGeneratorUtils.getUpperCaseLeading(tempType.getName()) + 
              ") == false)";
        }
        if (types.size() > 0) {
        	variableAddTemp += ")";
        }
        if (tempPart.getQuantity().isMaxValBoundless() == false) { 
        	// has a maximum number of participants that can be in this action
        	variableAddTemp += " || (" + tempPart.getName().toLowerCase()
              + "s.size() >= " + tempPart.getQuantity().getMaxVal().toString()
              + ")";
        }
        variableAddTemp += ")\n{\n";
        MethodSpec add = addBuilder.addCode(variableAddTemp)
		        .addStatement("return false")
		        .addCode("} else \n{\n")
		        .addStatement("$Ls.put(a, new $T(true))", lcName, booleanClass)
		        .addStatement("return true")
		        .addCode("}")
		        .build();

        // "remove" method:
        MethodSpec remove = MethodSpec.methodBuilder("remove" + tempPart.getName())
        		.addModifiers(Modifier.PUBLIC)
        		.returns(boolean.class)
        		.addParameter(tempClass, "a")
        		.beginControlFlow("if ($Ls.containsKey(a))", lcName)
        		.addStatement("$Ls.remove(a)", lcName)
        		.addStatement("return true")
        		.endControlFlow()
        		.addStatement("return false")
        		.build();

        // "setActive" method:
        MethodSpec setActive = MethodSpec.methodBuilder("set" + tempPart.getName() + "Active")
        		.addModifiers(Modifier.PUBLIC)
        		.returns(boolean.class)
        		.addParameter(tempClass, "a")
        		.beginControlFlow("if ($Ls.containsKey(a))", lcName)
        		.addStatement("$Ls.put(a, new $T(true))", lcName, Boolean.class)
        		.addStatement("return true")
        		.endControlFlow()
        		.addStatement("return false")
        		.build();

        // "setInactive" method:
        MethodSpec setInactive = MethodSpec.methodBuilder("set" + tempPart.getName() + "Inactive")
        		.addModifiers(Modifier.PUBLIC)
        		.returns(boolean.class)
        		.addParameter(tempClass, "a")
        		.beginControlFlow("if ($Ls.containsKey(a))", lcName)
        		.addStatement("$Ls.put(a, new $T(false))", lcName, Boolean.class)
        		.addStatement("return true")
        		.endControlFlow()
        		.addStatement("return false")
        		.build();
        
        adtBuilder.addMethod(getAll)
		        .addMethod(getAllActive)
		        .addMethod(getAllInActive)
		        .addMethod(add)
		        .addMethod(remove)
		        .addMethod(setActive)
		        .addMethod(setInactive);
      }
    
    TypeSpec adt = adtBuilder
    		.addMethod(refetchParticipants)
    		.build();

    JavaFile javaFile = JavaFile.builder(name, adt)
		    .build();
    
    try {
    	FileWriter writer = new FileWriter(adtFile);
    	
        javaFile.writeTo(writer);
        writer.close();
    } catch (IOException e) {
        JOptionPane.showMessageDialog(null, ("Error writing file "
            + adtFile.getPath() + ": " + e.toString()), "File IO Error",
            JOptionPane.WARNING_MESSAGE);
    }
  }
  
  private String generateRefetchParticipants(Vector<ActionTypeParticipant> participants) {
	  String refetch = "";
	  for (int i = 0; i < participants.size(); i++) {
	      ActionTypeParticipant tempPart = participants.elementAt(i);
	      String metaType = SimSEObjectTypeTypes.getText(
	      		tempPart.getSimSEObjectTypeType());
	      String hashtableTypeString = new String("<" + metaType + ", Boolean>");
	      String partNameLowerCase = tempPart.getName().toLowerCase();
	      refetch += "// " + partNameLowerCase + " participants:\n";
	  		refetch += "Hashtable" + hashtableTypeString + " new" + 
	  				CodeGeneratorUtils.getUpperCaseLeading(tempPart.getName()) + 
	  				"s = new Hashtable" + hashtableTypeString + "();\n";
	  		refetch += "Iterator<Map.Entry" + hashtableTypeString + "> " + 
	  				partNameLowerCase + "sIterator = " + partNameLowerCase + 
	  				"s.entrySet().iterator();\n";
	  		refetch += "while (" + partNameLowerCase + "sIterator.hasNext()) {\n";
	  		refetch += "Map.Entry" + hashtableTypeString + " entry = " +
	  				partNameLowerCase + "sIterator.next();\n";
	  		refetch += metaType + " old" + 
	  				CodeGeneratorUtils.getUpperCaseLeading(tempPart.getName()) + 
	  				" = entry.getKey();\n";
	  		
	  		// go through all allowable types for this participant:
	      Vector<SimSEObjectType> types = tempPart.getAllSimSEObjectTypes();
	      for (int j = 0; j < types.size(); j++) {
	        if (j > 0) { // not on first element
	          refetch += "else ";
	        }
	        refetch += "if (";
	        SimSEObjectType tempType = types.elementAt(j);
	        refetch += "old" + CodeGeneratorUtils.getUpperCaseLeading(
	        		tempPart.getName()) + " instanceof " + 
	        		CodeGeneratorUtils.getUpperCaseLeading(tempType.getName()) + 
	        		") {\n";
	        refetch += metaType + " new" + 
	        		CodeGeneratorUtils.getUpperCaseLeading(tempPart.getName()) + 
	        		" = " + metaType.toLowerCase() + "Rep.get" + 
	        		CodeGeneratorUtils.getUpperCaseLeading(tempType.getName()) + 
	        		"StateRepository().get(((" + 
	        		CodeGeneratorUtils.getUpperCaseLeading(tempType.getName()) + 
	        		")old" + 
	        		CodeGeneratorUtils.getUpperCaseLeading(tempPart.getName()) + 
	        		").get" +
	        		CodeGeneratorUtils.getUpperCaseLeading(
	        				tempType.getKey().getName()) + "());\n";
	        refetch += "Boolean activeStatus = " + partNameLowerCase + 
	        		"s.get(old" + CodeGeneratorUtils.getUpperCaseLeading(
	        				tempPart.getName()) + ");\n";
	        refetch += "new" + CodeGeneratorUtils.getUpperCaseLeading(
	        		tempPart.getName()) + "s.put(new" + 
	        		CodeGeneratorUtils.getUpperCaseLeading(tempPart.getName()) +
	        		", activeStatus);\n}\n";
	      }
	      refetch += "}\n";
	      refetch += partNameLowerCase + "s.clear();\n";
	      refetch += partNameLowerCase + "s.putAll(new" + 
	      		CodeGeneratorUtils.getUpperCaseLeading(tempPart.getName()) + "s);\n\n";
	    }
	  
	  return refetch;
  }
}