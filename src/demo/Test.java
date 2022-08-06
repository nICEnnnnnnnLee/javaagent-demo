package demo;

public class Test{

	String aa = "value-aa";
	String bb = "value-bb";

	public String toString() {
		StringBuilder sb =new StringBuilder("Test(");
		sb.append("aa").append("=").append(aa).append(",");
		sb.append("bb").append("=").append(bb).append(",");
		sb.append(")");
		return sb.toString();
	}

	public static void main(String[] args) {
		System.out.println(new Test());
		sayHello("java");
//		sayHello("修改过后的java");  // resouces编译的是这个
	}

	public static void sayHello(String name) {
		System.out.println("Hello, " + name);
	}
}
