package com.gewara.service;

import java.util.List;

import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.goods.TicketGoods;
import com.gewara.model.machine.Barcode;
import com.gewara.model.pay.OrderNote;
import com.gewara.support.ErrorCode;

public interface BarcodeService {
	List<Barcode> getBarcodeList(String barcode, Long relatedid, Long placeid, Long itemid, String tradeno, int from, int maxnum);
	Integer getBarcodeCount(String barcode, Long relatedid, Long placeid, Long itemid, String tradeno);
	List<Barcode> getFreeBarcodeList(Long relatedid, Long placeid, Long itemid, int from, int maxnum);
	List<Barcode> getSynchNewBarcodeList(Long placeid, int from, int maxnum);
	Integer getFreeBarcodeCount(Long relatedid, Long placeid, Long itemid);
	Integer getBarcodeCountByTradeno(String tradeno);
	List<Barcode> getBarcodeListByTradeno(String tradeno);
	Integer createNewBarcodeByPlaceid(Long placeid);
	ErrorCode<String> createBarcodeList(OrderNote orderNote, TicketGoods goods);
	ErrorCode<String> createBarcodeList(OrderNote orderNote, OpenDramaItem odi);
	Integer handCreateNewBarcodeByPlaceid(Long placeid);
}
