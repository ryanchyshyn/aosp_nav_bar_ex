package uk.co.lemberg.navbarexdemo.settings;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

public class Settings
{
	private static final String IDS_KEY = "ids";
	private static final String IMAGE_COLOR_KEY = "image_color";

	private static final ImageColor IMAGE_COLOR_DEFAULT = ImageColor.Red;

	private final Gson gson = new Gson();

	private Map<String, NavBarEntry> idsMap = new HashMap<>();
	private ImageColor imageColor = IMAGE_COLOR_DEFAULT;

	// region properties
	public Map<String, NavBarEntry> getIdsMap()
	{
		return idsMap;
	}

	public ImageColor getImageColor()
	{
		return imageColor;
	}

	public void setImageColor(ImageColor imageColor)
	{
		this.imageColor = imageColor;
	}
	// endregion

	public void load(SharedPreferences prefs)
	{
		String idsMapStr = prefs.getString(IDS_KEY, null);
		if (!TextUtils.isEmpty(idsMapStr))
		{
			idsMap = gson.fromJson(idsMapStr, new TypeToken<Map<String, NavBarEntry>>(){}.getType());
		}
		String imageColorStr = prefs.getString(IMAGE_COLOR_KEY, null);
		if (!TextUtils.isEmpty(imageColorStr))
		{
			try { imageColor = ImageColor.valueOf(imageColorStr); }
			catch (IllegalArgumentException ignored) { imageColor = IMAGE_COLOR_DEFAULT; }
		}
	}

	@SuppressLint("ApplySharedPref")
	public void save(SharedPreferences prefs)
	{
		SharedPreferences.Editor editor = prefs.edit();
		save(editor);
		editor.commit();
	}

	public void saveDeferred(SharedPreferences prefs)
	{
		SharedPreferences.Editor editor = prefs.edit();
		save(editor);
		editor.apply();
	}

	public void save(SharedPreferences.Editor editor)
	{
		String idsMapStr = gson.toJson(idsMap);
		editor.putString(IDS_KEY, idsMapStr);
		editor.putString(IMAGE_COLOR_KEY, imageColor.name());
	}
}
