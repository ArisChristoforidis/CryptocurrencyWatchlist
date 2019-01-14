package com.arisc.cryptocurrencywatchlist;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AlertDialogFragment extends DialogFragment {

    public static String priceKey = "PRICE_KEY";

    private static final String TAG = "AlertDialogFragment";

    private final String positiveButtonText = "Create";
    private final String negativeButtonText = "Cancel";

    AlertDialog.Builder mBuilder;
    AlertDialog mInstance;

    EditText txtAlertTitle;
    EditText txtLowerLimit;
    EditText txtUpperLimit;
    TextView txtPrice;
    private boolean mLowerLimitHasText,mUpperLimitHasText;

    private Double mPrice;

    private Activity mParentActivity;
    private View mView;

    private OnCreateAlertListener mOnCreateAlertListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mBuilder = new AlertDialog.Builder(getActivity());
        mParentActivity = getActivity();
        LayoutInflater inflater = mParentActivity.getLayoutInflater();

        mView = inflater.inflate(R.layout.dialog_new_alert,null);
        mBuilder.setView(mView);

        setupButtons();

        mInstance =  mBuilder.create();
        return mInstance;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        mPrice = args.getDouble(priceKey);
    }

    @Override
    public void onStart() {
        super.onStart();

        setupViewsAndEvents();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mOnCreateAlertListener = (OnCreateAlertListener) getActivity();
        }catch(ClassCastException e){
            Log.d(TAG,"onAttach:ClassCastException: " + e.getMessage());
        }
    }

    public void setupViewsAndEvents(){
        txtAlertTitle = mView.findViewById(R.id.etxtDialogAlertTitle);
        setupEditTextWatchers();

        txtPrice = mView.findViewById(R.id.txtAlertCurrentPrice);
        if(mPrice != null){
            String price = "$" + Utils.doubleToString(mPrice);
            txtPrice.setText(price);
        }else{
            txtPrice.setText("N/A");
        }

    }

    private void setupEditTextWatchers() {
    //The user needs to specify at least one upper or lower limit,if not both.


        txtLowerLimit = mView.findViewById(R.id.etxtDialogAlertLowerLimit);
        txtUpperLimit = mView.findViewById(R.id.etxtDialogAlertUpperLimit);

        mLowerLimitHasText = false;
        mUpperLimitHasText = false;

        final Button positiveButton = mInstance.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setEnabled(false);

        txtLowerLimit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(txtLowerLimit.getText().length() == 0){
                    mLowerLimitHasText = false;
                    if(!mUpperLimitHasText && positiveButton.isEnabled()) positiveButton.setEnabled(false);
                }else{
                    if(!mLowerLimitHasText) mLowerLimitHasText = true;
                    if(!positiveButton.isEnabled()) positiveButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        txtUpperLimit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(txtUpperLimit.getText().length() == 0){
                    mUpperLimitHasText = false;
                    if(!mLowerLimitHasText && positiveButton.isEnabled()) positiveButton.setEnabled(false);
                }else{
                    if(!mUpperLimitHasText) mUpperLimitHasText = true;
                    if(!positiveButton.isEnabled()) positiveButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


    }


    private void setupButtons(){
        mBuilder.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = txtAlertTitle.getText().toString();
                String lowerLimit = txtLowerLimit.getText().toString();
                String upperLimit = txtUpperLimit.getText().toString();
                mOnCreateAlertListener.onAlertCreated(title,lowerLimit,upperLimit);
            }
        });

        mBuilder.setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

    }


    public interface OnCreateAlertListener{
        void onAlertCreated(String alertTitle,String lowerLimit,String upperLimit);
    }

}
