package com.ultimatepolish.polishscorebook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileInfo;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.scorebookdb.Game;
import com.ultimatepolish.scorebookdb.Player;
import com.ultimatepolish.scorebookdb.Session;
import com.ultimatepolish.scorebookdb.Throw;
import com.ultimatepolish.scorebookdb.Venue;

public class SimpleSettings extends MenuContainerActivity {

	private DbxAccountManager mDbxAcctMgr;
	private static final String appKey = "v08dmrsen6b8pr5";
    private static final String appSecret = "epzfibxnco03c9v";
    private Button mLinkButton;
    private Button dbxSaveButton;
    private Button dbxLoadButton;
    private TextView mTestOutput;

    private static final int REQUEST_LINK_TO_DBX = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_simple_settings);

		mDbxAcctMgr = DbxAccountManager.getInstance(getApplicationContext(), appKey, appSecret);
		
		mLinkButton = (Button) findViewById(R.id.settings_linkToDropbox);
        mLinkButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickLinkToDropbox();
            }
        });
        dbxSaveButton = (Button) findViewById(R.id.button_saveDB_dropbox);
		dbxLoadButton = (Button) findViewById(R.id.button_loadDB_dropbox);
		mTestOutput = (TextView) findViewById(R.id.settings_dbxFiles);
		
		dbxLoadButton.setOnClickListener(new OnClickListener() {
			 
			@Override
			public void onClick(View view) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext());
				alertDialogBuilder.setTitle("Overwrite local database?");
				alertDialogBuilder.setMessage("The local database will be overwritten by the most recent file in dropbox.")
					.setPositiveButton("Overwrite",new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
								// if this button is clicked, close
								// current activity
							loadDBdropbox();
						}
					  })
					.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							// if this button is clicked, just close
							// the dialog box and do nothing
							dialog.cancel();
						}
					});
		 
					AlertDialog alertDialog = alertDialogBuilder.create();
					alertDialog.show();
				}
			});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		menu.findItem(R.id.settings).setVisible(false);
		return true;
	}
	@Override
	protected void onResume() {
		super.onResume();
		if (mDbxAcctMgr.hasLinkedAccount()) {
		    showLinkedView();
		} else {
			showUnlinkedView();
		}
	}
    private void showLinkedView() {
        mLinkButton.setVisibility(View.GONE);
        dbxSaveButton.setVisibility(View.VISIBLE);
        dbxLoadButton.setVisibility(View.VISIBLE);
    }
    private void showUnlinkedView() {
        mLinkButton.setVisibility(View.VISIBLE);
        dbxSaveButton.setVisibility(View.GONE);
        dbxLoadButton.setVisibility(View.GONE);
    }
	private void onClickLinkToDropbox() {
	    mDbxAcctMgr.startLink((Activity)this, REQUEST_LINK_TO_DBX);
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == REQUEST_LINK_TO_DBX) {
	        if (resultCode == Activity.RESULT_OK) {
	            // ... Start using Dropbox files.
	        	Context context = getApplicationContext();
	       		Toast.makeText(context, "Successfully connected to dropbox!", Toast.LENGTH_SHORT).show();
	       		mLinkButton.setVisibility(View.GONE);
	        } else {
	            // ... Link failed or was cancelled by the user.
	        	Context context = getApplicationContext();
	       		Toast.makeText(context, "Link failed or was cancelled by the user.", Toast.LENGTH_SHORT).show();
	        }            
	    } else {
	        super.onActivityResult(requestCode, resultCode, data);
	    }
	}
	public void clearTables(View view){
    	Dao<Player, Long> pd;
    	Dao<Game, Long> gd;
    	Dao<Session, Long> sd;
    	Dao<Throw, Long> td;
    	Dao<Venue, Long> vd;
    	try{
    		pd = getHelper().getPlayerDao();
    		gd = getHelper().getGameDao();
    		sd = getHelper().getSessionDao();
    		td = getHelper().getThrowDao();
   		 	vd = getHelper().getVenueDao();
   		 	for(Player x: pd){
   		 		pd.delete(x);
   		 	}
   		 	for(Game x: gd){
   		 		gd.delete(x);
   		 	}
   		 	for(Session x: sd){
   		 		sd.delete(x);
   		 	}
   		 	for(Throw x: td){
   		 		td.delete(x);
   		 	}
   		 	for(Venue x: vd){
   				vd.delete(x);
   			}
   		 
	   	}
	   	catch (SQLException e){
	   		Context context = getApplicationContext();
	   		int duration = Toast.LENGTH_LONG;
	   		Toast.makeText(context, e.getMessage(), duration).show();
	   		Log.e(PolishScorebook.class.getName(), "Clearing tables failed", e);
	   	}
    }
	public void doPopulateTest(View view){
    	Dao<Player, Long> playerDao=null;
    	Player[] players = {
	    	new Player("michael", "cannamela", "mike c", true, false,170, 70 ),
	    	new Player("erin", "arai", "samu", true, false,160, 50 ),
	    	new Player("matt", "tuttle", "king tut", true, false, 182, 63),
	    	new Player("andrew", "o'brien", "dru", true, false, 182, 63),
	    	new Player("matt", "miguez", "murder", true, false, 182, 63),
	    	new Player("julian", "spring", "juice", false, true, 182, 63),
	    	new Player("mike", "freeman", "freeeedom", true, false, 182, 63),
	    	new Player("phillip", "anderson", "pillip", false, true, 182, 63),
	    	new Player("jon", "sukovich", "sukes appeal", true, false, 182, 63)
    	};
    	Dao<Session, Long> sessionDao=null;
    	Session s1 = new Session("league", new Date());
    	Session s2 = new Session("side_books", new Date());
    	Dao<Venue, Long> venueDao=null;
    	Venue v1 = new Venue("cogswell", true);
		Venue v2 = new Venue("verndale", true);
		Venue v3 = new Venue("oxford", true);
    	try{
    		playerDao = getHelper().getPlayerDao();
    		for (int i=0;i<players.length;i++){
    			playerDao.create(players[i]);
    		}
    		
    		sessionDao = getHelper().getSessionDao();
    		sessionDao.create(s1);
    		sessionDao.create(s2);
			venueDao = getHelper().getVenueDao();
			venueDao.create(v1);
			venueDao.create(v2);
			venueDao.create(v3);
    	}
    	catch (SQLException e){
    		Context context = getApplicationContext();
    		int duration = Toast.LENGTH_LONG;
    		Toast.makeText(context, e.getMessage(), duration).show();
    		Log.e(PolishScorebook.class.getName(), "Creation of players failed", e);
    	}
    }
	public void saveDBdropbox(View view) {
		Context context = getApplicationContext();
   		Toast.makeText(context, "Saved to dropbox", Toast.LENGTH_SHORT).show();
   		
   		try {
            // Create DbxFileSystem for synchronized file access.
            DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
            
            String fileName = new SimpleDateFormat("yyyy-MM-dd_HH-mm'.db'", Locale.US).format(new Date());

            DbxPath phDBpath = new DbxPath(DbxPath.ROOT, fileName);
            if (!dbxFs.exists(phDBpath)) {
            	DbxFile phDBfile = dbxFs.create(phDBpath);
            	try {
            		phDBfile.writeFromExistingFile(getInternalPath(), false);
                } finally {
                	phDBfile.close();
                }
            	mTestOutput.append("\nCreated new file '" + phDBpath + "'.\n");
            }
        } catch (IOException e) {
            mTestOutput.setText("Dropbox test failed: " + e);
        }
	}
	public void loadDBdropbox() {
		DbxPath latestFile = null;
	
   		try {
            // Create DbxFileSystem for synchronized file access.
            DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());

            // Print the contents of the root folder.  This will block until we can
            // sync metadata the first time.
            List<DbxFileInfo> infos = dbxFs.listFolder(DbxPath.ROOT);
            mTestOutput.setText("\nStored .db Files:\n");
            for (DbxFileInfo info : infos) {
            	if (info.path.toString().contains(".db")) { //exclude files that dont have .db in the name
		        	if (latestFile == null) { //latestFile starts as null, so make first file latest
		        		latestFile = info.path;
		        	} else { //compare each file to latestFile, update if necessary
		        		if (info.modifiedTime.after(dbxFs.getFileInfo(latestFile).modifiedTime)) {
		        			latestFile = info.path;
		            	}
		        	}
		        // list all the .db files in the dropbox folder
                mTestOutput.append("    " + info.path + ", " + info.modifiedTime + '\n');
            	}
            }

            //open the latest .db file and copy over the local database
            if (latestFile != null) {
	            DbxFile latestDb = dbxFs.open(latestFile);
	            copyDbxFile(latestDb, getInternalPath());
	            mTestOutput.append("Loaded: " + latestDb.getPath() + '\n');
	            latestDb.close();
            } else {
            	mTestOutput.append("No database files were found.\n");
            }
            
        } catch (IOException e) {
            mTestOutput.setText("Dropbox test failed: " + e);
        }
	}
	File getInternalPath(){
		String dbPath = getHelper().getReadableDatabase().getPath();
		File internalDB = new File(dbPath);
		return internalDB;
	}
    public static void copyDbxFile(DbxFile sourceFile, File destFile) throws IOException {
        if(!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = sourceFile.getReadStream().getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        }
        finally {
            if(source != null) {
                source.close();
            }
            if(destination != null) {
                destination.close();
            }
        }
    }
}
