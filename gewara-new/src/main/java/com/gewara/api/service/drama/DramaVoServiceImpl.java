package com.gewara.api.service.drama;

import com.gewara.api.vo.ResultCode;
import com.gewara.api.vo.drama.DramaVo;
import com.gewara.api.vo.drama.OpenDramaItemVo;
import com.gewara.api.vo.drama.TheatreVo;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.Theatre;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.util.VoCopyUtil;

public class DramaVoServiceImpl  extends BaseServiceImpl implements DramaVoService{
	
	@Override
	public ResultCode<DramaVo> getDramaVoById(Long dramaid) {
		Drama drama = baseDao.getObject(Drama.class, dramaid);
		return VoCopyUtil.copyProperties(DramaVo.class, drama);
	}

	@Override
	public ResultCode<TheatreVo> getTheatreVoById(Long theatreid) {
		Theatre theatre = baseDao.getObject(Theatre.class, theatreid);
		return VoCopyUtil.copyProperties(TheatreVo.class, theatre);
	}

	@Override
	public ResultCode<OpenDramaItemVo> getOpenDramaItemVoById(Long dpid) {
		// TODO Auto-generated method stub
		return null;
	}

}
