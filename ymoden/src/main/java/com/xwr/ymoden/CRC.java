package com.xwr.ymoden;


public interface CRC {
    int getCRCLength();

    long calcCRC(byte[] block);
}
