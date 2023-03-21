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
import java.util.ArrayList;
import java.util.Vector;

import javax.lang.model.element.Modifier;
import javax.swing.JOptionPane;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
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

    	ArrayList<FieldSpec> actionFields = new ArrayList<>();
    
      
      
      // go through all actions:
      Vector<ActionType> actions = actTypes.getAllActionTypes();
      for (ActionType act : actions) {
    	  String actionsString = "";
        if (act.isVisibleInExplanatoryTool()) {
          Vector<ActionTypeTrigger> triggers = act.getAllTriggers();
          for (ActionTypeTrigger trigger : triggers) {
//            writer.write("static final String " + act.getName().toUpperCase()
//                + "_" + trigger.getName().toUpperCase() + " = ");
            String initalization = "";
            
//            writer.write(NEWLINE);
            initalization += "\n";
//            writer.write("\"This action occurs ");
            initalization += "\"This action occurs ";
            if (trigger instanceof RandomActionTypeTrigger) {
//              writer.write(((RandomActionTypeTrigger) trigger).getFrequency()
//                  + "% of the time ");
            	initalization += ((RandomActionTypeTrigger) trigger).getFrequency()
                      + "% of the time ";
            } else if (trigger instanceof UserActionTypeTrigger) {
            	initalization += "when the user chooses the menu item \\\""
                      + ((UserActionTypeTrigger) trigger).getMenuText()
                      + "\\\" and ";
            }
//            writer.write("when the following conditions are met: \\n");
            initalization += "when the following conditions are met:";
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
                    initalization += partName + "." + attName + " (" + typeName
                            + ") " + attGuard + " ";
                    if (attConstraint.getAttribute().getType() == 
                    	AttributeTypes.STRING) {
//                      writer.write("\\\"" + condVal + "\\\"");
                    	initalization += "\\\"" + condVal + "\\\"";
                    } else {
//                      writer.write(condVal);
                    	initalization += condVal;
                    }
//                    writer.write(" \\n");
                    initalization += " \"";
                  }
                }
              }
            }
            
            actionFields.add(FieldSpec.builder(String.class, 
            		act.getName().toUpperCase()
                    + "_" + trigger.getName().toUpperCase(), Modifier.STATIC, Modifier.PUBLIC, Modifier.FINAL).initializer(initalization).build());
          }
        }
      }
      
      TypeSpec triggerDescriptions = TypeSpec.classBuilder("TriggerDescriptions")
    		  .addModifiers(Modifier.PUBLIC)
    		  .addFields(actionFields)
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