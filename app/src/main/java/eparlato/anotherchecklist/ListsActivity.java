package eparlato.anotherchecklist;

import eparlato.anotherchecklist.EditDialog.EditDialogListener;
import eparlato.anotherchecklist.contentprovider.MainProvider;
import eparlato.anotherchecklist.database.ItemsTable;
import eparlato.anotherchecklist.database.ListsTable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

public class ListsActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor>,
		EditDialogListener<CheckList> {

	private final String TAG = "ListsActivity";

	private ListView checkListView;
	private SimpleCursorAdapter cursorAdapter;
	private String[] SOURCE_COLUMNS = { ListsTable.C_NAME };
	private int[] TO_VIEWS = { android.R.id.text1 };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.lists);
		initialize();

	}

	private void initialize() {

		// Load the list of the Lists
		checkListView = (ListView) findViewById(R.id.lists_activity_list);
		cursorAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, null, SOURCE_COLUMNS,
				TO_VIEWS, 0);
		checkListView.setAdapter(cursorAdapter);
		checkListView.setOnItemClickListener(new SelectedListClickListener());

		Button newListButton = (Button) findViewById(R.id.lists_activity_new_list);
		newListButton.setOnClickListener(new NewListClickListener());

		registerForContextMenu(checkListView);

		getSupportLoaderManager().initLoader(1, null, this);

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.list_single_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

		CheckList selectedCheckList = getCheckListAtPosition(checkListView, info.position);

		switch (item.getItemId()) {

		case R.id.lists_menu_edit_name:
			renameCheckList(selectedCheckList);
			return true;

		case R.id.lists_menu_delete:
			deleteCheckList(selectedCheckList);
			return true;

		default:
			return super.onContextItemSelected(item);
		}
	}

	private CheckList createNewCheckList() {
		CheckList newCheckList;
		int newCheckListId;
		String newCheckListName;
		long newCheckListCreationDate;

		ContentValues insertParameters = new ContentValues();

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.ITALIAN);
		long currentTimeInMillis = System.currentTimeMillis();
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(currentTimeInMillis);

		newCheckListName = dateFormat.format(calendar.getTime());
		newCheckListCreationDate = currentTimeInMillis;

		// When a new list is created, its default name is the current time
		insertParameters.put(ListsTable.C_NAME, newCheckListName);
		insertParameters.put(ListsTable.C_CREATION_DATE, newCheckListCreationDate);

		Uri uri = getContentResolver().insert(MainProvider.URI_CONTENT_LISTS, insertParameters);

		newCheckListId = Integer.parseInt(uri.getLastPathSegment());

		newCheckList = new CheckList(newCheckListId, newCheckListName, newCheckListCreationDate);

		return newCheckList;

	}

	private void deleteCheckList(CheckList checkList) {
		String whereClause;
		String[] whereArgs;

		// Prepare and launch delete statement on Items table: delete all items
		// of the selected list
		whereClause = ItemsTable.C_ID_CHECK_LIST + " = ? ";
		whereArgs = new String[] { String.valueOf(checkList.getId()) };
		getContentResolver().delete(MainProvider.URI_CONTENT_ITEMS, whereClause, whereArgs);

		// Delete this record from Lists table
		getContentResolver().delete(ContentUris.withAppendedId(MainProvider.URI_CONTENT_LISTS, checkList.getId()),
				null, null);
	}

	private void renameCheckList(CheckList checkList) {

		EditDialog<CheckList> editDialog = EditDialog.newInstance(checkList.getName());
		editDialog.setObjectToRename(checkList);
		editDialog.setEditDialogListener(this);
		editDialog.show(getSupportFragmentManager(), "editTitleFragment");
	}

	private CheckList getCheckListAtPosition(ListView listView, int position) {
		CheckList newList;
		int id;
		String name;
		long creationDate;

		Cursor c = ((SimpleCursorAdapter) listView.getAdapter()).getCursor();
		c.moveToPosition(position);

		id = c.getInt(c.getColumnIndex(ListsTable.C_ID));
		name = c.getString(c.getColumnIndex(ListsTable.C_NAME));
		creationDate = c.getLong(c.getColumnIndex(ListsTable.C_CREATION_DATE));

		newList = new CheckList(id, name, creationDate);

		return newList;

	}

	// LoaderManager callback methods

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		return new CursorLoader(this, MainProvider.URI_CONTENT_LISTS, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		// Here the CursorAdapter is filled with values from db and returned
		if (cursorAdapter != null && cursor != null) {
			cursorAdapter.swapCursor(cursor);
		} else {
			Log.d(TAG, "onLoadFinished: cursorAdapter is null");
		}

	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		if (cursorAdapter != null) {
			cursorAdapter.swapCursor(null);
		}
	}

	// End LoaderManager callback methods

	private class SelectedListClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			ListView parentListView = ((ListView) parent);

			CheckList selectedList = getCheckListAtPosition(parentListView, position);

			startList(selectedList);
		}

	}

	private void startList(CheckList checkList) {
		Intent intent = new Intent(getBaseContext(), ItemsActivity.class);
		intent.putExtra("eparlato.anotherchecklist.CheckList", checkList);

		startActivity(intent);
	}

	private class NewListClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {

			CheckList newCheckList = createNewCheckList();

			startList(newCheckList);
		}
	}

	@Override
	public void onDialogPositiveClick(CheckList objectToRename, String newListTitle) {

		// Store the new list's title on ListsTable
		ContentValues contentValues = new ContentValues();
		contentValues.put(ListsTable.C_NAME, newListTitle);

		getContentResolver().update(ContentUris.withAppendedId(MainProvider.URI_CONTENT_LISTS,
						objectToRename.getId()), contentValues,
				null, null);
	}


}
