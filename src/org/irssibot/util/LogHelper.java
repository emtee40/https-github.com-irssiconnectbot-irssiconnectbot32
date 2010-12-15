package org.irssibot.util;

import android.content.Context;
import android.util.Log;

import java.util.concurrent.atomic.AtomicReference;

/**
 * User: parkerkane
 * Date: 13.12.2010
 * Time: 10:52
 */
public class LogHelper {
	
	public static boolean SHORTIFY_PACKAGES = true;
	public static boolean DEBUG = true;

	final private static AtomicReference<String> packageName = new AtomicReference<String>("<unknown>");

	public static void register(String string) {
		packageName.set(string);
	}
	
	public static void register(Context context) {

		String[] packages = context.getPackageName().split("\\.");

		packageName.set(packages[packages.length - 1]);
	}
	
	public static String getName(Class<? extends Object> klass) {
		return klass.getCanonicalName();
	}

	public static void ERROR(Object... objs) {
		if (!DEBUG) return;
		
		Log.e(getTag(), makeString(objs));
	}

	public static void VERBOSE(Object... objs) {
		if (!DEBUG) return;
		
		Log.v(getTag(), makeString(objs));
	}
	
	public static void WARN(Object... objs) {
		if (!DEBUG) return;
		
		Log.w(getTag(), makeString(objs));
	}
	
	public static void WTF(Object... objs) {
		if (!DEBUG) return;
		
		Log.wtf(getTag(), makeString(objs));
	}
	
	public static void INFO(Object... objs) {
		if (!DEBUG) return;

		Log.i(getTag(), makeString(objs));
	}
	
	public static void DEBUG(Object... objs) {
		if (!DEBUG) return;
		
		Log.d(getTag(), makeString(objs));
	}

	public static String getTag() {
		return packageName.get();
	}

	private static String makeString(Object[] objs) {

		StringBuffer sb = new StringBuffer();

		boolean first = true;

		for (Object o : objs) {
			if (!first) {
				sb.append(" ");
			}

			first = false;

			sb.append(o.toString());
		}

		StackTraceElement s = Thread.currentThread().getStackTrace()[4];

		String footer = String.format("<%s:%d %s thread:%d>",
			s.getFileName(), s.getLineNumber(),
			s.getMethodName(), 
			Thread.currentThread().getId()
			);

		return String.format("%-80s %s", sb.toString().replace("\n", "\\n").replace("\r", "\\r"), footer);
	}

}
