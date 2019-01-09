package com.espressif.iot.esptouch.demo_activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.espressif.iot.esptouch.demo_activity.dummy.DummyContent;
import com.espressif.iot_esptouch_demo.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link BlankFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BlankFragment extends Fragment implements View.OnClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private View view;
    private RadioButton gprs,wifi;
    private Context context;
    private OnFragmentInteractionListener fragmentInteractionListener;
    private boolean isCheck=false;

    @SuppressLint("ValidFragment")
    public BlankFragment( OnFragmentInteractionListener fragmentInteractionListener) {
        // Required empty public constructor
        this.fragmentInteractionListener = fragmentInteractionListener;
    }

    public BlankFragment() {
        // Required empty public constructor
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BlankFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BlankFragment newInstance(String param1, String param2) {
        BlankFragment fragment = new BlankFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        // Inflate the layout for this fragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_blank, container, false);
        gprs = view.findViewById(R.id.gprs);
        wifi = view.findViewById(R.id.wifi);
        gprs.setOnClickListener(this);
        wifi.setOnClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick( View v ) {
        switch (v.getId()){
            case R.id.gprs:
                wifi.setChecked(false);
                if(fragmentInteractionListener != null) {
                    fragmentInteractionListener.onFragmentInteraction(v.getId());
                }
                break;
            case R.id.wifi:
                gprs.setChecked(false);
                if(fragmentInteractionListener != null){
                    fragmentInteractionListener.onFragmentInteraction(v.getId());
                }
                break;
            default:
                break;
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(int checkId);
    }
}
