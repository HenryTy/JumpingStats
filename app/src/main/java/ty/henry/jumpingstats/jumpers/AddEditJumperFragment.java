package ty.henry.jumpingstats.jumpers;


import android.content.Context;
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
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ty.henry.jumpingstats.Country;
import ty.henry.jumpingstats.DBHelper;
import ty.henry.jumpingstats.MainActivity;
import ty.henry.jumpingstats.PatternInputFilter;
import ty.henry.jumpingstats.R;
import ty.henry.jumpingstats.jumpers.Jumper;
import ty.henry.jumpingstats.jumpers.JumperDetailsFragment;
import ty.henry.jumpingstats.jumpers.JumpersFragment;

public class AddEditJumperFragment extends Fragment {

    private EditText nameEditText;
    private EditText surnameEditText;
    private EditText heightEditText;
    private DatePicker birthDatePicker;
    private Spinner countrySpinner;
    private FloatingActionButton saveButton;

    private TextWatcher textWatcher = new TextWatcher() {
        Pattern heightPattern = Pattern.compile("[12](\\.[0-9]{1,2})?");
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            int l1 = nameEditText.getText().toString().trim().length();
            int l2 = surnameEditText.getText().toString().trim().length();
            Matcher matcher = heightPattern.matcher(heightEditText.getText());
            if(l1 > 0 && l2 > 0 && matcher.matches()) {
                saveButton.setVisibility(View.VISIBLE);
            }
            else {
                saveButton.setVisibility(View.INVISIBLE);
            }
        }
    };
    private Jumper jumperToEdit;
    private JumperDetailsFragment jumperDetailsFragment;
    private JumpersFragment.JumpersFragmentListener jumpersFragmentListener;

    public AddEditJumperFragment() {
    }

    public void editJumper(Jumper jumper, JumperDetailsFragment jumperDetailsFragment) {
        this.jumperToEdit = jumper;
        this.jumperDetailsFragment = jumperDetailsFragment;
    }

    public void setListener(JumpersFragment.JumpersFragmentListener listener) {
        this.jumpersFragmentListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_add_edit_jumper, container, false);
        nameEditText = fragmentView.findViewById(R.id.nameEditText);
        surnameEditText = fragmentView.findViewById(R.id.surnameEditText);

        heightEditText = fragmentView.findViewById(R.id.heightEditText);
        Pattern heightPattern = Pattern.compile("[12]?\\.?[0-9]{0,2}");
        InputFilter heightInputFilter = new PatternInputFilter(heightPattern);
        heightEditText.setFilters(new InputFilter[]{heightInputFilter});

        nameEditText.addTextChangedListener(textWatcher);
        surnameEditText.addTextChangedListener(textWatcher);
        heightEditText.addTextChangedListener(textWatcher);

        birthDatePicker = fragmentView.findViewById(R.id.datePicker);
        Calendar today = Calendar.getInstance();
        birthDatePicker.updateDate(today.get(Calendar.YEAR) - 30, today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH));

        countrySpinner = fragmentView.findViewById(R.id.countrySpinner);
        countrySpinner.setAdapter(createCountrySpinnerAdapter(getActivity()));

        saveButton = fragmentView.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveJumper();
            }
        });
        if(jumperToEdit!=null) {
            fillWithDataToEdit();
        }
        return fragmentView;
    }

    public static SpinnerAdapter createCountrySpinnerAdapter(Context context) {
        String name = "name";
        String flag = "flag";
        ArrayList<HashMap<String, Object>> spinnerData = new ArrayList<>();
        for(Country c : Country.values()) {
            HashMap<String, Object> map = new HashMap<>();
            map.put(name, c.getCountryName(context));
            map.put(flag, c.getFlagId());
            spinnerData.add(map);
        }
        return new SimpleAdapter(context, spinnerData, R.layout.row_image_text, new String[]{name, flag},
                new int[]{R.id.text, R.id.image});
    }

    private void fillWithDataToEdit() {
        nameEditText.setText(jumperToEdit.getName());
        surnameEditText.setText(jumperToEdit.getSurname());
        heightEditText.setText(Float.toString(jumperToEdit.getHeight()));
        Calendar dateOfBirth = jumperToEdit.getDateOfBirth();
        birthDatePicker.updateDate(dateOfBirth.get(Calendar.YEAR), dateOfBirth.get(Calendar.MONTH),
                dateOfBirth.get(Calendar.DAY_OF_MONTH));
        int i = jumperToEdit.getCountry().ordinal();
        countrySpinner.setSelection(i);
    }

    private Jumper createJumper() {
        String name = nameEditText.getText().toString();
        String surname = surnameEditText.getText().toString();
        float height = Float.parseFloat(heightEditText.getText().toString());
        Calendar dateOfBirth = Calendar.getInstance();
        dateOfBirth.set(birthDatePicker.getYear(), birthDatePicker.getMonth(), birthDatePicker.getDayOfMonth());
        Country country = Country.values()[countrySpinner.getSelectedItemPosition()];
        Jumper jumper = new Jumper(name, surname, country, dateOfBirth, height);
        if(jumperToEdit!=null) {
            jumper.setId(jumperToEdit.getId());
            jumper.setCompResMap(jumperToEdit.getCompResMap());
        }
        return jumper;
    }

    private void saveJumper() {
        Jumper jumper = createJumper();
        boolean exists;
        ArrayList<Jumper> jumpers = jumpersFragmentListener.getJumpersList();
        int ind = jumpers.indexOf(jumper);
        SaveTask saveTask;
        if(jumperToEdit==null) {
            exists = ind!=-1;
            saveTask = new SaveTask(SaveTask.INSERT);
        }
        else {
            exists = ind!=-1 && jumpers.get(ind).getId()!=jumper.getId();
            saveTask = new SaveTask(SaveTask.UPDATE);
        }
        if(exists) {
            Toast.makeText(getActivity(), "Such a jumper already exists", Toast.LENGTH_SHORT).show();
        }
        else {
            saveTask.execute(jumper);
        }
    }

    private class SaveTask extends AsyncTask<Jumper, Void, Jumper> {
        static final int INSERT = 0;
        static final int UPDATE = 1;

        private int action;

        SaveTask(int action) {
            if(action!=INSERT && action!=UPDATE) {
                throw new IllegalArgumentException();
            }
            this.action = action;
        }

        @Override
        protected Jumper doInBackground(Jumper... params) {
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
        protected void onPostExecute(Jumper jumper) {
            switch (action) {
                case INSERT:
                    jumpersFragmentListener.onJumperAdded(jumper);
                    break;
                case UPDATE:
                    jumpersFragmentListener.onJumperUpdated(jumper);
                    jumperDetailsFragment.setJumper(jumper);
            }
            Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();
            if(getActivity()!=null) {
                getActivity().onBackPressed();
            }
        }
    }
}
