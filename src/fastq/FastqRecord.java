package fastq;

// Класс для хранения Fastq записей и полями имени, прочтения и качетсва
public record FastqRecord(String name, String seq, String qual) {
}
