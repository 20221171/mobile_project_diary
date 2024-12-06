package com.example.mobile_project_diary;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NewDiaryActivity extends AppCompatActivity {

    private EditText etDiaryTitle, etDiaryContent;
    private TextView tvDiaryDate;
    private RatingBar rbMood;
    private DiaryDatabaseManager diaryDatabaseManager;
    private List<Uri> attachmentUris = new ArrayList<>();
    private LinearLayout llAttachmentsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_diary);

        etDiaryTitle = findViewById(R.id.et_diary_title);
        etDiaryContent = findViewById(R.id.et_diary_content);
        tvDiaryDate = findViewById(R.id.tv_diary_date);
        rbMood = findViewById(R.id.rb_mood);
        llAttachmentsContainer = findViewById(R.id.ll_attachments_container);

        diaryDatabaseManager = new DiaryDatabaseManager(this);

        // 현재 날짜 자동 입력
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        tvDiaryDate.setText("작성 일자: " + currentDate);

        diaryDatabaseManager.open();

        // 파일 선택 버튼 동작
        Button btnAddAttachment = findViewById(R.id.btn_attach_file);
        btnAddAttachment.setOnClickListener(v -> openFilePicker());

        // 저장 버튼 클릭 시 처리
        findViewById(R.id.btn_save_diary).setOnClickListener(v -> saveDiary());
    }

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    if (result.getData().getClipData() != null) {
                        int count = result.getData().getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri fileUri = result.getData().getClipData().getItemAt(i).getUri();
                            addFileToLayout(fileUri);
                            attachmentUris.add(fileUri);
                        }
                    } else if (result.getData().getData() != null) {
                        Uri fileUri = result.getData().getData();
                        addFileToLayout(fileUri);
                        attachmentUris.add(fileUri);
                    }
                }
            }
    );

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        filePickerLauncher.launch(intent);
    }

    private void addFileToLayout(Uri fileUri) {
        View fileItem = LayoutInflater.from(this).inflate(R.layout.item_file, llAttachmentsContainer, false);
        TextView tvFileName = fileItem.findViewById(R.id.tv_file_name);
        tvFileName.setText(fileUri.getLastPathSegment());
        llAttachmentsContainer.addView(fileItem);
    }

    private void saveDiary() {
        String title = etDiaryTitle.getText().toString().trim();
        String content = etDiaryContent.getText().toString().trim();
        float mood = rbMood.getRating();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "모든 정보를 입력해주세요!", Toast.LENGTH_SHORT).show();
            return;
        }

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        StringBuilder filePaths = new StringBuilder();
        for (Uri uri : attachmentUris) {
            filePaths.append(uri.toString()).append(";");
        }

        long rowId = diaryDatabaseManager.addDiary(title, content, timestamp, filePaths.toString(), mood);

        if (rowId != -1) {
            Toast.makeText(this, "일기가 저장되었습니다.", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "일기 저장 실패. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
        }
    }
}
