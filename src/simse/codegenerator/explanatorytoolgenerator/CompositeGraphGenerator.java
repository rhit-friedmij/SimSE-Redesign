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
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

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
      ClassName textInput = ClassName.get("javafx.scene.control", "TextInputDialog");
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

     
      
      MethodSpec constructor = MethodSpec.constructorBuilder()
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
      

    
    	String rightClickBlock = "";
    	if (options.getAllowBranchingOption()) {
//	    	writer.write("if (me.getButton() != MouseEvent.BUTTON1) { // not left-click");
//	    	writer.write(NEWLINE);
	    	rightClickBlock += "if (me.getButton() != MouseEvent.BUTTON1) { // not left-click\n";
//	    	writer.write("XYPlot plot = chart.getXYPlot();");
//	    	writer.write(NEWLINE);
	    	rightClickBlock += "XYPlot plot = chart.getXYPlot();\n";
//	    	writer.write("Range domainRange = plot.getDataRange(plot.getDomainAxis());");
//	    	writer.write(NEWLINE);
	    	rightClickBlock += "Range domainRange = plot.getDataRange(plot.getDomainAxis());\n";
//	    	writer.write("if (domainRange != null) { // chart is not blank");
//	    	writer.write(NEWLINE);
	    	rightClickBlock += "if (domainRange != null) { // chart is not blank\\n";
//	    	writer.write("Point2D pt = chartPanel.translateScreenToJava2D(new Point(me.getX(), me.getY()));");
//	    	writer.write(NEWLINE);
	    	rightClickBlock += "javafx.geometry.Point2D pt = chartViewer.localToScreen(event.getScreenX(), event.getScreenY());\n";
//	    	writer.write("ChartRenderingInfo info = this.chartPanel.getChartRenderingInfo();");
//	    	writer.write(NEWLINE);
	    	rightClickBlock += "ChartRenderingInfo info = this.chartViewer.getRenderingInfo();\n";
//	    	writer.write("Rectangle2D dataArea = info.getPlotInfo().getDataArea();");
//	    	writer.write(NEWLINE);
	    	rightClickBlock += "java.awt.geom.Rectangle2D dataArea = info.getPlotInfo().getDataArea();\n";
//	    	writer.write("NumberAxis domainAxis = (NumberAxis)plot.getDomainAxis();");
//	    	writer.write(NEWLINE);
	    	rightClickBlock += "NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();\n";
//	    	writer.write("RectangleEdge domainAxisEdge = plot.getDomainAxisEdge();");
//	    	writer.write(NEWLINE);
	    	rightClickBlock += "RectangleEdge domainAxisEdge = plot.getDomainAxisEdge();\n";
//	    	writer.write("double chartX = domainAxis.java2DToValue(pt.getX(), dataArea, domainAxisEdge);");
//	    	writer.write(NEWLINE);
	    	rightClickBlock += "double chartX = domainAxis.java2DToValue(pt.getX(), dataArea, domainAxisEdge);\n";
//	    	writer.write("lastRightClickedX = (int)Math.rint(chartX);");
//	    	writer.write(NEWLINE);
	    	rightClickBlock += "lastRightClickedX = (int) Math.rint(chartX);\n";
//	    	writer.write("if (domainRange != null && lastRightClickedX >= domainRange.getLowerBound() && lastRightClickedX <= domainRange.getUpperBound()) { // clicked within domain range");
//	    	writer.write(NEWLINE);
	    	rightClickBlock += "if (domainRange != null && lastRightClickedX >= domainRange.getLowerBound() && lastRightClickedX <= domainRange.getUpperBound()) { // clicked within domain range\n";
//	    	writer.write("if (chartPanel.getPopupMenu().getComponentIndex(newBranchItem) == -1) { // no new branch item on menu currently");
//	    	writer.write(NEWLINE);
	    	rightClickBlock += "if ((chartViewer).getContextMenu().getItems().indexOf(\r\n" + 
	    			"							newBranchItem) == -1) { // no new branch item on\r\n" + 
	    			"													// menu currently\n";
//	    	writer.write("chartPanel.getPopupMenu().add(separator);");
//	    	writer.write(NEWLINE);
	    	rightClickBlock += "chartViewer.getContextMenu().getItems().add(separator);\n";
//	    	writer.write("chartPanel.getPopupMenu().add(newBranchItem);");
//	    	writer.write(NEWLINE);
	    	rightClickBlock += "chartViewer.getContextMenu().getItems().add(newBranchItem);\n";
	    	rightClickBlock += "}\n";
	    	rightClickBlock += "else { // clicked outside of domain range\n";
//	    	writer.write("if (chartPanel.getPopupMenu().getComponentIndex(newBranchItem) >= 0) { // new branch item currently on menu");
//	    	writer.write(NEWLINE);
	    	rightClickBlock += "if (chartViewer.getContextMenu().getItems().indexOf(newBranchItem) >= 0) { // new branch item currently\r\n" + 
	    			"													// on menu";
//	    	writer.write("chartPanel.getPopupMenu().remove(newBranchItem);");
//	    	writer.write(NEWLINE);
	    	rightClickBlock += "chartViewer.getContextMenu().getItems().remove(newBranchItem);\n";
//	    	writer.write("if (chartPanel.getPopupMenu().getComponentIndex(separator) >= 0) { // has separator");
//	    	writer.write(NEWLINE);
	    	rightClickBlock += "if (chartViewer.getContextMenu().getItems().indexOf(\r\n" + 
	    			"								separator) >= 0) { // has separator";
//	    	writer.write("chartPanel.getPopupMenu().remove(separator);");
//	    	writer.write(NEWLINE);
	    	rightClickBlock += "chartViewer.getContextMenu().getItems().remove(separator);";
//	    	writer.write(CLOSED_BRACK);
//	    	writer.write(NEWLINE);
	    	rightClickBlock += "}\n";
//	    	writer.write("chartPanel.getPopupMenu().pack();");
//	    	writer.write(NEWLINE);
	    	rightClickBlock += "}\n";
//	    	writer.write(CLOSED_BRACK);
//	    	writer.write(NEWLINE);
	    	rightClickBlock += "}\n";
//	    	writer.write(CLOSED_BRACK);
//	    	writer.write(NEWLINE);
	    	rightClickBlock += "}\n";
//	    	writer.write(CLOSED_BRACK);
//	    	writer.write(NEWLINE);
	    	rightClickBlock += "}\n";
    	}
//    	writer.write(CLOSED_BRACK);
//    	writer.write(NEWLINE);
//    	writer.write(NEWLINE);
    	
	MethodSpec update = MethodSpec.methodBuilder("update")
			.returns(void.class)
			.addStatement(CodeBlock.builder().add("actGraph.update()").build())
			.addStatement(CodeBlock.builder().add("objGraph.update()").build())
			.build();
	
	MethodSpec handle = MethodSpec.methodBuilder("handle")
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
	
	TypeSpec anonHandleClass = TypeSpec.anonymousClassBuilder("")
  		  .addField(String.class, "newBranchName")
            .addMethod(handle)
            .build();
	
	MethodSpec chartMouseClicked = MethodSpec.methodBuilder("chartMouseClicked")
			.addParameter(chartMouseEventFX, "me")
			.addStatement("MouseEvent event = me.getTrigger()")
			.addCode(rightClickBlock)
			.beginControlFlow("else ")
			.addStatement("actGraph.chartMouseClicked(me)")
			.endControlFlow()
			.build();
	
	TypeSpec eventHandler = TypeSpec.classBuilder("menuEvent")
  .addModifiers(Modifier.PRIVATE)
  .addStaticBlock(CodeBlock.builder()
  .addStatement("private $T<$T> menuEvent = new $T<$T>() $L",
                eventHandlerClass,
                actionEvent,
                eventHandlerClass,
                actionEvent,
                anonHandleClass).build())
  .build();
	
	TypeSpec compositeGraph = TypeSpec.classBuilder("CompositeGraph")
			.addField(actionGraph, "actGraph")
			.addField(objectGraph, "objGraph")
			.addField(jFreeChart, "chart")
			.addField(chartViewer, "chartViewer")
			.addField(int.class, "lastRightClickedX")
			.addField(menuItem, "newBranchItem")
			.addField(separatorMenuItem, "separator")
			.addField(branch, "branch")
			.addType(eventHandler)
			.addMethod(constructor)
			.addMethod(update)
			.addMethod(chartMouseClicked)
			.build();
			
	
	

    
    JavaFile javaFile = JavaFile.builder("simse.explanatorytool", compositeGraph)
		    .build();
	
    try {
		javaFile.writeTo(compGraphFile);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
}