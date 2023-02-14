package simse.codegenerator.guigenerator;

import java.io.IOException;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

public class Test {

	public static void main(String[] args) throws IOException {
		String hello = "hello";
		MethodSpec.Builder test = MethodSpec.methodBuilder("main")
				.addStatement("String x = $S", hello)
				.addStatement("x=\"goodbye\"");
		
		test.addStatement("$T.out.println(x)", System.class);
		
		if (hello.equals("hello")) test.addStatement("int i=0").addStatement("i++");
		
		MethodSpec testy = test.build();
		
		TypeSpec t = TypeSpec.classBuilder("Test")
				.addField(FieldSpec.builder(String.class, "testy", Modifier.PRIVATE)
						.initializer("null")
						.build())
				.addMethod(testy)
				.build();
		
		JavaFile file = JavaFile.builder("com.test", t)
				.build();
		
		file.writeTo(System.out);

	}

}
