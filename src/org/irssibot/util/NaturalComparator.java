package org.irssibot.util;

import ext.regex2.Matcher;
import ext.regex2.Pattern;

import java.util.Comparator;

/**
* User: parkerkane
* Date: 13.12.2010
* Time: 14:02
*/
public class NaturalComparator<T extends Object> implements Comparator<T> {

	private static final Pattern RE = Pattern.compile("(\\d+)|\\D+");

	public int compare(T me, T other) {

		String meStr = me.toString().toLowerCase();
		String otherStr = other.toString().toLowerCase();

		if (meStr.equals(otherStr)) {
			return 0;
		}

		Matcher meMatch = RE.matcher(meStr);
		Matcher otherMatch = RE.matcher(otherStr);

		int myPosition = 0;
		int otherPosition = 0;

		while (meMatch.find(myPosition) && otherMatch.find(otherPosition)) {

			if (meMatch.group(1) != null && meMatch.group(1) != null) {

				int myInt = Integer.parseInt(meMatch.group());
				int otherInt = Integer.parseInt(otherMatch.group());

				return myInt - otherInt;

			} else if (!meMatch.group().equals(otherMatch.group())) {

				return meMatch.group().compareTo(otherMatch.group());
			}

			myPosition = meMatch.end();
			otherPosition = otherMatch.end();
		}

		return 0;
	}
}
