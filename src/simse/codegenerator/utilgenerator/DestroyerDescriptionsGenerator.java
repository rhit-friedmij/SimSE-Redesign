/*
 * This class is responsible for generating all of the code for the
 * DestroyerDescriptions class in the explanatory tool
 */

package simse.codegenerator.utilgenerator;

import simse.codegenerator.CodeGeneratorConstants;
import simse.modelbuilder.actionbuilder.ActionType;
import simse.modelbuilder.actionbuilder.ActionTypeParticipantAttributeConstraint;
import simse.modelbuilder.actionbuilder.ActionTypeParticipantConstraint;
import simse.modelbuilder.actionbuilder.ActionTypeParticipantDestroyer;
import simse.modelbuilder.actionbuilder.ActionTypeDestroyer;
import simse.modelbuilder.actionbuilder.DefinedActionTypes;
import simse.modelbuilder.actionbuilder.RandomActionTypeDestroyer;
import simse.modelbuilder.actionbuilder.TimedActionTypeDestroyer;
import simse.modelbuilder.actionbuilder.UserActionTypeDestroyer;
import simse.modelbuilder.objectbuilder.AttributeTypes;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import javax.swing.*;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

public class DestroyerDescriptionsGenerator implements CodeGeneratorConstants {
  private File directory; // directory to save generated code into
  private DefinedActionTypes actTypes;

  public DestroyerDescriptionsGenerator(DefinedActionTypes actTypes, 
  		File directory) {
    this.actTypes = actTypes;
    this.directory = directory;
  }

  public void generate() {
    File destDescFile = new File(directory,
        ("simse\\explanatorytool\\DestroyerDescriptions.java"));
    if (destDescFile.exists()) {
      destDescFile.delete(); // delete old version of file
    }  
      String actionsString = "";

      // go through all actions:
      Vector<ActionType> actions = actTypes.getAllActionTypes();
      for (ActionType act : actions) {
        if (act.isVisibleInExplanatoryTool()) {
          Vector<ActionTypeDestroyer> destroyers = act.getAllDestroyers();
          for (ActionTypeDestroyer destroyer : destroyers) {
    	    actionsString += "static final String " + act.getName().toUpperCase()
                      + "_" + destroyer.getName().toUpperCase() + " = ";
            actionsString += "\n";

            actionsString += "\"This action stops ";
            if (destroyer instanceof TimedActionTypeDestroyer) {
              actionsString += "when the action has been occuring for "
                      + ((TimedActionTypeDestroyer) destroyer).getTime()
                      + " clock ticks.";
            } else {
              if (destroyer instanceof RandomActionTypeDestroyer) {
                actionsString += ((RandomActionTypeDestroyer) destroyer)
                        .getFrequency() + "% of the time ";
              } else if (destroyer instanceof UserActionTypeDestroyer) {
                actionsString += "when the user chooses the menu item \\\""
                        + ((UserActionTypeDestroyer) destroyer).getMenuText()
                        + "\\\" and ";
              }

              actionsString += "when the following conditions are met: \\n";

              // go through all participant conditions:
              Vector<ActionTypeParticipantDestroyer> partDestroyers = 
              	destroyer.getAllParticipantDestroyers();
              for (ActionTypeParticipantDestroyer partDestroyer : 
              	partDestroyers) {
                String partName = partDestroyer.getParticipant().getName();

                // go through all ActionTypeParticipantConstraints for this
                // participant:
                Vector<ActionTypeParticipantConstraint> partConstraints = 
                	partDestroyer.getAllConstraints();
                for (ActionTypeParticipantConstraint partConstraint: 
                	partConstraints) {
                  String typeName = partConstraint.getSimSEObjectType()
                      .getName();

                  // go through all ActionTypeParticipantAttributeConstraints
                  // for this type:
                  ActionTypeParticipantAttributeConstraint[] attConstraints = 
                  	partConstraint.getAllAttributeConstraints();
                  for (ActionTypeParticipantAttributeConstraint attConstraint : 
                  	attConstraints) {
                    String attName = attConstraint.getAttribute().getName();
                    String attGuard = attConstraint.getGuard();
                    if (attConstraint.isConstrained()) {
                      String condVal = attConstraint.getValue().toString();
                      actionsString += partName + "." + attName + " (" + typeName
                              + ") " + attGuard + " ";
                      if (attConstraint.getAttribute().getType() == 
                      	AttributeTypes.STRING) {

                        actionsString += "\\\"" + condVal + "\\\"";
                      } else {

                        actionsString += condVal;
                      }

                      actionsString += " \\n";
                    }
                  }
                }
              }
            }
            actionsString += "\";";
            actionsString += "\n";
          }
        }
      }
      
      TypeSpec destroyerDescriptions = TypeSpec.classBuilder("DestroyerDescription")
    		  .addStaticBlock(CodeBlock.builder()
    				  .add(actionsString)
    				  .build())
    		  .build();
      
      JavaFile javaFile = JavaFile.builder("simse.util", destroyerDescriptions).build();
      try {
		javaFile.writeTo(destDescFile);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
}