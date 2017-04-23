package com.gewara.untrans;

import com.gewara.model.acl.GewaraUser;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.movie.Cinema;
import com.gewara.model.pay.GymOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.Member;
import com.gewara.model.user.SysMessageAction;
import com.gewara.xmlbind.gym.CardItem;

public interface GewaMailService {

	void sendTicketOrderEmail(TicketOrder order, OpenPlayItem opi);
	void sendTicketOrderEmail(Member member, TicketOrder order, Cinema cinema);

	void sendDramaOrderEmail(DramaOrder order);

	void sendGymOrderEmail(GymOrder order, CardItem gymCardItem);

	SysMessageAction sendTemplateHtmlSysMessageAction(String body, Long memberid, Long actionid);

	void sendRegEmail(Member member);
	void sendChangeEmail(Member member, String newEmail);
	void sendSecurityEmail(Member member, String email);
	void sendRemoveMobileEmail(Member member, String mobile);
	void sendGetPasswordMail(String nickname, Long memberid, String email, String uuid);
	void sendValidateEmail(Member member, String uuid);
	void sendSeniorRecognitionEmail(String nickname, Long memberid, String email);
	void sendModifyCinemaMail(GewaraUser user, Long relatedid, String url, String msg);
	void sendAdviseEmail(String membername, String body, String email);
	void sendCardWarnEmail(String nickname, String email, String count);


}
