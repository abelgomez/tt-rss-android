package org.fox.ttrss;

import android.os.Bundle;

import com.livefront.bridge.Bridge;

import androidx.fragment.app.Fragment;

public class StateSavedFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bridge.restoreInstanceState(this, savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);

        Bridge.saveInstanceState(this, out);
    }
}
