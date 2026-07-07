package com.framework.extension.ocr;

import com.framework.extension.resource.IResource;

public interface IOCRExtension<T extends IResource> {
    T parseAndValidate(String rawOcrData);
    String getDocumentSchema();
}
