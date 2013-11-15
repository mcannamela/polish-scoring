package com.ultimatepolish.polishscorebook;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.scorebookdb.ActiveGame;
import com.ultimatepolish.scorebookdb.DeadType;
import com.ultimatepolish.scorebookdb.Throw;
import com.ultimatepolish.scorebookdb.ThrowResult;
import com.ultimatepolish.scorebookdb.ThrowType;

public class GameInProgress extends MenuContainerActivity implements
		ThrowTableFragment.OnTableRowClickedListener {
	public static String LOGTAG = "GIP";
	private FragmentArrayAdapter vpAdapter;
	private List<ThrowTableFragment> fragmentArray = new ArrayList<ThrowTableFragment>(
			0);
	private ViewPager vp;
	private View[] deadViews = new View[4];
	private View naView;
	NumberPicker resultNp;

	public ActiveGame ag;
	Dao<Throw, Long> tDao;
	Throw uiThrow;

	// LISTENERS ==============================================================
	private OnValueChangeListener numberPickerChangeListener = new OnValueChangeListener() {
		public void onValueChange(NumberPicker parent, int oldVal, int newVal) {
			if (uiThrow.getThrowResult() == ThrowResult.BROKEN
					|| uiThrow.getThrowResult() == ThrowResult.NA) {
				/*
				 * if the numberpicker changed, unset broken and NA status so
				 * that updateActiveThrow can change the result. Otherwise the
				 * numberpicker is ignored.
				 */
				uiThrow.setThrowResult(ThrowResult.CATCH);
				naView.setBackgroundColor(Color.LTGRAY);
			}
			updateActiveThrow();
		}
	};

	private OnLongClickListener mLongClickListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View view) {
			log("mLongClickListener(): " + view.getContentDescription()
					+ " was long pressed");
			int buttonId = view.getId();

			switch (buttonId) {
			case R.id.gip_button_strike:
				uiThrow.isTipped = !uiThrow.isTipped;
				if (uiThrow.isTipped) {
					((ImageView) view).getDrawable().setLevel(2);
				} else {
					((ImageView) view).getDrawable().setLevel(0);
				}
				break;
			case R.id.gip_button_pole:
				uiThrow.setThrowType(ThrowType.POLE);
				if (uiThrow.getThrowResult() == ThrowResult.BROKEN) {
					uiThrow.setThrowResult(getThrowResultFromNP());
				} else {
					uiThrow.setThrowResult(ThrowResult.BROKEN);
				}
				setBrokenButtonState(uiThrow.getThrowType());
				break;
			case R.id.gip_button_cup:
				uiThrow.setThrowType(ThrowType.CUP);
				if (uiThrow.getThrowResult() == ThrowResult.BROKEN) {
					uiThrow.setThrowResult(getThrowResultFromNP());
				} else {
					uiThrow.setThrowResult(ThrowResult.BROKEN);
				}
				setBrokenButtonState(uiThrow.getThrowType());
				break;
			case R.id.gip_button_bottle:
				uiThrow.setThrowType(ThrowType.BOTTLE);
				if (uiThrow.getThrowResult() == ThrowResult.BROKEN) {
					uiThrow.setThrowResult(getThrowResultFromNP());
				} else {
					uiThrow.setThrowResult(ThrowResult.BROKEN);
				}
				setBrokenButtonState(uiThrow.getThrowType());
				break;
			case R.id.gip_button_high:
				if (uiThrow.getDeadType() == DeadType.HIGH) {
					uiThrow.setDeadType(DeadType.ALIVE);
				} else {
					uiThrow.setDeadType(DeadType.HIGH);
				}
				break;
			case R.id.gip_button_right:
				if (uiThrow.getDeadType() == DeadType.RIGHT) {
					uiThrow.setDeadType(DeadType.ALIVE);
				} else {
					uiThrow.setDeadType(DeadType.RIGHT);
				}
				break;
			case R.id.gip_button_low:
				if (uiThrow.getDeadType() == DeadType.LOW) {
					uiThrow.setDeadType(DeadType.ALIVE);
				} else {
					uiThrow.setDeadType(DeadType.LOW);
				}
				break;
			case R.id.gip_button_left:
				if (uiThrow.getDeadType() == DeadType.LEFT) {
					uiThrow.setDeadType(DeadType.ALIVE);
				} else {
					uiThrow.setDeadType(DeadType.LEFT);
				}
				break;
			default:
				break;
			}
			if (buttonId == R.id.gip_button_pole
					|| buttonId == R.id.gip_button_cup
					|| buttonId == R.id.gip_button_bottle) {
				confirmThrow();
			} else {
				updateActiveThrow();
			}
			return true;
		}
	};

	private class MyPageChangeListener extends
			ViewPager.SimpleOnPageChangeListener {
		@Override
		public void onPageSelected(int position) {
			super.onPageSelected(position);
			renderPage(position, false);
		}
	}

	public void onThrowClicked(int local_throw_idx) {
		int global_throw_idx = ThrowTableFragment.localThrowIdxToGlobal(
				vp.getCurrentItem(), local_throw_idx);
		if (global_throw_idx > ag.nThrows() - 1) {
			global_throw_idx = ag.nThrows() - 1;
		}
		gotoThrowIdx(global_throw_idx);
	}

	public void buttonPressed(View view) {
		log("buttonPressed(): " + view.getContentDescription() + " was pressed");
		int buttonId = view.getId();

		if (uiThrow.getThrowType() == ThrowType.TRAP
				|| uiThrow.getThrowType() == ThrowType.TRAP_REDEEMED) {
			switch (buttonId) {
			case R.id.gip_button_trap:
				uiThrow.setThrowType(ThrowType.NOT_THROWN);
				uiThrow.setThrowResult(getThrowResultFromNP());
				((ImageView) view).getDrawable().setLevel(0);
				break;
			case R.id.gip_button_bottle:
			case R.id.gip_button_pole:
			case R.id.gip_button_cup:
				uiThrow.setThrowType(ThrowType.TRAP_REDEEMED);
				confirmThrow();
				break;
			default:
				confirmThrow();
				break;
			}
		} else {
			switch (buttonId) {
			case R.id.gip_button_high:
				uiThrow.setThrowType(ThrowType.BALL_HIGH);
				break;
			case R.id.gip_button_low:
				uiThrow.setThrowType(ThrowType.BALL_LOW);
				break;
			case R.id.gip_button_left:
				uiThrow.setThrowType(ThrowType.BALL_LEFT);
				break;
			case R.id.gip_button_right:
				uiThrow.setThrowType(ThrowType.BALL_RIGHT);
				break;
			case R.id.gip_button_trap:
				uiThrow.setThrowType(ThrowType.TRAP);
				((ImageView) view).getDrawable().setLevel(2);
				break;
			case R.id.gip_button_short:
				uiThrow.setThrowType(ThrowType.SHORT);
				break;
			case R.id.gip_button_strike:
				uiThrow.setThrowType(ThrowType.STRIKE);
				break;
			case R.id.gip_button_bottle:
				uiThrow.setThrowType(ThrowType.BOTTLE);
				break;
			case R.id.gip_button_pole:
				uiThrow.setThrowType(ThrowType.POLE);
				break;
			case R.id.gip_button_cup:
				uiThrow.setThrowType(ThrowType.CUP);
				break;
			}

			if (uiThrow.getThrowType() != ThrowType.TRAP) {
				confirmThrow();
			}
		}
	}

	public void errorButtonPressed(View view) {
		log("errorButtonPressed(): " + view.getContentDescription()
				+ " was pressed");
		int buttonId = view.getId();

		if (buttonId == R.id.gip_ownGoal) {
			OwnGoalDialog();
		} else if (buttonId == R.id.gip_playerError) {
			PlayerErrorDialog();
		}
	}

	// INNER CLASSES ==========================================================
	private class FragmentArrayAdapter extends FragmentPagerAdapter {

		public FragmentArrayAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return fragmentArray.size();
		}

		@Override
		public Fragment getItem(int position) {
			return fragmentArray.get(position);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			// tv.setText("nThrows: "+ throwsList.size());

			String title = "Page " + String.valueOf(position + 1);
			return title;
		}

	}

	public class ZoomOutPageTransformer implements ViewPager.PageTransformer {
		private float MIN_SCALE = 0.85f;
		private float MIN_ALPHA = 0.5f;

		public void transformPage(View view, float position) {
			int pageWidth = view.getWidth();
			int pageHeight = view.getHeight();

			if (position < -1) { // [-Infinity,-1)
				// This page is way off-screen to the left.
				view.setAlpha(0);

			} else if (position <= 1) { // [-1,1]
				// Modify the default slide transition to shrink the page as
				// well
				float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
				float vertMargin = pageHeight * (1 - scaleFactor) / 2;
				float horzMargin = pageWidth * (1 - scaleFactor) / 2;
				if (position < 0) {
					view.setTranslationX(horzMargin - vertMargin / 2);
				} else {
					view.setTranslationX(-horzMargin + vertMargin / 2);
				}

				// Scale the page down (between MIN_SCALE and 1)
				view.setScaleX(scaleFactor);
				view.setScaleY(scaleFactor);

				// Fade the page relative to its size.
				view.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE)
						/ (1 - MIN_SCALE) * (1 - MIN_ALPHA));

			} else { // (1,+Infinity]
				// This page is way off-screen to the right.
				view.setAlpha(0);
			}
		}
	}

	public void OwnGoalDialog() {
		final boolean[] ownGoals = uiThrow.getOwnGoals();

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Own Goal")
				.setMultiChoiceItems(R.array.owngoals, ownGoals,
						new DialogInterface.OnMultiChoiceClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which, boolean isChecked) {
								if (isChecked) {
									// If the user checked the item, add it to
									// the selected items
									ownGoals[which] = true;
								} else {
									ownGoals[which] = false;
								}
								uiThrow.setOwnGoals(ownGoals);
								updateActiveThrow();
							}
						})
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {

					}
				});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private void PlayerErrorDialog() {
		final boolean[] defErrors = uiThrow.getDefErrors();

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Defensive Error")
				.setMultiChoiceItems(R.array.defErrors, defErrors,
						new DialogInterface.OnMultiChoiceClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which, boolean isChecked) {
								if (isChecked) {
									// If the user checked the item, add it to
									// the selected items
									defErrors[which] = true;
								} else {
									defErrors[which] = false;
								}
								uiThrow.setDefErrors(defErrors);
								updateActiveThrow();
							}
						})
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {

					}
				});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public static class GentlemensDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the Builder class for convenient dialog construction
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage("Time out, Gentlemen!").setPositiveButton(
					"resume", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
						}
					});
			// Create the AlertDialog object and return it
			return builder.create();
		}
	}

	// ANDROID CALLBACKS ======================================================
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		log("onCreate(): creating GIP");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_in_progress);

		Intent intent = getIntent();
		Long gId = intent.getLongExtra("GID", -1);

		Context context = getApplicationContext();
		tDao = Throw.getDao(context);
		ag = new ActiveGame(gId, context);
		uiThrow = ag.getActiveThrow();

		initMetadata();
		initNumPickers();
		initListeners();

		log("onCreate(): about to create fragments");
		initTableFragments();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		log("onResume(): vp's adapter has " + vpAdapter.getCount() + " items");
		gotoThrowIdx(ag.getActiveIdx());
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onPause() {
		super.onPause();
		ag.saveAllThrows();
		ag.saveGame();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	// INITIALIZATION =========================================================
	private void initMetadata() {
		DateFormat df = new SimpleDateFormat("EEE MMM dd, yyyy @HH:mm",
				Locale.US);
		TextView tv;

		// player names
		tv = (TextView) findViewById(R.id.textView_players);
		tv.setText(ag.getP1Name() + " " + getString(R.string.gip_vs_text) + " "
				+ ag.getP2Name());

		// session
		tv = (TextView) findViewById(R.id.textView_session);
		tv.setText(getString(R.string.gip_session_text) + " "
				+ ag.getSessionName());

		// venue
		tv = (TextView) findViewById(R.id.textView_venue);
		tv.setText(getString(R.string.gip_venue_text) + " " + ag.getVenueName());

		// date
		tv = (TextView) findViewById(R.id.textView_datePlayed);
		tv.setText(df.format(ag.getGameDate()));

		// game ID
		tv = (TextView) findViewById(R.id.textView_gId);
		tv.setText(getString(R.string.gip_gamenum_text)
				+ String.valueOf(ag.getGameId()));

		// table header
		tv = (TextView) findViewById(R.id.header_p1);
		tv.setText(ag.getP1Nick());
		tv.setTextColor(ThrowTableRow.tableTextColor);
		tv.setTextSize(ThrowTableRow.tableTextSize);

		tv = (TextView) findViewById(R.id.header_p2);
		tv.setText(ag.getP2Nick());
		tv.setTextColor(ThrowTableRow.tableTextColor);
		tv.setTextSize(ThrowTableRow.tableTextSize);

		deadViews[0] = findViewById(R.id.gip_dead_high);
		deadViews[1] = findViewById(R.id.gip_dead_right);
		deadViews[2] = findViewById(R.id.gip_dead_low);
		deadViews[3] = findViewById(R.id.gip_dead_left);

		naView = findViewById(R.id.gip_na_indicator);
	}

	private void initNumPickers() {
		resultNp = (NumberPicker) findViewById(R.id.numPicker_catch);
		resultNp.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		String[] catchText = new String[3];
		catchText[0] = getString(R.string.gip_drop);
		catchText[1] = getString(R.string.gip_catch);
		catchText[2] = getString(R.string.gip_stalwart);
		resultNp.setMinValue(0);
		resultNp.setMaxValue(2);
		resultNp.setValue(1);
		resultNp.setDisplayedValues(catchText);
		resultNp.setOnValueChangedListener(numberPickerChangeListener);
	}

	private void initListeners() {
		View view;
		view = findViewById(R.id.gip_button_high);
		view.setOnLongClickListener(mLongClickListener);

		view = findViewById(R.id.gip_button_left);
		view.setOnLongClickListener(mLongClickListener);

		view = findViewById(R.id.gip_button_right);
		view.setOnLongClickListener(mLongClickListener);

		view = findViewById(R.id.gip_button_low);
		view.setOnLongClickListener(mLongClickListener);

		view = findViewById(R.id.gip_button_trap);
		view.setOnLongClickListener(mLongClickListener);

		view = findViewById(R.id.gip_button_short);
		view.setOnLongClickListener(mLongClickListener);

		view = findViewById(R.id.gip_button_strike);
		view.setOnLongClickListener(mLongClickListener);

		view = findViewById(R.id.gip_button_pole);
		view.setOnLongClickListener(mLongClickListener);

		view = findViewById(R.id.gip_button_cup);
		view.setOnLongClickListener(mLongClickListener);

		view = findViewById(R.id.gip_button_bottle);
		view.setOnLongClickListener(mLongClickListener);

	}

	private void initTableFragments() {
		fragmentArray.clear();

		// ThrowTableFragment.N_ROWS = 10;

		ThrowTableFragment frag = ThrowTableFragment.newInstance(0,
				getApplicationContext());
		fragmentArray.add(frag);

		vpAdapter = new FragmentArrayAdapter(getFragmentManager());
		vp = (ViewPager) findViewById(R.id.viewPager_throwsTables);
		vp.setAdapter(vpAdapter);
		vp.setOnPageChangeListener(new MyPageChangeListener());
		// vp.setPageTransformer(true, new ZoomOutPageTransformer());

		// vp.setCurrentItem(0);
		// log("initTableFragments() - Viewpager has limit of " +
		// vp.getOffscreenPageLimit());
		// log("initTableFragments() - fragments created, adapter has " +
		// vpAdapter.getCount() + " items");
	}

	// STATE LOGIC AND PROGRAM FLOW ===========================================
	void updateActiveThrow() {
		log("updateThrow(): Updating throw at idx " + ag.getActiveIdx());
		uiThrowToActiveThrow();
		renderPage(getPageIdx(ag.getActiveIdx()));
	}

	void confirmThrow() {
		int activeIdx = ag.getActiveIdx();
		if ((activeIdx + 7) % 70 == 0) {
			Toast.makeText(getApplicationContext(), "GTO in 3 innings",
					Toast.LENGTH_LONG).show();
		} else if ((activeIdx + 1) % 70 == 0) {
			respectGentlemens();
		}
		gotoThrowIdx(activeIdx + 1);
	}

	void gotoThrowIdx(int newActiveIdx) {
		log("gotoThrow() - Going from throw idx " + ag.getActiveIdx()
				+ " to throw idx " + newActiveIdx + ".");

		uiThrowToActiveThrow();
		ag.setActiveIdx(newActiveIdx);
		applyActiveThrowToUIState();

		int idx = ag.getActiveIdx();
		assert idx == newActiveIdx;
		try {
			renderPage(getPageIdx(idx));
			log("gotoThrow() - Changed to page " + getPageIdx(idx) + ".");
		} catch (NullPointerException e) {
			loge("gotoThrow() - Failed to change to page " + getPageIdx(idx)
					+ ".", e);
		}
		ag.saveGame();
	}

	private void respectGentlemens() {
		GentlemensDialogFragment frag = new GentlemensDialogFragment();
		frag.show(getFragmentManager(), "gentlemens");
	}

	// UI to AG ===============================================================
	private void uiThrowToActiveThrow() {
		ag.updateActiveThrow(uiThrow);
	}

	// AG to UI ===============================================================
	private void applyActiveThrowToUIState() {
		uiThrow = ag.getActiveThrow();
		applyThrowToUIState(uiThrow);
	}

	private void applyThrowToUIState(Throw t) {
		setThrowResult(t);
		setThrowType(t);
		setSpecialMarks(t);
	}

	private void setThrowResult(Throw t) {
		setThrowResultToNP(t.getThrowResult());
		uiThrow.setThrowResult(t.getThrowResult());
	}

	private void setThrowType(Throw t) {
		uiThrow.setThrowType(t.getThrowType());

		// wait until after click event?
		setThrowButtonState(ThrowType.BALL_HIGH, R.id.gip_button_high);
		setThrowButtonState(ThrowType.BALL_LOW, R.id.gip_button_low);
		setThrowButtonState(ThrowType.BALL_LEFT, R.id.gip_button_left);
		setThrowButtonState(ThrowType.BALL_RIGHT, R.id.gip_button_right);
		setThrowButtonState(ThrowType.TRAP, R.id.gip_button_trap);
		setThrowButtonState(ThrowType.SHORT, R.id.gip_button_short);
		setThrowButtonState(ThrowType.STRIKE, R.id.gip_button_strike);
		setThrowButtonState(ThrowType.BOTTLE, R.id.gip_button_bottle);
		setThrowButtonState(ThrowType.POLE, R.id.gip_button_pole);
		setThrowButtonState(ThrowType.CUP, R.id.gip_button_cup);
		if (t.isTipped) {
			((ImageView) findViewById(R.id.gip_button_strike)).getDrawable()
					.setLevel(3);
		}

		setBrokenButtonState(uiThrow.getThrowType());
	}

	private void setSpecialMarks(Throw t) {
		uiThrow.isTipped = t.isTipped;
		uiThrow.setFireCounts(t.getFireCounts());
		uiThrow.setDeadType(t.getDeadType());
		for (View vw : deadViews) {
			vw.setBackgroundColor(Color.LTGRAY);
		}
		if (t.getDeadType() > 0) {
			deadViews[t.getDeadType() - 1].setBackgroundColor(Color.RED);
		}

		uiThrow.isLineFault = t.isLineFault;
		uiThrow.isOffensiveDrinkDropped = t.isOffensiveDrinkDropped;
		uiThrow.isOffensivePoleKnocked = t.isOffensivePoleKnocked;
		uiThrow.isOffensiveBottleKnocked = t.isOffensiveBottleKnocked;
		uiThrow.isOffensiveBreakError = t.isOffensiveBreakError;

		uiThrow.isGoaltend = t.isGoaltend;
		uiThrow.isGrabbed = t.isGrabbed;
		uiThrow.isDrinkHit = t.isDrinkHit;
		uiThrow.isDefensiveDrinkDropped = t.isDefensiveDrinkDropped;
		uiThrow.isDefensivePoleKnocked = t.isDefensivePoleKnocked;
		uiThrow.isDefensiveBottleKnocked = t.isDefensiveBottleKnocked;
		uiThrow.isDefensiveBreakError = t.isDefensiveBreakError;

		setErrorButtonState();
	}

	// Draw the scores ========================================================

	private void renderPage(int pidx) {
		renderPage(pidx, true);
	}

	private void renderPage(int pidx, boolean setVpItem) {
		ThrowTableFragment frag;
		while (pidx >= fragmentArray.size()) {
			frag = ThrowTableFragment
					.newInstance(pidx, getApplicationContext());
			fragmentArray.add(frag);
		}
		if (setVpItem) {
			vp.setCurrentItem(pidx);
		}
		logd("renderPage(): vp currentitem is " + vp.getCurrentItem() + " of "
				+ vp.getChildCount() + " children");

		frag = fragmentArray.get(pidx);
		logd("renderPage() - got fragment");
		int[] range = ThrowTableFragment.throwIdxRange(pidx);
		logd("renderPage() - got throw range");
		frag.renderAsPage(pidx, ag.getThrows());
		log("renderPage() - rendered as page " + pidx);
		frag.clearHighlighted();
		logd("renderPage() - cleared highlighted");

		int idx = ag.getActiveIdx();
		if (idx >= range[0] && idx < range[1]) {
			frag.highlightThrow(idx);
		}
	}

	public int getThrowResultFromNP() {
		int theResult = 0;
		switch (resultNp.getValue()) {
		case 0:
			theResult = ThrowResult.DROP;
			break;
		case 1:
			theResult = ThrowResult.CATCH;
			break;
		case 2:
			theResult = ThrowResult.STALWART;
			break;
		}
		return theResult;
	}

	public void setThrowResultToNP(int result) {
		naView.setBackgroundColor(Color.LTGRAY);
		switch (result) {
		case ThrowResult.DROP:
			resultNp.setValue(0);
			break;
		case ThrowResult.CATCH:
			resultNp.setValue(1);
			break;
		case ThrowResult.STALWART:
			resultNp.setValue(2);
			break;
		case ThrowResult.NA:
			naView.setBackgroundColor(Color.RED);
			break;
		}
	}

	int getPageIdxMax() {
		return ag.nThrows() / (2 * ThrowTableFragment.N_ROWS);
	}

	int getPageIdx() {
		return getPageIdx(ag.nThrows());
	}

	int getPageIdx(int throwIdx) {
		if (throwIdx > ag.nThrows()) {
			throwIdx = ag.nThrows();
		}
		int pidx = (throwIdx) / (2 * ThrowTableFragment.N_ROWS);
		if (pidx < 0) {
			pidx = 0;
		}
		log("getPageIdx(int): Index is " + pidx + ".");
		return pidx;
	}

	private void setThrowButtonState(int throwType, int id) {
		ImageView btn = (ImageView) findViewById(id);

		if (throwType == uiThrow.getThrowType()) {
			btn.getDrawable().setLevel(1);
		} else if (throwType == ThrowType.TRAP
				&& uiThrow.getThrowType() == ThrowType.TRAP_REDEEMED) {
			btn.getDrawable().setLevel(1);
		} else {
			btn.getDrawable().setLevel(0);
		}
	}

	private void setBrokenButtonState(int throwType) {
		Drawable poleDwl = ((ImageView) findViewById(R.id.gip_button_pole))
				.getDrawable();
		Drawable cupDwl = ((ImageView) findViewById(R.id.gip_button_cup))
				.getDrawable();
		Drawable bottleDwl = ((ImageView) findViewById(R.id.gip_button_bottle))
				.getDrawable();

		if (uiThrow.getThrowResult() == ThrowResult.BROKEN) {
			switch (throwType) {
			case ThrowType.POLE:
				poleDwl.setLevel(3);
				cupDwl.setLevel(2);
				bottleDwl.setLevel(2);
				break;
			case ThrowType.CUP:
				poleDwl.setLevel(2);
				cupDwl.setLevel(3);
				bottleDwl.setLevel(2);
				break;
			case ThrowType.BOTTLE:
				poleDwl.setLevel(2);
				cupDwl.setLevel(2);
				bottleDwl.setLevel(3);
				break;
			}
		} else {
			switch (throwType) {
			case ThrowType.POLE:
				poleDwl.setLevel(1);
				cupDwl.setLevel(0);
				bottleDwl.setLevel(0);
				break;
			case ThrowType.CUP:
				poleDwl.setLevel(0);
				cupDwl.setLevel(1);
				bottleDwl.setLevel(0);
				break;
			case ThrowType.BOTTLE:
				poleDwl.setLevel(0);
				cupDwl.setLevel(0);
				bottleDwl.setLevel(1);
				break;
			}
		}
	}

	private void setErrorButtonState() {
		TextView ogVw = (TextView) findViewById(R.id.gip_ownGoal);
		TextView deVw = (TextView) findViewById(R.id.gip_playerError);
		ogVw.setTextColor(Color.BLACK);
		deVw.setTextColor(Color.BLACK);
		for (boolean og : uiThrow.getOwnGoals()) {
			if (og) {
				ogVw.setTextColor(Color.RED);
			}
		}
		for (boolean de : uiThrow.getDefErrors()) {
			if (de) {
				deVw.setTextColor(Color.RED);
			}
		}
	}

}
