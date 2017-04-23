package com.gewara.helper.sys;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import com.gewara.util.BeanUtil;

public class CachedScript implements Serializable {
	private static final long serialVersionUID = -5632756060640454376L;
	private CompiledScript compiledScript;

	public CachedScript() {
	}

	public CachedScript(Compilable scriptEngine, String script) {
		try {
			compiledScript = scriptEngine.compile(script);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public <T> ScriptResult<T> run(Map<String, Object> context){
		ScriptContext ctx = new SimpleScriptContext();
		if(context!=null){
			put(ctx, context);
		}
		ScriptResult result = new ScriptResult(ctx);
		try {
			Object retval = compiledScript.eval(ctx);
			result.setRetval(retval);
		} catch (ScriptException e) {
			result.setErrorMsg("execption:" + e + "," + e.getMessage());

		}
		return result;
	}

	private void put(ScriptContext ctx, Map<String, Object> context) {
		for (String k : context.keySet()) {
			//参数方法只读，不可更改
			Object value = context.get(k);
			if(value!=null && !BeanUtil.isSimpleProperty(value.getClass())){
				if(value instanceof Collection){
					ctx.setAttribute(k, BeanUtil.getBeanMapList((Collection) value, true), ScriptContext.ENGINE_SCOPE);
				}else{
					ctx.setAttribute(k, BeanUtil.getBeanMap(value), ScriptContext.ENGINE_SCOPE);
				}
			}else{
				ctx.setAttribute(k, value, ScriptContext.ENGINE_SCOPE);
			}
		}
	}
	public static class ScriptResult<T> {
		private ScriptContext ctx;
		private T retval;
		private String errorMsg;
		public ScriptResult(ScriptContext ctx){
			this.ctx = ctx;
		}
		public T getRetval() {
			return retval;
		}
		public Object getAttribute(String name){
			return ctx.getAttribute(name);
		}
		public void setRetval(T retval) {
			this.retval = retval;
		}
		public String getErrorMsg() {
			return errorMsg;
		}
		public void setErrorMsg(String errorMsg) {
			this.errorMsg = errorMsg;
		}
		public boolean hasError(){
			return this.errorMsg!=null;
		}
	}
	public CompiledScript getCompiledScript() {
		return compiledScript;
	}

}
