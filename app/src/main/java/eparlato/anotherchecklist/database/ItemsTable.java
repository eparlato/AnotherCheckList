package eparlato.anotherchecklist.database;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

public class ItemsTable {

	public static final String TABLE_NAME = "items";
	private static final String TABLE_TEMP_NAME = "items_temp";
	public static final String C_ID = BaseColumns._ID;
	public static final String C_ID_CHECK_LIST = "id_check_list";
	public static final String C_NAME = "item_name";
	public static final String C_STATUS = "item_status";

	public static final int ITEM_STATUS_SELECTED = 10;
	public static final int ITEM_STATUS_NOT_SELECTED = 20;

	private static final String CREATE_TABLE_SQL = String.format(
			" create table if not exists %s ("
					+ " %s integer primary key autoincrement," + " %s integer,"
					+ " %s text," + " %s integer" + ");", TABLE_NAME, C_ID,
			C_ID_CHECK_LIST, C_NAME, C_STATUS);

	private static final String CREATE_TABLE_TEMP_SQL = String.format(
			"create table if not exists %s (%s integer, %s text, %s integer);",
			TABLE_TEMP_NAME, C_ID_CHECK_LIST, C_NAME, C_STATUS);

	private static final String SAVE_DATA_SQL = String.format(
			"insert into %s(%s, %s, %s) select %s, %s, %s from %s;", TABLE_TEMP_NAME,
			C_ID_CHECK_LIST, C_NAME, C_STATUS, C_ID_CHECK_LIST, C_NAME, C_STATUS, TABLE_NAME);

	private static final String RESTORE_DATA_SQL = String.format(
			"insert into %s(%s, %s, %s) select %s, %s, %s from %s;", TABLE_NAME,
			C_ID_CHECK_LIST, C_NAME, C_STATUS, C_ID_CHECK_LIST, C_NAME, C_STATUS, TABLE_TEMP_NAME);

	private static final String DELETE_TABLE_SQL = String.format(
			" drop table if exists %s;", TABLE_NAME);

	private static final String DELETE_TABLE_TEMP_SQL = String.format(
			"drop table if exists %s;", TABLE_TEMP_NAME);

	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_SQL);
	}

	public static void onDelete(SQLiteDatabase db) {
		db.execSQL(DELETE_TABLE_SQL);
	}

	public static void saveOldData(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_TEMP_SQL);
		db.execSQL(SAVE_DATA_SQL);
	}

	public static void restoreOldData(SQLiteDatabase db) {
		db.execSQL(RESTORE_DATA_SQL);
	}

	public static void deleteTempTable(SQLiteDatabase db) {
		db.execSQL(DELETE_TABLE_TEMP_SQL);
	}
}
