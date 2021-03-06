package com.jerbotron_mac.spotishake.activities.home.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jerbotron_mac.spotishake.R;
import com.jerbotron_mac.spotishake.activities.home.HomeActivity;
import com.jerbotron_mac.spotishake.activities.home.HomePresenter;
import com.jerbotron_mac.spotishake.activities.home.adapters.HistoryListAdapter;
import com.jerbotron_mac.spotishake.activities.home.custom.RecyclerItemTouchHelper;
import com.jerbotron_mac.spotishake.data.DatabaseAdapter;
import com.jerbotron_mac.spotishake.data.SongInfo;
import com.jerbotron_mac.spotishake.utils.SharedUserPrefs;

public class HistoryFragment extends Fragment implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener, SwipeRefreshLayout.OnRefreshListener{

    private DatabaseAdapter databaseAdapter;

    private Context context;
    private SharedUserPrefs sharedUserPrefs;
    private HomePresenter presenter;
    private HistoryListAdapter historyListAdapter;
    private ItemTouchHelper.SimpleCallback itemTouchHelperCallback;

    private RecyclerView recyclerView;
    private RelativeLayout fragmentLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView noHistoryText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        fragmentLayout = (RelativeLayout) view.findViewById(R.id.fragment_history_layout);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.history_swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this);

        noHistoryText = (TextView) view.findViewById(R.id.no_history);

        recyclerView = (RecyclerView) view.findViewById(R.id.song_history_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        if (historyListAdapter != null) {
            historyListAdapter.refreshCursor();
            recyclerView.setAdapter(historyListAdapter);
            if (historyListAdapter.getItemCount() == 0) {
                noHistoryText.setVisibility(View.VISIBLE);
            } else {
                noHistoryText.setVisibility(View.GONE);
            }
        }

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.history_toolbar);
        toolbar.setTitle(R.string.history_toolbar_title);
        toolbar.inflateMenu(R.menu.menu_history);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menu_settings) {
                    ((HomeActivity) context).launchSettingsActivity();
                    return true;
                }
                return false;
            }
        });

        itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        return view;
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        this.context = context;
        if (sharedUserPrefs != null) {
            sharedUserPrefs.setHistoryFragmentId(getTag());
        }
        ((HomeActivity) context).getHomeComponent().inject(this);
    }

    public void init(@NonNull HomePresenter presenter,
                     @NonNull SharedUserPrefs sharedUserPrefs,
                     @NonNull DatabaseAdapter databaseAdapter) {
        this.presenter = presenter;
        this.sharedUserPrefs = sharedUserPrefs;
        this.databaseAdapter = databaseAdapter;
        initHistoryListAdapter();
    }

    public void refreshView() {
        if (historyListAdapter != null) {
            historyListAdapter.refreshCursor();
            historyListAdapter.notifyDataSetChanged();
        }
    }

    public void initHistoryListAdapter() {
        if (historyListAdapter == null) {
            historyListAdapter = new HistoryListAdapter(this, presenter, databaseAdapter);
        }
    }

    @Override
    public void onRefresh() {
        if (historyListAdapter != null) {
            historyListAdapter.refreshAdapter();
            if (historyListAdapter.getItemCount() == 0) {
                noHistoryText.setVisibility(View.VISIBLE);
            } else {
                noHistoryText.setVisibility(View.GONE);
            }
        } else {
            historyListAdapter = new HistoryListAdapter(this, presenter, databaseAdapter);
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, final int position) {
        if (viewHolder instanceof HistoryListAdapter.SongViewHolder) {
            final View cardForeground = viewHolder.itemView.findViewById(R.id.card_foreground);
            final View cardBackground = viewHolder.itemView.findViewById(R.id.card_background);
            cardForeground.setVisibility(View.GONE);
            cardBackground.setVisibility(View.GONE);

            // showing snack bar with Undo option
            Snackbar snackbar = Snackbar.make(fragmentLayout, R.string.history_deleted, Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.undo, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cardForeground.setVisibility(View.VISIBLE);
                    historyListAdapter.notifyItemChanged(position);
                    cardForeground.setVisibility(View.VISIBLE);
                }
            });
            snackbar.setActionTextColor(getResources().getColor(R.color.spotifyGreen));
            snackbar.show();

            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    super.onDismissed(transientBottomBar, event);
                    if (!cardForeground.isShown()) {
                        historyListAdapter.notifyItemRemoved(position);
                        historyListAdapter.deleteSongFromDb(position);
                    }
                }
            });
        }
    }

    public void saveSong(SongInfo songInfo) {
        if (historyListAdapter != null) {
            historyListAdapter.saveSongToDb(songInfo);
        }
    }
}
