/**
 * 
 */
package com.gewara.web.action.partner;

import java.util.ArrayList;
import java.util.List;

import com.gewara.model.movie.Cinema;

public abstract class CinemaFilter {
	public List<Cinema> filter(List<Cinema> cinemaList) {
		List<Cinema> result = new ArrayList<Cinema>();
		for (Cinema cinema : cinemaList) {
			if (accept(cinema))
				result.add(cinema);
		}
		return result;
	}

	public abstract boolean accept(Cinema cinema);
}
