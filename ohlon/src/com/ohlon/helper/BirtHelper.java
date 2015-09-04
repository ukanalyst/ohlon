package com.ohlon.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.ohlon.service.BirtService;

public class BirtHelper {

	public static BirtService getService(HttpServletRequest request) {
		ServletContext servletContext = request.getSession().getServletContext();
		WebApplicationContext spring = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		return (BirtService) spring.getBean("ohlon.birtService");
	}

	public static Calendar toCalendar(String date) throws ParseException {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		cal.setTime(sdf.parse(date));
		return cal;
	}

	public static String toString(Date date) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(date);
	}

	public static String humanReadableDuration(int seconds) {
		String duration = "";
		long hour = seconds / 3600;
		long min = (seconds - hour * 3600) / 60;
		long second = seconds % 60;
		if (hour > 0) {
			duration = hour + "h ";
		}
		if (min > 0) {
			duration = duration + min + "m ";
		}
		duration = duration + second + "s";
		return duration;
	}

	public static Double computeMedian(int[] values) {
		Arrays.sort(values);
		double median;
		if (values.length % 2 == 0)
			median = ((double) values[values.length / 2] + (double) values[values.length / 2 - 1]) / 2;
		else
			median = (double) values[values.length / 2];
		return median;
	}
}
