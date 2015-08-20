package eparlato.anotherchecklist;

import eparlato.anotherchecklist.database.ItemsTable;
import android.content.ContentValues;

public class Item {
	
	private int id;
	private int idCheckList;
	private String itemName;
	private int itemStatus;

	
	public Item(int id, int idCheckList, String itemName, int itemStatus){
		this.id = id;
		this.idCheckList = idCheckList;
		this.itemName = itemName;
		this.itemStatus = itemStatus;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getIdCheckList() {
		return idCheckList;
	}

	public void setIdCheckList(int idCheckList) {
		this.idCheckList = idCheckList;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public int getItemStatus() {
		return itemStatus;
	}

	public void setItemStatus(int itemStatus) {
		this.itemStatus = itemStatus;
	}


	public ContentValues getContentValues() {
		ContentValues values = new ContentValues();
		values.put(ItemsTable.C_ID, id);
		values.put(ItemsTable.C_ID_CHECK_LIST, idCheckList);
		values.put(ItemsTable.C_NAME, itemName);
		values.put(ItemsTable.C_STATUS, itemStatus);
		
		return values;
	}
	
	

}
