/*
 * This class is responsible for generating all of the code for the ActionGraph
 * class in the explanatory tool
 */

package simse.codegenerator.explanatorytoolgenerator;

import simse.codegenerator.CodeGeneratorConstants;
import simse.codegenerator.CodeGeneratorUtils;
import simse.modelbuilder.actionbuilder.ActionType;
import simse.modelbuilder.actionbuilder.DefinedActionTypes;
import simse.modelbuilder.ModelOptions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

public class ActionGraphGenerator implements CodeGeneratorConstants {
  private File directory; // directory to save generated code into
  private DefinedActionTypes actTypes;
  private ModelOptions options;

  public ActionGraphGenerator(DefinedActionTypes actTypes, File directory, 
  		ModelOptions options) {
    this.actTypes = actTypes;
    this.directory = directory;
    this.options = options;
  }

  public void generate() {
    File actGraphFile = new File(directory,
        ("simse\\explanatorytool\\ActionGraph.java"));
    if (actGraphFile.exists()) {
      actGraphFile.delete(); // delete old version of file
    }
      
      ClassName stage = ClassName.get("javafx.stage", "Stage");
      ClassName state = ClassName.get("simse.state", "State");
      ClassName stringClass = ClassName.get("java.lang", "String");
      ClassName chartMouseListener = ClassName.get("org.jfree.chart.fx.interaction", "ChartMouseListenerFX");
      ClassName arrayList = ClassName.get("java.util", "ArrayList");
      ClassName jFreeChart = ClassName.get("org.jfree.chart", "JFreeChart");
      ClassName chartViewer = ClassName.get("org.jfree.chart.fx", "ChartViewer");
      ClassName menuItem = ClassName.get("javafx.scene.control", "MenuItem");
      ClassName separatorMenuItem = ClassName.get("javafx.scene.control", "SeparatorMenuItem");
      ClassName xySeriesCollection = ClassName.get("org.jfree.data.xy", "XYSeriesCollection");
      ClassName branch = ClassName.get("simse.explanatorytool", "Branch");
      ClassName xySeries = ClassName.get("org.jfree.data.xy", "XYSeries");
      ClassName xyDataset = ClassName.get("org.jfree.data.xy", "XYDataset");
      ClassName xyPlot = ClassName.get("org.jfree.chart.plot", "XYPlot");
      ClassName chartMouseEventFX = ClassName.get("org.jfree.chart.fx.interaction", "ChartMouseEventFX");
      ClassName hashTable = ClassName.get("java.util", "Hashtable");
      ClassName integer = ClassName.get("java.lang", "Integer");
      ClassName action = ClassName.get("simse.adts.actions", "Action");
      ClassName scene = ClassName.get("javafx.scene", "Scene");
      ClassName colorAWT = ClassName.get("java.awt", "Color");
      ClassName rectangleInsets = ClassName.get("org.jfree.chart.ui", "RectangleInsets");
      ClassName valueAxis = ClassName.get("org.jfree.chart.axis", "ValueAxis");
      ClassName numberAxis = ClassName.get("org.jfree.chart.axis", "NumberAxis");
      ClassName xyLineAndShapeRenderer = ClassName.get("org.jfree.chart.renderer.xy", "XYLineAndShapeRenderer");
      ClassName chartFactory = ClassName.get("org.jfree.chart", "ChartFactory");
      ClassName plotOrientation = ClassName.get("org.jfree.chart.plot", "PlotOrientation");
      ClassName javaFXHelpers = ClassName.get("simse.gui.util", "JavaFXHelpers");
      ClassName color = ClassName.get("javafx.scene.paint", "Color");
  	  ClassName mouseEvent = ClassName.get("javafx.scene.input", "MouseEvent");
  	  ClassName mouseButton = ClassName.get("javafx.scene.input", "MouseButton");
  	  ClassName chartEntity = ClassName.get("org.jfree.chart.entity", "ChartEntity");
  	  ClassName xyItemEntity = ClassName.get("org.jfree.chart.entity", "XYItemEntity");
  	  ClassName eventHandlerClass = ClassName.get("javafx.event", "EventHandler");
  	  ClassName actionEvent = ClassName.get("javafx.event", "ActionEvent");
  	  ClassName actionInfoWindow = ClassName.get("simse.explanatorytool", "ActionInfoWindow");
  	  ClassName abstractXYItemLabelGenerator = ClassName.get("org.jfree.chart.labels", "AbstractXYItemLabelGenerator");
  	  ClassName xyToolTipGenerator = ClassName.get("org.jfree.chart.labels", "XYToolTipGenerator");
  	  ClassName clock = ClassName.get("simse.state", "Clock");
  	  ClassName textInputDialog = ClassName.get("javafx.scene.control", "TextInputDialog");
  	  ClassName optional = ClassName.get("java.util", "Optional");  
  	  ClassName object = ClassName.get("java.lang", "Object");
  	  ClassName logger = ClassName.get("simse.state.logger", "Logger");
  	  ClassName simse = ClassName.get("simse", "SimSE");
  	  ArrayTypeName stringArray = ArrayTypeName.of(String.class);
      
     
      
      MethodSpec toolTipConstructor = MethodSpec.constructorBuilder()
    		  .addModifiers(Modifier.PUBLIC)
    		  .addStatement("super()")
    		  .build();
      
      MethodSpec generateToolTip = MethodSpec.methodBuilder("generateToolTip")
    		  .returns(String.class)
    		  .addModifiers(Modifier.PUBLIC)
    		  .addParameter(xyDataset, "dataset")
    		  .addParameter(int.class, "series")
    		  .addParameter(int.class, "item")
    		  .addStatement("return new String(dataset.getSeriesKey(series) + \": click for Action info\")")
    		  .build();
      
      TypeSpec actionGraphToolTipGenerator = TypeSpec.classBuilder("ActionGraphToolTipGenerator")
    		  .superclass(abstractXYItemLabelGenerator)
    		  .addSuperinterface(xyToolTipGenerator)
    		  .addField(FieldSpec.builder(long.class, "serialVersionUID")
    				  .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
    				  .initializer("1L").build())
    		  .addMethod(toolTipConstructor)
    		  .addMethod(generateToolTip)
    		  .build();
      

      
      ArrayList<FieldSpec> actionFields = new ArrayList<>();
      
      // generate an index and an indices array list for each type of action:
      Vector<ActionType> actions = actTypes.getAllActionTypes();
      for (int i = 0; i < actions.size(); i++) {
        ActionType act = actions.get(i);
        if (act.isVisibleInExplanatoryTool()) {
        	
          String lCaseName = act.getName().toLowerCase();
          actionFields.add(FieldSpec.builder(int.class, lCaseName + "Index", Modifier.PRIVATE).initializer("1; // index to be used for labeling multiple actions of the same type\\").build());
          actionFields.add(FieldSpec.builder(ParameterizedTypeName.get(arrayList, integer), lCaseName + "Indices", Modifier.PRIVATE).initializer("new ArrayList<Integer>(); // an ArrayList to map indices for "
                  + act.getName() + " Action labels to action ids\n").build());
          
          
          
        }
      }
      
      String dummyEntriesBlock = "";
      
      for (int i = 0; i < actions.size(); i++) {
        ActionType act = actions.get(i);
        if (act.isVisibleInExplanatoryTool()) {
          dummyEntriesBlock += act.getName().toLowerCase()
                  + "Indices.add(0, -1);\n";
        }
      }
      
      String actionBlock = "";
      
      // go through each action and generate code for it:
      boolean writeElse = false;
      for (int i = 0; i < actions.size(); i++) {
        ActionType act = actions.get(i);
        String uCaseName = 
        	CodeGeneratorUtils.getUpperCaseLeading(act.getName());
        String lCaseName = act.getName().toLowerCase();
        if (act.isVisibleInExplanatoryTool()) {
          if (writeElse) {
            actionBlock += "else ";
          } else {
            writeElse = true;
          }
          actionBlock += "if (actionNames[i].equals(\"" + uCaseName + "\")) {\n";
          actionBlock += "// go through the " + uCaseName
                  + "ActionStateRepository for each clock tick:\n";
          actionBlock += "for (int j = 0; j < log.size(); j++) {\n";
          actionBlock += "State state = log.get(j);\n";
          actionBlock += "Vector<" + uCaseName + "Action> " + lCaseName
                  + "Actions = state.getActionStateRepository().get" + uCaseName
                  + "ActionStateRepository().getAllActions();\n\n";
          actionBlock += "// go through each " + uCaseName + "Action:\n";
          actionBlock += "for (int k = 0; k < " + lCaseName
                  + "Actions.size(); k++) {\n";
          actionBlock += uCaseName + "Action action = " + lCaseName + "Actions.get(k);\n\n";
          actionBlock += "// update series:\n";
          actionBlock += "updateSeries(action, j);\n";
          actionBlock += "}\n";
          actionBlock += "}\n";
          actionBlock += "}\n";
        }
      }
      

      
      MethodSpec setChartColors = MethodSpec.methodBuilder("setChartColors")
    		  .addModifiers(Modifier.PRIVATE)
    		  .addStatement("chartViewer.backgroundProperty().set($T.createBackgroundColor($T.WHITE))", javaFXHelpers, color)
    		  .build();
      

      
      MethodSpec createChart = MethodSpec.methodBuilder("createChart")
    		  .addModifiers(Modifier.PRIVATE)
    		  .returns(jFreeChart)
    		  .addParameter(xyDataset, "dataset")
    		  .addStatement("// create the chart")
    		  .addStatement("$T chart = $T.createXYLineChart(\"Action Graph\", \"Clock Ticks\", null, dataset, $T.VERTICAL, true, true, false)", jFreeChart, chartFactory, plotOrientation)
    		  .addStatement("$T plot = ($T) chart.getPlot()", xyPlot, xyPlot)
    		  .addStatement("plot.getRenderer().setDefaultToolTipGenerator(new ActionGraphToolTipGenerator())")
    		  .addStatement("plot.setBackgroundPaint(new $T(0xFF, 0xFF, 0xCC))", colorAWT)
    		  .addStatement("plot.setAxisOffset(new $T(5.0, 5.0, 5.0, 5.0))", rectangleInsets)
    		  .addStatement("plot.setDomainGridlinePaint($T.BLACK)", colorAWT)
    		  .addStatement("plot.setRangeGridlinePaint($T.BLACK)", colorAWT)
    		  .addStatement("$T rangeAxis = plot.getRangeAxis()", valueAxis)
    		  .addStatement("rangeAxis.setTickLabelsVisible(false)")
    		  .addStatement("rangeAxis.setTickMarksVisible(false)")
    		  .addStatement("rangeAxis.setAxisLineVisible(false)")
    		  .addStatement("rangeAxis.setStandardTickUnits($T.createIntegerTickUnits())", numberAxis)
    		  .addStatement("plot.getDomainAxis().setStandardTickUnits($T.createIntegerTickUnits())", numberAxis)
    		  .addStatement("$T renderer = ($T) plot.getRenderer()", xyLineAndShapeRenderer, xyLineAndShapeRenderer)
    		  .addStatement("renderer.setDefaultShapesVisible(true)")
    		  .addStatement("renderer.setDefaultShapesFilled(true)")
    		  .addStatement("// change the auto tick unit selection to integer units only")
    		  .addStatement("$T domainAxis = ($T) plot.getDomainAxis()", numberAxis, numberAxis)
    		  .addStatement("domainAxis.setStandardTickUnits($T.createIntegerTickUnits())", numberAxis)
    		  .addStatement("return chart")
    		  .build();
      
      
      
      MethodSpec createDataset = MethodSpec.methodBuilder("createDataset")
    		  .returns(xyDataset)
    		  .addModifiers(Modifier.PRIVATE)
    		  .addStatement("// add a dummy entry for index 0")
    		  .addStatement("indices.add(0, \"Action\")")
    		  .addStatement("// go through each action")
    		  .beginControlFlow("for (int i = 0; i < actionNames.length; i++)")
    		  .addCode(actionBlock)
    		  .endControlFlow()
    		  .addStatement("return dataset")
    		  .build();
      
      
      
      MethodSpec constructor = MethodSpec.constructorBuilder()
    		  .addModifiers(Modifier.PUBLIC)
    		  .addParameter(ParameterizedTypeName.get(arrayList, state), "log")
    		  .addParameter(stringArray, "actionNames")
    		  .addParameter(boolean.class, "showChart")
    		  .addParameter(branch, "branch")
    		  .addStatement("super()")
    		  .addStatement("$T title = \"Action Graph\"", String.class)
    		  .beginControlFlow("if (branch.getName() != null)")
    		  .addStatement("title = title.concat(\" - \" + branch.getName())")
    		  .endControlFlow()
    		  .addStatement("setTitle(title)")
    		  .addStatement("this.log = log")
    		  .addStatement("this.actionNames = actionNames")
    		  .addStatement("lastRightClickedX = 0")
    		  .addCode(dummyEntriesBlock)
    		  .addStatement("dataset = new $T()", xySeriesCollection)
    		  .addStatement("$T xydataset = $N()", xyDataset, createDataset)
    		  .addStatement("chart = $N(xydataset)", createChart)
    		  .addStatement("chartViewer = new $T(chart, true)", chartViewer)
    		  .addStatement("$N()", setChartColors)
    		  .addStatement("chartViewer.addChartMouseListener(this)")
    		  .addStatement("chartViewer.setPrefSize(500, 270)")
    		  .addStatement("setScene(new $T(chartViewer))", scene)
    		  .addStatement("newBranchItem = new $T(\"Start new branch from here\")", menuItem)
    		  .addStatement("newBranchItem.setOnAction(menuEvent)")
    		  .addStatement("separator = new $T()", separatorMenuItem)
    		  .beginControlFlow("if (showChart)")
    		  .addStatement("show()")
    		  .endControlFlow()
    		  .build();
      
      String ifStatementActionBlock = "";

      // go through each action and generate code for it:
      boolean writeElse2 = false;
      for (int i = 0; i < actions.size(); i++) {
        ActionType act = actions.get(i);
        String uCaseName = 
        	CodeGeneratorUtils.getUpperCaseLeading(act.getName());
        String lCaseName = act.getName().toLowerCase();
        if (act.isVisibleInExplanatoryTool()) {
          if (writeElse2) {
            ifStatementActionBlock += "else ";
          } else {
            writeElse2 = true;
          }
          ifStatementActionBlock += "if (action instanceof " + uCaseName + "Action) {\n";
          ifStatementActionBlock += "newSeriesName = \"" + uCaseName + "Action-\" + "
                  + lCaseName + "Index;\n";
          ifStatementActionBlock += "newSeries = new XYSeries(newSeriesName);\n";
          ifStatementActionBlock += lCaseName + "Indices.add(" + lCaseName
                  + "Index, new Integer(action.getId()));\n";
          ifStatementActionBlock += lCaseName + "Index++;\n";
          ifStatementActionBlock += "}\n";
        }
      }

      String elseStatementCodeBlock = "";
      
      // go through each action and generate code for it:
      boolean writeElse3 = false;
      for (int i = 0; i < actions.size(); i++) {
        ActionType act = actions.get(i);
        String uCaseName = 
        	CodeGeneratorUtils.getUpperCaseLeading(act.getName());
        String lCaseName = act.getName().toLowerCase();
        if (act.isVisibleInExplanatoryTool()) {
          if (writeElse3) {
            elseStatementCodeBlock += "else ";
          } else {
            writeElse3 = true;
          }
          elseStatementCodeBlock += "if (action instanceof " + uCaseName + "Action) {\n";
          elseStatementCodeBlock += "index = " + lCaseName
                  + "Indices.indexOf(new Integer(action.getId()));\n\n";
          elseStatementCodeBlock += "// add the data value to the series:\n"; 
          elseStatementCodeBlock += "oldSeries.add(clockTick, indices.indexOf(\""
                  + uCaseName + "Action-\" + index));\n";
          elseStatementCodeBlock += "}\n";
        }
      }

      
      MethodSpec updateSeries = MethodSpec.methodBuilder("updateSeries")
    		  .addModifiers(Modifier.PRIVATE)
    		  .addParameter(action, "action")
    		  .addParameter(int.class, "clockTick")
    		  .beginControlFlow("if (!series.containsKey(action.getId()))")
    		  .addStatement("$T newSeries = null", xySeries)
    		  .addStatement("$T newSeriesName = \"\"", String.class)
    		  .addCode(ifStatementActionBlock)
    		  .addStatement("// add the data value to the series")
    		  .addStatement("newSeries.add(clockTick, actionIndex)")
    		  .addStatement("// add the series to the Hashtable")
    		  .addStatement("series.put(action.getId(), newSeries)")
    		  .addStatement("// add the index entry to the ArrayList")
    		  .addStatement("indices.add(actionIndex, newSeriesName)")
    		  .addStatement("dataset.addSeries(newSeries)")
    		  .addStatement("// update the index for the next new action")
    		  .addStatement("actionIndex++")
    		  .endControlFlow()
    		  .beginControlFlow("else ")
    		  .addStatement("$T oldSeries = series.get(action.getId())", xySeries)
    		  .addStatement("int index = 0")
    		  .addCode(elseStatementCodeBlock)
    		  .endControlFlow()
    		  .build();
      
      MethodSpec getIdOfActionWithSeriesName = MethodSpec.methodBuilder("getIdOfActionWithSeriesName")
    		  .addModifiers(Modifier.PRIVATE)
    		  .addParameter(String.class, "seriesName")
    		  .returns(int.class)
    		  .addStatement("$T<$T> keys = series.keys()", Enumeration.class, Integer.class)
    		  .beginControlFlow("while (keys.hasMoreElements())")
    		  .addStatement("$T id = keys.nextElement()", Integer.class)
    		  .addStatement("$T xys = series.get(id)", xySeries)
    		  .beginControlFlow("if (xys.getKey().equals(seriesName))")
    		  .addStatement("return id.intValue()")
    		  .endControlFlow()
    		  .endControlFlow()
    		  .addStatement("return -1")
    		  .build();
      
      MethodSpec getXYPlot = MethodSpec.methodBuilder("getXYPlot")
    		  .addModifiers(Modifier.PUBLIC)
    		  .returns(xyPlot)
    		  .addStatement("return chart.getXYPlot()")
    		  .build();
    	
    	String mouseReleasedBlock = "";
    	
    	if (options.getAllowBranchingOption()) {
	    	mouseReleasedBlock += "XYPlot plot = chart.getXYPlot();\n";
	    	mouseReleasedBlock += "Range domainRange = plot.getDataRange(plot.getDomainAxis());\n";
	    	mouseReleasedBlock += "if (domainRange != null) { // chart is not blank\n";
	    	mouseReleasedBlock += "javafx.geometry.Point2D pt = chartViewer.localToScreen(event.getScreenX(), event.getScreenY());\n";
	    	mouseReleasedBlock += "ChartRenderingInfo info = this.chartViewer.getRenderingInfo();\n";
	    	mouseReleasedBlock += "java.awt.geom.Rectangle2D dataArea = info.getPlotInfo().getDataArea();\n";
	    	mouseReleasedBlock += "NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();\n";
	    	mouseReleasedBlock += "RectangleEdge domainAxisEdge = plot.getDomainAxisEdge();\n";
	    	mouseReleasedBlock += "double chartX = domainAxis.java2DToValue(pt.getX(), dataArea, domainAxisEdge);\n";
	    	mouseReleasedBlock += "lastRightClickedX = (int)Math.rint(chartX);\n";
	    	mouseReleasedBlock += "if (domainRange != null && lastRightClickedX >= domainRange.getLowerBound() && lastRightClickedX <= domainRange.getUpperBound()) { // clicked within domain range\n";
	    	mouseReleasedBlock += "if (chartViewer.getContextMenu().getItems().indexOf(newBranchItem) == -1) { // no new branch item on menu currently\n";
	    	mouseReleasedBlock += "chartViewer.getContextMenu().getItems().add(separator);\n";
	    	mouseReleasedBlock += "chartViewer.getContextMenu().getItems().add(newBranchItem);\n";
	    	mouseReleasedBlock += "\n}\n";
	    	mouseReleasedBlock += "else { // clicked outside of domain range\n";
	    	mouseReleasedBlock += "if (chartViewer.getContextMenu().getItems().indexOf(newBranchItem) >= 0) { // new branch item currently on menu\n";
	    	mouseReleasedBlock += "chartViewer.getContextMenu().getItems().remove(newBranchItem);\n";
	    	mouseReleasedBlock += "if (chartViewer.getContextMenu().getItems().indexOf(separator) >= 0) { // has separator\n";
	    	mouseReleasedBlock += "chartViewer.getContextMenu().getItems().remove(separator);\n";
	    	mouseReleasedBlock += "}\n";
	    	mouseReleasedBlock += "}\n";
	    	mouseReleasedBlock += "}\n";
	    	mouseReleasedBlock += "}\n";
	    	mouseReleasedBlock += "}\n";
    	}
    	
    	
        MethodSpec chartMouseClicked = MethodSpec.methodBuilder("chartMouseClicked")
      		  .addModifiers(Modifier.PUBLIC)
        	  .addParameter(chartMouseEventFX, "me")
      		  .addStatement("$T event = me.getTrigger()", mouseEvent)
      		  .beginControlFlow("if (event.getButton() == $T.PRIMARY)", mouseButton)
      		  .addStatement("$T entity = ($T) me.getEntity()", chartEntity, chartEntity)
      		  .beginControlFlow("if ((entity != null) && (entity instanceof $T))", xyItemEntity)
      		  .addStatement("$T xyEntity = ($T) entity", xyItemEntity, xyItemEntity)
      		  .addStatement("// get the x-value of the action (clock tick)")
      		  .addStatement("int xVal = (int) xyEntity.getDataset().getXValue(xyEntity.getSeriesIndex(), xyEntity.getItem())")
      		  .addStatement("// get the y-value of the action (action index)")
      		  .addStatement("int yVal = (int) xyEntity.getDataset().getYValue(xyEntity.getSeriesIndex(), xyEntity.getItem())")
      		  .addStatement("// get the series name of the action")
      		  .addStatement("$T seriesName = indices.get(yVal)", String.class)
      		  .addStatement("// get the action id")
      		  .addStatement("int actionId = $N(seriesName)", getIdOfActionWithSeriesName)
      		  .beginControlFlow("if (actionId > -1)")
      		  .addStatement("$T action = log.get(xVal).getActionStateRepository().getActionWithId(actionId)", action)
      		  .beginControlFlow("if (action != null)")
      		  .addStatement("new $T(seriesName, action, xVal)", actionInfoWindow)
      		  .endControlFlow()
      		  .endControlFlow()
      		  .endControlFlow()
      		  .endControlFlow()
      		  .beginControlFlow("else ")
      		  .addCode(mouseReleasedBlock)
      		  .endControlFlow()
      		  .build();

    	
    	String updateActionCodeBlock = "";
    	// go through each action and generate code for it:
      writeElse = false;
      for (int i = 0; i < actions.size(); i++) {
        ActionType act = actions.get(i);
        String uCaseName = 
        	CodeGeneratorUtils.getUpperCaseLeading(act.getName());
        String lCaseName = act.getName().toLowerCase();
        if (act.isVisibleInExplanatoryTool()) {
          if (writeElse) {
            updateActionCodeBlock += "else ";
          } else {
            writeElse = true;
          }
          updateActionCodeBlock += "if (actionNames[i].equals(\"" + uCaseName + "\")) {\n";
          updateActionCodeBlock += "// get the " + uCaseName
                  + "ActionStateRepository for the last clock tick:\n";
          updateActionCodeBlock += "State state = log.get(log.size() - 1);\n";
          updateActionCodeBlock += "Vector<" + uCaseName + "Action> " + lCaseName
                  + "Actions = state.getActionStateRepository().get" + uCaseName
                  + "ActionStateRepository().getAllActions();\n\n";
          updateActionCodeBlock += "// go through each " + uCaseName + "Action:\n";
          updateActionCodeBlock += "for (int k = 0; k < " + lCaseName
                  + "Actions.size(); k++) {\n";
          updateActionCodeBlock += uCaseName + "Action action = " + lCaseName + 
            		"Actions.get(k);\n\n";
          updateActionCodeBlock += "// update series:\n";
          updateActionCodeBlock += "updateSeries(action, (log.size() - 1));\n";
          updateActionCodeBlock += "}\n}\n";
        }
      }
      
      MethodSpec update = MethodSpec.methodBuilder("update")
    		  .addModifiers(Modifier.PUBLIC)
    		.beginControlFlow("if ((log.size() > 0) && (log.get(log.size() - 1) != null))")
    		.addStatement("// add a new end data point for each series:")
    		.addStatement("// go through each action:")
    		.beginControlFlow("for (int i = 0; i < actionNames.length; i++)")
    		.addCode(updateActionCodeBlock)
    		.endControlFlow()
    		.endControlFlow()
      		.build();
      
  	MethodSpec handle = MethodSpec.methodBuilder("handle")
  			.addModifiers(Modifier.PUBLIC)
			.addParameter(actionEvent, "event")
			.addStatement("$T source = event.getSource()", object)
			.beginControlFlow("if (source == newBranchItem)")
			.addStatement("$T td = new $T()", textInputDialog, textInputDialog)
			.addStatement("td.setTitle(\"Name New Branch\")")
			.addStatement("td.setContentText(\"Please name this new game:\")")
			.addStatement("td.setHeaderText(null)")
			.addStatement("$T<$T> result = td.showAndWait()", optional, String.class)
			.addStatement("result.ifPresent(name -> { this.newBranchName = name; })")
			.beginControlFlow("if (newBranchName != null)")
			.addStatement("$T tempState = ($T) log.get(lastRightClickedX).clone()", state, state)
			.addStatement("$T tempLogger = new $T(tempState, new $T<$T>(log.subList(0, lastRightClickedX)))", logger, logger, arrayList, state)
			.addStatement("$T tempClock = new $T(tempLogger, lastRightClickedX)", clock, clock)
			.addStatement("tempState.setClock(tempClock)")
			.addStatement("tempState.setLogger(tempLogger)")
			.addStatement("$T.startNewBranch(tempState, new Branch(newBranchName, lastRightClickedX, tempClock.getTime(), branch, null))", simse)
			.endControlFlow()
			.endControlFlow()
			.build();
  	
  	MethodSpec chartMouseMoved = MethodSpec.methodBuilder("chartMouseMoved")
  			.addModifiers(Modifier.PUBLIC)
  			.addParameter(chartMouseEventFX, "event")
  			.build();
      
  	TypeSpec anonHandleClass = TypeSpec.anonymousClassBuilder("", eventHandlerClass, actionEvent)
    		  .addSuperinterface(ParameterizedTypeName.get(eventHandlerClass, actionEvent))
			  .addField(String.class, "newBranchName", Modifier.PRIVATE)
              .addMethod(handle)
              .build();
    		  
      
      TypeSpec actionGraph = TypeSpec.classBuilder("ActionGraph")
    		  .addModifiers(Modifier.PUBLIC)
    		  .superclass(stage)
    		  .addSuperinterface(chartMouseListener)
    		  .addField(ParameterizedTypeName.get(arrayList, state), "log", Modifier.PRIVATE)
    		  .addField(stringArray, "actionNames", Modifier.PRIVATE)
    		  .addField(jFreeChart, "chart", Modifier.PRIVATE)
    		  .addField(chartViewer, "chartViewer", Modifier.PRIVATE)
    		  .addField(menuItem, "newBranchItem", Modifier.PRIVATE)
    		  .addField(separatorMenuItem, "separator", Modifier.PRIVATE)
    		  .addField(int.class, "lastRightClickedX", Modifier.PRIVATE)
    		  .addField(xySeriesCollection, "dataset", Modifier.PRIVATE)
    		  .addField(branch, "branch", Modifier.PRIVATE)
    		  .addField(ParameterizedTypeName.get(hashTable, integer, xySeries), "series", Modifier.PRIVATE)
    		  .addField(FieldSpec.builder(ParameterizedTypeName.get(eventHandlerClass, actionEvent), "menuEvent", Modifier.PRIVATE)
					.initializer("$L", anonHandleClass).build())
    		  .addField(FieldSpec.builder(ParameterizedTypeName.get(arrayList, stringClass), "indices", Modifier.PRIVATE)
    				  .initializer("new $T<$T>()", arrayList, stringClass).build())
    		  .addField(FieldSpec.builder(int.class, "actionIndex", Modifier.PRIVATE)
    				  .initializer("1").build())
    		  .addFields(actionFields)
    		  .addMethod(constructor)
    		  .addMethod(createDataset)
    		  .addMethod(setChartColors)
    		  .addMethod(createChart)
    		  .addMethod(updateSeries)
    		  .addMethod(chartMouseClicked)
    		  .addMethod(getIdOfActionWithSeriesName)
    		  .addMethod(update)
    		  .addMethod(getXYPlot)
    		  .addMethod(chartMouseMoved)
    		  .addType(actionGraphToolTipGenerator)
    		  .build();
      
      JavaFile javaFile = JavaFile.builder("", actionGraph)
    		    .build();

      try {
    	FileWriter writer = new FileWriter(actGraphFile);
    	
  	  	String toAppend = "/* File generated by: simse.codegenerator.explanatorytool.ActionGraph */\n"
  	  	  		+ "package simse.explanatorytool;\n"
  	  	  		+ "\n"
  	  	  		+ "import simse.adts.actions.*;\n"
  	  	  		+ "import java.util.Vector;\n"
  	  			+ "import org.jfree.data.Range;\n"
  	  	  		+ "import org.jfree.chart.ChartRenderingInfo;\n"
  	  			+ "import org.jfree.chart.ui.RectangleEdge;\n";
    	
		writer.write(toAppend + javaFile.toString());
		
		writer.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
}
