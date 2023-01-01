package de.teddy.minesweeper.util;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.UUID;

public class UUIDConverter {

    public static UUID getPlayerUUID(String playerName) {
        try{
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet("https://api.mojang.com/users/profiles/minecraft/" + playerName);
            HttpResponse response = httpClient.execute(request);

            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 200) {
                String str = EntityUtils.toString(response.getEntity());

                int startIndex = str.indexOf("\"id\":\"") + 6;
                int endIndex = str.indexOf("\"", startIndex);
                String uuidString = str.substring(startIndex, endIndex);

                return UUID.fromString(uuidString.substring(0, 8) + "-" +
                                               uuidString.substring(8, 12) + "-" +
                                               uuidString.substring(12, 16) + "-" +
                                               uuidString.substring(16, 20) + "-" +
                                               uuidString.substring(20));
            } else {
                return null;
            }
        }catch(IOException e){
            return null;
        }

    }

}
