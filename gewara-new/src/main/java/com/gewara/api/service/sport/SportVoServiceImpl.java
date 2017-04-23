package com.gewara.api.service.sport;

import com.gewara.api.vo.ResultCode;
import com.gewara.api.vo.sport.SportVo;
import com.gewara.model.sport.Sport;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.util.VoCopyUtil;

public class SportVoServiceImpl  extends BaseServiceImpl implements SportVoService{

	@Override
	public ResultCode<SportVo> getSportVoById(Long sportid) {
		Sport sport = baseDao.getObject(Sport.class, sportid);
		return VoCopyUtil.copyProperties(SportVo.class, sport);
	}

}
