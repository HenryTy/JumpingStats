package ty.henry.jumpingstats.competitions;


import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ty.henry.jumpingstats.ConfirmFragment;
import ty.henry.jumpingstats.MainViewModel;
import ty.henry.jumpingstats.PatternInputFilter;
import ty.henry.jumpingstats.R;
import ty.henry.jumpingstats.statistics.NoResultForJumperException;


public class EditResultFragment extends Fragment {

    public static final String SERIES_ARG = "series";

    private int series;
    private SeriesResult result;
    private List<Float> judgeMarks;
    private AddEditResultsFragment parent;

    private TextView marksTextView;
    private EditText distanceEditText;
    private EditText windEditText;
    private EditText speedEditText;
    private EditText gateEditText;
    private FloatingActionButton saveButton;
    private FloatingActionButton deleteButton;

    private TextWatcher textWatcher = new TextWatcher() {

        Pattern windPattern = Pattern.compile("-?[0-9]+(\\.[0-9]{1,2})?");
        Pattern speedPattern = Pattern.compile("[0-9]+(\\.[0-9])?");
        Pattern gatePattern = Pattern.compile("-?[0-9]+(\\.[0-9])?");

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            Matcher[] matchers = new Matcher[4];
            matchers[0] = PatternInputFilter.DISTANCE_END_PATTERN.matcher(distanceEditText.getText());
            matchers[1] = windPattern.matcher(windEditText.getText());
            matchers[2] = speedPattern.matcher(speedEditText.getText());
            matchers[3] = gatePattern.matcher(gateEditText.getText());
            for(Matcher m : matchers) {
                if(!m.matches()) {
                    saveButton.setVisibility(View.INVISIBLE);
                    return;
                }
            }
            saveButton.setVisibility(View.VISIBLE);
        }
    };

    public EditResultFragment() {

    }

    public static EditResultFragment newInstance(int series) {
        Result.checkSeriesArgument(series);

        Bundle args = new Bundle();
        args.putInt(SERIES_ARG, series);

        EditResultFragment fragment = new EditResultFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_edit_result, container, false);
        if(!(getParentFragment() instanceof AddEditResultsFragment)) {
            throw new IllegalStateException("Parent fragment for EditResultFragment must be AddEditResultsFragment");
        }

        series = getArguments() != null ? getArguments().getInt(SERIES_ARG, 1) : 1;

        parent = (AddEditResultsFragment) getParentFragment();
        try {
            if(parent.result != null) {
                result = parent.result.getResultForSeries(series);
            }
        } catch (NoResultForJumperException ex) {

        }

        distanceEditText = fragmentView.findViewById(R.id.distanceEditText);
        windEditText = fragmentView.findViewById(R.id.windEditText);
        speedEditText = fragmentView.findViewById(R.id.speedEditText);
        gateEditText = fragmentView.findViewById(R.id.gateEditText);

        Pattern windPattern = Pattern.compile("(-?[0-9]*)|(-?[0-9]+\\.[0-9]{0,2})");
        Pattern speedPattern = Pattern.compile("([0-9]*)|([0-9]+\\.[0-9]?)");
        Pattern gatePattern = Pattern.compile("(-?[0-9]*)|(-?[0-9]+\\.[0-9]?)");

        InputFilter distanceInputFilter = new PatternInputFilter(PatternInputFilter.DISTANCE_PATTERN);
        InputFilter windInputFilter = new PatternInputFilter(windPattern);
        InputFilter speedInputFilter = new PatternInputFilter(speedPattern);
        InputFilter gateInputFilter = new PatternInputFilter(gatePattern);

        distanceEditText.setFilters(new InputFilter[]{distanceInputFilter});
        windEditText.setFilters(new InputFilter[]{windInputFilter});
        speedEditText.setFilters(new InputFilter[]{speedInputFilter});
        gateEditText.setFilters(new InputFilter[]{gateInputFilter});

        saveButton = fragmentView.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(view -> saveResult());
        deleteButton = fragmentView.findViewById(R.id.deleteButton);

        distanceEditText.addTextChangedListener(textWatcher);
        windEditText.addTextChangedListener(textWatcher);
        speedEditText.addTextChangedListener(textWatcher);
        gateEditText.addTextChangedListener(textWatcher);

        if(result == null) {
            judgeMarks = Collections.nCopies(5, 17.5f);
            deleteButton.setVisibility(View.GONE);
        }
        else {
            judgeMarks = result.getStyleScores();
            fillWithDataToEdit();
            deleteButton.setOnClickListener(view -> deleteResult());
        }

        marksTextView = fragmentView.findViewById(R.id.marksTextView);
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
        for (int i = 0; i < judgeMarks.size(); i++) {
            marksBuilder.append(judgeMarks.get(i));
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

    private SeriesResult createResult() {
        float distance = Float.parseFloat(distanceEditText.getText().toString());
        float wind = Float.parseFloat(windEditText.getText().toString());
        float speed = Float.parseFloat(speedEditText.getText().toString());
        float gateCompensation = Float.parseFloat(gateEditText.getText().toString());

        return new SeriesResult(series, distance, wind, speed, judgeMarks, gateCompensation, parent.result);
    }

    private void saveResult() {
        SeriesResult createdResult = createResult();
        MainViewModel mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        if(this.result == null) {
            mainViewModel.addResult(createdResult);
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(view -> deleteResult());
        }
        else {
            mainViewModel.updateResult(createdResult);
        }

        this.result = createdResult;
        parent.updateResult(createdResult);
    }

    private void deleteResult() {
        ConfirmFragment confirmFragment = new ConfirmFragment();
        confirmFragment.setMessage(getString(R.string.delete_result_message));
        confirmFragment.setListener(() -> {
            MainViewModel mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
            mainViewModel.deleteResult(result);
            parent.deleteResult(series);
            clearData();
        });
        confirmFragment.show(getChildFragmentManager(), "dialog");
    }

    private void clearData() {
        result = null;
        distanceEditText.setText("");
        windEditText.setText("");
        speedEditText.setText("");
        gateEditText.setText("");
        judgeMarks = Collections.nCopies(5, 17.5f);
        marksTextView.setText(getMarksText());
        deleteButton.setVisibility(View.GONE);
    }

    public static class SetMarksFragment extends DialogFragment {

        public interface DialogListener {
            void onPositiveClick(List<Float> marks);
        }

        private NumberPicker[] judgePickers;
        private DialogListener listener;
        private List<Float> initialMarks;

        public void setListener(DialogListener listener) {
            this.listener = listener;
        }

        public void setInitialMarks(List<Float> initialMarks) {
            this.initialMarks = initialMarks;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View pickersView = getActivity().getLayoutInflater().inflate(R.layout.pickers_judge, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(pickersView)
                    .setPositiveButton(R.string.ok, (dialogInterface, j) ->  {
                        if(listener != null) {
                            List<Float> marks = new ArrayList<>();
                            for (int i = 0; i < 5; i++) {
                                marks.add(((float) judgePickers[i].getValue()) / 2);
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
                judgePickers[i].setValue((int) (initialMarks.get(i)*2));
            }
            return builder.create();
        }
    }
}
