package fastq;

import java.io.*;
import java.util.*;

public class FastqParser {

    private int gcSum = 0;
    private int phredSum = 0;
    private int phredCount = 0;

    public void parse(String filename, List<Integer> originalLengths, List<Integer> customTrimmedLengths) throws IOException {
        List<FastqRecord> records = readRecords(filename);
        parseRecords(records, originalLengths, customTrimmedLengths);
    }

    public void parseRecords(List<FastqRecord> records, List<Integer> originalLengths, List<Integer> customTrimmedLengths) {
        for (FastqRecord r : records) {
            String sequence = r.seq();
            String quality = r.qual();

            originalLengths.add(sequence.length()); // храним длины прочтений
            gcSum += countGC(sequence); // накапливаем GC состав

            // сразу считаем среднее значение (сумма / количество) качества Phred для позиции 10.
            if (quality.length() >= 10) {
                phredSum += quality.charAt(9) - 33;
                phredCount++;
            }

            // Делаем тримминг
            String[] trimmed = CustomTrimmer.trim(sequence, quality);

            // и сохраняем длину обработанного прочтения
            if (!trimmed[0].isEmpty())
                customTrimmedLengths.add(trimmed[0].length());
        }
    }

    public static List<FastqRecord> readRecords(String filename) throws IOException {
        List<FastqRecord> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String header;
            while ((header = br.readLine()) != null) {
                if (header.isEmpty())
                    continue;

                String seq = br.readLine();
                br.readLine();
                String qual = br.readLine();

                records.add(new FastqRecord(header.substring(1).trim(), seq.trim(), qual.trim()));
            }
        }
        return records;
    }

    // метод для подсчета G и C из последовательности
    private int countGC(String seq) {
        int count = 0;
        for (char c : seq.toCharArray()) {
            if (c == 'G' || c == 'C') count++;
        }
        return count;
    }

    // метод для определения GC состава
    public double getGcContent(List<Integer> lengths) {
        if (lengths.isEmpty())
            return 0.0;
        return Math.round((gcSum * 100.0 / lengths.stream().mapToInt(i -> i).sum()) * 100.0) / 100.0;
    }

    // метод для определения качества по шкале Phred для 10 символа
    public int getPhred10() {
        if (phredCount == 0) return 0;
        return Math.round((float) phredSum / phredCount);
    }
}
