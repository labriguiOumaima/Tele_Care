package com.assistance.alertes.entity;

/**
 * Cycle de vie d'une alerte :
 * DECLENCHEE -> PRISE_EN_CHARGE -> RESOLUE
 */
public enum AlertStatus {
    DECLENCHEE,
    PRISE_EN_CHARGE,
    RESOLUE
}
