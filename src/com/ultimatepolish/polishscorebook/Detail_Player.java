package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.scorebookdb.Player;

public class Detail_Player extends MenuContainerActivity {
	Long pId;
	Player p;
	Dao<Player, Long> pDao;
	
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
	}
}
