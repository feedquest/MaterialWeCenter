package com.langtaosha.sjwyd.controller.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.View;

import com.langtaosha.sjwyd.BuildConfig;
import com.langtaosha.sjwyd.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // 指定配置
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 显示版本号
        Preference version = findPreference("version");
        version.setSummary(BuildConfig.VERSION_NAME);
        // 打开GitHub项目地址
        Preference github = findPreference("github_repo");
        github.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getString(R.string.github_url)));
                startActivity(intent);
                return false;
            }
        });
    }

}
