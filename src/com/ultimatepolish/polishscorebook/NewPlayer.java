package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;
import java.util.Locale;

import yuku.ambilwarna.AmbilWarnaDialog;
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.db.Player;
import com.ultimatepolish.polishscorebook.backend.MenuContainerActivity;

public class NewPlayer extends MenuContainerActivity {
	Long pId;
	Player p;
	Dao<Player, Long> pDao;

	TextView name;
	TextView nick;
	TextView weight;
	TextView height;
	CheckBox rh;
	CheckBox lh;
	CheckBox prefL;
	CheckBox prefR;
	Button playerColorBtn;
	int playerColor = Color.BLACK;
	CheckBox isActiveCB;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_player);

		Button createButton = (Button) findViewById(R.id.button_createPlayer);
		name = (TextView) findViewById(R.id.editText_playerName);
		nick = (TextView) findViewById(R.id.editText_nickname);
		weight = (TextView) findViewById(R.id.editText_weight);
		height = (TextView) findViewById(R.id.editText_height);
		rh = (CheckBox) findViewById(R.id.checkBox_throwsRightHanded);
		lh = (CheckBox) findViewById(R.id.checkBox_throwsLeftHanded);
		prefR = (CheckBox) findViewById(R.id.checkBox_prefersRightSide);
		prefL = (CheckBox) findViewById(R.id.checkBox_prefersLeftSide);
		playerColorBtn = (Button) findViewById(R.id.newPlayer_colorPicker);
		isActiveCB = (CheckBox) findViewById(R.id.newPlayer_isActive);

		Intent intent = getIntent();
		pId = intent.getLongExtra("PID", -1);
		if (pId != -1) {
			try {
				pDao = Player.getDao(getApplicationContext());
				p = pDao.queryForId(pId);
				createButton.setText("Modify");
				name.setText(p.getFirstName() + " " + p.getLastName());
				nick.setText(p.getNickName());
				weight.setText(String.valueOf(p.getWeight_kg()));
				height.setText(String.valueOf(p.getHeight_cm()));
				if (p.throwsLeftHanded == true) {
					lh.setChecked(true);
				}
				if (p.throwsRightHanded == true) {
					rh.setChecked(true);
				}
				if (p.prefersLeftSide == true) {
					prefL.setChecked(true);
				}
				if (p.prefersRightSide == true) {
					prefR.setChecked(true);
				}
				playerColorBtn.setBackgroundColor(p.getColor());
				playerColor = p.getColor();
				isActiveCB.setVisibility(View.VISIBLE);
				isActiveCB.setChecked(p.getIsActive());
			} catch (SQLException e) {
				Toast.makeText(getApplicationContext(), e.getMessage(),
						Toast.LENGTH_LONG).show();
			}
		}
	}

	public void createNewPlayer(View view) {
		Context context = getApplicationContext();
		Player newPlayer = null;
		String firstName = null;
		String lastName = null;
		String nickname = null;

		int height_cm = -1;
		int weight_kg = -1;

		Boolean throwsRightHanded = rh.isChecked();
		Boolean throwsLeftHanded = lh.isChecked();
		Boolean prefersRightSide = prefR.isChecked();
		Boolean prefersLeftSide = prefL.isChecked();

		byte[] emptyImage = new byte[0];

		Boolean isActive = isActiveCB.isChecked();

		String s = name.getText().toString();
		String[] toks;

		toks = s.split("\\s+");
		if (toks.length >= 2) {
			firstName = toks[0].toLowerCase(Locale.US);
			lastName = toks[1].toLowerCase(Locale.US);
		}

		s = nick.getText().toString().trim().toLowerCase(Locale.US);
		if (!s.isEmpty()) {
			nickname = new String(s);
		}

		s = weight.getText().toString().trim();
		if (!s.isEmpty()) {
			weight_kg = Integer.parseInt(s);
		}

		s = height.getText().toString().trim();
		if (!s.isEmpty()) {
			height_cm = Integer.parseInt(s);
		}

		if (pId != -1) {
			p.setFirstName(firstName);
			p.setLastName(lastName);
			p.setNickName(nickname);
			p.setWeight_kg(weight_kg);
			p.setHeight_cm(height_cm);
			p.setLeftHanded(throwsLeftHanded);
			p.setRightHanded(throwsRightHanded);
			p.setPrefersLeftSide(prefersLeftSide);
			p.setPrefersRightSide(prefersRightSide);
			p.setColor(playerColor);
			p.setIsActive(isActive);
			try {
				pDao.update(p);
				Toast.makeText(context, "Player modified.", Toast.LENGTH_SHORT)
						.show();
				finish();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Toast.makeText(context, "Could not modify player.",
						Toast.LENGTH_SHORT).show();
			}

		} else {
			newPlayer = new Player(firstName, lastName, nickname,
					throwsRightHanded, throwsLeftHanded, prefersRightSide,
					prefersLeftSide, height_cm, weight_kg, emptyImage,
					playerColor);

			try {
				Dao<Player, Long> dao = getHelper().getPlayerDao();
				dao.create(newPlayer);
				Toast.makeText(context, "Player created!", Toast.LENGTH_SHORT)
						.show();
				finish();
			} catch (SQLException e) {
				Log.e(PolishScorebook.class.getName(),
						"Could not create player.", e);
				boolean player_exists = false;
				try {
					player_exists = newPlayer.exists(context);
					if (player_exists) {
						Toast.makeText(context, "Player already exists.",
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(context, "Could not create player.",
								Toast.LENGTH_SHORT).show();
					}
				} catch (SQLException ee) {
					Toast.makeText(context, ee.getMessage(), Toast.LENGTH_LONG)
							.show();
					Log.e(PolishScorebook.class.getName(),
							"Could not test for existence of player", ee);
				}
			}
		}
	}

	public void showColorPicker(View view) {
		// initialColor is the initially-selected color to be shown in the
		// rectangle on the left of the arrow.
		// for example, 0xff000000 is black, 0xff0000ff is blue. Please be aware
		// of the initial 0xff which is the alpha.
		AmbilWarnaDialog dialog = new AmbilWarnaDialog(view.getContext(),
				playerColor, new OnAmbilWarnaListener() {
					@Override
					public void onOk(AmbilWarnaDialog dialog, int color) {
						playerColor = color;
						playerColorBtn.setBackgroundColor(color);
					}

					@Override
					public void onCancel(AmbilWarnaDialog dialog) {
						// cancel was selected by the user
					}
				});

		dialog.show();
	}
}
