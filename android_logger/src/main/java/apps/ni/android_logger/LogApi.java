package apps.ni.android_logger;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface LogApi {

    @POST("{apiEndPoint}")
    @FormUrlEncoded
    Call<ResponseBody> postLog(@Path(value = "apiEndPoint", encoded = true) String apiEndPoint,
                               @Field("sessionId") String sessionId,
                               @Field("logMessage") String logMessage);

}
