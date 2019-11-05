package org.fox.ttrss;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class NetworkPreferencesFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences_network);
    }
}