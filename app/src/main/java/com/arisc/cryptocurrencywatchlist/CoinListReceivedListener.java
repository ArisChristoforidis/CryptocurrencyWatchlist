package com.arisc.cryptocurrencywatchlist;

import java.util.List;

public interface CoinListReceivedListener {
    void onCoinListReceived(List<CoinListing> coinListings);
}
