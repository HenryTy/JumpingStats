package ty.henry.jumpingstats.jumpers;


import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.time.format.DateTimeFormatter;

import ty.henry.jumpingstats.ConfirmFragment;
import ty.henry.jumpingstats.MainViewModel;
import ty.henry.jumpingstats.R;


public class JumperDetailsFragment extends Fragment {

    public static final String ID_ARG = "id";

    private Jumper jumper;
    private JumpersFragment.JumpersFragmentListener listener;

    public JumperDetailsFragment() {

    }

    public static JumperDetailsFragment newInstance(int jumperId) {
        Bundle args = new Bundle();
        args.putInt(ID_ARG, jumperId);

        JumperDetailsFragment fragment = new JumperDetailsFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_jumper_details, container, false);

        int jumperId = getArguments() != null ? getArguments().getInt(ID_ARG, -1) : -1;

        MainViewModel mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        jumper = mainViewModel.getJumperById(jumperId);

        if(jumper != null) {
            TextView nameTextView = fragmentView.findViewById(R.id.nameTextView);
            TextView surnameTextView = fragmentView.findViewById(R.id.surnameTextView);
            TextView countryTextView = fragmentView.findViewById(R.id.countryTextView);
            TextView dateTextView = fragmentView.findViewById(R.id.dateTextView);
            TextView heightTextView = fragmentView.findViewById(R.id.heightTextView);

            String nameText = getString(R.string.name) + ": " + jumper.getName();
            String surnameText = getString(R.string.surname) + ": " + jumper.getSurname();
            String countryText = getString(R.string.country) + ": " + getActivity().getString(jumper.getCountry().getNameId());
            String dateText = getString(R.string.date_of_birth) + ": " + DateTimeFormatter.ISO_LOCAL_DATE.format(jumper.getDateOfBirth());
            String heightText = getString(R.string.height_text) + ": " + jumper.getHeight() + "m";

            nameTextView.setText(nameText);
            surnameTextView.setText(surnameText);
            countryTextView.setText(countryText);
            dateTextView.setText(dateText);
            heightTextView.setText(heightText);
        }
        return fragmentView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (JumpersFragment.JumpersFragmentListener) context;
        } catch (ClassCastException ex) {
            throw new ClassCastException(context.toString() + " must implement JumpersFragmentListener");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_delete_edit, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                ConfirmFragment confirmFragment = new ConfirmFragment();
                confirmFragment.setMessage(getString(R.string.delete_jumper_message));
                confirmFragment.setListener(new ConfirmFragment.DialogListener() {
                    @Override
                    public void onPositiveClick() {
                        MainViewModel mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
                        mainViewModel.deleteJumper(jumper.getId());
                        getActivity().onBackPressed();
                    }
                });
                confirmFragment.show(getChildFragmentManager(), "dialog");
                return true;
            case R.id.edit:
                AddEditJumperFragment fragment = AddEditJumperFragment.newInstance(jumper.getId());
                listener.openFragment(fragment, true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
