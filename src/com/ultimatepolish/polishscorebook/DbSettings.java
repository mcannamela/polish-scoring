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
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileInfo;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.db.DatabaseHelper;
import com.ultimatepolish.db.DatabaseUpgrader;
import com.ultimatepolish.db.Game;
import com.ultimatepolish.db.OrmLiteFragment;
import com.ultimatepolish.db.Player;
import com.ultimatepolish.db.Session;
import com.ultimatepolish.db.Throw;
import com.ultimatepolish.db.Venue;
import com.ultimatepolish.enums.RuleType;

public class DbSettings extends OrmLiteFragment {

	private DbxAccountManager mDbxAcctMgr;
	private static final String appKey = "v08dmrsen6b8pr5";
	private static final String appSecret = "epzfibxnco03c9v";
	private Button mLinkButton;
	private Button dbxSaveButton;
	private Button dbxLoadButton;
	private TextView mTestOutput;
	private View rootView;
	private Context context;

	private static final int REQUEST_LINK_TO_DBX = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.activity_db_settings, container,
				false);

		mDbxAcctMgr = DbxAccountManager.getInstance(
				context.getApplicationContext(), appKey, appSecret);

		Button mClearButton = (Button) rootView.findViewById(R.id.db_clearDB);
		mClearButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				clearTables();
			}
		});

		Button mPopulateButton = (Button) rootView.findViewById(R.id.db_popDB);
		mPopulateButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				doPopulateTest();
			}
		});

		mLinkButton = (Button) rootView.findViewById(R.id.db_linkToDropbox);
		mLinkButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				linkToDropbox();
			}
		});

		dbxSaveButton = (Button) rootView.findViewById(R.id.db_save_dropbox);
		dbxSaveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				saveDBdropbox();
			}
		});

		dbxLoadButton = (Button) rootView.findViewById(R.id.db_load_dropbox);
		mTestOutput = (TextView) rootView.findViewById(R.id.db_dbxFiles);

		dbxLoadButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						view.getContext());
				alertDialogBuilder.setTitle("Overwrite local database?");
				alertDialogBuilder
						.setMessage(
								"The local database will be overwritten by the most recent file in dropbox.")
						.setPositiveButton("Overwrite",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// if this button is clicked, close
										// current activity
										loadDBdropbox();
									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// if this button is clicked, just close
										// the dialog box and do nothing
										dialog.cancel();
									}
								});

				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();
			}
		});

		Button mUpdtBtn = (Button) rootView.findViewById(R.id.db_updateScores);
		mUpdtBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				updateScores();
			}
		});

		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		context = getActivity();
	}

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem fav = menu.add("New Player");
		fav.setIcon(R.drawable.ic_menu_add);
		fav.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		fav.setIntent(new Intent(context, NewPlayer.class));
	}

	@Override
	public void onResume() {
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

	private void linkToDropbox() {
		mDbxAcctMgr.startLink((Activity) context, REQUEST_LINK_TO_DBX);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_LINK_TO_DBX) {
			if (resultCode == Activity.RESULT_OK) {
				// ... Start using Dropbox files.
				Toast.makeText(context, "Successfully connected to dropbox!",
						Toast.LENGTH_SHORT).show();
				mLinkButton.setVisibility(View.GONE);
			} else {
				// ... Link failed or was cancelled by the user.
				Toast.makeText(context,
						"Link failed or was cancelled by the user.",
						Toast.LENGTH_SHORT).show();
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	public void clearTables() {
		DatabaseHelper h = getHelper();
		h.dropAll();
		h.createAll();
	}

	public void doPopulateTest() {
		Dao<Player, Long> playerDao = null;
		byte[] emptyImage = new byte[0];
		Player[] players = {
				new Player("michael", "cannamela", "mike c", true, false, true,
						false, 170, 70, emptyImage, getResources().getColor(
								R.color.Aqua)),
				new Player("erin", "arai", "samu", true, false, true, false,
						160, 50, emptyImage, getResources().getColor(
								R.color.BlanchedAlmond)),
				new Player("matt", "tuttle", "king tut", true, false, true,
						false, 182, 63, emptyImage, getResources().getColor(
								R.color.CornflowerBlue)),
				new Player("andrew", "o'brien", "dru", true, false, true,
						false, 182, 63, emptyImage, getResources().getColor(
								R.color.DodgerBlue)),
				new Player("matt", "miguez", "murder", true, false, true,
						false, 182, 63, emptyImage, getResources().getColor(
								R.color.FireBrick)),
				new Player("julian", "spring", "juice", false, true, true,
						false, 182, 63, emptyImage, getResources().getColor(
								R.color.Goldenrod)),
				new Player("mike", "freeman", "freeeedom", true, false, true,
						false, 182, 63, emptyImage, getResources().getColor(
								R.color.HotPink)),
				new Player("phillip", "anderson", "pillip", false, true, true,
						false, 182, 63, emptyImage, getResources().getColor(
								R.color.Indigo)),
				new Player("jon", "sukovich", "sukes appeal", true, false,
						true, false, 182, 63, emptyImage, getResources()
								.getColor(R.color.Khaki)) };
		Dao<Session, Long> sessionDao = null;
		Session s1 = new Session("league", 1, RuleType.rs01, new Date(), false);
		Session s2 = new Session("side_books", 0, RuleType.rsNull, new Date(),
				false);
		Dao<Venue, Long> venueDao = null;
		Venue v1 = new Venue("cogswell", true);
		Venue v2 = new Venue("verndale", true);
		Venue v3 = new Venue("oxford", true);
		try {
			playerDao = getHelper().getPlayerDao();
			for (int i = 0; i < players.length; i++) {
				playerDao.create(players[i]);
			}

			sessionDao = getHelper().getSessionDao();
			sessionDao.create(s1);
			sessionDao.create(s2);
			venueDao = getHelper().getVenueDao();
			venueDao.create(v1);
			venueDao.create(v2);
			venueDao.create(v3);
		} catch (SQLException e) {
			int duration = Toast.LENGTH_LONG;
			Toast.makeText(context, e.getMessage(), duration).show();
			Log.e(PolishScorebook.class.getName(),
					"Creation of players failed", e);
		}
	}

	public void updateScores() {
		List<Long> badGames = null;
		List<Long> badThrows = null;
		Dao<Game, Long> gDao;
		Dao<Throw, Long> tDao;
		try {

			gDao = Game.getDao(context);
			tDao = Throw.getDao(context);
			badGames = DatabaseUpgrader.updateScores(gDao, context);
			if (badGames.size() > 0) {
				Log.w("SimpleSettings",
						"The following games had different scores after upgrade: "
								+ badGames.toString());
				// throw new RuntimeException("Scores changed on upgrade");
			} else {
				Log.i("SimpleSettings",
						"All game scores unchanged after upgrade!");
			}

			badThrows = DatabaseUpgrader.checkThrows(tDao, context);
			if (badThrows.size() > 0) {
				Log.w("SimpleSettings", "The following throws are not valid: "
						+ badThrows.toString());
			} else {
				Log.i("SimpleSettings", "All throws are valid!");
			}
		} catch (SQLException e) {
			int duration = Toast.LENGTH_LONG;
			Toast.makeText(context, e.getMessage(), duration).show();
			Log.e(PolishScorebook.class.getName(), "Update of scores failed", e);
		}
	}

	public void saveDBdropbox() {
		Toast.makeText(context, "Saved to dropbox", Toast.LENGTH_SHORT).show();

		try {
			// Create DbxFileSystem for synchronized file access.
			DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr
					.getLinkedAccount());

			String fileName = new SimpleDateFormat("yyyy-MM-dd_HH-mm'.db'",
					Locale.US).format(new Date());

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
			DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr
					.getLinkedAccount());

			// Print the contents of the root folder. This will block until we
			// can
			// sync metadata the first time.
			List<DbxFileInfo> infos = dbxFs.listFolder(DbxPath.ROOT);
			mTestOutput.setText("\nStored .db Files:\n");
			for (DbxFileInfo info : infos) {
				if (info.path.toString().contains(".db")) { // exclude files
															// that dont have
															// .db in the name
					if (latestFile == null) { // latestFile starts as null, so
												// make first file latest
						latestFile = info.path;
					} else { // compare each file to latestFile, update if
								// necessary
						if (info.modifiedTime.after(dbxFs
								.getFileInfo(latestFile).modifiedTime)) {
							latestFile = info.path;
						}
					}
					// list all the .db files in the dropbox folder
					mTestOutput.append("    " + info.path + ", "
							+ info.modifiedTime + '\n');
				}
			}

			// open the latest .db file and copy over the local database
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

	File getInternalPath() {
		String dbPath = getHelper().getReadableDatabase().getPath();
		File internalDB = new File(dbPath);
		return internalDB;
	}

	public static void copyDbxFile(DbxFile sourceFile, File destFile)
			throws IOException {
		if (!destFile.exists()) {
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;

		try {
			source = sourceFile.getReadStream().getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}
}
