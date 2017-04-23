package com.gewara.job.impl;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.gewara.constant.Status;
import com.gewara.job.JobService;
import com.gewara.model.agency.TrainingGoods;
import com.gewara.model.goods.GoodsPrice;
import com.gewara.service.DaoService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.untrans.monitor.MonitorService.EXCEPTION_TAG;
import com.gewara.util.DateUtil;

public class GoodsPriceJobImpl extends JobService{
	@Autowired@Qualifier("monitorService")
	private MonitorService monitorService;
	
	@Autowired@Qualifier("hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	
	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	
	public void updateTrainingGoodsAllownum(){
		String hql = "from GoodsPrice g where g.status<>? and g.quantity>g.sellquantity and exists(select t.id from TrainingGoods t where t.id=g.goodsid and t.status = ? and t.fromtime<? and t.totime>? and t.quantity>t.sales)";
		Timestamp cur = DateUtil.getCurFullTimestamp();
		try{
			dbLogger.warn("update TrainingGoods goodsPrice allownum start....");
			List<GoodsPrice> priceList = hibernateTemplate.find(hql, Status.DEL, Status.Y, cur, cur);
			int count = 0;
			for (GoodsPrice price : priceList) {
				int allownum = price.getQuantity() - price.getSellquantity();
				if(allownum < 0) continue;
				int tmp = price.getAllowaddnum();
				price.setAllowaddnum(allownum);
				daoService.saveObject(price);
				dbLogger.warn("priceid:" + price.getId() + ",old allownum:" + tmp + " ----> " + "new allownum:" + allownum);
				count ++;
			}
			dbLogger.warn("update TrainingGoods goodsPrice allownum end, count :" + count);
		}catch(Exception e){
			monitorService.logException(EXCEPTION_TAG.JOB, "updateTrainingGoodsAllownum1", "GoodsPriceJobImpl", e, null);
			dbLogger.warn("update TrainingGoods goodsPrice", e);
		}
		hql = "from TrainingGoods t where t.quantity>t.sales and t.status = ? and t.fromtime<? and t.totime>?";
		try{
			dbLogger.warn("update TrainingGoods allownum start....");
			List<TrainingGoods> trainingGoodsList = hibernateTemplate.find(hql, Status.Y, cur, cur);
			int count = 0;
			for (TrainingGoods trainingGoods : trainingGoodsList) {
				int allownum = trainingGoods.getQuantity() - trainingGoods.getSales();
				if(allownum < 0) continue;
				int tmp = trainingGoods.getAllowaddnum();
				trainingGoods.setAllowaddnum(allownum);
				daoService.saveObject(trainingGoods);
				dbLogger.warn("goodsid:" + trainingGoods.getId() + ",old allownum:" + tmp + " ----> " + "new allownum:" + allownum);
				count ++;
			}
			dbLogger.warn("update TrainingGoods allownum end, count :" + count);
		}catch(Exception e){
			monitorService.logException(EXCEPTION_TAG.JOB, "updateTrainingGoodsAllownum2", "GoodsPriceJobImpl", e, null);
			dbLogger.warn("update TrainingGoods", e);
		}
	}
}
