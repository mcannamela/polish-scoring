package com.ultimatepolish.polishscorebook.backend;

import java.util.ArrayList;
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

public class ListAdapter_Session extends BaseExpandableListAdapter {
	private Context context;
	private ArrayList<ViewHolderHeader_Session> statusList;
 
	public ListAdapter_Session(Context context, ArrayList<ViewHolderHeader_Session> statusList) {
    	this.context = context;
    	this.statusList = statusList;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
     ArrayList<ViewHolder_Session> sessionList = statusList.get(groupPosition).getSessionList();
     return sessionList.get(childPosition);
    }
	
    @Override
    public long getChildId(int groupPosition, int childPosition) {
     return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, 
      View view, ViewGroup parent) {
      
     ViewHolder_Session sessionInfo = (ViewHolder_Session) getChild(groupPosition, childPosition);
     if (view == null) {
      LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      view = infalInflater.inflate(R.layout.list_item_session, null);
     }
      
     TextView sessionId = (TextView) view.findViewById(R.id.textView_sessionId);
     sessionId.setText(sessionInfo.getId().trim());
     
     TextView name = (TextView) view.findViewById(R.id.textView_sessionName);
     name.setText(sessionInfo.getName().trim());
     
     TextView type = (TextView) view.findViewById(R.id.textView_sessionType);
     type.setText(sessionInfo.getType().trim());
     
     TextView team = (TextView) view.findViewById(R.id.textView_sessionTeam);
     team.setText(sessionInfo.getTeam().trim());
     
     
     return view;
    }
    @Override
    public int getChildrenCount(int groupPosition) {
      
     List<ViewHolder_Session> sessionList = statusList.get(groupPosition).getSessionList();
     return sessionList.size();
    
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
      
     ViewHolderHeader_Session statusInfo = (ViewHolderHeader_Session) getGroup(groupPosition);
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