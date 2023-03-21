/*
 * This class is responsible for generating all of the code for the ObjectGraph
 * class in the explanatory tool
 */

package simse.codegenerator.explanatorytoolgenerator;

import simse.codegenerator.CodeGeneratorConstants;
import simse.codegenerator.CodeGeneratorUtils;
import simse.modelbuilder.ModelOptions;
import simse.modelbuilder.objectbuilder.Attribute;
import simse.modelbuilder.objectbuilder.AttributeTypes;
import simse.modelbuilder.objectbuilder.DefinedObjectTypes;
import simse.modelbuilder.objectbuilder.NumericalAttribute;
import simse.modelbuilder.objectbuilder.SimSEObjectType;
import simse.modelbuilder.objectbuilder.SimSEObjectTypeTypes;
import simse.modelbuilder.startstatebuilder.CreatedObjects;
import simse.modelbuilder.startstatebuilder.SimSEObject;

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

public class ObjectGraphGenerator implements CodeGeneratorConstants {
  private File directory; // directory to save generated code into
  private DefinedObjectTypes objTypes;
  private CreatedObjects objects;
  private ModelOptions options;

  public ObjectGraphGenerator(DefinedObjectTypes objTypes, CreatedObjects objs,
      File directory, ModelOptions options) {
    this.objTypes = objTypes;
    this.objects = objs;
    this.directory = directory;
    this.options = options;
  }

  public void generate() {
    File objGraphFile = new File(directory,
        ("simse\\explanatorytool\\ObjectGraph.java"));
    if (objGraphFile.exists()) {
      objGraphFile.delete(); // delete old version of file
    }
      
      ClassName arrayList = ClassName.get("java.util", "ArrayList");
      ClassName state = ClassName.get("simse.state", "State");
      ClassName jFreeChart = ClassName.get("org.jfree.chart", "JFreeChart");
      ClassName chartViewer = ClassName.get("org.jfree.chart.fx", "ChartViewer");
      ClassName menuItem = ClassName.get("javafx.scene.control", "MenuItem");
      ClassName separatorMenuItem = ClassName.get("javafx.scene.control", "SeparatorMenuItem");
      ClassName xySeries = ClassName.get("org.jfree.data.xy", "XYSeries");
      ClassName xyDataset = ClassName.get("org.jfree.data.xy", "XYDataset");
      ClassName xyPlot = ClassName.get("org.jfree.chart.plot", "XYPlot");
      ClassName branch = ClassName.get("simse.explanatorytool", "Branch");
      ClassName eventHandlerClass = ClassName.get("javafx.event", "EventHandler");
      ClassName actionEvent = ClassName.get("javafx.event", "ActionEvent");
      ClassName stage = ClassName.get("javafx.stage", "Stage");
      ClassName chartMouseListenerFX = ClassName.get("org.jfree.chart.fx.interaction", "ChartMouseListenerFX");
      ClassName chartMouseEventFX = ClassName.get("org.jfree.chart.fx.interaction", "ChartMouseEventFX");
      ClassName chartFactory = ClassName.get("org.jfree.chart", "ChartFactory");
      ClassName plotOrientation = ClassName.get("org.jfree.chart.plot", "PlotOrientation");
      ClassName rectangleInsets = ClassName.get("org.jfree.chart.ui", "RectangleInsets");
      ClassName xyLineAndShapeRenderer = ClassName.get("org.jfree.chart.renderer.xy", "XYLineAndShapeRenderer");
      ClassName xySeriesCollection = ClassName.get("org.jfree.data.xy", "XYSeriesCollection");
      ClassName numberAxis = ClassName.get("org.jfree.chart.axis", "NumberAxis");
      ClassName range = ClassName.get("org.jfree.data", "Range");
      ClassName chartRenderingInfo = ClassName.get("org.jfree.chart", "ChartRenderingInfo");
      ClassName rectangleEdge = ClassName.get("org.jfree.chart.ui", "RectangleEdge");
      ArrayTypeName stringArray = ArrayTypeName.of(String.class);
      ArrayTypeName xySeriesArray = ArrayTypeName.of(xySeries);

      
      ClassName scene = ClassName.get("javafx.scene", "Scene");
      
      String createDatasetCodeBlock = "";
      // go through all object types and generate code for them:
      Vector<SimSEObjectType> types = objTypes.getAllObjectTypes();
      for (int i = 0; i < types.size(); i++) {
        SimSEObjectType type = types.get(i);
        String uCaseName = 
        	CodeGeneratorUtils.getUpperCaseLeading(type.getName());
        String lCaseName = type.getName().toLowerCase();
        if (i > 0) {
          createDatasetCodeBlock += "else ";
        }
        createDatasetCodeBlock += "if (objTypeType.equals(\""
                + SimSEObjectTypeTypes.getText(type.getType())
                + "\") && objType.equals(\"" + uCaseName + "\")) {";
        createDatasetCodeBlock += "\n";
        createDatasetCodeBlock += uCaseName + " " + lCaseName + " = null;";
        createDatasetCodeBlock += "\n";

        // go through each created object of that type and generate code for it:
        Vector<SimSEObject> objsOfType = objects.getAllObjectsOfType(type);
        for (int j = 0; j < objsOfType.size(); j++) {
          SimSEObject obj = objsOfType.get(j);
          if (j > 0) {
            createDatasetCodeBlock += "else ";
          }
          createDatasetCodeBlock += "if (keyAttVal.equals(\""
                  + obj.getKey().getValue().toString() + "\")) {";
          createDatasetCodeBlock += "\n";
          createDatasetCodeBlock += lCaseName + " = log.get(i).get"
                  + SimSEObjectTypeTypes.getText(type.getType())
                  + "StateRepository().get" + uCaseName + "StateRepository().get(";
          if (obj.getKey().getAttribute().getType() == AttributeTypes.STRING) { 
            createDatasetCodeBlock += "\"" + obj.getKey().getValue().toString() + "\");";
          } else { // non-String attribute
            createDatasetCodeBlock += obj.getKey().getValue().toString() + ");";
          }
          createDatasetCodeBlock += "\n";
          createDatasetCodeBlock += "}";
          createDatasetCodeBlock += "\n";
        }
        createDatasetCodeBlock += "if (" + lCaseName + " != null) {";
        createDatasetCodeBlock += "\n";

        // go through each attribute for this type and generate code for it:
        Vector<Attribute> atts = type.getAllAttributes();
        boolean writeElse = false;
        for (Attribute att : atts) {
          if ((att instanceof NumericalAttribute)
              && (att.isVisible() || att.isVisibleOnCompletion())) {
            if (writeElse) {
              createDatasetCodeBlock += "else ";
            }
            createDatasetCodeBlock += "if (attributes[j].equals(\""
                    + CodeGeneratorUtils.getUpperCaseLeading(att.getName()) + 
                    "\")) {";
            createDatasetCodeBlock += "\n";
            createDatasetCodeBlock += "series[j].add(i, " + lCaseName + ".get"
                    + CodeGeneratorUtils.getUpperCaseLeading(att.getName()) + 
                    "());";
            createDatasetCodeBlock += "\n";
            createDatasetCodeBlock += "}";
            createDatasetCodeBlock += "\n";
            writeElse = true;
          }
        }
        createDatasetCodeBlock += "}";
        createDatasetCodeBlock += "\n";
        createDatasetCodeBlock += "}";
        createDatasetCodeBlock += "\n";
      }
      
      MethodSpec createDataset = MethodSpec.methodBuilder("createDataset")
    		  .addModifiers(Modifier.PRIVATE)
    		  .returns(xyDataset)
    		  .addStatement("series = new $T[attributes.length]", xySeries)
    		  .beginControlFlow("for (int i = 0; i < attributes.length; i++)")
    		  .addStatement("series[i] = new $T(attributes[i])", xySeries)
    		  .endControlFlow()
    		  .beginControlFlow("for (int i = 0; i < log.size(); i++)")
    		  .beginControlFlow("for (int j = 0; j < attributes.length; j++)")
    		  .addCode(createDatasetCodeBlock)
    		  .endControlFlow()
    		  .addStatement("$T dataset = new $T()", xySeriesCollection, xySeriesCollection)
    		  .beginControlFlow("for (int i = 0; i < series.length; i++)")
    		  .addStatement("dataset.addSeries(series[i])")
    		  .endControlFlow()
    		  .addStatement("return dataset")
    		  .build();
      
  	MethodSpec chartMouseMoved = MethodSpec.methodBuilder("chartMouseMoved")
			.addParameter(chartMouseEventFX, "me")
			.build();

      
      MethodSpec createChart = MethodSpec.methodBuilder("createChart")
    		  .addModifiers(Modifier.PRIVATE)
    		  .returns(jFreeChart)
    		  .addParameter(xyDataset, "dataset")
    		  .addStatement("$T chart = $T.createXYLineChart(this.getTitle(), \"Clock Ticks\", null, dataset,\r\n" + 
    		  		"				$T.VERTICAL, true, true, false)", jFreeChart, chartFactory, plotOrientation)
    		  .addStatement("$T plot = ($T) chart.getPlot()", xyPlot, xyPlot)
    		  .addStatement("plot.setBackgroundPaint(java.awt.Color.LIGHT_GRAY)")
    		  .addStatement("plot.setAxisOffset(new $T(5.0, 5.0, 5.0, 5.0))", rectangleInsets)
    		  .addStatement("plot.setDomainGridlinePaint(java.awt.Color.WHITE)")
    		  .addStatement("plot.setRangeGridlinePaint(java.awt.Color.WHITE)")
    		  .addStatement("$T renderer = ($T) plot.getRenderer()", xyLineAndShapeRenderer, xyLineAndShapeRenderer)
    		  .addStatement("renderer.setDefaultShapesVisible(true)")
    		  .addStatement("renderer.setDefaultShapesFilled(true)")
    		  .addStatement("$T domainAxis = ($T) plot.getDomainAxis()", numberAxis, numberAxis)
    		  .addStatement("domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits())")
    		  .addStatement("return chart")
    		  .build();
      
      MethodSpec constructor = MethodSpec.constructorBuilder()
    		  .addModifiers(Modifier.PUBLIC)
    		  .addParameter(String.class, "title")
    		  .addParameter(ParameterizedTypeName.get(arrayList, state), "log")
    		  .addParameter(String.class, "objTypeType")
    		  .addParameter(String.class, "objType")
    		  .addParameter(String.class, "keyAttVal")
    		  .addParameter(stringArray, "attributes")
    		  .addParameter(boolean.class, "showChart")
    		  .addParameter(branch, "branch")
    		  .addStatement("super()")
    		  .addStatement("this.branch = branch")
    		  .beginControlFlow("if(branch.getName() != null)")
    		  .addStatement("title = title.concat(\" - \" + branch.getName())")
    		  .endControlFlow()
    		  .addStatement("setTitle(title)")
    		  .addStatement("this.log = log")
    		  .addStatement("this.objTypeType = objTypeType")
    		  .addStatement("this.objType = objType")
    		  .addStatement("this.keyAttVal = keyAttVal")
    		  .addStatement("this.attributes = attributes")
    		  .addStatement("lastRightClickedX = 0")
    		  .addStatement("$T dataset = $N()", xyDataset, createDataset)
    		  .addStatement("chart = $N(dataset)", createChart)
    		  .addStatement("chartViewer = new $T(chart)", chartViewer)
    		  .addStatement("setChartColors()")
    		  .addStatement("chartViewer.addChartMouseListener(this)")
    		  .addStatement("chartViewer.setPrefSize(500, 270)")
    		  .addStatement("setScene(new $T(chartViewer))", scene)
    		  .addStatement("newBranchItem = new $T(\"Start new game from here\")", menuItem)
    		  .addStatement("newBranchItem.setOnAction(menuEvent)")
    		  .addStatement("separator = new $T()", separatorMenuItem)
    		  .beginControlFlow("if (showChart)")
    		  .addStatement("show()")
    		  .endControlFlow()
    		  .build();

    	String updateCodeBlock = "";
      // go through all object types and generate code for them:
      for (int i = 0; i < types.size(); i++) {
        SimSEObjectType type = types.get(i);
        String uCaseName = 
        	CodeGeneratorUtils.getUpperCaseLeading(type.getName());
        String lCaseName = type.getName().toLowerCase();
        if (i > 0) {
          updateCodeBlock += "else ";
        }
        updateCodeBlock += "if (objTypeType.equals(\""
                + SimSEObjectTypeTypes.getText(type.getType())
                + "\") && objType.equals(\"" + uCaseName + "\")) {";
        updateCodeBlock += "\n";
        updateCodeBlock += uCaseName + " " + lCaseName + " = null;";
        updateCodeBlock += "\n";

        // go through each created object of that type and generate code for it:
        Vector<SimSEObject> objsOfType = objects.getAllObjectsOfType(type);
        for (int j = 0; j < objsOfType.size(); j++) {
          SimSEObject obj = objsOfType.get(j);
          if (j > 0) {
            updateCodeBlock += "else ";
          }
          updateCodeBlock += "if (keyAttVal.equals(\""
                  + obj.getKey().getValue().toString() + "\")) {";
          updateCodeBlock += "\n";
          updateCodeBlock += lCaseName + " = log.get(log.size() - 1).get"
                  + SimSEObjectTypeTypes.getText(type.getType())
                  + "StateRepository().get" + uCaseName + "StateRepository().get(";
          if (obj.getKey().getAttribute().getType() == AttributeTypes.STRING) {
            updateCodeBlock += "\"" + obj.getKey().getValue().toString() + "\");";
          } else { // non-String attribute
            updateCodeBlock += obj.getKey().getValue().toString() + ");";
          }
          updateCodeBlock += "\n";
          updateCodeBlock += "}";
          updateCodeBlock += "\n";
        }
        updateCodeBlock += "if (" + lCaseName + " != null) {";
        updateCodeBlock += "\n";

        // go through each attribute for this type and generate code for it:
        Vector<Attribute> atts = type.getAllAttributes();
        boolean writeElse = false;
        for (Attribute att : atts) {
          if ((att instanceof NumericalAttribute)
              && (att.isVisible() || att.isVisibleOnCompletion())) {
            if (writeElse) {
              updateCodeBlock += "else ";
            }
            updateCodeBlock += "if (attributes[j].equals(\""
                    + CodeGeneratorUtils.getUpperCaseLeading(att.getName()) + 
                    "\")) {";
            updateCodeBlock += "\n";
            updateCodeBlock += "series[j].add(log.size(), " + lCaseName + ".get"
                    + CodeGeneratorUtils.getUpperCaseLeading(att.getName()) + 
                    "());";
            updateCodeBlock += "\n";
            updateCodeBlock += "}";
            updateCodeBlock += "\n";
            writeElse = true;
          }
        }
        updateCodeBlock += "}";
        updateCodeBlock += "\n";
        updateCodeBlock += "}";
        updateCodeBlock += "\n";
      }
      
      MethodSpec update = MethodSpec.methodBuilder("update")
    		  .addModifiers(Modifier.PUBLIC)
    		  .beginControlFlow("if ((log.size() > 0) && (log.get(log.size() - 1) != null))")
    		  .beginControlFlow("for (int j = 0; j < attributes.length; j++)")
    		  .addCode(updateCodeBlock)
    		  .endControlFlow()
    		  .endControlFlow()
    		  .build();
    	
    	CodeBlock chartMouseClickedBlock = null;
    	
    	if (options.getAllowBranchingOption()) {
    		
    		chartMouseClickedBlock = CodeBlock.builder()
    				.beginControlFlow("if (event.getButton() != MouseEvent.PRIMARY)")
    				.addStatement("$T plot = chart.getXYPlot()", xyPlot)
    				.addStatement("$T domainRange = plot.getDataRange(plot.getDomainAxis())", range)
    				.beginControlFlow("if (domainRange != null)")
    				.addStatement("javafx.geometry.Point2D pt = chartViewer.localToScreen(event.getScreenX(), event.getScreenY())")
    				.addStatement("$T info = this.chartViewer.getRenderingInfo()", chartRenderingInfo)
    				.addStatement("java.awt.geom.Rectangle2D dataArea = info.getPlotInfo().getDataArea()")
    				.addStatement("$T domainAxis = ($T) plot.getDomainAxis()", numberAxis, numberAxis)
    				.addStatement("$T domainAxisEdge = plot.getDomainAxisEdge()", rectangleEdge)
    				.addStatement("double chartX = domainAxis.java2DToValue(pt.getX(), dataArea, domainAxisEdge)")
    				.addStatement("lastRightClickedX = ($T) Math.rint(chartX)", int.class)
    				.beginControlFlow("if (domainRange != null && lastRightClickedX >= domainRange.getLowerBound() && lastRightClickedX <= (domainRange.getUpperBound() - 1) && lastRightClickedX >= 0)")
    				.beginControlFlow("if (chartViewer.getContextMenu().getItems().indexOf(newBranchItem) == -1)")
    				.addStatement("chartViewer.getContextMenu().getItems().add(separator)")
    				.addStatement("chartViewer.getContextMenu().getItems().add(newBranchItem)")
    				.endControlFlow()
    				.endControlFlow()
    				.beginControlFlow("else ")
    				.beginControlFlow("if (chartViewer.getContextMenu().getItems().indexOf(newBranchItem) >= 0)")
    				.addStatement("chartViewer.getContextMenu().getItems().remove(newBranchItem)")
    				.beginControlFlow("if (chartViewer.getContextMenu().getItems().indexOf(separator) >= 0)")
    				.addStatement("chartViewer.getContextMenu().getItems().remove(separator)")
    				.endControlFlow()
    				.endControlFlow()
    				.endControlFlow()
    				.endControlFlow()
    				.endControlFlow()
    				.build();
    	} 
    	else {
    		chartMouseClickedBlock = CodeBlock.builder().build();
    	}
    	
    	ClassName mouseEvent = ClassName.get("javafx.scene.input", "MouseEvent");
    	
    	MethodSpec chartMouseClicked = MethodSpec.methodBuilder("chartMouseClicked")
    			.addModifiers(Modifier.PUBLIC)
    			.addStatement("$T event = me.getTrigger()", mouseEvent)
    			.addCode(chartMouseClickedBlock)
    			.build();
    	
    	ClassName object = ClassName.get("java.lang", "Object");
    	ClassName textInputDialog = ClassName.get("javafx.scene.control", "TextInputDialog");
    	ClassName optional = ClassName.get("java.util", "Optional");
    	ClassName clock = ClassName.get("simse.state", "Clock");
    	ClassName logger = ClassName.get("simse.state.logger", "Logger");
    	ClassName simse = ClassName.get("simse", "SimSE");
    	ClassName javaFXHelpers = ClassName.get("simse.gui.util", "JavaFXHelpers");
    	ClassName color = ClassName.get("javafx.scene.paint", "Color");

		TypeSpec anonHandleClass = TypeSpec.anonymousClassBuilder("", eventHandlerClass, actionEvent)
        		.addSuperinterface(ParameterizedTypeName.get(eventHandlerClass, actionEvent))  
				.addField(String.class, "newBranchName", Modifier.PRIVATE)
                  .addMethod(MethodSpec.methodBuilder("handle")
                		  .addParameter(actionEvent, "event")
                		  .addStatement("$T source = event.getSource()", object)
                		  .beginControlFlow("if (source == newBranchItem)")
                		  .addStatement("$T td = new $T()", textInputDialog, textInputDialog)
                		  .addStatement("td.setTitle(\"Name New Branch\")")
                		  .addStatement("td.setContentText(\"Please name this new game:\")")
                		  .addStatement("td.setHeaderText(null)")
                		  .addStatement("$T<$T> result = td.showAndWait()", optional, String.class)
                		  .addStatement("result.ifPresent(name -> { this.newBranchName = name;});")
                		  .endControlFlow()
                		  .beginControlFlow("if (newBranchName != null)")
                		  .addStatement("$T tempState = ($T) log.get(lastRightClickedX).clone()", state, state)
                		  .addStatement("$T tempLogger = new $T(tempState, new $T<$T>(log.subList(0, lastRightClickedX)))", logger, logger, arrayList, state)
                		  .addStatement("$T tempClock = new $T(tempLogger, lastRightClickedX)", clock, clock)
                		  .addStatement("tempState.setClock(tempClock)")
                		  .addStatement("tempState.setLogger(tempLogger)")
                		  .addStatement("$T.startNewBranch(tempState, new $T(newBranchName, lastRightClickedX, tempClock.getTime(), branch, null))", simse, branch)
                		  .endControlFlow()
                		  .build())
                  .build();

    		            
    	
      
      MethodSpec getXYPlot = MethodSpec.methodBuilder("getXYPlot")
    		  .addModifiers(Modifier.PUBLIC)
    		  .returns(xyPlot)
    		  .addStatement("return chart.getXYPlot()")
    		  .build();

      MethodSpec getChart = MethodSpec.methodBuilder("getChart")
    		  .addModifiers(Modifier.PUBLIC)
    		  .returns(jFreeChart)
    		  .addStatement("return chart")
    		  .build();
      
      MethodSpec getChartTitle = MethodSpec.methodBuilder("getChartTitle")
    		  .addModifiers(Modifier.PUBLIC)
    		  .returns(String.class)
    		  .addStatement("return this.getTitle()")
    		  .build();
      
      MethodSpec getLog = MethodSpec.methodBuilder("getLog")
    		  .addModifiers(Modifier.PUBLIC)
    		  .returns(ParameterizedTypeName.get(arrayList, state))
    		  .addStatement("return log")
    		  .build();
      
      MethodSpec setChartColors = MethodSpec.methodBuilder("setChartColors")
    		  .addModifiers(Modifier.PRIVATE)
    		  .addStatement("chartViewer.backgroundProperty().set($T.createBackgroundColor($T.WHITE))", javaFXHelpers, color)
    		  .build();
      
      
      
      
      TypeSpec objectGraph = TypeSpec.classBuilder("ObjectGraph")
    		  .addModifiers(Modifier.PUBLIC)
    		  .superclass(stage)
    		  .addSuperinterface(chartMouseListenerFX)
			  .addField(ParameterizedTypeName.get(arrayList, state), "log", Modifier.PRIVATE)
			  .addField(String.class, "objTypeType", Modifier.PRIVATE)
			  .addField(String.class, "objType", Modifier.PRIVATE)
			  .addField(String.class, "keyAttVal", Modifier.PRIVATE)
			  .addField(stringArray, "attributes", Modifier.PRIVATE)
			  .addField(jFreeChart, "chart", Modifier.PRIVATE)
			  .addField(chartViewer, "chartViewer", Modifier.PRIVATE)
			  .addField(menuItem, "newBranchItem", Modifier.PRIVATE)
			  .addField(separatorMenuItem, "separator", Modifier.PRIVATE)
			  .addField(int.class, "lastRightClickedX", Modifier.PRIVATE)
			  .addField(xySeriesArray, "series", Modifier.PRIVATE)
			  .addField(branch, "branch", Modifier.PRIVATE)
			  .addMethod(constructor)
			  .addMethod(createDataset)
			  .addMethod(setChartColors)
			  .addMethod(createChart)
			  .addMethod(update)
			  .addMethod(getXYPlot)
			  .addMethod(getChart)
			  .addMethod(getChartTitle)
			  .addMethod(getLog)
			  .addMethod(chartMouseClicked)
			  .addMethod(chartMouseMoved)
				.addField(FieldSpec.builder(ParameterizedTypeName.get(eventHandlerClass, actionEvent), "menuEvent", Modifier.PRIVATE)
						.initializer("$L", anonHandleClass).build())
    		  .build();
      


      
      JavaFile javaFile = JavaFile.builder("simse.explanatorytool", objectGraph)
  		    .build();

    try {
    	FileWriter writer = new FileWriter(objGraphFile);
		javaFile.writeTo(writer);
		
		writer.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
      
  }
}