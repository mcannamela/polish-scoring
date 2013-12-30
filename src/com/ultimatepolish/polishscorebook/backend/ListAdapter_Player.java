package com.ultimatepolish.polishscorebook.backend;

import java.util.ArrayList;

import com.ultimatepolish.polishscorebook.R;
import com.ultimatepolish.polishscorebook.R.id;
import com.ultimatepolish.polishscorebook.R.layout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class ListAdapter_Player extends BaseExpandableListAdapter {
	private Context context;
	private ArrayList<ViewHolderHeader_Player> statusList;
 
	public ListAdapter_Player(Context context, ArrayList<ViewHolderHeader_Player> statusList) {
    	this.context = context;
    	this.statusList = statusList;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
     ArrayList<ViewHolder_Player> playerList = statusList.get(groupPosition).getPlayerList();
     return playerList.get(childPosition);
    }
	
    @Override
    public long getChildId(int groupPosition, int childPosition) {
     return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, 
      View view, ViewGroup parent) {
      
     ViewHolder_Player playerInfo = (ViewHolder_Player) getChild(groupPosition, childPosition);
     if (view == null) {
      LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      view = infalInflater.inflate(R.layout.list_item_player, null);
     }
      
     TextView playerId = (TextView) view.findViewById(R.id.textView_playerId);
     playerId.setText(playerInfo.getId().trim());
     TextView playerColor = (TextView) view.findViewById(R.id.textView_playerColor);
     playerColor.setBackgroundColor(playerInfo.getColor());
     TextView name = (TextView) view.findViewById(R.id.textView_name);
     name.setText(playerInfo.getName().trim());
     TextView nickName = (TextView) view.findViewById(R.id.textView_nickName);
     nickName.setText(playerInfo.getNickName().trim());
     
     return view;
    }
    @Override
    public int getChildrenCount(int groupPosition) {
      
     ArrayList<ViewHolder_Player> playerList = statusList.get(groupPosition).getPlayerList();
     return playerList.size();
    
    }
    
    @Override
    public Object getGroup(int groupPosition) {
     return statusList.get(groupPosition);
    }
    
    @Override
    public int getGroupCount() {
     return statusList.size();
    }
    
    @Override
    public long getGroupId(int groupPosition) {
     return groupPosition;
    }
	
    @Override
    public View getGroupView(int groupPosition, boolean isLastChild, View view,
      ViewGroup parent) {
      
     ViewHolderHeader_Player statusInfo = (ViewHolderHeader_Player) getGroup(groupPosition);
     if (view == null) {
      LayoutInflater inf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      view = inf.inflate(R.layout.list_header, null);
     }
      
     TextView heading = (TextView) view.findViewById(R.id.heading);
     heading.setText(statusInfo.getName().trim());
      
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
