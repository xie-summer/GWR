package com.gewara.xmlbind.gym;

import java.util.ArrayList;
import java.util.List;

import com.gewara.xmlbind.BaseObjectListResponse;

public class CardItemListResponse extends BaseObjectListResponse<CardItem> {
	private List<CardItem> cardList = new ArrayList<CardItem>();

	public List<CardItem> getCardList() {
		return cardList;
	}

	public void setCardList(List<CardItem> cardItemList) {
		this.cardList = cardItemList;
	}

	public void addCard(CardItem cardItem){
		this.cardList.add(cardItem);
	}
	
	@Override
	public List<CardItem> getObjectList() {
		return cardList;
	}
	
	
}
