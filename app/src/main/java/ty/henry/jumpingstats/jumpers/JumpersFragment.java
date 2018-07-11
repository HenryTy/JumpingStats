package ty.henry.jumpingstats.jumpers;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Set;

import ty.henry.jumpingstats.DBHelper;
import ty.henry.jumpingstats.R;
import ty.henry.jumpingstats.TextImageAdapter;


public class JumpersFragment extends Fragment {

    private JumpersFragmentListener listener;
    private TextImageAdapter<Jumper> textImageAdapter;
    private FloatingActionButton addButton;

    public interface JumpersFragmentListener {
        void openFragment(Fragment fragment, boolean backStack);
        ArrayList<Jumper> getJumpersList();
        void onJumperAdded(Jumper jumper);
        void onJumperDeleted(Jumper jumper);
        void onJumpersDeleted(Set<Jumper> delJumps);
        void onJumperUpdated(Jumper jumper);
    }

    public JumpersFragment() {
    }

    public void setListener(JumpersFragmentListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_jumpers, container, false);
        RecyclerView recyclerView = fragmentView.findViewById(R.id.jumpersRecycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        textImageAdapter = new TextImageAdapter<>(listener.getJumpersList());
        textImageAdapter.setListener(new TextImageAdapter.Listener() {
            @Override
            public void onClick(int position) {
                JumperDetailsFragment fragment = new JumperDetailsFragment();
                fragment.setListener(listener);
                fragment.setJumper(listener.getJumpersList().get(position));
                listener.openFragment(fragment, true);
            }
        });
        recyclerView.setAdapter(textImageAdapter);

        addButton = fragmentView.findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddEditJumperFragment fragment = new AddEditJumperFragment();
                fragment.setListener(listener);
                listener.openFragment(fragment, true);
            }
        });
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
        menu.findItem(R.id.edit).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                if(getActivity()!=null) {
                    ((AppCompatActivity) getActivity()).startSupportActionMode(new ActionMode.Callback() {
                        @Override
                        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                            textImageAdapter.setMultiselection(true);
                            menu.add(getString(R.string.delete));
                            addButton.setVisibility(View.GONE);
                            return true;
                        }

                        @Override
                        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                            return false;
                        }

                        @Override
                        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                            Set<Jumper> toDelete = textImageAdapter.getSelectedItems();
                            if(toDelete.size() > 0) {
                                DeleteTask deleteTask = new DeleteTask(toDelete);
                                deleteTask.execute();
                                listener.onJumpersDeleted(toDelete);
                            }
                            mode.finish();
                            return true;
                        }

                        @Override
                        public void onDestroyActionMode(ActionMode mode) {
                            textImageAdapter.setMultiselection(false);
                            textImageAdapter.notifyDataSetChanged();
                            addButton.setVisibility(View.VISIBLE);
                        }
                    });
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class DeleteTask extends AsyncTask<Void, Void, Boolean> {

        private Set<Jumper> toDelete;

        DeleteTask(Set<Jumper> jumpers) {
            toDelete = jumpers;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            DBHelper dbHelper = new DBHelper(getActivity());
            dbHelper.deleteJumpers(toDelete);
            return true;
        }

    }

}
