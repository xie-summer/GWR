package com.gewara.xmlbind;

import java.util.List;

public abstract class BaseObjectListResponse<T> extends BaseInnerResponse{
	public abstract List<T> getObjectList();
}
