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

public class ListAdapter_Team extends BaseExpandableListAdapter {
	private Context context;
	private List<ViewHolderHeader_Team> statusList;
 
	public ListAdapter_Team(Context context, List<ViewHolderHeader_Team> statusList) {
    	this.context = context;
    	this.statusList = statusList;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
     List<ViewHolder_Team> teamList = statusList.get(groupPosition).getTeamList();
     return teamList.get(childPosition);
    }
	
    @Override
    public long getChildId(int groupPosition, int childPosition) {
     return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, 
      View view, ViewGroup parent) {
      
     ViewHolder_Team teamInfo = (ViewHolder_Team) getChild(groupPosition, childPosition);
     if (view == null) {
      LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      view = infalInflater.inflate(R.layout.list_item_team, null);
     }
      
     TextView teamId = (TextView) view.findViewById(R.id.textView_teamId);
     teamId.setText(teamInfo.getId().trim());
     
     TextView name = (TextView) view.findViewById(R.id.textView_name);
     name.setText(teamInfo.getTeamName().trim());
     
     TextView nickName = (TextView) view.findViewById(R.id.textView_players);
     nickName.setText(teamInfo.getPlayerNames().trim());
     
     return view;
    }
    @Override
    public int getChildrenCount(int groupPosition) {
      
     List<ViewHolder_Team> teamList = statusList.get(groupPosition).getTeamList();
     return teamList.size();
    
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
      
     ViewHolderHeader_Team statusInfo = (ViewHolderHeader_Team) getGroup(groupPosition);
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
