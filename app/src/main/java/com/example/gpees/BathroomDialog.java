package com.example.gpees;

import android.app.Dialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class BathroomDialog extends DialogFragment {

    private static final String ARG_BATHROOM = "bathroom_obj";

    public static BathroomDialog newInstance(Bathroom bathroom) {
        BathroomDialog fragment = new BathroomDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_BATHROOM, bathroom);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.bathroom_dialog);

        Bathroom bathroom = null;
        if (getArguments() != null) {
            bathroom = (Bathroom) getArguments().getSerializable(ARG_BATHROOM);
        }

        if (bathroom != null) {
            TextView nameTextView = dialog.findViewById(R.id.bathroomNameText);
            if (nameTextView != null) {
                nameTextView.setText(bathroom.getName());
            }

            TextView addressTextView = dialog.findViewById(R.id.bathroomAddressText);
            if (addressTextView != null) {
                addressTextView.setText("");
                addressTextView.append("Address: ");
                addressTextView.append(bathroom.getAddress());
            }
        }

        Button btnClose = dialog.findViewById(R.id.btnClose);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dismiss());
        }

        Window window = dialog.getWindow();
        if (window != null) {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int width = (int) (metrics.widthPixels * 0.95);
            int height = (int) (metrics.heightPixels * 0.85);
            window.setLayout(width, height);
        }

        return dialog;
    }
}
