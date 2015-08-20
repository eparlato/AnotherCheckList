package eparlato.anotherchecklist.contentprovider;

import eparlato.anotherchecklist.database.*;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class MainProvider extends ContentProvider {

	private static final int LISTS_ALL_RECORDS = 1;
	private static final int LISTS_SINGLE_RECORD = 2;
	private static final int ITEMS_ALL_RECORDS = 3;
	private static final int ITEMS_SINGLE_RECORD = 4;

	static final String URI_AUTHORITY = "eparlato.anotherchecklist.contentprovider";
	public static final String URI_PATH_LISTS = "lists";
	public static final String URI_PATH_ITEMS = "items";
	public static final Uri URI_CONTENT_LISTS = Uri.parse("content://" + URI_AUTHORITY + "/" + URI_PATH_LISTS);
	public static final Uri URI_CONTENT_ITEMS = Uri.parse("content://" + URI_AUTHORITY + "/" + URI_PATH_ITEMS);


	private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

	static {
		sUriMatcher.addURI(URI_AUTHORITY, URI_PATH_LISTS, LISTS_ALL_RECORDS);
		sUriMatcher.addURI(URI_AUTHORITY, URI_PATH_LISTS + "/#", LISTS_SINGLE_RECORD);
		sUriMatcher.addURI(URI_AUTHORITY, URI_PATH_ITEMS, ITEMS_ALL_RECORDS);
		sUriMatcher.addURI(URI_AUTHORITY, URI_PATH_ITEMS + "/#", ITEMS_SINGLE_RECORD);
	}

	public MainDbHelper dbHelper;

	@Override
	public boolean onCreate() {
		dbHelper = new MainDbHelper(getContext());

		return true;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		StringBuilder uriPathNewRecord = new StringBuilder();
		long idInsertedRecord;

		switch (sUriMatcher.match(uri)) {
		case LISTS_ALL_RECORDS:
			idInsertedRecord = db.insert(ListsTable.TABLE_NAME, null, values);
			uriPathNewRecord.append(URI_PATH_LISTS);
			break;
		case ITEMS_ALL_RECORDS:
			idInsertedRecord = db.insert(ItemsTable.TABLE_NAME, null, values);
			uriPathNewRecord.append(URI_PATH_ITEMS);
			break;
		default:
			throw new IllegalArgumentException("Wrong URI: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		uriPathNewRecord.append("/").append(idInsertedRecord);
		return Uri.parse(uriPathNewRecord.toString());
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int affectedRows;
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		String tableToUpdate;
		String whereClause = null;
		String[] whereArgs = null;
		
		switch (sUriMatcher.match(uri)) {
		case LISTS_ALL_RECORDS:
			tableToUpdate = ListsTable.TABLE_NAME;
			break;
		case LISTS_SINGLE_RECORD:
			tableToUpdate = ListsTable.TABLE_NAME;
			whereClause = String.format("%s == ?", ListsTable.C_ID);
			whereArgs = new String[] { uri.getLastPathSegment() };
			break;
		case ITEMS_ALL_RECORDS:
			tableToUpdate = ItemsTable.TABLE_NAME;
			break;
		case ITEMS_SINGLE_RECORD:
			tableToUpdate = ItemsTable.TABLE_NAME;
			whereClause = String.format("%s == ?", ItemsTable.C_ID);
			whereArgs = new String[] { uri.getLastPathSegment() };
			break;
		default:
			throw new IllegalArgumentException("Invalid uri " + uri);
		}
		
		affectedRows = db.update(tableToUpdate, values, whereClause, whereArgs);
		
		getContext().getContentResolver().notifyChange(uri, null);
		return affectedRows;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int rowsDeleted;
		
		String tableToDelete;
		String whereClause;
		String[] whereArgs;
		
		switch (sUriMatcher.match(uri)) {
		case LISTS_ALL_RECORDS:
			tableToDelete = ListsTable.TABLE_NAME;
			whereClause = selection;
			whereArgs = selectionArgs;
			break;
		case LISTS_SINGLE_RECORD:
			tableToDelete = ListsTable.TABLE_NAME;
			whereClause = String.format("%s == ?", ListsTable.C_ID);
			whereArgs = new String[] { uri.getLastPathSegment() };
			break;
		case ITEMS_ALL_RECORDS:
			tableToDelete = ItemsTable.TABLE_NAME;
			whereClause = selection;
			whereArgs = selectionArgs;
			break;
		case ITEMS_SINGLE_RECORD:
			tableToDelete = ItemsTable.TABLE_NAME;
			whereClause = String.format("%s == ?", ItemsTable.C_ID);
			whereArgs = new String[] { uri.getLastPathSegment() };
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		
		rowsDeleted = db.delete(tableToDelete, whereClause, whereArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;

	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db;
		Cursor cursor;

		switch (sUriMatcher.match(uri)) {
		case LISTS_ALL_RECORDS:
			db = dbHelper.getWritableDatabase();
			cursor = db.query(ListsTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);

			cursor.setNotificationUri(getContext().getContentResolver(), uri);
			return cursor;
		case ITEMS_ALL_RECORDS:
			db = dbHelper.getWritableDatabase();
			cursor = db.query(ItemsTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);

			cursor.setNotificationUri(getContext().getContentResolver(), uri);
			return cursor;
		case LISTS_SINGLE_RECORD:
		case ITEMS_SINGLE_RECORD:
			return null;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

	}

	@Override
	public String getType(Uri arg0) {
		return null;
	}

}
