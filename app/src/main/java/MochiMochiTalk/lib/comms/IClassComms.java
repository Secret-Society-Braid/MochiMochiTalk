package MochiMochiTalk.lib.comms;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

/**
 * クラス間通信に用いるインターフェースクラス
 * クラス間通信に際して必要な最低限のAPIを記述
 * 
 * @author Ranfa
 * @since 4.0.0
 */
public interface IClassComms extends Serializable {
    
    /**
     * クラス間通信における送信元を定義します。
     * <p>
     * クラス間通信では送信先によるインスタンスの使いまわしをしないよう設計するため、すでに送信元が設定されている場合は {@link UnsupportedOperationException} をスローします。
     * @param sender 送信元。すでに設定されている場合は {@link UnsupportedOperationException}がスローされる。
     */
    void setSender(Class<?> sender);

    Class<?> getSender() throws IllegalStateException;


}
