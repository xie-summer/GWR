package com.gewara.helper.sys;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.script.Compilable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.gewara.util.StringUtil;

public class ScriptEngineUtil {
	private static Compilable engine;
	static{
		ScriptEngineManager manager = new ScriptEngineManager();
		engine = (Compilable) manager.getEngineByName("groovy");
	}
	private static Map<String, CachedScript> cacheMap = new LinkedHashMap<String, CachedScript>(500, 1, true){
        private static final long serialVersionUID = 387454821812532541L;
		protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > 500;
        }
    };
	public static synchronized CachedScript buildCachedScript(String script, boolean cached) {
		String key = StringUtil.md5(script);
		CachedScript cs = cacheMap.get(key);
		if(cs == null){
			cs = new CachedScript(engine,script);
			if(cached) {
				cacheMap.put(key, cs);
			}
		}
		return cs;
	}
	public static ScriptEngine getEngine() {
        return (ScriptEngine) engine;
    }
}
