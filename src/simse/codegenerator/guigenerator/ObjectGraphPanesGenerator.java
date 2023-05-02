package simse.codegenerator.guigenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import javax.lang.model.element.Modifier;
import javax.swing.JOptionPane;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import simse.codegenerator.CodeGeneratorUtils;
import simse.modelbuilder.objectbuilder.Attribute;
import simse.modelbuilder.objectbuilder.DefinedObjectTypes;
import simse.modelbuilder.objectbuilder.NumericalAttribute;
import simse.modelbuilder.objectbuilder.SimSEObjectType;
import simse.modelbuilder.objectbuilder.SimSEObjectTypeTypes;
import simse.modelbuilder.startstatebuilder.CreatedObjects;
import simse.modelbuilder.startstatebuilder.SimSEObject;

public class ObjectGraphPanesGenerator {

	private CreatedObjects objs;
	private DefinedObjectTypes objTypes;
	private File directory; // directory to save generated code into

	  public ObjectGraphPanesGenerator(CreatedObjects objs, DefinedObjectTypes objTypes, File directory) {
		  this.objs = objs;
		  this.objTypes = objTypes;
		  this.directory = directory;
	  }
	  
	  public void generate() {
		  generateGraphPanel();
		  generateGraphPane();
	  }

	  private void generateGraphPanel() {
	    File gPanelFile = new File(directory, ("simse\\gui\\ObjectGraphPanel.java"));
	    if (gPanelFile.exists()) {
	    	gPanelFile.delete(); // delete old version of file
	    }
	    try {
	      FileWriter writer = new FileWriter(gPanelFile);
	      ClassName eventhandler = ClassName.get("javafx.event", "EventHandler");
	      ClassName button = ClassName.get("javafx.scene.control", "Button");
	      ClassName mouseevent = ClassName.get("javafx.scene.input", "MouseEvent");
	      ClassName pane = ClassName.get("javafx.scene.layout", "Pane");
	      ClassName vbox = ClassName.get("javafx.scene.layout", "VBox");
	      ClassName hbox = ClassName.get("javafx.scene.layout", "HBox");
	      ClassName simsegui = ClassName.get("simse.gui", "SimSEGUI");
	      ClassName panels = ClassName.get("simse.gui", "Panels");
	      ClassName objectgraphpane = ClassName.get("simse.gui", "ObjectGraphPane");
	      ClassName simsepanel = ClassName.get("simse.gui", "SimSEPanel");
	      ClassName arrayList = ClassName.get("java.util", "ArrayList");
	  	  TypeName listOfStrings = ParameterizedTypeName.get(arrayList, ClassName.get(String.class));
	      TypeName mousehandler = ParameterizedTypeName.get(eventhandler, mouseevent);
	      
	      String objTypeType = CodeGeneratorUtils.getUpperCaseLeading(
	  				SimSEObjectTypeTypes.getText(SimSEObjectTypeTypes.PROJECT));
	      
	      MethodSpec constructor = MethodSpec.constructorBuilder()
	    		  .addModifiers(Modifier.PUBLIC)
	    		  .addParameter(simsegui, "gui")
	    		  .addStatement("this.mainPane = new $T()", vbox)
	    		  .addStatement("this.$N = gui", "gui")
	    		  .addStatement("this.$N = 0", "currentProj")
	    		  .addStatement("this.$N = new $T()", "titles", listOfStrings)
	    		  .addStatement("this.$N = new $T()", "objTypes", listOfStrings)
	    		  .addStatement("this.$N = new $T()", "keyAttVals", listOfStrings)
	    		  .addStatement("this.$N = $S", "objTypeType", objTypeType)
	    		  .addCode(generateProjsList().build())
	    		  .addStatement("this.$N = new ObjectGraphPane(titles.get(currentProj), $N.getLog(), objTypeType, "
	    		  		+ "objTypes.get(currentProj), keyAttVals.get(currentProj), $N.getBranch(), $N)", "objGraph", "gui", "gui", "gui")
	    		  .addStatement("mainPane.getChildren().add($N)", "objGraph")
	    		  .addStatement("$T buttonPanel = new $T()", hbox, hbox)
	    		  .addStatement("$N = new $T(\"Update Graph\")", "updateGraph", button)
	    		  .addStatement("$N.addEventHandler($T.MOUSE_CLICKED, this)", "updateGraph", mouseevent)
	    		  .addStatement("buttonPanel.getChildren().add($N)", "updateGraph")
	    		  .addStatement("$N = new $T(\"Next Project\")", "nextProj", button)
	    		  .addStatement("$N.addEventHandler($T.MOUSE_CLICKED, this)", "nextProj", mouseevent)
	    		  .addStatement("buttonPanel.getChildren().add($N)", "nextProj")
	    		  .addStatement("this.mainPane.getChildren().add($N)", "buttonPanel")
	    		  .addStatement("this.getChildren().add(mainPane)")
	    		  .build();
	      
	      MethodSpec update = MethodSpec.methodBuilder("update")
	    		  .addModifiers(Modifier.PUBLIC)
	    		  .returns(void.class)
	    		  .beginControlFlow("if (!gui.getEngine().isRunning())")
	    		  .addStatement("this.objGraph.update()")
	    		  .endControlFlow()
	    		  .build();
	      
	      MethodSpec panelType = MethodSpec.methodBuilder("getPanelType")
	    		  .addAnnotation(Override.class)
	    		  .addModifiers(Modifier.PUBLIC)
	    		  .returns(panels)
	    		  .addStatement("return $T.GRAPH", panels)
	    		  .build();
	      
	      MethodSpec handle = MethodSpec.methodBuilder("handle")
	    		  .addAnnotation(Override.class)
	    		  .addModifiers(Modifier.PUBLIC)
	    		  .returns(void.class)
	    		  .addParameter(mouseevent, "e")
	    		  .beginControlFlow("if (e.getSource() == updateGraph)")
	    		  .addStatement("this.objGraph.update()")
	    		  .nextControlFlow("else if (e.getSource() == nextProj)")
	    		  .addStatement("mainPane.getChildren().remove(objGraph)")
	    		  .addStatement("currentProj++")
	    		  .beginControlFlow("if (currentProj >= titles.size())")
	    		  .addStatement("currentProj = 0")
	    		  .endControlFlow()
	    		  .addStatement("this.$N = new ObjectGraphPane(titles.get(currentProj), $N.getLog(), objTypeType, "
		    		  		+ "objTypes.get(currentProj), keyAttVals.get(currentProj), $N.getBranch(), $N)", "objGraph", "gui", "gui", "gui")
	    		  .addStatement("mainPane.getChildren().add(0, $N)", "objGraph")
	    		  .endControlFlow()
	    		  .build();
	      
	      TypeSpec ogPanel = TypeSpec.classBuilder("ObjectGraphPanel")
	    		  .superclass(pane)
	    		  .addSuperinterface(simsepanel)
	    		  .addSuperinterface(mousehandler)
	    		  .addModifiers(Modifier.PUBLIC)
	    		  .addField(listOfStrings, "titles", Modifier.PRIVATE)
	    		  .addField(listOfStrings, "objTypes", Modifier.PRIVATE)
	    		  .addField(listOfStrings, "keyAttVals", Modifier.PRIVATE)
	    		  .addField(String.class, "objTypeType", Modifier.PRIVATE)
	    		  .addField(int.class, "currentProj", Modifier.PRIVATE)
	    		  .addField(objectgraphpane, "objGraph", Modifier.PRIVATE)
	    		  .addField(button, "updateGraph", Modifier.PRIVATE)
	    		  .addField(button, "nextProj", Modifier.PRIVATE)
	    		  .addField(vbox, "mainPane", Modifier.PRIVATE)
	    		  .addField(simsegui, "gui", Modifier.PRIVATE)
	    		  .addMethod(constructor)
	    		  .addMethod(update)
	    		  .addMethod(panelType)
	    		  .addMethod(handle)
	    		  .build();
	      
	      JavaFile file = JavaFile.builder("simse.gui", ogPanel)
	    		  .build();
	      
	      file.writeTo(writer);
	      
	      writer.close();
	    } catch (IOException e) {
	      JOptionPane.showMessageDialog(null, ("Error writing file "
	          + gPanelFile.getPath() + ": " + e.toString()), "File IO Error",
	          JOptionPane.WARNING_MESSAGE);
	    }
	  }
	  
	  private CodeBlock.Builder generateProjsList() {
		  CodeBlock.Builder projList = CodeBlock.builder();
		  
		  
		  Vector<SimSEObject> projects = getAllProjs();
		  for (SimSEObject proj : projects) {
		      String keyAttVal = proj.getKey().getValue().toString();
		      String objType = CodeGeneratorUtils.getUpperCaseLeading(proj.getSimSEObjectType().getName());
		      String title = keyAttVal + " Attributes";
		      projList.addStatement("this.$N.add($S)", "titles", title);
		      projList.addStatement("this.$N.add($S)", "objTypes", objType);
		      projList.addStatement("this.$N.add($S)", "keyAttVals", keyAttVal);
		  }
		  return projList;
	  }
	  
	  private void generateGraphPane() {
		    File gPaneFile = new File(directory, ("simse\\gui\\ObjectGraphPane.java"));
		    if (gPaneFile.exists()) {
		    	gPaneFile.delete(); // delete old version of file
		    }
		    try {
		      FileWriter writer = new FileWriter(gPaneFile);
		      
		      ClassName arraylist = ClassName.get("java.util", "ArrayList");
		      ClassName optional = ClassName.get("java.util", "Optional");
		      ClassName chartrenderinginfo = ClassName.get("org.jfree.chart", "ChartRenderingInfo");
		      ClassName jfreechart = ClassName.get("org.jfree.chart", "JFreeChart");
		      ClassName numberaxis = ClassName.get("org.jfree.chart.axis", "NumberAxis");
		      ClassName chartviewer = ClassName.get("org.jfree.chart.fx", "ChartViewer");
		      ClassName chartmouseeventfx = ClassName.get("org.jfree.chart.fx.interaction", "ChartMouseEventFX");
		      ClassName chartmouselistenerfx = ClassName.get("org.jfree.chart.fx.interaction", "ChartMouseListenerFX");
		      ClassName xyplot = ClassName.get("org.jfree.chart.plot", "XYPlot");
		      ClassName rectangleedge = ClassName.get("org.jfree.chart.ui", "RectangleEdge");
		      ClassName range = ClassName.get("org.jfree.data", "Range");
		      ClassName actionevent = ClassName.get("javafx.event", "ActionEvent");
		      ClassName eventhandler = ClassName.get("javafx.event", "EventHandler");
		      ClassName menuitem = ClassName.get("javafx.scene.control", "MenuItem");
		      ClassName separatormenuitem = ClassName.get("javafx.scene.control", "SeparatorMenuItem");
		      ClassName textinputdialog = ClassName.get("javafx.scene.control", "TextInputDialog");
		      ClassName mousebutton = ClassName.get("javafx.scene.input", "MouseButton");
		      ClassName mouseevent = ClassName.get("javafx.scene.input", "MouseEvent");
		      ClassName pane = ClassName.get("javafx.scene.layout", "Pane");
		      ClassName vbox = ClassName.get("javafx.scene.layout", "VBox");
		      ClassName simse = ClassName.get("simse", "SimSE");
		      ClassName branch = ClassName.get("simse.explanatorytool", "Branch");
		      ClassName objectgraph = ClassName.get("simse.explanatorytool", "ObjectGraph");
		      ClassName clock = ClassName.get("simse.state", "Clock");
		      ClassName state = ClassName.get("simse.state", "State");
		      ClassName logger = ClassName.get("simse.state.logger", "Logger");
		      ClassName simsegui = ClassName.get("simse.gui", "SimSEGUI");
		      ClassName string = ClassName.get(String.class);
		      TypeName listOfState = ParameterizedTypeName.get(arraylist, state);
		      TypeName actionHandler = ParameterizedTypeName.get(eventhandler, actionevent);
		      TypeName optionalString = ParameterizedTypeName.get(optional, string);
		      
		      MethodSpec constructor = MethodSpec.constructorBuilder()
		    		  .addModifiers(Modifier.PUBLIC)
		    		  .addParameter(String.class, "title")
		    		  .addParameter(listOfState, "log")
		    		  .addParameter(String.class, "objTypeType")
		    		  .addParameter(String.class, "objType")
		    		  .addParameter(String.class, "keyAttVal")
		    		  .addParameter(branch, "branch")
		    		  .addParameter(simsegui, "gui")
		    		  .addStatement("$T mainPane = new $T()", vbox, vbox)
		    		  .addStatement("this.$N = $N", "branch", "branch")
		    		  .addStatement("this.$N = $N", "gui", "gui")
		    		  .beginControlFlow("if ($N.getName() != null)", "branch")
		    		  .addStatement("$N = $N.concat(\" - \" + $N.getName())", "title", "title", "branch")
		    		  .endControlFlow()
		    		  .addStatement("this.$N = $N", "title", "title")
		    		  .addStatement("this.$N = $N", "objTypeType", "objTypeType")
		    		  .addStatement("this.$N = $N", "objType", "objType")
		    		  .addStatement("this.$N = $N", "keyAttVal", "keyAttVal")
		    		  .addStatement("$N = 0", "lastRightClickedX")
		    		  .addStatement("setAttributeList()")
		    		  .addStatement("this.$N = new $T(title, log, objTypeType, objType, keyAttVal, attributes, false, $N)", "objGraph", objectgraph, "branch")
		    		  .addStatement("$N = $N.getChart()", "chart", "objGraph")
		    		  .addStatement("$N = new $T($N)", "chartViewer", chartviewer, "chart")
		    		  .addStatement("$N.addChartMouseListener(this)", "chartViewer")
		    		  .addStatement("$N.setPrefSize(500, 300)", "chartViewer")
		    		  .addStatement("")
		    		  .addStatement("mainPane.getChildren().add($N)", "chartViewer")
		    		  .addStatement("this.getChildren().add(mainPane)")
		    		  .addStatement("$N = new $T(\"Start new game from here\")", "newBranchItem", menuitem)
		    		  .addStatement("$N.setOnAction($N)", "newBranchItem", "menuEvent")
		    		  .addStatement("$N = new $T()", "separator", separatormenuitem)
		    		  .build();
		      
		      MethodSpec setList = MethodSpec.methodBuilder("setAttributeList")
		    		  .addModifiers(Modifier.PRIVATE)
		    		  .returns(void.class)
		    		  .addStatement("$T selectedObject = this.$N + \" \" + this.$N", String.class, "objType", "objTypeType")
		    		  .beginControlFlow("if (selectedObject != null)")
		    		  .addCode(attributeList())
		    		  .endControlFlow()
		    		  .build();
		      
		      MethodSpec update = MethodSpec.methodBuilder("update")
		    		  .addModifiers(Modifier.PUBLIC)
		    		  .returns(void.class)
		    		  .addStatement("$N = $N.getLog()", "log", "gui")
		    		  .beginControlFlow("if ($N != null)", "log")
		    		  .addStatement("this.$N = new $T(title, log, objTypeType, objType, keyAttVal, attributes, false, $N)", "objGraph", objectgraph, "branch")
		    		  .addStatement("$N = $N.getChart()", "chart", "objGraph")
		    		  .addStatement("$N.setChart($N)", "chartViewer", "chart")
		    		  .endControlFlow()
		    		  .build();
		      
		      MethodSpec chartMouseClicked = MethodSpec.methodBuilder("chartMouseClicked")
		    		  .addAnnotation(Override.class)
		    		  .addModifiers(Modifier.PUBLIC)
		    		  .returns(void.class)
		    		  .addParameter(chartmouseeventfx, "me")
		    		  .addStatement("$T event = me.getTrigger()", mouseevent)
		    		  .beginControlFlow("if (event.getButton() != $T.PRIMARY)", mousebutton)
		    		  .addStatement("$T plot = chart.getXYPlot()", xyplot)
		    		  .addStatement("$T domainRange = plot.getDataRange(plot.getDomainAxis())", range)
		    		  .beginControlFlow("if (domainRange != null)")
		    		  .addStatement("javafx.geometry.Point2D pt = $N.localToScreen(event.getScreenX(), event.getScreenY())", "chartViewer")
		    		  .addStatement("$T info = this.$N.getRenderingInfo()", chartrenderinginfo, "chartViewer")
		    		  .addStatement("java.awt.geom.Rectangle2D dataArea = info.getPlotInfo().getDataArea()")
		    		  .addStatement("$T domainAxis = ($T) plot.getDomainAxis()", numberaxis, numberaxis)
		    		  .addStatement("$T domainAxisEdge = plot.getDomainAxisEdge()", rectangleedge)
		    		  .addStatement("double chartX = domainAxis.java2DToValue(pt.getX(), dataArea,domainAxisEdge)")
		    		  .addStatement("$N = (int) Math.rint(chartX)", "lastRightClickedX")
		    		  .beginControlFlow("if (domainRange != null && lastRightClickedX >= domainRange.getLowerBound() && lastRightClickedX <= (domainRange.getUpperBound() - 1) && lastRightClickedX >= 0)")
		    		  .beginControlFlow("if (chartViewer.getContextMenu().getItems().indexOf(newBranchItem) == -1)")
		    		  .addStatement("chartViewer.getContextMenu().getItems().add(separator)")
		    		  .addStatement("chartViewer.getContextMenu().getItems().add(newBranchItem)")
		    		  .endControlFlow()
		    		  .nextControlFlow("else")
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
		      
		      MethodSpec chartMouseMoved = MethodSpec.methodBuilder("chartMouseMoved")
		    		  .addAnnotation(Override.class)
		    		  .addModifiers(Modifier.PUBLIC)
		    		  .returns(void.class)
		    		  .addParameter(chartmouseeventfx, "me")
		    		  .build();
		      
		      TypeSpec anon = TypeSpec.anonymousClassBuilder("")
		    		  .addSuperinterface(actionHandler)
		    		  .addField(FieldSpec.builder(String.class, "newBranchName", Modifier.PRIVATE)
		    				  .initializer("null")
		    				  .build())
		    		  .addMethod(MethodSpec.methodBuilder("handle")
		    				  .addModifiers(Modifier.PUBLIC)
		    				  .returns(void.class)
		    				  .addParameter(actionevent, "event")
		    				  .addStatement("Object source = event.getSource()")
		    				  .beginControlFlow("if (source == newBranchItem)")
		    				  .addStatement("$T td = new $T()", textinputdialog, textinputdialog)
		    				  .addStatement("td.setTitle(\"Name New $T\")", branch)
		    				  .addStatement("td.setContentText(\"Please name this new game:\")")
		    				  .addStatement("td.setHeaderText(null)")
		    				  .addStatement("$T result = td.showAndWait()", optionalString)
		    				  .addStatement("result.ifPresent(name -> {this.newBranchName = name;})")
		    				  .beginControlFlow("if (newBranchName != null)")
		    				  .addStatement("$T temp$T = (State) $N.get($N).clone()", state, state, "log", "lastRightClickedX")
		    				  .addStatement("$T temp$T = new Logger(tempState, new ArrayList<State>($N.subList(0, $N)))", logger, logger, "log", "lastRightClickedX")
		    				  .addStatement("$T temp$T = new Clock(tempLogger, $N)", clock, clock, "lastRightClickedX")
		    				  .addStatement("tempState.setClock(tempClock)")
		    				  .addStatement("tempState.setLogger(tempLogger)")
		    				  .addStatement("$T.startNew$T(tempState, new Branch(newBranchName, lastRightClickedX, tempClock.getTime(), branch, null))", simse, branch)
		    				  .endControlFlow()
		    				  .endControlFlow()
		    				  .build())
		    		  .build();
		      
		      
		      TypeSpec ogPane = TypeSpec.classBuilder("ObjectGraphPane")
		    		  .addModifiers(Modifier.PUBLIC)
		    		  .superclass(pane)
		    		  .addSuperinterface(chartmouselistenerfx)
		    		  .addField(listOfState, "log", Modifier.PRIVATE)
		    		  .addField(String.class, "title", Modifier.PRIVATE)
		    		  .addField(String.class, "objTypeType", Modifier.PRIVATE)
		    		  .addField(String.class, "objType", Modifier.PRIVATE)
		    		  .addField(String.class, "keyAttVal", Modifier.PRIVATE)
		    		  .addField(String[].class, "attributes", Modifier.PRIVATE)
		    		  .addField(jfreechart, "chart", Modifier.PRIVATE)
		    		  .addField(chartviewer, "chartViewer", Modifier.PRIVATE)
		    		  .addField(menuitem, "newBranchItem", Modifier.PRIVATE)
		    		  .addField(separatormenuitem, "separator", Modifier.PRIVATE)
		    		  .addField(int.class, "lastRightClickedX", Modifier.PRIVATE)
		    		  .addField(branch, "branch", Modifier.PRIVATE)
		    		  .addField(objectgraph, "objGraph", Modifier.PRIVATE)
		    		  .addField(simsegui, "gui", Modifier.PRIVATE)
		    		  .addField(FieldSpec.builder(actionHandler, "menuEvent", Modifier.PRIVATE)
		    				  .initializer("$L", anon)
		    				  .build())
		    		  .addMethod(constructor)
		    		  .addMethod(setList)
		    		  .addMethod(update)
		    		  .addMethod(chartMouseClicked)
		    		  .addMethod(chartMouseMoved)
		    		  .build();
		      
		      JavaFile file = JavaFile.builder("simse.gui", ogPane)
		    		  .build();
		      
		      file.writeTo(writer);
		      
		      
		      writer.close();
		    } catch (IOException e) {
		      JOptionPane.showMessageDialog(null, ("Error writing file "
		          + gPaneFile.getPath() + ": " + e.toString()), "File IO Error",
		          JOptionPane.WARNING_MESSAGE);
		    }
		  }
	  
	  private Vector<SimSEObject> getAllProjs() {
		  Vector<SimSEObject> objects = objs.getAllObjects();
		  Vector<SimSEObject> projects = new Vector<>();
	      for (int i = 0; i < objects.size(); i++) {
	        SimSEObject obj = objects.get(i);
	        if (obj.getSimSEObjectType().getType() == SimSEObjectTypeTypes.PROJECT) {
	        	projects.add(obj);
	        }
	      }
	      return projects;
	  }
	  
	  private String attributeList() {
		  String al = "";
		  Vector<SimSEObjectType> objectTypes = objTypes.getAllObjectTypes();
		        for (int i = 0; i < objectTypes.size(); i++) {
		          SimSEObjectType objType = objectTypes.get(i);
		          if (i > 0) {
		        	  al = al.concat("else ");
		          }
		          al = al.concat("if (selectedObject.startsWith(\""
		              + CodeGeneratorUtils.getUpperCaseLeading(objType.getName()) + " "
		              + SimSEObjectTypeTypes.getText(objType.getType())
		              + "\")) {");
		          al = al.concat("\n");
		          al = al.concat("String[] currAttributes = {");
		          al = al.concat("\n");
		          Vector<Attribute> attributes = objType.getAllAttributes();
		          int numVisibleNumericalAtts = 0;
		          for (int j = 0; j < attributes.size(); j++) {
		            Attribute att = attributes.get(j);
		            if ((att instanceof NumericalAttribute)
		                && ((att.isVisible()) || (att.isVisibleOnCompletion()))) {
		            	al = al.concat("\"" + att.getName() + "\",");
		            	al = al.concat("\n");
		              numVisibleNumericalAtts++;
		            }
		          }
		          if (numVisibleNumericalAtts == 0) {
		        	  al = al.concat("\"(No numerical attributes)\"");
		        	  al = al.concat("\n");
		          }
		          al = al.concat("};");
		          al = al.concat("\n");
		          al = al.concat("this.attributes = currAttributes;");
		          al = al.concat("\n");
		          al = al.concat("}");
		          al = al.concat("\n");
		        }
		        
		        al = al.concat(" else {\n");
		        al = al.concat("String[] currAttributes = {};\n");
		        al = al.concat("this.attributes = currAttributes;\n");
		        al = al.concat("}\n");
		  
		  return al;
	  }
}
