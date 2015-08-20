package eparlato.anotherchecklist.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MainDbHelper extends SQLiteOpenHelper {

	public static String DB_NAME = "anotherchecklist.db";
	public static int DB_VERSION = 1;

	public MainDbHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		ListsTable.onCreate(db);
		ItemsTable.onCreate(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(MainDbHelper.class.getName(), String.format(
				"Upgrading database from version %s to %s", oldVersion,
				newVersion));
		
		// Delete temp tables if exist
		ItemsTable.deleteTempTable(db);
		ListsTable.deleteTempTable(db);
		
		// Save old data
		ItemsTable.saveOldData(db);
		ListsTable.saveOldData(db);
		
		// Delete old tables
		ItemsTable.onDelete(db);
		ListsTable.onDelete(db);
		
		// Create new tables
		ItemsTable.onCreate(db);
		ListsTable.onCreate(db);
		
		// Restore old data
		ItemsTable.restoreOldData(db);
		ListsTable.restoreOldData(db);
		
		// Delete temp tables
		ItemsTable.deleteTempTable(db);
		ListsTable.deleteTempTable(db);
	}

}
