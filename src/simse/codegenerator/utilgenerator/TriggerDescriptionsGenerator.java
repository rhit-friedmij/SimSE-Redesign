/*
 * This class is responsible for generating all of the code for the
 * TriggerDescriptions class in the explanatory tool
 */

package simse.codegenerator.utilgenerator;

import simse.codegenerator.CodeGeneratorConstants;
import simse.modelbuilder.actionbuilder.ActionType;
import simse.modelbuilder.actionbuilder.ActionTypeParticipantAttributeConstraint;
import simse.modelbuilder.actionbuilder.ActionTypeParticipantConstraint;
import simse.modelbuilder.actionbuilder.ActionTypeParticipantTrigger;
import simse.modelbuilder.actionbuilder.ActionTypeTrigger;
import simse.modelbuilder.actionbuilder.DefinedActionTypes;
import simse.modelbuilder.actionbuilder.RandomActionTypeTrigger;
import simse.modelbuilder.actionbuilder.UserActionTypeTrigger;
import simse.modelbuilder.objectbuilder.AttributeTypes;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JOptionPane;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

public class TriggerDescriptionsGenerator implements CodeGeneratorConstants {
  private File directory; // directory to save generated code into
  private DefinedActionTypes actTypes;

  public TriggerDescriptionsGenerator(DefinedActionTypes actTypes, 
  		File directory) {
    this.actTypes = actTypes;
    this.directory = directory;
  }

  public void generate() {
    File trigDescFile = new File(directory,
        ("simse\\util\\TriggerDescriptions.java"));
    if (trigDescFile.exists()) {
      trigDescFile.delete(); // delete old version of file
    }

      String actionsString = "";
      
      // go through all actions:
      Vector<ActionType> actions = actTypes.getAllActionTypes();
      for (ActionType act : actions) {
        if (act.isVisibleInExplanatoryTool()) {
          Vector<ActionTypeTrigger> triggers = act.getAllTriggers();
          for (ActionTypeTrigger trigger : triggers) {
//            writer.write("static final String " + act.getName().toUpperCase()
//                + "_" + trigger.getName().toUpperCase() + " = ");
            actionsString += "static final String " + act.getName().toUpperCase()
                    + "_" + trigger.getName().toUpperCase() + " = ";
            
//            writer.write(NEWLINE);
            actionsString += "\n";
//            writer.write("\"This action occurs ");
            actionsString += "\"This action occurs ";
            if (trigger instanceof RandomActionTypeTrigger) {
//              writer.write(((RandomActionTypeTrigger) trigger).getFrequency()
//                  + "% of the time ");
              actionsString += ((RandomActionTypeTrigger) trigger).getFrequency()
                      + "% of the time ";
            } else if (trigger instanceof UserActionTypeTrigger) {
              actionsString += "when the user chooses the menu item \\\""
                      + ((UserActionTypeTrigger) trigger).getMenuText()
                      + "\\\" and ";
            }
//            writer.write("when the following conditions are met: \\n");
            actionsString += "when the following conditions are met: \\n";
            // go through all participant conditions:
            Vector<ActionTypeParticipantTrigger> partTriggers = 
            	trigger.getAllParticipantTriggers();
            for (int k = 0; k < partTriggers.size(); k++) {
              ActionTypeParticipantTrigger partTrigger = partTriggers.get(k);
              String partName = partTrigger.getParticipant().getName();

              // go through all ActionTypeParticipantConstraints for this
              // participant:
              Vector<ActionTypeParticipantConstraint> partConstraints = 
              	partTrigger.getAllConstraints();
              for (int m = 0; m < partConstraints.size(); m++) {
                ActionTypeParticipantConstraint partConstraint = 
                	partConstraints.get(m);
                String typeName = partConstraint.getSimSEObjectType().getName();

                // go through all ActionTypeParticipantAttributeConstraints for
                // this type:
                ActionTypeParticipantAttributeConstraint[] attConstraints = 
                	partConstraint.getAllAttributeConstraints();
                for (int n = 0; n < attConstraints.length; n++) {
                  ActionTypeParticipantAttributeConstraint attConstraint = 
                  	attConstraints[n];
                  String attName = attConstraint.getAttribute().getName();
                  String attGuard = attConstraint.getGuard();
                  if (attConstraint.isConstrained()) {
                    String condVal = attConstraint.getValue().toString();
//                    writer.write(partName + "." + attName + " (" + typeName
//                        + ") " + attGuard + " ");
                    actionsString += partName + "." + attName + " (" + typeName
                            + ") " + attGuard + " ";
                    if (attConstraint.getAttribute().getType() == 
                    	AttributeTypes.STRING) {
//                      writer.write("\\\"" + condVal + "\\\"");
                      actionsString += "\\\"" + condVal + "\\\"";
                    } else {
//                      writer.write(condVal);
                      actionsString += condVal;
                    }
//                    writer.write(" \\n");
                    actionsString += " \\n";
                  }
                }
              }
            }
//            writer.write("\";");
            actionsString += "\";";
//            writer.write(NEWLINE);
            actionsString += "\n";
          }
        }
      }
      
      TypeSpec triggerDescriptions = TypeSpec.classBuilder("TriggerDescriptions")
    		  .addStaticBlock(CodeBlock.builder()
    				  .add(actionsString)
    				  .build())
    		  .build();
      
      JavaFile javaFile = JavaFile.builder("simse.util", triggerDescriptions).build();
      
      try {
    	FileWriter writer = new FileWriter(trigDescFile);
		javaFile.writeTo(writer);
		
		writer.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
}