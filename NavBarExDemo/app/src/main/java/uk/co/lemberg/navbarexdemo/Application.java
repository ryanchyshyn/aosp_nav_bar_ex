package uk.co.lemberg.navbarexdemo;

import android.preference.PreferenceManager;

import uk.co.lemberg.navbarexdemo.settings.AppSettings;

public class Application extends android.app.Application
{
	private AppSettings settings;

	public AppSettings getSettings() {
		return settings;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();

		settings = new AppSettings(PreferenceManager.getDefaultSharedPreferences(this));
		settings.load();
	}
}
