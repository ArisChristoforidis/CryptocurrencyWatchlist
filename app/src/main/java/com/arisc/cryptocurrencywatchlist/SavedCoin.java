package com.arisc.cryptocurrencywatchlist;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class SavedCoin {

    @Id(autoincrement = true)
    private Long id;

    @NotNull
    private String mCoinId;

    private Long userAccountId;

    @Generated(hash = 1386427687)
    public SavedCoin(Long id, @NotNull String mCoinId, Long userAccountId) {
        this.id = id;
        this.mCoinId = mCoinId;
        this.userAccountId = userAccountId;
    }

    @Generated(hash = 200888543)
    public SavedCoin() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMCoinId() {
        return this.mCoinId;
    }

    public void setMCoinId(String mCoinId) {
        this.mCoinId = mCoinId;
    }

    public Long getUserAccountId() {
        return this.userAccountId;
    }

    public void setUserAccountId(Long userAccountId) {
        this.userAccountId = userAccountId;
    }

}
