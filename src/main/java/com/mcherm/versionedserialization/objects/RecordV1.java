package com.mcherm.versionedserialization.objects;

import java.util.List;

/** A Java record used as a serializable object. */
public record RecordV1(String name, int score, List<String> tags) {}
