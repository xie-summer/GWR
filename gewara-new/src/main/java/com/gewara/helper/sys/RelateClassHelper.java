package com.gewara.helper.sys;

import java.util.HashMap;
import java.util.Map;

import com.gewara.model.BaseObject;
import com.gewara.model.agency.Agency;
import com.gewara.model.agency.TrainingGoods;
import com.gewara.model.api.ApiUser;
import com.gewara.model.bbs.Diary;
import com.gewara.model.bbs.DiaryComment;
import com.gewara.model.bbs.Moderator;
import com.gewara.model.bbs.commu.Commu;
import com.gewara.model.bbs.qa.GewaAnswer;
import com.gewara.model.bbs.qa.GewaQuestion;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.content.HeadInfo;
import com.gewara.model.content.News;
import com.gewara.model.content.Picture;
import com.gewara.model.content.Video;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaStar;
import com.gewara.model.drama.Theatre;
import com.gewara.model.draw.DrawActivity;
import com.gewara.model.goods.Goods;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.PubSale;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.sport.MemberCardType;
import com.gewara.model.sport.OpenTimeItem;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.SportItem;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.Agenda;
import com.gewara.model.user.Album;
import com.gewara.model.user.Friend;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;

public class RelateClassHelper {
	private static Map<String, Class<? extends BaseObject>> classMap = new HashMap<String, Class<? extends BaseObject>>();
	static{
		classMap.put("movie", Movie.class);
		classMap.put("cinema", Cinema.class);
		classMap.put("sport", Sport.class);
		classMap.put("sportservice", SportItem.class);
		classMap.put("news", News.class);
		classMap.put("video", Video.class);
		classMap.put("theatre", Theatre.class);
		classMap.put("dramastar", DramaStar.class);
		
		classMap.put("diary", Diary.class);
		classMap.put("topic", Diary.class);
		classMap.put("diarycomment", DiaryComment.class);
		classMap.put("gewaquestion", GewaQuestion.class);
		classMap.put("gewaanswer", GewaAnswer.class);
		classMap.put("commu", Commu.class);
		classMap.put("member", Member.class);
		classMap.put("ticket", TicketOrder.class);
		classMap.put("album", Album.class);
		classMap.put("friend", Friend.class);
		classMap.put("agenda", Agenda.class);
		classMap.put("goods", Goods.class);
		classMap.put("moderator", Moderator.class);
		classMap.put("drama", Drama.class);
		classMap.put("dramastar", DramaStar.class);
		classMap.put("headinfo", HeadInfo.class);
		classMap.put("memberInfo", MemberInfo.class);
		
		classMap.put("openplayitem", OpenPlayItem.class);
		classMap.put("ticketorder", TicketOrder.class);
		classMap.put("goodsorder", GoodsOrder.class);
		classMap.put("drawactivity",DrawActivity.class);
		classMap.put("partner",ApiUser.class);
		classMap.put("gymvideo", Video.class);
		
		classMap.put("gewacommend", GewaCommend.class);
		classMap.put("picture", Picture.class);
		classMap.put("sportitem", SportItem.class);
		classMap.put("pubsale", PubSale.class);
		
		classMap.put("agency", Agency.class);
		classMap.put("training", TrainingGoods.class);
		
		classMap.put("membercard", MemberCardType.class);
		classMap.put("opentimeitem", OpenTimeItem.class);
	}
	public static Class<? extends BaseObject> getRelateClazz(String tag) {
		return classMap.get(tag);
	}
}
