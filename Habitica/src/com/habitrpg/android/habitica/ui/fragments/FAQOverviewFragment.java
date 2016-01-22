package com.habitrpg.android.habitica.ui.fragments;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.adapter.CustomizationRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.adapter.FAQOverviewRecyclerAdapter;
import com.habitrpg.android.habitica.ui.helpers.MarginDecoration;
import com.magicmicky.habitrpgwrapper.lib.models.Customization;
import com.magicmicky.habitrpgwrapper.lib.models.FAQArticle;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.Preferences;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Where;

import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FAQOverviewFragment extends BaseFragment {
        @Bind(R.id.recyclerView)
        RecyclerView recyclerView;

        FAQOverviewRecyclerAdapter adapter;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            View v = inflater.inflate(R.layout.fragment_recyclerview, container, false);

            ButterKnife.bind(this, v);
            adapter = new FAQOverviewRecyclerAdapter();
            adapter.activity = activity;
            recyclerView.setLayoutManager(new LinearLayoutManager(activity));
            recyclerView.setAdapter(adapter);
            this.loadArticles();

            return v;
        }

        private void loadArticles() {
            if(user == null || adapter == null){
                return;
            }

            List<FAQArticle> articles = new Select()
                    .from(FAQArticle.class).queryList();

            adapter.setArticles(articles);
        }
}