package com.ultimatepolish.polishscorebook.backend;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TableRow;
import android.widget.TextView;

import com.ultimatepolish.db.Throw;
import com.ultimatepolish.polishscorebook.R;
import com.ultimatepolish.polishscorebook.R.drawable;
import com.ultimatepolish.rulesets.RuleSet;

public class ThrowTableRow extends TableRow {

	public static int tableTextSize = 20;
	public static int tableTextColor = Color.BLACK;
	public static int tableBackgroundColor = Color.WHITE;
	public static int columnWidth = 30;

	public ThrowTableRow(Context context) {
		super(context);
	}

	public ThrowTableRow(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public static ThrowTableRow buildBlankRow(Context context) {
		ThrowTableRow tr = new ThrowTableRow(context);
		TextView tv = new TextView(context);
		ImageView iv = new ImageView(context);

		ThrowTableRow.formatTextView(tv);
		tv.setText("--");
		tv.setWidth(100);
		tv.setGravity(Gravity.RIGHT);
		tv.setPadding(0, 0, 15, 0);

		tr.addView(tv);

		iv.setBackgroundColor(tableBackgroundColor);
		iv.setImageDrawable(context.getResources().getDrawable(
				R.drawable.bxs_blank));
		iv.setScaleType(ScaleType.CENTER_INSIDE);
		iv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.MATCH_PARENT));
		tr.addView(iv);

		tv = new TextView(context);
		tv.setText("--");
		ThrowTableRow.formatTextView(tv);
		tv.setWidth(100);
		tr.addView(tv);

		iv = new ImageView(context);
		iv.setBackgroundColor(tableBackgroundColor);
		iv.setImageDrawable(context.getResources().getDrawable(
				R.drawable.bxs_blank));
		iv.setScaleType(ScaleType.CENTER_INSIDE);
		iv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.MATCH_PARENT));
		tr.addView(iv);

		tv = new TextView(context);
		tv.setText("--");
		ThrowTableRow.formatTextView(tv);
		tv.setWidth(100);
		tr.addView(tv);

		tr.appendBlank();
		return tr;
	}

	public static TextView[] buildScoreViews(int p1Score, int p2Score,
			Context context) {
		TextView[] views = { new TextView(context), new TextView(context) };
		views[0].setText(String.valueOf(p1Score));
		views[1].setText(String.valueOf(p2Score));

		for (TextView tv : views) {
			ThrowTableRow.formatTextView(tv);
		}
		return views;
	}

	public static void formatTextView(TextView v) {
		v.setTextColor(tableTextColor);
		v.setTextSize(tableTextSize);
		v.setBackgroundColor(tableBackgroundColor);
		v.setGravity(Gravity.CENTER);
		v.setWidth((int) (columnWidth * v.getResources().getDisplayMetrics().density));
		v.setPadding(1, 0, 1, 0);
	}

	public void appendBlank() {
		TextView[] views = { new TextView(this.getContext()),
				new TextView(this.getContext()) };
		for (TextView tv : views) {
			tv.setText("--");
			ThrowTableRow.formatTextView(tv);
			this.addView(tv);
		}
	}

	public void appendScore(int p1Score, int p2Score) {
		TextView[] views = buildScoreViews(p1Score, p2Score, this.getContext());
		for (int i = 0; i < views.length; i++) {
			this.addView(views[i]);
		}
	}

	protected void updateText(Throw t, RuleSet rs) {
		// inning
		updateInning(t);
		// p1 throw
		if (t.throwIdx % 2 == 0) {
			updateP1Text(t, rs);
		}
		// p2 throw
		else {
			updateP2Text(t, rs);
		}
	}

	protected void updateInning(Throw t) {
		NumberFormat formatter = new DecimalFormat("   ");
		// String inning = formatter.format(t.getThrowIdx()/2 + 1);
		String inning = String.valueOf(t.throwIdx / 2 + 1);
		getInningView().setText(inning);
	}

	protected void updateP1Text(Throw t, RuleSet rs) {
		rs.setThrowDrawable(t, getP1ThrowView());
		getP1SpecialView().setText(rs.getSpecialString(t));

		int sc[] = rs.getFinalScores(t);
		updateScoreText(sc[0], sc[1]);
	}

	protected void updateP2Text(Throw t, RuleSet rs) {
		rs.setThrowDrawable(t, getP2ThrowView());
		getP2SpecialView().setText(rs.getSpecialString(t));

		int sc[] = rs.getFinalScores(t);
		updateScoreText(sc[1], sc[0]);
	}

	protected void updateScoreText(int p1Score, int p2Score) {
		getP1ScoreView().setText(String.valueOf(p1Score));
		getP2ScoreView().setText(String.valueOf(p2Score));
	}

	protected TextView getInningView() {
		return (TextView) getChildAt(0);
	}

	protected ImageView getP1ThrowView() {
		return (ImageView) getChildAt(1);
	}

	protected TextView getP1SpecialView() {
		return (TextView) getChildAt(2);
	}

	protected TextView getP1ScoreView() {
		return (TextView) getChildAt(5);
	}

	protected ImageView getP2ThrowView() {
		return (ImageView) getChildAt(3);
	}

	protected TextView getP2SpecialView() {
		return (TextView) getChildAt(4);
	}

	protected TextView getP2ScoreView() {
		return (TextView) getChildAt(6);
	}
}