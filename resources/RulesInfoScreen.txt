package simse.gui;

import java.util.Vector;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import simse.adts.actions.Action;
import simse.state.State;
import simse.util.RuleCategories;
import simse.util.RuleType;

public class RulesInfoScreen  extends Stage implements EventHandler<MouseEvent>{
	State state;

	Label titleLabel;
	Label toolsLabel;
	
	BorderPane tablePane;
	VBox mainPane;
	
	private ComboBox<String> actionComboBox;
	private ListView<String> triggerRuleList;
	private ListView<String> destroyerRuleList;
	private ListView<String> intermediateRuleList;
	private TextArea descriptionArea;
	private String lastSelectedAction = "";
	private CheckBox advRulesCheck;
	private boolean advRulesOn;

	EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
        public void handle(ActionEvent e) {
            if (e.getSource() == advRulesCheck) {
            	advRulesOn = advRulesCheck.isSelected();
            	refreshRuleLists(lastSelectedAction);
                descriptionArea.setText("");
            } else if (e.getSource() == actionComboBox) {
            	if (actionComboBox.getItems().size() > 0) {
    				lastSelectedAction = (String) actionComboBox.getSelectionModel().getSelectedItem();
    				refreshRuleLists(lastSelectedAction);
    				descriptionArea.setText("");
    			}
            }
        }
    };
	
	public RulesInfoScreen(State s, RuleType ruleType) {
		this.state = s;

		this.setTitle("Rules Screen");
		mainPane = new VBox();
		
		ObservableList<String> actions = setActionsByType(ruleType);
		
		// Create viewRuleTitlePane and label:
		VBox ruleSelectorPane = new VBox();
		Pane viewRulesTitlePane = new Pane();
		viewRulesTitlePane.getChildren().add(new Label("View Rules:"));
		ruleSelectorPane.getChildren().add(viewRulesTitlePane);

		// Create actionsComboBoxPane:
		Pane actionComboBoxPane = new Pane();
		actionComboBoxPane.getChildren().add(new Label("Actions:"));
		actionComboBox = new ComboBox<String>(actions);
		actionComboBox.setOnAction(event);
		actionComboBoxPane.getChildren().add(actionComboBox);
		ruleSelectorPane.getChildren().add(actionComboBoxPane);
		
        TilePane advancedRulesPane = new TilePane();
        advRulesCheck = new CheckBox("View Advanced Rules");
        advancedRulesPane.getChildren().add(advRulesCheck);
        advRulesCheck.setIndeterminate(true);
        advRulesCheck.setOnAction(event);
        
        HBox selections = new HBox();
        selections.setPadding(new Insets(10));
        selections.getChildren().add(ruleSelectorPane);
        selections.getChildren().add(advancedRulesPane);
        mainPane.getChildren().add(selections);

		// Create rulesMainPane:
		TilePane rulesMainPane = new TilePane();

		// Create ruleListsPane:
		VBox ruleListsPane = new VBox();

		// rule lists:
		Pane trigRuleTitlePane = new Pane();
		trigRuleTitlePane.getChildren().add(new Label("Trigger Rules:"));
		ruleListsPane.getChildren().add(trigRuleTitlePane);
		triggerRuleList = new ListView<String>();
		triggerRuleList.setFixedCellSize(24);
		triggerRuleList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		triggerRuleList.addEventHandler(MouseEvent.MOUSE_CLICKED, this);
		triggerRuleList.setMinWidth(272);
		ScrollPane triggerRuleListPane = new ScrollPane(triggerRuleList);
		triggerRuleListPane.setMaxHeight(80);
		ruleListsPane.getChildren().add(triggerRuleListPane);

		Pane destRuleTitlePane = new Pane();
		destRuleTitlePane.getChildren().add(new Label("Destroyer Rules:"));
		ruleListsPane.getChildren().add(destRuleTitlePane);
		destroyerRuleList = new ListView<String>();
		destroyerRuleList.setFixedCellSize(24);
		destroyerRuleList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		destroyerRuleList.addEventHandler(MouseEvent.MOUSE_CLICKED, this);
		destroyerRuleList.setMinWidth(272);
		ScrollPane destroyerRuleListPane = new ScrollPane(destroyerRuleList);
		destroyerRuleListPane.setMaxHeight(80);
		ruleListsPane.getChildren().add(destroyerRuleListPane);

		Pane intRuleTitlePane = new Pane();
		intRuleTitlePane.getChildren().add(new Label("Intermediate Rules:"));
		ruleListsPane.getChildren().add(intRuleTitlePane);
		intermediateRuleList = new ListView<String>();
		intermediateRuleList.setFixedCellSize(24);
		intermediateRuleList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		intermediateRuleList.addEventHandler(MouseEvent.MOUSE_CLICKED, this);
		intermediateRuleList.setMinWidth(272);
		ScrollPane intermediateRuleListPane = new ScrollPane(intermediateRuleList);
		intermediateRuleListPane.setMaxHeight(80);
		ruleListsPane.getChildren().add(intermediateRuleListPane);
		ruleListsPane.setPadding(new Insets(0,40,0,40));
		rulesMainPane.getChildren().add(ruleListsPane);

		// description pane:
		VBox descriptionPane = new VBox();
		Pane descriptionTitlePane = new Pane();
		descriptionTitlePane.getChildren().add(new Label("Description:"));
		descriptionPane.getChildren().add(descriptionTitlePane);

		// description text area:
		descriptionArea = new TextArea();
		descriptionArea.setWrapText(true);
		descriptionArea.setPrefRowCount(16);
		descriptionArea.setPrefColumnCount(30);
		descriptionArea.setEditable(false);
		ScrollPane descriptionScrollPane = new ScrollPane(descriptionArea);
		descriptionScrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
		descriptionScrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
		descriptionPane.getChildren().add(descriptionScrollPane);

		rulesMainPane.getChildren().add(descriptionPane);

		// Add panes to main pane:
		mainPane.getChildren().add(rulesMainPane);
		
		Scene scene = new Scene(mainPane, 800, 400);
		this.setScene(scene);
	}

	private ObservableList<String> setActionsByType(RuleType ruleType) {
		ObservableList<String> actions = null;
		
		switch(ruleType) {
		case ARTIFACT:
			actions = FXCollections.observableArrayList(
					Action.CREATEREQUIREMENTS, Action.REVIEWREQUIREMENTS,
					Action.CORRECTREQUIREMENTS, Action.CREATEDESIGN, Action.REVIEWDESIGN,
					Action.CORRECTDESIGN, Action.CREATECODE, Action.INSPECTCODE, Action.CORRECTCODE,
					Action.INTEGRATECODE, Action.SYSTEMTEST, Action.CREATESYSTEMTESTPLAN,
					Action.REVIEWSYSTEMTESTPLAN, Action.CORRECTSYSTEMTESTPLAN,
					Action.DELIVERPRODUCT, Action.INTRODUCENEWREQUIREMENTS);
			break;
		case PROJECT:
			actions = FXCollections.observableArrayList( Action.DELIVERPRODUCT, Action.QUIT, 
					Action.INTRODUCENEWREQUIREMENTS, Action.GIVEBONUS, Action.FIRE, Action.PURCHASETOOL, 
					Action.SUGGESTEDREQUIREMENTSPHASEDURATION, Action.SUGGESTEDDESIGNPHASEDURATION, 
					Action.SUGGESTEDIMPLINTEGRATIONPHASEDURATION, Action.SUGGESTEDTESTINGPHASEDURATION);
			break;
		case PEOPLE:
			actions = FXCollections.observableArrayList(
					Action.CREATEREQUIREMENTS, Action.REVIEWREQUIREMENTS,
					Action.CORRECTREQUIREMENTS, Action.CREATEDESIGN, Action.REVIEWDESIGN,
					Action.CORRECTDESIGN, Action.CREATECODE, Action.INSPECTCODE, Action.CORRECTCODE,
					Action.INTEGRATECODE, Action.SYSTEMTEST, Action.CREATESYSTEMTESTPLAN,
					Action.REVIEWSYSTEMTESTPLAN, Action.CORRECTSYSTEMTESTPLAN, Action.DELIVERPRODUCT, 
					Action.BREAK, Action.GETSICK, Action.QUIT, Action.CHANGEPAYRATE, Action.GIVEBONUS);
			break;
		default:
			actions = FXCollections.observableArrayList(
					Action.CREATEREQUIREMENTS, Action.REVIEWREQUIREMENTS,
					Action.CORRECTREQUIREMENTS, Action.CREATEDESIGN, Action.REVIEWDESIGN,
					Action.CORRECTDESIGN, Action.CREATECODE, Action.INSPECTCODE, Action.CORRECTCODE,
					Action.INTEGRATECODE, Action.SYSTEMTEST, Action.CREATESYSTEMTESTPLAN,
					Action.REVIEWSYSTEMTESTPLAN, Action.CORRECTSYSTEMTESTPLAN,
					Action.DELIVERPRODUCT, Action.BREAK, Action.GETSICK, Action.QUIT,
					Action.INTRODUCENEWREQUIREMENTS, Action.CHANGEPAYRATE, Action.GIVEBONUS,
					Action.FIRE, Action.PURCHASETOOL, Action.SUGGESTEDREQUIREMENTSPHASEDURATION,
					Action.SUGGESTEDDESIGNPHASEDURATION, Action.SUGGESTEDIMPLINTEGRATIONPHASEDURATION,
					Action.SUGGESTEDTESTINGPHASEDURATION);
		}
		return actions;
	}

	private void refreshRuleLists(String actionName) {
		triggerRuleList.getItems().setAll(new Vector<String>());
		destroyerRuleList.getItems().setAll(new Vector<String>());
		intermediateRuleList.getItems().setAll(new Vector<String>());
		
		intermediateRuleList.getItems().setAll(RuleCategories.getIntRulesForAction(actionName));
		if (advRulesOn) {
			triggerRuleList.getItems().setAll(RuleCategories.getAllTrigRulesForAction(actionName));
			destroyerRuleList.getItems().setAll(RuleCategories.getAllDestRulesForAction(actionName));
		} else {
			triggerRuleList.getItems().setAll(RuleCategories.getTrigRulesForAction(actionName));
			destroyerRuleList.getItems().setAll(RuleCategories.getDestRulesForAction(actionName));
		}
	}

	// refreshes the description area with the selected rule description
	private void refreshDescriptionArea(String ruleName) {
		if (ruleName != null) {
			String text = RuleCategories.getRuleMapping(ruleName);
			if (text == "" && advRulesOn) {
				text = RuleCategories.getBackendRuleMappings(lastSelectedAction, ruleName);
			}
			descriptionArea.setText(text);
			descriptionArea.positionCaret(0);
		}
	}

	@Override
	public void handle(MouseEvent event) {
		Object source = event.getSource();
		
		if ((source == triggerRuleList && !triggerRuleList.getSelectionModel().isEmpty())) {
			destroyerRuleList.getSelectionModel().clearSelection();
			intermediateRuleList.getSelectionModel().clearSelection();
			refreshDescriptionArea((String) triggerRuleList.getSelectionModel().getSelectedItem());
		} else if (source == destroyerRuleList && !destroyerRuleList.getSelectionModel().isEmpty()) {
			triggerRuleList.getSelectionModel().clearSelection();
			intermediateRuleList.getSelectionModel().clearSelection();
			refreshDescriptionArea((String) destroyerRuleList.getSelectionModel().getSelectedItem());
		} else if (source == intermediateRuleList && !intermediateRuleList.getSelectionModel().isEmpty()) {
			triggerRuleList.getSelectionModel().clearSelection();
			destroyerRuleList.getSelectionModel().clearSelection();
			refreshDescriptionArea((String) intermediateRuleList.getSelectionModel().getSelectedItem());
		}
	}
}
