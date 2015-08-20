package eparlato.anotherchecklist.database;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

public class ListsTable {

    public static final String TABLE_NAME = "lists";
    public static final String C_ID = BaseColumns._ID;
    public static final String C_NAME = "list_name";
    public static final String C_CREATION_DATE = "creation_date";
    private static final String TABLE_TEMP_NAME = "lists_temp";
    private static final String CREATE_TABLE_SQL = String.format("create table if not exists %s " +
                    "(%s integer primary key autoincrement, %s text, %s integer);", TABLE_NAME, C_ID,
                    C_NAME, C_CREATION_DATE);
    private static final String DELETE_TABLE_SQL = String.format("drop table if exists %s;", TABLE_NAME);
    private static final String DELETE_TABLE_TEMP_SQL = String.format("drop table if exists %s;",
            TABLE_TEMP_NAME);
    private static final String CREATE_TABLE_TEMP_SQL = String.format("create table if not " +
            "exists %s (%s text, %s integer);", TABLE_TEMP_NAME, C_NAME, C_CREATION_DATE);
    private static final String SAVE_DATA_SQL = String.format("insert into %s(%s, %s) select %s, " +
                    "%s from %s;", TABLE_TEMP_NAME, C_NAME, C_CREATION_DATE, C_NAME, C_CREATION_DATE,
                    TABLE_NAME);
    private static final String RESTORE_DATA_SQL = String.format("insert into %s(%s, %s) select " +
                    "%s, %s from %s;", TABLE_NAME, C_NAME, C_CREATION_DATE, C_NAME, C_CREATION_DATE,
                    TABLE_TEMP_NAME);
    public static void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SQL);
    }
    public static void onDelete(SQLiteDatabase db) {
        db.execSQL(DELETE_TABLE_SQL);
    }


    public static void deleteTempTable(SQLiteDatabase db) {
        db.execSQL(DELETE_TABLE_TEMP_SQL);
    }

    public static void saveOldData(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_TEMP_SQL);
        db.execSQL(SAVE_DATA_SQL);
    }

    public static void restoreOldData(SQLiteDatabase db) {
        db.execSQL(RESTORE_DATA_SQL);
    }
}
