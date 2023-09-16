package com.dspread.blusalt.network;


import com.dspread.blusalt.blusaltmpos.pay.BlusaltTerminalInfo;
import com.dspread.blusalt.blusaltmpos.pay.TerminalResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface GetDataService {

    @POST(APIConstant.POST_TRANSACTION+"/charge")
    Call<TerminalResponse> postTransactionToMiddleWare(@Body BlusaltTerminalInfo blusaltTerminalInfo);


}
