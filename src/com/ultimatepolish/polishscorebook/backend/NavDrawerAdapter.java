package com.ultimatepolish.polishscorebook.backend;

import com.ultimatepolish.polishscorebook.R;
import com.ultimatepolish.polishscorebook.R.id;
import com.ultimatepolish.polishscorebook.R.layout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NavDrawerAdapter extends ArrayAdapter<NavDrawerItem> {
	private Context context;
	private int layoutId;
	private NavDrawerItem[] drawerItems;

	public NavDrawerAdapter(Context context, int layoutId,
			NavDrawerItem[] drawerItems) {
		super(context, layoutId, drawerItems);
		this.context = context;
		this.layoutId = layoutId;

		this.drawerItems = drawerItems;
	}

	static class NavViewHolder {
		public final TextView labelView;
		public final ImageView iconView;
		public final TextView counterView;

		public NavViewHolder(TextView tvLabel, ImageView ivIcon,
				TextView tvCounter) {
			this.labelView = tvLabel;
			this.iconView = ivIcon;
			this.counterView = tvCounter;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		NavViewHolder holder = null;
		NavDrawerItem item = getItem(position);

		if (!item.isHeader) {
			if (v == null) {
				int layout = R.layout.nav_drawer_item;

				v = LayoutInflater.from(getContext()).inflate(layout, null);

				TextView tvLabel = (TextView) v.findViewById(R.id.navLabel);
				ImageView ivIcon = (ImageView) v.findViewById(R.id.navIcon);
				TextView tvCounter = (TextView) v.findViewById(R.id.navCounter);
				v.setTag(new NavViewHolder(tvLabel, ivIcon, tvCounter));
			}

			if (holder == null && v != null) {
				Object tag = v.getTag();
				if (tag instanceof NavViewHolder) {
					holder = (NavViewHolder) tag;
				}
			}

			if (item != null && holder != null) {
				if (holder.labelView != null)
					holder.labelView.setText(item.label);

				if (holder.counterView != null) {
					if (item.counter > 0) {
						holder.counterView.setText("" + item.counter);
					} else {
						holder.counterView.setText("");
					}
				}

				if (holder.iconView != null) {
					if (item.iconId > 0) {
						holder.iconView.setVisibility(View.VISIBLE);
						holder.iconView.setImageResource(item.iconId);
					} else {
						holder.iconView.setVisibility(View.GONE);
					}
				}
			}
		} else {
			if (v == null) {
				int layout = R.layout.nav_drawer_header;
				v = LayoutInflater.from(getContext()).inflate(layout, null);
			}
		}

		return v;
	}
}