package com.arisc.cryptocurrencywatchlist;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.List;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Unique;

@Entity
public class UserAccount {

    @Id(autoincrement = true)
    private Long id;

    @Unique
    private String userId;

    @ToMany(referencedJoinProperty = "userAccountId")
    private List<CoinAlert> alerts;

    @ToMany(referencedJoinProperty = "userAccountId")
    private List<SavedCoin> savedCoins;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 1823587417)
    private transient UserAccountDao myDao;

    @Generated(hash = 456045987)
    public UserAccount(Long id, String userId) {
        this.id = id;
        this.userId = userId;
    }

    @Generated(hash = 1029142458)
    public UserAccount() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 495788628)
    public List<CoinAlert> getAlerts() {
        if (alerts == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            CoinAlertDao targetDao = daoSession.getCoinAlertDao();
            List<CoinAlert> alertsNew = targetDao._queryUserAccount_Alerts(id);
            synchronized (this) {
                if (alerts == null) {
                    alerts = alertsNew;
                }
            }
        }
        return alerts;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1373494378)
    public synchronized void resetAlerts() {
        alerts = null;
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 121299837)
    public List<SavedCoin> getSavedCoins() {
        if (savedCoins == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            SavedCoinDao targetDao = daoSession.getSavedCoinDao();
            List<SavedCoin> savedCoinsNew = targetDao._queryUserAccount_SavedCoins(id);
            synchronized (this) {
                if (savedCoins == null) {
                    savedCoins = savedCoinsNew;
                }
            }
        }
        return savedCoins;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 681918861)
    public synchronized void resetSavedCoins() {
        savedCoins = null;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 2000659435)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getUserAccountDao() : null;
    }

}
