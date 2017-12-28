package ca.edumedia.riba0007.doorsopenottawa;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 *  Dialog to confirm delete
 *  @author Priscila Ribas da Costa (riba0007)
 */

public class DeleteDialogFragment extends DialogFragment {
    DeleteDialogInterface ddi;

    DeleteDialogFragment(DeleteDialogInterface ddi){
        this.ddi = ddi;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.action_discard)
                .setMessage(R.string.message_confirm_discard)
                .setCancelable(false)
                .setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ddi.confirmDeleteAction();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        return builder.create();
    }
}
