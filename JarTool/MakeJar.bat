set /p jarName= Enter a name for the generated file:
echo %jarName%

javac -classpath ".;lib\javafx.base.jar;lib\javafx.controls.jar;lib\javafx.fxml.jar;lib\javafx.graphics.jar;lib\javafx.media.jar;lib\javafx.swing.jar;lib\javafx.web.jar;lib\javafx-swt.jar;lib\jcommon-1.0.0-rc1.jar;lib\jfreechart-1.5.3.jar;lib\org.jfree.chart.fx-2.0.1.jar;lib\org.jfree.fxgraphics2d-2.1.jar;" simse\adts\actions\*.java simse\adts\objects\*.java simse\animation\*.java simse\engine\*.java simse\explanatorytool\*.java simse\gui\util\*.java simse\gui\*.java simse\logic\*.java simse\logic\dialogs\*.java simse\state\*.java simse\state\logger\*.java simse\util\*.java simse\*.java --module-path lib --add-modules javafx.base,javafx.controls,javafx.fxml,javafx.graphics,javafx.media,javafx.swing,javafx.web,org.jfree.jfreechart,org.jfree.chart.fx

md %jarName%

xcopy /s lib %jarName%\lib\
xcopy /s openjfx-19.0.2.1_windows-x64_bin-sdk %jarName%\openjfx-19.0.2.1_windows-x64_bin-sdk\
xcopy /s simse\gui\icons %jarName%\src\simse\gui\icons\
xcopy /s simse\gui\images %jarName%\src\simse\gui\images\
xcopy /s simse\SimSEMap %jarName%\src\simse\simSEMap\
xcopy /s simse\sprites %jarName%\src\simse\sprites\

jar cmf manifest.txt %jarName%\%jarName%.jar style.css simse\adts\actions\*.class simse\adts\objects\*.class simse\animation\*.class simse\engine\*.class simse\explanatorytool\*.class simse\gui\util\*.class simse\gui\*.class simse\logic\*.class simse\logic\dialogs\*.class simse\state\*.class simse\state\logger\*.class simse\util\*.class simse\*.class lib\*.jar openjfx-19.0.2.1_windows-x64_bin-sdk\*

echo java --module-path lib --add-modules javafx.controls,javafx.fxml,org.jfree.jfreechart,org.jfree.chart.fx --add-exports javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED -Djava.library.path="lib" -Djava.library.path="openjfx-19.0.2.1_windows-x64_bin-sdk\javafx-sdk-19.0.2.1\bin" -jar %jarName%.jar > %jarName%\RUNME.bat