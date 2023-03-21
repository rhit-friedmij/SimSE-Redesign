/*
 * This class is responsible for generating all of the code for the
 * RuleInfoPanel class in the explanatory tool
 */

package simse.codegenerator.explanatorytoolgenerator;

import simse.codegenerator.CodeGeneratorConstants;
import simse.codegenerator.CodeGeneratorUtils;
import simse.modelbuilder.actionbuilder.ActionType;
import simse.modelbuilder.actionbuilder.DefinedActionTypes;
import simse.modelbuilder.rulebuilder.Rule;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import javax.lang.model.element.Modifier;
import javax.swing.JOptionPane;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

public class RuleInfoPanelGenerator implements CodeGeneratorConstants {
  private File directory; // directory to save generated code into
  private DefinedActionTypes actTypes;

  public RuleInfoPanelGenerator(DefinedActionTypes actTypes, File directory) {
    this.actTypes = actTypes;
    this.directory = directory;
  }

  public void generate() {
    File ruleInfoFile = new File(directory,
        ("simse\\explanatorytool\\RuleInfoPanel.java"));
    if (ruleInfoFile.exists()) {
      ruleInfoFile.delete(); // delete old version of file
    }
      
      ClassName pos = ClassName.get("javafx.geometry", "Pos");
      ClassName listView = ClassName.get("javafx.scene.control", "ListView");
      ClassName string = ClassName.get("java.lang", "String");
      ClassName scrollPane = ClassName.get("javafx.scene.control", "ScrollPane");
      ClassName scrollBarPolicy = ClassName.get("javafx.scene.control.ScrollPane", "ScrollBarPolicy");
      ClassName selectionMode = ClassName.get("javafx.scene.control", "SelectionMode");
      ClassName textArea = ClassName.get("javafx.scene.control", "TextArea");
      ClassName titledPane = ClassName.get("javafx.scene.control", "TitledPane");
      ClassName toolTip = ClassName.get("javafx.scene.control", "Tooltip");
      ClassName mouseEvent = ClassName.get("javafx.scene.input", "MouseEvent");
      ClassName gridPane = ClassName.get("javafx.scene.layout", "GridPane");
      ClassName pane = ClassName.get("javafx.scene.layout", "Pane");
      ClassName vBox = ClassName.get("javafx.scene.layout", "VBox");
      ClassName stage = ClassName.get("javafx.stage", "Stage");
      ClassName actionClass = ClassName.get("simse.adts.actions", "Action");
      ClassName ruleCategories = ClassName.get("simse.util", "RuleCategories");
      ClassName eventHandler = ClassName.get("javafx.event", "EventHandler");
      ArrayTypeName stringArray = ArrayTypeName.of(String.class);
      
  	MethodSpec constructor = MethodSpec.constructorBuilder()
			.addModifiers(Modifier.PUBLIC)
			.addParameter(stage, "owner")
			.addParameter(actionClass, "action")
			.addStatement("this.action = action")
			.addStatement("$T mainPane = new $T()", gridPane, gridPane)
			.addStatement("mainPane.setPrefSize(900,600)")
			.addStatement("mainPane.setHgap(10)")
			.addStatement("$T tRulePane = new $T()", vBox, vBox)
			.addStatement("$T trigRuleTitlePane = new $T(\"Trigger Rules:\", tRulePane)", titledPane, titledPane)
			.addStatement("triggerRuleList = new $T<$T>()", listView, string)
			.addStatement("triggerRuleList.setFixedCellSize(24)")
			.addStatement("triggerRuleList.getSelectionModel().setSelectionMode($T.SINGLE)", selectionMode)
			.addStatement("triggerRuleList.addEventHandler($T.MOUSE_CLICKED, this)", mouseEvent)
			.addStatement("$T triggerRuleListPane = new $T(triggerRuleList)", scrollPane, scrollPane)
			.addStatement("triggerRuleListPane.setMaxHeight(100)")
			.addStatement("triggerRuleListPane.setVbarPolicy($T.AS_NEEDED)", scrollBarPolicy)
			.addStatement("triggerRuleListPane.setHbarPolicy($T.NEVER)", scrollBarPolicy)
			.addStatement("$T trigToolTip = \"Rules that execute at the beginning of the action\"", string)
			.addStatement("trigRuleTitlePane.setTooltip(new $T(trigToolTip))", toolTip)
			.addStatement("triggerRuleList.setTooltip(new $T(trigToolTip))", toolTip)
			.addStatement("tRulePane.getChildren().add(triggerRuleListPane)")
			.addStatement("$T dRulePane = new $T()", vBox, vBox)
			.addStatement("$T destRuleTitlePane = new $T(\"Trigger Rules:\", dRulePane)", titledPane, titledPane)
			.addStatement("destroyerRuleList = new $T<$T>()", listView, string)
			.addStatement("destroyerRuleList.setFixedCellSize(24)")
			.addStatement("destroyerRuleList.getSelectionModel().setSelectionMode($T.SINGLE)", selectionMode)
			.addStatement("destroyerRuleList.addEventHandler($T.MOUSE_CLICKED, this)", mouseEvent)
			.addStatement("$T destroyerRuleListPane = new $T(destroyerRuleList)", scrollPane, scrollPane)
			.addStatement("destroyerRuleListPane.setMaxHeight(100)")
			.addStatement("destroyerRuleListPane.setVbarPolicy($T.AS_NEEDED)", scrollBarPolicy)
			.addStatement("destroyerRuleListPane.setHbarPolicy($T.NEVER)", scrollBarPolicy)
			.addStatement("$T destToolTip = \"Rules that execute at the beginning of the action\"", string)
			.addStatement("destRuleTitlePane.setTooltip(new $T(destToolTip))", toolTip)
			.addStatement("destroyerRuleList.setTooltip(new $T(destToolTip))", toolTip)
			.addStatement("dRulePane.getChildren().add(destroyerRuleListPane)")
			.addStatement("$T iRulePane = new $T()", vBox, vBox)
			.addStatement("$T intRuleTitlePane = new $T(\"Trigger Rules:\", iRulePane)", titledPane, titledPane)
			.addStatement("intermediateRuleList = new $T<$T>()", listView, string)
			.addStatement("intermediateRuleList.setFixedCellSize(24)")
			.addStatement("intermediateRuleList.getSelectionModel().setSelectionMode($T.SINGLE)", selectionMode)
			.addStatement("intermediateRuleList.addEventHandler($T.MOUSE_CLICKED, this)", mouseEvent)
			.addStatement("$T intermediateRuleListPane = new $T(intermediateRuleList)", scrollPane, scrollPane)
			.addStatement("intermediateRuleListPane.setMaxHeight(100)")
			.addStatement("intermediateRuleListPane.setVbarPolicy($T.AS_NEEDED)", scrollBarPolicy)
			.addStatement("intermediateRuleListPane.setHbarPolicy($T.NEVER);", scrollBarPolicy)
			.addStatement("$T intToolTip = \"Rules that execute at the beginning of the action\"", string)
			.addStatement("intRuleTitlePane.setTooltip(new $T(intToolTip))", toolTip)
			.addStatement("intermediateRuleList.setTooltip(new $T(intToolTip))", toolTip)
			.addStatement("iRulePane.getChildren().add(intermediateRuleListPane)")
			.addStatement("$T rulePane = new $T()", vBox, vBox)
			.addStatement("rulePane.getChildren().add(trigRuleTitlePane)")
			.addStatement("rulePane.getChildren().add(destRuleTitlePane)")
			.addStatement("rulePane.getChildren().add(intRuleTitlePane)")
			.addStatement("initializeRuleLists()")
			.addStatement("// description pane:")
			.addStatement("$T descriptionPane = new $T()", vBox, vBox)
			.addStatement("$T descriptionTitlePane = new $T(\"Description:\", descriptionPane)", titledPane, titledPane)
			.addStatement("// description text area:")
			.addStatement("descriptionArea = new $T()", textArea)
			.addStatement("descriptionArea.setPrefColumnCount(29)")
			.addStatement("descriptionArea.setPrefRowCount(25)")
			.addStatement("descriptionArea.setWrapText(true)")
			.addStatement("descriptionArea.setEditable(false)")
			.addStatement("$T descriptionScrollPane = new $T(descriptionArea)", scrollPane, scrollPane)
			.addStatement("descriptionScrollPane.setHbarPolicy($T.NEVER)", scrollBarPolicy)
			.addStatement("descriptionScrollPane.setVbarPolicy($T.AS_NEEDED)", scrollBarPolicy)
			.addStatement("descriptionPane.getChildren().add(descriptionScrollPane)")
			.addStatement("mainPane.add(rulePane, 0, 0)")
			.addStatement("mainPane.add(descriptionTitlePane, 1, 0, 1, 3)")
			.addStatement("mainPane.setAlignment($T.CENTER)", pos)
			.addStatement("this.getChildren().add(mainPane)")
			.build();
  	
  	MethodSpec initializeRuleLists = MethodSpec.methodBuilder("initializeRuleLists")
  			.addModifiers(Modifier.PRIVATE)
  			.addStatement("$T actionName = action.getActionName()", String.class)
  			.addStatement("$T intList = $T.getIntRulesForAction(actionName)", stringArray, ruleCategories)
  			.addStatement("$T trigList = $T.getTrigRulesForAction(actionName)", stringArray, ruleCategories)
  			.addStatement("$T destList = $T.getDestRulesForAction(actionName)", stringArray, ruleCategories)
  			.addStatement("intermediateRuleList.getItems().setAll(intList)")
  			.addStatement("triggerRuleList.getItems().setAll(trigList)")
  			.addStatement("destroyerRuleList.getItems().setAll(destList)")
  			.build();
  			


      // "valueChanged" method:
      
      MethodSpec handle = MethodSpec.methodBuilder("handle")
    		  .addModifiers(Modifier.PUBLIC)
    		  .addParameter(mouseEvent, "event")
    		  .beginControlFlow("if ((event.getSource() == triggerRuleList && !triggerRuleList.getSelectionModel().isEmpty()))")
    		  .addStatement("destroyerRuleList.getSelectionModel().clearSelection()")
    		  .addStatement("intermediateRuleList.getSelectionModel().clearSelection()")
    		  .addStatement("refreshDescriptionArea()")
    		  .nextControlFlow("else if (!destroyerRuleList.getSelectionModel().isEmpty())")
    		  .addStatement("name = ($T) destroyerRuleList.getSelectionModel().getSelectedItem()", string)
    		  .nextControlFlow("else if (!intermediateRuleList.getSelectionModel().isEmpty())")
    		  .addStatement("name = ($T) intermediateRuleList.getSelectionModel().getSelectedItem()", string)
    		  .endControlFlow()
    		  .build();

      MethodSpec refreshDescriptionArea = MethodSpec.methodBuilder("refreshDescriptionArea")
    		  .addModifiers(Modifier.PRIVATE)
    		  .addStatement("$T name = null", string)
    		  .beginControlFlow("if (!triggerRuleList.getSelectionModel().isEmpty())")
    		  .addStatement("name = ($T) triggerRuleList.getSelectionModel().getSelectedItem()", string)
    		  .nextControlFlow("else if (!destroyerRuleList.getSelectionModel().isEmpty())")
    		  .addStatement("name = ($T) destroyerRuleList.getSelectionModel().getSelectedItem()", string)
    		  .nextControlFlow("else if (!intermediateRuleList.getSelectionModel().isEmpty())")
    		  .addStatement("name = ($T) intermediateRuleList.getSelectionModel().getSelectedItem()", string)
    		  .endControlFlow()
    		  .beginControlFlow("if (name != null)")
    		  .addStatement("$T text = $T.getRuleMapping(name)", string, ruleCategories)
    		  .addStatement("descriptionArea.setText(text)")
    		  .addStatement("descriptionArea.positionCaret(0)")
    		  .endControlFlow()
    		  .build();
      
	  TypeSpec ruleInfoPanel = TypeSpec.classBuilder("RuleInfoPanel")
			  .addModifiers(Modifier.PUBLIC)
			  .superclass(pane)
			  .addSuperinterface(ParameterizedTypeName.get(eventHandler, mouseEvent))
			  .addField(actionClass, "action", Modifier.PRIVATE)
			  .addField(ParameterizedTypeName.get(listView, string), "triggerRuleList", Modifier.PRIVATE)
			  .addField(ParameterizedTypeName.get(listView, string), "destroyerRuleList", Modifier.PRIVATE)
			  .addField(ParameterizedTypeName.get(listView, string), "intermediateRuleList", Modifier.PRIVATE)
			  .addField(textArea, "descriptionArea", Modifier.PRIVATE)
			  .addMethod(constructor)
			  .addMethod(initializeRuleLists)
			  .addMethod(refreshDescriptionArea)
			  .addMethod(handle)
			  .build();
      
 
      
      JavaFile javaFile = JavaFile.builder("", ruleInfoPanel)
  		    .build();

    try {
    	FileWriter writer = new FileWriter(ruleInfoFile);
    	
  	  	String toAppend = "/* File generated by: simse.codegenerator.explanatorytool.RuleInfoPanel */\n"
  	  	  		+ "package simse.explanatorytool;\n"
  	  	  		+ "\n"
  	  	  		+ "import simse.adts.actions.*;\n";
    	
		writer.write(toAppend + javaFile.toString());
		
		writer.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
     
  }
}