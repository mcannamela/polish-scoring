package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.db.Venue;
import com.ultimatepolish.polishscorebook.backend.MenuContainerActivity;

public class Detail_Venue extends MenuContainerActivity {
	Long vId;
	Venue v;
	Dao<Venue, Long> vDao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail_venue);

		Intent intent = getIntent();
		vId = intent.getLongExtra("VID", -1);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem fav = menu.add(R.string.menu_modify);
		fav.setIcon(R.drawable.ic_action_edit);
		fav.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		Intent intent = new Intent(this, NewVenue.class);
		intent.putExtra("VID", vId);

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
		if (vId != -1) {
			try {
				vDao = Venue.getDao(getApplicationContext());
				v = vDao.queryForId(vId);
			} catch (SQLException e) {
				Toast.makeText(getApplicationContext(), e.getMessage(),
						Toast.LENGTH_LONG).show();
			}
		}

		TextView vName = (TextView) findViewById(R.id.vDet_name);
		vName.setText(v.getName());

		TextView vId = (TextView) findViewById(R.id.vDet_id);
		vId.setText(String.valueOf(v.getId()));

		TextView pFromTop = (TextView) findViewById(R.id.vDet_fromTop);
		if (v.scoreKeptFromTop) {
			pFromTop.setText("Scored from top");
		} else {
			pFromTop.setText("Scored from bottom");
		}

		TextView vIsActive = (TextView) findViewById(R.id.vDet_isActive);
		if (v.getIsActive()) {
			vIsActive.setText("This venue is active.");
		} else {
			vIsActive.setText("This venue is not active.");
		}
	}
}
