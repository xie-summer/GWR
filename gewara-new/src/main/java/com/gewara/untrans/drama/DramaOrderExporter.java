package com.gewara.untrans.drama;

import java.util.List;

import com.gewara.model.drama.DramaOrder;
import com.lowagie.text.Document;

public interface DramaOrderExporter {

	void getPdfDramaOrderDocument(Document document, List<DramaOrder> orderList);
}
