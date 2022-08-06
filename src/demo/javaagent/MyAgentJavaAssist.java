package demo.javaagent;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;

public class MyAgentJavaAssist {
	public static void premain(String args, Instrumentation inst) {
		// args 是命令行的入参
		inst.addTransformer(new Transformer(args));
	}

	private static class Transformer implements ClassFileTransformer {

		public Transformer(String args) {
		}

		@Override
		public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
				ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
			try {
				ClassPool pool = ClassPool.getDefault();
				ByteArrayInputStream in = new ByteArrayInputStream(classfileBuffer);
				CtClass cc = pool.makeClass(in);
				// 判断有无声明 toString() 方法，没有的话就生成一个
				try {
					cc.getDeclaredMethod("toString", new CtClass[] {});
				} catch (NotFoundException e) {
					CtMethod cm = new CtMethod(pool.getCtClass("java.lang.String"), "toString", new CtClass[] {}, cc);
					StringBuilder sBody = new StringBuilder();
					sBody.append("{StringBuilder sb =new StringBuilder(\"").append(cc.getSimpleName()).append("(\");");
					CtField[] cfs = cc.getDeclaredFields();
					for (CtField cf : cfs) {
						sBody.append("sb.append(\"").append(cf.getName()).append("\").append(\"=\").append(")
								.append(cf.getName()).append(").append(\",\");");
					}
					sBody.append("sb.append(\")\");").append("return sb.toString();}");
					cm.setBody(sBody.toString());
					cc.addMethod(cm);
				}
				// 判断是否是 demo/Test， 是的话修改 main方法
				if (className.equals("demo/Test")) {
					CtMethod cm = cc.getDeclaredMethod("main",
							new CtClass[] { pool.getCtClass("[Ljava.lang.String;") });
					cm.setBody("{System.out.println(new demo.Test());sayHello(\"修改过后的java\");}");
				}
				return cc.toBytecode();
			} catch (Throwable t) {
				t.printStackTrace();
				return classfileBuffer;
			}

		}
	}
}