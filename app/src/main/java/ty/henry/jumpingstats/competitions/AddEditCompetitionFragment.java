package ty.henry.jumpingstats.competitions;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ty.henry.jumpingstats.Country;
import ty.henry.jumpingstats.DBHelper;
import ty.henry.jumpingstats.PatternInputFilter;
import ty.henry.jumpingstats.R;
import ty.henry.jumpingstats.jumpers.AddEditJumperFragment;


public class AddEditCompetitionFragment extends Fragment {

    private Spinner countrySpinner;
    private EditText cityEditText;
    private EditText pointKEditText;
    private EditText hillSizeEditText;
    private EditText headWindEditText;
    private EditText tailWindEditText;
    private DatePicker datePicker;
    private FloatingActionButton saveButton;
    private Competition compToEdit;
    private CompetitionDetailsFragment competitionDetailsFragment;
    private CompetitionsFragment.CompetitionsFragmentListener competitionsFragmentListener;
    private TextWatcher textWatcher = new TextWatcher() {

        Pattern windPointsPattern = Pattern.compile("[0-9]+(\\.[0-9]{1,2})?");

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            int l = cityEditText.getText().toString().trim().length();
            Matcher matcher1 = PatternInputFilter.DISTANCE_END_PATTERN.matcher(pointKEditText.getText());
            Matcher matcher2 = PatternInputFilter.DISTANCE_END_PATTERN.matcher(hillSizeEditText.getText());
            Matcher matcher3 = windPointsPattern.matcher(headWindEditText.getText());
            Matcher matcher4 = windPointsPattern.matcher(tailWindEditText.getText());
            if(l>0 && matcher1.matches() && matcher2.matches() && matcher3.matches() && matcher4.matches()) {
                saveButton.setVisibility(View.VISIBLE);
            }
            else {
                saveButton.setVisibility(View.INVISIBLE);
            }
        }
    };

    public AddEditCompetitionFragment() {

    }

    public void editCompetition(Competition competition, CompetitionDetailsFragment competitionDetailsFragment) {
        this.compToEdit = competition;
        this.competitionDetailsFragment = competitionDetailsFragment;
    }

    public void setListener(CompetitionsFragment.CompetitionsFragmentListener listener) {
        this.competitionsFragmentListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_add_edit_competition, container, false);
        countrySpinner = fragmentView.findViewById(R.id.countrySpinner);
        countrySpinner.setAdapter(AddEditJumperFragment.createCountrySpinnerAdapter(getActivity()));
        cityEditText = fragmentView.findViewById(R.id.cityEditText);

        pointKEditText = fragmentView.findViewById(R.id.pointKEditText);
        hillSizeEditText = fragmentView.findViewById(R.id.hillSizeEditText);
        InputFilter distanceFilter = new PatternInputFilter(PatternInputFilter.DISTANCE_PATTERN);
        pointKEditText.setFilters(new InputFilter[]{distanceFilter});
        hillSizeEditText.setFilters(new InputFilter[]{distanceFilter});

        headWindEditText = fragmentView.findViewById(R.id.headWindEditText);
        tailWindEditText = fragmentView.findViewById(R.id.tailWindEditText);
        Pattern windPointsPattern = Pattern.compile("[0-9]*|([0-9]+\\.[0-9]{0,2})");
        InputFilter windPointsFilter = new PatternInputFilter(windPointsPattern);
        headWindEditText.setFilters(new InputFilter[]{windPointsFilter});
        tailWindEditText.setFilters(new InputFilter[]{windPointsFilter});

        datePicker = fragmentView.findViewById(R.id.datePicker);
        saveButton = fragmentView.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveTask saveTask;
                if(compToEdit==null) {
                    saveTask = new SaveTask(SaveTask.INSERT);
                }
                else {
                    saveTask = new SaveTask(SaveTask.UPDATE);
                }
                saveTask.execute(createCompetition());
            }
        });

        cityEditText.addTextChangedListener(textWatcher);
        pointKEditText.addTextChangedListener(textWatcher);
        hillSizeEditText.addTextChangedListener(textWatcher);
        headWindEditText.addTextChangedListener(textWatcher);
        tailWindEditText.addTextChangedListener(textWatcher);

        if(compToEdit!=null) {
            fillWithDataToEdit();
        }
        return fragmentView;
    }

    private void fillWithDataToEdit() {
        int i = compToEdit.getCountry().ordinal();
        countrySpinner.setSelection(i);
        cityEditText.setText(compToEdit.getCity());
        pointKEditText.setText(Float.toString(compToEdit.getPointK()));
        hillSizeEditText.setText(Float.toString(compToEdit.getHillSize()));
        headWindEditText.setText(Float.toString(compToEdit.getHeadWindPoints()));
        tailWindEditText.setText(Float.toString(compToEdit.getTailWindPoints()));
        Calendar date = compToEdit.getDate();
        datePicker.updateDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH));
    }

    private Competition createCompetition() {
        Country country = Country.values()[countrySpinner.getSelectedItemPosition()];
        String city = cityEditText.getText().toString();
        float pointK = Float.parseFloat(pointKEditText.getText().toString());
        float hillSize = Float.parseFloat(hillSizeEditText.getText().toString());
        float headWindPoints = Float.parseFloat(headWindEditText.getText().toString());
        float tailWindPoints = Float.parseFloat(tailWindEditText.getText().toString());
        Calendar date = Calendar.getInstance();
        date.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
        Competition competition = new Competition(city, country, pointK, hillSize, headWindPoints, tailWindPoints, date);
        if(compToEdit!=null) {
            competition.setId(compToEdit.getId());
        }
        return competition;
    }

    private class SaveTask extends AsyncTask<Competition, Void, Competition> {
        static final int INSERT = 0;
        static final int UPDATE = 1;

        private int action;

        SaveTask(int action) {
            this.action = action;
        }

        @Override
        protected Competition doInBackground(Competition... params) {
            DBHelper dbHelper = new DBHelper(getActivity());
            switch (action) {
                case INSERT:
                    int id = dbHelper.add(params[0]);
                    params[0].setId(id);
                    break;
                case UPDATE:
                    dbHelper.update(params[0]);
            }
            return params[0];
        }

        @Override
        protected void onPostExecute(Competition competition) {
            switch (action) {
                case INSERT:
                    competitionsFragmentListener.onCompetitionAdded(competition);
                    break;
                case UPDATE:
                    competitionsFragmentListener.onCompetitionUpdated(competition);
                    competitionDetailsFragment.setCompetition(competition);
            }
            Toast.makeText(getActivity(), R.string.saved, Toast.LENGTH_SHORT).show();
            if(getActivity()!=null) {
                getActivity().onBackPressed();
            }
        }
    }

}
