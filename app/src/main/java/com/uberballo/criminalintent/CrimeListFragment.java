package com.uberballo.criminalintent;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.ProtocolFamily;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CrimeListFragment extends Fragment {
    private RecyclerView mCrimeRecyclerView;
    private CrimeAdapter mAdapter;

    private int mLastUpdatedPosition = -1;

    private abstract class CrimeHolder extends RecyclerView.ViewHolder
                                implements View.OnClickListener{
        private TextView mTitleTextView;
        private TextView mDateTextView;
        private ImageView mSolvedImageView;

        Crime mCrime;

        public CrimeHolder(LayoutInflater inflater, ViewGroup parent,int layout){
            super(inflater.inflate(layout, parent, false));
            itemView.setOnClickListener(this);

            mTitleTextView = (TextView) itemView.findViewById(R.id.crime_title);
            mDateTextView = (TextView) itemView.findViewById(R.id.crime_date);
            mSolvedImageView = (ImageView) itemView.findViewById(R.id.crime_solved_Image_View);
        }

        public void bind(Crime crime){
            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());
            Date date = mCrime.getDate();
            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
            mDateTextView.setText(df.format(date));
            mSolvedImageView.setVisibility(crime.isSolved() ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onClick(View v) {
            Intent intent = CrimePagerActivity.newIntent(getActivity(),mCrime.getId());
            mLastUpdatedPosition = this.getAdapterPosition();
            startActivity(intent);
        }
    }

    private class NormalCrimeHolder extends CrimeHolder{

        public NormalCrimeHolder(LayoutInflater inflater, ViewGroup parent){
            super(inflater,parent, R.layout.list_item_crime);
        }
    }

    private class PoliceCrimeHolder extends CrimeHolder{

        private Button mPoliceButton;

        public PoliceCrimeHolder(LayoutInflater inflater, ViewGroup parent){
            super(inflater,parent, R.layout.list_item_crime_police);
        }

        @Override
        public void bind(Crime crime){
            super.bind(crime);

            mPoliceButton = (Button) itemView.findViewById(R.id.report_to_police_button);
            mPoliceButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    Toast.makeText(getActivity(),"Police contacted for "+mCrime.getTitle(), Toast.LENGTH_SHORT)
                    .show();
                }
            });
        }

    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> {
        private List<Crime> mCrimes;

        public CrimeAdapter(List<Crime> crimes){
            mCrimes=crimes;
        }

        @Override
        public CrimeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            if (viewType == 1){
                return new PoliceCrimeHolder(layoutInflater,parent);
            }else{
                return new NormalCrimeHolder(layoutInflater,parent);

            }
        }

        @Override
        public void onBindViewHolder(CrimeHolder crimeHolder, int i) {
            Crime crime = mCrimes.get(i);
            crimeHolder.bind(crime);
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        @Override
        public int getItemViewType(int position){
            return mCrimes.get(position).isRequiresPolice() ? 1 : 0;
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        updateUi();
    }

    private void updateUi(){
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();

        if (mAdapter == null) {
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mAdapter);
        } else{
            if (mLastUpdatedPosition>-1){
                mAdapter.notifyItemChanged(mLastUpdatedPosition);
                mLastUpdatedPosition = -1;
            }
            mAdapter.notifyDataSetChanged();

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);

        mCrimeRecyclerView = (RecyclerView) view
                .findViewById(R.id.crime_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mCrimeRecyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration itemDecoration = new DividerItemDecoration(
                mCrimeRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL
        );
        mCrimeRecyclerView.addItemDecoration(itemDecoration);

        updateUi();

        return view;
    }
}
