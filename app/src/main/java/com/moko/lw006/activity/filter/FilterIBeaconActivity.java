package com.moko.lw006.activity.filter;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.lib.loraui.utils.ToastUtils;
import com.moko.lw006.activity.Lw006BaseActivity;
import com.moko.lw006.databinding.Lw006ActivityFilterIbeaconBinding;
import com.moko.support.lw006.LoRaLW006MokoSupport;
import com.moko.support.lw006.OrderTaskAssembler;
import com.moko.support.lw006.entity.OrderCHAR;
import com.moko.support.lw006.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FilterIBeaconActivity extends Lw006BaseActivity {
    private Lw006ActivityFilterIbeaconBinding mBind;
    private boolean savedParamsError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = Lw006ActivityFilterIbeaconBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);

        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getFilterIBeaconEnable());
        orderTasks.add(OrderTaskAssembler.getFilterIBeaconUUID());
        orderTasks.add(OrderTaskAssembler.getFilterIBeaconMajorRange());
        orderTasks.add(OrderTaskAssembler.getFilterIBeaconMinorRange());
        LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 400)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 400)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        final String action = event.getAction();
        if (!MokoConstants.ACTION_CURRENT_DATA.equals(action))
            EventBus.getDefault().cancelEventDelivery(event);
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
            }
            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                dismissSyncProgressDialog();
            }
            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                byte[] value = response.responseValue;
                if (orderCHAR == OrderCHAR.CHAR_PARAMS) {
                    if (value.length >= 4) {
                        int header = value[0] & 0xFF;// 0xED
                        int flag = value[1] & 0xFF;// read or write
                        int cmd = value[2] & 0xFF;
                        if (header != 0xED)
                            return;
                        ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                        if (configKeyEnum == null) {
                            return;
                        }
                        int length = value[3] & 0xFF;
                        if (flag == 0x01) {
                            // write
                            int result = value[4] & 0xFF;
                            switch (configKeyEnum) {
                                case KEY_FILTER_IBEACON_UUID:
                                case KEY_FILTER_IBEACON_MAJOR_RANGE:
                                case KEY_FILTER_IBEACON_MINOR_RANGE:
                                    if (result != 1) {
                                        savedParamsError = true;
                                    }
                                    break;
                                case KEY_FILTER_IBEACON_ENABLE:
                                    if (result != 1) {
                                        savedParamsError = true;
                                    }
                                    if (savedParamsError) {
                                        ToastUtils.showToast(FilterIBeaconActivity.this, "Opps！Save failed. Please check the input characters and try again.");
                                    } else {
                                        ToastUtils.showToast(this, "Save Successfully！");
                                    }
                                    break;
                            }
                        }
                        if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_FILTER_IBEACON_UUID:
                                    if (length > 0) {
                                        String uuid = MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 4, 4 + length));
                                        mBind.etIbeaconUuid.setText(String.valueOf(uuid));
                                        mBind.etIbeaconUuid.setSelection(mBind.etIbeaconUuid.getText().length());
                                    }
                                    break;
                                case KEY_FILTER_IBEACON_MAJOR_RANGE:
                                    if (length == 4) {
                                        int majorMin = MokoUtils.toInt(Arrays.copyOfRange(value, 4, 6));
                                        int majorMax = MokoUtils.toInt(Arrays.copyOfRange(value, 6, 8));
                                        mBind.etIbeaconMajorMin.setText(String.valueOf(majorMin));
                                        mBind.etIbeaconMajorMax.setText(String.valueOf(majorMax));
                                        mBind.etIbeaconMajorMin.setSelection(mBind.etIbeaconMajorMin.getText().length());
                                        mBind.etIbeaconMajorMax.setSelection(mBind.etIbeaconMajorMax.getText().length());
                                    }
                                    break;
                                case KEY_FILTER_IBEACON_MINOR_RANGE:
                                    if (length == 4) {
                                        int minorMin = MokoUtils.toInt(Arrays.copyOfRange(value, 4, 6));
                                        int minorMax = MokoUtils.toInt(Arrays.copyOfRange(value, 6, 8));
                                        mBind.etIbeaconMinorMin.setText(String.valueOf(minorMin));
                                        mBind.etIbeaconMinorMax.setText(String.valueOf(minorMax));
                                        mBind.etIbeaconMinorMin.setSelection(mBind.etIbeaconMinorMin.getText().length());
                                        mBind.etIbeaconMinorMax.setSelection(mBind.etIbeaconMinorMax.getText().length());
                                    }
                                    break;
                                case KEY_FILTER_IBEACON_ENABLE:
                                    if (length > 0) {
                                        int enable = value[4] & 0xFF;
                                        mBind.cbIbeacon.setChecked(enable == 1);
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
        });
    }

    public void onSave(View view) {
        if (isWindowLocked()) return;
        if (isValid()) {
            showSyncingProgressDialog();
            saveParams();
        } else {
            ToastUtils.showToast(this, "Para error!");
        }
    }

    private boolean isValid() {
        if (!TextUtils.isEmpty(mBind.etIbeaconUuid.getText())) {
            String uuid = mBind.etIbeaconUuid.getText().toString();
            int length = uuid.length();
            if (length % 2 != 0) {
                return false;
            }
        }
        if (!TextUtils.isEmpty(mBind.etIbeaconMajorMin.getText()) && !TextUtils.isEmpty(mBind.etIbeaconMajorMax.getText())) {
            String majorMin = mBind.etIbeaconMajorMin.getText().toString();
            String majorMax = mBind.etIbeaconMajorMax.getText().toString();
            if (Integer.parseInt(majorMin) > 65535) {
                return false;
            }
            if (Integer.parseInt(majorMax) > 65535) {
                return false;
            }
            if (Integer.parseInt(majorMax) < Integer.parseInt(majorMin)) {
                return false;
            }
        } else if (!TextUtils.isEmpty(mBind.etIbeaconMajorMin.getText()) && TextUtils.isEmpty(mBind.etIbeaconMajorMax.getText())) {
            return false;
        } else if (TextUtils.isEmpty(mBind.etIbeaconMajorMin.getText()) && !TextUtils.isEmpty(mBind.etIbeaconMajorMax.getText())) {
            return false;
        }
        if (!TextUtils.isEmpty(mBind.etIbeaconMinorMin.getText()) && !TextUtils.isEmpty(mBind.etIbeaconMinorMax.getText())) {
            String minorMin = mBind.etIbeaconMinorMin.getText().toString();
            String minorMax = mBind.etIbeaconMinorMax.getText().toString();
            if (Integer.parseInt(minorMin) > 65535) {
                return false;
            }
            if (Integer.parseInt(minorMax) > 65535) {
                return false;
            }
            return Integer.parseInt(minorMax) >= Integer.parseInt(minorMin);
        } else if (!TextUtils.isEmpty(mBind.etIbeaconMinorMin.getText()) && TextUtils.isEmpty(mBind.etIbeaconMinorMax.getText())) {
            return false;
        } else
            return !TextUtils.isEmpty(mBind.etIbeaconMinorMin.getText()) || TextUtils.isEmpty(mBind.etIbeaconMinorMax.getText());
    }

    private void saveParams() {
        final String uuid = mBind.etIbeaconUuid.getText().toString();
        int majorMin;
        int majorMax;
        int minorMin;
        int minorMax;
        savedParamsError = false;
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setFilterIBeaconUUID(uuid));
        if (TextUtils.isEmpty(mBind.etIbeaconMajorMin.getText()) && TextUtils.isEmpty(mBind.etIbeaconMajorMax.getText())) {
            majorMin = 0;
            majorMax = 0xffff;
        } else {
            majorMin = Integer.parseInt(mBind.etIbeaconMajorMin.getText().toString());
            majorMax = Integer.parseInt(mBind.etIbeaconMajorMax.getText().toString());
        }
        if (TextUtils.isEmpty(mBind.etIbeaconMinorMin.getText()) && TextUtils.isEmpty(mBind.etIbeaconMinorMax.getText())) {
            minorMin = 0;
            minorMax = 0xffff;
        } else {
            minorMin = Integer.parseInt(mBind.etIbeaconMinorMin.getText().toString());
            minorMax = Integer.parseInt(mBind.etIbeaconMinorMax.getText().toString());
        }
        orderTasks.add(OrderTaskAssembler.setFilterIBeaconMajorRange(majorMin, majorMax));
        orderTasks.add(OrderTaskAssembler.setFilterIBeaconMinorRange(minorMin, minorMax));
        orderTasks.add(OrderTaskAssembler.setFilterIBeaconEnable(mBind.cbIbeacon.isChecked() ? 1 : 0));
        LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }

    public void onBack(View view) {
        backHome();
    }

    @Override
    public void onBackPressed() {
        backHome();
    }

    private void backHome() {
        EventBus.getDefault().unregister(this);
        setResult(RESULT_OK);
        finish();
    }
}
