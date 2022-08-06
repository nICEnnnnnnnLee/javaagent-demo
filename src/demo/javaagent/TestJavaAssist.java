package demo.javaagent;

import java.lang.reflect.Method;

import demo.Test;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;

public class TestJavaAssist {

	public static void main(String[] a) {
		ClassPool pool = ClassPool.getDefault();
		// 去掉下面注释, 使用ct.toClass()会报错，因为同一个ClassLoader不能重复加载相同的类两遍
		System.out.println(new Test());
		try {
			CtClass ct = pool.get("demo.Test");
			System.out.println(ct.getField("aa").getSignature());
			// 遍历声明的Filed，生成toString()方法的body
			CtField[] cfs = ct.getDeclaredFields();
			StringBuilder sBody = new StringBuilder();
			sBody.append("{StringBuilder sb =new StringBuilder(\"").append(ct.getName()).append("(\");");
			for (CtField cf : cfs) {
				sBody.append("sb.append(\"").append(cf.getName()).append("\").append(\"=\").append(")
						.append(cf.getName()).append(").append(\",\");");
			}
			sBody.append("sb.append(\")\");").append("return sb.toString();}");
			CtMethod cm = null;
			boolean isDeclared = true;
			try {
				// 修改 String toString() 方法
				cm = ct.getDeclaredMethod("toString", new CtClass[] {});
			} catch (Exception e) {
				// 添加 String toString() 方法
				cm = new CtMethod(pool.getCtClass("java.lang.String"), "toString", new CtClass[] {}, ct);
				isDeclared = false;
			}
			cm.setBody(sBody.toString());
			if (!isDeclared)
				ct.addMethod(cm);

			{
				// 添加注解
				ConstPool cp = ct.getClassFile().getConstPool();
				AnnotationsAttribute attr = new AnnotationsAttribute(cp, AnnotationsAttribute.visibleTag);
				Annotation anno = new Annotation("demo.javaagent.Option", cp);
				attr.addAnnotation(anno);
				cm.getMethodInfo().addAttribute(attr);
			}

			{
				ct.debugWriteFile(".");
			}
			Class<?> aClass = null;
			try {
				aClass = ct.toClass();
			} catch (Exception e) {
				aClass = ct.toClass(new ClassLoader() {
				}, null);
			}
			Object t = aClass.newInstance();
			Method m = aClass.getMethod("toString");
			System.out.println(m.invoke(t));
			System.out.println(t);
			System.out.println(new Test());
			System.out.println("toString()的注解数量: " + Test.class.getMethod("toString").getAnnotations().length);

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
