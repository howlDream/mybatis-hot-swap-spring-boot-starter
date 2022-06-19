package org.hot.batis.janino;

import org.codehaus.commons.compiler.IScriptEvaluator;
import org.codehaus.janino.ExpressionEvaluator;
import org.codehaus.janino.JavaSourceClassLoader;
import org.codehaus.janino.ScriptEvaluator;
import org.codehaus.janino.util.resource.MapResourceFinder;
import org.codehaus.janino.util.resource.ResourceFinder;

import java.lang.reflect.InvocationTargetException;


public class EvaluatorTest {

    /**
     * 代码块执行
     */
    public static void scriptEvaluatorTest() {
        String content = "System.out.println(\"unstoppable ！！\");";
        ScriptEvaluator evaluator = new ScriptEvaluator();
        // 也可定义参数
        // evaluator.setParameters();
        try {
            // 这里处理（扫描，解析，编译和加载）上面定义的代码.
            evaluator.cook(content);
            // 输入参数计算运行
            evaluator.evaluate(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 带参表达式计算
     */
    public static void ExpressionEvaluatorTest() {
        try {
            String c = "a+b+1";
            ExpressionEvaluator  ee = new ExpressionEvaluator();
            ee.setParameters(new String[] {"a","b"},new Class[]{Integer.class,Integer.class});
            ee.setExpressionType(Integer.class);
            ee.cook(c);
            int result = (Integer) ee.evaluate(new Integer[]{1,3});
            System.out.println(result);
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 编译类
     */
    public void ClassScriptEvaluatorTest() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        // 类名不可带后缀
        String className = "org.hot.batis.janino.B";
        // 类源码内容
        String source = "package org.hot.batis.janino;"+
                        "import org.slf4j.*;"+
                        "public class B implements Runnable {"+
                            "Logger log = LoggerFactory.getLogger(this.getClass());"+
                            "public void run() {"+
                                "log.info(\"HELLO from {}\", this.getClass().getCanonicalName());"+
                            "}"+
                        "}";

        MapResourceFinder finder = new MapResourceFinder();
        finder.addResource(className.replace(".","/") + ".java",source);
        // 自定义类加载器
        JavaSourceClassLoader classLoader = new JavaSourceClassLoader(this.getClass().getClassLoader(),finder,"UTF-8");
        // 加载编译类
        Class<?> class1 = classLoader.loadClass(className);
        // 类源码实现了Runnable，获取类实例
        Runnable runnable = (Runnable)class1.getDeclaredConstructor().newInstance();
        runnable.run();
    }

    public static void main(String[] args) {
        scriptEvaluatorTest();
        ExpressionEvaluatorTest();
    }

}
