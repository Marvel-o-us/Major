package in.vestapp.major;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by shubham on 1/1/17.
 */

public interface ApiService {
    @Multipart
    @POST("/api/caption/")
    Call<Void> uploadImageMySpace(@Part MultipartBody.Part media);
}
