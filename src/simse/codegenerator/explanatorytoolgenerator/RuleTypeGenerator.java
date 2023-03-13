/*
 * This class is responsible for generating all of the code for the Branch
 * class in the explanatory tool
 */

package simse.codegenerator.explanatorytoolgenerator;

import simse.codegenerator.CodeGeneratorConstants;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.lang.model.element.Modifier;
import javax.swing.JOptionPane;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

public class RuleTypeGenerator implements CodeGeneratorConstants {
  private File directory; // directory to save generated code into

  public RuleTypeGenerator(File directory) {
    this.directory = directory;
  }

  public void generate() {
    File ruleTypeFile = new File(directory,
        ("simse\\explanatorytool\\RuleType.java"));
    if (ruleTypeFile.exists()) {
    	ruleTypeFile.delete(); // delete old version of file
    }
      
      
      TypeSpec ruleType = TypeSpec.enumBuilder("RuleType")
    		  .addModifiers(Modifier.PUBLIC)
    		  .addEnumConstant("TRIGGER")
    		  .addEnumConstant("DESTROY")
    		  .addEnumConstant("RULE")
    		  .addEnumConstant("ARTIFACT")
    		  .addEnumConstant("PROJECT")
    		  .addEnumConstant("PEOPLE")
    		  .build();
      
      JavaFile javaFile = JavaFile.builder("simse.explantorytool", ruleType)
    		    .build();

      try {
  		FileWriter writer = new FileWriter(ruleTypeFile);
  		javaFile.writeTo(writer);
  	} catch (IOException e) {
  		// TODO Auto-generated catch block
  		e.printStackTrace();
  	}
  }
}
