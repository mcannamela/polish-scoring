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
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.db.Throw;
import com.ultimatepolish.enums.DeadType;
import com.ultimatepolish.enums.ThrowResult;
import com.ultimatepolish.enums.ThrowType;
import com.ultimatepolish.polishscorebook.backend.ActiveGame;
import com.ultimatepolish.polishscorebook.backend.MenuContainerActivity;
import com.ultimatepolish.polishscorebook.backend.ThrowTableFragment;
import com.ultimatepolish.polishscorebook.backend.ThrowTableRow;

public class GameInProgress extends MenuContainerActivity implements
		ThrowTableFragment.OnTableRowClickedListener {
	public static String LOGTAG = "GIP";
	private FragmentArrayAdapter vpAdapter;
	private List<ThrowTableFragment> fragmentArray = new ArrayList<ThrowTableFragment>(
			0);
	private ViewPager vp;

	private View[] deadViews = new View[4];
	private ImageView ivHigh;
	private ImageView ivLow;
	private ImageView ivLeft;
	private ImageView ivRight;
	private ImageView ivTrap;
	private ImageView ivShort;
	private ImageView ivStrike;
	private ImageView ivBottle;
	private ImageView ivCup;
	private ImageView ivPole;
	private TextView tvOwnGoal;
	private TextView tvDefErr;
	private ToggleButton tbFire;
	private View naViewL;
	private View naViewR;
	NumberPicker resultNp;

	public ActiveGame ag;
	Dao<Throw, Long> tDao;
	Throw uiThrow;

	// LISTENERS ==============================================================
	private OnValueChangeListener resultNPChangeListener = new OnValueChangeListener() {
		public void onValueChange(NumberPicker parent, int oldVal, int newVal) {
			switch (newVal) {
			case 0:
				uiThrow.throwResult = ThrowResult.DROP;
				break;
			case 1:
				uiThrow.throwResult = ThrowResult.CATCH;
				break;
			case 2:
				uiThrow.throwResult = ThrowResult.STALWART;
				break;
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

			if (uiThrow.throwType == ThrowType.TRAP
					|| uiThrow.throwType == ThrowType.TRAP_REDEEMED) {
				switch (buttonId) {
				case R.id.gip_button_pole:
					toggleBroken();
					ag.ruleSet.setThrowType(uiThrow, ThrowType.TRAP_REDEEMED);
					break;
				case R.id.gip_button_cup:
					toggleBroken();
					ag.ruleSet.setThrowType(uiThrow, ThrowType.TRAP_REDEEMED);
					break;
				case R.id.gip_button_bottle:
					toggleBroken();
					ag.ruleSet.setThrowType(uiThrow, ThrowType.TRAP_REDEEMED);
					break;
				}
			} else {
				switch (buttonId) {
				case R.id.gip_button_pole:
					toggleBroken();
					ag.ruleSet.setThrowType(uiThrow, ThrowType.POLE);
					break;
				case R.id.gip_button_cup:
					toggleBroken();
					ag.ruleSet.setThrowType(uiThrow, ThrowType.CUP);
					break;
				case R.id.gip_button_bottle:
					toggleBroken();
					ag.ruleSet.setThrowType(uiThrow, ThrowType.BOTTLE);
					break;
				}
			}

			switch (buttonId) {
			case R.id.gip_button_strike:
				ag.ruleSet.setIsTipped(uiThrow, !uiThrow.isTipped);
				if (uiThrow.isTipped) {
					ivStrike.getDrawable().setLevel(2);
				} else {
					ivStrike.getDrawable().setLevel(0);
				}
				break;
			case R.id.gip_button_high:
				toggleDeadType(DeadType.HIGH);
				break;
			case R.id.gip_button_right:
				toggleDeadType(DeadType.RIGHT);
				break;
			case R.id.gip_button_low:
				toggleDeadType(DeadType.LOW);
				break;
			case R.id.gip_button_left:
				toggleDeadType(DeadType.LEFT);
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

	public void throwTypePressed(View view) {
		log("buttonPressed(): " + view.getContentDescription() + " was pressed");
		int buttonId = view.getId();

		if (uiThrow.throwType == ThrowType.TRAP
				|| uiThrow.throwType == ThrowType.TRAP_REDEEMED) {
			switch (buttonId) {
			case R.id.gip_button_trap:
				ag.ruleSet.setThrowResult(uiThrow, getThrowResultFromNP());
				ag.ruleSet.setThrowType(uiThrow, ThrowType.NOT_THROWN);
				((ImageView) view).getDrawable().setLevel(0);
				break;
			case R.id.gip_button_bottle:
			case R.id.gip_button_pole:
			case R.id.gip_button_cup:
				ag.ruleSet.setThrowType(uiThrow, ThrowType.TRAP_REDEEMED);
				confirmThrow();
				break;
			default:
				ag.ruleSet.setThrowType(uiThrow, ThrowType.TRAP);
				confirmThrow();
				break;
			}
		} else {
			switch (buttonId) {
			case R.id.gip_button_high:
				ag.ruleSet.setThrowType(uiThrow, ThrowType.BALL_HIGH);
				break;
			case R.id.gip_button_low:
				ag.ruleSet.setThrowType(uiThrow, ThrowType.BALL_LOW);
				break;
			case R.id.gip_button_left:
				ag.ruleSet.setThrowType(uiThrow, ThrowType.BALL_LEFT);
				break;
			case R.id.gip_button_right:
				ag.ruleSet.setThrowType(uiThrow, ThrowType.BALL_RIGHT);
				break;
			case R.id.gip_button_trap:
				ag.ruleSet.setThrowType(uiThrow, ThrowType.TRAP);
				((ImageView) view).getDrawable().setLevel(2);
				break;
			case R.id.gip_button_short:
				ag.ruleSet.setThrowType(uiThrow, ThrowType.SHORT);
				break;
			case R.id.gip_button_strike:
				ag.ruleSet.setThrowType(uiThrow, ThrowType.STRIKE);
				break;
			case R.id.gip_button_bottle:
				ag.ruleSet.setThrowType(uiThrow, ThrowType.BOTTLE);
				break;
			case R.id.gip_button_pole:
				ag.ruleSet.setThrowType(uiThrow, ThrowType.POLE);
				break;
			case R.id.gip_button_cup:
				ag.ruleSet.setThrowType(uiThrow, ThrowType.CUP);
				break;
			}

			if (buttonId != R.id.gip_button_trap) {
				confirmThrow();
			}
		}
	}

	public void fireButtonPressed(View view) {
		boolean isChecked = ((ToggleButton) view).isChecked();

		if (isChecked) {
			uiThrow.offenseFireCount = 3;
			uiThrow.defenseFireCount = 0;
			if (uiThrow.throwResult != ThrowResult.BROKEN) {
				ag.ruleSet.setThrowResult(uiThrow, ThrowResult.NA);
			}
			if (uiThrow.throwType == ThrowType.FIRED_ON) {
				ag.ruleSet.setThrowType(uiThrow, ThrowType.NOT_THROWN);
			}
		} else {
			uiThrow.offenseFireCount = 0;
			ag.ruleSet.setThrowResult(uiThrow, getThrowResultFromNP());
		}
		log("fire checked changed");
		updateActiveThrow();
	};

	public void firedOnPressed(View view) {
		log("buttonPressed(): " + view.getContentDescription() + " was pressed");

		if (uiThrow.defenseFireCount == 0) {
			uiThrow.defenseFireCount = 3;
			uiThrow.offenseFireCount = 0;
			ag.ruleSet.setThrowType(uiThrow, ThrowType.FIRED_ON);
			confirmThrow();
		} else {
			uiThrow.defenseFireCount = 0;
			ag.ruleSet.setThrowType(uiThrow, ThrowType.NOT_THROWN);
			ag.ruleSet.setThrowResult(uiThrow, getThrowResultFromNP());
			updateActiveThrow();
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

	public void OwnGoalDialog(View view) {
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

	public void PlayerErrorDialog(View view) {
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

	public void InfoDialog() {
		DateFormat df = new SimpleDateFormat("EEE MMM dd, yyyy. HH:mm",
				Locale.US);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Game #" + String.valueOf(ag.getGameId()))
				.setPositiveButton("Close",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
							}
						});
		LayoutInflater inflater = getLayoutInflater();

		View fView = inflater.inflate(R.layout.dialog_game_information, null);
		TextView tv;

		// players
		tv = (TextView) fView.findViewById(R.id.gInfo_p1);
		tv.setText(ag.getP1Name());

		tv = (TextView) fView.findViewById(R.id.gInfo_p2);
		tv.setText(ag.getP2Name());

		// // session
		tv = (TextView) fView.findViewById(R.id.gInfo_session);
		tv.setText(ag.getSessionName());

		// venue
		tv = (TextView) fView.findViewById(R.id.gInfo_venue);
		tv.setText(ag.getVenueName());

		// date
		tv = (TextView) fView.findViewById(R.id.gInfo_date);
		tv.setText(df.format(ag.getGameDate()));

		builder.setView(fView);
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public static class GentlemensDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the Builder class for convenient dialog construction
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage("Time out, Gentlemen!").setPositiveButton(
					"Resume", new DialogInterface.OnClickListener() {
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
		int testRuleSetId = intent.getIntExtra("RSID", 0);

		Context context = getApplicationContext();
		tDao = Throw.getDao(context);
		ag = new ActiveGame(gId, context, testRuleSetId);
		uiThrow = ag.getActiveThrow();

		initMetadata();
		initListeners();

		log("onCreate(): about to create fragments");
		initTableFragments();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem fav = menu.add(0, 1, 0, "Game Information");
		fav.setIcon(R.drawable.ic_action_about);
		fav.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case 1:
			InfoDialog();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
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
		TextView tv;

		// table header
		tv = (TextView) findViewById(R.id.header_p1);
		tv.setText(ag.getP1Nick());
		tv.setTextColor(ThrowTableRow.tableTextColor);
		tv.setTextSize(ThrowTableRow.tableTextSize);

		tv = (TextView) findViewById(R.id.header_p2);
		tv.setText(ag.getP2Nick());
		tv.setTextColor(ThrowTableRow.tableTextColor);
		tv.setTextSize(ThrowTableRow.tableTextSize);
	}

	private void initListeners() {
		deadViews[0] = findViewById(R.id.gip_dead_high);
		deadViews[1] = findViewById(R.id.gip_dead_right);
		deadViews[2] = findViewById(R.id.gip_dead_low);
		deadViews[3] = findViewById(R.id.gip_dead_left);

		ivHigh = (ImageView) findViewById(R.id.gip_button_high);
		ivHigh.setOnLongClickListener(mLongClickListener);

		ivLeft = (ImageView) findViewById(R.id.gip_button_left);
		ivLeft.setOnLongClickListener(mLongClickListener);

		ivRight = (ImageView) findViewById(R.id.gip_button_right);
		ivRight.setOnLongClickListener(mLongClickListener);

		ivLow = (ImageView) findViewById(R.id.gip_button_low);
		ivLow.setOnLongClickListener(mLongClickListener);

		ivTrap = (ImageView) findViewById(R.id.gip_button_trap);
		ivTrap.setOnLongClickListener(mLongClickListener);

		ivShort = (ImageView) findViewById(R.id.gip_button_short);
		ivShort.setOnLongClickListener(mLongClickListener);

		ivStrike = (ImageView) findViewById(R.id.gip_button_strike);
		ivStrike.setOnLongClickListener(mLongClickListener);

		ivPole = (ImageView) findViewById(R.id.gip_button_pole);
		ivPole.setOnLongClickListener(mLongClickListener);

		ivCup = (ImageView) findViewById(R.id.gip_button_cup);
		ivCup.setOnLongClickListener(mLongClickListener);

		ivBottle = (ImageView) findViewById(R.id.gip_button_bottle);
		ivBottle.setOnLongClickListener(mLongClickListener);

		tvOwnGoal = (TextView) findViewById(R.id.gip_ownGoal);
		tvDefErr = (TextView) findViewById(R.id.gip_playerError);

		tbFire = (ToggleButton) findViewById(R.id.gip_toggle_fire);

		if (ag.ruleSet.useAutoFire() == true) {
			tbFire.setVisibility(View.GONE);
			Button bFiredOn = (Button) findViewById(R.id.gip_button_fired_on);
			bFiredOn.setVisibility(View.GONE);
		}

		naViewL = findViewById(R.id.gip_na_indicatorL);
		naViewR = findViewById(R.id.gip_na_indicatorR);

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
		resultNp.setOnValueChangedListener(resultNPChangeListener);

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
		ag.updateActiveThrow(uiThrow);
		renderPage(getPageIdx(ag.getActiveIdx()));
		refreshUI();
	}

	void confirmThrow() {
		int activeIdx = ag.getActiveIdx();
		if ((activeIdx + 7) % 70 == 0) {
			Toast.makeText(getApplicationContext(), "GTO in 3 innings",
					Toast.LENGTH_LONG).show();
		} else if ((activeIdx + 1) % 70 == 0) {
			GentlemensDialogFragment frag = new GentlemensDialogFragment();
			frag.show(getFragmentManager(), "gentlemens");
		}
		gotoThrowIdx(activeIdx + 1);
		ag.updateScoresFrom(activeIdx + 1);
	}

	void gotoThrowIdx(int newActiveIdx) {
		log("gotoThrow() - Going from throw idx " + ag.getActiveIdx()
				+ " to throw idx " + newActiveIdx + ".");

		ag.updateActiveThrow(uiThrow); // ui -> ag
		ag.setActiveIdx(newActiveIdx); // change index
		uiThrow = ag.getActiveThrow(); // ag -> ui
		refreshUI();

		int idx = ag.getActiveIdx();
		assert idx == newActiveIdx; // validation

		// try to render the throw table
		try {
			renderPage(getPageIdx(idx));
			log("gotoThrow() - Changed to page " + getPageIdx(idx) + ".");
		} catch (NullPointerException e) {
			loge("gotoThrow() - Failed to change to page " + getPageIdx(idx)
					+ ".", e);
		}
		ag.saveGame(); // save the game
	}

	// UI =====================================================================
	private void refreshUI() {
		setThrowResultToNP(uiThrow.throwResult);
		setThrowButtonState(ThrowType.BALL_HIGH, ivHigh);
		setThrowButtonState(ThrowType.BALL_LOW, ivLow);
		setThrowButtonState(ThrowType.BALL_LEFT, ivLeft);
		setThrowButtonState(ThrowType.BALL_RIGHT, ivRight);
		setThrowButtonState(ThrowType.TRAP, ivTrap);
		setThrowButtonState(ThrowType.SHORT, ivShort);
		setThrowButtonState(ThrowType.STRIKE, ivStrike);
		setThrowButtonState(ThrowType.BOTTLE, ivBottle);
		setThrowButtonState(ThrowType.POLE, ivPole);
		setThrowButtonState(ThrowType.CUP, ivCup);
		setBrokenButtonState();
		setExtrasButtonState();

		if (uiThrow.isTipped) {
			ivStrike.getDrawable().setLevel(3);
		}

		for (View vw : deadViews) {
			vw.setBackgroundColor(Color.LTGRAY);
		}
		if (uiThrow.deadType > 0) {
			deadViews[uiThrow.deadType - 1].setBackgroundColor(Color.RED);
		}
	}

	private void setThrowButtonState(int throwType, ImageView iv) {
		if (throwType == uiThrow.throwType) {
			iv.getDrawable().setLevel(1);
		} else if (throwType == ThrowType.TRAP
				&& uiThrow.throwType == ThrowType.TRAP_REDEEMED) {
			iv.getDrawable().setLevel(1);
		} else {
			iv.getDrawable().setLevel(0);
		}
	}

	private void setBrokenButtonState() {
		Drawable poleDwl = ivPole.getDrawable();
		Drawable cupDwl = ivCup.getDrawable();
		Drawable bottleDwl = ivBottle.getDrawable();

		if (uiThrow.throwResult == ThrowResult.BROKEN) {
			switch (uiThrow.throwType) {
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
			case ThrowType.TRAP:
			case ThrowType.TRAP_REDEEMED:
				poleDwl.setLevel(2);
				cupDwl.setLevel(2);
				bottleDwl.setLevel(2);
				break;
			}
		} else {
			switch (uiThrow.throwType) {
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
			case ThrowType.TRAP:
			case ThrowType.TRAP_REDEEMED:
				poleDwl.setLevel(0);
				cupDwl.setLevel(0);
				bottleDwl.setLevel(0);
				break;
			}
		}
	}

	private void setExtrasButtonState() {
		tvOwnGoal.setTextColor(Color.BLACK);
		tvDefErr.setTextColor(Color.BLACK);
		for (boolean og : uiThrow.getOwnGoals()) {
			if (og) {
				tvOwnGoal.setTextColor(Color.RED);
			}
		}
		for (boolean de : uiThrow.getDefErrors()) {
			if (de) {
				tvDefErr.setTextColor(Color.RED);
			}
		}

		if (uiThrow.offenseFireCount >= 3) {
			tbFire.setChecked(true);
		} else {
			tbFire.setChecked(false);
		}
	}

	private void renderPage(int pidx) {
		renderPage(pidx, true);
	}

	private void renderPage(int pidx, boolean setVpItem) {
		ThrowTableFragment frag;
		while (pidx >= fragmentArray.size()) {
			frag = ThrowTableFragment
					.newInstance(pidx, getApplicationContext());
			fragmentArray.add(frag);
			vpAdapter.notifyDataSetChanged();
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
		frag.renderAsPage(pidx, ag.getThrows(), ag.ruleSet);
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
		naViewL.setBackgroundColor(Color.LTGRAY);
		naViewR.setBackgroundColor(Color.LTGRAY);
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
			naViewL.setBackgroundColor(Color.RED);
			naViewR.setBackgroundColor(Color.RED);
			break;
		}
	}

	public void toggleDeadType(int deadType) {
		if (uiThrow.deadType == deadType) {
			ag.ruleSet.setDeadType(uiThrow, DeadType.ALIVE);
		} else {
			ag.ruleSet.setDeadType(uiThrow, deadType);
		}
	}

	public void toggleBroken() {
		if (uiThrow.throwResult == ThrowResult.BROKEN) {
			ag.ruleSet.setThrowResult(uiThrow, getThrowResultFromNP());
		} else {
			ag.ruleSet.setThrowResult(uiThrow, ThrowResult.BROKEN);
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
}
