package com.example.mobile_project_diary;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DiaryDetailActivity extends AppCompatActivity {

    private TextView tvDate;
    private EditText tvTitle, etContent;
    private RatingBar rbMood;
    private ImageView ivImage;
    private Button btnSave;

    private long diaryId;
    private String diaryImageUrl;

    private DiaryDatabaseManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.read_diary);

        // 뷰 초기화
        tvTitle = findViewById(R.id.et_diary_title1);
        tvDate = findViewById(R.id.tv_diary_date1);
        etContent = findViewById(R.id.et_diary_content1);
        rbMood = findViewById(R.id.rb_mood1);
        btnSave = findViewById(R.id.btn_save_diary1);

        // 데이터베이스 관리 객체 초기화
        dbManager = new DiaryDatabaseManager(this);

        // 인텐트에서 데이터 받기
        Intent intent = getIntent();
        diaryId = intent.getLongExtra("diary_id", -1);

        // 일기 데이터 로드
        loadDiaryDetails();

        // 수정 버튼 클릭 시 일기 내용 수정
        btnSave.setOnClickListener(v -> {
            String updatedContent = etContent.getText().toString();
            int moodRating = (int) rbMood.getRating();

            // 수정된 내용 저장
            updateDiaryContent(updatedContent, moodRating);

            // 수정 후 액티비티 종료
            finish();
        });
    }

    // 일기 내용 로드
    private void loadDiaryDetails() {
        dbManager.open();

        // 일기 아이디에 해당하는 일기 가져오기
        DiaryItem diaryItem = dbManager.getDiaryById(diaryId);

        if (diaryItem != null) {
            tvTitle.setText(diaryItem.getTitle());
            tvDate.setText(diaryItem.getTimestamp());
            etContent.setText(diaryItem.getContent());
            rbMood.setRating(Float.parseFloat(diaryItem.getMood()));

            // 이미지가 있을 경우 이미지 로드
            if (diaryItem.getImageUrl() != null) {
                diaryImageUrl = diaryItem.getImageUrl();
                loadImage(diaryImageUrl);
            }
        }

        dbManager.close();
    }

    // 이미지 로드 (파일 시스템에서)
    private void loadImage(String imageUrl) {
        try {
            File file = new File(imageUrl);
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
                ivImage.setImageBitmap(bitmap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 일기 내용 수정
    private void updateDiaryContent(String content, int moodRating) {
        dbManager.open();

        // 수정된 내용 저장
        dbManager.updateDiary(diaryId, content, moodRating);

        dbManager.close();
    }
}
