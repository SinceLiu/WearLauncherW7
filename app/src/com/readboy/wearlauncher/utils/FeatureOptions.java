package com.readboy.wearlauncher.utils;

import android.os.SystemProperties;
/**
 * Created by Administrator on 2017/6/28.
 */

public class FeatureOptions {

    // GMO support.
    public static final boolean LOW_RAM_SUPPORT = isPropertyEnabledBoolean("ro.config.low_ram");
    // CTA feature support
    public static final boolean MTK_CTA_SET = isPropertyEnabledInt("ro.mtk_cta_set");
    /// M: Add for CT 6M. @ {
    public static final boolean MTK_CT6M_SUPPORT = isPropertyEnabledInt("ro.ct6m_support");
    // A1 Support FO
    public static final boolean MTK_A1_SUPPORT = isPropertyEnabledInt("ro.mtk_a1_feature");

    /**
     *
     * @param propertyString
     * @return true, property is enable.
     */
    private static boolean isPropertyEnabledBoolean(String propertyString) {
        return "true".equals(SystemProperties.get(propertyString, "true"));
    }

    /**
     *
     * @param propertyString
     * @return true, property is enable.
     */
    private static boolean isPropertyEnabledInt(String propertyString) {
        return "1".equals(SystemProperties.get(propertyString));
    }
}
