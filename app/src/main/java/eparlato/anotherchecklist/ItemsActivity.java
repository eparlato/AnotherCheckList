package eparlato.anotherchecklist;

import eparlato.anotherchecklist.EditDialog.EditDialogListener;
import eparlato.anotherchecklist.contentprovider.MainProvider;
import eparlato.anotherchecklist.database.ItemsTable;
import eparlato.anotherchecklist.database.ListsTable;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;

public class ItemsActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor>,
		EditDialogListener<Object> {

	protected final String TAG = "ItemsActivity";
	protected ListView itemsListView;
	private ItemsCursorAdapter cursorAdapter;
	protected String[] SOURCE_COLUMNS = { ItemsTable.C_NAME };
	protected int[] TO_VIEWS = { R.id.item_checked_text };

	private CheckList checkList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// If no data was given in the intent (because we were started
		// as a MAIN activity), then use our default content provider.
		Bundle parameters = getIntent().getExtras();

		if (parameters != null) {

			checkList = parameters.getParcelable("eparlato.anotherchecklist.CheckList");
		}

		setContentView(R.layout.items);

		initializeData();
	}

	protected void initializeData() {

		// Get the selected list's title and set the Action bar title
		ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle(checkList.getName());

		Button addButton = (Button) findViewById(R.id.items_button_add);
		addButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				EditText editText = ((EditText) findViewById(R.id.items_edit_name));
				String itemName = editText.getText().toString();

				if (!TextUtils.isEmpty(itemName)) {
					createNewItem(itemName, checkList.getId());
					editText.getText().clear();
				}

			}
		});

		// Load the selected checklist
		itemsListView = (ListView) findViewById(R.id.items_activity_list_items);

		cursorAdapter = new ItemsCursorAdapter(this, R.layout.item_single, null, SOURCE_COLUMNS, TO_VIEWS, 0);

		itemsListView.setAdapter(cursorAdapter);
		itemsListView.setOnItemClickListener(new CurrentListClickListener());

		registerForContextMenu(itemsListView);

		getSupportLoaderManager().initLoader(1, null, this);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.item_single_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Item listItem = getItemAtPosition(itemsListView, info.position);

		switch (item.getItemId()) {
		case R.id.item_single_menu_edit:
			renameItemName(listItem);
		default:
			return super.onContextItemSelected(item);
		}
	}

	public Item createNewItem(String name, int idCheckList) {

		Item newItem;
		int id;
		int status = ItemsTable.ITEM_STATUS_NOT_SELECTED;

		Uri insertUri;
		ContentValues values = new ContentValues();

		// Create a new record into Items table
		values.put(ItemsTable.C_ID_CHECK_LIST, idCheckList);
		values.put(ItemsTable.C_NAME, name);
		values.put(ItemsTable.C_STATUS, status);

		insertUri = getContentResolver().insert(MainProvider.URI_CONTENT_ITEMS, values);

		id = Integer.parseInt(insertUri.getLastPathSegment());

		newItem = new Item(id, idCheckList, name, status);

		return newItem;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.items_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.items_menu_delete_items:
			deleteSelectedItemsFromList();
			return true;
		case R.id.items_menu_edit_title:
			renameCheckList(checkList);
			return true;
		case R.id.items_menu_delete_list:
			deleteList(checkList);
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void renameItemName(Item item) {
		EditDialog<Object> editDialog = EditDialog.newInstance(item.getItemName());
		editDialog.setObjectToRename(item);
		editDialog.setEditDialogListener(this);
		editDialog.show(getSupportFragmentManager(), "editTitleFragment");
	}

	private void renameCheckList(CheckList checkList) {
		EditDialog<Object> editDialog = EditDialog.newInstance(checkList.getName());
		editDialog.setObjectToRename(checkList);
		editDialog.setEditDialogListener(this);
		editDialog.show(getSupportFragmentManager(), "editTitleFragment");
	}

	protected void deleteSelectedItemsFromList() {
		Cursor cursor = cursorAdapter.getCursor();
		int idItemToDelete;
		int itemSelection;

		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

			itemSelection = cursor.getInt(cursor.getColumnIndex(ItemsTable.C_STATUS));

			if (itemSelection == ItemsTable.ITEM_STATUS_SELECTED) {
				idItemToDelete = cursor.getInt(cursor.getColumnIndex(ItemsTable.C_ID));

				getContentResolver().delete(ContentUris.withAppendedId(MainProvider.URI_CONTENT_ITEMS, idItemToDelete),
						null, null);
			}
		}
	}

	protected class CurrentListClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			Item selectedItem = getItemAtPosition((ListView) parent, position);

			int selectedItemNewStatus = selectedItem.getItemStatus() == ItemsTable.ITEM_STATUS_SELECTED ? ItemsTable.ITEM_STATUS_NOT_SELECTED
					: ItemsTable.ITEM_STATUS_SELECTED;

			selectedItem.setItemStatus(selectedItemNewStatus);

			getContentResolver().update(
					ContentUris.withAppendedId(MainProvider.URI_CONTENT_ITEMS, selectedItem.getId()),
					selectedItem.getContentValues(), null, null);

		}
	}

	private Item getItemAtPosition(ListView listView, int position) {

		Item item;
		int itemId;
		int idCheckList;
		String itemName;
		int itemStatus;

		Cursor c = ((SimpleCursorAdapter) listView.getAdapter()).getCursor();
		c.moveToPosition(position);

		itemId = c.getInt(c.getColumnIndex(ItemsTable.C_ID));
		idCheckList = c.getInt(c.getColumnIndex(ItemsTable.C_ID_CHECK_LIST));
		itemName = c.getString(c.getColumnIndex(ItemsTable.C_NAME));
		itemStatus = c.getInt(c.getColumnIndex(ItemsTable.C_STATUS));

		item = new Item(itemId, idCheckList, itemName, itemStatus);

		return item;
	}

	// LoaderManager callback methods

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		// Get only items of the selected list (its id has been passed by the
		// intent to this activity)
		String selection = ItemsTable.C_ID_CHECK_LIST + " = ? ";
		String[] selectionArgs = { String.valueOf(checkList.getId()) };

		return new CursorLoader(this, MainProvider.URI_CONTENT_ITEMS, null, selection, selectionArgs, null);
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
	public void onLoaderReset(Loader<Cursor> loader) {

		if (cursorAdapter != null) {
			cursorAdapter.swapCursor(null);
		}
	}

	// End LoaderManager callback methods

	@Override
	public void onDialogPositiveClick(Object objectToRename, String newName) {


		ContentValues contentValues = new ContentValues();
		

		if (objectToRename instanceof CheckList) {
			
			contentValues.put(ListsTable.C_NAME, newName);
			getContentResolver()
					.update(ContentUris.withAppendedId(MainProvider.URI_CONTENT_LISTS,
							((CheckList) objectToRename).getId()), contentValues, null, null);
			getSupportActionBar().setTitle(newName);
			
			checkList.setName(newName);
			
		} else if (objectToRename instanceof Item) {
			
			contentValues.put(ItemsTable.C_NAME, newName);
			getContentResolver().update(
					ContentUris.withAppendedId(MainProvider.URI_CONTENT_ITEMS, ((Item) objectToRename).getId()),
					contentValues, null, null);
		}
	}

	private void deleteList(CheckList checkList) {

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

	private class ItemsCursorAdapter extends SimpleCursorAdapter {

		public ItemsCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
			super(context, layout, c, from, to, flags);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {

			// Get data of the selected item
			String itemName = cursor.getString(cursor.getColumnIndex(ItemsTable.C_NAME));
			int itemStatus = cursor.getInt(cursor.getColumnIndex(ItemsTable.C_STATUS));

			// Set the textView with the item name and its status
			CheckedTextView checkedTextView = (CheckedTextView) view.findViewById(R.id.item_checked_text);
			checkedTextView.setText(itemName);

			switch (itemStatus) {
			case ItemsTable.ITEM_STATUS_SELECTED:
				checkedTextView.setPaintFlags(checkedTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
				break;
			case ItemsTable.ITEM_STATUS_NOT_SELECTED:
				checkedTextView.setPaintFlags(checkedTextView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
				break;
			default:
				break;
			}

		}

	}

}
