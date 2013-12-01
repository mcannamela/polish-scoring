package com.ultimatepolish.polishscorebook;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class PolishScorebook extends  MenuContainerActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_polish_scorebook);
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
    
    public void openNewGameActivity(View view){
    	Intent intent = new Intent(this, NewGame.class);
    	startActivity(intent);
    }
}