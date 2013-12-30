package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.db.Session;
import com.ultimatepolish.enums.RuleType;
import com.ultimatepolish.enums.SessionType;
import com.ultimatepolish.polishscorebook.backend.Bracket;
import com.ultimatepolish.polishscorebook.backend.Bracket.MatchInfo;
import com.ultimatepolish.polishscorebook.backend.MenuContainerActivity;

public class Detail_Session extends MenuContainerActivity {
	public static String LOGTAG = "Detail_Session";
	Long sId;
	Session s;
	Dao<Session, Long> sDao;
	Bracket bracket = null;
	TextView matchText;
	Button loadMatch;
	MatchInfo mInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		sId = intent.getLongExtra("SID", -1);

		if (sId != -1) {
			try {
				sDao = Session.getDao(getApplicationContext());

				s = sDao.queryForId(sId);
			} catch (SQLException e) {
				Toast.makeText(getApplicationContext(), e.getMessage(),
						Toast.LENGTH_LONG).show();
			}
		}

		if (s.sessionType == SessionType.SNGL_ELIM
				|| s.sessionType == SessionType.DBL_ELIM) {
			boolean isDblElim = s.sessionType == SessionType.DBL_ELIM;

			setContentView(R.layout.activity_detail_session_singleelim);
			ScrollView sv = (ScrollView) findViewById(R.id.scrollView1);

			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);

			matchText = (TextView) findViewById(R.id.sDet_match);
			loadMatch = (Button) findViewById(R.id.sDet_loadMatch);

			bracket = new Bracket(sv, s, isDblElim) {
				@Override
				public void onClick(View v) {
					mInfo = bracket.getMatchInfo(v.getId());
					matchText.setText(mInfo.marquee);
					if (mInfo.allowCreate) {
						loadMatch.setVisibility(View.VISIBLE);
						loadMatch.setText("Create");
						loadMatch.setOnClickListener(mCreateMatchListener);
						loadMatch.setOnLongClickListener(null);
					} else if (mInfo.allowView) {
						loadMatch.setVisibility(View.VISIBLE);
						loadMatch.setText("View Match");
						loadMatch.setOnClickListener(mViewMatchListener);
						loadMatch.setOnLongClickListener(mMatchGIPListener);
					} else {
						loadMatch.setVisibility(View.GONE);
					}
				}
			};
			sv.addView(bracket.rl);
		} else {
			setContentView(R.layout.activity_detail_session);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem fav = menu.add(R.string.menu_modify);
		fav.setIcon(R.drawable.ic_action_edit);
		fav.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		Intent intent = new Intent(this, NewSession.class);
		intent.putExtra("SID", sId);

		fav.setIntent(intent);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshDetails();
		if (bracket != null) {
			bracket.refreshWinnersBracket();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		// TODO: move this to bracket.java
		// try {
		// for (SessionMember sm: sMembers) {
		// smDao.update(sm);
		// }
		// }
		// catch (SQLException e) {
		// Toast.makeText(getApplicationContext(), e.getMessage(),
		// Toast.LENGTH_LONG).show();
		// }
	}

	public void refreshDetails() {
		TextView sName = (TextView) findViewById(R.id.sDet_name);
		sName.setText(s.getSessionName());

		TextView sId = (TextView) findViewById(R.id.sDet_id);
		sId.setText(String.valueOf(s.getId()));

		TextView sType = (TextView) findViewById(R.id.sDet_type);
		sType.setText(SessionType.typeString[s.getSessionType()]);

		TextView sessionRuleSet = (TextView) findViewById(R.id.sDet_ruleSet);
		if (s.getRuleSetId() == -1) {
			sessionRuleSet.setText("Ruleset not enforced.");
		} else {
			sessionRuleSet.setText("("
					+ RuleType.map.get(s.getRuleSetId()).getId() + ") "
					+ RuleType.map.get(s.getRuleSetId()).getDescription());
		}

		TextView sStartDate = (TextView) findViewById(R.id.sDet_startDate);
		sStartDate.setText("Start date: " + String.valueOf(s.getStartDate()));

		TextView sEndDate = (TextView) findViewById(R.id.sDet_endDate);
		sEndDate.setText("End date: " + String.valueOf(s.getEndDate()));

		TextView sIsTeam = (TextView) findViewById(R.id.sDet_isTeam);
		if (s.getIsTeam()) {
			sIsTeam.setText("Doubles session");
		} else {
			sIsTeam.setText("Singles session");
		}

		TextView sIsActive = (TextView) findViewById(R.id.sDet_isActive);
		if (s.getIsActive()) {
			sIsActive.setText("This session is active");
		} else {
			sIsActive.setText("This session is no longer active");
		}
	}

	private OnClickListener mViewMatchListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(v.getContext(), Detail_Game.class);
			intent.putExtra("GID", mInfo.gameId);
			startActivity(intent);
		}
	};

	private OnLongClickListener mMatchGIPListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			Intent intent = new Intent(v.getContext(), GameInProgress.class);
			intent.putExtra("GID", mInfo.gameId);
			startActivity(intent);
			finish();
			return true;
		}
	};

	private OnClickListener mCreateMatchListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(v.getContext(), NewGame.class);
			intent.putExtra("p1", mInfo.p1Id);
			intent.putExtra("p2", mInfo.p2Id);
			intent.putExtra("sId", sId);
			startActivity(intent);
		}
	};
}
