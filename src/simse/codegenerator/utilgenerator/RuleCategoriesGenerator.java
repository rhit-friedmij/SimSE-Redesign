/*
 * This class is responsible for generating all of the code for the Branch
 * class in the explanatory tool
 */

package simse.codegenerator.utilgenerator;

import simse.codegenerator.CodeGeneratorConstants;
import simse.codegenerator.CodeGeneratorUtils;
import simse.modelbuilder.actionbuilder.ActionType;
import simse.modelbuilder.actionbuilder.ActionTypeDestroyer;
import simse.modelbuilder.actionbuilder.ActionTypeTrigger;
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
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

public class RuleCategoriesGenerator implements CodeGeneratorConstants {
  private File directory; // directory to save generated code into
  private DefinedActionTypes actTypes;
  
  
  public RuleCategoriesGenerator(File directory, DefinedActionTypes actTypes) {
    this.directory = directory;
    this.actTypes = actTypes;
  }

  public void generate() {
    File ruleCategoriesFile = new File(directory,
        ("simse\\util\\RuleCategories.java"));
    if (ruleCategoriesFile.exists()) {
    	ruleCategoriesFile.delete(); // delete old version of file
    } 
      ArrayTypeName stringArray = ArrayTypeName.of(String.class);
      ClassName hashTable = ClassName.get("java.util", "Hashtable");
      ClassName string = ClassName.get(String.class);
      ParameterizedTypeName doubleStringHashTable = ParameterizedTypeName.get(hashTable, string, string);
      ParameterizedTypeName stringStringArrayHashTable = ParameterizedTypeName.get(hashTable, string, stringArray);
      ParameterizedTypeName stringdoubleStringHashTable = ParameterizedTypeName.get(hashTable, string, doubleStringHashTable);
      
      Vector<ActionType> actions = actTypes.getAllActionTypes();
      
      String actionBlock = "{";
      
      for(int i = 0; i < actions.size(); i++) {
    	  if(actions.size() == 1 || i == actions.size() - 1) {
    		  actionBlock += "Action." + actions.get(i).getName().toUpperCase();
    	  }
    	  else {
    		  actionBlock += "Action." + actions.get(i).getName().toUpperCase() + ", ";
    	  }
      }
      
      actionBlock += "}";
      
      CodeBlock actionArray = CodeBlock.builder()
    		  .add(actionBlock)
    		  .build();
      
      MethodSpec getAllDestRulesForAction = MethodSpec.methodBuilder("getAllDestRulesForAction")
    		  .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
    		  .returns(stringArray)
    		  .addParameter(String.class, "actionName")
    		  .addStatement("$T rules = getDestRulesForAction(actionName)", stringArray)
    		  .addStatement("$T backendRules = getBackendDestRulesForAction(actionName)", stringArray)
    		  .addStatement("$T rulesLen = rules.length", int.class)
    		  .addStatement("$T bRulesLen = backendRules.length", int.class)
    		  .addStatement("$T result = new $T[rulesLen + bRulesLen]", stringArray, String.class)
    		  .addStatement("System.arraycopy(rules, 0, result, 0, rulesLen)")
    		  .addStatement("System.arraycopy(backendRules, 0, result, rulesLen, bRulesLen)")
    		  .addStatement("return result")
    		  .build();
      
      MethodSpec getAllTrigRulesForAction = MethodSpec.methodBuilder("getAllTrigRulesForAction")
    		  .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
    		  .returns(stringArray)
    		  .addParameter(String.class, "actionName")
    		  .addStatement("$T rules = getTrigRulesForAction(actionName)", stringArray)
    		  .addStatement("$T backendRules = getBackendTrigRulesForAction(actionName)", stringArray)
    		  .addStatement("$T rulesLen = rules.length", int.class)
    		  .addStatement("$T bRulesLen = backendRules.length", int.class)
    		  .addStatement("$T result = new $T[rulesLen + bRulesLen]", stringArray, String.class)
    		  .addStatement("System.arraycopy(rules, 0, result, 0, rulesLen)")
    		  .addStatement("System.arraycopy(backendRules, 0, result, rulesLen, bRulesLen)")
    		  .addStatement("return result")
    		  .build();
      
      MethodSpec getAllRuleMappings = MethodSpec.methodBuilder("getAllRuleMappings")
    		  .addModifiers(Modifier.PUBLIC)
    		  .returns(String.class)
    		  .addModifiers(Modifier.STATIC)
    		  .addParameter(String.class, "actionName")
    		  .addParameter(String.class, "ruleName")
    		  .addStatement("$T text = getRuleMapping(ruleName)", String.class)
    		  .beginControlFlow("if (text == \"\")")
    		  .addStatement("text = getBackendRuleMappings(actionName, ruleName)")
    		  .endControlFlow()
    		  .addStatement("return text")
    		  .build();
      
      MethodSpec getBackendDestRulesForAction = MethodSpec.methodBuilder("getBackendDestRulesForAction")
    		  .addModifiers(Modifier.PUBLIC)
    		  .returns(stringArray)
    		  .addModifiers(Modifier.STATIC)
    		  .addParameter(String.class, "actionName")
    		  .addStatement("$T rules = destBackendRules.get(actionName)", stringArray)
    		  .beginControlFlow("if (rules == null)")
    		  .addStatement("rules = new $T{}", stringArray)
    		  .endControlFlow()
    		  .addStatement("return rules")
    		  .build();
      
      MethodSpec getBackendTrigRulesForAction = MethodSpec.methodBuilder("getBackendTrigRulesForAction")
    		  .addModifiers(Modifier.PUBLIC)
    		  .returns(stringArray)
    		  .addModifiers(Modifier.STATIC)
    		  .addParameter(String.class, "actionName")
    		  .addStatement("$T rules = trigBackendRules.get(actionName)", stringArray)
    		  .beginControlFlow("if (rules == null)")
    		  .addStatement("rules = new $T{}", stringArray)
    		  .endControlFlow()
    		  .addStatement("return rules")
    		  .build();
      
      MethodSpec getDestRulesForAction = MethodSpec.methodBuilder("getDestRulesForAction")
    		  .addModifiers(Modifier.PUBLIC)
    		  .returns(stringArray)
    		  .addModifiers(Modifier.STATIC)
    		  .addParameter(String.class, "actionName")
    		  .addStatement("$T rules = destRules.get(actionName)", stringArray)
    		  .beginControlFlow("if (rules == null)")
    		  .addStatement("rules = new $T{}", stringArray)
    		  .endControlFlow()
    		  .addStatement("return rules")
    		  .build();
      
      MethodSpec getTrigRulesForAction = MethodSpec.methodBuilder("getTrigRulesForAction")
    		  .addModifiers(Modifier.PUBLIC)
    		  .returns(stringArray)
    		  .addModifiers(Modifier.STATIC)
    		  .addParameter(String.class, "actionName")
    		  .addStatement("$T rules = trigRules.get(actionName)", stringArray)
    		  .beginControlFlow("if (rules == null)")
    		  .addStatement("rules = new $T{}", stringArray)
    		  .endControlFlow()
    		  .addStatement("return rules")
    		  .build();
      
      MethodSpec getIntRulesForAction = MethodSpec.methodBuilder("getIntRulesForAction")
    		  .addModifiers(Modifier.PUBLIC)
    		  .returns(stringArray)
    		  .addModifiers(Modifier.STATIC)
    		  .addParameter(String.class, "actionName")
    		  .addStatement("$T rules = intRules.get(actionName)", stringArray)
    		  .beginControlFlow("if (rules == null)")
    		  .addStatement("rules = new $T{}", stringArray)
    		  .endControlFlow()
    		  .addStatement("return rules")
    		  .build();
      
      MethodSpec getBackendRuleMappings = MethodSpec.methodBuilder("getBackendRuleMappings")
    		  .addModifiers(Modifier.PUBLIC)
    		  .returns(String.class)
    		  .addModifiers(Modifier.STATIC)
    		  .addParameter(String.class, "actionName")
    		  .addParameter(String.class, "ruleName")
    		  .addStatement("$T text = backendRuleMapping.get(actionName).get(ruleName)", String.class)
    		  .beginControlFlow("if (text == null)")
    		  .addStatement("text = \"\"")
    		  .endControlFlow()
    		  .addStatement("return text")
    		  .build();
      
      MethodSpec getRuleMapping = MethodSpec.methodBuilder("getRuleMapping")
    		  .addModifiers(Modifier.PUBLIC)
    		  .returns(String.class)
    		  .addModifiers(Modifier.STATIC)
    		  .addParameter(String.class, "ruleName")
    		  .addStatement("$T text = ruleMapping.get(ruleName)", String.class)
    		  .beginControlFlow("if (text == null)")
    		  .addStatement("text = \"\"")
    		  .endControlFlow()
    		  .addStatement("return text")
    		  .build();
      
      MethodSpec makeRuleMappingTable = MethodSpec.methodBuilder("makeRuleMappingTable")
    		  .addModifiers(Modifier.PUBLIC)
    		  .returns(doubleStringHashTable)
    		  .addModifiers(Modifier.STATIC)
    		  .addParameter(stringArray, "rules")
    		  .addParameter(stringArray, "ruleDesc")
    		  .addStatement("$T ruleMaps = new $T<>()", doubleStringHashTable, hashTable)
    		  .beginControlFlow("if (rules.length == ruleDesc.length)")
    		  .beginControlFlow("for (int i = 0; i < rules.length; i++)")
    		  .addStatement("ruleMaps.put(rules[i], ruleDesc[i])")
    		  .endControlFlow()
    		  .endControlFlow()
    		  .addStatement("return ruleMaps")
    		  .build();
      
      String ruleDescriptionBlock = "";
      
      
      for (ActionType act : actions) {
        if (act.isVisibleInExplanatoryTool()) {
        	Vector<Rule> rules = act.getAllRules();
        	for(Rule rule : rules) {
        		if (rule.isVisibleInExplanatoryTool()) {
        			ruleDescriptionBlock += "ruleMapping.put(\"" + rule.getName() + "\", RuleDescriptions." + act.getName().toUpperCase() + "_" + rule.getName().toUpperCase() + ");\n";
        		}
        	}
        }
      }
      
      MethodSpec initalizeRuleMapping = MethodSpec.methodBuilder("initializeRuleMapping")
    		  .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
    		  .addStatement("ruleMapping = new $T<>()", hashTable)
    		  .addCode(ruleDescriptionBlock)
    		  .build();
      
      String intRuleDescriptionBlock = "String[][] actionRules = {";
      for (ActionType act : actions) {
        if (act.isVisibleInExplanatoryTool()) {
        	Vector<Rule> rules = act.getAllContinuousRules();
        	intRuleDescriptionBlock += "new String[]{";
        	String ruleBlock = "";
        	for(int i = 0; i < rules.size(); i++) {
        		Rule rule = rules.get(i);
        		if (rule.isVisibleInExplanatoryTool()) {
	        		if(rules.size() == 1 || i == rules.size() - 1) {
	        			ruleBlock += "\"" + rule.getName() + "\"";
	        		} else {
	        			ruleBlock += "\"" + rule.getName() + "\"" + ", ";
	        		}
        		}	
        	}
        	intRuleDescriptionBlock += ruleBlock + "},\n";
        }
      }
      intRuleDescriptionBlock += "};\n";
      
      MethodSpec initializeIntRules = MethodSpec.methodBuilder("initializeIntRules")
    		  .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
    		  .addStatement("destRules = new $T<>()", hashTable)
    		  .addCode(intRuleDescriptionBlock)
    		  .beginControlFlow("if (actions.length == actionRules.length)")
    		  .beginControlFlow("for (int i = 0; i < actions.length; i++)")
    		  .addStatement("intRules.put(actions[i], actionRules[i])")
    		  .endControlFlow()
    		  .endControlFlow()
    		  .build();
      
      String trigRuleDescriptionBlock = "String[][] actionRules = {";
      for (ActionType act : actions) {
        if (act.isVisibleInExplanatoryTool()) {
        	Vector<Rule> rules = act.getAllTriggerRules();
        	trigRuleDescriptionBlock += "new String[]{";
        	String ruleBlock = "";
        	for(int i = 0; i < rules.size(); i++) {
        		Rule rule = rules.get(i);
        		if (rule.isVisibleInExplanatoryTool()) {
	        		if(rules.size() == 1 || i == rules.size() - 1) {
	        			ruleBlock += "\"" + rule.getName() + "\"";
	        		} else {
	        			ruleBlock += "\"" + rule.getName() + "\"" + ", ";
	        		}
        		}
        	}
        	trigRuleDescriptionBlock += ruleBlock + "},\n";
        	
        }
      }
      trigRuleDescriptionBlock += "};\n";
      
      MethodSpec initializeTrigRules = MethodSpec.methodBuilder("initializeTrigRules")
    		  .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
    		  .addStatement("trigRules = new $T<>()", hashTable)
    		  .addCode(trigRuleDescriptionBlock)
    		  .beginControlFlow("if (actions.length == actionRules.length)")
    		  .beginControlFlow("for (int i = 0; i < actions.length; i++)")
    		  .addStatement("trigRules.put(actions[i], actionRules[i])")
    		  .endControlFlow()
    		  .endControlFlow()
    		  .build();
      
      String destRuleDescriptionBlock = "String[][] actionRules = {";
      for (ActionType act : actions) {
        if (act.isVisibleInExplanatoryTool()) {
        	Vector<Rule> rules = act.getAllDestroyerRules();
        	destRuleDescriptionBlock += "new String[]{";
        	String ruleBlock = "";
        	for(int i = 0; i < rules.size(); i++) {
        		Rule rule = rules.get(i);
        		if (rule.isVisibleInExplanatoryTool()) {
	        		if(rules.size() == 1 || i == rules.size() - 1) {
	        			ruleBlock += "\"" + rule.getName() + "\"";
	        		} else {
	        			ruleBlock += "\"" + rule.getName() + "\"" + ", ";
	        		}
        		}
        	}
        	destRuleDescriptionBlock += ruleBlock + "},\n";
        	
        }
      }
      destRuleDescriptionBlock += "};\n";
      
      MethodSpec initializeDestRules = MethodSpec.methodBuilder("initializeDestRules")
    		  .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
    		  .addStatement("destRules = new $T<>()", hashTable)
    		  .addCode(destRuleDescriptionBlock)
    		  .beginControlFlow("if (actions.length == actionRules.length)")
    		  .beginControlFlow("for (int i = 0; i < actions.length; i++)")
    		  .addStatement("destRules.put(actions[i], actionRules[i])")
    		  .endControlFlow()
    		  .endControlFlow()
    		  .build();
      
      String backendRuleBlock = "";
      for (ActionType act : actions) {
    	  if (act.isVisibleInExplanatoryTool()) {
        	Vector<ActionTypeDestroyer> destroyerRules = act.getAllDestroyers();
        	Vector<ActionTypeTrigger> triggerRules = act.getAllTriggers();
        	backendRuleBlock += "backendRuleMapping.put(Action." + act.getName().toUpperCase()
        			+ ", makeRuleMappingTable(";
        	backendRuleBlock += "new String[]{";
        	
        	//Generate trigger/destroyer names
        	String ruleBlock = "";        	
        	
        	//Generate trigger rules
        	String triggerRulesBlock = "";
        	for(int i = 0; i < triggerRules.size(); i++) {
        		ActionTypeTrigger triggerRule = triggerRules.get(i);
        		ruleBlock += "\"" + triggerRule.getName() + "\"";
        		if(triggerRules.size() != 1 || i != triggerRules.size() - 1 || destroyerRules.size() != 0) {
        			ruleBlock += ", ";
        		}
        		triggerRulesBlock += "TriggerDescriptions." + act.getName().toUpperCase() + "_" + triggerRule.getName().toUpperCase();			
        		if(i != triggerRules.size() - 1 || destroyerRules.size() != 0) {
        			triggerRulesBlock += ", ";		
        		} 
    			
        	}
        	
        	//Generate destroyer rules
        	String destroyerRulesBlock = "";
        	for(int i = 0; i < destroyerRules.size(); i++) {
        		ActionTypeDestroyer destroyerRule = destroyerRules.get(i);
        		ruleBlock += "\"" + destroyerRule.getName() + "\"";
        		if(destroyerRules.size() != 1 || i != destroyerRules.size() - 1) {
        			ruleBlock += ", ";
        		}
        		destroyerRulesBlock += "DestroyerDescriptions." + act.getName().toUpperCase() + "_" + destroyerRule.getName().toUpperCase();
        		if(i != destroyerRules.size() - 1) {
        			destroyerRulesBlock += ", ";		
        		} 
        					
        	}
        	backendRuleBlock += ruleBlock + "},\n";
        	backendRuleBlock += "new String[]{";
        	backendRuleBlock += triggerRulesBlock;
        	backendRuleBlock += destroyerRulesBlock + "}));\n";
    	  }
      }
      
      MethodSpec initializeBackendRuleMapping = MethodSpec.methodBuilder("initializeBackendRuleMapping")
    		  .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
    		  .addStatement("backendRuleMapping = new $T<>()", hashTable)
    		  .addCode(backendRuleBlock)
    		  .build();
      
      String backendTrigRuleDescriptionBlock = "String[][] actionRules = {";
      for (ActionType act : actions) {
        if (act.isVisibleInExplanatoryTool()) {
        	Vector<ActionTypeTrigger> rules = act.getAllTriggers();
        	backendTrigRuleDescriptionBlock += "new String[]{";
        	String ruleBlock = "";
        	for(int i = 0; i < rules.size(); i++) {
        		ActionTypeTrigger rule = rules.get(i);
        		if(rules.size() == 1 || i == rules.size() - 1) {
        			ruleBlock += "\"" + rule.getName() + "\"";
        		} else {
        			ruleBlock += "\"" + rule.getName() + "\"" + ", ";
        		}
        	}
        	backendTrigRuleDescriptionBlock += ruleBlock + "},\n";
        	
        }
      }
      backendTrigRuleDescriptionBlock += "};\n";
      
      MethodSpec initializeBackendTrigRules = MethodSpec.methodBuilder("initializeBackendTrigRules")
    		  .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
    		  .addStatement("trigBackendRules = new $T<>()", hashTable)
    		  .addCode(backendTrigRuleDescriptionBlock)
    		  .build();
      
      String backendDestRuleDescriptionBlock = "String[][] actionRules = {";
      for (ActionType act : actions) {
        if (act.isVisibleInExplanatoryTool()) {
        	Vector<ActionTypeDestroyer> rules = act.getAllDestroyers();
        	backendDestRuleDescriptionBlock += "new String[]{";
        	String ruleBlock = "";
        	for(int i = 0; i < rules.size(); i++) {
        		ActionTypeDestroyer rule = rules.get(i);
        		if(rules.size() == 1 || i == rules.size() - 1) {
        			ruleBlock += "\"" + rule.getName() + "\"";
        		} else {
        			ruleBlock += "\"" + rule.getName() + "\"" + ", ";
        		}
        	}
        	backendDestRuleDescriptionBlock += ruleBlock + "},\n";
        	
        }
      }
      backendDestRuleDescriptionBlock += "};\n";
      
      MethodSpec initializeBackendDestRules = MethodSpec.methodBuilder("initializeBackendDestRules")
    		  .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
    		  .addStatement("destBackendRules = new $T<>()", hashTable)
    		  .addCode(backendDestRuleDescriptionBlock)
    		  .build();
      
      MethodSpec initializeRuleCategories = MethodSpec.methodBuilder("initializeRuleCategories")
    		  .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
    		  .addStatement("$N()", initalizeRuleMapping)
    		  .addStatement("$N()", initializeIntRules)
    		  .addStatement("$N()", initializeTrigRules)
    		  .addStatement("$N()", initializeDestRules)
    		  .addStatement("$N()", initializeBackendRuleMapping)
    		  .addStatement("$N()", initializeBackendTrigRules)
    		  .addStatement("$N()", initializeBackendDestRules)
    		  .build();
      
      
      TypeSpec ruleCategories = TypeSpec.classBuilder("RuleCategories")
    		  .addModifiers(Modifier.PUBLIC)
    		  .addField(doubleStringHashTable, "ruleMapping", Modifier.STATIC, Modifier.PRIVATE)
    		  .addField(stringStringArrayHashTable, "intRules", Modifier.STATIC, Modifier.PRIVATE)
    		  .addField(stringStringArrayHashTable, "destRules", Modifier.STATIC, Modifier.PRIVATE)
    		  .addField(stringStringArrayHashTable, "trigRules", Modifier.STATIC, Modifier.PRIVATE)
    		  .addField(stringStringArrayHashTable, "trigBackendRules", Modifier.STATIC, Modifier.PRIVATE)
    		  .addField(stringStringArrayHashTable, "destBackendRules", Modifier.STATIC, Modifier.PRIVATE)
    		  .addField(stringdoubleStringHashTable, "backendRuleMapping", Modifier.STATIC, Modifier.PRIVATE)
    		  .addField(FieldSpec.builder(stringArray, "actions", Modifier.STATIC, Modifier.PRIVATE).initializer(actionArray).build())
    		  .addMethod(initializeRuleCategories)
    		  .addMethod(initalizeRuleMapping)
    		  .addMethod(initializeIntRules)
    		  .addMethod(initializeTrigRules)
    		  .addMethod(initializeDestRules)
    		  .addMethod(initializeBackendRuleMapping)
    		  .addMethod(initializeBackendTrigRules)
    		  .addMethod(initializeBackendDestRules)
    		  .addMethod(makeRuleMappingTable)
    		  .addMethod(getRuleMapping)
    		  .addMethod(getIntRulesForAction)
    		  .addMethod(getTrigRulesForAction)
    		  .addMethod(getDestRulesForAction)
    		  .addMethod(getBackendRuleMappings)
    		  .addMethod(getBackendTrigRulesForAction)
    		  .addMethod(getBackendDestRulesForAction)
    		  .addMethod(getAllRuleMappings)
    		  .addMethod(getAllTrigRulesForAction)
    		  .addMethod(getAllDestRulesForAction)
    		  .build();
      
      JavaFile javaFile = JavaFile.builder("", ruleCategories).build();
      
      try {
    	FileWriter writer = new FileWriter(ruleCategoriesFile);
  	  	String toAppend = "/* File generated by: simse.codegenerator.util.RuleCategories */\n"
  	  	  		+ "package simse.util;\n"
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
