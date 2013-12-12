package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.scorebookdb.Player;
import com.ultimatepolish.scorebookdb.Throw;
import com.ultimatepolish.throwstats.IndicatorNode;
import com.ultimatepolish.throwstats.ReadingVisitor;
import com.ultimatepolish.throwstats.SimpleThrowStats;

public class Detail_Player extends MenuContainerActivity {
	Long pId;
	Player p;
	Dao<Player, Long> pDao;
	Dao<Throw, Long> tDao;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail_player);
		
		Intent intent = getIntent();
		pId = intent.getLongExtra("PID", -1);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);		
		menu.findItem(R.id.modifyButton).setVisible(true);
		return true;
	}
	@Override
	public void openModifyActivity() {
		Intent intent = new Intent(getApplicationContext(), NewPlayer.class);
        intent.putExtra("PID", pId);
        startActivity(intent);
    }
	@Override
    protected void onRestart(){
    	super.onRestart();
    	refreshDetails();
    }
    @Override
    protected void onResume(){
    	super.onResume();
    	refreshDetails();
    }
	
	public void refreshDetails(){
		if (pId != -1){
			try{
				pDao = Player.getDao(getApplicationContext());
				p = pDao.queryForId(pId);
				
				tDao = Throw.getDao(getApplicationContext());
			}
			catch (SQLException e){
				Toast.makeText(getApplicationContext(), 
						e.getMessage(), 
						Toast.LENGTH_LONG).show();
			}
		}
		
		TextView pName = (TextView) findViewById(R.id.pDet_name);
		pName.setText(p.getFirstName() + ' ' + p.getLastName() + " (" + p.getNickName() + ")");
		
		TextView playerId = (TextView) findViewById(R.id.pDet_id);
		playerId.setText(String.valueOf(p.getId()));
		
		TextView pHeight = (TextView) findViewById(R.id.pDet_height);
		pHeight.setText("Height: " + String.valueOf(p.getHeight_cm()) + " cm");
		
		TextView pWeight = (TextView) findViewById(R.id.pDet_weight);
		pWeight.setText("Weight: " + String.valueOf(p.getWeight_kg()) + " kg");
		
		TextView pWinRatio = (TextView) findViewById(R.id.pDet_winRatio);
//		pWinRatio.setText(String.valueOf(p.getnWins()) + "/" + String.valueOf(p.getnLosses()));
		pWinRatio.setText("Win Ratio here eventually...");
		
		TextView pHanded = (TextView) findViewById(R.id.pDet_handed);
		if (p.throwsLeftHanded) {
			if (p.throwsRightHanded) {
				pHanded.setText("L + R");
			}
			else { pHanded.setText("L");
			}
		}
		else {pHanded.setText("R");
		}
		
		TextView pStatsSummary = (TextView) findViewById(R.id.pDet_statsSummary);
		pStatsSummary.setText("Stats \n will \n go \n here");
	}
	
	public void computeStats(View view){
		TextView pStatsSummary = (TextView) findViewById(R.id.pDet_statsSummary);
		pStatsSummary.setText("Summoning throws from db");
		List<Throw> tList;
		
		try{
			tList = tDao.queryForEq(Throw.OFFENSIVE_PLAYER, pId);
		}
		catch (SQLException e){
			Toast.makeText(getApplicationContext(), 
					e.getMessage(), 
					Toast.LENGTH_LONG).show();
			pStatsSummary.setText("NO STATS FOR YOU!");
			return;
		}
		
		pStatsSummary.setText("Aggregating...");
		SimpleThrowStats sts = new SimpleThrowStats(tList);
		IndicatorNode statsTree = sts.computeStats();
		ReadingVisitor rv = new ReadingVisitor();
		rv.visit(statsTree);
		
		String stats = "";
		for (String stat : rv) {
			stats+= stat+"\n";
		}
		
		pStatsSummary.setText(stats);
		
	}
}
