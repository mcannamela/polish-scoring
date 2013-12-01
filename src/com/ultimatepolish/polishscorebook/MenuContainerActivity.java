package com.ultimatepolish.polishscorebook;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.ultimatepolish.scorebookdb.DatabaseHelper;

public class MenuContainerActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	public static String LOGTAG = "MenuContainer";

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.modifyButton:
			openModifyActivity();
			return true;
		case R.id.dbSettings:
			openDbSettingsActivity();
			return true;
		case R.id.preferences:
			openPreferencesActivity();
			return true;
		case R.id.about:
			openAboutActivity();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void openModifyActivity() {
		// this wont do anything unless overridden in the file that extends this
		// class
	}

	public void openDbSettingsActivity() {
		Intent intent = new Intent(this, DbSettings.class);
		startActivity(intent);
	}

	public void openPreferencesActivity() {
		Intent intent = new Intent(this, Preferences.class);
		startActivity(intent);
	}

	public void openAboutActivity() {
		Intent intent = new Intent(this, AboutPage.class);
		startActivity(intent);
	}

	public void log(String msg) {
		Log.i(LOGTAG, msg);
	}

	public void logd(String msg) {
		Log.d(LOGTAG, msg);
	}

	public void loge(String msg, Exception e) {
		Log.e(LOGTAG, msg + ": " + e.getMessage());
	}
}
