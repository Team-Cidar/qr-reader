package com.example.menu_qr_reader.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.menu_qr_reader.R;
import com.journeyapps.barcodescanner.BarcodeView;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ScanBySchoolActivity extends AppCompatActivity {
    private ActivityResultLauncher<ScanOptions> barcodeLauncher;
    private JSONObject response;
    private ScanOptions setScanOption(){
        ScanOptions scanOptions = new ScanOptions();
        scanOptions.setCameraId(0);  // use behind camera, 1 -> front camera
        scanOptions.setOrientationLocked(true);  // allow rotating display
        scanOptions.setPrompt("");
        scanOptions.setBeepEnabled(true);  // beep sound if scan
        scanOptions.setBarcodeImageEnabled(false);  // use scanned image
        scanOptions.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        return scanOptions;
    }

    private void succeedScanning(ScanIntentResult result) {
        new Thread(() -> {
            try {
                this.response = this.sendApiRequest(result.getContents());
            } catch (IOException | JSONException e) {
                throw new RuntimeException(e);
            }
        }).start();

        while(this.response == null) {}  // wait for the new thread's result

        boolean isValid;  // parse json data
        String ticketName, ownerName;
        try {
            isValid = this.response.getBoolean("is_valid");
            ticketName = this.response.getString("ticket_type");
            ownerName = this.response.getString("owner_name"); // 혹시 몰라서 일단 추가해 둠
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // after request
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (isValid) builder = builder.setTitle("Qr인증 성공").setMessage(String.format("[%s] 식권 입니다.", ticketName));  // 학생 메뉴, 추가 메뉴, 교직원 메뉴
        else builder.setTitle("실패").setMessage("유효하지 않은 식권입니다.");

        builder.setCancelable(false)
                .setPositiveButton("다음 스캔하기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // prepare next scan
                        barcodeLauncher.launch(setScanOption());
                    }
                }).create().show();
    }

    private JSONObject sendApiRequest(String ticketId) throws IOException, JSONException {
        String urlStr = getResources().getString(R.string.api_url) + ticketId;
        URL url = new URL(urlStr);

        // api request
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoInput(true);  // to get result

        // get result
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;

        while ((line = reader.readLine()) != null) {  // while can read line
            stringBuilder.append(line);
        }

        JSONObject result = new JSONObject(stringBuilder.toString());
        System.out.println(result);

        reader.close();
        return result;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.avtivity_scan_by_school);

        barcodeLauncher = registerForActivityResult(new ScanContract(),
                result -> {
                    if(result.getContents() != null) {
                        succeedScanning(result);
                        return;
                    }
                    this.finish();
//                    Toast.makeText(this, "QR코드를 다시 인식해주세요", Toast.LENGTH_LONG).show();
//                    barcodeLauncher.launch(setScanOption());
                }
        );

        barcodeLauncher.launch(setScanOption());
    }
}
