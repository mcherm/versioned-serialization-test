package com.mcherm.versionedserialization.objects;

import com.mcherm.versionedserialization.objects.contents.Shape;

import java.util.List;

/** A class containing a polymorphic list of shapes. */
public class PolymorphicV1 {
    public String label;
    public List<Shape> shapes;
}
