package demo.javaagent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class MyAgent {
	public static void premain(String args, Instrumentation inst) {
		// args 是命令行的入参
		inst.addTransformer(new Transformer(args));
	}

	private static class Transformer implements ClassFileTransformer {

//		String pkg;

		public Transformer(String pkg) {
//			this.pkg = pkg.replace(".", "/");
		}

		static byte[] readAll(InputStream in) throws IOException {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			for (int len = 0; (len = in.read(buffer)) != -1;) {
				out.write(buffer, 0, len);
			}
			in.close();
			return out.toByteArray();
		}

		@Override
		public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
				ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
			//System.out.println(className);
			// 转换指定包名开头的类
			if (className.equals("demo/Test")) {
				try {
					System.out.println("demo/test被修改");
					return readAll(MyAgent.class.getResourceAsStream("/resources/Test.class.modified"));
				} catch (Throwable t) {
					t.printStackTrace();
					return classfileBuffer;
				}
			} else {
				return classfileBuffer;
			}

		}
	}
}