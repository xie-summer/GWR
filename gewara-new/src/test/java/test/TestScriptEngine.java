package test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.Compilable;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.apache.commons.lang.StringUtils;

import com.gewara.helper.sys.CachedScript;
import com.gewara.helper.sys.CachedScript.ScriptResult;
import com.gewara.helper.sys.ScriptEngineUtil;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.user.Member;
import com.gewara.util.BeanUtil;
import com.gewara.util.RandomUtils;

public class TestScriptEngine {
	/**
	 * @param args
	 * @throws Exception 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws Exception  {
		//test();
		testCachedScript();
		//testNotSafeThread();
		//testSafeThread();
		//testCachedScriptThread();
		//testVarScope();
	}
	public static void testCachedScript(){
		Map<String, Object> context = new HashMap<String, Object>();
		MoviePlayItem mpi = new MoviePlayItem();
		mpi.setLowest(70);
		context.put("mpi", mpi);
		String s = "mpi.lowest=66;a =mpi.lowest;";
		CachedScript script0 = ScriptEngineUtil.buildCachedScript(s, true);
		
		ScriptResult<Object> result = script0.run(context);
		
		System.out.println(mpi.getLowest());
		
		System.out.println(result.getRetval());
		
		List<Map> ss = new ArrayList<Map>();
		Map map = new HashMap();
		map.put("price", 2);
		ss.add(map);
		Map map2 = new HashMap();
		map2.put("price", 3);
		ss.add(map2);
		String sss = "int i= 0; for(map in ss){i+=map.price;}; i+=10;";
		
		context = new HashMap<String, Object>();
		context.put("ss", ss);
		script0 = ScriptEngineUtil.buildCachedScript(sss, true);
		result = script0.run(context);
		
		System.out.println(result.getErrorMsg() + "====>sss" + result.getRetval() + ",x:" + result.getAttribute("x"));
		//最后一条语句作为计算结果返回
		context = new HashMap<String, Object>();
		context.put("a", 10);
		context.put("b", 100);
		s = "x=b; a>b || a*a==b;";
		script0 = ScriptEngineUtil.buildCachedScript(s, true);
		result = script0.run(context);
		System.out.println(result.getErrorMsg() + "====>" + result.getRetval() + ",x:" + result.getAttribute("x"));
		s += "x = a; \n" +
			 "y = b; \n";
		CachedScript script = ScriptEngineUtil.buildCachedScript(s, true);
		result = script.run(context);
		System.out.println(result.getErrorMsg() + "====>" + result.getRetval() + ",x:" + result.getAttribute("x"));
		//测试之前的是否受影响
		result = script0.run(context);
		System.out.println(result.getErrorMsg() + "====>" + result.getRetval() + ",x:" + result.getAttribute("x"));
		
		
		//此处调用debug，查看scriptEngine里有多少CompiledScript
		ScriptEngine engine = script.getCompiledScript().getEngine();
		System.out.println("engine:" + BeanUtil.buildString(engine, true));
		System.out.println("context:" + BeanUtil.buildString(engine.getContext(), true));
		System.out.println("binding:" + new LinkedHashMap(engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE)));
		s += "x+y;\n";
		script = ScriptEngineUtil.buildCachedScript(s, true);
		result = script.run(context);
		System.out.println(result.getErrorMsg() + "====>" + result.getRetval() + ",x:" + result.getAttribute("x"));
				
		s +="abc='a';\n";
		script = ScriptEngineUtil.buildCachedScript(s, true);
		result = script.run(context);
		System.out.println(result.getErrorMsg() + "====>" + result.getRetval() + ",x:" + result.getAttribute("x"));
		
		s +="if(a<b) abc+'xyz';else a*b; \n";
		script = ScriptEngineUtil.buildCachedScript(s, true);
		result = script.run(context);
		System.out.println(result.getErrorMsg() + "====>" + result.getRetval() + ",x:" + result.getAttribute("x"));
		
		s += "true;\n";
		script = ScriptEngineUtil.buildCachedScript(s, true);
		result = script.run(context);
		System.out.println(result.getErrorMsg() + "====>" + result.getRetval() + ",x:" + result.getAttribute("x"));

		
		s += "null;";
		script = ScriptEngineUtil.buildCachedScript(s, true);
		result = script.run(context);
		System.out.println(result.getErrorMsg() + "====>" + (result.getRetval()==null?"NULL":result.getRetval()) + ",x:" + result.getAttribute("x"));

		s += "xyz;";
		script = ScriptEngineUtil.buildCachedScript(s, true);
		result = script.run(context);
		System.out.println(result.getErrorMsg() + "====>" + result.getRetval() + ",x:" + result.getAttribute("x"));

	}
	public static void testNotSafeThread(){
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("js");
		CyclicBarrier barrier = new CyclicBarrier(31);
		for(int i=0; i<30; i++){
			new NotThreadSafeWorker(barrier, engine, i, i*10).start();
		}
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		new NotThreadSafeWorker(barrier, engine, 100, 1000).start();
		
	}
	public static void testCachedScriptThread(){
		CyclicBarrier barrier = new CyclicBarrier(31);
		for(int i=0; i<30; i++){
			new CachedWorker(barrier, i, i*10).start();
		}
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		new CachedWorker(barrier, 100, 1000).start();
	}
	public static void testSafeThread(){
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("js");
		CyclicBarrier barrier = new CyclicBarrier(31);
		for(int i=0; i<30; i++){
			new ThreadSafeWorker(barrier, engine, i, i*10).start();
		}
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		new ThreadSafeWorker(barrier, engine, 100, 1000).start();
	}
	public abstract static class Worker extends Thread{
		protected String name;
		protected ScriptEngine engine;
		protected CyclicBarrier barrier;
		protected Integer a;
		protected Integer b;
		public Worker(CyclicBarrier barrier, Integer a, Integer b){
			this.name = a + " + " + b;
			this.barrier = barrier;
			this.a = a;
			this.b = b;
		}
		public Worker(CyclicBarrier barrier, ScriptEngine engine, Integer a, Integer b){
			this.name = a + " + " + b;
			this.barrier = barrier;
			this.a = a;
			this.b = b;
			this.engine = engine;
		}
		public void run(){
			try {
				barrier.await();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			} catch (BrokenBarrierException e1) {
				e1.printStackTrace();
			}
			runInternal();
		}
		public abstract void runInternal();
	}
	public static class NotThreadSafeWorker extends Worker{
		public NotThreadSafeWorker(CyclicBarrier barrier, ScriptEngine engine, Integer a, Integer b) {
			super(barrier, engine, a, b);
		}

		public void runInternal(){
			engine.put("a", a);
			try {
				Thread.sleep(1000 + RandomUtils.randomInt(3000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			engine.put("b", b);
			try {
				Thread.sleep(RandomUtils.randomInt(3000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Object result = null;
			try {
				result = engine.eval("a + b");
			} catch (ScriptException e) {
				e.printStackTrace();
			}
			System.out.println(name + " = " + result);
		}
	}
	public static class CachedWorker extends Worker{
		private CachedScript script;
		public CachedWorker(CyclicBarrier barrier, Integer a, Integer b) {
			super(barrier, a, b);
			script = ScriptEngineUtil.buildCachedScript("a + b", true);
			
		}

		@Override
		public void runInternal() {
			Map<String, Object> context = new HashMap<String, Object>();
			context.put("a", a);
			context.put("b", b);
			Object result = script.run(context);
			try {
				Thread.sleep(RandomUtils.randomInt(3000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(name + " = " + result);
		}
	}
	public static void testVarScope(){
		CyclicBarrier barrier = new CyclicBarrier(31);
		for(int i=0; i<30; i++){
			new VarWorker(barrier, i, i*10).start();
		}
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		new VarWorker(barrier, 100, 1000).start();
	}
	public static class VarWorker extends Worker{
		//测试变量作用域
		private CachedScript script;
		public VarWorker(CyclicBarrier barrier, Integer a, Integer b) {
			super(barrier, a, b);
			script = ScriptEngineUtil.buildCachedScript("function test(){x = a;y=b; return x+y;}test();", true);
			
		}

		@Override
		public void runInternal() {
			Map<String, Object> context = new HashMap<String, Object>();
			context.put("a", a);
			context.put("b", b);
			ScriptResult<Object> result = script.run(context);
			try {
				Thread.sleep(RandomUtils.randomInt(3000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			String s = name + "=" + result.getRetval() + ", " + result.getAttribute("x") + " + ";
			try {
				Thread.sleep(RandomUtils.randomInt(3000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			s += result.getAttribute("y");
			System.out.println(s);
		}
	}
	public static class ThreadSafeWorker extends Worker{
		public ThreadSafeWorker(CyclicBarrier barrier, ScriptEngine engine, Integer a, Integer b) {
			super(barrier, engine, a, b);
		}

		public void runInternal(){
			ScriptContext context = new SimpleScriptContext();
			context.setAttribute("a", a, ScriptContext.ENGINE_SCOPE);
			try {
				Thread.sleep(1000 + RandomUtils.randomInt(3000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			context.setAttribute("b", b, ScriptContext.ENGINE_SCOPE);
			try {
				Thread.sleep(RandomUtils.randomInt(3000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Object result = null;
			try {
				result = engine.eval("a + b", context);
			} catch (ScriptException e) {
				e.printStackTrace();
			}
			System.out.println(name + " = " + result);
		}
	}
	public static void test() throws Exception{
		ScriptEngineManager manager = new ScriptEngineManager();
		/*
	    SandboxContextFactory contextFactory = new SandboxContextFactory();
	    Context cx = contextFactory.enter();
	    try {
	        ScriptableObject prototype = cx.initStandardObjects();
	        //prototype.setParentScope(null);
	        Scriptable scope = cx.newObject(prototype);
	        scope.setPrototype(prototype);
	        String s = "function test2(){" +
	        		"var file = new java.io.File('E:/bookstore.xml');" +
	        		"var name = file.getName();" +
	        		"return name;" + 
	        		"}";
	        Object result = cx.evaluateString(scope, s, null, 1, null);
	        System.out.println(result);
	        // do what you want within the scope
	    } finally {
	    	Context.exit();
	    }*/
		
	    ScriptEngine engine = manager.getEngineByName("js");
        System.out.println(engine.getFactory().getParameter("THREADING"));

        //context = new SimpleScriptContext();
        engine.put("a", 10);
        engine.put("b", 20);
        
        Member member = new Member();
        member.setMobile("test1111");
        engine.put("member", member);
        engine.put("StringUtils", new StringUtils());
        
        //engine.eval("java = undefined;Packages = undefined;org = undefined;");
        //engine.eval("java = undefined;Packages = undefined;org = undefined;");
        
        System.out.println(engine.eval("member.mobile"));
        String fn1 = "function test(xx){" +
        		"var x = 10;" +
        		"var y = 20;" +
        		"return x + y;" +
        		"}";
        engine.eval(fn1);        		
        String fn2 = "function test2(){" +
        		"var file = new java.io.File('E:/bookstore.xml');" +
        		"var name = file.getName();" +
        		"return name;" + 
        		"}";

        CachedScript cached = new CachedScript((Compilable) engine, fn1 + ";test();");
        ScriptResult<String> result = cached.run(null);
        System.out.println("cached1:" + result.getErrorMsg() + "," + result.getRetval());

        cached = new CachedScript((Compilable) engine, fn2 + ";test2();");
        result = cached.run(null);
        System.out.println("cached2:" + result.getErrorMsg() + "," + result.getRetval());

        //engine.eval(fn2);
        engine.eval("function test3(){" +
        		"var x = 10;" +
        		"var y = 20;" +
        		"return StringUtils.repeat('aaa', 10)+(x - y);" +
        		"}");
        
        System.out.println(((Invocable)engine).invokeFunction("test", new Test()));
        System.out.println(((Invocable)engine).invokeFunction("test3"));
        //System.out.println(((Invocable)engine).invokeFunction("test2"));
        
        String formula = "(23+a)*2+b*3+(a-b<0?1000:2000)";
        Object s = engine.eval(formula);
        System.out.println(s);
        
        CachedScript f2 = new CachedScript((Compilable) engine, formula);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("a", 50);
        map.put("b", 60);

        Object result1 = f2.run(map);
        System.out.println("formula:" + result1);
	}
	
	public static void regMatch(String src, Pattern pattern) {
		Matcher matcher = pattern.matcher(src);
		if(matcher.find()){
			System.out.println(src);	
			String result = matcher.replaceAll("$1,$2,$3,$4,$5,$6");
			System.out.println(result);
		}
	}
	static class Test{
		public int getFile(){
			new java.io.File("E:/card.txt").delete();
			return 10;
		}
	}

}
