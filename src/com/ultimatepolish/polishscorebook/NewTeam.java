package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.db.Player;
import com.ultimatepolish.db.Team;
import com.ultimatepolish.polishscorebook.backend.MenuContainerActivity;

public class NewTeam extends MenuContainerActivity {
	Long tId;
	Team t;
	Dao<Team, Long> tDao;

	TextView name;
	Spinner p1;
	Spinner p2;
	int p1_pos = 0;
	int p2_pos = 1;
	CheckBox isActiveCB;

	List<Player> players = new ArrayList<Player>();
	List<String> playerNames = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_team);

		name = (TextView) findViewById(R.id.editText_teamName);
		p1 = (Spinner) findViewById(R.id.spinner_player1);
		p2 = (Spinner) findViewById(R.id.spinner_player2);
		Button createButton = (Button) findViewById(R.id.button_createTeam);
		isActiveCB = (CheckBox) findViewById(R.id.newTeam_isActive);

		refreshSpinners();

		p1.setOnItemSelectedListener(mPlayerOneSelectedHandler);
		p2.setOnItemSelectedListener(mPlayerTwoSelectedHandler);

		Intent intent = getIntent();
		tId = intent.getLongExtra("TID", -1);
		if (tId != -1) {
			try {
				Context context = getApplicationContext();
				// TODO: uncomment once teams are re-implemented
				// tDao = Team.getDao(context);
				t = tDao.queryForId(tId);
				createButton.setText("Modify");
				name.setText(t.getTeamName());

				// p1_pos = players.indexOf(t.getPlayers(context)[0]);
				// p2_pos = players.indexOf(t.getPlayers(context)[1]);
				// p1.setSelection(p1_pos);
				// p2.setSelection(p2_pos);
				p1.setVisibility(View.GONE);
				p2.setVisibility(View.GONE);
				isActiveCB.setVisibility(View.VISIBLE);
				isActiveCB.setChecked(t.getIsActive());
			} catch (SQLException e) {
				Toast.makeText(getApplicationContext(), e.getMessage(),
						Toast.LENGTH_LONG).show();
			}
		}
	}

	public void createNewTeam(View view) {
		Context context = getApplicationContext();
		Team newTeam = null;
		String teamName = null;
		Player[] chosenPlayers = new Player[2];

		String s = name.getText().toString().trim().toLowerCase(Locale.US);
		if (!s.isEmpty()) {
			teamName = new String(s);
		}
		chosenPlayers[0] = players.get(p1_pos);
		chosenPlayers[1] = players.get(p2_pos);

		// check that they are different players.

		if (tId != -1) {
			t.setTeamName(teamName);
			// t.setImageBytes(imageBytes);
			t.setIsActive(isActiveCB.isChecked());
			try {
				tDao.update(t);
				Toast.makeText(context, "Team modified.", Toast.LENGTH_SHORT)
						.show();
				finish();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Toast.makeText(context, "Could not modify team.",
						Toast.LENGTH_SHORT).show();
			}

		} else {
			// newTeam = new Team(teamName, chosenPlayers);

			// try{
			// Dao<Team, Long> dao = getHelper().getTeamDao();
			// dao.create(newTeam);
			// Toast.makeText(context, "Team created!",
			// Toast.LENGTH_SHORT).show();
			Toast.makeText(context, "Teams are disabled!", Toast.LENGTH_SHORT)
					.show();

			finish();
			// }
			// catch (SQLException e){
			// Log.e(PolishScorebook.class.getName(), "Could not create team.",
			// e);
			// boolean team_exists = false;
			// try{
			// player_exists = newPlayer.exists(context);
			// if (player_exists){
			// Toast.makeText(context, "Player already exists.",
			// Toast.LENGTH_SHORT).show();
			// }
			// else{
			// Toast.makeText(context, "Could not create player.",
			// Toast.LENGTH_SHORT).show();
			// }
			// }
			// catch (SQLException ee){
			// Toast.makeText(context, ee.getMessage(),
			// Toast.LENGTH_LONG).show();
			// Log.e(PolishScorebook.class.getName(),
			// "Could not test for existence of player", ee);
			// }
			// }
		}
	}

	private OnItemSelectedListener mPlayerOneSelectedHandler = new OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> parent, View v, int position,
				long id) {
			p1_pos = position;
		}

		public void onNothingSelected(AdapterView<?> parent) {
		}
	};
	private OnItemSelectedListener mPlayerTwoSelectedHandler = new OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> parent, View v, int position,
				long id) {
			p2_pos = position;
		}

		public void onNothingSelected(AdapterView<?> parent) {
		}
	};

	public void refreshSpinners() {
		Context context = getApplicationContext();
		try {
			players = Player.getAll(context);
		} catch (SQLException e) {
			Log.e(PolishScorebook.class.getName(), "Could not get objects", e);
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}
		playerNames.clear();

		for (Player p : players) {
			playerNames.add(p.getFirstName() + " " + p.getLastName());
		}

		ArrayAdapter<String> pAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, playerNames);
		pAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		p1.setAdapter(pAdapter);
		p2.setAdapter(pAdapter);
	}
}
