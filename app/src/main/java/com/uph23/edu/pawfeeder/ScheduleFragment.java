package com.uph23.edu.pawfeeder;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.uph23.edu.pawfeeder.adapter.DateAdapter;
import com.uph23.edu.pawfeeder.model.DateItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ScheduleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScheduleFragment extends Fragment implements DateAdapter.OnDateClickListener {

    private RecyclerView rvDates;
    private DateAdapter dateAdapter;
    private List<DateItem> dateList = new ArrayList<>();


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ScheduleFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ScheduleFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ScheduleFragment newInstance(String param1, String param2) {
        ScheduleFragment fragment = new ScheduleFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_schedule, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        rvDates = view.findViewById(R.id.rvDates);

        // Setup RecyclerView
        setupRecyclerView();

        // Generate and display dates automatically
        generateDateCards();
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                getContext(), LinearLayoutManager.HORIZONTAL, false
        );

        rvDates.setLayoutManager(layoutManager);

        // Initialize adapter
        dateAdapter = new DateAdapter(dateList, this);
        rvDates.setAdapter(dateAdapter);
    }

    private void generateDateCards() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());

        dateList.clear();

        // Generate dates for current month
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);

        // Set calendar to first day of current month
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 1; i <= maxDays; i++) {
            calendar.set(Calendar.DAY_OF_MONTH, i);

            boolean isToday = isToday(calendar);

            DateItem dateItem = new DateItem(
                    i,
                    dayFormat.format(calendar.getTime()).toUpperCase(),
                    monthFormat.format(calendar.getTime()).toUpperCase(),
                    isToday,
                    calendar.getTime()
            );

            dateList.add(dateItem);
        }

        dateAdapter.notifyDataSetChanged();
        scrollToToday();
    }

    private boolean isToday(Calendar calendar) {
        Calendar today = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH);
    }

    private void scrollToToday() {
        Calendar today = Calendar.getInstance();
        int todayDate = today.get(Calendar.DAY_OF_MONTH);

        if (todayDate - 1 >= 0 && todayDate - 1 < dateList.size()) {
            rvDates.scrollToPosition(todayDate - 1);
        }
    }

    @Override
    public void onDateClick(DateItem dateItem) {
        if (getContext() != null) {
            Toast.makeText(getContext(),
                    "Selected: " + dateItem.getDayName() + ", " +
                            dateItem.getMonth() + " " + dateItem.getDate(),
                    Toast.LENGTH_SHORT).show();
        }
    }
}