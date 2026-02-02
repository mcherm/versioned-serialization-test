package com.mcherm.versionedserialization.objects;

import com.mcherm.versionedserialization.objects.contents.Shape;

import java.util.List;

/** Adding a "artist" field to PolymorphicV1. */
public class PolymorphicV2a {
    public String label;
    public List<Shape> shapes;
    public String artist;
}
