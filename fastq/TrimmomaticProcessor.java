import java.io.*;
import java.util.*;

public class TrimmomaticProcessor {

    // обрабатываем файл, полученный с помощью Trimmomatic
    public static void process(String trimmedFilePath, List<Integer> originalLengths) throws IOException {

        List<Integer> trimmedLengths = new ArrayList<>(); // храним обрезанные длины прочтений

        try (BufferedReader file = new BufferedReader(new FileReader(trimmedFilePath))) {
            while (file.readLine() != null) { // читаем данные четверками, нужна только 2 строка - обрезанное прочтение
                String seq = file.readLine();
                file.readLine();
                file.readLine();

                trimmedLengths.add(seq.length());
            }
        }

        // Считаем разницу, определяем сколько обрезано
        int trimmedReads = originalLengths.size() - trimmedLengths.size();

        // Выводим статистику
        System.out.println("\nРезультаты Trimmomatic:");
        System.out.println("Число прочтений подвергшихся триммингу: " + trimmedReads);
        System.out.println("Минимальная длина равна: " + Collections.min(trimmedLengths));
        System.out.println("Средняя длина равна: " +
                Math.round(trimmedLengths
                .stream()
                .mapToInt(i -> i)
                .average()
                .orElse(0)));
        System.out.println("Максимальная длина равна: " + Collections.max(trimmedLengths));

        // тримминг по длине 60
        long filtered = trimmedLengths
                .stream()
                .filter(l -> l >= 60)
                .count();
        System.out.println("Число прочтений после фильтра длины 60: " + filtered);
    }
}