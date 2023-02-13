package simse.codegenerator.guigenerator;


import java.io.IOException;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

public abstract class Test {

	public static void main(String[] args) throws IOException {
		MethodSpec main = MethodSpec.methodBuilder("main")
				.addModifiers(Modifier.PUBLIC)
				.returns(void.class)
				.addStatement("$T.out.println(\"Hello, World!\")", System.class)
				.beginControlFlow("for (int i=0; i<10; i++)")
				.beginControlFlow("for (j=0; j<10; j++)")
				.addStatement("$T.out.println(i*j)", System.class)
				.endControlFlow()
				.endControlFlow()
				.build();
		
		TypeSpec inner = TypeSpec.classBuilder("Inner")
				.addModifiers(Modifier.PROTECTED)
				.build();
		
		TypeSpec outer = TypeSpec.classBuilder("Main")
				.addModifiers(Modifier.PUBLIC)
				.addType(inner)
				.addMethod(main)
				.build();
		
		JavaFile javaFile = JavaFile.builder("com.example.test", outer)
				.build();
		
		javaFile.writeTo(System.out);
		
		String s = "";
		s = s + "Hello World";
		System.out.println(s);

	}


}
