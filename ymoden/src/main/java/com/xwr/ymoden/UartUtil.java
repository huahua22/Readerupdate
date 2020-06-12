package com.xwr.ymoden;

import com.van.uart.UartManager;

/**
 * Create by xwr on 2020/6/12
 * Describe:
 */
public class UartUtil {
  public static UartManager.BaudRate getBaudRate(int baudrate) {
    UartManager.BaudRate value = null;
    switch (baudrate) {
      case 9600:
        value = UartManager.BaudRate.B9600;
        break;
      case 19200:
        value = UartManager.BaudRate.B19200;
        break;
      case 57600:
        value = UartManager.BaudRate.B57600;
        break;
      case 115200:
        value = UartManager.BaudRate.B115200;
        break;
      case 230400:
        value = UartManager.BaudRate.B230400;
        break;
    }
    return value;
  }



}
