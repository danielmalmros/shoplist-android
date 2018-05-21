package org.projects.shoppinglist;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class MyDialogFragment extends DialogFragment {

    public MyDialogFragment() {

    }

    OnPositiveListener mCallback;

    // Container Activity must implement this interface.
    public interface OnPositiveListener {
        void onPositiveClicked();
    }

    // This method will be called when the dialog fragment is called and "attached" to the current activity.
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented the callback interface. If not, it throws an exception.
        try {

            // ONLY do this cast IF the activity actually implements the interface.
            mCallback = (OnPositiveListener) activity;
        } catch (ClassCastException e) {

            // This kills the program, because we have not implemented the interface in the activity.
            throw new ClassCastException(activity.toString()
                    + " must implement OnPositiveListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Here we create a new dialog builder.
        AlertDialog.Builder alert = new AlertDialog.Builder(
                getActivity());
        alert.setTitle(R.string.confirmation);
        alert.setMessage(R.string.areYouSure);
        alert.setPositiveButton(R.string.yes, pListener);
        alert.setNegativeButton(R.string.no, nListener);

        return alert.create();
    }

    // This is our positive listener for when the user presses the yes button.
    DialogInterface.OnClickListener pListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface arg0, int arg1) {

            // This will be executed when user click Yes button.
            positiveClick();
        }
    };

    // This is our negative listener for when the user presses the no button.
    DialogInterface.OnClickListener nListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface arg0, int arg1) {
            // These will be executed when user click No button.
            negativeClick();
        }
    };

    // This method ensures that we actually now call the implemented method defined in the ACTIVITY!
    protected void positiveClick() {
        mCallback.onPositiveClicked();
    }

    // This method is empty, because it will be overriden.
    protected void negativeClick() {

    }
}