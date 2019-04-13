package ty.henry.jumpingstats.jumpers;


import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ty.henry.jumpingstats.Country;
import ty.henry.jumpingstats.MainViewModel;
import ty.henry.jumpingstats.PatternInputFilter;
import ty.henry.jumpingstats.R;

public class AddEditJumperFragment extends Fragment {

    public static final String ID_ARG = "id";

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

    public AddEditJumperFragment() {
    }

    public static AddEditJumperFragment newInstance(int jumperToEditId) {
        Bundle args = new Bundle();
        args.putInt(ID_ARG, jumperToEditId);

        AddEditJumperFragment fragment = new AddEditJumperFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_add_edit_jumper, container, false);
        nameEditText = fragmentView.findViewById(R.id.nameEditText);
        surnameEditText = fragmentView.findViewById(R.id.surnameEditText);

        heightEditText = fragmentView.findViewById(R.id.heightEditText);
        Pattern heightPattern = Pattern.compile("[12]|([12]\\.[0-9]{0,2})");
        InputFilter heightInputFilter = new PatternInputFilter(heightPattern);
        heightEditText.setFilters(new InputFilter[]{heightInputFilter});

        nameEditText.addTextChangedListener(textWatcher);
        surnameEditText.addTextChangedListener(textWatcher);
        heightEditText.addTextChangedListener(textWatcher);

        birthDatePicker = fragmentView.findViewById(R.id.datePicker);
        LocalDate today = LocalDate.now();
        birthDatePicker.updateDate(today.getYear() - 30, today.getMonthValue() - 1,
                today.getDayOfMonth());

        countrySpinner = fragmentView.findViewById(R.id.countrySpinner);
        countrySpinner.setAdapter(createCountrySpinnerAdapter(getActivity()));

        saveButton = fragmentView.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveJumper();
            }
        });

        int jumperId = getArguments() != null ? getArguments().getInt(ID_ARG, -1) : -1;
        if(jumperId != -1) {
            MainViewModel mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
            jumperToEdit = mainViewModel.getJumperById(jumperId);
        }

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
            map.put(name, context.getString(c.getNameId()));
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
        LocalDate dateOfBirth = jumperToEdit.getDateOfBirth();
        birthDatePicker.updateDate(dateOfBirth.getYear(), dateOfBirth.getMonthValue() - 1,
                dateOfBirth.getDayOfMonth());
        int i = jumperToEdit.getCountry().ordinal();
        countrySpinner.setSelection(i);
    }

    private Jumper createJumper() {
        String name = nameEditText.getText().toString();
        String surname = surnameEditText.getText().toString();
        float height = Float.parseFloat(heightEditText.getText().toString());
        LocalDate dateOfBirth = LocalDate.of(birthDatePicker.getYear(), birthDatePicker.getMonth() + 1,
                birthDatePicker.getDayOfMonth());
        Country country = Country.values()[countrySpinner.getSelectedItemPosition()];
        Jumper jumper = new Jumper(name, surname, country, dateOfBirth, height);
        if(jumperToEdit!=null) {
            jumper.setId(jumperToEdit.getId());
        }
        return jumper;
    }

    private void saveJumper() {
        Jumper jumper = createJumper();
        boolean exists;
        MainViewModel mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        List<Jumper> jumpers = mainViewModel.getJumpers().getValue();
        int ind = jumpers.indexOf(jumper);
        if(jumperToEdit==null) {
            exists = ind!=-1;
        }
        else {
            exists = ind!=-1 && jumpers.get(ind).getId()!=jumper.getId();
        }
        if(exists) {
            Toast.makeText(getActivity(), R.string.jumper_exists_message, Toast.LENGTH_SHORT).show();
        }
        else {
            if(jumperToEdit == null) {
                mainViewModel.addJumper(jumper);
            }
            else {
                mainViewModel.updateJumper(jumper);
            }
        }
    }
}
