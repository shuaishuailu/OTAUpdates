package com.ota.updates.fragments;

/*
 * Copyright (C) 2015 Matt Booth.
 *
 * Licensed under the Attribution-NonCommercial-ShareAlike 4.0 International 
 * (the "License") you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://creativecommons.org/licenses/by-nc-sa/4.0/legalcode
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.ota.updates.R;
import com.ota.updates.db.helpers.UploadSQLiteHelper;
import com.ota.updates.db.helpers.VersionSQLiteHelper;
import com.ota.updates.items.UploadItem;
import com.ota.updates.items.VersionItem;
import com.ota.updates.utils.Constants;
import com.ota.updates.utils.FragmentInteractionListener;
import com.ota.updates.utils.Utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class VersionsFragment extends Fragment implements Constants {
    private FragmentInteractionListener mListener;
    private Context mContext;

    public VersionsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        AppCompatActivity activity = (AppCompatActivity) getActivity();

        mContext = activity;

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_versions, container, false);

       VersionSQLiteHelper versionSQLiteHelper = new VersionSQLiteHelper(mContext);

        ArrayList<VersionItem> listOfVersions = versionSQLiteHelper.getListOfVersions();

        if (!listOfVersions.isEmpty()) {
            RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
            recyclerView.setLayoutManager(new LinearLayoutManager(activity));
            recyclerView.setAdapter(new RecyclerAdapter(listOfVersions));
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            mListener = (FragmentInteractionListener) mContext;
        } catch (ClassCastException e) {
            throw new ClassCastException(mContext.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mListener = null;
    }

    public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

        private ArrayList<VersionItem> mItems;

        RecyclerAdapter(ArrayList<VersionItem> items) {
            mItems = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_list_items, viewGroup, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            // Addon Item at latest position in the list
            final VersionItem item = mItems.get(position);

            // Title
            viewHolder.mTitle.setText(item.getFullName());

            // Filesize
            UploadSQLiteHelper uploadSQLiteHelper = new UploadSQLiteHelper(mContext);
            int fullUploadId = item.getFullUploadId();
            UploadItem uploadItem = uploadSQLiteHelper.getUpload(fullUploadId);
            int size = uploadItem.getSize();
            String formattedSize = Utils.formatDataFromBytes(size);
            viewHolder.mFilesize.setText(formattedSize);

            // Date
            String updatedOnStr = getResources().getString(R.string.updated_on);
            String date = item.getPublishedAt();

            Locale locale = Locale.getDefault();
            DateFormat fromDate = new SimpleDateFormat("yyyy-MM-dd", locale);
            DateFormat toDate = new SimpleDateFormat("dd, MMMM yyyy", locale);

            try {
                date = toDate.format(fromDate.parse(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            viewHolder.mUpdatedOn.setText(updatedOnStr + " " + date);

            viewHolder.mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onOpenFileDownloadRequest(FILE_TYPE_VERSION, item.getId());
                }
            });
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private final TextView mTitle;
            private final TextView mUpdatedOn;
            private final TextView mFilesize;
            private final Button mButton;

            ViewHolder(View view) {
                super(view);
                mTitle = (TextView) view.findViewById(R.id.headline);
                mUpdatedOn = (TextView) view.findViewById(R.id.updated_on);
                mFilesize = (TextView) view.findViewById(R.id.size);
                mButton = (Button) view.findViewById(R.id.open);
            }
        }

    }
}