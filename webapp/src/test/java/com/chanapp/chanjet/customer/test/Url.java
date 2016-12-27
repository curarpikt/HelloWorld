package com.chanapp.chanjet.customer.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

public class Url {
    String newUrl;
    String origUrl;

    public Url(String newUrl, String origUrl) {
        this.newUrl = newUrl;
        this.origUrl = origUrl;
    }

    public Url append(String str) {
        return new Url(this.newUrl + str, this.origUrl + str);
    }

    public static void writeFile(File file, String str) {
        String oldStr = readFile(file);
        if (oldStr.equals(str)) {
            return;
        }
        OutputStreamWriter out = null;
        try {
            out = new OutputStreamWriter(new FileOutputStream(file.getAbsoluteFile()), "UTF-8");
            out.write(str + "\n");
            out.flush();
            System.out.println(file.getAbsolutePath());
        } catch (IOException e) {
            // CspLogger.log(e.getMessage());
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // CspLogger.log(e.getMessage());
                }
            }
        }

        try {
            Thread.sleep(20);// 暂停一下
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }

    public static String readFile(File file) {
        if (file.exists()) {
            StringBuffer sb = new StringBuffer();
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(file.getAbsoluteFile()), "UTF-8"));
                String line = null;
                boolean first = true;
                while ((line = br.readLine()) != null) {
                    if (!first) {
                        sb.append("\n");
                    } else {
                        first = false;
                    }
                    sb.append(line);
                }
            } catch (Exception e) {

            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {

                    }
                }
            }
            return sb.toString();
        }

        return "";
    }

    public static void createJavaFile(String arg) {
        String path = Url.class.getResource("").getPath();
        // /D:/csp/chanjet_customer/customer-service/webapp/target/test-classes/
        if (File.separator.equals("\\") && path.startsWith("/")) {
            path = path.substring(1);
        }
        String pkg = Url.class.getPackage().getName();
        String pkgPath = pkg.replace('.', '/');
        path = path.replace("/target/test-classes/" + pkgPath, "");

        File jsonFile = new File(path + "src/main/resources/" + arg + "Url.json");
        if (!jsonFile.exists() || !jsonFile.isFile()) {
            return;
        }

        String jsonString = readFile(jsonFile);
        LinkedHashMap<String, Map<String, String>> map = JSON.parseObject(jsonString,
                new TypeReference<LinkedHashMap<String, Map<String, String>>>() {
                });

        StringBuilder strbuilder = new StringBuilder();
        strbuilder.append("/********************************************************************************\n"
                + "* * 不要手动修改这个文件，由 {@link " + pkg + ".Url} 生成  *\n" + "* * \t\t\t\t\t"
                + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS").format(new Date()) + " \t\t\t\t\t\t\t\t *\n"
                + "********************************************************************************/\n");
        strbuilder.append("package " + pkg + ";\n\n");
        strbuilder.append("public interface " + (arg.equals("rest") ? "Rest" : "Web") + " {\n");
        for (Map.Entry<String, Map<String, String>> entry : map.entrySet()) {
            strbuilder.append("\tUrl " + entry.getKey().replace('.', '$') + " = new Url(\""
                    + entry.getValue().get("newUrl") + "\", \"" + entry.getValue().get("origUrl") + "\");\n");
        }
        strbuilder.append("}");

        writeFile(new File(path + "src/test/java/" + pkgPath + "/" + (arg.equals("rest") ? "Rest" : "Web") + ".java"),
                strbuilder.toString());
    }

    public static void main(String[] args) {
        createJavaFile("rest");
        createJavaFile("web");

    }
}
