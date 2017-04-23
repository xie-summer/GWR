/** 
 */
package com.gewara.untrans;

import java.util.Map;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Jun 3, 2013  4:20:48 PM
 */
public interface PlayItemRefreshService {
	void clearOrderedPageCache(Map<String, String> params, String citycode);
}
