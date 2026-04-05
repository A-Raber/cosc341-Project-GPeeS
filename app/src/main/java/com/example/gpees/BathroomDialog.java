package com.example.gpees;

import android.app.Dialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

public class BathroomDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.bathroom_dialog);

        Button btnClose = dialog.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> dismiss());

        Window window = dialog.getWindow();
        if (window != null) {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int width = (int) (metrics.widthPixels * 0.95);  // 90% of screen width
            int height = (int) (metrics.heightPixels * 0.7); // 70% of screen height
            window.setLayout(width, height);
        }

        return dialog;
    }
}
