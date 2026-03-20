package graphDeBruijn;

import java.io.*;
import java.util.*;

public class GraphWriter {

    // Записывает список сжатых рёбер как contigs в FASTA
    public static void writeContigsFasta(List<CompressedGraph.CEdge> edges, String path) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            int idx = 1;
            for (CompressedGraph.CEdge e : edges) {
                // Заголовок FASTA с длиной, средним покрытием и количеством k-меров
                String header = String.format(">contig_%d len=%d cov=%.2f kmerCount=%d",
                        idx++, e.seq().length(), e.avgCoverage(), e.kmerCount());
                bw.write(header);
                bw.newLine();
                // Пишем последовательность с переносом строк каждые 80 символов
                writeWrapped(bw, e.seq(), 80);
            }
        }
    }

    // Записывает граф в формате GFA (Graphical Fragment Assembly)
    public static void writeGfa(CompressedGraph g, String path) throws IOException {
        List<CompressedGraph.CEdge> edges = g.edges();
        Map<CompressedGraph.CEdge, String> ids = new HashMap<>();
        for (int i = 0; i < edges.size(); i++) {
            ids.put(edges.get(i), "E" + (i + 1)); // каждому ребру даём уникальный ID
        }

        // Индексы рёбер по началу и концу для построения связей
        Map<String, List<CompressedGraph.CEdge>> startMap = new HashMap<>();
        Map<String, List<CompressedGraph.CEdge>> endMap = new HashMap<>();
        for (CompressedGraph.CEdge e : edges) {
            startMap.computeIfAbsent(e.from(), k -> new ArrayList<>()).add(e);
            endMap.computeIfAbsent(e.to(), k -> new ArrayList<>()).add(e);
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            bw.write("H\tVN:Z:1.0"); // заголовок GFA
            bw.newLine();
            for (CompressedGraph.CEdge e : edges) {
                String id = ids.get(e);
                // Строка сегмента с последовательностью, k-мер счетом и средним покрытием
                String line = String.format(Locale.ROOT,
                        "S\t%s\t%s\tKC:i:%d\tRC:f:%.2f",
                        id, e.seq(), e.kmerCount(), e.avgCoverage());
                bw.write(line);
                bw.newLine();
            }

            int k = g.getK();
            // Строки связей (links) между сегментами
            for (CompressedGraph.CEdge e1 : edges) {
                List<CompressedGraph.CEdge> outs = startMap.get(e1.to());
                if (outs == null) continue;
                for (CompressedGraph.CEdge e2 : outs) {
                    String line = String.format("L\t%s\t+\t%s\t+\t%dM",
                            ids.get(e1), ids.get(e2), k);
                    bw.write(line);
                    bw.newLine();
                }
            }
        }
    }

    // Вспомогательный метод для переноса длинных последовательностей в FASTA
    private static void writeWrapped(BufferedWriter bw, String seq, int width) throws IOException {
        int i = 0;
        while (i < seq.length()) {
            int end = Math.min(seq.length(), i + width);
            bw.write(seq, i, end - i);
            bw.newLine();
            i = end;
        }
    }
}