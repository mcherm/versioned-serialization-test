package com.mcherm.versionedserialization.objects;

import com.mcherm.versionedserialization.objects.contents.Color;
import com.mcherm.versionedserialization.objects.contents.Lamp;
import com.mcherm.versionedserialization.objects.contents.Widget;

import java.util.List;
import java.util.Optional;

/**
 * A class demonstrating a number of different ways to customize the
 * jackson serialization.
 */
public class CustomSerializingV1 {
    public String simplePublic;
    private final String simpleGetter;
    private final Optional<String> optionalValue;
    private final List<String> listOfStrings;
    private final List<Widget> listOfWidgets;
    private final List<Lamp> listOfLamps;
    private final Widget nestedWidget;
    private final Color backgroundColor;

    /**
     * Private constructor for use by deserialization tools only. This leaves the
     * object in an invalid state.
     */
    private CustomSerializingV1() {
        this.simpleGetter = null;
        this.optionalValue = null;
        this.listOfStrings = null;
        this.listOfWidgets = null;
        this.listOfLamps = null;
        this.nestedWidget = null;
        this.backgroundColor = null;
    }

    /** All-fields constructor. */
    public CustomSerializingV1(
            String simplePublic,
            String simpleGetter,
            Optional<String> optionalValue,
            List<String> listOfStrings,
            List<Widget> listOfWidgets,
            List<Lamp> listOfLamps,
            Widget nestedWidget,
            Color backgroundColor
    ) {
        this.simplePublic = simplePublic;
        this.simpleGetter = simpleGetter;
        this.optionalValue = optionalValue;
        this.listOfStrings = listOfStrings;
        this.listOfWidgets = listOfWidgets;
        this.listOfLamps = listOfLamps;
        this.nestedWidget = nestedWidget;
        this.backgroundColor = backgroundColor;
    }

    /** Getter for private field. */
    public String getSimpleGetter() {
        return simpleGetter;
    }

    /** Getter for a field. */
    public Optional<String> getOptionalValue() {
        return optionalValue;
    }

    /** Getter for a field. */
    public List<String> getListOfStrings() {
        return listOfStrings;
    }

    /** Getter. */
    public List<Widget> getListOfWidgets() {
        return listOfWidgets;
    }

    /** Getter. */
    public List<Lamp> getListOfLamps() {
        return listOfLamps;
    }

    /** Getter. */
    public Widget getNestedWidget() {
        return nestedWidget;
    }

    /** Getter. */
    public Color getBackgroundColor() {
        return backgroundColor;
    }
}
