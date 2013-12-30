package com.ultimatepolish.polishscorebook.backend;

import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.ultimatepolish.db.DatabaseHelper;

public class MenuContainerActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	public static String LOGTAG = "MenuContainer";

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
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
