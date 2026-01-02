package com.payment.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Supported currencies for payment processing
 */
@Getter
@RequiredArgsConstructor
public enum Currency {
    USD("US Dollar", "$"),
    EUR("Euro", "€"),
    GBP("British Pound", "£"),
    JPY("Japanese Yen", "¥"),
    IDR("Indonesian Rupiah", "Rp");

    private final String name;
    private final String symbol;
}
