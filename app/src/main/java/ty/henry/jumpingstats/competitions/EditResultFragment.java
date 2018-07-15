package ty.henry.jumpingstats.competitions;


import android.app.Dialog;
import android.os.AsyncTask;
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
import android.widget.Toast;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ty.henry.jumpingstats.ConfirmFragment;
import ty.henry.jumpingstats.DBHelper;
import ty.henry.jumpingstats.PatternInputFilter;
import ty.henry.jumpingstats.R;


public class EditResultFragment extends Fragment {

    private int series;
    private Result result;
    private float[] judgeMarks;
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
            judgeMarks = new float[5];
            Arrays.fill(judgeMarks, 17.5f);
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

        return new Result(parent.jumper, parent.competition, series, distance, wind, speed, judgeMarks, gateCompensation);
    }

    private void saveResult() {
        Result createdResult = createResult();
        UpdateTask updateTask;
        if(this.result == null) {
            updateTask = new UpdateTask(UpdateTask.INSERT);
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(view -> deleteResult());
        }
        else {
            updateTask = new UpdateTask(UpdateTask.UPDATE);
        }
        this.result = createdResult;
        updateTask.execute(createdResult);
        parent.jumper.setResult(parent.competition, createdResult);
    }

    private void deleteResult() {
        ConfirmFragment confirmFragment = new ConfirmFragment();
        confirmFragment.setMessage(getString(R.string.delete_result_message));
        confirmFragment.setListener(() -> {
            UpdateTask updateTask = new UpdateTask(UpdateTask.DELETE);
            updateTask.execute(result);
            parent.jumper.removeResult(parent.competition, series);
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
        Arrays.fill(judgeMarks, 17.5f);
        marksTextView.setText(getMarksText());
        deleteButton.setVisibility(View.GONE);
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

    private class UpdateTask extends AsyncTask<Result, Void, Boolean> {

        static final int INSERT = 0;
        static final int UPDATE = 1;
        static final int DELETE = 2;

        private int action;

        UpdateTask(int action) {
            if(action!=INSERT && action!=UPDATE && action!=DELETE) {
                throw new IllegalArgumentException();
            }
            this.action = action;
        }

        @Override
        protected Boolean doInBackground(Result... res) {
            DBHelper dbHelper = new DBHelper(getContext());
            switch (action) {
                case INSERT:
                    dbHelper.add(res[0]);
                    break;
                case UPDATE:
                    dbHelper.update(res[0]);
                    break;
                case DELETE:
                    dbHelper.deleteResult(res[0]);
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            String toastText;
            if(action == DELETE) {
                toastText = getString(R.string.deleted);
            }
            else {
                toastText = getString(R.string.saved);
            }
            Toast.makeText(getContext(), toastText, Toast.LENGTH_SHORT).show();
        }
    }

}
