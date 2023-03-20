/*
 * This class is responsible for generating all of the code for the
 * RuleDescriptions class in the explanatory tool
 */

package simse.codegenerator.utilgenerator;

import simse.codegenerator.CodeGeneratorConstants;
import simse.modelbuilder.actionbuilder.ActionType;
import simse.modelbuilder.actionbuilder.DefinedActionTypes;
import simse.modelbuilder.rulebuilder.Rule;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import javax.lang.model.element.Modifier;
import javax.swing.JOptionPane;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

public class RuleDescriptionsGenerator implements CodeGeneratorConstants {
  private File directory; // directory to save generated code into
  private DefinedActionTypes actTypes;

  public RuleDescriptionsGenerator(DefinedActionTypes actTypes, 
  		File directory) {
    this.actTypes = actTypes;
    this.directory = directory;
  }

  public void generate() {
    File ruleDescFile = new File(directory,
        ("simse\\util\\RuleDescriptions.java"));
    if (ruleDescFile.exists()) {
      ruleDescFile.delete(); // delete old version of file
    }
      
    ArrayList<FieldSpec> actionFields = new ArrayList<>();

      // go through all actions:
      Vector<ActionType> actions = actTypes.getAllActionTypes();
      for (ActionType act : actions) {
    	  String actionsString = "\"";
        if (act.isVisibleInExplanatoryTool()) {
          Vector<Rule> rules = act.getAllRules();
          for (int j = 0; j < rules.size(); j++) {
            Rule rule = rules.get(j);
            if (rule.isVisibleInExplanatoryTool()) {
              actionsString += rule.getAnnotation().replaceAll("\"\n", "\\\n").
                      replaceAll("\"", "\\\\\"") + "\"";
            }
            actionFields.add(FieldSpec.builder(String.class, 
            		act.getName().toUpperCase()
                    + "_" + rule.getName().toUpperCase(), Modifier.STATIC, Modifier.PUBLIC, Modifier.FINAL).initializer(actionsString).build());
          }
        }
        
        
        
      }
      TypeSpec ruleDescriptions = TypeSpec.classBuilder("RuleDescriptions")
    		  .addModifiers(Modifier.PUBLIC)
    		  .addFields(actionFields)
    		  .build();
    		  
      JavaFile javaFile = JavaFile.builder("simse.util", ruleDescriptions).build();
      
      try {
    	FileWriter writer = new FileWriter(ruleDescFile);
		javaFile.writeTo(writer);
		
		writer.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
}