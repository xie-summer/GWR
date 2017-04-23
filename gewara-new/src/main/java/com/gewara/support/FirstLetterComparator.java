/**
 * 
 */
package com.gewara.support;

import java.util.Comparator;

import org.apache.commons.lang.StringUtils;

import com.gewara.model.common.BaseEntity;
import com.gewara.util.PinYinUtils;
//°´ÕÕÊ××ÖÄ¸ÅÅÐò
public class FirstLetterComparator implements Comparator<BaseEntity>{
	public int compare(BaseEntity b1, BaseEntity b2) {
		String v1 = StringUtils.substring(PinYinUtils.getFirstSpell(b1.getRealBriefname()), 0, 1);
		String v2 = StringUtils.substring(PinYinUtils.getFirstSpell(b1.getRealBriefname()), 0, 1);
		if(v1==null && v2==null ) return 0;
		if(v1!=null && v2!=null ) return v1.compareTo(v2);
		return v1==null ? -1: 1;
	}
}
