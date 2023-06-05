package com.moko.lw006.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moko.lw006.activity.DeviceInfoActivity;
import com.moko.lw006.databinding.Lw006FragmentGeneralBinding;
import com.moko.support.lw006.LoRaLW006MokoSupport;
import com.moko.support.lw006.OrderTaskAssembler;

public class GeneralFragment extends Fragment {
    private static final String TAG = GeneralFragment.class.getSimpleName();
    private Lw006FragmentGeneralBinding mBind;

    private DeviceInfoActivity activity;

    public GeneralFragment() {
    }


    public static GeneralFragment newInstance() {
        GeneralFragment fragment = new GeneralFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        mBind = Lw006FragmentGeneralBinding.inflate(inflater, container, false);
        activity = (DeviceInfoActivity) getActivity();
        return mBind.getRoot();
    }

    public void setHeartbeatInterval(int interval) {
        mBind.etHeartbeatInterval.setText(String.valueOf(interval));
    }

    public boolean isValid() {
        final String intervalStr = mBind.etHeartbeatInterval.getText().toString();
        if (TextUtils.isEmpty(intervalStr))
            return false;
        final int interval = Integer.parseInt(intervalStr);
        if (interval < 300 || interval > 86400) {
            return false;
        }
        return true;
    }

    public void saveParams() {
        final String intervalStr = mBind.etHeartbeatInterval.getText().toString();
        final int interval = Integer.parseInt(intervalStr);
        LoRaLW006MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setHeartBeatInterval(interval));
    }
}
