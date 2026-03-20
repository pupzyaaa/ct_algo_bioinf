package graphDeBruijn;

import fastq.*;
import fasta.*;

import java.io.*;
import java.util.*;


public class SequenceReader {

    public static List<SequenceRecord> read(String path) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String first = br.readLine();
            if (first == null)
                return Collections.emptyList();

            // определяем тип для чтения
            if (first.charAt(0) == '>') {
                return readFasta(path);
            } else if (first.charAt(0) == '@') {
                return readFastq(path);
            } else {
                throw new IOException("Unknown format: expected '>' or '@' at start");
            }
        }
    }

    // использует парсер Fasta и записывает в массив SequenceRecord
    private static List<SequenceRecord> readFasta(String path) throws IOException {
        List<FastaRecord> recs = FastaParser.readRecords(path);
        List<SequenceRecord> out = new ArrayList<>(recs.size());
        for (FastaRecord r : recs) {
            out.add(new SequenceRecord(r.name(), r.seq(), null));
        }
        return out;
    }

    // использует парсер Fastq и записывает в массив SequenceRecord
    public static List<SequenceRecord> readFastq(String path) throws IOException {
        List<FastqRecord> recs = FastqParser.readRecords(path);
        List<SequenceRecord> out = new ArrayList<>(recs.size());
        for (FastqRecord r : recs) {
            out.add(new SequenceRecord(r.name(), r.seq(), r.qual()));
        }
        return out;
    }
}
