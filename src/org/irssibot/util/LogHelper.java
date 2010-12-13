package org.irssibot.util;

import android.util.Log;

/**
 * User: treunanen
 * Date: 13.12.2010
 * Time: 10:52
 */
public class LogHelper {
	
	public static boolean SHORTIFY_PACKAGES = true;
	public static boolean DEBUG = true;

	public static String getName(Class<? extends Object> klass) {
		return klass.getCanonicalName();
	}

	public static void ERROR(Object... objs) {
		if (!DEBUG) return;
		
		Log.e(getTag(), stringify(objs));
	}

	public static void VERBOSE(Object... objs) {
		if (!DEBUG) return;
		
		Log.v(getTag(), stringify(objs));
	}
	
	public static void WARN(Object... objs) {
		if (!DEBUG) return;
		
		Log.w(getTag(), stringify(objs));
	}
	
	public static void WTF(Object... objs) {
		if (!DEBUG) return;
		
		Log.wtf(getTag(), stringify(objs));
	}
	
	public static void INFO(Object... objs) {
		if (!DEBUG) return;

		Log.i(getTag(), stringify(objs));
	}
	
	public static void DEBUG(Object... objs) {
		if (!DEBUG) return;
		
		Log.d(getTag(), stringify(objs));
	}

	public static String getTag() {

		String tag;

		try {
			throw new Exception();
		} catch (Exception e) {
			StackTraceElement s = e.getStackTrace()[2];
			
			String className = s.getClassName();
			
			if (SHORTIFY_PACKAGES) {
				StringBuffer sb = new StringBuffer();
				
				String [] classes = className.split("\\.");

				for (int i = 0; i < classes.length-1; i++) {
					sb.append(classes[i].substring(0, 1));
					sb.append('.');
				}
				
				sb.append(classes[classes.length-1]);
				
				className = sb.toString();
			}

			tag = String.format("%s::%s:%d", className, s.getMethodName(), s.getLineNumber());
		}
		return tag;
	}

	private static String stringify(Object[] objs) {

		StringBuffer sb = new StringBuffer();

		boolean first = true;

		for (Object o : objs) {
			if (!first) {
				sb.append(", ");
			}
			
			first = false;
			
			sb.append(o.toString());
		}
		return sb.toString();
	}

}
