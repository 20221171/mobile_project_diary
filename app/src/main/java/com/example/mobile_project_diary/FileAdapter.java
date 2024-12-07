package com.example.mobile_project_diary;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    private final List<Uri> fileList;

    // Constructor
    public FileAdapter(List<Uri> fileList) {
        this.fileList = new ArrayList<>(fileList);
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        Uri fileUri = fileList.get(position);

        // 파일 이름 가져오기
        String fileName = getFileName(holder.itemView.getContext().getContentResolver(), fileUri);
        holder.tvFileName.setText(fileName);

        // 이미지 로드
        loadImageFromUri(holder, fileUri);
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    // 파일 이름 가져오기
    private String getFileName(ContentResolver contentResolver, Uri fileUri) {
        Cursor cursor = contentResolver.query(fileUri, null, null, null, null);
        String fileName = "Unknown File";

        if (cursor != null) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (cursor.moveToFirst() && nameIndex != -1) {
                fileName = cursor.getString(nameIndex);
            }
            cursor.close();
        }
        return fileName;
    }

    // Uri에서 이미지 로드
    private void loadImageFromUri(FileViewHolder holder, Uri fileUri) {
        try {
            InputStream inputStream = holder.itemView.getContext().getContentResolver().openInputStream(fileUri);
            if (inputStream != null) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2; // 이미지 크기를 줄임
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                holder.ivFileIcon.setImageBitmap(bitmap);
                inputStream.close();
            } else {
                holder.ivFileIcon.setImageResource(R.drawable.ic_launcher_background); // 기본 이미지
            }
        } catch (Exception e) {
            e.printStackTrace();
            holder.ivFileIcon.setImageResource(R.drawable.ic_launcher_background); // 기본 이미지
        }
    }

    // 데이터 변경 시 RecyclerView 업데이트
    public void updateFileList(List<Uri> newFileList) {
        fileList.clear();
        fileList.addAll(newFileList);
        notifyDataSetChanged();
    }

    // ViewHolder
    static class FileViewHolder extends RecyclerView.ViewHolder {
        TextView tvFileName;
        ImageView ivFileIcon;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFileName = itemView.findViewById(R.id.tv_file_name);
            ivFileIcon = itemView.findViewById(R.id.iv_file_icon);
        }
    }
}
