package com.arisc.cryptocurrencywatchlist;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

public class CustomMarker extends MarkerView {
    //Disabled cause it doesn't look good.

    private TextView mMarkerTextView;
    private MPPointF mOffSet;

    public CustomMarker(Context context, int layoutResource) {
        super(context, layoutResource);
        mMarkerTextView = findViewById(R.id.txtMarker);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        float value = e.getY();
        String strValue = Utils.doubleToString(Double.valueOf(value));
        mMarkerTextView.setText("$"+strValue);
        super.refreshContent(e, highlight);
    }


    @Override
    public MPPointF getOffset() {
        if(mOffSet == null) {
            // center the marker horizontally and vertically
            mOffSet = new MPPointF(-(getWidth() / 2), -getHeight());
        }
        return mOffSet;
    }
}
