package com.example.finalproject;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class myActionDialog extends DialogFragment {
    private static final String TAG = "MyCustomDialog";

    public interface OnInputListener{
        void sendInput(String input);
    }
    public OnInputListener mOnInputListener;

    //widgets
    private TextView mActionOk, mActionCancel;
    private RadioGroup radioGroup;
    private RadioButton radioButton;


    //vars

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.action_dialog, container, false);
        mActionCancel = view.findViewById(R.id.actionCancel);
        mActionOk = view.findViewById(R.id.actionOk);
        radioGroup = view.findViewById(R.id.radioGroup);

        mActionCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing dialog");
                getDialog().dismiss();
            }
        });


        mActionOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: capturing input");

                int radioID = radioGroup.getCheckedRadioButtonId();
                checkButton(view);

//                if(!input.equals("")){
//
//                    //Easiest way: just set the value
//                    ((MainActivity)getActivity()).mInputDisplay.setText(input);
//
//                }

                //"Best Practice" but it takes longer
                mOnInputListener.sendInput(radioButton.getText().toString());
                getDialog().dismiss();
            }
        });

        return view;
    }

    public void checkButton(View v) {
        int radioId = radioGroup.getCheckedRadioButtonId();

        radioButton = v.findViewById(radioId);

        //Toast.makeText(getContext(), "Selected Radio Button: " + radioButton.getText(),
                //Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mOnInputListener = (OnInputListener) getActivity();
        }catch (ClassCastException e){
            Log.e(TAG, "onAttach: ClassCastException: " + e.getMessage() );
        }
    }
}
