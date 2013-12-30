package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.db.Venue;
import com.ultimatepolish.polishscorebook.backend.MenuContainerActivity;

public class NewVenue extends MenuContainerActivity {
	Long vId;
	Venue v;
	Dao<Venue, Long> vDao;

	TextView name;
	CheckBox sfTop;
	CheckBox isActiveCB;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_venue);

		name = (TextView) findViewById(R.id.editText_venueName);
		sfTop = (CheckBox) findViewById(R.id.checkBox_scoreKeptFromTop);
		Button createButton = (Button) findViewById(R.id.button_createVenue);
		isActiveCB = (CheckBox) findViewById(R.id.newVenue_isActive);

		Intent intent = getIntent();
		vId = intent.getLongExtra("VID", -1);
		if (vId != -1) {
			try {
				vDao = Venue.getDao(getApplicationContext());
				v = vDao.queryForId(vId);
				createButton.setText("Modify");
				name.setText(v.getName());
				sfTop.setChecked(v.scoreKeptFromTop);
				isActiveCB.setVisibility(View.VISIBLE);
				isActiveCB.setChecked(v.getIsActive());
			} catch (SQLException e) {
				Toast.makeText(getApplicationContext(), e.getMessage(),
						Toast.LENGTH_LONG).show();
			}
		}
	}

	public void createNewVenue(View view) {
		Context context = getApplicationContext();
		Venue venue = null;
		String venueName = null;
		boolean scoreKeptFromTop;

		String s;
		s = name.getText().toString().trim().toLowerCase(Locale.US);
		if (!s.isEmpty()) {
			venueName = new String(s);
		}

		if (vId != -1) {
			v.setName(venueName);
			v.setScoreFromTop(sfTop.isChecked());
			v.setIsActive(isActiveCB.isChecked());
			try {
				vDao.update(v);
				Toast.makeText(context, "Venue modified.", Toast.LENGTH_SHORT)
						.show();
				finish();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Toast.makeText(context, "Could not modify venue.",
						Toast.LENGTH_SHORT).show();
			}
		} else {
			venue = new Venue(venueName, sfTop.isChecked());

			try {
				Dao<Venue, Long> dao = getHelper().getVenueDao();
				dao.create(venue);
				Toast.makeText(context, "Venue created!", Toast.LENGTH_SHORT)
						.show();
				finish();
			} catch (SQLException e) {
				Log.e(PolishScorebook.class.getName(),
						"Could not create venue.", e);
				Toast.makeText(context, "Could not create venue.",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

}
