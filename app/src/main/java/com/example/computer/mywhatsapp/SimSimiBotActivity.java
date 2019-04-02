package com.example.computer.mywhatsapp;

import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.computer.mywhatsapp.Adapter.CustomAdapter;
import com.example.computer.mywhatsapp.Helper.HttpDetailHandler;
import com.example.computer.mywhatsapp.Model.ChatModel;
import com.example.computer.mywhatsapp.Model.SimsimiModel;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class SimSimiBotActivity extends AppCompatActivity {
ListView listView;
EditText editText;
List<ChatModel> list_chatmodel = new ArrayList<>();
FloatingActionButton btn_send_message;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sim_simi_bot);
        listView = (ListView)findViewById(R.id.list_of_messages);
        editText = (EditText)findViewById(R.id.user_messages);
        btn_send_message = (FloatingActionButton) findViewById(R.id.fab);
        btn_send_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getText().toString();
                ChatModel model= new ChatModel(text,true);
                list_chatmodel.add(model);
                new SimsimiAPI().execute(list_chatmodel);
                editText.setText("");

            }
        });
    }

    private class SimsimiAPI extends AsyncTask<List<ChatModel>,Void,String> {
        String stream = null;
        List<ChatModel> models;
        String text = editText.getText().toString();
        @Override
        protected String doInBackground(List<ChatModel>... lists) {
             String url = String.format("http://sandbox.api.simsimi.com/request.p?key=%s&lc=en&ft=1.0&text=%s",getString(R.string.simsimi_api),text);
             models = lists[0];
            HttpDetailHandler httpDataHandler = new HttpDetailHandler();
            stream = httpDataHandler.GetHttpData(url);
            return stream;
        }

        @Override
        protected void onPostExecute(String s) {
            Gson gson = new Gson();
            SimsimiModel response = gson.fromJson(s,SimsimiModel.class);
            ChatModel chatModel = new ChatModel(response.getResponse(),false);
            models.add(chatModel);
            CustomAdapter adapter = new CustomAdapter(models,getApplicationContext());
            listView.setAdapter(adapter);
        }
    }
}
