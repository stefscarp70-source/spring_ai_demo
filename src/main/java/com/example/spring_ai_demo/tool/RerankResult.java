package com.example.spring_ai_demo.tool;

import java.util.List;

public record RerankResult(
        List<RerankedDocument> documentIds
) {
}
