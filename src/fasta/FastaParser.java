package fasta;

import java.io.*;
import java.util.*;

public class FastaParser {

    public static List<FastaRecord> readRecords(String path) throws IOException {

        List<FastaRecord> records = new ArrayList<>();
        // создаём список, куда будем складывать все прочитанные записи (header + sequence)

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            String header = null;
            StringBuilder seq = new StringBuilder(); // будем накапливать последовательность


            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;
                if (line.startsWith(">")) { // если строка начинается с '>' — это новый заголовок
                    // если у нас уже был предыдущий header значит предыдущая последовательность закончилась
                    if (header != null) {
                        // сохраняем предыдущую запись
                        records.add(new FastaRecord(header, seq.toString()));
                    }

                    header = line.substring(1).trim(); // новое имя
                    seq.setLength(0); // очищаем StringBuilder для новой последовательности

                } else { // иначе это часть последовательности
                    seq.append(line);
                }
            }

            // сохраним последнюю запись
            if (header != null) {
                records.add(new FastaRecord(header, seq.toString()));
            }
        }

        // возвращаем список всех последовательностей из файла
        return records;
    }
}