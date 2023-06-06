package com.moko.support.lw006.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.lw006.entity.OrderCHAR;
import com.moko.support.lw006.entity.ParamsKeyEnum;

import java.util.Arrays;

public class ParamsReadTask extends OrderTask {
    public byte[] data;

    public ParamsReadTask() {
        super(OrderCHAR.CHAR_PARAMS, OrderTask.RESPONSE_TYPE_WRITE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(ParamsKeyEnum key) {
        createGetConfigData(key.getParamsKey());
    }

    private void createGetConfigData(int configKey) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x00,
                (byte) configKey,
                (byte) 0x00
        };
        response.responseValue = data;
    }

    public void getFilterName() {
        data = new byte[]{
                (byte) 0xEE,
                (byte) 0x00,
                (byte) ParamsKeyEnum.KEY_FILTER_NAME_RULES.getParamsKey(),
                (byte) 0x00
        };
        response.responseValue = data;
    }

    private int packetCount;
    private int packetIndex;
    private int remainPack;
    private int dataLength;
    private int dataOrigin;
    private byte[] dataBytes;

    @Override
    public boolean parseValue(byte[] value) {
        final int header = value[0] & 0xFF;
        if (header == 0xED)
            return true;
        final int cmd = value[2] & 0xFF;
        packetCount = value[3] & 0xFF;
        packetIndex = value[4] & 0xFF;
        final int length = value[5] & 0xFF;
        ParamsKeyEnum keyEnum = ParamsKeyEnum.fromParamKey(cmd);
        switch (keyEnum) {
            case KEY_FILTER_NAME_RULES:
                dataLength += length;
                byte[] responseData = Arrays.copyOfRange(value, 6, 6 + length);
                break;
        }
        return false;
    }
}
