package com.example.visaapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@SpringBootApplication
public class VisaAppApplication {

    private static String APP_PROP_FILE_NAME = "application.properties";

    public static void main(String[] args) throws IOException {
        SpringApplication.run(VisaAppApplication.class, args);

        boolean found = true;

        URL url = new URL("https://appointment.bmeia.gv.at/HomeWeb/Scheduler");
        HttpURLConnection http = (HttpURLConnection)url.openConnection();
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.setRequestProperty("Content-Type", "application/json");

        String data = "{\"office\": \"islamabad\",\n\"calendarid\":\"21836737\",\n\"personcount\":\"1\"}";

        byte[] out = data.getBytes(StandardCharsets.UTF_8);

        OutputStream stream = http.getOutputStream();
        stream.write(out);

        System.out.println(http.getResponseCode() + " " + http.getResponseMessage());
        BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream()));
        String strCurrentLine;
        while ((strCurrentLine = br.readLine()) != null) {
           // System.out.println(strCurrentLine);
            if(strCurrentLine.contains("For your selection there are unfortunately no appointments available")){
                found = false;
            }
        }

        if(found){
            System.out.println("Appointment found");
            sendEmail();
        }else{
            System.out.println("No Appointments found!");
        }
        http.disconnect();
        //sendEmail();
    }

    private static void sendEmail() throws IOException {
        InputStream inputStream = VisaAppApplication.class.getClassLoader().getResourceAsStream(APP_PROP_FILE_NAME);
        Properties props = new Properties();
        props.load(inputStream);

        final String fromEmail = props.getProperty("from.email");
        final String password = props.getProperty("fromPs");
        final String toEmail = props.getProperty("to.email");

        System.out.println("TLSEmail Start");

        //create Authenticator object to pass in Session.getInstance argument
        Authenticator auth = new Authenticator() {
            //override the getPasswordAuthentication method
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        };
        Session session = Session.getInstance(props, auth);

        EmailUtil.sendEmail(session, toEmail, "ISB Embassy Appointment", "Appointments found for the following dates!");
    }

}
