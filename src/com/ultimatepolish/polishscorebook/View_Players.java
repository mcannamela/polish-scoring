package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.scorebookdb.Player;

public class View_Players extends MenuContainerActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_view_list);
        
        refreshPlayerList();
        
        // Make sure we're running on Honeycomb or higher to use ActionBar APIs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    @Override
    protected void onStop() {
    	super.onStop();
    	finish();
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		menu.findItem(R.id.players).setEnabled(false);
		return true;
	}
    
    @Override
	public void openAddActivity() {
    	Intent intent = new Intent(this, NewPlayer.class);
    	startActivity(intent);
    }

    private OnItemClickListener mPlayerClickedHandler = new OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position, long id) {
        	String msg;
        	
        	ViewHolder_Player h = (ViewHolder_Player) v.getTag();
        	msg = h.getFirstName() +" was clicked";
        	Context context = getApplicationContext();
    		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onRestart(){
    	super.onRestart();
    	refreshPlayerList();
    }
    @Override
    protected void onResume(){
    	super.onResume();
    	refreshPlayerList();
    }
    
    protected void refreshPlayerList(){
    	ArrayList<String> firstNames = new ArrayList<String>();
    	ArrayList<String> lastNames = new ArrayList<String>();
    	ArrayList<String> nicknames = new ArrayList<String>();
    	ArrayList<String> displayNames = new ArrayList<String>();
    	ArrayList<Player> players = new ArrayList<Player>();
        Dao<Player, Long> playerDao=null;
    	
    	try{
    		 playerDao = getHelper().getPlayerDao();
    		 for(Player p: playerDao){
    			 players.add(p);
    			 firstNames.add(p.getFirstName());
    			 lastNames.add(p.getLastName());
    			 nicknames.add(p.getNickName());
    			}
    	}
    	catch (SQLException e){
    		Context context = getApplicationContext();
    		Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
    		Log.e(View_Players.class.getName(), "Retrieval of players failed", e);
    	}
    	
    	String first, last, nick;
    	for(int i=0; i<firstNames.size();i++){
    		first = firstNames.get(i);
    		last = lastNames.get(i);
    		nick = nicknames.get(i);
    		displayNames.add(buildDisplayName(first, last, nick));
    	}
        
        ViewAdapter_Player adapter = new ViewAdapter_Player(this, 
											                R.id.layout_player_list_item, 
											                R.id.textView_firstName, 
											                players);
        ListView listView = (ListView) findViewById(R.id.db_listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(mPlayerClickedHandler); 
    }
    protected String buildDisplayName(String first, String last, String nick){
    	return first+" \"" +nick+"\" "+last;
    	
    }
    
    public void startNewPlayerDialog(View view) {
    	Intent intent = new Intent(this, NewPlayer.class);
    	startActivity(intent);
    }
    public void editPlayerDialog(View view){
    	
    }

}
