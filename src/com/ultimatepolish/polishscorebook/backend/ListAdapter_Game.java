package com.ultimatepolish.polishscorebook.backend;

import java.util.List;

import com.ultimatepolish.polishscorebook.R;
import com.ultimatepolish.polishscorebook.R.id;
import com.ultimatepolish.polishscorebook.R.layout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class ListAdapter_Game extends BaseExpandableListAdapter {
	private Context context;
	private List<ViewHolderHeader_Game> sessionList;

	public ListAdapter_Game(Context context,
			List<ViewHolderHeader_Game> sessionList) {
		this.context = context;
		this.sessionList = sessionList;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		List<ViewHolder_Game> gameList = sessionList.get(groupPosition)
				.getGameList();
		return gameList.get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View view, ViewGroup parent) {

		ViewHolder_Game gameInfo = (ViewHolder_Game) getChild(groupPosition,
				childPosition);
		if (view == null) {
			LayoutInflater infalInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = infalInflater.inflate(R.layout.list_item_game, null);
		}

		TextView gameId = (TextView) view.findViewById(R.id.textView_gameId);
		gameId.setText(gameInfo.getId().trim());
		TextView playerOne = (TextView) view
				.findViewById(R.id.textView_playerOne);
		playerOne.setText(gameInfo.getPlayerOne().trim());
		TextView playerTwo = (TextView) view
				.findViewById(R.id.textView_playerTwo);
		playerTwo.setText(gameInfo.getPlayerTwo().trim());
		TextView score = (TextView) view.findViewById(R.id.textView_score);
		score.setText(gameInfo.getScore().trim());

		return view;
	}

	@Override
	public int getChildrenCount(int groupPosition) {

		List<ViewHolder_Game> gameList = sessionList.get(groupPosition)
				.getGameList();
		return gameList.size();

	}

	@Override
	public Object getGroup(int groupPosition) {
		return sessionList.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return sessionList.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isLastChild, View view,
			ViewGroup parent) {

		ViewHolderHeader_Game sessionInfo = (ViewHolderHeader_Game) getGroup(groupPosition);
		if (view == null) {
			LayoutInflater inf = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inf.inflate(R.layout.list_header, null);
		}

		TextView heading = (TextView) view.findViewById(R.id.heading);
		heading.setText(sessionInfo.getName().trim());

		return view;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}