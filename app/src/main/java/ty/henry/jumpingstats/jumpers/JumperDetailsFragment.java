package ty.henry.jumpingstats.jumpers;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ty.henry.jumpingstats.ConfirmFragment;
import ty.henry.jumpingstats.DBHelper;
import ty.henry.jumpingstats.R;


public class JumperDetailsFragment extends Fragment {

    private Jumper jumper;
    private JumpersFragment.JumpersFragmentListener listener;

    public JumperDetailsFragment() {

    }

    public void setJumper(Jumper jumper) {
        this.jumper = jumper;
    }

    public void setListener(JumpersFragment.JumpersFragmentListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_jumper_details, container, false);
        TextView nameTextView = fragmentView.findViewById(R.id.nameTextView);
        TextView surnameTextView = fragmentView.findViewById(R.id.surnameTextView);
        TextView countryTextView = fragmentView.findViewById(R.id.countryTextView);
        TextView dateTextView = fragmentView.findViewById(R.id.dateTextView);
        TextView heightTextView = fragmentView.findViewById(R.id.heightTextView);

        String nameText = getString(R.string.name) + ": " + jumper.getName();
        String surnameText = getString(R.string.surname) + ": " + jumper.getSurname();
        String countryText = getString(R.string.country) + ": " + jumper.getCountry().getCountryName(getActivity());
        String dateText = getString(R.string.date_of_birth) + ": " + DBHelper.calendarToString(jumper.getDateOfBirth());
        String heightText = getString(R.string.height_text) + ": " + jumper.getHeight() + "m";

        nameTextView.setText(nameText);
        surnameTextView.setText(surnameText);
        countryTextView.setText(countryText);
        dateTextView.setText(dateText);
        heightTextView.setText(heightText);
        return fragmentView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
                        DeleteTask deleteTask = new DeleteTask();
                        deleteTask.execute(jumper);
                    }
                });
                confirmFragment.show(getChildFragmentManager(), "dialog");
                return true;
            case R.id.edit:
                AddEditJumperFragment fragment = new AddEditJumperFragment();
                fragment.setListener(listener);
                fragment.editJumper(jumper, this);
                listener.openFragment(fragment, true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private class DeleteTask extends AsyncTask<Jumper, Void, Jumper> {
        @Override
        protected Jumper doInBackground(Jumper... params) {
            DBHelper dbHelper = new DBHelper(getActivity());
            dbHelper.deleteJumper(params[0].getId());
            return params[0];
        }

        @Override
        protected void onPostExecute(Jumper jumper) {
            listener.onJumperDeleted(jumper);
            if(getActivity()!=null) {
                getActivity().onBackPressed();
            }
        }
    }
}
