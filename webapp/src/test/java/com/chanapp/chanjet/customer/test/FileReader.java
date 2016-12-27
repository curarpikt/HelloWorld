package com.chanapp.chanjet.customer.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

public class FileReader {
    public static String read(String fileName) {
        InputStream fis = null;
        try {
        	String path = FileReader.class.getClassLoader().getResource(fileName).getPath();
        	System.out.println("path:"+path);
            fis = FileReader.class.getClassLoader().getResourceAsStream(fileName);
            
            return readStringFromStream(new LineNumberReader(new InputStreamReader(fis)));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return "";
    }

    private static String readStringFromStream(LineNumberReader reader) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }
        reader.close();
        return stringBuilder.toString();
    }
}
