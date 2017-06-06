package uk.co.lemberg.navbarexdemo.activity;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.NavBarExServiceMgr;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.co.lemberg.navbarexdemo.R;
import uk.co.lemberg.navbarexdemo.Utils;
import uk.co.lemberg.navbarexdemo.adapter.NavBarEntryAdapter;
import uk.co.lemberg.navbarexdemo.broadcast.ClickReceiver;
import uk.co.lemberg.navbarexdemo.data.EntryType;
import uk.co.lemberg.navbarexdemo.settings.AppSettings;
import uk.co.lemberg.navbarexdemo.settings.NavBarEntry;

public class MainActivity extends Activity
{
	private AppSettings settings;

	private EditText editPriority;
	private EditText editText;
	private EditText editColor;
	private Button btnAdd;
	private Button btnDelete;
	private Button btnReplace;
	private RadioGroup radioGroup;
	private ListView listIds;

	private NavBarEntryAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		settings = AppSettings.getAppSettings(this);
		setContentView(R.layout.activity_main);

		editPriority = (EditText) findViewById(R.id.edit_priority);
		editText = (EditText) findViewById(R.id.edit_text);
		editColor = (EditText) findViewById(R.id.edit_color);
		btnAdd = (Button) findViewById(R.id.btn_add);
		btnDelete = (Button) findViewById(R.id.btn_delete);
		btnReplace = (Button) findViewById(R.id.btn_replace);
		radioGroup = (RadioGroup) findViewById(R.id.radio_group);
		listIds = (ListView) findViewById(R.id.list_ids);

		adapter = new NavBarEntryAdapter(this, new ArrayList<Pair<String /* uuid */, NavBarEntry>>());
		listIds.setAdapter(adapter);

		radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(RadioGroup radioGroup, int i)
			{
				fillViews();
			}
		});

		editPriority.addTextChangedListener(textWatcher);
		editText.addTextChangedListener(textWatcher);
		editColor.addTextChangedListener(textWatcher);

		listIds.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
			{
				fillViews();
			}
		});

		btnAdd.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				addView();
			}
		});
		btnDelete.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				deleteView();
			}
		});
		btnReplace.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				replaceView();
			}
		});

		fillListView();
		fillViews();
	}

	private final TextWatcher textWatcher = new TextWatcher()
	{
		@Override
		public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
		{
		}

		@Override
		public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
		{
			fillViews();
		}

		@Override
		public void afterTextChanged(Editable editable)
		{
		}
	};

	private NavBarEntry navBarEntryFromView()
	{
		int priority = Utils.parseIntSafe(editPriority.getText(), -1);
		if (priority == -1) return null;

		String text = null;
		EntryType entryType = null;
		Integer color = null;

		int checkedId = radioGroup.getCheckedRadioButtonId();
		switch (checkedId)
		{
			case R.id.radio_button:
				text = editText.getText().toString();
				if (TextUtils.isEmpty(text)) return null;
				entryType = EntryType.Button;
				break;
			case R.id.radio_text:
				text = editText.getText().toString();
				if (TextUtils.isEmpty(text)) return null;
				entryType = EntryType.Text;
				long colorLong = Utils.parseLongSafe(editColor.getText().toString(), 16, -1);
				if (colorLong == -1) return null;
				color = (int) colorLong;
				break;
			case R.id.radio_image:
				entryType = EntryType.Image;
				break;
		}
		if (entryType == null) return null;

		return new NavBarEntry(priority, text, color, entryType);
	}

	private void addView()
	{
		NavBarEntry entry = navBarEntryFromView();
		if (entry == null) return;

		RemoteViews remoteViews = createRemoteViewsId(entry);
		if (remoteViews == null) return;

		NavBarExServiceMgr navBarExServiceMgr = (NavBarExServiceMgr) getSystemService(Context.NAVBAREX_SERVICE);

		String id = navBarExServiceMgr.addView(entry.priority, remoteViews);
		if (TextUtils.isEmpty(id))
		{
			Toast.makeText(this, "Failed to add remote views. Returned null.", Toast.LENGTH_SHORT).show();
			return;
		}
		settings.getIdsMap().put(id, entry);
		settings.saveDeferred();

		fillListView();
		fillViews();
	}

	private void deleteView()
	{
		int pos = listIds.getCheckedItemPosition();
		if ((pos == -1) || (pos >= adapter.getCount())) return;

		Pair<String /* uuid */, NavBarEntry> pair = adapter.getItem(pos);

		NavBarExServiceMgr navBarExServiceMgr = (NavBarExServiceMgr) getSystemService(Context.NAVBAREX_SERVICE);

		boolean res = navBarExServiceMgr.removeView(pair.first);
		if (!res)
		{
			Toast.makeText(this, "Failed to remove remote views. Returned false.", Toast.LENGTH_SHORT).show();
		}
		settings.getIdsMap().remove(pair.first);
		settings.saveDeferred();

		fillListView();
		fillViews();
	}

	private void replaceView()
	{
		int pos = listIds.getCheckedItemPosition();
		if ((pos == -1) || (pos >= adapter.getCount())) return;

		NavBarEntry entry = navBarEntryFromView();
		if (entry == null) return;

		Pair<String /* uuid */, NavBarEntry> pair = adapter.getItem(pos);

		RemoteViews remoteViews = createRemoteViewsId(entry);
		if (remoteViews == null) return;

		NavBarExServiceMgr navBarExServiceMgr = (NavBarExServiceMgr) getSystemService(Context.NAVBAREX_SERVICE);

		boolean res = navBarExServiceMgr.replaceView(pair.first, remoteViews);
		if (!res)
		{
			Toast.makeText(this, "Failed to replace remote views. Returned false.", Toast.LENGTH_SHORT).show();
		}

		settings.getIdsMap().put(pair.first, entry);
		settings.saveDeferred();

		fillListView();
		fillViews();
	}

	private RemoteViews createRemoteViewsId(NavBarEntry entry)
	{
		Intent intent = new Intent(this, ClickReceiver.class);
		RemoteViews ret = null;
		switch (entry.entryType)
		{
			case Button:
				ret = new RemoteViews(getPackageName(), R.layout.remote_button);
				ret.setTextViewText(R.id.root, entry.text);
				intent.setAction(ClickReceiver.ACTION_CLICK_BUTTON);
				break;
			case Text:
				ret = new RemoteViews(getPackageName(), R.layout.remote_text);
				ret.setTextViewText(R.id.root, entry.text);
				ret.setInt(R.id.root, "setBackgroundColor", entry.color);
				intent.setAction(ClickReceiver.ACTION_CLICK_TEXT);
				break;
			case Image:
				ret = new RemoteViews(getPackageName(), R.layout.remote_image);
				ret.setImageViewResource(R.id.root, R.drawable.smile);
				intent.setAction(ClickReceiver.ACTION_CLICK_IMAGE);
				break;
		}

		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		ret.setOnClickPendingIntent(R.id.root, pendingIntent);

		return ret;
	}

	private List<Pair<String /* uuid */, NavBarEntry>> getIdsList()
	{
		Set<Map.Entry<String, NavBarEntry>> set = settings.getIdsMap().entrySet();
		ArrayList<Pair<String /* uuid */, NavBarEntry>> ret = new ArrayList<>(set.size());
		for (Map.Entry<String, NavBarEntry> entry : set)
		{
			ret.add(new Pair<>(entry.getKey(), entry.getValue()));
		}
		return ret;
	}

	private void fillViews()
	{
		int radioCheckedItemPos = radioGroup.getCheckedRadioButtonId();
		int listSelectedPos = listIds.getCheckedItemPosition();
		if (listIds.getCount() == 0) listSelectedPos = -1;
		switch (radioCheckedItemPos)
		{
			case R.id.radio_button:
				editColor.setEnabled(false);
				editText.setEnabled(true);
				break;
			case R.id.radio_text:
				editColor.setEnabled(true);
				editText.setEnabled(true);
				break;
			case R.id.radio_image:
				editColor.setEnabled(false);
				editText.setEnabled(false);
				break;
		}
		boolean addEnabled = (radioCheckedItemPos != -1) &&
			!TextUtils.isEmpty(editPriority.getText()) &&
			(!editText.isEnabled() || !TextUtils.isEmpty(editText.getText())) &&
			(!editColor.isEnabled() || !TextUtils.isEmpty(editColor.getText()));

		boolean replaceEnabled = addEnabled && (listSelectedPos != -1);

		btnReplace.setEnabled(replaceEnabled);
		btnAdd.setEnabled(addEnabled);
		btnDelete.setEnabled(listSelectedPos != -1);
	}

	private void fillListView()
	{
		adapter.clear();
		adapter.addAll(getIdsList());
	}
}
