package com.example.music.vk_api.screens;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
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
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.methods.VKApiUsers;
import com.vk.sdk.api.model.VKApiUser;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.util.VKUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.prefs.Preferences;

public class MainActivity extends AppCompatActivity implements BasicStudentsInfo{


    boolean isIdRGot = true;
    private ListView namesListView;
    private Button buttonCalculate, buttonToVK, addNameButton;
    private Context context;
    private int[] result = new int[0];
    private final String STUDENTS_NAMES = "STUDENT_NAMES";
    private final String STUDENTS_IDES = "STUDENTS_IDES";
    private final String STUDENTS_VK_IDES = "STUDENTS_VK_IDES";
    private String message;
    private SharedPreferences sPref;

    private String[] scope = new String[]{
            VKScope.PAGES,
            VKScope.GROUPS,
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
        Set<String> vkIdes = null;
        names = sPref.getStringSet(STUDENTS_NAMES, names);
        ides = sPref.getStringSet(STUDENTS_IDES, ides);
        vkIdes = sPref.getStringSet(STUDENTS_VK_IDES, vkIdes);

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
        Students.setVkIdes(vkIdes);
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
                                MessagesSender sender = new MessagesSender();
                                sender.execute();
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

        if (result.length <= 1){
            Toast.makeText(context, "Do Rand", Toast.LENGTH_LONG).show();
            return;
        }

        Set<String> vkIdes = Students.getVkIdes();

        if (vkIdes == null) {
            isIdRGot = false;
            initVkIdes();
        } else if (vkIdes.size() < 27){
            isIdRGot = false;
            initVkIdes();
        }



        while (!isIdRGot){
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        vkIdes = Students.getVkIdes();

        for (String vkId : vkIdes) {
            VKRequest request = new VKRequest(
                    "messages.send",
                    VKParameters.from(
                            VKApiConst.USER_ID,
                            Integer.parseInt(vkId),
                            VKApiConst.MESSAGE, message
                    )
            );

            request.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);
                }
            });

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private void initVkIdes(){
        Set<String> ides = Students.getIdes();
        final HashSet<String> vkIdesTemp = new HashSet<>();
        SharedPreferences.Editor editor = sPref.edit();
        Iterator<String> iterator = ides.iterator();
        for (int i = 0; i < ides.size(); i++) {
            VKRequest friendsRequest = new VKRequest(
                    "search.getHints",
                    VKParameters.from(
                            VKApiConst.Q,
                            iterator.next()));

            friendsRequest.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                    super.attemptFailed(request, attemptNumber, totalAttempts);
                    Toast.makeText(context, "attemptFailed "+ attemptNumber, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onError(VKError error) {
                    super.onError(error);
                    Toast.makeText(context, "Error "+ error.toString(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
                    super.onProgress(progressType, bytesLoaded, bytesTotal);
                }

                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);
                    int userId = 0;

                    JSONObject object = response.json;
                    try {
                        JSONArray responseArray = object.getJSONArray("response");
                        JSONObject userInfo = responseArray.getJSONObject(0);
                        JSONObject profile = userInfo.getJSONObject("profile");
                        String id = profile.getString("id");
                        userId = Integer.parseInt(id);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (userId != 0)
                        vkIdesTemp.add("" + userId);

                    if(vkIdesTemp.size() == 27)
                        isIdRGot = true;
                }
            });

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        Students.setVkIdes(vkIdesTemp);
        editor.putStringSet(STUDENTS_VK_IDES, vkIdesTemp);
        editor.commit();
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
             for (int i = 0; i < result.length; i++) {
                 out[i] = names[i] + " - " + result[i] + "th";
                 String tempMessage = message;
                 message = tempMessage + out[i] + '\n';
             }
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


    private class MessagesSender extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            sendMessages();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Toast.makeText(context, "Sent", Toast.LENGTH_LONG).show();
        }
    }
}
