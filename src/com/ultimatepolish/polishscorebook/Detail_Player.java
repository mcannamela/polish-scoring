package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.db.Player;
import com.ultimatepolish.db.Throw;
import com.ultimatepolish.polishscorebook.backend.MenuContainerActivity;
import com.ultimatepolish.throwstats.IndicatorNode;
import com.ultimatepolish.throwstats.ReadingVisitor;
import com.ultimatepolish.throwstats.SimpleThrowStats;

public class Detail_Player extends MenuContainerActivity {
	Long pId;
	Player p;
	Dao<Player, Long> pDao;
	Dao<Throw, Long> tDao;

	public static String LOGTAG = "DET_PLAYER";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail_player);

		Intent intent = getIntent();
		pId = intent.getLongExtra("PID", -1);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem fav = menu.add(R.string.menu_modify);
		fav.setIcon(R.drawable.ic_action_edit);
		fav.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		Intent intent = new Intent(this, NewPlayer.class);
		intent.putExtra("PID", pId);

		fav.setIntent(intent);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		refreshDetails();
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshDetails();
	}

	public void refreshDetails() {
		if (pId != -1) {
			try {
				pDao = Player.getDao(getApplicationContext());
				p = pDao.queryForId(pId);

				tDao = Throw.getDao(getApplicationContext());
			} catch (SQLException e) {
				Toast.makeText(getApplicationContext(), e.getMessage(),
						Toast.LENGTH_LONG).show();
			}
		}

		TextView pName = (TextView) findViewById(R.id.pDet_name);
		pName.setText(p.getFirstName() + ' ' + p.getLastName() + " ("
				+ p.getNickName() + ")");

		TextView playerId = (TextView) findViewById(R.id.pDet_id);
		playerId.setText(String.valueOf(p.getId()));

		TextView pHeight = (TextView) findViewById(R.id.pDet_height);
		pHeight.setText("Height: " + String.valueOf(p.getHeight_cm()) + " cm");

		TextView pWeight = (TextView) findViewById(R.id.pDet_weight);
		pWeight.setText("Weight: " + String.valueOf(p.getWeight_kg()) + " kg");

		TextView pWinRatio = (TextView) findViewById(R.id.pDet_winRatio);
		// pWinRatio.setText(String.valueOf(p.getnWins()) + "/" +
		// String.valueOf(p.getnLosses()));
		pWinRatio.setText("Win Ratio here eventually...");

		TextView pHanded = (TextView) findViewById(R.id.pDet_handed);
		if (p.throwsLeftHanded) {
			if (p.throwsRightHanded) {
				pHanded.setText("L + R");
			} else {
				pHanded.setText("L");
			}
		} else {
			pHanded.setText("R");
		}

		TextView pStatsSummary = (TextView) findViewById(R.id.pDet_statsSummary);
		pStatsSummary.setText("Stats \n will \n go \n here");
		View oStatButton = findViewById(R.id.button_ComputeOffensiveStats);
		computeOffensiveStats(oStatButton);
	}

	public void computeOffensiveStats(View view) {
		log("Will now compute offensive stats");
		log("Summoning throws from db");
		List<Throw> tList;

		try {
			tList = tDao.queryForEq(Throw.OFFENSIVE_PLAYER, pId);
		} catch (SQLException e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_LONG).show();
			log("NO STATS FOR YOU!");
			return;
		}

		String stats = computeStats(tList);
		
		TextView pStatsSummary = (TextView) findViewById(R.id.pDet_statsSummary);
		pStatsSummary.setText("OFFENSIVE STATS\n"+stats);
	}

	public void computeDefensiveStats(View view) {
		log("Will now compute defensive stats");
		log("Summoning throws from db");
		List<Throw> tList;

		try {
			tList = tDao.queryForEq(Throw.DEFENSIVE_PLAYER, pId);
		} catch (SQLException e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_LONG).show();
			log("NO STATS FOR YOU!");
			return;
		}

		String stats = computeStats(tList); 
		
		TextView pStatsSummary = (TextView) findViewById(R.id.pDet_statsSummary);
		pStatsSummary.setText("DEFENSIVE STATS\n"+stats);
	}

	public String computeStats(List<Throw> tList) {

		log("Aggregating...");
		SimpleThrowStats sts = new SimpleThrowStats(tList);
		log("Tree is built");

		IndicatorNode statsTree = sts.computeStats();
		log("Stats are computed");
		ReadingVisitor rv = new ReadingVisitor();
		rv.visit(statsTree);
		log("Stats are read");

		String stats = "";
		int cnt = 0;
		for (String stat : rv) {
//			log(stat);
			stats += stat + "\n";
			cnt++;
		}
		return stats;

		

	}
}
