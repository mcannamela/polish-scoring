package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.scorebookdb.Game;
import com.ultimatepolish.scorebookdb.Player;
import com.ultimatepolish.scorebookdb.Session;
import com.ultimatepolish.scorebookdb.Venue;

public class NewGame extends MenuContainerActivity {
	Spinner spinner_p1 ;
	Spinner spinner_p2 ;
	Spinner spinner_session ;
	Spinner spinner_venue ;
	ListView lv_players;
	
	int p1_pos = 0;
	int p2_pos = 1;
	int session_pos = 0;
	int venue_pos = 1;
	
	List<Player> players = new ArrayList<Player>();
	List<Session> sessions= new ArrayList<Session>();
	List<Venue> venues= new ArrayList<Venue>();
	
	List<String> playerNames = new ArrayList<String>();
	List<String> sessionNames = new ArrayList<String>();
	List<String> venueNames = new ArrayList<String>();
	
	Dao<Player, Long> pDao;
	Dao<Session, Long> sDao;
	Dao<Venue, Long> vDao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_game);
		spinner_p1 = (Spinner) findViewById(R.id.spinner_player1);
		spinner_p2 = (Spinner) findViewById(R.id.spinner_player2);
		spinner_session = (Spinner) findViewById(R.id.spinner_session);
		spinner_venue = (Spinner) findViewById(R.id.spinner_venue);
		
		refreshSpinners(spinner_p1);
		
		spinner_p1.setOnItemSelectedListener(mPlayerOneSelectedHandler);
		spinner_p2.setOnItemSelectedListener(mPlayerTwoSelectedHandler);
		spinner_session.setOnItemSelectedListener(mSessionSelectedHandler);
		spinner_venue.setOnItemSelectedListener(mVenueSelectedHandler);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	private OnItemSelectedListener mPlayerOneSelectedHandler = new OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
			p1_pos = position;
		}
		public void onNothingSelected(AdapterView<?> parent) {}
	    
	};
	private OnItemSelectedListener mPlayerTwoSelectedHandler = new OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
			p2_pos = position;
		}
		public void onNothingSelected(AdapterView<?> parent) {}
		
	};
	private OnItemSelectedListener mSessionSelectedHandler = new OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
			session_pos = position;

		}
		public void onNothingSelected(AdapterView<?> parent) {}
	};
	private OnItemSelectedListener mVenueSelectedHandler = new OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        	venue_pos = position;
        }
        public void onNothingSelected(AdapterView<?> parent) {}
    };

	public void refreshSpinners(View view){
		Context context = getApplicationContext();
		try{
			pDao = Player.getDao(context);
			sDao = Session.getDao(context);
			vDao = Venue.getDao(context);
			players = pDao.queryBuilder().where().eq(Player.IS_ACTIVE, true).query();
			sessions = sDao.queryBuilder().where().eq(Session.IS_ACTIVE, true).query();
			venues = vDao.queryBuilder().where().eq(Venue.IS_ACTIVE, true).query();
		}
		catch (SQLException e){
			Log.e(PolishScorebook.class.getName(), "Could not get objects", e);
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}
		playerNames.clear();
		sessionNames.clear();
		venueNames.clear();
		for(Player p: players){
			playerNames.add(p.getFirstName() + " " + p.getLastName());
		}
		for(Session s: sessions){
			sessionNames.add(String.valueOf(s.getId())+" "+s.getSessionName());
		}
		for(Venue v: venues){
			venueNames.add(String.valueOf(v.getId())+" "+v.getName());
		}

		ArrayAdapter<String> pAdapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_spinner_dropdown_item,
				playerNames);
		pAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		ArrayAdapter<String> sAdapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_spinner_dropdown_item,
				sessionNames);
		sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		ArrayAdapter<String> vAdapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_spinner_dropdown_item,
				venueNames);
		vAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		
		spinner_p1.setAdapter(pAdapter);
		spinner_p2.setAdapter(pAdapter);
		
		spinner_session.setAdapter(sAdapter);
		spinner_venue.setAdapter(vAdapter);
	}
	
	public void createGame(View view){
		Player p1 = players.get(p1_pos);
		Player p2 = players.get(p2_pos);
		Game g = new Game(p1, p2, sessions.get(session_pos), venues.get(venue_pos), true);
		long gid; 
		g.setDatePlayed(new Date());
		
		try{
			Dao<Game, Long> d = Game.getDao(getApplicationContext());
			d.createIfNotExists(g);
			gid = g.getId();
			Intent intent = new Intent(this, GameInProgress.class);
			intent.putExtra("GID", gid);
	    	startActivity(intent);
	    	finish();
			
		}
		catch(SQLException e){
			Toast.makeText(getApplicationContext(), 
					e.getMessage(), 
					Toast.LENGTH_LONG).show();
		}
	}


}
