import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {

        List<Integer> originalLengths = new ArrayList<>(); // оригинальные прочтения
        List<Integer> customTrimmedLengths = new ArrayList<>(); // после тримминга

        // Парсим файл, собираем нужные данные
        Parser parser = new Parser();
        parser.parse("fastq/reads.fastq.txt", originalLengths, customTrimmedLengths);

        // Выводим статистику
        System.out.println("Общее число прочтений в файле равно: " + originalLengths.size());
        System.out.println("Минимальная длина прочтения равна: " + Collections.min(originalLengths));
        System.out.println("Средняя длина прочтения равна: " +
                Math.round(originalLengths
                        .stream()
                        .mapToInt(i -> i)
                        .average()
                        .orElse(0)));
        System.out.println("Максимальная длина прочтения равна: " + Collections.max(originalLengths));
        System.out.println("GC состав: " + parser.getGcContent(originalLengths));
        System.out.println("Средний Phred для позиции 10: " + parser.getPhred10());

        // Выводим результаты Trimmomatic
        TrimmomaticProcessor.process("fastq/trimmed.fastq", originalLengths);
        System.out.println();

        // Выводим результаты кастомного тримминга
        int trimmedReads = originalLengths.size() - customTrimmedLengths.size();
        System.out.println("\nРезультаты кастомного тримминга:");
        System.out.println("Число прочтений подвергшихся триммингу: " + trimmedReads);
        System.out.println("Минимальная длина равна: " + Collections.min(customTrimmedLengths));
        System.out.println("Средняя длина равна: " +
                Math.round(customTrimmedLengths
                        .stream()
                        .mapToInt(i -> i)
                        .average()
                        .orElse(0)));
        System.out.println("Максимальная длина равна: " + Collections.max(customTrimmedLengths));

        // тримминг по длине 60
        long filteredCustom = customTrimmedLengths
                .stream()
                .filter(l -> l >= 60)
                .count();
        System.out.println("Число прочтений после фильтра длины: 60 " + filteredCustom);
    }
}