package com.gewara.service.gewapay;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gewara.command.InvoiceCommand;
import com.gewara.model.acl.GewaraUser;
import com.gewara.model.acl.User;
import com.gewara.model.pay.BaseOrderExtra;
import com.gewara.model.pay.Charge;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.user.Invoice;
import com.gewara.model.user.Member;
import com.gewara.service.OrderException;
import com.gewara.support.ErrorCode;

public interface InvoiceService {

	/**
	 * 根据条件查询发票申请记录
	 * @param memberid
	 * @return
	 */
	List<Invoice> getInvoiceList(Long memberid, String invoiceid, String contactor, String phone, String order, boolean isasc, Date fromDate, Date toDate, String pretype, int from, int maxnum);
	
	/**
	 * 查询数量
	 * @param memberid
	 * @return
	 */
	Integer getInvoiceCount(Long memberid, String invoiceid, String contactor, String phone, String order, Date fromDate, Date toDate, String pretype);
	/**
	 * 查询已开发票数额
	 * @param memberid
	 * @return
	 */
	Integer getAllTotalOpenedInvoiceByMemberid(Long memberid);
	/**
	 * 根据条件查询列表
	 * @param startAmount
	 * @param endAmount
	 * @param fromDate
	 * @param toDate
	 * @param citycode
	 * @param invoicestatus
	 * @param order
	 * @param applytype
	 * @param from
	 * @param maxnum
	 * @return
	 */
	List<Invoice> getInvoiceList(Integer startAmount, Integer endAmount,
			Date fromDate, Date toDate, String citycode, String invoicestatus,
			String order, boolean isasc, String applytype, String pretype, int from, int maxnum);
    
   /**
    * 根据条件查询数量
    * @param startAmount
    * @param endAmount
    * @param fromDate
    * @param toDate
    * @param citycode
    * @param invoicestatus
    * @param order
    * @param applytype
    * @param from
    * @param maxnum
    * @return
    */
	Integer getInvoiceCount(Integer startAmount, Integer endAmount,
			Date fromDate, Date toDate, String citycode, String invoicestatus,
			String order, String applytype, String pretype);

	/**
	 * 根据条件合并发票
	 * @param invoiceList
	 */
	void mergeInvoice(List<Invoice> invoiceList, GewaraUser user) throws OrderException;
	/**
	 * 用户已经申请过的发票关联的对象id
	 * @param memberid 用户或商家ID
	 * @return
	 */
	List<String> getOpenedRelatedidList(Long memberid);
	
	ErrorCode updateInvoiceTrash(Invoice invoice, User user);

	ErrorCode receiveInvoice(Member member, InvoiceCommand invoiceCommand) throws OrderException;
	
	ErrorCode receiveInvoice(User user, InvoiceCommand invoiceCommand) throws OrderException;
	
	Map<BaseOrderExtra,GewaOrder> validDataTradeNo(Long memberid, Set<String> tradeNoSet, Timestamp cur, final boolean checkValidtime) throws OrderException;
	List<Charge> validDataCharge(Long memberid, Set<String> chargeNoSet, Timestamp cur, final boolean checkValidtime) throws OrderException;
}
