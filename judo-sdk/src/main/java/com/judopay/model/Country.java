package com.judopay.model;

/**
 * A Country that can be selected by the user when providing information for
 * address verification (AVS) checks during a transaction.
 */
public class Country {

    public static final String UNITED_KINGDOM = "UK";
    public static final String UNITED_STATES = "USA";
    public static final String CANADA = "Canada";
    public static final String OTHER = "Other";

    private final int code;
    private final String displayName;

    public Country(int code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public int getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

}