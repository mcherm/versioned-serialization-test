package com.mcherm.versionedserialization.schemadiff.deltas;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** Data object to contain the differences between two schemas. */
public class SchemaDeltas {
    private final List<Add> adds;
    private final List<Drop> drops;
    private final List<Change> changes;

    public SchemaDeltas() {
        this.adds = new ArrayList<>();
        this.drops = new ArrayList<>();
        this.changes = new ArrayList<>();
    }

    public List<Add> getAdds() {
        return adds;
    }

    public List<Drop> getDrops() {
        return drops;
    }

    public List<Change> getChanges() {
        return changes;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SchemaDeltas that)) return false;
        return Objects.equals(adds, that.adds) && Objects.equals(drops, that.drops) && Objects.equals(changes, that.changes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(adds, drops, changes);
    }

    @Override
    public String toString() {
        List<Alteration> alterations = new ArrayList<>();
        alterations.addAll(adds);
        alterations.addAll(drops);
        alterations.addAll(changes);
        return alterations.stream().map(Object::toString).collect(Collectors.joining("\n")) + "\n";
    }
}
