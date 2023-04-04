/*
 * This class is responsible for generating all of the code for the tab panel in
 * the GUI
 */

package simse.codegenerator.guigenerator;

import simse.codegenerator.CodeGenerator;
import simse.codegenerator.CodeGeneratorConstants;
import simse.codegenerator.CodeGeneratorUtils;

import simse.modelbuilder.objectbuilder.AttributeTypes;
import simse.modelbuilder.objectbuilder.DefinedObjectTypes;
import simse.modelbuilder.objectbuilder.SimSEObjectType;
import simse.modelbuilder.objectbuilder.SimSEObjectTypeTypes;
import simse.modelbuilder.startstatebuilder.InstantiatedAttribute;
import simse.modelbuilder.startstatebuilder.SimSEObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.lang.model.element.Modifier;
import javax.swing.JOptionPane;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class TabPanelGenerator implements CodeGeneratorConstants {
  private File directory; // directory to save generated code into
  private File iconDir;
  private DefinedObjectTypes objTypes; // holds all of the defined object types
                                       // from an sso file
  private Hashtable<SimSEObject, String> objsToImages; // maps SimSEObjects
  private ArrayList<String> impTypes;
																												// (keys) to pathname
																												// (String) of image
																												// file (values)

  public TabPanelGenerator(DefinedObjectTypes objTypes, Hashtable<SimSEObject, 
  		String> objsToImages, File directory, File iconDir) {
    this.objTypes = objTypes;
    this.directory = directory;
    this.iconDir = iconDir;
    this.objsToImages = objsToImages;
    this.impTypes = new ArrayList<>();
  }

  public void generate() {
    // generate file:
    File tabPanelFile = new File(directory, ("simse\\gui\\TabPanel.java"));
    if (tabPanelFile.exists()) {
      tabPanelFile.delete(); // delete old version of file
    }
    try {
    	
    	ClassName enumeration = ClassName.get("java.util", "Enumeration");
    	ClassName hashtable = ClassName.get("java.util", "Hashtable");
    	ClassName vector = ClassName.get("java.util", "Vector");
    	ClassName actionevent = ClassName.get("javafx.event", "ActionEvent");
    	ClassName event = ClassName.get("javafx.event", "Event");
    	ClassName eventhandler = ClassName.get("javafx.event", "EventHandler");
    	ClassName hpos = ClassName.get("javafx.geometry", "HPos");
    	ClassName insets = ClassName.get("javafx.geometry", "Insets");
    	ClassName vpos = ClassName.get("javafx.geometry", "VPos");
    	ClassName button = ClassName.get("javafx.scene.control", "Button");
    	ClassName contextmenu = ClassName.get("javafx.scene.control", "ContextMenu");
    	ClassName menuitem = ClassName.get("javafx.scene.control", "MenuItem");
    	ClassName scrollpane = ClassName.get("javafx.scene.control", "ScrollPane");
    	ClassName image = ClassName.get("javafx.scene.image", "Image");
    	ClassName imageview = ClassName.get("javafx.scene.image", "ImageView");
    	ClassName mouseevent = ClassName.get("javafx.scene.input", "MouseEvent");
    	ClassName border = ClassName.get("javafx.scene.layout", "Border");
    	ClassName borderstroke = ClassName.get("javafx.scene.layout", "BorderStroke");
    	ClassName borderstrokestyle = ClassName.get("javafx.scene.layout", "BorderStrokeStyle");
    	ClassName borderwidths = ClassName.get("javafx.scene.layout", "BorderWidths");
    	ClassName columnconstraints = ClassName.get("javafx.scene.layout", "ColumnConstraints");
    	ClassName cornerradii = ClassName.get("javafx.scene.layout", "CornerRadii");
    	ClassName flowpane = ClassName.get("javafx.scene.layout", "FlowPane");
    	ClassName gridpane = ClassName.get("javafx.scene.layout", "GridPane");
    	ClassName hbox = ClassName.get("javafx.scene.layout", "HBox");
    	ClassName pane = ClassName.get("javafx.scene.layout", "Pane");
    	ClassName priority = ClassName.get("javafx.scene.layout", "Priority");
    	ClassName color = ClassName.get("javafx.scene.paint", "Color");
    	ClassName acustomer = ClassName.get("simse.adts.objects", "ACustomer");
    	ClassName automatedtestingtool = ClassName.get("simse.adts.objects", "AutomatedTestingTool");
    	ClassName code = ClassName.get("simse.adts.objects", "Code");
    	ClassName designdocument = ClassName.get("simse.adts.objects", "DesignDocument");
    	ClassName designenvironment = ClassName.get("simse.adts.objects", "DesignEnvironment");
    	ClassName employee = ClassName.get("simse.adts.objects", "Employee");
    	ClassName ide = ClassName.get("simse.adts.objects", "IDE");
    	ClassName requirementscapturetool = ClassName.get("simse.adts.objects", "RequirementsCaptureTool");
    	ClassName requirementsdocument = ClassName.get("simse.adts.objects", "RequirementsDocument");
    	ClassName seproject = ClassName.get("simse.adts.objects", "SEProject");
    	ClassName ssobject = ClassName.get("simse.adts.objects", "SSObject");
    	ClassName softwareengineer = ClassName.get("simse.adts.objects", "SoftwareEngineer");
    	ClassName systemtestplan = ClassName.get("simse.adts.objects", "SystemTestPlan");
    	ClassName engine = ClassName.get("simse.engine", "Engine");
    	ClassName explanatorytool = ClassName.get("simse.explanatorytool", "ExplanatoryTool");
    	ClassName javafxhelpers = ClassName.get("simse.gui.util", "JavaFXHelpers");
    	ClassName logic = ClassName.get("simse.logic", "Logic");
    	ClassName state = ClassName.get("simse.state", "State");
    	ClassName simsegui = ClassName.get("simse.gui", "SimSEGUI");
    	ClassName clockpanel = ClassName.get("simse.gui", "ClockPanel");
    	ClassName informationpanel = ClassName.get("simse.gui", "InformationPanel");
    	ClassName logopanel = ClassName.get("simse.gui", "LogoPanel");
    	ClassName employeesoverviewscreen = ClassName.get("simse.gui", "EmployeesOverviewScreen");
    	ClassName artifactsoverviewscreen = ClassName.get("simse.gui", "ArtifactsOverviewScreen");
    	ClassName projectoverviewscreen = ClassName.get("simse.gui", "ProjectOverviewScreen");
    	ClassName panelsscreen = ClassName.get("simse.gui", "PanelsScreen");
    	ClassName trackpanel = ClassName.get("simse.gui", "TrackPanel");
    	ClassName mellopanel = ClassName.get("simse.gui", "MelloPanel");
    	TypeName hashObjImg = ParameterizedTypeName.get(hashtable, ssobject, imageview);
    	TypeName hashButtonObj = ParameterizedTypeName.get(hashtable, button, ssobject);
    	TypeName actionHandler = ParameterizedTypeName.get(eventhandler, actionevent);
    	TypeName eventHandler = ParameterizedTypeName.get(eventhandler, event);
    	TypeName buttonArray = ArrayTypeName.of(button);
    	
      FileWriter writer = new FileWriter(tabPanelFile);
      writer
          .write("/* File generated by: simse.codegenerator.guigenerator.TabPanelGenerator */");

      
      String[] types = SimSEObjectTypeTypes.getAllTypesAsStrings();
      
      TypeSpec projButt = TypeSpec.anonymousClassBuilder("")
    		  .addSuperinterface(actionHandler)
    		  .addMethod(MethodSpec.methodBuilder("handle")
    				  .addAnnotation(Override.class)
    				  .addModifiers(Modifier.PUBLIC)
    				  .returns(void.class)
    				  .addParameter(actionevent, "e")
    				  .beginControlFlow("if ($N.isIconified())", "projectFrame")
    				  .addStatement("$N.setIconified(false)", "projectFrame")
    				  .endControlFlow()
    				  .addStatement("$N.show()", "projectFrame")
    				  .build())
    		  .build();
      
      TypeSpec peopButt = TypeSpec.anonymousClassBuilder("")
    		  .addSuperinterface(actionHandler)
    		  .addMethod(MethodSpec.methodBuilder("handle")
    				  .addAnnotation(Override.class)
    				  .addModifiers(Modifier.PUBLIC)
    				  .returns(void.class)
    				  .addParameter(actionevent, "e")
    				  .beginControlFlow("if ($N.isIconified())", "employeeFrame")
    				  .addStatement("$N.setIconified(false)", "employeeFrame")
    				  .endControlFlow()
    				  .addStatement("$N.update(true)", "employeeFrame")
    				  .addStatement("$N.show()", "employeeFrame")
    				  .build())
    		  .build();
      
      TypeSpec artButt = TypeSpec.anonymousClassBuilder("")
    		  .addSuperinterface(actionHandler)
    		  .addMethod(MethodSpec.methodBuilder("handle")
    				  .addAnnotation(Override.class)
    				  .addModifiers(Modifier.PUBLIC)
    				  .returns(void.class)
    				  .addParameter(actionevent, "e")
    				  .beginControlFlow("if ($N.isIconified())", "artifactFrame")
    				  .addStatement("$N.setIconified(false)", "artifactFrame")
    				  .endControlFlow()
    				  .addStatement("$N.show()", "artifactFrame")
    				  .build())
    		  .build();
      
      TypeSpec analButt = TypeSpec.anonymousClassBuilder("")
    		  .addSuperinterface(actionHandler)
    		  .addMethod(MethodSpec.methodBuilder("handle")
    				  .addAnnotation(Override.class)
    				  .addModifiers(Modifier.PUBLIC)
    				  .returns(void.class)
    				  .addParameter(actionevent, "e")
    				  .beginControlFlow("if (expTool.isIconified())")
    				  .addStatement("expTool.setIconified(false)")
    				  .endControlFlow()
    				  .addStatement("expTool.show()")
    				  .build())
    		  .build();
      
      TypeSpec panelButt = TypeSpec.anonymousClassBuilder("")
    		  .addSuperinterface(actionHandler)
    		  .addMethod(MethodSpec.methodBuilder("handle")
    				  .addAnnotation(Override.class)
    				  .addModifiers(Modifier.PUBLIC)
    				  .returns(void.class)
    				  .addParameter(actionevent, "e")
    				  .beginControlFlow("if ($N.isIconified())", "panelsFrame")
    				  .addStatement("$N.setIconified(false)", "panelsFrame")
    				  .endControlFlow()
    				  .addStatement("$N.show()", "panelsFrame")
    				  .build())
    		  .build();

      // constructor:
      MethodSpec constructor = MethodSpec.constructorBuilder()
    		  .addModifiers(Modifier.PUBLIC)
    		  .addParameter(simsegui, "g")
    		  .addParameter(state, "s")
    		  .addParameter(logic, "l")
    		  .addParameter(engine, "e")
    		  .addParameter(informationpanel, "a")
    		  .addParameter(explanatorytool, "expTool")
    		  .addStatement("$N = l", "logic")
    		  .addStatement("$N = g", "gui")
    		  .addStatement("$N = s", "state")
    		  .addStatement("$N = true", "guiChanged")
    		  .addStatement("$N = a", "attributePane")
    		  .addStatement("this.$N = $N", "expTool", "expTool")
    		  .addStatement("$N = new $T()", "objsToImages", hashObjImg)
    		  .addStatement("$N = new $T()", "buttonsToObjs", hashButtonObj)
    		  .addStatement("$N = new ArtifactsOverviewScreen(state, gui, l)", "artifactFrame")
    		  .addStatement("$N = new EmployeesOverviewScreen(state, gui, $N)", "employeeFrame", "logic")
    		  .addStatement("$N = new ProjectOverviewScreen(state, $N)", "projectFrame", "gui")
    		  .addStatement("$N = new PanelsScreen(state, gui, $N)", "panelsFrame", "logic")
    		  .addStatement("$N = TrackPanel.getInstance($N)", "trackPane", "state")
    		  .addStatement("$N = MelloPanel.getInstance($N)", "melloPane", "state")
    		  .addStatement("$N = $T.createImage(\"src/simse/gui/images/layout/border.gif\")", "border", javafxhelpers)
    		  .addStatement("$N = $T.createImage(\"src/simse/gui/images/all.GIF\")", "allIcon", javafxhelpers)
    		  .addStatement("// get the $T styles:", border)
    		  .addStatement("$N = new $T().getBorder()", "defaultBorder", button)
    		  .addStatement("$N = new $T(new $T($T.BLACK, $T.SOLID, $T.EMPTY, $T.DEFAULT))", "selectedBorder", border, borderstroke, color, borderstrokestyle, cornerradii, borderwidths)
    		  .addStatement("// Create main panel:")
    		  .addStatement("$N = new $T()", "gridPane", gridpane)
    		  .addStatement("$N.setPrefWidth(1024)", "gridPane")
    		  .addStatement("$N = new LogoPanel($N)", "logoPane", "gui")
    		  .addStatement("$N.setMinSize(340, 90)", "logoPane")
    		  .addStatement("$N.setPrefSize(340, 90)", "logoPane")
    		  .addStatement("$N.setTabPanel(this)", "logoPane")
    		  .addStatement("// Create buttons pane:")
    		  .addStatement("$N = new $T()", "buttonsPane", flowpane)
    		  .addStatement("$N.setBackground($T.createBackground$T(Color.LIGHTGRAY))", "buttonsPane", javafxhelpers, color)
    		  .addStatement("$T buttons$T = new ScrollPane($N)", scrollpane, scrollpane, "buttonsPane")
    		  .addStatement("buttonsScrollPane.setPrefSize(292, 75)")
    		  .addStatement("generateButtons()")
    		  .addStatement("$N = new ClockPanel(gui, s, e)", "clockPane")
    		  .addStatement("$N.setPrefSize(250, 100)", "clockPane")
    		  .addStatement("// Add panes and labels to main pane:")
    		  .addStatement("$N.setHgap(10)", "gridPane")
    		  .addStatement("$N.setVgap(10)", "gridPane")
    		  .addStatement("$N.setPadding(new $T(0, 0, 0, 0))", "gridPane", insets)
    		  .addStatement("$N.get$T().add(new ColumnConstraints($N.getWidth() + 100))", "gridPane", columnconstraints, "logoPane")
    		  .addStatement("// Add Logo Pane:")
    		  .addStatement("$T.setConstraints(logoPane, 0, 0, 2, 1, $T.LEFT, $T.TOP, $T.NEVER, $T.NEVER, new $T(0, 0, 0, 0))", gridpane, hpos, vpos, priority, priority, insets)
    		  .addStatement("$N.add(logoPane, 0, 0)", "gridPane")
    		  .addStatement("// Add panes and labels to main pane")
    		  .addStatement("$T buttons = new $T()", hbox, hbox)
    		  .addStatement("buttons.setSpacing(40)")
    		  .addStatement("$T $N = new $T(\"Project\")", button, "projectButton", button)
    		  .addStatement("$N.setId(\"TabButton\")", "projectButton")
    		  .addStatement("$N.setPrefHeight(40)", "projectButton")
    		  .addStatement("$T.setMargin(projectButton, new $T(15, 0, 0, 0))", hbox, insets)
    		  .addStatement("$N.setOnAction($L)", "projectButton", projButt)
    		  .addStatement("buttons.getChildren().add($N)", "projectButton")
    		  .addStatement("$T people$T = new Button(\"People\")", button, button)
    		  .addStatement("peopleButton.setId(\"TabButton\")")
    		  .addStatement("peopleButton.setPrefHeight(40)")
    		  .addStatement("$T.setMargin(peopleButton, new $T(15, 0, 0, 0))", hbox, insets)
    		  .addStatement("peopleButton.setOnAction($L)", peopButt)
    		  .addStatement("buttons.getChildren().add(peopleButton)")
    		  .addStatement("$T artifacts$T = new Button(\"Artifacts\")", button, button)
    		  .addStatement("artifactsButton.setId(\"TabButton\")")
    		  .addStatement("artifactsButton.setPrefHeight(40)")
    		  .addStatement("$T.setMargin(artifactsButton, new $T(15, 0, 0, 0))", hbox, insets)
    		  .addStatement("artifactsButton.setOnAction($L)", artButt)
    		  .addStatement("buttons.getChildren().add(artifactsButton)")
    		  .addStatement("$T analyze$T = new Button(\"Analyze\")", button, button)
    		  .addStatement("analyzeButton.setId(\"TabButton\")")
    		  .addStatement("analyzeButton.setPrefHeight(40)")
    		  .addStatement("$T.setMargin(analyzeButton, new $T(15, 0, 0, 0))", hbox, insets)
    		  .addStatement("analyzeButton.setOnAction($L)", analButt)
    		  .addStatement("buttons.getChildren().add(analyzeButton)")
    		  .addStatement("$T panels$T = new Button(\"Panels\")", button, button)
    		  .addStatement("panelsButton.setId(\"TabButton\")")
    		  .addStatement("panelsButton.setPrefHeight(40)")
    		  .addStatement("$T.setMargin(panelsButton, new $T(15, 0, 0, 0))", hbox, insets)
    		  .addStatement("panelsButton.setOnAction($L)", panelButt)
    		  .addStatement("buttons.getChildren().add(panelsButton)")
    		  .addStatement("$N.add(buttons, 2, 0)", "gridPane")
    		  .addStatement("$N.add(clockPane, 4, 0)", "gridPane")
    		  .addStatement("setPrefSize(1920, 100)")
    		  .addStatement("updateImages(EMPLOYEE)")
    		  .addStatement("this.getChildren().add($N)", "gridPane")
    		  .addStatement("this.setBackground($T.createBackground$T(Color.rgb(102, 102, 102, 1)))", javafxhelpers, color)

    		  .build();
      
      
      MethodSpec genButtons = MethodSpec.methodBuilder("generateButtons")
    		  .addModifiers(Modifier.PUBLIC)
    		  .returns(void.class)
    		  .addStatement("$N = new Button[MAXBUTTONS]", "artifactButton")
    		  .addStatement("$N = new Button[MAXBUTTONS]", "customerButton")
    		  .addStatement("$N = new Button[MAXBUTTONS]", "employeeButton")
    		  .addStatement("$N = new Button[MAXBUTTONS]", "projectButton")
    		  .addStatement("$N = new Button[MAXBUTTONS]", "toolButton")
    		  .beginControlFlow("for (int i = 0; i < MAXBUTTONS; i++)")
    		  .addStatement("artifact$T[i] = new Button()", button)
    		  .addStatement("artifactButton[i].addEventHandler($T.ACTION, this)", actionevent)
    		  .addStatement("customer$T[i] = new Button()", button)
    		  .addStatement("customerButton[i].addEventHandler($T.ACTION, this)", actionevent)
    		  .addStatement("employee$T[i] = new Button()", button)
    		  .addStatement("employeeButton[i].addEventHandler($T.ACTION, this)", actionevent)
    		  .addStatement("project$T[i] = new Button()", button)
    		  .addStatement("projectButton[i].addEventHandler($T.ACTION, this)", actionevent)
    		  .addStatement("tool$T[i] = new Button()", button)
    		  .addStatement("toolButton[i].addEventHandler($T.ACTION, this)", actionevent)
    		  .addStatement("$T popup = new $T()", contextmenu, contextmenu)
    		  .addStatement("PopupListener popupListener = new PopupListener(popup, $N)", "gui")
    		  .addStatement("popupListener.setEnabled(false)")
    		  .addStatement("employeeButton[i].addEventHandler($T.ANY, popupListener)", mouseevent)
    		  .addStatement("employeeButton[i].addEventHandler($T.MOUSE_RELEASED, this)", mouseevent)
    		  .endControlFlow()
    		  .addStatement("setButtonConstraints(artifactButton, $N)", "buttonsPane")
    		  .addStatement("setButtonConstraints(customerButton, $N)", "buttonsPane")
    		  .addStatement("setButtonConstraints(employeeButton, $N)", "buttonsPane")
    		  .addStatement("setButtonConstraints(projectButton, $N)", "buttonsPane")
    		  .addStatement("setButtonConstraints(toolButton, $N)", "buttonsPane")
    		  .build();

      
      MethodSpec setConstraints = MethodSpec.methodBuilder("setButtonConstraints")
    		  .addModifiers(Modifier.PUBLIC)
    		  .returns(void.class)
    		  .addParameter(buttonArray, "button")
    		  .addParameter(pane, "pane")
    		  .addStatement("int shift")
    		  .addStatement("int index")
    		  .beginControlFlow("for (int j=0; j<2; j++)")
    		  .addStatement("shift = 16*j")
    		  .beginControlFlow("for (int i=0; i<MAXBUTTONS/2; i++)")
    		  .addStatement("index = shift + i")
    		  .beginControlFlow("if (button == $N)", "employeeButton")
    		  .addStatement("button[index].disarm()")
    		  .endControlFlow()
    		  .addStatement("button[index].setBackground($T.createBackground$T(Color.LIGHTGRAY))", javafxhelpers, color)
    		  .addStatement("button[index].setBorder($N)", "defaultBorder")
    		  .addStatement("button[index].disarm()")
    		  .addStatement("$T.setConstraints(button[index], i, j, 1, 1, $T.LEFT, $T.TOP, $T.NEVER, $T.NEVER, new $T(2, 1, 0, 0))", gridpane, hpos, vpos, priority, priority, insets)
    		  .beginControlFlow("if (!pane.getChildren().contains(button[index]))")
    		  .addStatement("pane.getChildren().add(button[index])")
    		  .endControlFlow()
    		  .endControlFlow()
    		  .endControlFlow()
    		  .build();

      MethodSpec handle = MethodSpec.methodBuilder("handle")
    		  .addAnnotation(Override.class)
    		  .addModifiers(Modifier.PUBLIC)
    		  .returns(void.class)
    		  .addParameter(event, "event")
    		  .beginControlFlow("if (event.getEventType() == $T.MOUSE_RELEASED)", mouseevent)
    		  .beginControlFlow("if (event.getSource() instanceof $T)", button)
    		  .addStatement("$T button = ($T) event.getSource()", button, button)
    		  .addStatement("$T ico = ($T) button.getGraphic()", imageview, imageview)
    		  .beginControlFlow("if (ico != null)")
    		  .addStatement("$N = ($T) buttonsToObjs.get(button);", "rightClickedEmployee", employee)
    		  .endControlFlow()
    		  .endControlFlow()
    		  .nextControlFlow("else if (event.getEventType() == $T.ACTION)", actionevent)
    		  .addStatement("$N = true", "guiChanged")
    		  .addStatement("Object source = event.getSource()")
    		  .beginControlFlow("if (source instanceof $T)", button)
    		  .addStatement("$T button = ($T) source", button, button)
    		  .beginControlFlow("if ($N.get(button) != null)", "buttonsToObjs")
    		  .addStatement("$N.setGUIChanged()", "attributePane")
    		  .addStatement("$N = $N.get(button)", "objInFocus", "buttonsToObjs")
    		  .addStatement("String filename = getImage($N)", "objInFocus")
    		  .addStatement("$N.setObjectInFocus(objInFocus, $T.createImage(filename))", "attributePane", javafxhelpers)
    		  .addStatement("Enumeration<Button> buttons = $N.keys()", "buttonsToObjs")
    		  .beginControlFlow("for (int i=0; i<$N.size(); i++)", "buttonsToObjs")
    		  .addStatement("$T key = buttons.nextElement()", button)
    		  .addStatement("key.setBackground($T.createBackground$T(Color.LIGHTGRAY))", javafxhelpers, color)
    		  .addStatement("key.setBorder($N)", "defaultBorder")
    		  .endControlFlow()
    		  .addStatement("button.setBackground($T.createBackground$T(Color.LIGHTGRAY))", javafxhelpers, color)
    		  .addStatement("button.setBorder($N)", "selectedBorder")
    		  .endControlFlow()
    		  .endControlFlow()
    		  .endControlFlow()
    		  .build();
      
      MethodSpec setFocus = MethodSpec.methodBuilder("setObjectInFocus")
    		  .addModifiers(Modifier.PUBLIC)
    		  .returns(void.class)
    		  .addParameter(ssobject, "obj")
    		  .addStatement("$N=obj", "objInFocus")
    		  .addStatement("update()")
    		  .build();

      MethodSpec setGuiChanged = MethodSpec.methodBuilder("setGUIChanged")
    		  .addModifiers(Modifier.PUBLIC)
    		  .returns(void.class)
    		  .addStatement("$N=true", "guiChanged")
    		  .build();

      MethodSpec updateEmpty = MethodSpec.methodBuilder("update")
    		  .addModifiers(Modifier.PUBLIC)
    		  .returns(void.class)
    		  .addStatement("update($N.getSelectedTabIndex())", "logoPane")
    		  .addStatement("$N.update(employeeFrame.isEmployee())", "employeeFrame")
    		  .addStatement("$N.update()", "projectFrame")
    		  .addStatement("$N.update()", "artifactFrame")
    		  .addStatement("$N.update()", "panelsFrame")
    		  .addStatement("$N.update()", "clockPane")
    		  .build();
      
      String switchString = getSwitchStatement();
      
      MethodSpec updateParam = MethodSpec.methodBuilder("update")
    		  .addModifiers(Modifier.PUBLIC)
    		  .returns(void.class)
    		  .addParameter(int.class, "index")
    		  .beginControlFlow("if (!$N)", "guiChanged")
    		  .addStatement("return")
    		  .endControlFlow()
    		  .addStatement("// clear buttons:")
    		  .addStatement("$N.clear()", "buttonsToObjs")
    		  .addStatement("$N.getChildren().clear()", "buttonsPane")
    		  .addStatement("")
    		  .addStatement("// update images:")
    		  .addStatement("updateImages(index)")
    		  .addStatement("")
    		  .addStatement("Button[] buttonList")
    		  .addStatement("Vector<? extends SSObject> objs")
    		  .addCode(switchString)
    		  .addStatement("setButtonConstraints(buttonList, $N)", "buttonsPane")
    		  .addStatement("boolean atLeastOneObj = false")
    		  .beginControlFlow("if (objs.size()>0)")
    		  .addStatement("atLeastOneObj = true")
    		  .addStatement("$T allButton = buttonList[0]", button)
    		  .addStatement("allButton.arm()")
    		  .addStatement("allButton.setBorder($N)", "defaultBorder")
    		  .addStatement("$T allImage = new $T($N)", imageview, imageview, "allIcon")
    		  .addStatement("allImage.setPreserveRatio(true)")
    		  .addStatement("allImage.setFitHeight(30)")
    		  .addStatement("allImage.setFitWidth(30)")
    		  .addStatement("allButton.setGraphic(new $T($N))", imageview, "allIcon")
    		  .endControlFlow()
    		  .addStatement("int j=0")
    		  .beginControlFlow("for (int i=0; i<objs.size(); i++)")
    		  .addStatement("$T obj = objs.elementAt(i)", ssobject)
    		  .addStatement("$T button = null", button)
    		  .beginControlFlow("if (atLeastOneObj)")
    		  .addStatement("button=buttonList[++j]")
    		  .nextControlFlow("else")
    		  .addStatement("button=buttonList[j++]")
    		  .endControlFlow()
    		  .beginControlFlow("if ((index == EMPLOYEE) && ($N.getClock().isStopped() == false))", "state")
    		  .addStatement("$T e = ($T) obj", employee, employee)
    		  .addStatement("PopupListener pListener = ((PopupListener) button.getOnMousePressed())")
    		  .beginControlFlow("if (pListener != null)")
    		  .addStatement("pListener.setEnabled(true)")
    		  .addStatement("$T p = pListener.getPopupMenu()", contextmenu)
    		  .addStatement("p.getItems().removeAll()")
    		  .addStatement("Vector<String> v = e.getMenu()")
    		  .beginControlFlow("for (int k=0; k<v.size(); k++)")
    		  .addStatement("$T tempItem = new $T(v.elementAt(k))", menuitem, menuitem)
    		  .addStatement("tempItem.setOnAction(menuItemEvent)")
    		  .addStatement("p.getItems().add(tempItem)")
    		  .endControlFlow()
    		  .endControlFlow()
    		  .endControlFlow()
    		  .addStatement("button.arm()")
    		  .addStatement("button.setGraphic($N.get(obj))", "objsToImages")
    		  .beginControlFlow("if (obj.equals($N))", "objInFocus")
    		  .addStatement("button.setBackground($T.createBackground$T(Color.WHITE))", javafxhelpers, color)
    		  .addStatement("button.setBorder($N)", "selectedBorder")
    		  .nextControlFlow("else")
    		  .addStatement("button.setBackground($T.createBackground$T(Color.WHITE))", javafxhelpers, color)
    		  .addStatement("button.setBorder($N)", "defaultBorder")
    		  .endControlFlow()
    		  .addStatement("$N.put(button, obj)", "buttonsToObjs")
    		  .beginControlFlow("if (i == (MAXBUTTONS-1))")
    		  .addStatement("break")
    		  .endControlFlow()
    		  .endControlFlow()
    		  .addStatement("$N=false", "guiChanged")
    		  .build();
      
      String imageSwitch = getImageSwitch();
      
      MethodSpec updateImages = MethodSpec.methodBuilder("updateImages")
    		  .addModifiers(Modifier.PRIVATE)
    		  .returns(void.class)
    		  .addParameter(int.class, "index")
    		  .addStatement("Vector<? extends SSObject> objs")
    		  .addCode(imageSwitch)
    		  .beginControlFlow("for (int i=0; i<objs.size(); i++)")
    		  .addStatement("String filename = getImage(objs.elementAt(i))")
    		  .addStatement("$T scaledImage = $T.createImageView(filename)", imageview, javafxhelpers)
    		  .addStatement("scaledImage.setFitHeight(30)")
    		  .addStatement("scaledImage.setFitWidth(30)")
    		  .addStatement("scaledImage.setPreserveRatio(true)")
    		  .addStatement("$N.put(objs.elementAt(i), scaledImage)", "objsToImages")
    		  .endControlFlow()
    		  .build();

      String urlString = getUrlString();
      
      MethodSpec getImage = MethodSpec.methodBuilder("getImage")
    		  .addModifiers(Modifier.PUBLIC)
    		  .addModifiers(Modifier.STATIC)
    		  .returns(String.class)
    		  .addParameter(Object.class, "obj")
    		  .addStatement("$T url = \"\"", String.class)
    		  .addCode(urlString)
    		  .addStatement("return url")
    		  .build();

      MethodSpec getClock = MethodSpec.methodBuilder("getClockPanel")
    		  .addModifiers(Modifier.PUBLIC)
    		  .returns(clockpanel)
    		  .addStatement("return $N", "clockPane")
    		  .build();
      
      TypeSpec anon = TypeSpec.anonymousClassBuilder("")
    		  .addSuperinterface(actionHandler)
    		  .addMethod(MethodSpec.methodBuilder("handle")
    				  .addModifiers(Modifier.PUBLIC)
    				  .returns(void.class)
    				  .addParameter(actionevent, "event")
    				  .addStatement("$N=true", "guiChanged")
    				  .addStatement("Object source = event.getSource()")
    				  .beginControlFlow("if (source instanceof $T)", menuitem)
    				  .addStatement("$T jm = ($T) source", menuitem, menuitem)
    				  .addStatement("$N.getMenuInputManager().menuItemSelected(rightClickedEmployee, jm.getText(), $N)", "logic", "gui")
    				  .addStatement("$N.getWorld().update()", "gui")
    				  .endControlFlow()
    				  .build())
    		  .build();
      
      
      TypeSpec tabPanel = TypeSpec.classBuilder("TabPanel")
    		  .addModifiers(Modifier.PUBLIC)
    		  .superclass(pane)
    		  .addSuperinterface(eventHandler)
    		  .addField(FieldSpec.builder(int.class, "ARTIFACT", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
    				  .initializer("0")
    				  .build())
    		  .addField(FieldSpec.builder(int.class, "CUSTOMER", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
    				  .initializer("1")
    				  .build())
    		  .addField(FieldSpec.builder(int.class, "EMPLOYEE", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
    				  .initializer("2")
    				  .build())
    		  .addField(FieldSpec.builder(int.class, "PROJECT", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
    				  .initializer("3")
    				  .build())
    		  .addField(FieldSpec.builder(int.class, "TOOL", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
    				  .initializer("4")
    				  .build())
    		  .addField(FieldSpec.builder(int.class, "MAXBUTTONS", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
    				  .initializer("32")
    				  .build())
    		  .addField(logopanel, "logoPane", Modifier.PRIVATE)
    		  .addField(informationpanel, "attributePane", Modifier.PRIVATE)
    		  .addField(employeesoverviewscreen, "employeeFrame", Modifier.PRIVATE)
    		  .addField(artifactsoverviewscreen, "artifactFrame", Modifier.PRIVATE)
    		  .addField(projectoverviewscreen, "projectFrame", Modifier.PRIVATE)
    		  .addField(panelsscreen, "panelsFrame", Modifier.PRIVATE)
    		  .addField(clockpanel, "clockPane", Modifier.PRIVATE)
    		  .addField(trackpanel, "trackPane", Modifier.PRIVATE)
    		  .addField(mellopanel, "melloPane", Modifier.PRIVATE)
    		  .addField(gridpane, "gridPane", Modifier.PRIVATE)
    		  .addField(boolean.class, "guiChanged", Modifier.PRIVATE)
    		  .addField(buttonArray, "artifactButton", Modifier.PRIVATE)
    		  .addField(buttonArray, "customerButton", Modifier.PRIVATE)
    		  .addField(buttonArray, "employeeButton", Modifier.PRIVATE)
    		  .addField(buttonArray, "projectButton", Modifier.PRIVATE)
    		  .addField(buttonArray, "toolButton", Modifier.PRIVATE)
    		  .addField(employee, "rightClickedEmployee", Modifier.PRIVATE)
    		  .addField(state, "state", Modifier.PRIVATE)
    		  .addField(logic, "logic", Modifier.PRIVATE)
    		  .addField(simsegui, "gui", Modifier.PRIVATE)
    		  .addField(explanatorytool, "expTool", Modifier.PRIVATE)
    		  .addField(hashObjImg, "objsToImages", Modifier.PRIVATE)
    		  .addField(hashButtonObj, "buttonsToObjs", Modifier.PRIVATE)
    		  .addField(flowpane, "buttonsPane", Modifier.PRIVATE)
    		  .addField(FieldSpec.builder(ssobject, "objInFocus", Modifier.PRIVATE)
    				  .initializer("null")
    				  .build())
    		  .addField(border, "defaultBorder", Modifier.PRIVATE)
    		  .addField(border, "selectedBorder", Modifier.PRIVATE)
    		  .addField(image, "border", Modifier.PRIVATE)
    		  .addField(image, "allIcon", Modifier.PRIVATE)
    		  .addField(FieldSpec.builder(actionHandler, "menuItemEvent", Modifier.PRIVATE)
    				  .initializer("$L", anon)
    				  .build())
    		  .addMethod(constructor)
    		  .addMethod(genButtons)
    		  .addMethod(setConstraints)
    		  .addMethod(handle)
    		  .addMethod(setFocus)
    		  .addMethod(setGuiChanged)
    		  .addMethod(updateEmpty)
    		  .addMethod(updateParam)
    		  .addMethod(updateImages)
    		  .addMethod(getImage)
    		  .addMethod(getClock)
    		  .build();
      
      JavaFile file = JavaFile.builder("", tabPanel)
    		  .build();
      
      String fileString = "package simse.gui;\n\nimport java.util.Enumeration;\nimport java.util.Vector;\n";
      for (String type : this.impTypes) {
    	  fileString = fileString + "import simse.adts.objects." + type + ";\n";
      }
      fileString = fileString + file.toString();
      
      writer.write(fileString);;
      
      writer.close();
    } catch (IOException e) {
      JOptionPane.showMessageDialog(null, ("Error writing file "
          + tabPanelFile.getPath() + ": " + e.toString()), "File IO Error",
          JOptionPane.WARNING_MESSAGE);
    }
  }
  
  private String getSwitchStatement() {
	  String switchS = "";
	  switchS = switchS.concat("switch (index)");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("{");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("case ARTIFACT:");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("buttonList = artifactButton;");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("objs = state.getArtifactStateRepository().getAll();");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("break;");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("case CUSTOMER:");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("buttonList = customerButton;");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("objs = state.getCustomerStateRepository().getAll();");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("break;");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("case EMPLOYEE:");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("buttonList = employeeButton;");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("objs = state.getEmployeeStateRepository().getAll();");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("break;");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("case PROJECT:");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("buttonList = projectButton;");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("objs = state.getProjectStateRepository().getAll();");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("break;");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("case TOOL:");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("buttonList = toolButton;");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("objs = state.getToolStateRepository().getAll();");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("break;");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("default:");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("buttonList = toolButton;");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("objs = new Vector<SSObject>();");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("}");
	  switchS = switchS.concat("\n");
	  return switchS;
  }
  
  private String getImageSwitch() {
	  String switchS = "";
	  switchS = switchS.concat("switch(index)");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("{");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("case ARTIFACT:");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("objs = state.getArtifactStateRepository().getAll();");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("break;");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("case CUSTOMER:");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("objs = state.getCustomerStateRepository().getAll();");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("break;");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("case EMPLOYEE:");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("objs = state.getEmployeeStateRepository().getAll();");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("break;");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("case PROJECT:");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("objs = state.getProjectStateRepository().getAll();");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("break;");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("case TOOL:");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("objs = state.getToolStateRepository().getAll();");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("break;");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("default:");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("objs = new Vector<SSObject>();");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("break;");
	  switchS = switchS.concat("\n");
	  switchS = switchS.concat("}");
	  return switchS;
  }
  
  private String getUrlString() {
	  String urlS = "";
	// go through all object types:
	      Vector<SimSEObjectType> ssObjTypes = objTypes.getAllObjectTypes();
	      for (int j = 0; j < ssObjTypes.size(); j++) {
	        SimSEObjectType tempType = ssObjTypes.elementAt(j);
	        if (!this.impTypes.contains(CodeGeneratorUtils.getUpperCaseLeading(tempType.getName()))) {
	        	this.impTypes.add(CodeGeneratorUtils.getUpperCaseLeading(tempType.getName()));
	        }
	        if (j > 0) { // not on first element
	        	urlS = urlS.concat("else ");
	        }
	        urlS = urlS.concat("if(obj instanceof "
	            + CodeGeneratorUtils.getUpperCaseLeading(tempType.getName()) + ")");
	        urlS = urlS.concat("\n");
	        urlS = urlS.concat("{");
	        urlS = urlS.concat("\n");
	        urlS = urlS.concat(CodeGeneratorUtils.getUpperCaseLeading(tempType.getName()) 
	        		+ " p = (" + 
	        		CodeGeneratorUtils.getUpperCaseLeading(tempType.getName()) + 
	        		")obj;");
	        urlS = urlS.concat("\n");

	        /*
	         * go through all of the created objects (and objects created by create
	         * objects rules) with matching type and metatype:
	         */ 
	        Enumeration<SimSEObject> createdObjects = objsToImages.keys();
	        boolean putElse = false;
	        for (int k = 0; k < objsToImages.size(); k++) {
	          SimSEObject obj = createdObjects.nextElement();
	          if (obj.getName().equals(tempType.getName())) { // same type
	            boolean allAttValuesInit = true; // whether or not all this object's
	                                             // attribute values are initialized
	            Vector<InstantiatedAttribute> atts = obj.getAllAttributes();
	            if (atts.size() < obj.getSimSEObjectType().getAllAttributes()
	                .size()) { // not all atts instantiated
	              allAttValuesInit = false;
	            } else {
	              for (int m = 0; m < atts.size(); m++) {
	                InstantiatedAttribute att = atts.elementAt(m);
	                if (att.isInstantiated() == false) { // not instantiated
	                  allAttValuesInit = false;
	                  break;
	                }
	              }
	            }
	            if (allAttValuesInit) {
	              if (putElse) {
	            	  urlS = urlS.concat("else ");
	              } else {
	                putElse = true;
	              }
	              urlS = urlS.concat("if(p.get"
	                  + CodeGeneratorUtils.getUpperCaseLeading(
	                  		obj.getKey().getAttribute().getName()) + "()");
	              if (obj.getKey().getAttribute().getType() == 
	              	AttributeTypes.STRING) {
	            	  urlS = urlS.concat(".equals(\"" + obj.getKey().getValue().toString()
	                    + "\"))");
	              } else { // integer, double, or boolean att
	            	  urlS = urlS.concat(" == " + obj.getKey().getValue().toString() + ")");
	              }
	              urlS = urlS.concat("\n");
	              urlS = urlS.concat("{");
	              urlS = urlS.concat("\n");
	              String imgFilename = (String) objsToImages.get(obj);
	              if (((imgFilename) != null)
	                  && ((imgFilename).length() > 0) 
	                  && ((new File(iconDir, imgFilename)).exists())) {
	                String imagePath = (iconsDirectory + imgFilename);
	                urlS = urlS.concat("url = \"src" + imagePath + "\";");
	                urlS = urlS.concat("\n");
	              }
	              urlS = urlS.concat("}");
	              urlS = urlS.concat("\n");
	            }
	          }
	        }
	        urlS = urlS.concat("}");
	        urlS = urlS.concat("\n");
	      }
	      return urlS;
  }
}