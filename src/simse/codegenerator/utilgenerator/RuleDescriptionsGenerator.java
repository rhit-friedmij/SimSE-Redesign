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
import java.util.Vector;

import javax.swing.JOptionPane;

import com.squareup.javapoet.CodeBlock;
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
      String actionsString = "";

      // go through all actions:
      Vector<ActionType> actions = actTypes.getAllActionTypes();
      for (ActionType act : actions) {
        if (act.isVisibleInExplanatoryTool()) {
          Vector<Rule> rules = act.getAllRules();
          for (int j = 0; j < rules.size(); j++) {
            Rule rule = rules.get(j);
            if (rule.isVisibleInExplanatoryTool()) {
              actionsString += "static final String " + act.getName().toUpperCase()
                      + "_" + rule.getName().toUpperCase() + " = \""
                      + rule.getAnnotation().replaceAll("\n", "\\\\n").
                      replaceAll("\"", "\\\\\"") + "\";";
//              writer.write("static final String " + act.getName().toUpperCase()
//                  + "_" + rule.getName().toUpperCase() + " = \""
//                  + rule.getAnnotation().replaceAll("\n", "\\\\n").
//                  replaceAll("\"", "\\\\\"") + "\";");
//              writer.write(NEWLINE);
              actionsString += "\n";
            }
          }
        }
      }
      TypeSpec ruleDescriptions = TypeSpec.classBuilder("RuleDescriptions")
    		  .addStaticBlock(CodeBlock.builder()
    				  .add(actionsString)
    				  .build())
    		  .build();
    		  
      JavaFile javaFile = JavaFile.builder("simse.util", ruleDescriptions).build();
      
      try {
    	FileWriter writer = new FileWriter(ruleDescFile);
		javaFile.writeTo(writer);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
}