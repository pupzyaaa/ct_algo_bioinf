public class CustomTrimmer {

    public static String[] trim(String sequence, String quality) {

        // Реализуем тримминг с параметрами: ширина скользящего окна 5, качество 30
        int window = 5;
        int qualityLevel = 30;
        int totalWindowQuality = qualityLevel * window;

        // собираем массив phred score для всех символов прочтения
        int[] phreds = new int[quality.length()];
        for (int i = 0; i < quality.length(); i++) {
            phreds[i] = quality.charAt(i) - 33;
        }

        // Считаем сумму качеств в окне
        int total = 0;
        for (int i = 0; i < window; i++)
            total += phreds[i];

        // Среднее качество должно быть больше
        if (total <= totalWindowQuality)
            return new String[]{"", ""};

        // ищем длину прочтения для тримминга, изначально - вся строка
        int goodLength = phreds.length;

        // двигаем окно и пересчитываем сумму качества
        for (int i = 0; i < phreds.length - window; i++) {
            total = total - phreds[i] + phreds[i + window];
            // если нашли плохое окно, то останавливаемся и сохраняем длину
            if (total <= totalWindowQuality) {
                goodLength = i + window;
                break;
            }
        }

        // обрезать будем по последнему символу с подходящим качеством
        int i = goodLength;
        while (i > 1 && phreds[i - 1] <= qualityLevel) {
            i--;
        }

        // возвращаем обрезанные обработанные прочтения
        return new String[]{sequence.substring(0, i), quality.substring(0, i)};
    }
}