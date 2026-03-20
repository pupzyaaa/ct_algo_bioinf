package graphDeBruijn;

// общий класс для хранения прочтений
public class SequenceRecord {
    public final String id;
    public final String seq;
    public final String qual; // null for FASTA

    public SequenceRecord(String id, String seq, String qual) {
        this.id = id;
        this.seq = seq;
        this.qual = qual;
    }
}
