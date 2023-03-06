package MochiMochiTalk.lib.comms;

import java.io.Serializable;
import java.util.Collection;

/**
 * クラス間通信に用いるインターフェースクラス クラス間通信に際して必要な最低限のAPIを記述
 *
 * @author Ranfa
 * @since 4.0.0
 */
public interface IClassComms extends Serializable {

  /**
   * クラス間通信における送信元を取得します。
   * <p>
   * 通信元でこのメソッドを呼び出した場合、{@link IllegalStateException} がスローされます。
   *
   * @return 送信元クラス
   * @throws IllegalStateException 送信元でこのメソッドを呼び出した場合
   */
  Class<?> getSender() throws IllegalStateException;

  /**
   * クラス間通信における送信元を定義します。
   * <p>
   * クラス間通信では送信先によるインスタンスの使いまわしをしないよう設計するため、すでに送信元が設定されている場合は {@link UnsupportedOperationException}
   * をスローします。
   *
   * @param sender 送信元。すでに設定されている場合は {@link UnsupportedOperationException}がスローされる。
   * @throws UnsupportedOperationException 送信元がすでに設定されている場合
   */
  void setSender(Class<?> sender) throws UnsupportedOperationException;

  Class<?> getSendTo() throws IllegalStateException, UnsupportedOperationException;

  void setSendTo(Class<?> sender) throws IllegalStateException;

  Collection<Class<?>> getRecipients() throws IllegalStateException, UnsupportedOperationException;

  void setRecipients(Collection<Class<?>> recipients) throws IllegalStateException;

  /**
   * 送信元で設定されたメッセージを取得します。
   * <p>
   * メッセージが設定されていない場合は {@link UnsupportedOperationException}が、送信前にこのメソッドを呼び出した場合は
   * {@link IllegalStateException} がスローされます。
   *
   * @return 送信されたメッセージ
   * @throws IllegalStateException         送信前にこのメソッドを呼び出した場合
   * @throws UnsupportedOperationException メッセージが設定されていない場合
   */
  String getMessage() throws IllegalStateException, UnsupportedOperationException;

  /**
   * 送信するメッセージを設定します。
   * <p>
   * 送信先でこのメソッドを呼び出した場合、{@link IllegalStateException} がスローされます。
   *
   * @param message 送信するメッセージ
   * @throws IllegalStateException すでに送信されており、送信先でapp/src/main/java/MochiMochiTalk/lib/comms/IClassComms.java
   */
  void setMessage(String message) throws IllegalStateException;

  CommunicationAction sendMessage() throws IllegalStateException, UnsupportedOperationException;

  CommunicationAction sendMessage(String message)
      throws IllegalStateException, UnsupportedOperationException;

  boolean isClosed();
}
