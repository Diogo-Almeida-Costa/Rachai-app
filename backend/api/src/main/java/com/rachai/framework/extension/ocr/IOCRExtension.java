package com.rachai.framework.extension.ocr;

public interface IOCRExtension<T> {
    T parseAndValidate(String rawOcrData);
    String getDocumentSchema();
}
