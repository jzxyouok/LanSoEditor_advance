/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lansosdk.videoeditor.utils;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * The Class FileCopyHelper is used to copy files from resources to the
 * application directory and responsible for deleting the files.
 *
 * @see MediaStore_VideoTest
 * @see MediaStore_Images_MediaTest
 * @see MediaStore_Images_ThumbnailsTest
 * 
 * 
 * 举例:把raw文件下的mp3拷贝到外部.
 *  String fileDir = Environment.getExternalStorageDirectory() +
                "/" + getClass().getCanonicalName();
        String fileName = fileDir + "/Test.Mp3";
        writeFile(R.raw.testmp3, fileName);
        
 * private void writeFile(int resid, String path) throws IOException {
        File out = new File(path);
        File dir = out.getParentFile();
        dir.mkdirs();
        FileCopyHelper copier = new FileCopyHelper(mContext);
        copier.copyToExternalStorage(resid, out);
    }
    
 */
public class FileCopyHelper {
    /** The context. */
    private Context mContext;

    /** The files added. */
    private ArrayList<String> mFilesList;

    /**
     * Instantiates a new file copy helper.
     *
     * @param context the context
     */
    public FileCopyHelper(Context context) {
        mContext = context;
        mFilesList = new ArrayList<String>();
    }

    /**
     * Copy the file from the resources with a filename .
     *
     * @param resId the res id
     * @param fileName the file name
     *
     * @return the absolute path of the destination file
     * @throws IOException
     */
    public String copy(int resId, String fileName) throws IOException {
        InputStream source = mContext.getResources().openRawResource(resId);
        OutputStream target = mContext.openFileOutput(fileName, Context.MODE_WORLD_READABLE);
        copyFile(source, target);
        mFilesList.add(fileName);
        return mContext.getFileStreamPath(fileName).getAbsolutePath();
    }

    public void copyToExternalStorage(int resId, File path) throws IOException {
        InputStream source = mContext.getResources().openRawResource(resId);
        OutputStream target = new FileOutputStream(path);
        copyFile(source, target);
    }

    private void copyFile(InputStream source, OutputStream target) throws IOException {
        try {
            byte[] buffer = new byte[1024];
            for (int len = source.read(buffer); len > 0; len = source.read(buffer)) {
                target.write(buffer, 0, len);
            }
        } finally {
            if (source != null) {
                source.close();
            }
            if (target != null) {
                target.close();
            }
        }
    }

    /**
     * Delete all the files copied by the helper.
     */
    public void clear(){
        for (String path : mFilesList) {
            mContext.deleteFile(path);
        }
    }
}
