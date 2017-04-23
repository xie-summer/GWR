package com.gewara.untrans;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import com.gewara.util.RelatedHelper;

public interface RelateService {

	Object getRelatedObject(String tag, Serializable relatedid);

	void addRelatedObject(int idx, String group, RelatedHelper rh, String tag, Collection<Serializable> idList);

	void addRelatedObject(int idx, String group, RelatedHelper rh, Map<Serializable, String> idTagMap);
	
	void addRelatedObject(int idx, String group, RelatedHelper rh, String tag, Serializable id);
}
