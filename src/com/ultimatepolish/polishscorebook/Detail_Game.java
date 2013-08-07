package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.ultimatepolish.scorebookdb.Game;
import com.ultimatepolish.scorebookdb.Player;
import com.ultimatepolish.scorebookdb.Session;
import com.ultimatepolish.scorebookdb.Throw;
import com.ultimatepolish.scorebookdb.Venue;

public class Detail_Game extends MenuContainerActivity {
	Long gId;
	Game g;
	Player[] p = new Player[2];
	Dao<Game, Long> gDao;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail_game);
		
		Intent intent = getIntent();
		gId = intent.getLongExtra("GID", -1);
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
		Intent intent = new Intent(getApplicationContext(), GameInProgress.class);
        intent.putExtra("GID", gId);
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
		if (gId != -1){
			try{
				Context context = getApplicationContext();
				gDao = Game.getDao(context);
				Dao<Player, Long> playerDao = Player.getDao(context);
				Dao<Session, Long> sessionDao = Session.getDao(context);
				Dao<Venue, Long> venueDao = Venue.getDao(context);
				
				g = gDao.queryForId(gId);
				playerDao.refresh(g.getFirstPlayer());
				playerDao.refresh(g.getSecondPlayer());
				
				sessionDao.refresh(g.getSession());
				venueDao.refresh(g.getVenue());
				
				p[0] = g.getFirstPlayer();
				p[1] = g.getSecondPlayer();
			}
			catch (SQLException e){
				Toast.makeText(getApplicationContext(), 
						e.getMessage(), 
						Toast.LENGTH_LONG).show();
			}
		}
		
		TextView gameP1 = (TextView) findViewById(R.id.gDet_p1);
		gameP1.setText(p[0].getNickName());
		
		TextView gameP2 = (TextView) findViewById(R.id.gDet_p2);
		gameP2.setText(p[1].getNickName());
		
		TextView gameId = (TextView) findViewById(R.id.gDet_id);
		gameId.setText(String.valueOf(g.getId()));
		
		TextView gameSession = (TextView) findViewById(R.id.gDet_session);
		gameSession.setText(g.getSession().getSessionName());
		
		TextView gameVenue = (TextView) findViewById(R.id.gDet_venue);
		gameVenue.setText(g.getVenue().getName());
		
		TextView gameScore = (TextView) findViewById(R.id.gDet_score);
		gameScore.setText(String.valueOf(g.getFirstPlayerScore()) + "/" + String.valueOf(g.getSecondPlayerScore()));

		DateFormat df = new SimpleDateFormat("EEE MMM dd, yyyy @HH:mm", Locale.US);
		TextView gameDate = (TextView) findViewById(R.id.gDet_date);
		gameDate.setText(df.format(g.getDatePlayed()));
	}
	
	public void deleteGame(View view){
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext());
		alertDialogBuilder.setTitle("Delete this game?");
		alertDialogBuilder.setMessage("This action can not be undone.")
			.setPositiveButton("Delete",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					try{
						Dao<Throw, Long> tDao = Throw.getDao(getApplicationContext());
						DeleteBuilder<Throw, Long> tdb = tDao.deleteBuilder();
				        tdb.where().eq(Throw.GAME_ID, g.getId());
				        tDao.delete(tdb.prepare());
				        
						gDao.deleteById(g.getId());
						finish();
					}
					catch (SQLException e){
						Toast.makeText(getApplicationContext(), 
								e.getMessage(), 
								Toast.LENGTH_LONG).show();
					}
				}
			  })
			.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
				}
			});
 
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		
	}
	
	
}
