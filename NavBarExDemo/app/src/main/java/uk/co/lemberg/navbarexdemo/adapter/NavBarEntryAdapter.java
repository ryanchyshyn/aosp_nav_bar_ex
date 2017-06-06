package uk.co.lemberg.navbarexdemo.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import uk.co.lemberg.navbarexdemo.settings.NavBarEntry;

public class NavBarEntryAdapter extends ArrayAdapter<Pair<String /* uuid */, NavBarEntry>>
{
	private final LayoutInflater inflater;

	private static final class ViewHolder
	{
		public final TextView txtText;

		public ViewHolder(TextView txtText)
		{
			this.txtText = txtText;
		}
	}

	public NavBarEntryAdapter(Context context, List<Pair<String /* uuid */, NavBarEntry>> entries)
	{
		super(context, 0, entries);
		inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		View resultView = convertView;

		ViewHolder holder;

		if (resultView == null)
		{
			resultView = inflater.inflate(android.R.layout.simple_list_item_1, null);
			holder = new ViewHolder((TextView) resultView.findViewById(android.R.id.text1));

			resultView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) resultView.getTag();
		}

		Pair<String /* uuid */, NavBarEntry> entry = getItem(position);
		fillViews(holder, entry);

		return resultView;
	}

	private void fillViews(ViewHolder vh, Pair<String /* uuid */, NavBarEntry> item)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append(item.first);
		sb.append("}");
		if (!TextUtils.isEmpty(item.second.text))
		{
			sb.append(", text: ");
			sb.append(item.second.text);
		}
		if (item.second.color != null)
		{
			sb.append(", color: ");
			sb.append(String.format("%X", item.second.color));
		}
		sb.append(", type: ");
		sb.append(item.second.entryType);
		vh.txtText.setText(sb.toString());
	}
}
