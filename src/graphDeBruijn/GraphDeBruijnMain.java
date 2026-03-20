package graphDeBruijn;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class GraphDeBruijnMain {

    public static void main(String[] args) throws IOException {

        String refFile = "src/graphDeBruijn/ecoli_1k.fna";   // файл с референсным геномом
        String readsFile = "src/graphDeBruijn/ecoli_reads.fastq"; // файл с ридами
        String outDir = "src/graphDeBruijn/results"; // папка для результатов
        List<Integer> kValues = Arrays.asList(5, 10, 20, 30, 50); // разные k для графов

        Files.createDirectories(Paths.get(outDir)); // создаём выходную папку

        // Читаем референс
        List<SequenceRecord> refRecords = SequenceReader.read(refFile);
        if (refRecords.isEmpty()) {
            System.out.println("Reference genome file is empty");
            return;
        }

        System.out.println("Building graphs for reference genome...");
        for (int k : kValues) {
            DeBruijnGraph g = new DeBruijnGraph(k);
            for (SequenceRecord r : refRecords) {
                g.processSequence(r.seq); // добавляем последовательности в граф
            }

            CompressedGraph cg = CompressedGraph.compress(g); // сжимаем граф

            String gfaPath = Paths.get(outDir, String.format("ref_k%d.gfa", k)).toString();
            GraphWriter.writeGfa(cg, gfaPath); // сохраняем граф в GFA

            System.out.printf("Reference graph for k=%d saved: %s%n", k, gfaPath);
        }

        // Читаем прочтения fastq
        List<SequenceRecord> readRecords = SequenceReader.read(readsFile);
        if (readRecords.isEmpty()) {
            System.out.println("Reads file is empty");
            return;
        }

        System.out.println("Building graphs for reads...");
        for (int k : kValues) {
            DeBruijnGraph g = new DeBruijnGraph(k);
            for (SequenceRecord r : readRecords) {
                g.processSequence(r.seq); // строим граф
            }

            g.removeLowCoverageEdges(2); // убираем слабые рёбра
            g.removeTips(); // убираем тупики

            CompressedGraph cg = CompressedGraph.compress(g); // сжатие графа

            String gfaPath = Paths.get(outDir, String.format("reads_k%d_clean.gfa", k)).toString();
            String contigsPath = Paths.get(outDir, String.format("reads_k%d_contigs.fasta", k)).toString();

            GraphWriter.writeGfa(cg, gfaPath); // сохраняем граф
            GraphWriter.writeContigsFasta(cg.edges(), contigsPath); // сохраняем

            System.out.printf("Reads graph for k=%d saved: %s, contigs: %s%n", k, gfaPath, contigsPath);
        }

    }
}