package com.example.chatgpt;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
    EditText promptEditText;
    TextView resultTextView;
    Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        promptEditText = findViewById(R.id.promptEditText);
        resultTextView = findViewById(R.id.resultTextView);
        submitButton = findViewById(R.id.submitButton);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String prompt = promptEditText.getText().toString();
                new OpenAIRequestTask().execute(prompt);
            }
        });
    }

    private class OpenAIRequestTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String prompt = params[0];
            String apiKey = "Token here";
            String apiUrl = "https://api.openai.com/v1/chat/completions";
            String result = "";

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
                conn.setDoOutput(true);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("model", "gpt-3.5-turbo");
                JSONArray messagesArray = new JSONArray();
                JSONObject userMessage = new JSONObject();
                userMessage.put("role", "user");
                userMessage.put("content", prompt);
                messagesArray.put(userMessage);
                jsonObject.put("messages", messagesArray);

                OutputStream os = conn.getOutputStream();
                os.write(jsonObject.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();

                // Check if the request was successful
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line;
                    while ((line = br.readLine()) != null) {
                        result += line + "\n";
                    }
                    br.close();
                } else {
                    // Handle error response
                    Log.e("Error","Error here "+conn.getResponseCode() );
                    result = "Error: " + conn.getResponseMessage();
                }

                // Close the connection
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Error","Error below");
                result = "Error: " + e.getMessage();
            }

            return result;
        }


        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonResponse = new JSONObject(result);
                JSONArray choicesArray = jsonResponse.getJSONArray("choices");
                if (choicesArray.length() > 0) {
                    JSONObject choiceObject = choicesArray.getJSONObject(0);
                    JSONObject messageObject = choiceObject.getJSONObject("message");
                    String messageContent = messageObject.getString("content");
                    resultTextView.setText(messageContent);
                } else {
                    resultTextView.setText("No message found in the response.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                resultTextView.setText("Error parsing response: " + e.getMessage());
            }
        }

    }
}
