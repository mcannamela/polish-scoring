package com.ultimatepolish.polishscorebook;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.ultimatepolish.scorebookdb.DatabaseHelper;

public class MenuContainerActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
		    case android.R.id.home:
	            NavUtils.navigateUpFromSameTask(this);
	            return true;
		    case R.id.addButton:
	            openAddActivity();
	            return true;
	        case R.id.games:
	            openGamesActivity();
	            return true;
	        case R.id.players:
	            openPlayersActivity();
	            return true;
	        case R.id.teams:
	            openTeamsActivity();
	            return true;
	        case R.id.venues:
	            openVenuesActivity();
	            return true;
	        case R.id.sessions:
	            openSessionsActivity();
	            return true;
	        case R.id.modifyButton:
	            openModifyActivity();
	            return true;
	        case R.id.settings:
	            openSettingsActivity();
	            return true;
	        case R.id.about:
	            openAboutActivity();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	public void openAddActivity() {
    	// this wont do anything unless overridden in the file that extends this class
    }
	public void openModifyActivity() {
    	// this wont do anything unless overridden in the file that extends this class
    }
    public void openGamesActivity() {
    	Intent intent = new Intent(this, View_Games.class);
    	startActivity(intent);
    }
    public void openPlayersActivity() {
    	Intent intent = new Intent(this, View_Players.class);
    	startActivity(intent);
    }
    public void openTeamsActivity() {
    	Intent intent = new Intent(this, View_Teams.class);
    	startActivity(intent);
    }
    public void openVenuesActivity(){
    	Intent intent = new Intent(this, View_Venues.class);
    	startActivity(intent);
    }
    public void openSessionsActivity(){
    	Intent intent = new Intent(this, View_Sessions.class);
    	startActivity(intent);
    }
    public void openSettingsActivity(){
    	Intent intent = new Intent(this, SimpleSettings.class);
    	startActivity(intent);
    }
    public void openAboutActivity() {
    	Intent intent = new Intent(this, AboutPage.class);
    	startActivity(intent);
    }
}
