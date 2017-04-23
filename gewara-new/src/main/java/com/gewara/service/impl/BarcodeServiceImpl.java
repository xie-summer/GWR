package com.gewara.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.goods.TicketGoods;
import com.gewara.model.machine.Barcode;
import com.gewara.model.pay.OrderNote;
import com.gewara.service.BarcodeService;
import com.gewara.support.ErrorCode;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
@Service("barcodeService")
public class BarcodeServiceImpl  extends BaseServiceImpl implements BarcodeService{
	@Override
	public List<Barcode> getBarcodeList(String barcode, Long relatedid, Long placeid, Long itemid, String tradeno, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(Barcode.class);
		if(relatedid!=null){
			query.add(Restrictions.eq("relatedid", relatedid));
		}
		if(placeid!=null){
			query.add(Restrictions.eq("placeid", placeid));
		}
		if(itemid!=null){
			query.add(Restrictions.eq("itemid", itemid));
		}
		if(StringUtils.isNotBlank(tradeno)){
			query.add(Restrictions.eq("tradeno", tradeno));
		}
		if(StringUtils.isNotBlank(barcode)){
			query.add(Restrictions.eq("barcode", barcode));
		}
		query.addOrder(Order.desc("addtime"));
		List<Barcode> barcodeList = hibernateTemplate.findByCriteria(query, from, maxnum);
		return barcodeList;
	}
	@Override
	public Integer getBarcodeCount(String barcode, Long relatedid, Long placeid, Long itemid, String tradeno) {
		DetachedCriteria query = DetachedCriteria.forClass(Barcode.class);
		if(relatedid!=null){
			query.add(Restrictions.eq("relatedid", relatedid));
		}
		if(placeid!=null){
			query.add(Restrictions.eq("placeid", placeid));
		}
		if(itemid!=null){
			query.add(Restrictions.eq("itemid", itemid));
		}
		if(StringUtils.isNotBlank(tradeno)){
			query.add(Restrictions.eq("tradeno", tradeno));
		}
		if(StringUtils.isNotBlank(barcode)){
			query.add(Restrictions.eq("barcode", barcode));
		}
		query.setProjection(Projections.rowCount());
		List result = hibernateTemplate.findByCriteria(query);
		return Integer.valueOf(result.get(0)+"");
	}
	@Override
	public List<Barcode> getFreeBarcodeList(Long relatedid, Long placeid, Long itemid, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(Barcode.class);
		if(relatedid!=null){
			query.add(Restrictions.eq("relatedid", relatedid));
		}
		if(placeid!=null){
			query.add(Restrictions.eq("placeid", placeid));
		}
		if(itemid!=null){
			query.add(Restrictions.eq("itemid", itemid));
		}
		query.add(Restrictions.isNull("tradeno"));
		query.addOrder(Order.desc("addtime"));
		List<Barcode> barcodeList = hibernateTemplate.findByCriteria(query, from, maxnum);
		return barcodeList;
	}
	@Override
	public List<Barcode> getSynchNewBarcodeList(Long placeid, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(Barcode.class);
		query.add(Restrictions.eq("placeid", placeid));
		query.add(Restrictions.isNull("taketime"));
		query.add(Restrictions.ge("validtime", DateUtil.getMillTimestamp()));
		query.addOrder(Order.desc("addtime"));
		List<Barcode> barcodeList = hibernateTemplate.findByCriteria(query, from, maxnum);
		return barcodeList;
	}
	@Override
	public Integer getFreeBarcodeCount(Long relatedid, Long placeid, Long itemid) {
		DetachedCriteria query = DetachedCriteria.forClass(Barcode.class);
		if(relatedid!=null){
			query.add(Restrictions.eq("relatedid", relatedid));
		}
		if(placeid!=null){
			query.add(Restrictions.eq("placeid", placeid));
		}
		if(itemid!=null){
			query.add(Restrictions.eq("itemid", itemid));
		}
		query.add(Restrictions.isNull("tradeno"));
		query.setProjection(Projections.rowCount());
		List result = hibernateTemplate.findByCriteria(query);
		return Integer.valueOf(result.get(0)+"");
	}
	@Override
	public List<Barcode> getBarcodeListByTradeno(String tradeno) {
		DetachedCriteria query = DetachedCriteria.forClass(Barcode.class);
		query.add(Restrictions.eq("tradeno", tradeno));
		List<Barcode> barcodeList = hibernateTemplate.findByCriteria(query);
		return barcodeList;
	}
	@Override
	public Integer getBarcodeCountByTradeno(String tradeno) {
		DetachedCriteria query = DetachedCriteria.forClass(Barcode.class);
		query.add(Restrictions.eq("tradeno", tradeno));
		query.setProjection(Projections.rowCount());
		List result = hibernateTemplate.findByCriteria(query);
		return Integer.valueOf(result.get(0)+"");
	}
	
	@Override
	public Integer createNewBarcodeByPlaceid(Long placeid){
		return createNewBarcodeByPlaceid(placeid, Barcode.BARCODE_MAXNUM);
	}
	@Override
	public Integer handCreateNewBarcodeByPlaceid(Long placeid){
		return createNewBarcodeByPlaceid(placeid, Barcode.BARCODE_HANDMAXNUM);
	}
	private Integer createNewBarcodeByPlaceid(Long placeid, int maxnum){
		int count = getFreeBarcodeCount(null, placeid, null);
		int r = 0;
		if(count<maxnum){
			List<Barcode> barcodeList = new ArrayList<Barcode>();
			for(int i=1;i<=(maxnum - count);i++){
				String barcode = getRandomNum();
				Barcode code = new Barcode(placeid);
				code.setBarcode(barcode);
				barcodeList.add(code);
			}
			baseDao.saveObjectList(barcodeList);
			r = barcodeList.size();
		}
		return r;
	}
	@Override
	public ErrorCode<String> createBarcodeList(OrderNote orderNote, TicketGoods goods){
		return createBarcodeList(orderNote, goods.isOpenBarcode());
	}
	@Override
	public ErrorCode<String> createBarcodeList(OrderNote orderNote, OpenDramaItem odi){
		return createBarcodeList(orderNote, odi.isOpenBarcode());
	}
	private ErrorCode<String> createBarcodeList(OrderNote orderNote, boolean isOpenBarcode){
		if(!isOpenBarcode) return ErrorCode.getFailure("开放类型不正确！");
		Map<String, String> map = JsonUtils.readJsonToMap(orderNote.getDescription());
		if(map.containsKey("barcode") && StringUtils.isNotBlank(map.get("barcode"))){
			return ErrorCode.getFailure("条形码已经生产，不能重新生产！");
		}
		int ticketnum = orderNote.getTicketnum();
		Long placeid = orderNote.getPlaceid();
		List<Barcode> barcodeList = getFreeBarcodeList(null, placeid, null, 0, ticketnum);
		if(barcodeList.size()<ticketnum){
			for(int i=1;i<=(Barcode.BARCODE_MAXNUM - barcodeList.size());i++){
				String barcode = getRandomNum();
				Barcode code = new Barcode(placeid);
				code.setBarcode(barcode);
				barcodeList.add(code);
			}
			baseDao.saveObjectList(barcodeList);
		}
		barcodeList = BeanUtil.getSubList(barcodeList, 0, ticketnum);
		for(Barcode barcode : barcodeList){
			barcode.setSerialno(orderNote.getSerialno());
			barcode.setTradeno(orderNote.getTradeno());
		}
		baseDao.saveObjectList(barcodeList);
		List<String> codesList = BeanUtil.getBeanPropertyList(barcodeList, String.class, "barcode", false);
		return ErrorCode.getSuccessReturn(StringUtils.join(codesList, ","));
	}
	private static String getRandomNum(){
		Random random=new Random();
		String x = "";
		for(int i = 0;i<12;i++){
			int tmp= random.nextInt(10);
			x = x+tmp;
		}
		return x;
	}
}
