package com.ohlon.ephesoft.db.utils;

import java.util.List;

public class ListUtils {

	public static Double average(List<Double> list) {
		Double sum = 0d;
		for (Double val : list) {
			sum += val;
		}
		return sum / list.size();
	}
	
	public static Double minimum(List<Double> list) {
		Double minimum = 0d;
		boolean foundFirst = false;
		for (Double val : list) {
			if (!foundFirst) {
				foundFirst = true;
				minimum = val;
			} else if (val < minimum)
				minimum = val;
		}
		return minimum;
	}
	
	public static Double maximum(List<Double> list) {
		Double maximum = 0d;
		boolean foundFirst = false;
		for (Double val : list) {
			if (!foundFirst) {
				foundFirst = true;
				maximum = val;
			} else if (val > maximum)
				maximum = val;
		}
		return maximum;
	}
}
