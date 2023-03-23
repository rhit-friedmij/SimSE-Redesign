/*
 * This class is responsible for generating all of the code for the
 * ActionInfoPanel class in the explanatory tool
 */

package simse.codegenerator.explanatorytoolgenerator;

import simse.codegenerator.CodeGeneratorConstants;
import simse.codegenerator.CodeGeneratorUtils;
import simse.modelbuilder.actionbuilder.ActionType;
import simse.modelbuilder.actionbuilder.ActionTypeDestroyer;
import simse.modelbuilder.actionbuilder.ActionTypeParticipant;
import simse.modelbuilder.actionbuilder.ActionTypeTrigger;
import simse.modelbuilder.actionbuilder.DefinedActionTypes;
import simse.modelbuilder.objectbuilder.Attribute;
import simse.modelbuilder.objectbuilder.AttributeTypes;
import simse.modelbuilder.objectbuilder.SimSEObjectType;
import simse.modelbuilder.objectbuilder.SimSEObjectTypeTypes;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import javax.lang.model.element.Modifier;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

public class ActionInfoPanelGenerator implements CodeGeneratorConstants {
  private File directory; // directory to save generated code into
  private DefinedActionTypes actTypes;

  public ActionInfoPanelGenerator(DefinedActionTypes actTypes, File directory) {
    this.actTypes = actTypes;
    this.directory = directory;
  }

  public void generate() {
    File actInfoFile = new File(directory,
        ("simse\\explanatorytool\\ActionInfoPanel.java"));
    if (actInfoFile.exists()) {
      actInfoFile.delete(); // delete old version of file
    }
      
      ClassName pane = ClassName.get("javafx.scene.layout", "Pane");
      ClassName eventHandler = ClassName.get("javafx.event", "EventHandler");
      ClassName mouseEvent = ClassName.get("javafx.scene.input", "MouseEvent");
      ClassName action = ClassName.get("simse.adts.actions", "Action");
      ClassName vBox = ClassName.get("javafx.scene.layout", "VBox");
      ClassName titledPane = ClassName.get("javafx.scene.control", "TitledPane");
      ClassName textArea = ClassName.get("javafx.scene.control", "TextArea");
      ClassName scrollPane = ClassName.get("javafx.scene.control", "ScrollPane");
      ClassName pos = ClassName.get("javafx.geometry", "Pos");
      ClassName tableView = ClassName.get("javafx.scene.control", "TableView");
      ClassName tableColumn = ClassName.get("javafx.scene.control", "TableColumn");
      ClassName participant = ClassName.get("simse.explanatorytool", "Participant");
      ClassName scrollBarPolicy = ClassName.get("javafx.scene.control.ScrollPane", "ScrollBarPolicy");
      ClassName gridPane = ClassName.get("javafx.scene.layout", "GridPane");
      ClassName listView = ClassName.get("javafx.scene.control", "ListView");
      ClassName selectionMode = ClassName.get("javafx.scene.control", "SelectionMode");
      ClassName string = ClassName.get("java.lang", "String");
      ClassName ruleCategories = ClassName.get("simse.util", "RuleCategories");
      ClassName propertyValueFactory = ClassName.get("javafx.scene.control.cell", "PropertyValueFactory");
      ClassName observableList = ClassName.get("javafx.collections", "ObservableList");
      ClassName fxCollections = ClassName.get("javafx.collections", "FXCollections");
      
      String initalizeActionDescriptionActions = "";
      
      // go through all actions:
      Vector<ActionType> actions = actTypes.getAllActionTypes();
      boolean writeElse = false;
      for (ActionType act : actions) {
        if (act.isVisibleInExplanatoryTool()) {
          String uCaseName = 
          	CodeGeneratorUtils.getUpperCaseLeading(act.getName());
          if (writeElse) {
            initalizeActionDescriptionActions += "else ";
          } else {
            writeElse = true;
          }
          initalizeActionDescriptionActions += "if (action instanceof " + uCaseName + "Action) {\n";
          initalizeActionDescriptionActions += "text = \"" + 
                  act.getAnnotation().replaceAll("\n", "\\\\n").
                  replaceAll("\"", "\\\\\"") + "\";\n";
          initalizeActionDescriptionActions += "}";
        }
      }

      
      MethodSpec initializeActionDescription = MethodSpec.methodBuilder("initializeActionDescription")
    		  .addModifiers(Modifier.PRIVATE)
    		  .addStatement("String text = \"\"")
    		  .addCode(initalizeActionDescriptionActions)
    		  .addStatement("actionDescriptionArea.setText(text)")
    		  .addStatement("actionDescriptionArea.positionCaret(0)")
    		  .build();
      
      MethodSpec initializeDestroyerList = MethodSpec.methodBuilder("initializeDestroyerList")
    		  .addModifiers(Modifier.PRIVATE)
    		  .addStatement("String actionName = action.getActionName()")
    		  .addStatement("destroyerList.getItems().setAll($T.getBackendDestRulesForAction(actionName))", ruleCategories)
    		  .build();
      
      
      MethodSpec initializeTriggerList = MethodSpec.methodBuilder("initializeTriggerList")
    		  .addModifiers(Modifier.PRIVATE)
    		  .addStatement("String actionName = action.getActionName()")
    		  .addStatement("triggerList.getItems().setAll($T.getBackendTrigRulesForAction(actionName))", ruleCategories)
    		  .build();
      
      String participantsTableActions = "";
      
      // go through all actions:
      writeElse = false;
      for (ActionType act : actions) {
        String uCaseName = 
        	CodeGeneratorUtils.getUpperCaseLeading(act.getName());
        if (act.isVisibleInExplanatoryTool()) {
          if (writeElse) {
            participantsTableActions += "else ";
          } else {
            writeElse = true;
          }
          participantsTableActions += "if (action instanceof " + uCaseName + "Action) {\n";
		  participantsTableActions += uCaseName + "Action " + uCaseName.toLowerCase() +
					"Action = (" + uCaseName + "Action)action;\n";

          // go through all participants:
          Vector<ActionTypeParticipant> participants = act.getAllParticipants();
          for (ActionTypeParticipant part : participants) {
            String metaType = SimSEObjectTypeTypes.getText(part
                .getSimSEObjectTypeType());
            String lCasePartName = part.getName().toLowerCase();
  		  participantsTableActions += " // " + part.getName() + " participant:\n";
            participantsTableActions += "Vector<" + metaType + "> " + lCasePartName + "s = " +
            		uCaseName.toLowerCase() + "Action.getAll" + part.getName() + 
            		"s();\n";
            participantsTableActions += "Vector<" + metaType + "> active" + part.getName() + 
            		"s = " + uCaseName.toLowerCase() + "Action.getAllActive" + 
            		part.getName() + "s();\n";
            participantsTableActions += "for (int i = 0; i < " + lCasePartName
                    + "s.size(); i++) {\n";
            participantsTableActions += metaType + " " + lCasePartName + " = " + 
            		lCasePartName + "s.get(i);\n";
            participantsTableActions += "String title = \"" + part.getName() + "\";\n";
            participantsTableActions += "String title1 = \"\";\n";

            // go through all allowable SimSEObjectTypes:
            Vector<SimSEObjectType> types = part.getAllSimSEObjectTypes();
            for (int k = 0; k < types.size(); k++) {
              SimSEObjectType type = types.get(k);
              String uCaseTypeName = 
              	CodeGeneratorUtils.getUpperCaseLeading(type.getName());
              String uCasePartName =
              	CodeGeneratorUtils.getUpperCaseLeading(part.getName());
              if (k < 0) {
              }
              participantsTableActions += "if (" + lCasePartName + " instanceof "
                      + uCaseTypeName + ") {\n";
              Attribute keyAtt = type.getKey();
              String uCaseAttName = 
              	CodeGeneratorUtils.getUpperCaseLeading(keyAtt.getName());
              String singlePartName = uCaseTypeName.toLowerCase() + uCasePartName;
              participantsTableActions += uCaseTypeName + " " + singlePartName + " = (" + uCaseTypeName + ")" + lCasePartName +
                  		";\n";
              participantsTableActions += "title1 = \"" + uCaseTypeName + " " + uCasePartName + " \" + " + singlePartName 
            		  + ".get" + type.getKey().getName() + "();";
              
              participantsTableActions +="// find out whether it's active or not:\n";
              participantsTableActions += "boolean active = false;\n";
              participantsTableActions += "for (int j = 0; j < active" + part.getName()
              + "s.size(); j++) {\n";
              participantsTableActions += metaType + " active" + part.getName() + " = active" +
                		part.getName() + "s.get(j);\n";
              participantsTableActions += "if ((active" + part.getName() + " instanceof "
                      + uCaseTypeName + ") && ((" + uCaseTypeName + ")active"
                      + part.getName() + ").get";
              participantsTableActions += uCaseAttName + "()";
              if (keyAtt.getType() == AttributeTypes.STRING) {
                participantsTableActions += ".equals(";
              } else { // non-string
                participantsTableActions += " == ";
              }
              participantsTableActions += "((" + uCaseTypeName + ")" + lCasePartName + ").get"
                      + uCaseAttName + "())";
              if (keyAtt.getType() == AttributeTypes.STRING) {
                participantsTableActions += ")";
              }
              participantsTableActions += " {\n";
              participantsTableActions += "active = true;\n";
              participantsTableActions += "break;\n";
              participantsTableActions += "}\n";
              participantsTableActions += "}\n";
              participantsTableActions += "data.add(new Participant(title, title1, active ? \"Active\" : \"Inactive\"));";
              participantsTableActions += "}\n";
            }
            participantsTableActions += "}\n";
          }
          participantsTableActions += "}\n";
        }
      }
      
      MethodSpec createParticipantsTable = MethodSpec.methodBuilder("createParticipantsTable")
    		  .addModifiers(Modifier.PRIVATE)
    		  .returns(ParameterizedTypeName.get(tableView, participant))
    		  .addStatement("$T<$T> newView = new $T<$T>()", tableView, participant, tableView, participant)
    		  .addStatement("$T<$T, $T> name = new $T<>(\"Participant Name\")", tableColumn, participant, String.class, tableColumn)
    		  .addStatement("name.setCellValueFactory(new $T<>(\"title1\"))", propertyValueFactory)
    		  .addStatement("$T<$T, $T> participant = new $T<>(\"Participant\")", tableColumn, participant, String.class, tableColumn)
    		  .addStatement("participant.setCellValueFactory(new $T<>(\"title2\"))", propertyValueFactory)
    		  .addStatement("$T<$T, $T> status = new $T<>(\"Status\")", tableColumn, participant, String.class, tableColumn)
    		  .addStatement("status.setCellValueFactory(new $T<>(\"status\"))", propertyValueFactory)
    		  .addStatement("$T<$T> data = $T.observableArrayList()", observableList, participant, fxCollections)
    		  .addCode(participantsTableActions)
    		  .addStatement("newView.getColumns().setAll(name, participant, status)")
    		  .addStatement("newView.setItems(data)")
    		  .addStatement("return newView")
    		  .build();
      
      MethodSpec constructor = MethodSpec.constructorBuilder()
    		  .addModifiers(Modifier.PUBLIC)
    		  .addParameter(action, "action")
    		  .addStatement("this.action = action")
    		  .addStatement("$T mainPane = new $T()", vBox, vBox)
    		  .addStatement("$T actionDescriptionPane = new $T()", vBox, vBox)
    		  .addStatement("$T actionDescriptionTitlePane = new $T(\"ActionDescription: \", actionDescriptionPane)", titledPane, titledPane)
    		  .addStatement("actionDescriptionArea = new $T()", textArea)
    		  .addStatement("actionDescriptionArea.setWrapText(true)")
    		  .addStatement("actionDescriptionArea.setPrefRowCount(1)")
    		  .addStatement("actionDescriptionArea.setPrefColumnCount(50)")
    		  .addStatement("actionDescriptionArea.setEditable(false)")
    		  .addStatement("$T actionDescriptionScrollPane = new $T(actionDescriptionArea)", scrollPane, scrollPane)
    		  .addStatement("actionDescriptionScrollPane.setVbarPolicy($T.AS_NEEDED)", scrollBarPolicy)
    		  .addStatement("actionDescriptionScrollPane.setHbarPolicy($T.NEVER)", scrollBarPolicy)
    		  .addStatement("initializeActionDescription()")
    		  .addStatement("actionDescriptionPane.getChildren().add(actionDescriptionScrollPane)")
    		  .addStatement("actionDescriptionPane.setAlignment($T.CENTER)", pos)
    		  .addStatement("$T participantsPane = new $T()", vBox, vBox)
    		  .addStatement("$T participantsTitlePane = new $T(\"Participants:\", participantsPane)", titledPane, titledPane)
    		  .addStatement("table = $N()", createParticipantsTable)
    		  .addStatement("table.setMinWidth(800)")
    		  .addStatement("$T participantsTablePane = new $T(table)", scrollPane, scrollPane)
    		  .addStatement("participantsTablePane.setVbarPolicy($T.AS_NEEDED)", scrollBarPolicy)
    		  .addStatement("participantsTablePane.setHbarPolicy($T.NEVER)", scrollBarPolicy)
    		  .addStatement("participantsTablePane.setPrefSize(850, 150)")
    		  .addStatement("participantsPane.getChildren().add(participantsTablePane)")
    		  .addStatement("participantsPane.setAlignment($T.CENTER)", pos)
    		  .addStatement("// Create triggerDestroyer pane and components")
    		  .addStatement("$T triggerDestroyerPane = new $T()", gridPane, gridPane)
    		  .addStatement("// trigger list")
    		  .addStatement("$T tListPane = new $T()", vBox ,vBox)
    		  .addStatement("$T triggerListTitlePane = new $T(\"Triggers:\", tListPane)", titledPane, titledPane)
    		  .addStatement("triggerList = new $T<$T>()", listView, String.class)
    		  .addStatement("triggerList.setFixedCellSize(24)")
    		  .addStatement("triggerList.addEventHandler($T.MOUSE_CLICKED, this)", mouseEvent)
    		  .addStatement("triggerList.getSelectionModel().setSelectionMode($T.SINGLE)", selectionMode)
    		  .addStatement("$N()", initializeTriggerList)
    		  .addStatement("$T triggerListPane = new $T(triggerList)", scrollPane, scrollPane)
    		  .addStatement("triggerListPane.setMaxHeight(80)")
    		  .addStatement("triggerListPane.setVbarPolicy($T.AS_NEEDED)", scrollBarPolicy)
    		  .addStatement("triggerListPane.setHbarPolicy($T.NEVER)", scrollBarPolicy)
    		  .addStatement("tListPane.getChildren().add(triggerListPane)")
    		  .addStatement("// destroyer list")
    		  .addStatement("$T dListPane = new $T()", vBox, vBox)
    		  .addStatement("$T destroyerListTitlePane = new $T(\"Destroyers: \", dListPane)", titledPane, titledPane)
    		  .addStatement("destroyerList = new $T<$T>()", listView, String.class)
    		  .addStatement("destroyerList.setFixedCellSize(24)")
    		  .addStatement("destroyerList.addEventHandler($T.MOUSE_CLICKED, this)", mouseEvent)
    		  .addStatement("destroyerList.getSelectionModel().setSelectionMode($T.SINGLE)", selectionMode)
    		  .addStatement("$N()", initializeDestroyerList)
    		  .addStatement("$T destroyerListPane = new $T(destroyerList)", scrollPane, scrollPane)
    		  .addStatement("destroyerListPane.setMaxHeight(80)")
    		  .addStatement("destroyerListPane.setVbarPolicy($T.AS_NEEDED)", scrollBarPolicy)
    		  .addStatement("destroyerListPane.setHbarPolicy($T.NEVER)", scrollBarPolicy)
    		  .addStatement("dListPane.getChildren().add(destroyerListPane)")
    		  .addStatement("// description pane")
    		  .addStatement("$T descriptionPane = new $T()", vBox, vBox)
    		  .addStatement("$T descriptionTitlePane = new $T(\"Description: \", descriptionPane)", titledPane, titledPane)
    		  .addStatement("// description text area")
    		  .addStatement("descriptionArea = new TextArea()")
    		  .addStatement("descriptionArea.setWrapText(true)")
    		  .addStatement("descriptionArea.setPrefRowCount(9)")
    		  .addStatement("descriptionArea.setPrefColumnCount(30)")
    		  .addStatement("descriptionArea.setEditable(false)")
    		  .addStatement("$T descriptionScrollPane = new $T()", scrollPane, scrollPane)
    		  .addStatement("descriptionScrollPane.setVbarPolicy($T.AS_NEEDED)", scrollBarPolicy)
    		  .addStatement("descriptionScrollPane.setHbarPolicy($T.NEVER)", scrollBarPolicy)
    		  .addStatement("descriptionScrollPane.setContent(descriptionArea)")
    		  .addStatement("descriptionPane.getChildren().add(descriptionScrollPane)")
    		  .addStatement("triggerDestroyerPane.add(triggerListTitlePane, 0, 0)")
    		  .addStatement("triggerDestroyerPane.add(destroyerListTitlePane, 0, 1)")
    		  .addStatement("triggerDestroyerPane.add(descriptionTitlePane, 1, 0, 1, 2)")
    		  .addStatement("triggerDestroyerPane.setAlignment($T.CENTER)", pos)
    		  .addStatement("triggerDestroyerPane.setHgap(10)")
    		  .addStatement("// Add panes to main pane")
    		  .addStatement("mainPane.getChildren().add(actionDescriptionTitlePane)")
    		  .addStatement("mainPane.getChildren().add(participantsTitlePane)")
    		  .addStatement("mainPane.getChildren().add(triggerDestroyerPane)")
    		  .addStatement("mainPane.setAlignment(Pos.CENTER)")
    		  .addStatement("this.getChildren().add(mainPane)")
    		  .addStatement("this.setPrefSize(900, 550)")
    		  .build();
      
      String refreshDescriptionAreaBlock = "";

      // go through all actions:
      writeElse = false;
      for (ActionType act : actions) {
        String uCaseName = 
        	CodeGeneratorUtils.getUpperCaseLeading(act.getName());
        String capsName = act.getName().toUpperCase();
        if (act.isVisibleInExplanatoryTool()) {
          if (writeElse) {
        	  refreshDescriptionAreaBlock += "else ";
          } else {
            writeElse = true;
          }
          refreshDescriptionAreaBlock += "if (action instanceof " + uCaseName + "Action) {\n";
          refreshDescriptionAreaBlock += "// triggers:\n";

          // go through all triggers:
          Vector<ActionTypeTrigger> triggers = act.getAllTriggers();
          for (int j = 0; j < triggers.size(); j++) {
            ActionTypeTrigger trigger = triggers.get(j);
            String triggerCapsName = trigger.getName().toUpperCase();
            if (j > 0) {
              refreshDescriptionAreaBlock += "else ";
            }
            refreshDescriptionAreaBlock += "if (trigOrDest == TRIGGER && name.equals(\""
                    + trigger.getName() + "\")) {\n";
            refreshDescriptionAreaBlock += "text = TriggerDescriptions." + capsName + "_"
                    + triggerCapsName + ";\n";
            refreshDescriptionAreaBlock += "}\n";
          }

          // go through all destroyers:
          Vector<ActionTypeDestroyer> destroyers = act.getAllDestroyers();
          for (int j = 0; j < destroyers.size(); j++) {
            ActionTypeDestroyer destroyer = destroyers
                .get(j);
            String destroyerCapsName = destroyer.getName().toUpperCase();
            if (j > 0) {
            	refreshDescriptionAreaBlock += "else ";
            }
            refreshDescriptionAreaBlock += "if (trigOrDest == DESTROYER && name.equals(\""
                    + destroyer.getName() + "\")) {\n";
            refreshDescriptionAreaBlock += "text = DestroyerDescriptions." + capsName + "_"
                    + destroyerCapsName + ";\n";
            refreshDescriptionAreaBlock += "}\n";
          }
          refreshDescriptionAreaBlock += "}\n";
        }
      }
      
      MethodSpec refreshDescriptionArea = MethodSpec.methodBuilder("refreshDescriptionArea")
    		  .addModifiers(Modifier.PRIVATE)
    		  .addParameter(int.class, "trigOrDest")
    		  .addStatement("$T name = trigOrDest == TRIGGER ? ($T) triggerList.getSelectionModel().getSelectedItem() : ($T) destroyerList.getSelectionModel().getSelectedItem()", String.class, String.class, String.class)
    		  .beginControlFlow("if (name != null)")
    		  .addStatement("$T actionName = this.action.getActionName()", String.class)
    		  .addStatement("descriptionArea.setText($T.getBackendRuleMappings(actionName, name))", ruleCategories)
    		  .addStatement("descriptionArea.positionCaret(0)")
    		  .endControlFlow()
    		  .build();
      
      MethodSpec handle = MethodSpec.methodBuilder("handle")
    		  .addModifiers(Modifier.PUBLIC)
    		  .addParameter(mouseEvent, "event")
    		  .beginControlFlow("if (event.getSource() == triggerList && triggerList.getSelectionModel().getSelectedIndex() >= 0)")
    		  .addStatement("$N(TRIGGER)", refreshDescriptionArea)
    		  .addStatement("destroyerList.getSelectionModel().clearSelection()")
    		  .endControlFlow()
    		  .beginControlFlow("else if (event.getSource() == destroyerList && destroyerList.getSelectionModel().getSelectedIndex() >= 0)")
    		  .addStatement("$N(DESTROYER)", refreshDescriptionArea)
    		  .addStatement("triggerList.getSelectionModel().clearSelection()")
    		  .endControlFlow()
    		  .build();
      
    	TypeSpec actionInfoPanel = TypeSpec.classBuilder("ActionInfoPanel")
    			.addModifiers(Modifier.PUBLIC)
    			.addField(action, "action")
    			.addField(ParameterizedTypeName.get(tableView, participant), "table", Modifier.PRIVATE)
    			.addField(ParameterizedTypeName.get(listView, string), "triggerList", Modifier.PRIVATE)
    			.addField(ParameterizedTypeName.get(listView, string), "destroyerList", Modifier.PRIVATE)
    			.addField(textArea, "descriptionArea", Modifier.PRIVATE)
    			.addField(textArea, "actionDescriptionArea", Modifier.PRIVATE)
    			.addField(FieldSpec.builder(int.class, "TRIGGER", Modifier.PRIVATE, Modifier.FINAL).initializer("0").build())
    			.addField(FieldSpec.builder(int.class, "DESTROYER", Modifier.PRIVATE, Modifier.FINAL).initializer("1").build())
    			.superclass(pane)
    			.addSuperinterface(ParameterizedTypeName.get(eventHandler, mouseEvent))
    			.addMethod(constructor)
    			.addMethod(initializeActionDescription)
    			.addMethod(initializeTriggerList)
    			.addMethod(initializeDestroyerList)
    			.addMethod(createParticipantsTable)
    			.addMethod(refreshDescriptionArea)
    			.addMethod(handle)
    			.build();
      
      JavaFile javaFile = JavaFile.builder("", actionInfoPanel)
  		    .build();

    try {
    	FileWriter writer = new FileWriter(actInfoFile);
    	
  	  	String toAppend = "/* File generated by: simse.codegenerator.explanatorytool.ActionInfoPanelGenerator */\n"
  	  		+ "package simse.explanatorytool;\n"
  	  		+ "\n"
  	  		+ "import simse.adts.actions.*;\n"
  	  		+ "import simse.adts.objects.*;\n"
  	  		+ "import java.util.Vector;";
    	
		writer.write(toAppend + javaFile.toString());
		
		writer.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
}