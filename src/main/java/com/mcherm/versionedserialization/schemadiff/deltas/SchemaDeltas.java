package com.mcherm.versionedserialization.schemadiff.deltas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** An object to contain the differences between two schemas. */
public class SchemaDeltas {
    private final List<Alteration> deltas;

    public SchemaDeltas() {
        this.deltas = new ArrayList<>();
    }

    public void addAlteration(final Alteration alteration) {
        this.deltas.add(alteration);
    }

    public List<Alteration> getDeltas() {
        return Collections.unmodifiableList(deltas);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SchemaDeltas that)) return false;
        return Objects.equals(deltas, that.deltas);
    }

    @Override
    public int hashCode() {
        return deltas.hashCode();
    }

    @Override
    public String toString() {
        return deltas.stream().map(Object::toString).collect(Collectors.joining("\n")) + "\n";
    }
}
