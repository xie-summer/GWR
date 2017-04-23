package com.gewara.untrans;

import com.gewara.bank.SpsdoOrderQry;
import com.gewara.model.pay.GewaOrder;
import com.gewara.xmlbind.pay.QrySandOrder;


public interface PartnerWebService{
	//QryMSNResponse qryMSNContact(String account, String password);
	String qrySpsdoOrder(GewaOrder order);
	SpsdoOrderQry qrySpsdoOrder(String response);
	QrySandOrder qrySandOrder(GewaOrder order);
	boolean pushOrderToUnionpayWallet(String transSeq,String cdhdUsrId,String billId,int num);
}
