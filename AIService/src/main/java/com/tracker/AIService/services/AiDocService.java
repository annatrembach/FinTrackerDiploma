package com.tracker.AIService.services;

import com.tracker.AIService.models.dto.doc.DocRequest;

public interface AiDocService {

    String answer(DocRequest request, String token);
}
