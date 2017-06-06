package uk.co.lemberg.navbarexdemo.broadcast;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.NavBarExServiceMgr;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.Map;

import uk.co.lemberg.navbarexdemo.R;
import uk.co.lemberg.navbarexdemo.data.EntryType;
import uk.co.lemberg.navbarexdemo.settings.AppSettings;
import uk.co.lemberg.navbarexdemo.settings.ImageColor;
import uk.co.lemberg.navbarexdemo.settings.NavBarEntry;

public class ClickReceiver extends BroadcastReceiver
{
	public static final String ACTION_CLICK_TEXT = "uk.co.lemberg.navbarexdemo.broadcast.ACTION_CLICK_TEXT";
	public static final String ACTION_CLICK_BUTTON = "uk.co.lemberg.navbarexdemo.broadcast.ACTION_CLICK_BUTTON";
	public static final String ACTION_CLICK_IMAGE = "uk.co.lemberg.navbarexdemo.broadcast.ACTION_CLICK_IMAGE";

	@Override
	public void onReceive(Context context, Intent intent)
	{
		String action = intent != null ? intent.getAction() : null;
		Toast.makeText(context, String.format("Clicked. Action: %s", action), Toast.LENGTH_SHORT).show();

		if (ACTION_CLICK_IMAGE.equals(action))
		{
			// get ID
			AppSettings settings = AppSettings.getAppSettings(context);
			String id = getFirstImageId(settings.getIdsMap());
			if (id == null) return;

			ImageColor imageColor = settings.getImageColor();
			int ord = imageColor.ordinal() + 1;
			if (ord >= ImageColor.values().length) ord = 0;
			imageColor = ImageColor.values()[ord];

			// replace image
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.remote_image);
			remoteViews.setImageViewResource(R.id.root, imageColor == ImageColor.Red ? R.drawable.smile : R.drawable.sad);
			intent.setAction(ClickReceiver.ACTION_CLICK_IMAGE);

			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.root, pendingIntent);

			NavBarExServiceMgr navBarExServiceMgr = (NavBarExServiceMgr) context.getSystemService(Context.NAVBAREX_SERVICE);

			boolean res = navBarExServiceMgr.replaceView(id, remoteViews);
			if (!res)
			{
				Toast.makeText(context, "Failed to replace remote views. Returned false.", Toast.LENGTH_SHORT).show();
			}

			settings.setImageColor(imageColor);
			settings.saveDeferred();
		}
	}

	private static String getFirstImageId(Map<String, NavBarEntry> idsMap)
	{
		for (Map.Entry<String, NavBarEntry> entry : idsMap.entrySet())
		{
			if (entry.getValue().entryType == EntryType.Image) return entry.getKey();
		}

		return null;
	}
}
