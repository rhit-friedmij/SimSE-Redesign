/*
 * This class is responsible for generating all of the code for the action panel
 * in the GUI
 */

package simse.codegenerator.guigenerator;

import simse.modelbuilder.objectbuilder.Attribute;
import simse.modelbuilder.objectbuilder.DefinedObjectTypes;
import simse.modelbuilder.objectbuilder.SimSEObjectType;
import simse.modelbuilder.objectbuilder.SimSEObjectTypeTypes;
import simse.modelbuilder.actionbuilder.ActionType;
import simse.modelbuilder.actionbuilder.DefinedActionTypes;
import simse.codegenerator.CodeGenerator;
import simse.codegenerator.CodeGeneratorConstants;
import simse.codegenerator.CodeGeneratorUtils;

import java.util.ArrayList;
import java.util.Vector;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.lang.model.element.Modifier;
import javax.swing.JOptionPane;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;


public class EmployeesPanelGenerator implements CodeGeneratorConstants {
  private DefinedObjectTypes objTypes; // holds all of the defined object types
                                       // from an sso file
  private DefinedActionTypes actTypes; // holds all of the defined action types
                                       // from an ssa file
  private File directory; // directory to save generated code into
  
  private ArrayList<String> impTypes;
  
  private ArrayList<String> impActions;

  public EmployeesPanelGenerator(DefinedObjectTypes objTypes, 
  		DefinedActionTypes actTypes, File directory) {
    this.objTypes = objTypes;
    this.actTypes = actTypes;
    this.directory = directory;
    this.impTypes = new ArrayList<>();
    this.impActions = new ArrayList<>();
  }

  public void generate() {
    File actPanelFile = new File(directory, ("simse\\gui\\EmployeesPanel.java"));
    if (actPanelFile.exists()) {
      actPanelFile.delete(); // delete old version of file
    }
    try {
      FileWriter writer = new FileWriter(actPanelFile);
      writer
          .write("/* File generated by: simse.codegenerator.guigenerator.EmployeesPanelGenerator */");
      ClassName arraylist = ClassName.get("java.util", "ArrayList");
      ClassName enumeration = ClassName.get("java.util", "Enumeration");
      ClassName hashtable = ClassName.get("java.util", "Hashtable");
      ClassName vector = ClassName.get("java.util", "Vector");
      ClassName displayablecharacter = ClassName.get("simse.animation", "DisplayableCharacter");
      ClassName simsecharacter = ClassName.get("simse.animation", "SimSECharacter");
      ClassName actionevent = ClassName.get("javafx.event", "ActionEvent");
      ClassName eventhandler = ClassName.get("javafx.event", "EventHandler");
      ClassName pos = ClassName.get("javafx.geometry", "Pos");
      ClassName node = ClassName.get("javafx.scene", "Node");
      ClassName contextmenu = ClassName.get("javafx.scene.control", "ContextMenu");
      ClassName label = ClassName.get("javafx.scene.control", "Label");
      ClassName menuitem = ClassName.get("javafx.scene.control", "MenuItem");
      ClassName scrollpane = ClassName.get("javafx.scene.control", "ScrollPane");
      ClassName titledpane = ClassName.get("javafx.scene.control", "TitledPane");
      ClassName imageview = ClassName.get("javafx.scene.image", "ImageView");
      ClassName mousebutton = ClassName.get("javafx.scene.input", "MouseButton");
      ClassName mouseevent = ClassName.get("javafx.scene.input", "MouseEvent");
      ClassName border = ClassName.get("javafx.scene.layout", "Border");
      ClassName borderstroke = ClassName.get("javafx.scene.layout", "BorderStroke");
      ClassName borderstrokestyle = ClassName.get("javafx.scene.layout", "BorderStrokeStyle");
      ClassName borderwidths = ClassName.get("javafx.scene.layout", "BorderWidths");
      ClassName cornerradii = ClassName.get("javafx.scene.layout", "CornerRadii");
      ClassName gridpane = ClassName.get("javafx.scene.layout", "GridPane");
      ClassName hbox = ClassName.get("javafx.scene.layout", "HBox");
      ClassName pane = ClassName.get("javafx.scene.layout", "Pane");
      ClassName vbox = ClassName.get("javafx.scene.layout", "VBox");
      ClassName color = ClassName.get("javafx.scene.paint", "Color");
      ClassName font = ClassName.get("javafx.scene.text", "Font");
      ClassName textalignment = ClassName.get("javafx.scene.text", "TextAlignment");
      ClassName action = ClassName.get("simse.adts.actions", "Action");
      ClassName breakaction = ClassName.get("simse.adts.actions", "BreakAction");
      ClassName correctcodeaction = ClassName.get("simse.adts.actions", "CorrectCodeAction");
      ClassName correctdesignaction = ClassName.get("simse.adts.actions", "CorrectDesignAction");
      ClassName correctrequirementsaction = ClassName.get("simse.adts.actions", "CorrectRequirementsAction");
      ClassName correctsystemtestplanaction = ClassName.get("simse.adts.actions", "CorrectSystemTestPlanAction");
      ClassName createcodeaction = ClassName.get("simse.adts.actions", "CreateCodeAction");
      ClassName createdesignaction = ClassName.get("simse.adts.actions", "CreateDesignAction");
      ClassName createrequirementsaction = ClassName.get("simse.adts.actions", "CreateRequirementsAction");
      ClassName createsystemtestplanaction = ClassName.get("simse.adts.actions", "CreateSystemTestPlanAction");
      ClassName getsickaction = ClassName.get("simse.adts.actions", "GetSickAction");
      ClassName inspectcodeaction = ClassName.get("simse.adts.actions", "InspectCodeAction");
      ClassName integratecodeaction = ClassName.get("simse.adts.actions", "IntegrateCodeAction");
      ClassName reviewdesignaction = ClassName.get("simse.adts.actions", "ReviewDesignAction");
      ClassName reviewrequirementsaction = ClassName.get("simse.adts.actions", "ReviewRequirementsAction");
      ClassName reviewsystemtestplanaction = ClassName.get("simse.adts.actions", "ReviewSystemTestPlanAction");
      ClassName systemtestaction = ClassName.get("simse.adts.actions", "SystemTestAction");
      ClassName employee = ClassName.get("simse.adts.objects", "Employee");
      ClassName softwareengineer = ClassName.get("simse.adts.objects", "SoftwareEngineer");
      ClassName javafxhelpers = ClassName.get("simse.gui.util", "JavaFXHelpers");
      ClassName logic = ClassName.get("simse.logic", "Logic");
      ClassName state = ClassName.get("simse.state", "State");
      ClassName simsegui = ClassName.get("simse.gui", "SimSEGUI");
      ClassName panels = ClassName.get("simse.gui", "Panels");
      ClassName simsepanel = ClassName.get("simse.gui", "SimSEPanel");
      ClassName string = ClassName.get(String.class);
      TypeName vOfE = ParameterizedTypeName.get(vector, employee);
      TypeName vOfS = ParameterizedTypeName.get(vector, string);
      TypeName listOfDC = ParameterizedTypeName.get(arraylist, displayablecharacter);
      TypeName hashEmpVB = ParameterizedTypeName.get(hashtable, employee, vbox);
      TypeName hashEmpHB = ParameterizedTypeName.get(hashtable, employee, hbox);
      TypeName hashEmpLab = ParameterizedTypeName.get(hashtable, employee, label);
      TypeName enumOfE = ParameterizedTypeName.get(enumeration, employee);
      TypeName mouseHandler = ParameterizedTypeName.get(eventhandler, mouseevent);
      TypeName actionHandler = ParameterizedTypeName.get(eventhandler, actionevent);
      // constructor:
      MethodSpec constructor = MethodSpec.constructorBuilder()
    		  .addModifiers(Modifier.PUBLIC)
    		  .addParameter(simsegui, "gui")
    		  .addParameter(state, "s")
    		  .addParameter(logic, "l")
    		  .addStatement("$N = s", "state")
    		  .addStatement("$N = l", "logic")
    		  .addStatement("$N = gui", "mainGUIFrame")
    		  .addStatement("$N = new $T()", "layout", vbox)
    		  .addStatement("$N.setId(\"actionPanelVBox\")", "layout")
    		  .addStatement("$N = new $T()", "employeePane", scrollpane)
    		  .addStatement("$N.setId(\"scrollPaneActionPanel\")", "employeePane")
    		  .addStatement("$N.setPrefSize(225, 425)", "employeePane")
    		  .addStatement("$N.setId(\"ActionPanelMain\")", "employeePane")
    		  .addStatement("empsToEmpPanels = new $T()", hashEmpVB)
    		  .addStatement("empsToPicPanels = new $T()", hashEmpHB)
    		  .addStatement("empsToPicLabels = new $T()", hashEmpLab)
    		  .addStatement("empsToKeyLabels = new $T()", hashEmpLab)
    		  .addStatement("$T titlePanel = new $T(\"$T Panel\", $N)", titledpane, titledpane, employee, "employeePane")
    		  .addStatement("titlePanel.set$T(Border.EMPTY)", border)
    		  .addStatement("titlePanel.setId(\"ActionTitlePanel\")")
    		  .addStatement("titlePanel.setBackground($T.createBackground$T(Color.rgb(102, 102, 102, 1)))", javafxhelpers, color)
    		  .addStatement("$N = null", "selectedEmp")
    		  .addStatement("$N = new $T()", "popup", contextmenu)
    		  .addStatement("$N.getChildren().add(titlePanel)", "layout")
    		  .addStatement("$T allEmps = $N.getEmployeeStateRepository().getAll()", vOfE, "state")
    		  .addStatement("$N = new $T()", "characters", listOfDC)
    		  .addStatement("update()")
    		  .addStatement("this.getChildren().add(layout)")
    		  .build();

      // createPopupMenu function:
      MethodSpec createPopup = MethodSpec.methodBuilder("createPopupMenu")
    		  .addModifiers(Modifier.PUBLIC)
    		  .returns(void.class)
    		  .addParameter(node, "node")
    		  .addParameter(double.class, "x")
    		  .addParameter(double.class, "y")
    		  .addStatement("$N.getItems().clear()", "popup")
    		  .beginControlFlow("if (mainGUIFrame.getEngine().isRunning())")
    		  .addStatement("return")
    		  .endControlFlow()
    		  .beginControlFlow("if (selectedEmp != null)")
    		  .addStatement("$T menuItems = selectedEmp.getMenu()", vOfS)
    		  .beginControlFlow("for (int i = 0; i < menuItems.size(); i++)")
    		  .addStatement("String item = menuItems.elementAt(i)")
    		  .addStatement("$T tempItem = new $T(item)", menuitem, menuitem)
    		  .addStatement("tempItem.setOnAction($N)", "menuItemEvent")
    		  .addStatement("$N.getItems().addAll(tempItem)", "popup")
    		  .endControlFlow()
    		  .beginControlFlow("if (menuItems.size() >= 1)")
    		  .addStatement("$N.show(node, x, y)", "popup")
    		  .endControlFlow()
    		  .endControlFlow()
    		  .build();

      // update function:
      MethodSpec update = MethodSpec.methodBuilder("update")
    		  .addModifiers(Modifier.PUBLIC)
    		  .returns(void.class)
    		  .addStatement("$N.setContent(null)", "employeePane")
    		  .addStatement("")
    		  .addStatement("empsToEmpPanels.clear()")
    		  .addStatement("")
    		  .addStatement("$T titleLabel = new $T(\"Current Activities:\")", label, label)
    		  .addStatement("$T f = titleLabel.getFont()", font)
    		  .addStatement("$T newFont = new $T(f.getName(), 15)", font, font)
    		  .addStatement("titleLabel.setFont(newFont)")
    		  .addStatement("titleLabel.setTextFill($T.BLACK)", color)
    		  .addStatement("")
    		  .addStatement("$T employees = new $T()", vbox, vbox)
    		  .addStatement("employees.getChildren().add(titleLabel)")
    		  .addStatement("employees.setSpacing(2)")
    		  .addStatement("$T allEmps = $N.getEmployeeStateRepository().getAll()", vOfE, "state")
    		  .beginControlFlow("for (int i = 0; i < allEmps.size(); i++)")
    		  .addStatement("$T emp = allEmps.elementAt(i)", employee)
    		  .addCode(continueIfHire())
    		  .beginControlFlow("if (empsToEmpPanels.get(emp) == null)")
    		  .addStatement("$T tempPanel = new $T()", vbox, vbox)
    		  .addStatement("tempPanel.addEventHandler($T.ANY, this)", mouseevent)
    		  .addStatement("empsToEmpPanels.put(emp, tempPanel)")
    		  .endControlFlow()
    		  .beginControlFlow("if (empsToPicPanels.get(emp) == null)")
    		  .addStatement("$T tempPanel = new $T()", hbox, hbox)
    		  .addStatement("tempPanel.addEventHandler($T.ANY, this)", mouseevent)
    		  .addStatement("empsToPicPanels.put(emp, tempPanel)")
    		  .endControlFlow()
    		  .addStatement("$T empPanel = empsToEmpPanels.get(emp)", vbox)
    		  .addStatement("empPanel.getChildren().removeAll()")
    		  .addStatement("$T picPanel = ($T) empsToPicPanels.get(emp)", hbox, hbox)
    		  .addStatement("picPanel.setSpacing(5)")
    		  .addStatement("picPanel.setAlignment($T.BASELINE_LEFT)", pos)
    		  .addStatement("picPanel.getChildren().removeAll()")
    		  .addStatement("")
    		  .addStatement("$T gpLayout = new $T()", gridpane, gridpane)
    		  .addStatement("gpLayout.getChildren().add(empPanel)")
    		  .addStatement("")
    		  .addStatement("empPanel.setId(\"ActionPanelEmployee\")")
    		  .addStatement("picPanel.setId(\"ActionPanelEmployeeBox\")")
    		  .addStatement("picPanel.prefWidthProperty().bind($N.widthProperty())", "employeePane")
    		  .addStatement("empPanel.prefWidthProperty().bind($N.widthProperty())", "employeePane")
    		  .beginControlFlow("if (empsToPicLabels.get(emp) == null)")
    		  .addStatement("$T ico = allEmps.get(i).getCharacterModel().getDisplayedCharacter(true)", imageview)
    		  .addStatement("")
    		  .addStatement("ico.setFitHeight(40)")
    		  .addStatement("ico.setFitWidth(40)")
    		  .addStatement("$T temp = new $T()", label, label)
    		  .addStatement("temp.setGraphic(ico)")
    		  .addStatement("temp.addEventHandler($T.ANY, this)", mouseevent)
    		  .addStatement("empsToPicLabels.put(emp, temp)")
    		  .endControlFlow()
    		  .addStatement("$T picLabel = empsToPicLabels.get(emp)", label)
    		  .addStatement("picLabel.setAlignment($T.BASELINE_LEFT)", pos)
    		  .addStatement("picLabel.setId(\"EmployeePic\")")
    		  .beginControlFlow("if(!picPanel.getChildren().contains(picLabel))")
    		  .addStatement("picPanel.getChildren().add(picLabel)")
    		  .endControlFlow()
    		  .addCode(getEmployeeTypes())
    		  .addStatement("picPanel.setBorder(Border.EMPTY)")
    		  .beginControlFlow("if(!empPanel.getChildren().contains(picPanel))")
    		  .addStatement("empPanel.getChildren().add(picPanel)")
    		  .endControlFlow()
    		  .addStatement("$T actsPanel = new $T()", vbox, vbox)
    		  .addStatement("")
    		  .addStatement("actsPanel.setBackground($T.createBackground$T(Color.rgb(102, 102, 102, 1)))", javafxhelpers, color)
    		  .addStatement("empPanel.set$T(new Border(new $T($T.rgb(102, 102, 102, 1), $T.SOLID, $T.EMPTY, $T.FULL)))", border, borderstroke, color, borderstrokestyle, cornerradii, borderwidths)
    		  .addStatement("Vector<Action> acts = $N.getActionStateRepository().getAllActions(emp)", "state")
    		  .beginControlFlow("for (int j = 0; j < acts.size(); j++)")
    		  .addStatement("$T tempAct = acts.elementAt(j)", action)
    		  .addCode(getActionLables())
    		  .endControlFlow()
    		  .addStatement("actsPanel.setPrefSize(150, (int) (actsPanel.getPrefHeight()))")
    		  .addStatement("empPanel.getChildren().add(actsPanel)")
    		  .addStatement("employees.getChildren().add(empPanel)")
    		  .endControlFlow()
    		  .addStatement("$N.setContent(employees)", "employeePane")
    		  .build();	
      

      MethodSpec popupActions = MethodSpec.methodBuilder("popupMenuActions")
    		  .addModifiers(Modifier.PUBLIC)
    		  .returns(void.class)
    		  .addParameter(menuitem, "source")
    		  .addStatement("$T item = ($T) source", menuitem, menuitem)
    		  .addStatement("$N.getMenuInputManager().menuItemSelected(selectedEmp, item.getText(), $N)", "logic", "mainGUIFrame")
    		  .addStatement("$N.getWorld().update()", "mainGUIFrame")
    		  .build();

      MethodSpec empFromLabel = MethodSpec.methodBuilder("getEmpFromPicLabel")
    		  .addModifiers(Modifier.PRIVATE)
    		  .returns(employee)
    		  .addParameter(label, "label")
    		  .beginControlFlow("for ($T keys = empsToPicLabels.keys(); keys.hasMoreElements();)", enumOfE)
    		  .addStatement("$T keyEmp = keys.nextElement()", employee)
    		  .beginControlFlow("if (empsToPicLabels.get(keyEmp) == label)")
    		  .addStatement("return keyEmp")
    		  .endControlFlow()
    		  .endControlFlow()
    		  .addStatement("return null")
    		  .build();
      
      
      MethodSpec empFromPanel = MethodSpec.methodBuilder("getEmpFromPanel")
    		  .addModifiers(Modifier.PRIVATE)
    		  .returns(employee)
    		  .addParameter(pane, "panel")
    		  .beginControlFlow("for ($T keys = empsToEmpPanels.keys(); keys.hasMoreElements();)", enumOfE)
    		  .addStatement("$T keyEmp = keys.nextElement()", employee)
    		  .beginControlFlow("if (empsToEmpPanels.get(keyEmp) == panel)")
    		  .addStatement("return keyEmp")
    		  .endControlFlow()
    		  .endControlFlow()
    		  .beginControlFlow("for ($T keys = empsToPicPanels.keys(); keys.hasMoreElements();)", enumOfE)
    		  .addStatement("$T keyEmp = keys.nextElement()", employee)
    		  .beginControlFlow("if (empsToEmpPanels.get(keyEmp) == panel)")
    		  .addStatement("return keyEmp")
    		  .endControlFlow()
    		  .endControlFlow()
    		  .addStatement("return null")
    		  .build();
      
      MethodSpec handle = MethodSpec.methodBuilder("handle")
    		  .addAnnotation(Override.class)
    		  .addModifiers(Modifier.PUBLIC)
    		  .returns(void.class)
    		  .addParameter(mouseevent, "event")
    		  .beginControlFlow("if (event.getEventType() == $T.MOUSE_RELEASED)", mouseevent)
    		  .beginControlFlow("if (event.getSource() instanceof $T)", label)
    		  .addStatement("$T label = ($T) event.getSource()", label, label)
    		  .addStatement("$T emp = getEmpFromPicLabel(label)", employee)
    		  .beginControlFlow("if (emp != null)")
    		  .beginControlFlow("if (event.getButton().equals($T.PRIMARY))", mousebutton)
    		  .addStatement("$N.getTabPanel().setGUIChanged()", "mainGUIFrame")
    		  .addStatement("$N.getTabPanel().setObjectInFocus(emp)", "mainGUIFrame")
    		  .addStatement("$N.getAttributePanel().setGUIChanged()", "mainGUIFrame")
    		  .addStatement("$N.getAttributePanel().setObjectInFocus(emp, $T.createImage(TabPanel.getImage(emp)))", "mainGUIFrame", javafxhelpers)
    		  .nextControlFlow("else if (event.isPopupTrigger() && (state.getClock().isStopped() == false))")
    		  .addStatement("$N = emp", "selectedEmp")
    		  .addStatement("createPopupMenu(label, event.getScreenX(), event.getScreenY())")
    		  .endControlFlow()
    		  .endControlFlow()
    		  .nextControlFlow("else if (event.getSource() instanceof $T)", pane)
    		  .addStatement("$T pane = ($T) event.getSource()", pane, pane)
    		  .addStatement("$T emp = getEmpFromPanel(pane)", employee)
    		  .beginControlFlow("if (emp != null)")
    		  .beginControlFlow("if (event.getButton().equals($T.PRIMARY))", mousebutton)
    		  .addStatement("$N.getTabPanel().setGUIChanged()", "mainGUIFrame")
    		  .addStatement("$N.getTabPanel().setObjectInFocus(emp)", "mainGUIFrame")
    		  .addStatement("$N.getAttributePanel().setGUIChanged()", "mainGUIFrame")
    		  .addStatement("$N.getAttributePanel().setObjectInFocus(emp,JavaFXHelpers.createImage(TabPanel.getImage(emp)))", "mainGUIFrame")
    		  .nextControlFlow("else if (event.isPopupTrigger() && (state.getClock().isStopped() == false))")
    		  .addStatement("$N = emp", "selectedEmp")
    		  .addStatement("createPopupMenu(pane, event.getScreenX(), event.getScreenY())")
    		  .endControlFlow()
    		  .endControlFlow()
    		  .endControlFlow()
    		  .endControlFlow()
    		  .build();
      
      MethodSpec getPanelType = MethodSpec.methodBuilder("getPanelType")
    		  .addAnnotation(Override.class)
    		  .addModifiers(Modifier.PUBLIC)
    		  .returns(panels)
    		  .addStatement("return $T.EMPLOYEES", panels)
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
      
      TypeSpec actionPanel = TypeSpec.classBuilder("EmployeesPanel")
    		  .addModifiers(Modifier.PUBLIC)
    		  .superclass(pane)
    		  .addSuperinterface(mouseHandler)
    		  .addSuperinterface(simsepanel)
    		  .addField(state, "state", Modifier.PRIVATE)
    		  .addField(logic, "logic", Modifier.PRIVATE)
    		  .addField(simsegui, "mainGUIFrame", Modifier.PRIVATE)
    		  .addField(contextmenu, "popup", Modifier.PRIVATE)
    		  .addField(employee, "selectedEmp", Modifier.PRIVATE)
    		  .addField(scrollpane, "employeePane", Modifier.PRIVATE)
    		  .addField(hashEmpVB, "empsToEmpPanels", Modifier.PRIVATE)
    		  .addField(hashEmpHB, "empsToPicPanels", Modifier.PRIVATE)
    		  .addField(hashEmpLab, "empsToPicLabels", Modifier.PRIVATE)
    		  .addField(hashEmpLab, "empsToKeyLabels", Modifier.PRIVATE)
    		  .addField(listOfDC, "characters")
    		  .addField(vbox, "layout", Modifier.PRIVATE)
    		  .addField(FieldSpec.builder(actionHandler, "menuItemEvent", Modifier.PRIVATE)
    				  .initializer("$L", anon)
    				  .build())
    		  .addMethod(constructor)
    		  .addMethod(createPopup)
    		  .addMethod(update)
    		  .addMethod(popupActions)
    		  .addMethod(empFromLabel)
    		  .addMethod(empFromPanel)
    		  .addMethod(handle)
    		  .addMethod(getPanelType)
    		  .build();
      
      JavaFile file = JavaFile.builder("", actionPanel)
    		  .build();
      
      String fileString = "package simse.gui;\n\nimport javafx.scene.text.TextAlignment;\n";
      for (String type : impTypes) {
			fileString = fileString + "import simse.adts.objects." + type + ";\n";
		}
      
      for (String actionI : impActions) {
    	  fileString = fileString + "import simse.adts.actions." + actionI +"Action;\n";
      }
		
		fileString = fileString + file.toString();
		
		
		writer.write(fileString);
      
      writer.close();
    } catch (IOException e) {
      JOptionPane.showMessageDialog(null, ("Error writing file "
          + actPanelFile.getPath() + ": " + e.toString()), "File IO Error",
          JOptionPane.WARNING_MESSAGE);
    }
  }
  
  private String getActionLables() {
	  String actions = "";
	  Vector<ActionType> allActs = actTypes.getAllActionTypes();
	        boolean putElse = false;
	        for (ActionType tempActType : allActs) {
	          if ((tempActType.isVisibleInSimulation())
	              && (tempActType.getDescription() != null)
	              && (tempActType.getDescription().length() > 0)) {
	        	if (!this.impActions.contains(CodeGeneratorUtils.getUpperCaseLeading(tempActType.getName()))) {
	        		this.impActions.add(CodeGeneratorUtils.getUpperCaseLeading(tempActType.getName()));
	        	}
	            if (putElse) {
	            	actions = actions.concat("else ");
	            } else {
	              putElse = true;
	            }
	            actions = actions.concat("if(tempAct instanceof "
	                + CodeGeneratorUtils.getUpperCaseLeading(tempActType.getName()) + 
	                "Action)");
	            actions = actions.concat("\n");
	            actions = actions.concat("{");
	            actions = actions.concat("\n");
	            actions = actions.concat("Label tempLabel = new Label(\""
	                + tempActType.getDescription() + "\");");
	            actions = actions.concat("\n");
	            actions = actions.concat("tempLabel.setFont(new Font(tempLabel.getFont().getName(), 10));");
	            actions = actions.concat("\n");
	            actions = actions.concat("tempLabel.setTextFill(Color.WHITE);");
	            actions = actions.concat("\n");
	            actions = actions.concat("actsPanel.getChildren().add(tempLabel);");
	            actions = actions.concat("\n");
	            actions = actions.concat("}");
	            actions = actions.concat("\n");
	          }
	        }
	       return actions;
	      
  }
  
  private String continueIfHire() {
	  String hire = "";
	  if (CodeGenerator.allowHireFire) {
		  hire = hire.concat("if (!emp.getHired())\n");
		  hire = hire.concat("continue;\n");
	      }
	  return hire;
  }
  
  private String getEmployeeTypes() {
	  String emps = "";
	  Vector<SimSEObjectType> empTypes = objTypes
	            .getAllObjectTypesOfType(SimSEObjectTypeTypes.EMPLOYEE);
	        for (int i = 0; i < empTypes.size(); i++) {
	          SimSEObjectType tempType = empTypes.elementAt(i);
	          if (!this.impTypes.contains(CodeGeneratorUtils.getUpperCaseLeading(tempType.getName()))) {
	        	  this.impTypes.add(CodeGeneratorUtils.getUpperCaseLeading(tempType.getName()));
	          }
	          if (i > 0) {
	        	  emps = emps.concat("else ");
	          }

	          Vector<Attribute> v = tempType.getAllAttributes();
	          Attribute keyAtt = null;
	          for (Attribute att : v) {
	            if (att.isKey())
	              keyAtt = att;
	          }

	          emps = emps.concat("if(emp instanceof "
	              + CodeGeneratorUtils.getUpperCaseLeading(tempType.getName()) + ")");
	          emps = emps.concat("\n");
	          emps = emps.concat("{");
	          emps = emps.concat("\n");
	          emps = emps.concat(CodeGeneratorUtils.getUpperCaseLeading(tempType.getName()) 
	          		+ " e = (" + 
	          		CodeGeneratorUtils.getUpperCaseLeading(tempType.getName()) + 
	          		")emp;");
	          emps = emps.concat("\n");
	          emps = emps.concat("if(empsToKeyLabels.get(e) == null)");
	          emps = emps.concat("\n");
	          emps = emps.concat("{");
	          emps = emps.concat("\n");
	          emps = emps.concat("Label temp = new Label(\"\" + e.get"
	              + CodeGeneratorUtils.getUpperCaseLeading(keyAtt.getName()) + 
	              "());");
	          emps = emps.concat("\n");
	          emps = emps.concat("temp.setTextFill(Color.BLACK);");
	          emps = emps.concat("\n");
	          emps = emps.concat("temp.setAlignment(Pos.BASELINE_LEFT);");
	          emps = emps.concat("\n");
	          emps = emps.concat("temp.setTextAlignment(TextAlignment.LEFT);");
	          emps = emps.concat("\n");
	          emps = emps.concat("empsToKeyLabels.put(e, temp);");
	          emps = emps.concat("\n");
	          emps = emps.concat("}");
	          emps = emps.concat("\n");
	          emps = emps.concat("Label keyLabel = empsToKeyLabels.get(e);");
	          emps = emps.concat("\n");
	          emps = emps.concat("keyLabel.setId(\"EmployeeName\");");
	          emps = emps.concat("\n");
	          emps = emps.concat("if(!picPanel.getChildren().contains(keyLabel))");
	          emps = emps.concat("{");
	          emps = emps.concat("\n");
	          emps = emps.concat("picPanel.getChildren().add(keyLabel);");
	          emps = emps.concat("\n");
	          emps = emps.concat("}");
	          emps = emps.concat("\n");
	          emps = emps.concat("}");
	          emps = emps.concat("\n");
	        }
	  return emps;
  }
}