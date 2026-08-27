package com.gooshare.service;

import com.gooshare.dto.IntentResult;

public interface IntentRecognitionService {
    IntentResult recoginze(String message);
}
