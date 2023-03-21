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

import simse.modelbuilder.actionbuilder.ActionType;
import simse.modelbuilder.actionbuilder.DefinedActionTypes;
import simse.modelbuilder.objectbuilder.SimSEObjectTypeTypes;

public class RulesInfoScreenGenerator {
	
	private File directory; // directory to save generated code into
	private DefinedActionTypes actionTypes;

	  public RulesInfoScreenGenerator(DefinedActionTypes definedActions, File directory) {
	    this.directory = directory;
	    this.actionTypes = definedActions;
	  }

	  public void generate() {
	    File worldFile = new File(directory, ("simse\\gui\\RulesInfoScreen.java"));
	    if (worldFile.exists()) {
	      worldFile.delete(); // delete old version of file
	    }
	    try {
	      FileWriter writer = new FileWriter(worldFile);
	      
	      ClassName vector = ClassName.get("java.util", "Vector");
	      ClassName fxcollections = ClassName.get("javafx.collections", "FXCollections");
	      ClassName observablelist = ClassName.get("javafx.collections", "ObservableList");
	      ClassName actionevent = ClassName.get("javafx.event", "ActionEvent");
	      ClassName eventhandler = ClassName.get("javafx.event", "EventHandler");
	      ClassName insets = ClassName.get("javafx.geometry", "Insets");
	      ClassName scene = ClassName.get("javafx.scene", "Scene");
	      ClassName checkbox = ClassName.get("javafx.scene.control", "CheckBox");
	      ClassName combobox = ClassName.get("javafx.scene.control", "ComboBox");
	      ClassName label = ClassName.get("javafx.scene.control", "Label");
	      ClassName listview = ClassName.get("javafx.scene.control", "ListView");
	      ClassName scrollpane = ClassName.get("javafx.scene.control", "ScrollPane");
	      ClassName selectionmode = ClassName.get("javafx.scene.control", "SelectionMode");
	      ClassName textarea = ClassName.get("javafx.scene.control", "TextArea");
	      ClassName scrollbarpolicy = ClassName.get("javafx.scene.control.ScrollPane", "ScrollBarPolicy");
	      ClassName mouseevent = ClassName.get("javafx.scene.input", "MouseEvent");
	      ClassName borderpane = ClassName.get("javafx.scene.layout", "BorderPane");
	      ClassName hbox = ClassName.get("javafx.scene.layout", "HBox");
	      ClassName pane = ClassName.get("javafx.scene.layout", "Pane");
	      ClassName tilepane = ClassName.get("javafx.scene.layout", "TilePane");
	      ClassName vbox = ClassName.get("javafx.scene.layout", "VBox");
	      ClassName stage = ClassName.get("javafx.stage", "Stage");
	      ClassName action = ClassName.get("simse.adts.actions", "Action");
	      ClassName state = ClassName.get("simse.state", "State");
	      ClassName rulecategories = ClassName.get("simse.util", "RuleCategories");
	      ClassName ruletype = ClassName.get("simse.util", "RuleType");
	      ClassName string = ClassName.get(String.class);
	      TypeName observableStringList = ParameterizedTypeName.get(observablelist, string);
	      TypeName actionHandler = ParameterizedTypeName.get(eventhandler, actionevent);
	      TypeName mouseHandler = ParameterizedTypeName.get(eventhandler, mouseevent);
	      TypeName comboString = ParameterizedTypeName.get(combobox, string);
	      TypeName listString = ParameterizedTypeName.get(listview, string);
	      
	      MethodSpec constructor = MethodSpec.constructorBuilder()
	    		  .addModifiers(Modifier.PUBLIC)
	    		  .addParameter(state, "s")
	    		  .addParameter(ruletype, "ruleType")
	    		  .addStatement("this.$N = s", "state")
	    		  .addStatement("this.setTitle(\"Rules Screen\")")
	    		  .addStatement("$N = new $T()", "mainPane", vbox)
	    		  .addStatement("ObservableList<String> actions = setActionsByType(ruleType)")
	    		  .addStatement("// Create viewRuleTitlePane and label:")
	    		  .addStatement("$T ruleSelectorPane = new $T()", vbox, vbox)
	    		  .addStatement("$T viewRulesTitle$T = new Pane()", pane, pane)
	    		  .addStatement("viewRulesTitlePane.getChildren().add(new $T(\"View Rules:\"))", label)
	    		  .addStatement("ruleSelectorPane.getChildren().add(viewRulesTitlePane)")
	    		  .addStatement("// Create actionsComboBoxPane:")
	    		  .addStatement("$T actionComboBox$T = new Pane()", pane, pane)
	    		  .addStatement("actionComboBoxPane.getChildren().add(new $T(\"Actions:\"))", label)
	    		  .addStatement("$N = new ComboBox<String>(actions)", "actionComboBox")
	    		  .addStatement("$N.setOnAction(event)", "actionComboBox")
	    		  .addStatement("$NPane.getChildren().add(actionComboBox)", "actionComboBox")
	    		  .addStatement("ruleSelectorPane.getChildren().add(actionComboBoxPane)")
	    		  .addStatement("$T advancedRulesPane = new $T()", tilepane, tilepane)
	    		  .addStatement("$N = new $T(\"View Advanced Rules\")", "advRulesCheck", checkbox)
	    		  .addStatement("advancedRulesPane.getChildren().add($N)", "advRulesCheck")
	    		  .addStatement("$N.setIndeterminate(true)", "advRulesCheck")
	    		  .addStatement("$N.setOnAction(event)", "advRulesCheck")
	    		  .addStatement("$T selections = new $T()", hbox, hbox)
	    		  .addStatement("selections.setPadding(new $T(10))", insets)
	    		  .addStatement("selections.getChildren().add(ruleSelectorPane)")
	    		  .addStatement("selections.getChildren().add(advancedRulesPane)")
	    		  .addStatement("$N.getChildren().add(selections)", "mainPane")
	    		  .addStatement("// Create rulesMainPane:")
	    		  .addStatement("$T rulesMainPane = new $T()", tilepane, tilepane)
	    		  .addStatement("// Create ruleListsPane:")
	    		  .addStatement("$T ruleListsPane = new $T()", vbox, vbox)
	    		  .addStatement("// rule lists:")
	    		  .addStatement("$T trigRuleTitle$T = new Pane()", pane, pane)
	    		  .addStatement("trigRuleTitlePane.getChildren().add(new $T(\"Trigger Rules:\"))", label)
	    		  .addStatement("ruleListsPane.getChildren().add(trigRuleTitlePane)")
	    		  .addStatement("$N = new ListView<String>()", "triggerRuleList")
	    		  .addStatement("$N.setFixedCellSize(24)", "triggerRuleList")
	    		  .addStatement("$N.get$Tl().setSelectionMode(SelectionMode.SINGLE)", "triggerRuleList", selectionmode)
	    		  .addStatement("$N.addEventHandler($T.MOUSE_CLICKED, this)", "triggerRuleList", mouseevent)
	    		  .addStatement("$N.setMinWidth(272)", "triggerRuleList")
	    		  .addStatement("$T triggerRuleListPane = new $T(triggerRuleList)", scrollpane, scrollpane)
	    		  .addStatement("triggerRuleListPane.setMaxHeight(80)")
	    		  .addStatement("ruleListsPane.getChildren().add(triggerRuleListPane)")
	    		  .addStatement("$T destRuleTitle$T = new Pane()", pane, pane)
	    		  .addStatement("destRuleTitlePane.getChildren().add(new $T(\"Destroyer Rules:\"))", label)
	    		  .addStatement("ruleListsPane.getChildren().add(destRuleTitlePane)")
	    		  .addStatement("$N = new ListView<String>()", "destroyerRuleList")
	    		  .addStatement("$N.setFixedCellSize(24)", "destroyerRuleList")
	    		  .addStatement("$N.get$Tl().setSelectionMode(SelectionMode.SINGLE)", "destroyerRuleList", selectionmode)
	    		  .addStatement("$N.addEventHandler($T.MOUSE_CLICKED, this)", "destroyerRuleList", mouseevent)
	    		  .addStatement("$N.setMinWidth(272)", "destroyerRuleList")
	    		  .addStatement("$T destroyerRuleListPane = new $T(destroyerRuleList)", scrollpane, scrollpane)
	    		  .addStatement("destroyerRuleListPane.setMaxHeight(80)")
	    		  .addStatement("ruleListsPane.getChildren().add(destroyerRuleListPane)")
	    		  .addStatement("$T intRuleTitle$T = new Pane()", pane, pane)
	    		  .addStatement("intRuleTitlePane.getChildren().add(new $T(\"Intermediate Rules:\"))", label)
	    		  .addStatement("ruleListsPane.getChildren().add(intRuleTitlePane)")
	    		  .addStatement("$N = new ListView<String>()", "intermediateRuleList")
	    		  .addStatement("$N.setFixedCellSize(24)", "intermediateRuleList")
	    		  .addStatement("$N.get$Tl().setSelectionMode(SelectionMode.SINGLE)", "intermediateRuleList", selectionmode)
	    		  .addStatement("$N.addEventHandler($T.MOUSE_CLICKED, this)", "intermediateRuleList", mouseevent)
	    		  .addStatement("$N.setMinWidth(272)", "intermediateRuleList")
	    		  .addStatement("$T intermediateRuleListPane = new $T(intermediateRuleList)", scrollpane, scrollpane)
	    		  .addStatement("intermediateRuleListPane.setMaxHeight(80)")
	    		  .addStatement("ruleListsPane.getChildren().add(intermediateRuleListPane)")
	    		  .addStatement("ruleListsPane.setPadding(new $T(0,40,0,40))", insets)
	    		  .addStatement("rulesMainPane.getChildren().add(ruleListsPane)")
	    		  .addStatement("// description pane:")
	    		  .addStatement("$T descriptionPane = new $T()", vbox, vbox)
	    		  .addStatement("$T descriptionTitle$T = new Pane()", pane, pane)
	    		  .addStatement("descriptionTitlePane.getChildren().add(new $T(\"Description:\"))", label)
	    		  .addStatement("descriptionPane.getChildren().add(descriptionTitlePane)")
	    		  .addStatement("// description text area:")
	    		  .addStatement("$N = new $T()", "descriptionArea", textarea)
	    		  .addStatement("$N.setWrapText(true)", "descriptionArea")
	    		  .addStatement("$N.setPrefRowCount(16)", "descriptionArea")
	    		  .addStatement("$N.setPrefColumnCount(30)", "descriptionArea")
	    		  .addStatement("$N.setEditable(false)", "descriptionArea")
	    		  .addStatement("$T descriptionScrollPane = new ScrollPane($N)", scrollpane, "descriptionArea")
	    		  .addStatement("descriptionScrollPane.setHbarPolicy($T.AS_NEEDED)", scrollbarpolicy)
	    		  .addStatement("descriptionScrollPane.setVbarPolicy($T.NEVER)", scrollbarpolicy)
	    		  .addStatement("descriptionPane.getChildren().add(descriptionScrollPane)")
	    		  .addStatement("rulesMainPane.getChildren().add(descriptionPane)")
	    		  .addStatement("// Add panes to main pane:")
	    		  .addStatement("$N.getChildren().add(rulesMainPane)", "mainPane")
	    		  .addStatement("$T scene = new $T(mainPane, 800, 400)", scene, scene)
	    		  .addStatement("this.setScene(scene)")
	    		  .build();
	      
	      MethodSpec setActions = MethodSpec.methodBuilder("setActionsByType")
	    		  .addModifiers(Modifier.PRIVATE)
	    		  .returns(observableStringList)
	    		  .addParameter(ruletype, "ruleType")
	    		  .addStatement("$T actions = null", observableStringList)
	    		  .addCode(getActionSwitch())
	    		  .addStatement("return actions")
	    		  .build();
	      
	      MethodSpec refreshLists = MethodSpec.methodBuilder("refreshRuleLists")
	    		  .addModifiers(Modifier.PRIVATE)
	    		  .returns(void.class)
	    		  .addParameter(String.class, "actionName")
	    		  .addStatement("$N.getItems().setAll(new Vector<String>())", "triggerRuleList")
	    		  .addStatement("$N.getItems().setAll(new Vector<String>())", "destroyerRuleList")
	    		  .addStatement("$N.getItems().setAll(new Vector<String>())", "intermediateRuleList")
	    		  .addStatement("$N.getItems().setAll($T.getIntRulesForAction(actionName))", "intermediateRuleList", rulecategories)
	    		  .beginControlFlow("if (advRulesOn)")
	    		  .addStatement("$N.getItems().setAll($T.getAllTrigRulesForAction(actionName))", "triggerRuleList", rulecategories)
	    		  .addStatement("$N.getItems().setAll($T.getAllDestRulesForAction(actionName))", "destroyerRuleList", rulecategories)
	    		  .nextControlFlow("else")
	    		  .addStatement("$N.getItems().setAll($T.getTrigRulesForAction(actionName))", "triggerRuleList", rulecategories)
	    		  .addStatement("$N.getItems().setAll($T.getDestRulesForAction(actionName))", "destroyerRuleList", rulecategories)
	    		  .endControlFlow()
	    		  .build();
	      
	      MethodSpec refreshDescription = MethodSpec.methodBuilder("refreshDescriptionArea")
	    		  .addModifiers(Modifier.PRIVATE)
	    		  .returns(void.class)
	    		  .addParameter(String.class, "ruleName")
	    		  .beginControlFlow("if (ruleName != null)")
	    		  .addStatement("$T text = $T.getRuleMapping(ruleName)", String.class, rulecategories)
	    		  .beginControlFlow("if (text == \"\" && advRulesOn)")
	    		  .addStatement("text = $T.getBackendRuleMappings($N, ruleName)", rulecategories, "lastSelectedAction")
	    		  .endControlFlow()
	    		  .addStatement("$N.setText(text)", "descriptionArea")
	    		  .addStatement("$N.positionCaret(0)", "descriptionArea")
	    		  .endControlFlow()
	    		  .build();
	      
	      MethodSpec handle = MethodSpec.methodBuilder("handle")
	    		  .addAnnotation(Override.class)
	    		  .addModifiers(Modifier.PUBLIC)
	    		  .returns(void.class)
	    		  .addParameter(mouseevent, "event")
	    		  .addStatement("Object source = event.getSource()")
	    		  .beginControlFlow("if ((source == triggerRuleList && !triggerRuleList.getSelectionModel().isEmpty()))")
	    		  .addStatement("$N.getSelectionModel().clearSelection()", "destroyerRuleList")
	    		  .addStatement("$N.getSelectionModel().clearSelection()", "intermediateRuleList")
	    		  .addStatement("refreshDescriptionArea((String) $N.getSelectionModel().getSelectedItem())", "triggerRuleList")
	    		  .nextControlFlow("else if (source == destroyerRuleList && !destroyerRuleList.getSelectionModel().isEmpty())")
	    		  .addStatement("$N.getSelectionModel().clearSelection()", "triggerRuleList")
	    		  .addStatement("$N.getSelectionModel().clearSelection()", "intermediateRuleList")
	    		  .addStatement("refreshDescriptionArea((String) $N.getSelectionModel().getSelectedItem())", "destroyerRuleList")
	    		  .nextControlFlow("else if (source == intermediateRuleList && !intermediateRuleList.getSelectionModel().isEmpty())")
	    		  .addStatement("$N.getSelectionModel().clearSelection()", "triggerRuleList")
	    		  .addStatement("$N.getSelectionModel().clearSelection()", "destroyerRuleList")
	    		  .addStatement("refreshDescriptionArea((String) $N.getSelectionModel().getSelectedItem())", "intermediateRuleList")
	    		  .endControlFlow()
	    		  .build();
	      
	      TypeSpec anon = TypeSpec.anonymousClassBuilder("")
	    		  .addSuperinterface(actionHandler)
	    		  .addMethod(MethodSpec.methodBuilder("handle")
	    				  .addModifiers(Modifier.PUBLIC)
	    				  .returns(void.class)
	    				  .addParameter(actionevent, "e")
	    				  .beginControlFlow("if (e.getSource() == advRulesCheck)")
	    				  .addStatement("$N = $N.isSelected()", "advRulesOn", "advRulesCheck")
	    				  .addStatement("refreshRuleLists($N)", "lastSelectedAction")
	    				  .addStatement("$N.setText(\"\")", "descriptionArea")
	    				  .nextControlFlow("else if (e.getSource() == actionComboBox)")
	    				  .beginControlFlow("if (actionComboBox.getItems().size() > 0)")
	    				  .addStatement("$N = (String) $N.getSelectionModel().getSelectedItem()", "lastSelectedAction", "actionComboBox")
	    				  .addStatement("refreshRuleLists($N)", "lastSelectedAction")
	    				  .addStatement("$N.setText(\"\")", "descriptionArea")
	    				  .endControlFlow()
	    				  .endControlFlow()
	    				  .build())
	    		  .build();
	      
	      TypeSpec ruleScreen = TypeSpec.classBuilder("RulesInfoScreen")
	    		  .addModifiers(Modifier.PUBLIC)
	    		  .superclass(stage)
	    		  .addSuperinterface(mouseHandler)
	    		  .addField(state, "state")
	    		  .addField(label, "titleLabel")
	    		  .addField(label, "toolsLabel")
	    		  .addField(borderpane, "tablePane")
	    		  .addField(vbox, "mainPane")
	    		  .addField(comboString, "actionComboBox", Modifier.PRIVATE)
	    		  .addField(listString, "triggerRuleList", Modifier.PRIVATE)
	    		  .addField(listString, "destroyerRuleList", Modifier.PRIVATE)
	    		  .addField(listString, "intermediateRuleList", Modifier.PRIVATE)
	    		  .addField(textarea, "descriptionArea", Modifier.PRIVATE)
	    		  .addField(FieldSpec.builder(String.class, "lastSelectedAction", Modifier.PRIVATE)
	    				  .initializer("\"\"")
	    				  .build())
	    		  .addField(checkbox, "advRulesCheck", Modifier.PRIVATE)
	    		  .addField(boolean.class, "advRulesOn", Modifier.PRIVATE)
	    		  .addField(FieldSpec.builder(actionHandler, "event")
	    				  .initializer("$L", anon)
	    				  .build())
	    		  .addMethod(constructor)
	    		  .addMethod(setActions)
	    		  .addMethod(refreshLists)
	    		  .addMethod(refreshDescription)
	    		  .addMethod(handle)
	    		  .build();
	      
	      JavaFile file = JavaFile.builder("", ruleScreen)
	    		  .build();
	      
	      String fileString = "package simse.gui;\n\nimport javafx.collections.FXCollections;\nimport java.util.Vector;\nimport simse.adts.actions.Action;\n";
	      fileString = fileString + file.toString();
	      
	      writer.write(fileString);
	      
	      
	      
	      writer.close();
	    } catch (IOException e) {
	      JOptionPane.showMessageDialog(null, ("Error writing file "
	          + worldFile.getPath() + ": " + e.toString()), "File IO Error",
	          JOptionPane.WARNING_MESSAGE);
	    }
	  }
	  
	  private String getActionSwitch() {
		  String switchS = "switch(ruleType) {\n";
		  switchS = switchS.concat(getCase("ARTIFACT", SimSEObjectTypeTypes.ARTIFACT));
		  switchS = switchS.concat(getCase("PROJECT", SimSEObjectTypeTypes.PROJECT));
		  switchS = switchS.concat(getSpecialCase("PEOPLE", SimSEObjectTypeTypes.CUSTOMER, SimSEObjectTypeTypes.EMPLOYEE));
		  switchS = switchS.concat(getAllTypes());
		  switchS = switchS.concat("}\n");
		  return switchS;
		  
		  
	  }
	  
	  private String getCase(String typeName, int type) {
		  boolean addComma = false;
		  String caseS = "case " + typeName + ":\n";
		  caseS = caseS.concat("actions = FXCollections.observableArrayList(");
		  for (ActionType at : actionTypes.getAllActionTypes()) {
			  if (at.getCategories().contains(type)) {
				  if (addComma) {
					  caseS = caseS.concat(", Action." + at.getName().toUpperCase());
				  } else {
					  caseS = caseS.concat("Action."+at.getName().toUpperCase());
					  addComma = true;
				  }
			  }
		  }
		  caseS = caseS.concat(");\nbreak;\n");
		  return caseS;
	  }
	  
	  private String getSpecialCase(String typeName, int type, int type2) {
		  boolean addComma = false;
		  String caseS = "case " + typeName + ":\n";
		  caseS = caseS.concat("actions = FXCollections.observableArrayList(");
		  for (ActionType at : actionTypes.getAllActionTypes()) {
			  Vector<Integer> cats = at.getCategories();
			  if (cats.contains(type) || cats.contains(type2)) {
				  if (addComma) {
					  caseS = caseS.concat(", Action." + at.getName().toUpperCase());
				  } else {
					  caseS = caseS.concat("Action."+at.getName().toUpperCase());
					  addComma = true;
				  }
			  }
		  }
		  caseS = caseS.concat(");\nbreak;\n");
		  return caseS;
	  }
	  
	  private String getAllTypes() {
		  boolean addComma = false;
		  String caseS = "default:\n";
		  caseS = caseS.concat("actions = FXCollections.observableArrayList(");
		  for (ActionType at : actionTypes.getAllActionTypes()) {
			  if (addComma) {
				  caseS = caseS.concat(", Action." + at.getName().toUpperCase());
			  } else {
				  caseS = caseS.concat("Action."+at.getName().toUpperCase());
				  addComma = true;
			  }
		  }
		  caseS = caseS.concat(");\nbreak;\n");
		  return caseS;
	  }

}
