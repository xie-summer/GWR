package com.gewara.web.action.subject.gewacup;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import com.gewara.json.gewacup.ClubInfo;
import com.gewara.json.gewacup.MiddleTable;
import com.gewara.json.gewacup.Players;

@Service("gewaCupApiService")
public class GewaCupApiController extends BaseGewaCupController {

	@Override
	// 保存俱乐部信息
	public void saveClubInfo(ClubInfo club, String yearstype) {
		super.saveClubInfo(club, yearstype);
	}

	@Override
	// 保存参赛人信息
	public void savePlayers(List<Players> players, String type, String yearstype, String source) {
		super.savePlayers(players, type, yearstype, source);
	}

	@Override
	// 判断身份证是否重复
	public boolean getIdcards(String idcards, String type, String yearstype) {
		return super.getIdcards(idcards, type, yearstype);
	}

	@Override
	public Integer getObjectCountbyPropertyList(String namespace, Object propertyvalue, String... propertyname) {
		return super.getObjectCountbyPropertyList(namespace, propertyvalue, propertyname);
	}

	@Override
	public void getClubPlayersInfo(List<ClubInfo> clubList, String yearstype, ModelMap model) {
		super.getClubPlayersInfo(clubList, yearstype, model);
	}

	@Override
	public List<Map> getPersonalPlayersInfo(List<MiddleTable> midList) {
		return super.getPersonalPlayersInfo(midList);
	}

	@Override
	public boolean deletePlayers(String mid) {
		return super.deletePlayers(mid);
	}

	@Override
	public String getTime(String tag, String type) {
		return super.getTime(tag, type);
	}

}
