package simse.codegenerator.guigenerator;

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
import simse.modelbuilder.objectbuilder.Attribute;
import simse.modelbuilder.objectbuilder.AttributeTypes;
import simse.modelbuilder.objectbuilder.DefinedObjectTypes;
import simse.modelbuilder.objectbuilder.SimSEObjectType;
import simse.modelbuilder.objectbuilder.SimSEObjectTypeTypes;

public class InfoScreenGenerator {
	private DefinedObjectTypes objTypes;
	private File directory; // directory to save generated code into

	  public InfoScreenGenerator(DefinedObjectTypes objTypes, File directory) {
		this.objTypes = objTypes;
	    this.directory = directory;
	  }
	  
	  public void generate() {
		  generateArtifactInfoScreen();
		  generateEmployeeInfoScreen();
	  }

	  private void generateArtifactInfoScreen() {
	    File aisFile = new File(directory, ("simse\\gui\\ArtifactInfoScreen.java"));
	    if (aisFile.exists()) {
	    	aisFile.delete(); // delete old version of file
	    }
	    try {
	      FileWriter writer = new FileWriter(aisFile);
	      
	      ClassName eventhandler = ClassName.get("javafx.event", "EventHandler");
	      ClassName pos = ClassName.get("javafx.geometry", "Pos");
	      ClassName scene = ClassName.get("javafx.scene", "Scene");
	      ClassName button = ClassName.get("javafx.scene.control", "Button");
	      ClassName contextmenu = ClassName.get("javafx.scene.control", "ContextMenu");
	      ClassName label = ClassName.get("javafx.scene.control", "Label");
	      ClassName listview = ClassName.get("javafx.scene.control", "ListView");
	      ClassName imageview = ClassName.get("javafx.scene.image", "ImageView");
	      ClassName mouseevent = ClassName.get("javafx.scene.input", "MouseEvent");
	      ClassName stackpane = ClassName.get("javafx.scene.layout", "StackPane");
	      ClassName vbox = ClassName.get("javafx.scene.layout", "VBox");
	      ClassName font = ClassName.get("javafx.scene.text", "Font");
	      ClassName stage = ClassName.get("javafx.stage", "Stage");
	      ClassName artifact = ClassName.get("simse.adts.objects", "Artifact");
	      ClassName javafxhelpers = ClassName.get("simse.gui.util", "JavaFXHelpers");
	      ClassName logic = ClassName.get("simse.logic", "Logic");
	      ClassName state = ClassName.get("simse.state", "State");
	      ClassName simsegui = ClassName.get("simse.gui", "SimSEGUI");
	      ClassName string = ClassName.get(String.class);
	      TypeName mouseHandler = ParameterizedTypeName.get(eventhandler, mouseevent);
	      TypeName viewOfStrings = ParameterizedTypeName.get(listview, string);
	      
	      Vector<Attribute> aAts = this.getSharedVisibleAttributes(SimSEObjectTypeTypes.ARTIFACT);
	      String atts = "";
	      atts.concat("attributes = new ListView<String>();\n");
	      for (int i=0; i<aAts.size(); i++) {
	    	  atts.concat("attributes.getItems().add(\""+aAts.get(i).getName() + ": \"" + getTypeAsToString(aAts.get(i))+"(artifact.get"+aAts.get(i).getName()+"()));\n");
	      }
	      atts.concat("attributes.setMaxHeight(attributes.getItems().size()*25)");
	      
	      MethodSpec constructor = MethodSpec.constructorBuilder()
	    		  .addModifiers(Modifier.PUBLIC)
	    		  .addParameter(state, "s")
	    		  .addParameter(simsegui, "gui")
	    		  .addParameter(logic, "l")
	    		  .addParameter(artifact, "artifact")
	    		  .addStatement("this.$N = s", "state")
	    		  .addStatement("this.$N = $N", "gui", "gui")
	    		  .addStatement("this.$N = l", "logic")
	    		  .addStatement("this.$N = $N", "artifact", "artifact")
	    		  .addStatement("this.$N = new $T()", "actions", contextmenu)
	    		  .addStatement("this.$N = new $T()", "mainPane", vbox)
	    		  .addStatement("String $NName = artifact.getName()", "artifact")
	    		  .addStatement("this.setTitle(artifactName)")
	    		  .addStatement("")
	    		  .addStatement("$T imagePane = new $T()", stackpane, stackpane)
	    		  .addStatement("imagePane.setMinSize(110, 110)")
	    		  .addStatement("$T img = $T.createImageView(\"src/simse/gui/icons/\" + $N.getName() + \".gif\")", imageview, javafxhelpers, "artifact")
	    		  .beginControlFlow("if (img == null)")
	    		  .addStatement("img = JavaFXHelpers.createImageView(\"src/simse/gui/icons/3.gif\")")
	    		  .endControlFlow()
	    		  .addStatement("img.setScaleX(2)")
	    		  .addStatement("img.setScaleY(2)")
	    		  .addStatement("imagePane.getChildren().add(img)")
	    		  .addStatement("")
	    		  .addCode(atts)
	    		  .addStatement("")
	    		  .addStatement("String objTypeFull = $N.getClass().toString()", "artifact")
	    		  .addStatement("String[] objTypeArr = objTypeFull.split(\"\\.\")")
	    		  .addStatement("String objType = objTypeArr[objTypeArr.length - 1]")
	    		  .addStatement("String objTypeType = \"$T\"", artifact)
	    		  .addStatement("String title = artifactName + \" Attributes\"")
	    		  .addStatement("ObjectGraphPane objGraph = new ObjectGraphPane(title, $N.getLog(), objTypeType, objType, artifactName, $N.getBranch(), $N)", "gui", "gui", "gui")
	    		  .addStatement("")
	    		  .addStatement("$T name = new $T($N.getName())", label, label, "artifact")
	    		  .addStatement("name.set$T(new Font(30))", font)
	    		  .addStatement("$N.getChildren().add(name)", "mainPane")
	    		  .addStatement("$N.getChildren().add(imagePane)", "mainPane")
	    		  .addStatement("$N.getChildren().add($N)", "mainPane", "actionsButton")
	    		  .addStatement("$N.getChildren().add($N)", "mainPane", "attributes")
	    		  .addStatement("$N.getChildren().add(objGraph)", "mainPane")
	    		  .addStatement("$N.setAlignment($T.CENTER)", "mainPane", pos)
	    		  .addStatement("")
	    		  .addStatement("$T scene = new $T(mainPane, 500, 700)", scene, scene)
	    		  .addStatement("this.setScene(scene)")
	    		  .build();
	      
	      MethodSpec handle = MethodSpec.methodBuilder("handle")
	    		  .addAnnotation(Override.class)
	    		  .returns(void.class)
	    		  .addParameter(mouseevent, "e")
	    		  .addStatement("$N.show(mainPane, e.getScreenX(), e.getScreenY())", "actions")
	    		  .build();
	      
	      TypeSpec ais = TypeSpec.classBuilder("ArtifactInfoScreen")
	    		  .addModifiers(Modifier.PUBLIC)
	    		  .superclass(stage)
	    		  .addSuperinterface(mouseHandler)
	    		  .addField(contextmenu, "actions")
	    		  .addField(vbox, "mainPane")
	    		  .addField(simsegui, "gui")
	    		  .addField(logic, "logic")
	    		  .addField(state, "state")
	    		  .addField(artifact, "artifact")
	    		  .addField(button, "actionsButton")
	    		  .addField(viewOfStrings, "attributes")
	    		  .addMethod(constructor)
	    		  .addMethod(handle)
	    		  .build();
	      
	      JavaFile file = JavaFile.builder("simse.gui.ArtifactInfoScreen", ais)
					 .build();
	      
	      file.writeTo(writer);
	      
	      writer.close();
	    } catch (IOException e) {
	      JOptionPane.showMessageDialog(null, ("Error writing file "
	          + aisFile.getPath() + ": " + e.toString()), "File IO Error",
	          JOptionPane.WARNING_MESSAGE);
	    }
	  }
	  
	  private void generateEmployeeInfoScreen() {
		  File eisFile = new File(directory, ("simse\\gui\\EmployeeInfoScreen.java"));
		    if (eisFile.exists()) {
		    	eisFile.delete(); // delete old version of file
		    }
		    try {
		      FileWriter writer = new FileWriter(eisFile);
		      
		      ClassName vector = ClassName.get("java.util", "Vector");
		      ClassName actionevent = ClassName.get("javafx.event", "ActionEvent");
		      ClassName eventhandler = ClassName.get("javafx.event", "EventHandler");
		      ClassName pos = ClassName.get("javafx.geometry", "Pos");
		      ClassName scene = ClassName.get("javafx.scene", "Scene");
		      ClassName button = ClassName.get("javafx.scene.control", "Button");
		      ClassName contextmenu = ClassName.get("javafx.scene.control", "ContextMenu");
		      ClassName label = ClassName.get("javafx.scene.control", "Label");
		      ClassName listview = ClassName.get("javafx.scene.control", "ListView");
		      ClassName menuitem = ClassName.get("javafx.scene.control", "MenuItem");
		      ClassName imageview = ClassName.get("javafx.scene.image", "ImageView");
		      ClassName mouseevent = ClassName.get("javafx.scene.input", "MouseEvent");
		      ClassName stackpane = ClassName.get("javafx.scene.layout", "StackPane");
		      ClassName vbox = ClassName.get("javafx.scene.layout", "VBox");
		      ClassName font = ClassName.get("javafx.scene.text", "Font");
		      ClassName stage = ClassName.get("javafx.stage", "Stage");
		      ClassName employee = ClassName.get("simse.adts.objects", "Employee");
		      ClassName javafxhelpers = ClassName.get("simse.gui.util", "JavaFXHelpers");
		      ClassName logic = ClassName.get("simse.logic", "Logic");
		      ClassName state = ClassName.get("simse.state", "State");
		      ClassName simsegui = ClassName.get("simse.gui", "SimSEGUI");
		      ClassName string = ClassName.get(String.class);
		      TypeName mouseHandler = ParameterizedTypeName.get(eventhandler, mouseevent);
		      TypeName viewOfStrings = ParameterizedTypeName.get(listview, string);
		      TypeName actionHandler = ParameterizedTypeName.get(eventhandler, actionevent);
		      
		      Vector<Attribute> eAts = this.getSharedVisibleAttributes(SimSEObjectTypeTypes.EMPLOYEE);
		      String atts = "";
		      atts.concat("attributes = new ListView<String>();\n");
		      for (int i=0; i<eAts.size(); i++) {
		    	  atts.concat("attributes.getItems().add(\""+eAts.get(i).getName() + ": \"" + getTypeAsToString(eAts.get(i))+"(employee.get"+eAts.get(i).getName()+"()));\n");
		      }
		      atts.concat("attributes.setMaxHeight(attributes.getItems().size()*24)");
		      
		      MethodSpec constructor = MethodSpec.constructorBuilder()
		    		  .addModifiers(Modifier.PUBLIC)
		    		  .addParameter(state, "s")
		    		  .addParameter(simsegui, "gui")
		    		  .addParameter(logic, "l")
		    		  .addParameter(employee, "employee")
		    		  .addStatement("this.$N = s", "state")
		    		  .addStatement("this.$N = $N", "gui", "gui")
		    		  .addStatement("this.$N = l", "logic")
		    		  .addStatement("this.$N = new $T()", "mainPane", vbox)
		    		  .addStatement("this.$N = new $T()", "actions", contextmenu)
		    		  .addStatement("this.employee = employee")
		    		  .addStatement("")
		    		  .addStatement("String engineerName = employee.getName()")
		    		  .addStatement("this.setTitle(engineerName)")
		    		  .addStatement("")
		    		  .addStatement("Vector<String> menuItems = employee.getMenu()")
		    		  .beginControlFlow("for (int i=0; i < menuItems.size(); i++")
		    		  .addStatement("String item = menuItems.elementAt(i)")
		    		  .addStatement("$T tempItem = new $T(item)", menuitem, menuitem)
		    		  .addStatement("tempItem.setOnAction(menuItemEvent)")
		    		  .addStatement("$N.getItems().add(tempItem)", "actions")
		    		  .endControlFlow()
		    		  .addStatement("$T imagePane = new $T()\", stackpane\", stackpane")
		    		  .addStatement("imagePane.setMinSize(110, 110)")
		    		  .addStatement("$T img = $T.createImageView(\"src/simse/gui/icons/\" + employee.getName() + \".gif\")", imageview, javafxhelpers)
		    		  .beginControlFlow("if (img == null)")
		    		  .addStatement("img = JavaFXHelpers.createImageView(\"src/simse/gui/icons/alex.gif\")")
		    		  .endControlFlow()
		    		  .addStatement("img.setScaleX(2)")
		    		  .addStatement("img.setScaleY(2)")
		    		  .addStatement("imagePane.getChildren().add(img)")
		    		  .addStatement("")
		    		  .addStatement("")
		    		  .addStatement("$N = new $T(\"Assign $T to Tasks\")", "actionsButton", button, employee)
		    		  .addStatement("$N.addEventHandler($T.MOUSE_CLICKED, this)", "actionsButton", mouseevent)
		    		  .addStatement("")
		    		  .addCode(atts)
		    		  .addStatement("")
		    		  .addStatement("String objTypeFull = employee.getClass().toString()")
		    		  .addStatement("String[] objTypeArr = objTypeFull.split(\"\\.\")")
		    		  .addStatement("String objType = objTypeArr[objTypeArr.length - 1]")
		    		  .addStatement("String objTypeType = \"$T\"", employee)
		    		  .addStatement("String title = engineerName + \" Attributes\"")
		    		  .addStatement("ObjectGraphPane objGraph = new ObjectGraphPane(title, $N.getLog(), objTypeType, objType, engineerName, $N.getBranch(), $N)", "gui", "gui", "gui")
		    		  .addStatement("")
		    		  .addStatement("$T name = new $T(employee.getName())", label, label)
		    		  .addStatement("name.set$T(new Font(30))", font)
		    		  .addStatement("$N.getChildren().add(name)", "mainPane")
		    		  .addStatement("$N.getChildren().add(imagePane)", "mainPane")
		    		  .addStatement("$N.getChildren().add($N)", "mainPane", "actionsButton")
		    		  .addStatement("$N.getChildren().add($N)", "mainPane", "attributes")
		    		  .addStatement("$N.getChildren().add(objGraph)", "mainPane")
		    		  .addStatement("$N.setAlignment($T.CENTER)", "mainPane", pos)
		    		  .addStatement("")
		    		  .addStatement("$T scene = new $T(mainPane, 500, 700)", scene, scene)
		    		  .addStatement("this.setScene(scene)")
		    		  .build();
		      
		      MethodSpec pop = MethodSpec.methodBuilder("popupMenuActions")
		    		  .addModifiers(Modifier.PUBLIC)
		    		  .returns(void.class)
		    		  .addParameter(menuitem, "source")
		    		  .addStatement("$T item = ($T) source", menuitem, menuitem)
		    		  .addStatement("$N.getMenuInputManager().menuItemSelected(employee, item.getText(), $N)", "logic", "gui")
		    		  .addStatement("$N.getWorld().update()", "gui")
		    		  .build();
		      
		      MethodSpec handle = MethodSpec.methodBuilder("handle")
		    		  .addAnnotation(Override.class)
		    		  .returns(void.class)
		    		  .addParameter(mouseevent, "e")
		    		  .addStatement("$N.show(mainPane, e.getScreenX(), e.getScreenY())", "actions")
		    		  .build();
		      
		      TypeSpec anon = TypeSpec.anonymousClassBuilder("")
		    		  .addSuperinterface(actionHandler)
		    		  .addMethod(MethodSpec.methodBuilder("handle")
		    				  .addModifiers(Modifier.PUBLIC)
		    				  .returns(void.class)
		    				  .addParameter(actionevent, "event")
		    				  .addStatement("Object source = event.getSource()")
		    				  .beginControlFlow("if (source instanceof $T)", menuitem)
		    				  .addStatement("popupMenuActions(($T) source)", menuitem)
		    				  .endControlFlow()
		    				  .build())
		    		  .build();
		      
		      TypeSpec eis = TypeSpec.classBuilder("ArtifactInfoScreen")
		    		  .addModifiers(Modifier.PUBLIC)
		    		  .superclass(stage)
		    		  .addSuperinterface(mouseHandler)
		    		  .addField(contextmenu, "actions")
		    		  .addField(vbox, "mainPane")
		    		  .addField(simsegui, "gui")
		    		  .addField(logic, "logic")
		    		  .addField(state, "state")
		    		  .addField(employee, "employee")
		    		  .addField(button, "actionsButton")
		    		  .addField(viewOfStrings, "attributes")
		    		  .addField(FieldSpec.builder(actionHandler, "menuItemEvent", Modifier.PRIVATE)
		    				  .initializer("$L", anon)
		    				  .build())
		    		  .addMethod(constructor)
		    		  .addMethod(pop)
		    		  .addMethod(handle)
		    		  .build();
		      
		      JavaFile file = JavaFile.builder("simse.gui.EmployeeInfoScreen", eis)
						 .build();
		      
		      file.writeTo(writer);
		      
		      writer.close();
		    } catch (IOException e) {
		      JOptionPane.showMessageDialog(null, ("Error writing file "
		          + eisFile.getPath() + ": " + e.toString()), "File IO Error",
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
	  
	  private String getTypeAsToString(Attribute att) {
	      if (att.getType() == AttributeTypes.INTEGER) {
	        return "Integer.toString";
	      } else if (att.getType() == AttributeTypes.DOUBLE) {
	        return "Double.toString";
	      } else if (att.getType() == AttributeTypes.BOOLEAN) {
	        return "Boolean.toString";
	      } else { //(att.getType() == AttributeTypes.STRING)
	        return "";
	      }
	  }
}
