package com.mcherm.versionedserialization.objects;

import java.util.List;

/** Adding a "grade" field to RecordV1. */
public record RecordV2a(String name, int score, List<String> tags, String grade) {}
