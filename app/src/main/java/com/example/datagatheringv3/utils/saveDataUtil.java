package com.example.datagatheringv3.utils;

import android.content.Context;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public  class saveDataUtil {
    static FileOutputStream fout;
    public static void saveData(String data,Context context,String file) {
        try {
            fout= context.openFileOutput(file, Context.MODE_APPEND);

            OutputStreamWriter osw=new OutputStreamWriter(fout);
            osw.write("END OF RESPONSE"+"\n------------------------------\n"+data);
            osw.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
