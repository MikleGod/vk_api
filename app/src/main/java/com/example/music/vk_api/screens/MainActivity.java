package com.example.music.vk_api.screens;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.music.vk_api.CodesInfo;
import com.example.music.vk_api.Entities.BasicStudentsInfo;
import com.example.music.vk_api.Entities.Students;
import com.example.music.vk_api.R;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;
import com.vk.sdk.util.VKUtil;

import java.util.Arrays;
import java.util.Set;
import java.util.prefs.Preferences;

public class MainActivity extends AppCompatActivity implements BasicStudentsInfo{


    private ListView namesListView;
    private Button buttonCalculate, buttonToVK, addNameButton;
    private Context context;
    private int[] result = new int[0];
    private final String STUDENTS_NAMES = "STUDENT_NAMES";
    private final String STUDENTS_IDES = "STUDENTS_IDES";
    private SharedPreferences sPref;

    private String[] scope = new String[]{
            VKScope.MESSAGES,
            VKScope.FRIENDS
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        getStudents();
        initUI();
        VKSdk.login(this, scope);
    }

    private void getStudents() {
        sPref = getPreferences(MODE_PRIVATE);
        Set<String> names = null;
        Set<String> ides = null;
        names = sPref.getStringSet(STUDENTS_NAMES, names);
        ides = sPref.getStringSet(STUDENTS_IDES, ides);

        if (names == null || ides == null){
            SharedPreferences.Editor editor = sPref.edit();
            editor.putStringSet(STUDENTS_NAMES, namesImpl);
            editor.putStringSet(STUDENTS_IDES, idesImpl);
            editor.commit();
            names = namesImpl;
            ides = idesImpl;
        }

        Students.setNames(names);
        Students.setIdes(ides);
    }

    private void initUI(){
        context = this;
        addNameButton = findViewById(R.id.addButton);
        addNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = LayoutInflater.from(context);
                View nameAndIdInputView = inflater.inflate(R.layout.item_dialog_add_name, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(nameAndIdInputView);

                final EditText
                        nameInput = nameAndIdInputView.findViewById(R.id.typeNameEditText),
                        idInput = nameAndIdInputView.findViewById(R.id.typeIdEditText);

                builder.setCancelable(false)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String name = nameInput.getText().toString();
                                String id = idInput.getText().toString();
                                Set<String> names = Students.getNames();
                                Set<String> ides = Students.getIdes();
                                if (!name.isEmpty() && !id.isEmpty()){
                                    names.add(name);
                                    Students.setNames(names);
                                    ides.add(name);
                                    Students.setIdes(ides);
                                    initListView(CodesInfo.RENDER_ONLY);
                                } else
                                    Toast.makeText(context, "Fields mast be fulled",Toast.LENGTH_LONG).show();

                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        namesListView = findViewById(R.id.namesListView);
        buttonToVK = findViewById(R.id.button);
        buttonToVK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setCancelable(false)
                        .setTitle("Sending")
                        .setMessage("R U Sure?")
                        .setPositiveButton("Yep", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                sendMessages();
                            }
                        })
                        .setNegativeButton("Nope", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        buttonCalculate = findViewById(R.id.calculateButton);
        buttonCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calculate();
            }
        });
        initListView(CodesInfo.RENDER_ONLY);
    }

    private void sendMessages() {

    }

    private void calculate(){

        int numberOfStudents = Students.getNames().size();

        result = new int[numberOfStudents];

        for (int i = 0; i<result.length; i++) {
            int temp =(int) (Math.random()*numberOfStudents + 1);
            while (isRepeated(temp, i)){
                temp = (int) (Math.random()*numberOfStudents + 1);
            }
            result[i] = temp;
        }

        initListView(CodesInfo.RENDER_PLUS_RANDOM);
    }


    private void initListView(int code){
        String[] names = Students.getNames().toArray(new String[Students.getNames().size()]);
         if(code == CodesInfo.RENDER_ONLY) {
                ArrayAdapter<String> adapter0 = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        names);
                namesListView.setAdapter(adapter0);
         }else if( code == CodesInfo.RENDER_PLUS_RANDOM) {
             String[] out = new String[result.length];
             for (int i = 0; i < result.length; i++)
                 out[i] = names[i] + " - " + result[i] + "th";
             ArrayAdapter<String> adapter1 = new ArrayAdapter<>(
                     this,
                     android.R.layout.simple_list_item_1,
                     out);
             namesListView.setAdapter(adapter1);
         }

    }

    private boolean isRepeated(int temp, int sh){

        for (int i=0; i<sh; i++)
            if (result[i]== temp)
                return true;

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                // Пользователь успешно авторизовался
            }
            @Override
            public void onError(VKError error) {
                // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
            }
        })) super.onActivityResult(requestCode, resultCode, data);

    }
}
