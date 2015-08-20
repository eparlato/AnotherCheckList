package eparlato.anotherchecklist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;



public class EditDialog<T> extends DialogFragment {

	private final static String TEXT_TO_RENAME = "existingTitle";

	EditDialogListener<T> clickListener;
	T objectToRename;
	EditText title;	

	public interface EditDialogListener<T extends Object> {
		public void onDialogPositiveClick(T objectToRename, String newText);
	}

	
	public static <T> EditDialog<T> newInstance(String textToRename) {
		
		EditDialog<T> fragment = new EditDialog<T>();
		
		Bundle args = new Bundle();
		args.putString(TEXT_TO_RENAME, textToRename);
		
		fragment.setArguments(args);
		
		return fragment;
	}
	
	public void setObjectToRename(T objectToRename) {
		this.objectToRename = objectToRename;
	}
	
	public void setEditDialogListener(EditDialogListener<T> clickListener) {
		this.clickListener = clickListener;
	}
	

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.current_list_edit_title_dialog, null);
		builder.setView(view);

		title = (EditText) view.findViewById(R.id.edit_title);
		title.setText(getArguments().getString(TEXT_TO_RENAME));

		builder.setPositiveButton(R.string.items_edit_title_Ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
                clickListener.onDialogPositiveClick(objectToRename, title.getText().toString());
			}
		});

		builder.setNegativeButton(R.string.items_edit_title_Cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				EditDialog.this.getDialog().cancel();
			}
		});

		return builder.create();
	}

}
