package com.ultimatepolish.polishscorebook;

import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ultimatepolish.scorebookdb.Throw;

public class ThrowTableFragment extends Fragment {
	public static final String LOG_PREFIX = "TTFrag.";
	public static final String PAGE_IDX_KEY = "page_idx";
	public static int N_ROWS = 20;
	public static int highlightedColor = Color.GRAY;
	public static int unhighlightedColor = ThrowTableRow.tableBackgroundColor;
	public TableLayout layout;
	
	OnTableRowClickedListener mListener;
	
	static ThrowTableFragment newInstance() {	
		ThrowTableFragment f = new ThrowTableFragment();
        return f;
    }
	
	public static int throwIdxToPageIdx(int throwIdx){
		assert throwIdx >= 0;
		int global_ridx = (throwIdx)/2;
		int pidx = global_ridx/N_ROWS;
		return pidx;
	}
	public static int throwIdxToRowIdx(int throwIdx) throws ArrayIndexOutOfBoundsException{
		return((throwIdx)/2) % N_ROWS;
	}
	public static int[] throwIdxRange(int page_idx){
		int[] range = new int[2];
		range[0] = (2*N_ROWS)*page_idx;
		range[1] = range[0] + 2*N_ROWS - 1;
		return range;
	}
	public static int localThrowIdxToGlobal(int page_idx, int local_throw_idx){
		return 2*N_ROWS*page_idx + local_throw_idx;
	}
	public void log(String msg){
		Log.i(GameInProgress.LOGTAG, LOG_PREFIX+msg);
	}
		
	
	public interface OnTableRowClickedListener {
		public void onThrowClicked(int local_throw_nr);
	}
	private OnClickListener throwClickedListener = new OnClickListener(){
    	public void onClick(View v){
    		int row, col, local_throw_idx;
    		ViewGroup p = (ViewGroup) v.getParent();
    		ViewGroup gp = (ViewGroup) p.getParent();
    		   		
    		col = p.indexOfChild(v);
    		row = gp.indexOfChild(p);
    		
    		if (col == 0) {
    			// clicked on the inningNr column
    			return;
    		} else if (col > 4){
    			// clicked on a score column
    			return;
    		} else{
    			local_throw_idx = 2*row;
    			if (col >= 3){
    				local_throw_idx++;
    			}
    			mListener.onThrowClicked(local_throw_idx);
    		}
    		
    	}
    };
	
	@Override
	public void onAttach(Activity activity) {
		log("onAttach - attaching activity");
		super.onAttach(activity);
		try {
            mListener = (OnTableRowClickedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnTableRowClickedListener");
        }
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		log("onCreate - creating fragment");
		super.onCreate(savedInstanceState);
		Log.i("ThrowTableFragment", "onCreate(): hello!");
	}
	
	@Override
	public void onResume() {
		log("onResume - resuming fragment");
		super.onResume();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		log("onCreate - creating view");
		layout = (TableLayout) inflater.inflate(R.layout.fragment_throws_table, container, false);
		
		ThrowTableRow tr;
		for (int i = 0; i < N_ROWS; i++){
			tr = ThrowTableRow.buildBlankRow(container.getContext());
			for (int j = 0; j < tr.getChildCount(); j++){
				tr.getChildAt(j).setOnClickListener(throwClickedListener);	
			}
			layout.addView(tr);
		}
		Log.i("ThrowTableFragment", "onCreateView(): layout has " + layout.getChildCount() + " children");
		return layout;
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	public void renderAsPage(int page_idx, List<Throw> throwsList){
		Throw t;
		int nThrows = throwsList.size();
		Log.i("ThrowTableFragment", "nThrows = " + nThrows);
		int[] range = ThrowTableFragment.throwIdxRange(page_idx);
		
		Log.i("ThrowTableFragment", "Page's idx range: " + range[0] + "-" + range[1]);
		
		if (nThrows - 1 < range[0]){
			Log.i("ThrowTableFragment", "Highest throw idx is below lower range.");
			return;
		}
		
		for (int i = range[0]; i <= range[1]; i++){
//			Log.i("ThrowTableFragment", "Trying to render throw at idx " + i);
			if (i > nThrows - 1){
				break;
			}
			t = throwsList.get(i);
//			Log.i("ThrowTableFragment", "Retrieved throw " + throwsList.get(i).getThrowNumber());
			renderThrow(t);
		}
	}
	private void renderThrow(Throw t){
		try{
//			Log.i("ThrowTableFragment", "renderThrow(): Rendered throw at idx " + t.getThrowIdx());
			ThrowTableRow tr = getTableRow(t);
			tr.updateText(t);
			Log.i("ThrowTableFragment", "renderThrow(): Rendered throw at idx " + t.getThrowIdx());
		}
		catch (IndexOutOfBoundsException e){
			Log.e("ThrowTableFragment", "renderThrow(): Throw idx " + t.getThrowIdx() + " has no view on this page");
			return;
		}
	}
	
	public void highlightThrow(int throwIdx){
		setThrowHighlighted(throwIdx, true);
	}
	public void clearHighlighted(){
		for (int i = 0;i<2*N_ROWS; i++){
			setThrowHighlighted(i, false);
		}
	}
	
	private void setThrowHighlighted(int throwIdx, boolean highlight) {
		if (throwIdx < 0){
			return;
		}
		ThrowTableRow tr;
		try{
			tr = getTableRow(throwIdx);
		}
		catch (IndexOutOfBoundsException e){
			return;
		}
		
		TextView tv;
		int start, stop;
		if (Throw.isP1Throw(throwIdx)){
			start = 1;
			stop = 3;
		}
		else{
			start = 3;
			stop = 5;
		}
		for (int i = start; i < stop; i++){
			tv = (TextView) tr.getChildAt(i);
			if (highlight){
				tv.setBackgroundColor(highlightedColor);
			}
			else{
				tv.setBackgroundColor(unhighlightedColor);
			}
		}
	}
	public ThrowTableRow getTableRow(Throw t){
		return getTableRow(t.getThrowIdx());
	}
	
	public ThrowTableRow getTableRow(int throwIdx){
//		layout = (TableLayout) getView();
		int ridx = ThrowTableFragment.throwIdxToRowIdx(throwIdx);
//		Log.i("ThrowTableFragment", "getTableRow() - Getting row for throw idx "
//				+ throwIdx + ", it's " + ridx);
		ThrowTableRow tr;
		
		try{
			tr = (ThrowTableRow) layout.getChildAt(ridx);
		}
		catch (NullPointerException e){
			throw new IndexOutOfBoundsException("Child for throw nr " + throwIdx + " dne at row " + ridx);
		}

		return tr;
	}
	TableLayout getTableLayout(){
		return (TableLayout) getView();
	}
}
