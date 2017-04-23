package com.gewara.helper.ticket;

import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.gewara.helper.discount.MovieSpecialDiscountHelper;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.pay.PayValidHelper;
import com.gewara.util.VmUtils;
import com.gewara.web.action.partner.OpiFilterAdapter;

public class SdOpiFilter extends OpiFilterAdapter{
	private SpecialDiscount sd;
	private Timestamp addtime;
	public SdOpiFilter(SpecialDiscount sd, Timestamp addtime){
		this.sd = sd;
		this.addtime = addtime;
	}
	@Override
	public boolean excludeOpi(OpenPlayItem opi) {
		PayValidHelper pvh = new PayValidHelper(VmUtils.readJsonToMap(opi.getOtherinfo()));
		return !MovieSpecialDiscountHelper.isEnabled(sd, opi, pvh).isSuccess() || 
				StringUtils.isNotBlank(MovieSpecialDiscountHelper.getOpiFirstDisabledReason(sd, opi, addtime));
	}
	@Override
	public void filterCinema(List<Cinema> cinemaList) {
		throw new IllegalArgumentException("not supported!");
	}
	@Override
	public void filterMovie(List<Movie> movieList) {
		throw new IllegalArgumentException("not supported!");
	}
}
