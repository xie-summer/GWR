package com.gewara.web.action.partner;

import java.util.List;

import com.gewara.helper.ticket.OpiFilter;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;

public abstract class OpiFilterAdapter extends OpiFilter{

	@Override
	public void filterCinema(List<Cinema> cinemaList) {
	}

	@Override
	public void filterMovie(List<Movie> movieList) {
	}
	@Override
	public boolean hasFilter() {
		return true;
	}

}
