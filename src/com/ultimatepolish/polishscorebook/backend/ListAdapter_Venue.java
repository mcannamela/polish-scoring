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

public class ListAdapter_Venue extends BaseExpandableListAdapter {
	private Context context;
	private List<ViewHolderHeader_Venue> statusList;
 
	public ListAdapter_Venue(Context context, List<ViewHolderHeader_Venue> statusList) {
    	this.context = context;
    	this.statusList = statusList;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
     List<ViewHolder_Venue> venueList = statusList.get(groupPosition).getVenueList();
     return venueList.get(childPosition);
    }
	
    @Override
    public long getChildId(int groupPosition, int childPosition) {
     return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, 
      View view, ViewGroup parent) {
      
     ViewHolder_Venue venueInfo = (ViewHolder_Venue) getChild(groupPosition, childPosition);
     if (view == null) {
      LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      view = infalInflater.inflate(R.layout.list_item_venue, null);
     }
      
     TextView venueId = (TextView) view.findViewById(R.id.textView_venueId);
     venueId.setText(venueInfo.getId().trim());
     TextView venueName = (TextView) view.findViewById(R.id.textView_venueName);
     venueName.setText(venueInfo.getName().trim());
     
     return view;
    }
    @Override
    public int getChildrenCount(int groupPosition) {
      
     List<ViewHolder_Venue> venueList = statusList.get(groupPosition).getVenueList();
     return venueList.size();
    
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
      
     ViewHolderHeader_Venue statusInfo = (ViewHolderHeader_Venue) getGroup(groupPosition);
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