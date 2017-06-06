package uk.co.lemberg.navbarexdemo.settings;

import uk.co.lemberg.navbarexdemo.data.EntryType;

public class NavBarEntry
{
	public final int priority;
	public final String text;
	public final Integer color;
	public final EntryType entryType;

	public NavBarEntry(int priority, String text, Integer color, EntryType entryType)
	{
		this.priority = priority;
		this.text = text;
		this.color = color;
		this.entryType = entryType;
	}
}
