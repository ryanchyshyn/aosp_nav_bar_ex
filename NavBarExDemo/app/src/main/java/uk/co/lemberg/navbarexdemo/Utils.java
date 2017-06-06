package uk.co.lemberg.navbarexdemo;

import android.text.TextUtils;

public class Utils
{
	public static int parseIntSafe(CharSequence cs, int defaultValue)
	{
		if (TextUtils.isEmpty(cs)) return defaultValue;
		try
		{
			return Integer.parseInt(cs.toString());
		}
		catch (Exception e)
		{
			return defaultValue;
		}
	}

	public static long parseLongSafe(CharSequence cs, int radix, int defaultValue)
	{
		if (TextUtils.isEmpty(cs)) return defaultValue;
		try
		{
			return Long.parseLong(cs.toString(), radix);
		}
		catch (Exception e)
		{
			return defaultValue;
		}
	}
}
