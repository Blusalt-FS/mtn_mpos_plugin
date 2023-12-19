package net.blusalt.mposplugin.network;


import net.blusalt.mposplugin.blusaltmpos.pay.BlusaltTerminalInfo;
import net.blusalt.mposplugin.blusaltmpos.pay.TerminalResponse;
import net.blusalt.mposplugin.processor.processor_blusalt.BlusaltTerminalInfoProcessor;
import net.blusalt.mposplugin.processor.processor_blusalt.KeyDownloadRequest;
import net.blusalt.mposplugin.processor.processor_blusalt.KeyDownloadResponse;
import net.blusalt.mposplugin.processor.processor_blusalt.param.ParamDownloadResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface GetDataService {

    @POST(APIConstant.POST_TRANSACTION+"/charge")
    Call<TerminalResponse> postTransactionToMiddleWare(@Body BlusaltTerminalInfo blusaltTerminalInfo);

    @POST("/pos/key-exchange")
    Call<BaseData<KeyDownloadResponse>> downloadKeyExchangeFromProcessor(@Body KeyDownloadRequest keyDownloadRequest);

    //    @POST("/processor/v1/transaction")
    @POST(APIConstant.POST_TRANSACTION + "/charge")
    Call<TerminalResponse> postTransactionToProcessor(@Body BlusaltTerminalInfoProcessor blusaltTerminalInfoProcessor);

    @GET("api/v1/devices/parameter-download/{serialNumber}")
    Call<BaseData<ParamDownloadResponse>> downloadTerminalParam(@Path("serialNumber") String serialNumber);
}
