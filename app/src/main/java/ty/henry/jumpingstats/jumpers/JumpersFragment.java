package ty.henry.jumpingstats.jumpers;


import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
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

import java.util.List;
import java.util.Set;

import ty.henry.jumpingstats.MainViewModel;
import ty.henry.jumpingstats.R;
import ty.henry.jumpingstats.TextImageAdapter;


public class JumpersFragment extends Fragment {

    private JumpersFragmentListener listener;
    private TextImageAdapter<Jumper> textImageAdapter;
    private FloatingActionButton addButton;
    private RecyclerView recyclerView;

    public interface JumpersFragmentListener {
        void openFragment(Fragment fragment, boolean backStack);
    }

    public JumpersFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_jumpers, container, false);
        recyclerView = fragmentView.findViewById(R.id.jumpersRecycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        addButton = fragmentView.findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddEditJumperFragment fragment = new AddEditJumperFragment();
                listener.openFragment(fragment, true);
            }
        });
        return fragmentView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        MainViewModel mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        mainViewModel.getJumpers().observe(this, this::updateRecyclerView);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (JumpersFragmentListener) context;
        } catch (ClassCastException ex) {
            throw new ClassCastException(context.toString() + " must implement JumpersFragmentListener");
        }
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
                                MainViewModel mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
                                mainViewModel.deleteJumpers(toDelete);
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

    private void updateRecyclerView(List<Jumper> jumpers) {
        textImageAdapter = new TextImageAdapter<>(jumpers, getActivity());
        textImageAdapter.setListener(new TextImageAdapter.Listener() {
            @Override
            public void onClick(int position) {
                int id = jumpers.get(position).getId();
                JumperDetailsFragment fragment = JumperDetailsFragment.newInstance(id);
                listener.openFragment(fragment, true);
            }
        });
        recyclerView.setAdapter(textImageAdapter);
    }
}
