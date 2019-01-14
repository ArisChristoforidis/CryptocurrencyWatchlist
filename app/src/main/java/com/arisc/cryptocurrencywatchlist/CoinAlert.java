package com.arisc.cryptocurrencywatchlist;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class CoinAlert {

    @Id(autoincrement = true)
    private Long id;

    @NotNull
    private String alertTitle;

    @NotNull
    private  String coinId;

    private Double lowerLimit;

    private Double upperLimit;

    private Long userAccountId;

    @Generated(hash = 723871622)
    public CoinAlert(Long id, @NotNull String alertTitle, @NotNull String coinId,
            Double lowerLimit, Double upperLimit, Long userAccountId) {
        this.id = id;
        this.alertTitle = alertTitle;
        this.coinId = coinId;
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.userAccountId = userAccountId;
    }

    @Generated(hash = 1071014701)
    public CoinAlert() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAlertTitle() {
        return this.alertTitle;
    }

    public void setAlertTitle(String alertTitle) {
        this.alertTitle = alertTitle;
    }

    public String getCoinId() {
        return this.coinId;
    }

    public void setCoinId(String coinId) {
        this.coinId = coinId;
    }

    public Double getLowerLimit() {
        return this.lowerLimit;
    }

    public void setLowerLimit(Double lowerLimit) {
        this.lowerLimit = lowerLimit;
    }

    public Double getUpperLimit() {
        return this.upperLimit;
    }

    public void setUpperLimit(Double upperLimit) {
        this.upperLimit = upperLimit;
    }

    public Long getUserAccountId() {
        return this.userAccountId;
    }

    public void setUserAccountId(Long userAccountId) {
        this.userAccountId = userAccountId;
    }




}
