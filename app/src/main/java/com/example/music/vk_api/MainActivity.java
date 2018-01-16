package com.example.music.vk_api;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {


    private ListView listView;
    private EditText editText;
    private Button buttonCalculate;
    private Button buttonToVK;
    private Context context;
    private int[] result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
    }

    private void initUI(){
        context = this;
        listView = (ListView) findViewById(R.id.listViewOut);
        editText = (EditText) findViewById(R.id.input);
        buttonToVK = (Button) findViewById(R.id.button);
        buttonToVK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listView.setAdapter(null);
                buttonToVK.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(context, VKActivity.class);
                intent.putExtra("numbers", result);
                startActivity(intent);
            }
        });
        buttonCalculate = (Button) findViewById(R.id.calculateButton);
        buttonCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calculate();
                buttonToVK.setVisibility(View.VISIBLE);
            }
        });
    }

    private void calculate(){
        int numberOfStudents;
        if (!editText.getText().toString().isEmpty())
            numberOfStudents = Integer.parseInt(editText.getText().toString());
        else
            return;

        result = new int[numberOfStudents];

        for (int i = 0; i<result.length; i++) {
            int temp =(int) (Math.random()*numberOfStudents + 1);
            while (isRepeated(temp, i)){
                temp = (int) (Math.random()*numberOfStudents + 1);
            }
            result[i] = temp;
        }

        initListView();
    }

    private void initListView(){

        String[] names = new String[result.length];
        for (int i =0; i< result.length; i++)
            names[i] = "Number " + (i + 1) + " - " + result[i] + "th";


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);
        listView.setAdapter(adapter);
    }

    private boolean isRepeated(int temp, int sh){

        for (int i=0; i<sh; i++)
            if (result[i]== temp)
                return true;

        return false;
    }
}
