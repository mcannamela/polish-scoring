package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.scorebookdb.Player;

public class View_Players extends MenuContainerActivity {
	private LinkedHashMap<String, ViewHolderHeader_Player> sHash = new LinkedHashMap<String, ViewHolderHeader_Player>();
	private ArrayList<ViewHolderHeader_Player> statusList = new ArrayList<ViewHolderHeader_Player>();
	private ListAdapter_Player playerAdapter;
	private ExpandableListView elv;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_listing);
        
        // Make sure we're running on Honeycomb or higher to use ActionBar APIs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        elv = (ExpandableListView) findViewById(R.id.dbListing);
        playerAdapter = new ListAdapter_Player(View_Players.this, statusList);
        elv.setAdapter(playerAdapter);
        expandAll();
        elv.setOnChildClickListener(elvItemClicked);
        elv.setOnGroupClickListener(elvGroupClicked);
        
    }
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		menu.findItem(R.id.players).setEnabled(false);
		menu.findItem(R.id.addButton).setVisible(true);
		return true;
	}    
    @Override
	public void openAddActivity() {
    	Intent intent = new Intent(this, NewPlayer.class);
    	startActivity(intent);
    }
    @Override
    protected void onRestart(){
    	super.onRestart();
    }
    @Override
    protected void onResume(){
    	super.onResume();
    	refreshPlayersListing();
    }    
    @Override
    protected void onStop() {
    	super.onStop();
    }
    private void expandAll() {
    	//method to expand all groups
    	int count = playerAdapter.getGroupCount();
    	for (int i = 0; i < count; i++){
		elv.expandGroup(i);
    	}
    }
    private void collapseAll() {
    	//method to collapse all groups
    	int count = playerAdapter.getGroupCount();
    	for (int i = 0; i < count; i++){
    	elv.collapseGroup(i);
    	}
    }
    protected void refreshPlayersListing(){
    	sHash.clear();
    	statusList.clear();
    	
    	// add all the statii to the headers
        addStatus("Active");
        addStatus("Retired");
        
        // add all the players
    	Dao<Player, Long> playerDao = null;
        try{
        	playerDao = getHelper().getPlayerDao();
        	for (Player p: playerDao) {
        		addPlayer(p.getIsActive(), 
        				String.valueOf(p.getId()), p.color,
        				p.getFirstName() + " " + p.getLastName(), 
        				"(" + p.getNickName() + ")"
        				);
        	}
    	}
        catch (SQLException e){
    		Context context = getApplicationContext();
    		Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
    		Log.e(View_Players.class.getName(), "Retrieval of players failed", e);
        }
        
    	expandAll();
    	playerAdapter.notifyDataSetChanged(); // required in case the list has changed
    }
    private OnChildClickListener elvItemClicked =  new OnChildClickListener() {
    	public boolean onChildClick(ExpandableListView parent, View v,
    	int groupPosition, int childPosition, long id) {

	    //get the group header
	    ViewHolderHeader_Player statusInfo = statusList.get(groupPosition);
	    //get the child info
   		ViewHolder_Player playerInfo =  statusInfo.getPlayerList().get(childPosition);
   		//display it or do something with it
   		Toast.makeText(getBaseContext(), "Selected " + playerInfo.getName(), Toast.LENGTH_SHORT).show();
    	
   		// load the game in progress screen
   		Long pId  = Long.valueOf(playerInfo.getId());
		Intent intent = new Intent(getApplicationContext(), Detail_Player.class);
        intent.putExtra("PID", pId);
        startActivity(intent);
    	return false;
    	}
    };
    private OnGroupClickListener elvGroupClicked =  new OnGroupClickListener() {
    	public boolean onGroupClick(ExpandableListView parent, View v,
    	int groupPosition, long id) {
    	    
    	//get the group header
    	ViewHolderHeader_Player statusInfo = statusList.get(groupPosition);
    	//display it or do something with it
    	Toast.makeText(getBaseContext(), "Tapped " + statusInfo.getName(), Toast.LENGTH_SHORT).show();
    	return false;
    	}
    };
    private void addStatus(String statusName){
    	ViewHolderHeader_Player vhh_Player = new ViewHolderHeader_Player();
    	vhh_Player.setName(statusName);
    	statusList.add(vhh_Player);
    	sHash.put(statusName, vhh_Player);
    }
    private void addPlayer(Boolean isActive, String playerId, Integer playerColor, String playerName, String playerNick){
    	//find the index of the player header
    	String sortBy;
    	if (isActive) {
    		sortBy = "Active";
    	} else {
    		sortBy = "Retired";
    	}
    	ViewHolderHeader_Player statusInfo = sHash.get(sortBy);
	    ArrayList<ViewHolder_Player> playerList = statusInfo.getPlayerList();
	    
	    //create a new child and add that to the group
	    ViewHolder_Player playerInfo = new ViewHolder_Player();
	    playerInfo.setId(playerId);
	    playerInfo.setColor(playerColor);
	    playerInfo.setName(playerName);
	    playerInfo.setNickName(playerNick);
	    playerList.add(playerInfo);
		statusInfo.setPlayerList(playerList);
	}
}
