/*
 * This class is responsible for generating all of the code for the 
 * ActionInfoWindow class in the explanatory tool
 */
package simse.codegenerator.explanatorytoolgenerator;
import simse.codegenerator.CodeGeneratorConstants;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ActionInfoWindowGenerator implements CodeGeneratorConstants {
    private File dir;
    public ActionInfoWindowGenerator(File dir) {
        this.dir = dir;
    }
    public void generate() {
        // TODO Auto-generated method stub
        File actionInfoWindowFile = new File(dir,
                ("simse\\explanatorytool\\ActionInfoWindow.java"));
            if (actionInfoWindowFile.exists()) {
                actionInfoWindowFile.delete(); // delete old version of file
            }
        try {
        FileReader reader = new FileReader("res\\static\\explanatorytool\\ActionInfoWindow.txt");
        FileWriter writer = new FileWriter(actionInfoWindowFile);
        String fileContents = "";
        int index;
        while ((index = reader.read()) != -1) {
            fileContents += (char)index;
        }
        writer.write(fileContents);
        reader.close();
        writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 }