package com.judopay.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.judopay.R;

import java.io.Serializable;

public enum OrderStatus implements Serializable {
    TIMEOUT(R.string.order_timeout, R.string.close, R.drawable.ic_timeout),
    FAILED(R.string.order_fail, R.string.close, R.drawable.ic_fail),
    SUCCEEDED(R.string.order_success, R.string.close, R.drawable.ic_success),
    PENDING(R.string.order_fail, R.string.close, R.drawable.ic_fail),
    NETWORK_ERROR(R.string.no_internet, R.string.try_again, R.drawable.ic_fail);

    @StringRes
    private final int orderStatusTextId;

    @StringRes
    private final int orderStatusButtonTextId;

    @DrawableRes
    private final int orderStatusImageId;

    OrderStatus(@StringRes int orderStatusTextId, @StringRes int orderStatusButtonTextId, @DrawableRes int orderStatusImageId) {
        this.orderStatusTextId = orderStatusTextId;
        this.orderStatusButtonTextId = orderStatusButtonTextId;
        this.orderStatusImageId = orderStatusImageId;
    }

    public int getOrderStatusTextId() {
        return orderStatusTextId;
    }

    public int getOrderStatusButtonTextId() {
        return orderStatusButtonTextId;
    }

    public int getOrderStatusImageId() {
        return orderStatusImageId;
    }
}
