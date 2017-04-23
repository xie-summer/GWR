package com.gewara.helper.ticket;

import java.util.List;

import com.gewara.helper.sys.ObjectFilter;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.ticket.OpenPlayItem;

public abstract class OpiFilter extends ObjectFilter<OpenPlayItem> {
	public abstract void filterMovie(List<Movie> movieList);
	public abstract void filterCinema(List<Cinema> cinemaList);
}
