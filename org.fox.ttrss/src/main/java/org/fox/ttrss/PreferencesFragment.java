package org.fox.ttrss;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PreferencesFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        findPreference("show_logcat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), LogcatActivity.class);
                startActivity(intent);
                return false;
            }
        });

        findPreference("network_settings").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.preferences_container, new NetworkPreferencesFragment() )
                        .addToBackStack( NetworkPreferencesFragment.class.getSimpleName() )
                        .commit();

                return false;
            }
        });

        CommonActivity activity = (CommonActivity) getActivity();

        findPreference("force_phone_layout").setEnabled(activity.isTablet());

        try {
            String version;
            int versionCode;
            String buildTimestamp;

            PackageInfo packageInfo = activity.getPackageManager().
                    getPackageInfo(activity.getPackageName(), 0);

            version = packageInfo.versionName;
            versionCode = packageInfo.versionCode;

            findPreference("version").setSummary(getString(R.string.prefs_version, version, versionCode));

            buildTimestamp = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date(BuildConfig.TIMESTAMP));

            findPreference("build_timestamp").setSummary(getString(R.string.prefs_build_timestamp, buildTimestamp));

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }
}