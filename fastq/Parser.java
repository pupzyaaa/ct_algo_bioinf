import java.io.*;
import java.util.*;

public class Parser {

    private int gcSum = 0;
    private int phredSum = 0;
    private int phredCount = 0;

    public void parse(String filename, List<Integer> originalLengths, List<Integer> customTrimmedLengths) throws IOException {

        try (BufferedReader file = new BufferedReader(new FileReader(filename))) {

            while (file.readLine() != null) { // Читаем из файла четверками: имя (пока не null), прочтение, коммент и строка качества
                String sequence = file.readLine();
                file.readLine();
                String quality = file.readLine();

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