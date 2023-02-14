/*
 * This class is responsible for generating all of the code for the different
 * table models in the At-A-Glance Frames in the GUI
 */

package simse.codegenerator.guigenerator;

import simse.modelbuilder.objectbuilder.Attribute;
import simse.modelbuilder.objectbuilder.AttributeTypes;
import simse.modelbuilder.objectbuilder.DefinedObjectTypes;
import simse.modelbuilder.objectbuilder.NumericalAttribute;
import simse.modelbuilder.objectbuilder.SimSEObjectType;
import simse.modelbuilder.objectbuilder.SimSEObjectTypeTypes;
import simse.codegenerator.CodeGeneratorConstants;
import simse.codegenerator.CodeGeneratorUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;

import javax.lang.model.element.Modifier;
import javax.swing.JOptionPane;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class TableModelGenerator implements CodeGeneratorConstants {
  private File directory; // directory to save generated code into
  private DefinedObjectTypes objTypes; // holds all of the defined object types
                                       // from an sso file

  public TableModelGenerator(DefinedObjectTypes objTypes, 
  		File directory) {
    this.objTypes = objTypes;
    this.directory = directory;
  }

  public void generate() {
	  generateAbstractTableModel();
	  generateEmployeeTableModel();
	  generateCustomerTableModel();
	  generateArtifactTableModel();
	  generateToolTableModel();
	  generateProjectTableModel();
    
  }
  
  private void generateAbstractTableModel() {
	    File tModFile = new File(directory, ("simse\\gui\\TableModel.java"));
	    if (tModFile.exists()) {
	    	tModFile.delete(); // delete old version of file
	    }
	    try {
	      FileReader reader = new FileReader("\\resources\\TableModel.txt");
	      Scanner s = new Scanner(reader);
	      FileWriter writer = new FileWriter(tModFile);
	      
	      while (s.hasNext()) {
	    	  writer.write(s.next());
	      }
	      
	      writer.close();
	      s.close();
	    } catch (IOException e) {
	      JOptionPane.showMessageDialog(null, ("Error writing file "
	          + tModFile.getPath() + ": " + e.toString()), "File IO Error",
	          JOptionPane.WARNING_MESSAGE);
	    }
	  }

  private void generateEmployeeTableModel() {
	 ClassName vector = ClassName.get("java.util", "Vector");
	 ClassName employee = ClassName.get("simse.adts.objects", "Employee");
	 ClassName state = ClassName.get("simse.state", "State");
	 ClassName tablemodel = ClassName.get("simse.gui", "TableModel");
	 TypeName tOfE = ParameterizedTypeName.get(tablemodel, employee);
	 TypeName vOfE = ParameterizedTypeName.get(vector, employee);
	 File etmFile = new File(directory, ("simse\\gui\\EmployeeTableModel.java")); 
	 
	 MethodSpec constructor = MethodSpec.constructorBuilder()
			 .addAnnotation(Override.class)
			 .addModifiers(Modifier.PUBLIC)
			 .addParameter(state, "s")
			 .addStatement("super(s)")
			 .build();
	 
	 MethodSpec getRepo = MethodSpec.methodBuilder("getRepository")
			 .addAnnotation(Override.class)
			 .returns(vOfE)
			 .addStatement("return state.getEmployeeStateRepository().getAll()")
			 .build();
	 
	 Vector<Attribute> common = getSharedVisibleAttributes(SimSEObjectTypeTypes.EMPLOYEE);
	 
	 String getVal = "";
	 getVal.concat("switch(row) {\n");
	 for (int i=0; i<common.size(); i++) {
		 getVal.concat("case " + i + ": returnValue = model.get"+common.get(i).getName()+"();\n");
		 getVal.concat("break;\n");
	 }
	 getVal.concat("}\n");
	 
	 MethodSpec getValue = MethodSpec.methodBuilder("getValueAt")
			 .addAnnotation(Override.class)
			 .returns(Object.class)
			 .addParameter(int.class, "row")
			 .addParameter(int.class, "col")
			 .addStatement("$T model = data.elementAt(col)", employee)
			 .addStatement("Object returnValue = null")
			 .addCode(getVal)
			 .addStatement("return returnValue")
			 .build();
	 
	 String setVal = "";
	 setVal.concat("switch(row) {\n");
	 for (int i=0; i<common.size(); i++) {
		 setVal.concat("case " + i + ": model.set"+common.get(i).getName()+"("+getTypeAsCast(common.get(i))+" value);\n");
		 setVal.concat("break;\n");
	 }
	 setVal.concat("}\n");
	 
	 MethodSpec setValue = MethodSpec.methodBuilder("setValueAt")
			 .addAnnotation(Override.class)
			 .returns(void.class)
			 .addParameter(Object.class, "value")
			 .addParameter(int.class, "row")
			 .addParameter(int.class, "col")
			 .addStatement("$T model = data.elementAt(col)", employee)
			 .addCode(setVal)
			 .addStatement("fireTableCellUpdated(row, col)")
			 .build();
	 
	 TypeSpec empTabMod = TypeSpec.classBuilder("EmployeeTableModel")
			 .addModifiers(Modifier.PUBLIC)
			 .superclass(tOfE)
			 .addMethod(constructor)
			 .addMethod(getRepo)
			 .addMethod(getValue)
			 .addMethod(setValue)
			 .build();
	 
	 JavaFile file = JavaFile.builder("simse.gui.EmployeeTableModel", empTabMod)
			 .build();
	 
	 try {
		FileWriter writer = new FileWriter(etmFile);
		writer.write("/* File generated by: simse.codegenerator.guigenerator.TableModelGenerator */");
		file.writeTo(writer);
		writer.close();
	} catch (IOException e) {
		JOptionPane.showMessageDialog(null, ("Error writing file "
		          + etmFile.getPath() + ": " + e.toString()), "File IO Error",
		          JOptionPane.WARNING_MESSAGE);
	}
	 
  }
  
  private void generateArtifactTableModel() {
	  ClassName vector = ClassName.get("java.util", "Vector");
		 ClassName artifact = ClassName.get("simse.adts.objects", "Artifact");
		 ClassName state = ClassName.get("simse.state", "State");
		 ClassName tablemodel = ClassName.get("simse.gui", "TableModel");
		 TypeName tOfA = ParameterizedTypeName.get(tablemodel, artifact);
		 TypeName vOfA = ParameterizedTypeName.get(vector, artifact);
		 File atmFile = new File(directory, ("simse\\gui\\ArtifactTableModel.java")); 
		 
		 MethodSpec constructor = MethodSpec.constructorBuilder()
				 .addAnnotation(Override.class)
				 .addModifiers(Modifier.PUBLIC)
				 .addParameter(state, "s")
				 .addStatement("super(s)")
				 .build();
		 
		 MethodSpec getRepo = MethodSpec.methodBuilder("getRepository")
				 .addAnnotation(Override.class)
				 .returns(vOfA)
				 .addStatement("return state.getArtifactStateRepository().getAll()")
				 .build();
		 
		 Vector<Attribute> common = getSharedVisibleAttributes(SimSEObjectTypeTypes.ARTIFACT);
		 
		 String getVal = "";
		 getVal.concat("switch(row) {\n");
		 for (int i=0; i<common.size(); i++) {
			 getVal.concat("case " + i + ": returnValue = model.get"+common.get(i).getName()+"();\n");
			 getVal.concat("break;\n");
		 }
		 getVal.concat("}\n");
		 
		 MethodSpec getValue = MethodSpec.methodBuilder("getValueAt")
				 .addAnnotation(Override.class)
				 .returns(Object.class)
				 .addParameter(int.class, "row")
				 .addParameter(int.class, "col")
				 .addStatement("$T model = data.elementAt(col)", artifact)
				 .addStatement("Object returnValue = null")
				 .addCode(getVal)
				 .addStatement("return returnValue")
				 .build();
		 
		 String setVal = "";
		 setVal.concat("switch(row) {\n");
		 for (int i=0; i<common.size(); i++) {
			 setVal.concat("case " + i + ": model.set"+common.get(i).getName()+"("+getTypeAsCast(common.get(i))+" value);\n");
			 setVal.concat("break;\n");
		 }
		 setVal.concat("}\n");
		 
		 MethodSpec setValue = MethodSpec.methodBuilder("setValueAt")
				 .addAnnotation(Override.class)
				 .returns(void.class)
				 .addParameter(Object.class, "value")
				 .addParameter(int.class, "row")
				 .addParameter(int.class, "col")
				 .addStatement("$T model = data.elementAt(col)", artifact)
				 .addCode(setVal)
				 .addStatement("fireTableCellUpdated(row, col)")
				 .build();
		 
		 TypeSpec artTabMod = TypeSpec.classBuilder("ArtifactTableModel")
				 .addModifiers(Modifier.PUBLIC)
				 .superclass(tOfA)
				 .addMethod(constructor)
				 .addMethod(getRepo)
				 .addMethod(getValue)
				 .addMethod(setValue)
				 .build();
		 
		 JavaFile file = JavaFile.builder("simse.gui.ArtifactTableModel", artTabMod)
				 .build();
		 
		 try {
			FileWriter writer = new FileWriter(atmFile);
			writer.write("/* File generated by: simse.codegenerator.guigenerator.TableModelGenerator */");
			file.writeTo(writer);
			writer.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, ("Error writing file "
			          + atmFile.getPath() + ": " + e.toString()), "File IO Error",
			          JOptionPane.WARNING_MESSAGE);
		}
  }
  
  private void generateToolTableModel() {
	  ClassName vector = ClassName.get("java.util", "Vector");
		 ClassName tool = ClassName.get("simse.adts.objects", "Tool");
		 ClassName state = ClassName.get("simse.state", "State");
		 ClassName tablemodel = ClassName.get("simse.gui", "TableModel");
		 TypeName tOfT = ParameterizedTypeName.get(tablemodel, tool);
		 TypeName vOfT = ParameterizedTypeName.get(vector, tool);
		 File ttmFile = new File(directory, ("simse\\gui\\ToolTableModel.java")); 
		 
		 MethodSpec constructor = MethodSpec.constructorBuilder()
				 .addAnnotation(Override.class)
				 .addModifiers(Modifier.PUBLIC)
				 .addParameter(state, "s")
				 .addStatement("super(s)")
				 .build();
		 
		 MethodSpec getRepo = MethodSpec.methodBuilder("getRepository")
				 .addAnnotation(Override.class)
				 .returns(vOfT)
				 .addStatement("return state.getToolStateRepository().getAll()")
				 .build();
		 
		 Vector<Attribute> common = getSharedVisibleAttributes(SimSEObjectTypeTypes.TOOL);
		 
		 String getVal = "";
		 getVal.concat("switch(row) {\n");
		 for (int i=0; i<common.size(); i++) {
			 getVal.concat("case " + i + ": returnValue = model.get"+common.get(i).getName()+"();\n");
			 getVal.concat("break;\n");
		 }
		 getVal.concat("}\n");
		 
		 MethodSpec getValue = MethodSpec.methodBuilder("getValueAt")
				 .addAnnotation(Override.class)
				 .returns(Object.class)
				 .addParameter(int.class, "row")
				 .addParameter(int.class, "col")
				 .addStatement("$T model = data.elementAt(col)", tool)
				 .addStatement("Object returnValue = null")
				 .addCode(getVal)
				 .addStatement("return returnValue")
				 .build();
		 
		 String setVal = "";
		 setVal.concat("switch(row) {\n");
		 for (int i=0; i<common.size(); i++) {
			 setVal.concat("case " + i + ": model.set"+common.get(i).getName()+"("+getTypeAsCast(common.get(i))+" value);\n");
			 setVal.concat("break;\n");
		 }
		 setVal.concat("}\n");
		 
		 MethodSpec setValue = MethodSpec.methodBuilder("setValueAt")
				 .addAnnotation(Override.class)
				 .returns(void.class)
				 .addParameter(Object.class, "value")
				 .addParameter(int.class, "row")
				 .addParameter(int.class, "col")
				 .addStatement("$T model = data.elementAt(col)", tool)
				 .addCode(setVal)
				 .addStatement("fireTableCellUpdated(row, col)")
				 .build();
		 
		 TypeSpec toolTabMod = TypeSpec.classBuilder("ToolTableModel")
				 .addModifiers(Modifier.PUBLIC)
				 .superclass(tOfT)
				 .addMethod(constructor)
				 .addMethod(getRepo)
				 .addMethod(getValue)
				 .addMethod(setValue)
				 .build();
		 
		 JavaFile file = JavaFile.builder("simse.gui.ToolTableModel", toolTabMod)
				 .build();
		 
		 try {
			FileWriter writer = new FileWriter(ttmFile);
			writer.write("/* File generated by: simse.codegenerator.guigenerator.TableModelGenerator */");
			file.writeTo(writer);
			writer.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, ("Error writing file "
			          + ttmFile.getPath() + ": " + e.toString()), "File IO Error",
			          JOptionPane.WARNING_MESSAGE);
		}
  }
  
  private void generateCustomerTableModel() {
	  ClassName vector = ClassName.get("java.util", "Vector");
		 ClassName customer = ClassName.get("simse.adts.objects", "Customer");
		 ClassName state = ClassName.get("simse.state", "State");
		 ClassName tablemodel = ClassName.get("simse.gui", "TableModel");
		 TypeName tOfC = ParameterizedTypeName.get(tablemodel, customer);
		 TypeName vOfC = ParameterizedTypeName.get(vector, customer);
		 File ctmFile = new File(directory, ("simse\\gui\\CustomerTableModel.java")); 
		 
		 MethodSpec constructor = MethodSpec.constructorBuilder()
				 .addAnnotation(Override.class)
				 .addModifiers(Modifier.PUBLIC)
				 .addParameter(state, "s")
				 .addStatement("super(s)")
				 .build();
		 
		 MethodSpec getRepo = MethodSpec.methodBuilder("getRepository")
				 .addAnnotation(Override.class)
				 .returns(vOfC)
				 .addStatement("return state.getCustomerStateRepository().getAll()")
				 .build();
		 
		 Vector<Attribute> common = getSharedVisibleAttributes(SimSEObjectTypeTypes.CUSTOMER);
		 
		 String getVal = "";
		 getVal.concat("switch(row) {\n");
		 for (int i=0; i<common.size(); i++) {
			 getVal.concat("case " + i + ": returnValue = model.get"+common.get(i).getName()+"();\n");
			 getVal.concat("break;\n");
		 }
		 getVal.concat("}\n");
		 
		 MethodSpec getValue = MethodSpec.methodBuilder("getValueAt")
				 .addAnnotation(Override.class)
				 .returns(Object.class)
				 .addParameter(int.class, "row")
				 .addParameter(int.class, "col")
				 .addStatement("$T model = data.elementAt(col)", customer)
				 .addStatement("Object returnValue = null")
				 .addCode(getVal)
				 .addStatement("return returnValue")
				 .build();
		 
		 String setVal = "";
		 setVal.concat("switch(row) {\n");
		 for (int i=0; i<common.size(); i++) {
			 setVal.concat("case " + i + ": model.set"+common.get(i).getName()+"("+getTypeAsCast(common.get(i))+" value);\n");
			 setVal.concat("break;\n");
		 }
		 setVal.concat("}\n");
		 
		 MethodSpec setValue = MethodSpec.methodBuilder("setValueAt")
				 .addAnnotation(Override.class)
				 .returns(void.class)
				 .addParameter(Object.class, "value")
				 .addParameter(int.class, "row")
				 .addParameter(int.class, "col")
				 .addStatement("$T model = data.elementAt(col)", customer)
				 .addCode(setVal)
				 .addStatement("fireTableCellUpdated(row, col)")
				 .build();
		 
		 TypeSpec custTabMod = TypeSpec.classBuilder("CustomerTableModel")
				 .addModifiers(Modifier.PUBLIC)
				 .superclass(tOfC)
				 .addMethod(constructor)
				 .addMethod(getRepo)
				 .addMethod(getValue)
				 .addMethod(setValue)
				 .build();
		 
		 JavaFile file = JavaFile.builder("simse.gui.CustomerTableModel", custTabMod)
				 .build();
		 
		 try {
			FileWriter writer = new FileWriter(ctmFile);
			writer.write("/* File generated by: simse.codegenerator.guigenerator.TableModelGenerator */");
			file.writeTo(writer);
			writer.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, ("Error writing file "
			          + ctmFile.getPath() + ": " + e.toString()), "File IO Error",
			          JOptionPane.WARNING_MESSAGE);
		}
  }
  
  private void generateProjectTableModel() {
	  ClassName vector = ClassName.get("java.util", "Vector");
		 ClassName project = ClassName.get("simse.adts.objects", "Project");
		 ClassName state = ClassName.get("simse.state", "State");
		 ClassName tablemodel = ClassName.get("simse.gui", "TableModel");
		 TypeName tOfP = ParameterizedTypeName.get(tablemodel, project);
		 TypeName vOfP = ParameterizedTypeName.get(vector, project);
		 File ptmFile = new File(directory, ("simse\\gui\\ProjectTableModel.java")); 
		 
		 MethodSpec constructor = MethodSpec.constructorBuilder()
				 .addAnnotation(Override.class)
				 .addModifiers(Modifier.PUBLIC)
				 .addParameter(state, "s")
				 .addStatement("super(s)")
				 .build();
		 
		 MethodSpec getRepo = MethodSpec.methodBuilder("getRepository")
				 .addAnnotation(Override.class)
				 .returns(vOfP)
				 .addStatement("return state.getProjectStateRepository().getAll()")
				 .build();
		 
		 Vector<Attribute> common = getSharedVisibleAttributes(SimSEObjectTypeTypes.PROJECT);
		 
		 String getVal = "";
		 getVal.concat("switch(row) {\n");
		 for (int i=0; i<common.size(); i++) {
			 getVal.concat("case " + i + ": returnValue = model.get"+common.get(i).getName()+"();\n");
			 getVal.concat("break;\n");
		 }
		 getVal.concat("}\n");
		 
		 MethodSpec getValue = MethodSpec.methodBuilder("getValueAt")
				 .addAnnotation(Override.class)
				 .returns(Object.class)
				 .addParameter(int.class, "row")
				 .addParameter(int.class, "col")
				 .addStatement("$T model = data.elementAt(col)", project)
				 .addStatement("Object returnValue = null")
				 .addCode(getVal)
				 .addStatement("return returnValue")
				 .build();
		 
		 String setVal = "";
		 setVal.concat("switch(row) {\n");
		 for (int i=0; i<common.size(); i++) {
			 setVal.concat("case " + i + ": model.set"+common.get(i).getName()+"("+getTypeAsCast(common.get(i))+" value);\n");
			 setVal.concat("break;\n");
		 }
		 setVal.concat("}\n");
		 
		 MethodSpec setValue = MethodSpec.methodBuilder("setValueAt")
				 .addAnnotation(Override.class)
				 .returns(void.class)
				 .addParameter(Object.class, "value")
				 .addParameter(int.class, "row")
				 .addParameter(int.class, "col")
				 .addStatement("$T model = data.elementAt(col)", project)
				 .addCode(setVal)
				 .addStatement("fireTableCellUpdated(row, col)")
				 .build();
		 
		 TypeSpec projTabMod = TypeSpec.classBuilder("ProjectTableModel")
				 .addModifiers(Modifier.PUBLIC)
				 .superclass(tOfP)
				 .addMethod(constructor)
				 .addMethod(getRepo)
				 .addMethod(getValue)
				 .addMethod(setValue)
				 .build();
		 
		 JavaFile file = JavaFile.builder("simse.gui.ProjectTableModel", projTabMod)
				 .build();
		 
		 try {
			FileWriter writer = new FileWriter(ptmFile);
			writer.write("/* File generated by: simse.codegenerator.guigenerator.TableModelGenerator */");
			file.writeTo(writer);
			writer.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, ("Error writing file "
			          + ptmFile.getPath() + ": " + e.toString()), "File IO Error",
			          JOptionPane.WARNING_MESSAGE);
		}
  }
  
  
  private Vector<Attribute> getSharedVisibleAttributes(int dType) {
	  Vector<SimSEObjectType> typeTypes = new Vector<>();
	  Vector<SimSEObjectType> types = objTypes.getAllObjectTypes();
	  for (SimSEObjectType type: types) {
	        if (type.getType() == dType) {
	            typeTypes.add(type);
	        }
	    }
	  Vector<Attribute> compareAttributes = new Vector<>();
	  if (typeTypes.size() > 0) {
	        SimSEObjectType compareType = typeTypes.get(0);
	        compareAttributes = compareType.getAllVisibleAttributes();
	        for (SimSEObjectType obj: typeTypes) {
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
	        
	  return compareAttributes;
  }
  
  private String getTypeAsCast(Attribute att) {
      if (att.getType() == AttributeTypes.INTEGER) {
        return "(int)";
      } else if (att.getType() == AttributeTypes.DOUBLE) {
        return "(double)";
      } else if (att.getType() == AttributeTypes.BOOLEAN) {
        return "(boolean)";
      } else { //(att.getType() == AttributeTypes.STRING)
        return "(String)";
      }
  }
}