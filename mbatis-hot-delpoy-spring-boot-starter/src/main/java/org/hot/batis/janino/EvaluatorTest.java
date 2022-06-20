package org.hot.batis.janino;

import org.codehaus.commons.compiler.IScriptEvaluator;
import org.codehaus.commons.compiler.ISimpleCompiler;
import org.codehaus.janino.*;
import org.codehaus.janino.util.ResourceFinderClassLoader;
import org.codehaus.janino.util.resource.MapResourceCreator;
import org.codehaus.janino.util.resource.MapResourceFinder;
import org.codehaus.janino.util.resource.ResourceFinder;
import org.codehaus.janino.util.resource.StringResource;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;


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


//    /**
//     * 另一种写法（不知哪个版本）
//     * @param source
//     * @param className
//     * @param <T>
//     * @return
//     */
//    public <T> T classCompileTest(String source,String className) {
//        try {
//            String fileName =  className.replace(".","/") + ".java";
//
//            CompilerFactory compilerFactory = new CompilerFactory();
//            ICompiler compiler = compilerFactory.newCompiler();
//            Map<String, byte[]> classes = new HashMap();
//            compiler.setClassFileCreator(new MapResourceCreator(classes));
//            compiler.setDebugLines(true);
//            compiler.setDebugSource(true);
//            compiler.setDebugVars(true);
//            compiler.compile(new StringResource[]{new StringResource(fileName, source)});
//            ClassLoader cl = new ResourceFinderClassLoader(new MapResourceFinder(classes), this.getClass().getClassLoader());
//            Class c = cl.loadClass(className);
//            T instance = (T) c.newInstance();
//            return instance;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    public static void main(String[] args) {
        scriptEvaluatorTest();
        ExpressionEvaluatorTest();
    }

}
