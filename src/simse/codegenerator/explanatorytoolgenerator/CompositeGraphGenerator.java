/*
 * This class is responsible for generating all of the code for the
 * CompositeGraph class in the explanatory tool
 */

package simse.codegenerator.explanatorytoolgenerator;

import simse.codegenerator.CodeGeneratorConstants;
import simse.modelbuilder.ModelOptions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.lang.model.element.Modifier;
import javax.swing.JOptionPane;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.sun.glass.events.MouseEvent;

public class CompositeGraphGenerator implements CodeGeneratorConstants {
  private File directory; // directory to save generated code into
  private ModelOptions options;

  public CompositeGraphGenerator(File directory, ModelOptions options) {
    this.directory = directory;
    this.options = options;
  }

  public void generate() {
    File compGraphFile = new File(directory,
        ("simse\\explanatorytool\\CompositeGraph.java"));
    if (compGraphFile.exists()) {
      compGraphFile.delete(); // delete old version of file
    }
      
      ClassName chartRenderingInfo = ClassName.get("org.jfree.chart", "ChartRenderingInfo");
      ClassName jFreeChart = ClassName.get("org.jfree.chart", "JFreeChart");
      ClassName numberAxis = ClassName.get("org.jfree.chart.axis", "NumberAxis");
      ClassName chartViewer = ClassName.get("org.jfree.chart.fx", "ChartViewer");
      ClassName mouseEvent = ClassName.get("javafx.scene.input", "MouseEvent");
      ClassName mouseButton = ClassName.get("javafx.scene.input", "MouseButton");
      ClassName chartMouseEventFX = ClassName.get("org.jfree.chart.fx.interaction", "ChartMouseEventFX");
      ClassName chartMouseListenerFX = ClassName.get("org.jfree.chart.fx.interaction", "ChartMouseListenerFX");
      ClassName combinedDomainXYPlot = ClassName.get("org.jfree.chart.plot", "CombinedDomainXYPlot");
      ClassName plotOrientation = ClassName.get("org.jfree.chart.plot", "PlotOrientation");
      ClassName xyPlot = ClassName.get("org.jfree.chart.plot", "XYPlot");
      ClassName textTitle = ClassName.get("org.jfree.chart.title", "TextTitle");
      ClassName rectangleEdge = ClassName.get("org.jfree.chart.ui", "RectangleEdge");
      ClassName actionEvent = ClassName.get("javafx.event", "ActionEvent");
      ClassName eventHandlerClass = ClassName.get("javafx.event", "EventHandler");
      ClassName scene = ClassName.get("javafx.scene", "Scene");
      ClassName menuItem = ClassName.get("javafx.scene.control", "MenuItem");
      ClassName separatorMenuItem = ClassName.get("javafx.scene.control", "SeparatorMenuItem");
      ClassName objectGraph = ClassName.get("simse.explanatorytool", "ObjectGraph");
      ClassName actionGraph = ClassName.get("simse.explanatorytool", "ActionGraph");
      ClassName branch = ClassName.get("simse.explanatorytool", "Branch");
      ClassName javaFXHelpers = ClassName.get("simse.gui.util", "JavaFXHelpers");
      ClassName color = ClassName.get("javafx.scene.paint", "Color");
  	  ClassName object = ClassName.get("java.lang", "Object");
  	  ClassName textInputDialog = ClassName.get("javafx.scene.control", "TextInputDialog");
  	  ClassName optional = ClassName.get("java.util", "Optional");
  	  ClassName arrayList = ClassName.get("java.util", "ArrayList");
  	  ClassName state = ClassName.get("simse.state", "State");
  	  ClassName clock = ClassName.get("simse.state", "Clock");
  	  ClassName logger = ClassName.get("simse.state.logger", "Logger");
  	  ClassName simse = ClassName.get("simse", "SimSE");
  	  ClassName stage = ClassName.get("javafx.stage", "Stage");
  	  ClassName range = ClassName.get("org.jfree.data", "Range");
  	  
      MethodSpec constructor = MethodSpec.constructorBuilder()
    		  .addModifiers(Modifier.PUBLIC)
    		  .addParameter(objectGraph, "objGraph")
    		  .addParameter(actionGraph, "actGraph")
    		  .addParameter(branch, "branch")
    		  .addStatement("super()")
    		  .addStatement("this.branch = branch")
    		  .addStatement("$T title = \"Composite Graph\"", String.class)
    		  .beginControlFlow("if (branch.getName() != null)")
    		  .addStatement("title = title.concat(\" - \" + branch.getName())")
    		  .endControlFlow()
    		  .addStatement("setTitle(title)")
    		  .addStatement("this.actGraph = actGraph")
    		  .addStatement("this.objGraph = objGraph")
    		  .addStatement("lastRightClickedX = 1")
    		  .addStatement("newBranchItem = new $T(\"Start new branch from here\")", menuItem)
    		  .addStatement("newBranchItem.setOnAction(menuEvent)")
    		  .addStatement("separator = new $T()", separatorMenuItem)
    		  .addStatement("// parent plot")
    		  .addStatement("$T domainAxis = new $T(\"Clock Ticks\")", numberAxis, numberAxis)
    		  .addStatement("domainAxis.setStandardTickUnits($T.createIntegerTickUnits())", numberAxis)
    		  .addStatement("$T plot = new $T(domainAxis)", combinedDomainXYPlot, combinedDomainXYPlot)
    		  .addStatement("// add the subplots")
    		  .addStatement("plot.add(this.objGraph.getXYPlot(), 1)")
    		  .addStatement("plot.add(this.actGraph.getXYPlot(), 1)")
    		  .addStatement("plot.setOrientation($T.VERTICAL)", plotOrientation)
    		  .addStatement("// make a new chart containing the overlaid plot")
    		  .addStatement("chart = new $T(\"Composite (Object/Action) Graph\", $T.DEFAULT_TITLE_FONT, plot, true)", jFreeChart, jFreeChart)
    		  .addStatement("$T subtitle = new $T(objGraph.getChartTitle() + \" and Selected Actions\")", textTitle, textTitle)
    		  .addStatement("chart.addSubtitle(subtitle)")
    		  .addStatement("chartViewer = new $T(chart)", chartViewer)
    		  .addStatement("chartViewer.backgroundProperty().set($T.createBackgroundColor($T.WHITE))", javaFXHelpers, color)
    		  .addStatement("chartViewer.addChartMouseListener(this)")
    		  .addStatement("setScene(new $T(chartViewer))", scene)
    		  .addStatement("show()")
    		  .build();
      
      	ClassName math = ClassName.get("java.lang", "Math");
      	
      	CodeBlock rightClick = null;
    
    	if (options.getAllowBranchingOption()) {
    		rightClick = CodeBlock.builder()
    				.beginControlFlow("if (event.getButton() != $T.PRIMARY) { // not left-click", mouseButton)
    				.addStatement("$T plot = chart.getXYPlot()", xyPlot)
    				.addStatement("$T domainRange = plot.getDataRange(plot.getDomainAxis())", range)
    				.beginControlFlow("if (domainRange != null) { // chart is not blank\\")
    				.addStatement("javafx.geometry.Point2D pt = chartViewer.localToScreen(event.getScreenX(), event.getScreenY())")
    				.addStatement("$T info = this.chartViewer.getRenderingInfo()", chartRenderingInfo)
    				.addStatement("java.awt.geom.Rectangle2D dataArea = info.getPlotInfo().getDataArea()")
    				.addStatement("$T domainAxis = ($T) plot.getDomainAxis()", numberAxis, numberAxis)
    				.addStatement("$T domainAxisEdge = plot.getDomainAxisEdge()", rectangleEdge)
    				.addStatement("double chartX = domainAxis.java2DToValue(pt.getX(), dataArea, domainAxisEdge)")
    				.addStatement("lastRightClickedX = ($T) $T.rint(chartX)", int.class, math)
    				.beginControlFlow("if (domainRange != null && lastRightClickedX >= domainRange.getLowerBound() && lastRightClickedX <= domainRange.getUpperBound()) { // clicked within domain range")
    				.beginControlFlow("if ((chartViewer).getContextMenu().getItems().indexOf(newBranchItem) == -1)")
    				.addStatement("chartViewer.getContextMenu().getItems().add(separator)")
    				.addStatement("chartViewer.getContextMenu().getItems().add(newBranchItem)")
    				.endControlFlow()
    				.beginControlFlow("else")
    				.beginControlFlow("if (chartViewer.getContextMenu().getItems().indexOf(newBranchItem) >= 0) { // new branch item currently")
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
    	else  {
    		rightClick = CodeBlock.builder().build();
    	}

	MethodSpec update = MethodSpec.methodBuilder("update")
			.addModifiers(Modifier.PUBLIC)
			.returns(void.class)
			.addStatement(CodeBlock.builder().add("actGraph.update()").build())
			.addStatement(CodeBlock.builder().add("objGraph.update()").build())
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
			.addStatement("$T tempState = ($T) objGraph.getLog().get(lastRightClickedX).clone()", state, state)
			.addStatement("$T tempLogger = new $T(tempState, new $T<$T>(objGraph.getLog().subList(0, lastRightClickedX)))", logger, logger, arrayList, state)
			.addStatement("$T tempClock = new $T(tempLogger, lastRightClickedX)", clock, clock)
			.addStatement("tempState.setClock(tempClock)")
			.addStatement("tempState.setLogger(tempLogger)")
			.addStatement("$T.startNewBranch(tempState, new Branch(newBranchName, lastRightClickedX, tempClock.getTime(), branch, null))", simse)
			.endControlFlow()
			.endControlFlow()
			.build();
	
	TypeSpec anonHandleClass = TypeSpec.anonymousClassBuilder("", eventHandlerClass, actionEvent)
			.addSuperinterface(ParameterizedTypeName.get(eventHandlerClass, actionEvent))
			.addField(String.class, "newBranchName", Modifier.PRIVATE)
            .addMethod(handle)
            .build();
	
	MethodSpec chartMouseClicked = MethodSpec.methodBuilder("chartMouseClicked")
			.addModifiers(Modifier.PUBLIC)
			.addParameter(chartMouseEventFX, "me")
			.addStatement("$T event = me.getTrigger()", mouseEvent)
			.addCode(rightClick)
			.beginControlFlow("else ")
			.addStatement("actGraph.chartMouseClicked(me)")
			.endControlFlow()
			.endControlFlow()
			.build();
	
	MethodSpec chartMouseMoved = MethodSpec.methodBuilder("chartMouseMoved")
			.addModifiers(Modifier.PUBLIC)
			.addParameter(chartMouseEventFX, "me")
			.build();
	

	
	TypeSpec compositeGraph = TypeSpec.classBuilder("CompositeGraph")
			.addModifiers(Modifier.PUBLIC)
			.superclass(stage)
			.addSuperinterface(chartMouseListenerFX)
			.addField(actionGraph, "actGraph", Modifier.PRIVATE)
			.addField(objectGraph, "objGraph", Modifier.PRIVATE)
			.addField(jFreeChart, "chart", Modifier.PRIVATE)
			.addField(chartViewer, "chartViewer", Modifier.PRIVATE)
			.addField(int.class, "lastRightClickedX", Modifier.PRIVATE)
			.addField(menuItem, "newBranchItem", Modifier.PRIVATE)
			.addField(separatorMenuItem, "separator", Modifier.PRIVATE)
			.addField(branch, "branch",Modifier.PRIVATE)
			.addField(FieldSpec.builder(ParameterizedTypeName.get(eventHandlerClass, actionEvent), "menuEvent", Modifier.PRIVATE)
					.initializer("$L", anonHandleClass).build())
			.addMethod(constructor)
			.addMethod(update)
			.addMethod(chartMouseClicked)
			.addMethod(chartMouseMoved)
			.build();
			

    
    JavaFile javaFile = JavaFile.builder("simse.explanatorytool", compositeGraph)
		    .build();
	
    try {
    	FileWriter writer = new FileWriter(compGraphFile);
		javaFile.writeTo(writer);
		
		writer.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
}