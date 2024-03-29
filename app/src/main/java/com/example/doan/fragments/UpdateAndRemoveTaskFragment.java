package com.example.doan.fragments;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.doan.IpAddressWifi;
import com.example.doan.MyNotificationPublisher;
import com.example.doan.R;
import com.example.doan.Retrofit2.APIUtils;
import com.example.doan.Retrofit2.DataCilent;
import com.example.doan.Session;
import com.example.doan.Adapter.TaskApdater;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

public class UpdateAndRemoveTaskFragment extends Fragment {
    public ImageButton imageButtonDate, imageButtonTime;
    public Button btnUpdateTask , btnRemoveTask;
    public TextView textViewDate , textviewTime;
    public EditText editTextName ,editTextDes;
    String timeTonotify;
    Session session;
    int id;
    public void AnhXa(View view){
        imageButtonDate = (ImageButton) view.findViewById(R.id.imageButtonDate);
        imageButtonTime = (ImageButton) view.findViewById(R.id.imageButtonTime);
        btnUpdateTask = (Button) view.findViewById(R.id.buttonUpdateTask);
        btnRemoveTask = (Button) view.findViewById(R.id.buttonRemoveTask);
        editTextDes = (EditText) view.findViewById(R.id.edittextDes);
        editTextName = (EditText) view.findViewById(R.id.editTextName);
        textviewTime = (TextView) view.findViewById(R.id.textviewTime);
        textViewDate = (TextView) view.findViewById(R.id.textViewContentDate);
        session = new Session(getActivity());
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update_and_remove_task, container, false);
        AnhXa(view);
        Bundle bundle = getArguments();
        String title =  bundle.getString("title");
        String time = bundle.getString("time");
        String date = bundle.getString("date");
        String content = bundle.getString("content");
        String user_id = bundle.getString("nameLogin");
        id = Integer.valueOf(bundle.getString("id"));
        editTextName.setText(title);
        editTextDes.setText(content);
        textviewTime.setText(time);
        textViewDate.setText(date);
        imageButtonTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectTime();
            }
        });
        imageButtonDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDate();
            }
        });
        btnUpdateTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editTextName.getText().toString().trim();
                String date1 = textViewDate.getText().toString().trim();
                String time1 = textviewTime.getText().toString().trim();
                String content1 = editTextDes.getText().toString().trim();
                UpdateTask(id,name,date1,time1,content1,user_id);
            }
        });
        btnRemoveTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeTask(id);
            }
        });
        return view;
    }
    public void selectTime() {
        //this method performs the time picker task
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                timeTonotify = i + ":" + i1;
                //temp variable to store the time to set alarm
                textviewTime.setText(FormatTime(i, i1));
                //sets the button text as selected time
            }
        }, hour, minute, false);
        timePickerDialog.show();
    }
    public String FormatTime(int hour, int minute) {
        //this method converts the time into 12hr format and assigns am or pm
        String time;
        time = "";
        String formattedMinute;
        if (minute / 10 == 0) {
            formattedMinute = "0" + minute;
        } else {
            formattedMinute = "" + minute;
        }
        if (hour == 0) {
            time = "12" + ":" + formattedMinute + " AM";
        } else if (hour < 12) {
            time = hour + ":" + formattedMinute + " AM";
        } else if (hour == 12) {
            time = "12" + ":" + formattedMinute + " PM";
        } else {
            int temp = hour - 12;
            time = temp + ":" + formattedMinute + " PM";
        }
        return time;
    }
    public void selectDate() {
        //this method performs the date picker task
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                textViewDate.setText(day + "-" + (month + 1) + "-" + year);
                //sets the selected date as test for button
            }
        }, year, month, day);
        datePickerDialog.show();
    }

    private void setAlarm(String text, String date, String time) {
        AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        //chỉ định đối tượng quản lý báo động để đặt báo thức
        Intent intent = new Intent(getActivity(), MyNotificationPublisher.class);
        intent.putExtra("event", text);
        //gửi dữ liệu đến lớp báo động để tạo kênh và thông báo
        intent.putExtra("time", date);
        intent.putExtra("date", time);
        intent.putExtra("content", editTextDes.getText().toString().trim());
        intent.putExtra("id", id);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), id, intent, PendingIntent.FLAG_ONE_SHOT);
        String dateandtime = date + " " + timeTonotify;
        DateFormat formatter = new SimpleDateFormat("d-M-yyyy hh:mm");
        try {
            Date date1 = formatter.parse(dateandtime);
            Log.d("Date",date1.toString());
            am.set(AlarmManager.RTC_WAKEUP, date1.getTime(), pendingIntent);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        ListFragment lfm = new ListFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.constraint,lfm);
        TaskApdater taskApdater = ListFragment.taskApdater;
        taskApdater.notifyDataSetChanged();
        transaction.commit();
        //điều hướng từ việc thêm hoạt động nhắc nhở thành hoạt động
    }

    private void UpdateTask(int id , String title , String date , String time , String content , String user_id){
        DataCilent dataCilent = APIUtils.getData();
        if (!(title.isEmpty() || date.isEmpty() || time.isEmpty() || content.isEmpty())){
            Call<String> call = dataCilent.updateTask(String.valueOf(id).trim(),title,content,time,date,user_id);
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                    if (response != null ){
                        String result = response.body();
                        if(result.equals("success")){
                            Toast.makeText(getActivity(), "Cập nhập thành công", Toast.LENGTH_SHORT).show();
                            setAlarm(title,date,time);
                            ListFragment listFragment = new ListFragment();
                            TaskApdater taskApdater = listFragment.taskApdater;
                            taskApdater.notifyDataSetChanged();
                            FragmentTransaction fm = getActivity().getSupportFragmentManager().beginTransaction();
                            fm.replace(R.id.constraint,listFragment,"fragment");
                            fm.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
                            fm.commit();
                        } else {
                            Toast.makeText(getActivity(), "Cập nhập thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.d("Error", t.getMessage());
                }
            });
        } else {
            Toast.makeText(getActivity(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
        }
    }
    private void removeTask(int id){
        DataCilent dataCilent = APIUtils.getData();
            Call<String> call = dataCilent.removeTask(id);
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                    if (response != null ){
                        String result = response.body();
                        if(result.equals("success")){
                            Toast.makeText(getActivity(), "Xóa thành công", Toast.LENGTH_SHORT).show();
                            ListFragment listFragment = new ListFragment();
                            TaskApdater taskApdater = listFragment.taskApdater;
                            taskApdater.notifyDataSetChanged();
                            FragmentTransaction fm = getActivity().getSupportFragmentManager().beginTransaction();
                            fm.replace(R.id.constraint,listFragment,"fragment");
                            fm.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
                            fm.commit();
                        } else {
                            Toast.makeText(getActivity(), "Xóa thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.d("Error", t.getMessage());
                }
            });
    }
}