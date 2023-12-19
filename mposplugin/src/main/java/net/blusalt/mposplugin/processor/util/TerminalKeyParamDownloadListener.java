package net.blusalt.mposplugin.processor.util;

import androidx.annotation.Keep;


@Keep
public interface TerminalKeyParamDownloadListener {
    public void onSuccess(String message);
    public void onFailed(String error);
}
