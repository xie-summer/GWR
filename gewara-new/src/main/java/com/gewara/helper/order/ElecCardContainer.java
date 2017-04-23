package com.gewara.helper.order;

import java.util.ArrayList;
import java.util.List;

import com.gewara.model.pay.Discount;
import com.gewara.model.pay.ElecCard;
import com.gewara.util.BeanUtil;

/**
 * @author gebiao(ge.biao@gewara.com)
 * @since Nov 30, 2013 8:11:01 PM
 */
public class ElecCardContainer {
	//ËùÓÐÈ¯
	private List<ElecCard> usedList = new ArrayList<ElecCard>();
	private List<ElecCard> avaliableList = new ArrayList<ElecCard>();
	private List<ElecCard> disableList = new ArrayList<ElecCard>();
	public ElecCardContainer(List<ElecCard> allCardList, List<Discount> discountList, ElecCardFilter filter){
		List<Long> usedidList = BeanUtil.getBeanPropertyList(discountList, Long.class, "relatedid", true);
		for(ElecCard card: allCardList){
			if(usedidList.contains(card.getId())){
				usedList.add(card);
			}else if(filter.available(card)){
				avaliableList.add(card);
			}else{
				disableList.add(card);
			}
		}
	}

	public List<ElecCard> getUsedList() {
		return usedList;
	}

	public List<ElecCard> getAvaliableList() {
		return avaliableList;
	}
	public void setAvaliableList(List<ElecCard> avaliableList) {
		this.avaliableList = avaliableList;
	}
	
	public List<ElecCard> getDisableList() {
		return disableList;
	}
	public void setDisableList(List<ElecCard> disableList) {
		this.disableList = disableList;
	}
	
}
