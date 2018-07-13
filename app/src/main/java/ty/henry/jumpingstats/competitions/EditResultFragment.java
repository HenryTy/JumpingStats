package ty.henry.jumpingstats.competitions;


import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.util.Arrays;

import ty.henry.jumpingstats.R;


public class EditResultFragment extends Fragment {

    private int series;
    private Result result;
    private float[] judgeMarks;
    private AddEditResultsFragment parent;

    private EditText distanceEditText;
    private EditText windEditText;
    private EditText speedEditText;
    private EditText gateEditText;

    public EditResultFragment() {

    }

    public void setSeries(int series) {
        if(series>2 || series<1) {
            throw new IllegalArgumentException("Series argument must be 1 or 2");
        }
        this.series = series;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_edit_result, container, false);
        if(!(getParentFragment() instanceof AddEditResultsFragment)) {
            throw new IllegalStateException("Parent fragment for EditResultFragment must be AddEditResultsFragment");
        }

        parent = (AddEditResultsFragment) getParentFragment();
        try {
            result = parent.jumper.getResults(parent.competition)[series - 1];
        } catch(Exception ex) {

        }

        distanceEditText = fragmentView.findViewById(R.id.distanceEditText);
        windEditText = fragmentView.findViewById(R.id.windEditText);
        speedEditText = fragmentView.findViewById(R.id.speedEditText);
        gateEditText = fragmentView.findViewById(R.id.gateEditText);

        if(result == null) {
            judgeMarks = new float[5];
            Arrays.fill(judgeMarks, 17.5f);
        }
        else {
            judgeMarks = result.getStyleScores();
            fillWithDataToEdit();
        }

        TextView marksTextView = fragmentView.findViewById(R.id.marksTextView);
        marksTextView.setText(getMarksText());
        marksTextView.setOnClickListener(view -> {
            SetMarksFragment setMarksFragment = new SetMarksFragment();
            setMarksFragment.setInitialMarks(judgeMarks);
            setMarksFragment.setListener(marks -> {
                judgeMarks = marks;
                ((TextView) view).setText(getMarksText());
            });
            setMarksFragment.show(getChildFragmentManager(), "dialog");
        });
        return fragmentView;
    }

    private String getMarksText() {
        StringBuilder marksBuilder = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            marksBuilder.append(judgeMarks[i]);
            if (i < 2 || i == 3) {
                marksBuilder.append("   ");
            } else if (i == 2) {
                marksBuilder.append("\n ");
            }
        }
        return marksBuilder.toString();
    }

    private void fillWithDataToEdit() {
        distanceEditText.setText(Float.toString(result.getDistance()));
        windEditText.setText(Float.toString(result.getWind()));
        speedEditText.setText(Float.toString(result.getSpeed()));
        gateEditText.setText(Float.toString(result.pointsForGate()));
    }

    private Result createResult() {
        float distance = Float.parseFloat(distanceEditText.getText().toString());
        float wind = Float.parseFloat(windEditText.getText().toString());
        float speed = Float.parseFloat(speedEditText.getText().toString());
        float gateCompensation = Float.parseFloat(gateEditText.getText().toString());

        return new Result(parent.jumper, parent.competition, distance, wind, speed, judgeMarks, gateCompensation);
    }


    public static class SetMarksFragment extends DialogFragment {

        public interface DialogListener {
            void onPositiveClick(float[] marks);
        }

        private NumberPicker[] judgePickers;
        private DialogListener listener;
        private float[] initialMarks;

        public void setListener(DialogListener listener) {
            this.listener = listener;
        }

        public void setInitialMarks(float[] initialMarks) {
            this.initialMarks = initialMarks;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View pickersView = getActivity().getLayoutInflater().inflate(R.layout.pickers_judge, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(pickersView)
                    .setPositiveButton(R.string.ok, (dialogInterface, j) ->  {
                        if(listener != null) {
                            float[] marks = new float[5];
                            for (int i = 0; i < 5; i++) {
                                marks[i] = ((float) judgePickers[i].getValue()) / 2;
                            }
                            listener.onPositiveClick(marks);
                        }
                    })
                    .setNegativeButton(R.string.cancel, ((dialogInterface, i) -> {

                    }));
            judgePickers = new NumberPicker[5];
            judgePickers[0] = pickersView.findViewById(R.id.judgePicker1);
            judgePickers[1] = pickersView.findViewById(R.id.judgePicker2);
            judgePickers[2] = pickersView.findViewById(R.id.judgePicker3);
            judgePickers[3] = pickersView.findViewById(R.id.judgePicker4);
            judgePickers[4] = pickersView.findViewById(R.id.judgePicker5);
            String[] marks = new String[41];
            Arrays.setAll(marks, i -> Float.toString(((float) i)/2));
            for(int i=0; i<5; i++) {
                judgePickers[i].setMinValue(0);
                judgePickers[i].setMaxValue(40);
                judgePickers[i].setDisplayedValues(marks);
                judgePickers[i].setValue((int) (initialMarks[i]*2));
            }
            return builder.create();
        }
    }

}
