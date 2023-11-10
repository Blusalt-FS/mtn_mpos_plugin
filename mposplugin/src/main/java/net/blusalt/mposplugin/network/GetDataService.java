package net.blusalt.mposplugin.network;


import net.blusalt.mposplugin.blusaltmpos.pay.BlusaltTerminalInfo;
import net.blusalt.mposplugin.blusaltmpos.pay.TerminalResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface GetDataService {

    @POST(APIConstant.POST_TRANSACTION+"/charge")
    Call<TerminalResponse> postTransactionToMiddleWare(@Body BlusaltTerminalInfo blusaltTerminalInfo);


}
