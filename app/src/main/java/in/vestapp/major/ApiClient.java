package in.vestapp.major;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by shubham on 1/1/17.
 */

public class ApiClient {
    private static ApiService service;

    public static ApiService getService() {

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();


        Retrofit r = new Retrofit.Builder().baseUrl(MainActivity.BASE_URL).addConverterFactory(GsonConverterFactory.create(gson)).build();
        service = r.create(ApiService.class);

        return service;
    }
}
