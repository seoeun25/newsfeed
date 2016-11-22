package com.nexr.newsfeed.client;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by seoeun on 11/8/16.
 */
public class PaymentTest {

    private static String AUTHORIZATION = "bearer b58b583f-2bac-4035-a8c8-9a8c56e64085";

    private static String filePrefix = "201611091400_201611091800";

    @Test
    public void testPayment() {
        String fileName = filePrefix + "_az.csv";
        read(fileName);
    }

    public void read(String fileName) {
        FileWriter fileWriter = null;
        BufferedReader reader = null;
        try {
            String outfile = filePrefix + "_result.csv";
            fileWriter = new FileWriter(outfile);
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = reader.readLine();
            reader.readLine(); // header skip
            int i = 0;
            while ((line = reader.readLine()) != null) {
                i++;
                //System.out.println(line);
                String[] lines = line.split(",");
                String num = lines[0];
                String id = lines[1];
                if (i % 2 == 1) {
                    String url = getUrl(id);
                    String result = sendGet(url, AUTHORIZATION);
                    boolean isOK = isOK(result);
                    fileWriter.write(id + "," + isOK);
                    System.out.println(String.format("%s, %s, %s, %s \n%s", i, num, id, isOK, result));
                } else {
                    //empty line
                    fileWriter.write(",");
                }
                fileWriter.write("\n");

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
                fileWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private String getUrl(String id) {
        String url = "https://cms-api.lezhin.com/v2/payments?_=1478588895962&id_approval=" + id;
        return url;
    }

    @Test
    public void test2() {
        String id = "TSTORE0004_20161108160032069390906629073";
        //curl(id);
        try {
            String url = getUrl(id);
            String result = sendGet(url, AUTHORIZATION);
            System.out.println("isOK = " + isOK(result));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isOK(String result) {
        return !result.contains("\"count\":0");
    }

    // HTTP GET request
    private String sendGet(String url, String authorization) throws Exception {

        //String url = "http://www.google.com";

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        //con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Authorization", authorization);

        int responseCode = con.getResponseCode();
        //System.out.println("\nSending 'GET' request to URL : " + url);
        //System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        //System.out.println(response.toString());
        return response.toString();
    }

}
