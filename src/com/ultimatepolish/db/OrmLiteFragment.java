package com.ultimatepolish.db;

import android.app.Fragment;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;

public class OrmLiteFragment extends Fragment {
	public static String LOGTAG = "OrmLiteFragment";

	private DatabaseHelper databaseHelper = null;

	protected DatabaseHelper getHelper() {
		if (databaseHelper == null) {
			databaseHelper = OpenHelperManager.getHelper(getActivity(),
					DatabaseHelper.class);
		}
		return databaseHelper;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (databaseHelper != null) {
			OpenHelperManager.releaseHelper();
			databaseHelper = null;
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