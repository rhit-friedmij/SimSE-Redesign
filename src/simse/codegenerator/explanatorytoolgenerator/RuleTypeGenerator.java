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
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

public class RuleTypeGenerator implements CodeGeneratorConstants {
  private File directory; // directory to save generated code into

  public RuleTypeGenerator(File directory) {
    this.directory = directory;
  }

  public void generate() {
    File branchFile = new File(directory,
        ("simse\\explanatorytool\\Branch.java"));
    if (branchFile.exists()) {
      branchFile.delete(); // delete old version of file
    }
    try {
      FileWriter writer = new FileWriter(branchFile);
      
      
      TypeSpec ruleType = TypeSpec.enumBuilder("RuleType")
    		  .addModifiers(Modifier.PUBLIC)
    		  .addEnumConstant("TRIGGER")
    		  .addEnumConstant("DESTROY")
    		  .addEnumConstant("RULE")
    		  .addEnumConstant("ARTIFACT")
    		  .addEnumConstant("PROJECT")
    		  .addEnumConstant("PEOPLE")
    		  .build();
     
    } catch (IOException e) {
      JOptionPane.showMessageDialog(null, ("Error writing file "
          + branchFile.getPath() + ": " + e.toString()), "File IO Error",
          JOptionPane.WARNING_MESSAGE);
    }
  }
}
